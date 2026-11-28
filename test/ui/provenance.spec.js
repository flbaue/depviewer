// Provenance in the viewer (spec/wiki/functional-requirements/FR-011-record-the-provenance-of-every-language-and-module.md).
// build/fixture.html: test/fixture (Java and Kotlin); build/fixture-ts.html: test/fixture-ts.
const { test, expect } = require('@playwright/test');
const path = require('path');

const build = process.env.DEPVIEWER_BUILD || path.resolve(__dirname, '../../build');
const page_ = name => 'file://' + path.join(build, name);
const row = (page, name) => page.locator('.row', { has: page.locator('.nm', { hasText: new RegExp('^' + name.replace(/\./g, '\\.') + '$') }) });

test('FR-011: the header lists each language and marks name-based ones as approximate', async ({ page }) => {
  await page.goto(page_('fixture.html'));
  const langs = page.locator('#langs');
  await expect(langs).toHaveText('Java · Kotlin (approximate)');
  await expect(langs.locator('.approx')).toHaveText('Kotlin (approximate)');
  await expect(langs).toHaveAttribute('title', /Kotlin is resolved by name/);
});

test('FR-011: a module row shows tool, version, source and extractor', async ({ page }) => {
  await page.goto(page_('fixture.html'));
  await page.locator('#viewSel').selectOption('');   // the physical structure: one row per module
  await row(page, 'fixture').click();
  const kv = page.locator('#pbody .kv');
  await expect(kv).toContainText(/Java\s*javac \d[\w.+-]* · from the PATH · extractor depviewer-jvm-extractor \d/);
  await expect(kv).toContainText(/Kotlin\s*depviewer-kotlin \d[\w.]* · pinned · extractor depviewer-jvm-extractor \d[\w.]* · by name/);
});

test('FR-011: a TypeScript module shows the chosen TypeScript', async ({ page }) => {
  await page.goto(page_('fixture-ts.html'));
  await expect(page.locator('#langs')).toHaveText('TypeScript/JavaScript');
  await row(page, 'shop-web').click();
  await expect(page.locator('#pbody .kv')).toContainText(/TypeScript\/JavaScript\s*typescript \d+\.\d+\.\d+ · (override|from the project|from the PATH|pinned) · extractor depviewer-typescript-extractor 1\.0\.0/);
});
