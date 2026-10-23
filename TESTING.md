# Dayerize 0.4.1 testing APK

[Download APK directly — 98 MB](https://raw.githubusercontent.com/YONG-BEGINNER/dayerize/main/downloads/Dayerize-0.4.1-arm64.apk)

Android 8.0+ · ARM64 · version code 7 · same signing certificate as previous builds. Back up your data before updating.

Keeps the existing minimalist Alloy layout and 100 item identities / 200 original icons. Adds one **Scan receipt** action in new asset entry. Recognition runs on-device, without an account or paid API. Review price, currency, date and retailer before applying to the unsaved draft. Raw text is hidden under **Show details**.

[Preview the receipt review screen](previews/0.4.1-receipt-review.png) · [3D icons](previews/0.4.0-3d.png) · [Cute 2D icons](previews/0.4.0-cute2d.png) · [Scope, testing and limitations](ENHANCEMENTS-0.4.1.md)

79 host tests passed, plus actual native OCR on an Android 15 emulator. Lint: zero errors, 28 warnings. Physical-phone validation and real-world receipt accuracy remain outstanding. Google login/sync, photo classification and runtime icon generation are not enabled. All existing offline features remain available. Encrypted backups retain the 8 MiB limit.

APK SHA-256: `47b0a9eeb0fe8e8213ae69a34fa90727cce0680f3be7e8827fd4b4129f507b31`
