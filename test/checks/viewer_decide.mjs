// Compares the viewer's decide() with the scanner's Architecture.resolve() on the random views DecideCheck.java
// wrote (spec/wiki/functional-requirements/FR-041-show-pattern-assignments-as-by-pattern.md): for every module,
// package, file and external, the group the viewer shows must be the group check uses.
// Usage: node viewer_decide.mjs <viewer.html> <model.json> <cases.json>
import { readFileSync } from 'node:fs';

const [html, model, cases] = process.argv.slice(2).map(f => readFileSync(f, 'utf8'));

// decide() taken verbatim from the viewer, with the one helper it uses.
const start = html.indexOf('function decide(');
const end = html.indexOf('\n}\n', start);
if (start < 0 || end < 0) { console.error('FAIL viewer_decide: no decide() in viewer.html'); process.exit(1); }
const hasOwn = (o, k) => k != null && Object.prototype.hasOwnProperty.call(o, k);
const decide = new Function('hasOwn', html.slice(start, end + 2) + '\nreturn decide;')(hasOwn);

// The original elements as the viewer's load() builds them: key and parent.
const data = JSON.parse(model);
const nodes = data.nodes.map(d => ({ n: d.n, k: d.k, q: d.q || d.n, path: d.path || '', op: null }));
data.nodes.forEach((d, i) => { if (d.p != null) nodes[i].op = nodes[d.p]; });
for (const n of nodes) {
  n.mod = n.op ? n.op.mod : n.n;
  n.okey = n.k === 'module' ? 'R:' + n.n : n.k === 'package' ? 'P:' + n.mod + ':' + n.q : n.k === 'file' ? 'F:' + n.mod + ':' + n.path
    : n.k === 'ext-root' ? 'X' : n.k === 'ext-package' ? 'XP:' + n.q : n.k === 'ext-type' ? 'XT:' + n.q : null;
}
const byKey = new Map(nodes.filter(n => n.okey).map(n => [n.okey, n]));

let compared = 0;
const failures = [];
JSON.parse(cases).forEach((c, ci) => {
  const v = c.view;
  v.assign = v.assign || {}; v.matched = v.matched || {};
  const ids = new Set(v.groups.map(g => g.id));   // build() shows an unknown group id as ungrouped
  for (const [key, want] of Object.entries(c.expected)) {
    const d = decide(v, byKey.get(key)).gid;
    const got = d && ids.has(d) ? d : null;
    compared++;
    if (got !== want && failures.length < 10) failures.push(`case ${ci}: ${key}: viewer ${got}, check ${want}\n    ${JSON.stringify(v)}`);
  }
});
if (failures.length) { console.error('FAIL viewer_decide:\n  ' + failures.join('\n  ')); process.exit(1); }
console.log(`ok (viewer and check agree on ${compared} elements in ${JSON.parse(cases).length} random views)`);
