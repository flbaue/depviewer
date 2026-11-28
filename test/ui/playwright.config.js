// UI scenarios for the generated viewer. scripts/check.sh builds the pages in build/ and runs them.
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: '.',
  timeout: 30000,
  reporter: 'line',
  use: { browserName: 'chromium', trace: 'off', viewport: { width: 1400, height: 1000 } },
});
