"""JSON report checks for `depviewer check` on the fixture (spec/wiki/functional-requirements/FR-036-provide-the-check-command-and-its-exit-codes.md,
FR-038-report-violations-as-versioned-json.md).

Usage: rules.py <dir.json> <model.json> <moved.json> <kotlin.json>
  dir.json     check of test/fixture with test/fixture-architecture.json
  model.json   the same check on build/fixture.json (--model)
  moved.json   the same check on a copy with a blank line above Order.total()
  kotlin.json  a forbid rule from the Kotlin notify package to the domain
"""
import json
import sys

failures = []


def check(cond, msg):
    if not cond:
        failures.append(msg)


def load(path):
    return json.load(open(path, encoding="utf-8"))


def layering(report):
    vs = [v for v in report["violations"] if v["rule"] == "layering"]
    check(len(vs) == 1, f"expected one layering violation, got {len(vs)}")
    return vs[0] if vs else {}


d, m, moved, kt = (load(p) for p in sys.argv[1:5])

# FR-038: the format.
check(d.get("format") == 1, "format must be 1")
check(d["model"]["title"] == "fixture" and d["model"].get("generated"), "model title and date")
# The model's analysis (FR-011), null while the scanner does not record it.
check("analysis" in d["model"] and d["model"]["analysis"] is None, f"model analysis: {d['model']}")
check(set(d["summary"]) == {"error", "warning", "baselined", "resolved", "overlaps"}, f"summary keys: {sorted(d['summary'])}")
check(d["summary"]["error"] == 3, f"expected 3 errors, got {d['summary']['error']}")
v = layering(d)
for k in ("rule", "view", "severity", "kind", "from", "to", "lines", "count", "exact", "baselined", "fingerprint"):
    check(k in v, f"violation lacks {k}")
check(v.get("view") == "layers" and v.get("severity") == "error" and v.get("kind") == "layers", f"rule fields: {v}")
check(v.get("from") == {"key": "T:fixture:com.example.domain.Order", "q": "com.example.domain.Order.total()",
                        "path": "src/main/java/com/example/domain/Order.java", "group": "dom"}, f"from: {v.get('from')}")
check(v.get("to", {}).get("q") == "com.example.service.PriceCalculator.gross(double)" and v["to"].get("group") == "svc", f"to: {v.get('to')}")
# PriceCalculator.gross(net) is a call to the method and a use of the type: one violation, at the method.
check(v.get("lines") == [25] and v.get("count") == 2, f"lines/count: {v.get('lines')} {v.get('count')}")
check(v.get("refs") == [{"kind": "call", "lines": [25], "count": 1},
                        {"kind": "uses", "to": "com.example.service.PriceCalculator", "lines": [25], "count": 1}], f"refs: {v.get('refs')}")
check(v.get("exact") is True and v.get("baselined") is False, "exact and baselined")
cyc = [x for x in d["violations"] if x["rule"] == "no-pkg-cycles"]
check(len(cyc) == 1 and [x["q"] for x in cyc[0].get("members", [])] == ["com.example.domain", "com.example.service"],
      f"cycle members: {cyc}")

# FR-036: --model gives the same report as scanning the directory, except the date.
for r in (d, m):
    r["model"].pop("generated", None)
check(d == m, "check --model differs from checking the project directory")

# FR-038: the fingerprint survives a blank line above Order.total().
mv = layering(moved)
check(mv.get("lines") == [26], f"the copy should move the line to 26, got {mv.get('lines')}")
check(mv.get("fingerprint") == v.get("fingerprint"), f"fingerprint changed: {v.get('fingerprint')} -> {mv.get('fingerprint')}")
check(v.get("fingerprint") == "layers|layering|T:fixture:com.example.domain.Order#total()|T:fixture:com.example.service.PriceCalculator#gross(double)",
      f"fingerprint: {v.get('fingerprint')}")

# FR-038: Kotlin violations are name-based.
kv = [x for x in kt["violations"] if x["rule"] == "notify-no-domain"]
check(kv and all(x["exact"] is False and x["from"]["path"].endswith(".kt") for x in kv), f"Kotlin violations must have exact false: {kv}")

if failures:
    print("FAIL rules.py:\n  " + "\n  ".join(failures))
    sys.exit(1)
print("ok (check JSON report)")
