# Dayerize 0.4.2 compact interface

Home, analytics and settings use tighter spacing, smaller artwork/header/chart dimensions and a slimmer navigation bar while retaining 48dp minimum actions. All four ownership states appear in the home and analytics counts. Home has a separate full-width scrollable status filter row, including Scrapped / 已报废, and a category selector. Decimal precision uses inline 0–3 chips with amber selection and existing persistence. Empty analytics replaces the category chart with a short prompt.

Large-text navigation now occupies Scaffold's measured bottom inset instead of overlapping content. The category selector uses its own full row above 1.3× text scaling. Financial logic, storage schemas, icons, and explicit compact/large density settings remain unchanged; comfortable spacing is tighter.

## Validation

- 84 host tests passed: 43 core, 1 imaging, 40 Android; no failures, errors or skips.
- New regressions verified RED→GREEN for Scrapped summary, inline decimal choice, category legibility at 2×, and final settings content clearing navigation at 2×. Chinese Scrapped filtering and stored decimal value/selected-chip state also pass.
- Existing category-name collision tests now target the modal's category row explicitly because home also has a category selector.
- Full lint: 0 errors, 28 warnings. APK build and ARM64 signature verification passed. Existing Room/Coil shutdown messages appear in test output without failed tests.
- Native host screenshots inspected for home, analytics, settings, Chinese filtering and enlarged settings text. Generated screenshots retained outside tracked screenshot files under /workspace/setup/test-screenshots and compact-previews.
- Independent reviewer found two Important large-text risks; both reproduced with tests and fixed. No Critical findings.

## Decisions and remaining validation

Used existing Compose architecture and existing cloud checkout. No extra design approval, worktree, source push or publication was needed for the user's explicit implementation request. Changes remain in the working tree. Standard comfortable spacing changes also apply to shared panels; explicit compact and large modes retain their original values.

Deferred minor: no dedicated empty-analytics automated regression. Physical-device testing and full process-restart decimal preference testing remain outstanding; current persistence coverage checks the stored settings and UI selection.

## Testing artifact

Version 0.4.2 / code 8, Android 8.0+, ARM64:
/workspace/downloads/Dayerize-0.4.2-arm64-debug.apk

SHA-256: 5224b08375c3a2fba491697b580e8f01e33253c4758f1d50babea578b15bcca8

This artifact uses the environment's debug certificate, which differs from the previously distributed APK. It cannot directly upgrade that installation. Do not uninstall an existing app with valuable data to work around signing; obtain an exported backup and the original signing key before arranging an upgrade.
