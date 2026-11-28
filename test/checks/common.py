"""Shared helpers for the model checks in this folder (one script per fixture project).

Each check names one fact about a fixture's sources. When you change the scanner on purpose,
update the expectation in the same commit (and say why in the commit message).
"""
import json
import sys


class Checker:
    def __init__(self, path):
        self.failures = []
        self.data = json.load(open(path, encoding="utf-8"))
        self.nodes, kinds = self.data["nodes"], self.data["edgeKinds"]
        self.by_q = {}
        for i, n in enumerate(self.nodes):
            self.by_q.setdefault(self.qname(n), i)
        self.edges = {(f, t, kinds[k]): (c, lines) for f, t, k, c, lines in self.data["edges"]}
        # node() returns -1 for a missing node, which indexes this placeholder, so checks like
        # nodes[node(q)].get("t") report the missing node instead of crashing.
        self.nodes.append({"n": "", "k": "", "p": None, "missing": True})

    @staticmethod
    def qname(n):
        return n.get("q", n.get("path", n["n"]))

    def analysis(self, language, modules, tool, exact, extractor):
        """The provenance entry of a language (spec FR-011), checked field by field."""
        entries = [a for a in self.data.get("analysis") or [] if a.get("language") == language]
        self.check(len(entries) == 1, f"analysis: expected one {language} entry, found {len(entries)}")
        a = entries[0] if entries else {}
        self.check(sorted(a.get("modules", [])) == sorted(modules), f"analysis {language}: modules {a.get('modules')}")
        self.check(a.get("tool") == tool, f"analysis {language}: tool {a.get('tool')}")
        self.check(isinstance(a.get("version"), str) and a["version"][:1].isdigit(), f"analysis {language}: version {a.get('version')}")
        self.check(a.get("source") in ("override", "project", "path", "pinned"), f"analysis {language}: source {a.get('source')}")
        self.check(a.get("exact") is exact, f"analysis {language}: exact should be {exact}")
        x = a.get("extractor") or {}
        self.check(x.get("name") == extractor and isinstance(x.get("version"), str), f"analysis {language}: extractor {x}")
        return a

    def check(self, cond, msg):
        if not cond:
            self.failures.append(msg)

    def node(self, q, kind=None):
        i = self.by_q.get(q)
        self.check(i is not None, f"missing node {q}")
        if i is None:
            return -1
        if kind:
            self.check(self.nodes[i]["k"] == kind, f"{q}: expected kind {kind}, got {self.nodes[i]['k']}")
        return i

    def edge(self, src, dst, kind, present=True):
        f, t = self.by_q.get(src), self.by_q.get(dst)
        if f is None or t is None:
            self.check(not present, f"edge {src} -{kind}-> {dst}: endpoint missing")
            return
        found = (f, t, kind) in self.edges
        self.check(found == present, f"edge {src} -{kind}-> {dst} should {'exist' if present else 'not exist'}")
        if found and present:
            c, lines = self.edges[(f, t, kind)]
            self.check(c >= 1 and all(l > 0 for l in lines), f"edge {src} -{kind}-> {dst}: bad count/lines {c} {lines}")

    def parents_first(self):
        for i, n in enumerate(self.nodes[:-1]):
            if "p" in n:
                self.check(n["p"] < i, f"node {i} ({self.qname(n)}) appears before its parent")

    def finish(self, name):
        if self.failures:
            print(f"{name}: model check FAILED ({len(self.failures)}):")
            for f in self.failures:
                print("  - " + f)
            sys.exit(1)
        print(f"{name}: model check ok ({len(self.nodes)} nodes, {len(self.edges)} edges)")
