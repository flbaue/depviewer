// Views from an architecture file in the viewer (spec/wiki/functional-requirements/FR-040-show-rule-violations-in-the-viewer.md).
// build/fixture-arch.html: test/fixture with --views test/fixture-architecture.json.
// build/fixture-forbid.html: the same with a forbid rule from app to infra, a downward arc.
const { test, expect } = require('@playwright/test');
const fs = require('fs');
const path = require('path');

const build = process.env.DEPVIEWER_BUILD || path.resolve(__dirname, '../../build');
const page_ = name => 'file://' + path.join(build, name);
// Selects a row afresh: clicking a selected row would deselect it.
const pick = async (page, name) => { await page.keyboard.press('Escape'); await row(page, name).click(); };
const row = (page, name) => page.locator('.row', { has: page.locator('.nm', { hasText: new RegExp('^' + name.replace(/\./g, '\\.') + '$') }) });

// Clicks the arc whose tooltip is `from → to (count)`.
async function clickArc(page, from, to) {
  const i = await page.evaluate(([from, to]) => {
    for (const h of document.querySelectorAll('#arcs path.hit')) {
      h.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
      if (h.textContent.startsWith(`${from} → ${to} (`)) { h.dispatchEvent(new MouseEvent('click', { bubbles: true })); return +h.dataset.a; }
    }
    return -1;
  }, [from, to]);
  expect(i, `arc ${from} → ${to}`).toBeGreaterThanOrEqual(0);
  return page.locator(`#arcs path.arc[data-a="${i}"]`);
}

test('FR-041: a pattern assignment shows as by pattern, a drag overrides it, removing the override brings it back', async ({ page }) => {
  await page.goto(page_('fixture-arch.html'));
  const select = page.locator('#pbody select[data-assign]');
  await pick(page, 'OrderRepository.java');
  await expect(select).toHaveValue('dom');
  await expect(page.locator('#pbody .kv')).toContainText('by pattern');

  // Drag it onto an ungrouped row: a manual assignment to "no group".
  await row(page, 'OrderRepository.java').dragTo(page.locator('.row:not(.ing)').first());
  await pick(page, 'OrderRepository.java');
  await expect(select).toHaveValue('');
  await expect(page.locator('#pbody .kv')).toContainText('manual (overrides pattern)');

  // The override is kept in browser storage, and the embedded patterns still apply after a reload.
  await page.reload();
  await pick(page, 'OrderRepository.java');
  await expect(page.locator('#pbody .kv')).toContainText('manual (overrides pattern)');

  await page.locator('#pbody [data-unmanual]').click();
  await pick(page, 'OrderRepository.java');
  await expect(select).toHaveValue('dom');
  await expect(page.locator('#pbody .kv')).toContainText('by pattern');
});

test('FR-042: the arc Order → PriceCalculator lists layering, error, exact', async ({ page }) => {
  await page.goto(page_('fixture-arch.html'));
  await clickArc(page, 'Order.java', 'PriceCalculator.java');
  const v = page.locator('#pbody .viol');
  await expect(v.filter({ hasText: 'layering' })).toContainText('error');
  await expect(v.filter({ hasText: 'layering' })).toContainText('exact');
});

test('FR-042: a downward arc with an error violation is drawn as a violation', async ({ page }) => {
  await page.goto(page_('fixture-forbid.html'));
  const arc = await clickArc(page, 'Main.java', 'InMemoryOrderRepository.java');
  await expect(arc).toHaveClass(/\bviol\b/);
  await expect(page.locator('#pbody .viol').filter({ hasText: 'no-app-infra' })).toContainText('error');
});

test('FR-043: Export views writes match, rules and version back unchanged and no resolved assignments', async ({ page }) => {
  await page.goto(page_('fixture-arch.html'));
  await page.locator('#viewMenu summary').click();
  const [download] = await Promise.all([page.waitForEvent('download'), page.locator('#viewMenu button[data-m="export"]').click()]);
  const out = JSON.parse(fs.readFileSync(await download.path(), 'utf8'));
  const src = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../fixture-architecture.json'), 'utf8'));
  expect(out.version).toBe(src.version);
  const [v] = out.views, [s] = src.views;
  expect(v.rules).toEqual(s.rules);
  expect(v.groups.map(g => g.match)).toEqual(s.groups.map(g => g.match));
  expect(v.assign).toEqual(s.assign);
  expect(v).not.toHaveProperty('matched');
  expect(v).not.toHaveProperty('violations');
});
