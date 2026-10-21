# Dayerize 0.3.1 — everyday icons and fixes

User-requested follow-up to the approved Alloy design: fix both deferred 0.3.0 issues and expand everyday item coverage using the existing original 3D/cute 2D styles. No new architecture or cloud service is introduced.

## Fixes

The collection filter now carries nullable category values separately from localized labels. A category literally named All, 全部 or Semua is a real category; the all-categories selection has a null value. Reset restores the complete collection.

The photo workspace distinguishes a pending preview from a completed, failed preview using the request key. Apply stays disabled until the current source/outline preview is ready. A loading indicator replaces the previous transient error; genuine decode failures still show an error and retain the original. Preview decoding was extracted without changing behavior to allow tests to pause real decoding until a gate is released.

## Intended catalog batch

24 additional item identities, each in both original styles: tablet, earbuds, smartwatch, monitor, keyboard, mouse, portable speaker, handheld game console, power bank, camera lens, drone, television, air conditioner, refrigerator, washing machine, stick vacuum, air fryer, rice cooker, desk, office chair, suitcase, handbag, sunglasses and motorcycle. These are common-use selections, not a measured market ranking, and are generic designs rather than verified branded models.

Together with the prior 12 identities the verified packaged total is 36 identities / 72 icons. Completion counts and verification are recorded below. The 600-identity target and Google login/sync remain separate milestones.

## Artwork production and packaging

An early batch was rejected after visual inspection found incorrect subjects caused by a missing subject description in prompt assembly. Those files are excluded from the runtime manifest. Corrected prompts explicitly name each item and assert the description is present. Source paths and prompts are recorded for the accepted batch.

Original masters are retained outside the APK. Runtime PNG derivatives use a uniform 512×512 canvas, preserve transparency and aspect ratio, and retain safe fit scaling; they are not cropped. The export script records master/export checksums. This keeps offline selection and download size practical without adding a network dependency or deleting legacy artwork needed for existing backups.

## Verification evidence

- Category regression failed before the fix because an unrelated category remained visible.
- Both controlled delayed-preview tests failed before the fix because an error appeared before decoding completed.
- Nine focused filter/photo tests passed after the fixes, including real Activity configurations for all four supported locales and a gated replacement-preview test. A prior photo-test timeout during the red run did not recur in the focused green runs; final full-suite verification is still required.
- Catalog coverage test failed before expansion, as expected.

Final full-suite verification passed: **52 tests, 0 failures, 0 errors**. Android lint reports **0 errors and 18 warnings**. The catalog validator confirms **36 selectable identities / 72 original style exports**, totaling 13,978,502 bytes before APK packaging. Native host renders of both icon styles and the Home category were inspected, alongside 48/64/96px contact sheets on light/dark backgrounds. APK signature and public delivery checks are recorded below.

The new bitmap-alpha assertion initially failed under Robolectric legacy graphics despite PNG-level validation confirming transparency. Switching this decoding test to native graphics verified all 72 images through Android BitmapFactory. The final full test run succeeded in 56 seconds. Physical-device testing remains outstanding; test logs include existing Room resource-close warnings, not test failures.

## Independent review

The independent code review found no Critical or Important issues. Its one Minor recommendation—cover replacing a ready/pending preview—was addressed with a regression test that changes outline width twice, releases the obsolete request first, verifies Apply stays disabled, then checks the applied output has only the newest outline dimensions. It passed.

An initial localized test used a composition-only locale override, which did not reach the modal window. The test was corrected to use real Activity locale qualifiers; no product workaround was added.

## APK identity

Version 0.3.1 / code 5; application ID `app.dayerize`; ARM64; Android 8.0+. Fresh archive build and signature verification passed. Size: **92,997,483 bytes** (about 93 MB).

APK SHA-256: `3c98dd704452b4565e96855972d6f287538a996b99dbfb7f86eaa6d4e7d3600c`.

Signing certificate SHA-256: `8912651d3133725e41bf8fe0d34cd94b4b24c92fa58edc128915efcbffa06962`, unchanged from the previous testing builds. Source branch remains in the development workspace; APK delivery uses the previously authorized public repository route.
