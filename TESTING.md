# Dayerize 0.3.1 — everyday icons and fixes

[Download the ARM64 testing APK directly — 93 MB](https://raw.githubusercontent.com/YONG-BEGINNER/dayerize/main/downloads/Dayerize-0.3.1-arm64.apk)

Fixed the category named “All” filter issue and the false error shown while a photo preview loads. Preview changes now have regression coverage to ensure Apply uses the newest result.

Added **48 original icons**, bringing the catalog to **36 item types / 72 icons** in realistic 3D and cute 2D. New items include tablets, earbuds, smartwatches, monitors, keyboards, mice, speakers, game consoles, power banks, camera lenses, drones, televisions, air conditioners, refrigerators, washing machines, vacuum cleaners, air fryers, rice cookers, desks, office chairs, suitcases, handbags, sunglasses and motorcycles. All are bundled for offline selection with localized search and proportional rendering.

[Preview all 3D icons](previews/0.3.1-3d.png) · [Preview all cute 2D icons](previews/0.3.1-cute2d.png)

52 host tests pass; Android lint reports 0 errors and 18 warnings. Independent code review found no critical or important issues, and its suggested additional preview test was added and passed. Physical-device testing remains outstanding. [Review and verification notes](ALLOY-0.3.1-REVIEW.md).

Android 8.0+; ARM64; version code 5. Same signing identity as the previous testing builds. Back up local data before updating. These are generic item illustrations; the larger model-specific catalog and Google login/sync remain separate milestones. Source changes are retained in the development workspace; this delivery repository contains the APK, previews and notes.

APK SHA-256: `3c98dd704452b4565e96855972d6f287538a996b99dbfb7f86eaa6d4e7d3600c`
