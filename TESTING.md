# Dayerize 0.2.0 — redesigned testing build

[Download APK directly — ARM64, 78 MB](https://raw.githubusercontent.com/YONG-BEGINNER/dayerize/main/downloads/Dayerize-0.2.0-arm64.apk)

Open the APK on a modern ARM64 Android phone to install. No ZIP extraction is needed. Version code 2 uses the same application ID and debug signing certificate as the previous testing build; back up your local data before updating.

- Screenshot-inspired dark design with rounded bordered cards, lime accents and floating navigation.
- Wishlist with purchase conversion; wishes do not inflate owned totals.
- Grouped editor, wheel date picker, category sheets and 2,568 offline 3D/2D icon choices.
- Redesigned detail, analytics and settings; four languages and multiple currencies retained.
- Corrections for retirement/resale calculations, unsafe restore preferences and edit-mode regressions.

Verified: 30 automated tests passed, Android lint has no errors (16 warnings), APK signature checked, native host-rendered screen comparison including Chinese at 200% text size. These are host tests, not a completed physical-device test campaign. Camera uses thumbnail capture; use gallery import for high-resolution photos. On-device cutout quality and full backup interruption recovery still need device validation. No authentication or historical snapshot recorder is included.

The application source is retained in the development workspace; this delivery repository commit is not a reproducible source snapshot.

APK SHA-256: `62210bd10fe4e0b45df886f3eff1ae32a52c9c39041b243cf38e2bc994d51d4f`
