# Dayerize 0.3.0 — Alloy testing build

[Download the ARM64 APK directly — 103 MB](https://raw.githubusercontent.com/YONG-BEGINNER/dayerize/main/downloads/Dayerize-0.3.0-arm64.apk)

Graphite/amber Alloy interface, labeled navigation, compact asset cards, collection filtering/sorting, photo preview/outline controls, sale preview and backup status. Includes 12 original identities in realistic 3D and cute 2D (24 icons), with proportional rendering. Multi-currency accounting and four languages remain supported.

44 host tests pass; lint has 0 errors and 18 warnings. Physical-device testing remains outstanding. This is an early testing build: back up local data before updating. The larger model-specific catalog and Google login/sync are separate future milestones.

Known minor issues: filtering a category literally named “All” collides with the all-categories option; the photo preview may briefly show an error while loading. See [review and execution notes](ALLOY-REVIEW.md).

Android 8.0+; ARM64; version code 4. The signing certificate is unchanged. This delivery repository contains the APK and notes; source changes remain in the development workspace.

APK SHA-256: `c397ac46f9845495fe98347ff55a8ffd17fa6253f77e21bb4a6dfb22b617e362`
