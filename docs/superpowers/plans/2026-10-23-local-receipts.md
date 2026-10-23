# Local receipt assistance — continuation of approved enhancement roadmap

Base f5a1bf8, isolated branch work. User requested continued implementation. Scope: independently feasible free OCR; no Google dependency, no remote/private-data calls, no account requirement.

## Design and rulings

Use pinned maintained Tesseract4Android 4.9.0 source (Apache-2.0), native Tesseract 5.5.1, and bundled tessdata_fast models if build/recognition probes succeed. Keep recognition separate from deterministic receipt interpretation and existing editor persistence. Alternative ML Kit remains blocked by official-document access. No billed provider or server is introduced.

Review sheet in new owned-asset entry: select receipt image, select script, run locally, inspect raw recognized text and editable amount/currency/date/retailer, then explicitly apply to the unsaved draft. Receipt totals may contain multiple purchased items; explain this adjacent to amount. Do not invent model/name, exchange rates or verified status. Ambiguous date/currency suggestions are not silently chosen. Dismiss/cancel leaves editor unchanged. No source receipt or OCR text persists to the asset store or backup.

Ruling: treat this as the approved roadmap's receipt-scanning implementation slice; preserve existing editor/navigation rather than reopening product approval. Model-specific art, cloud sync and runtime generation remain separate unmet prerequisites.

## Tasks and verification

1. Native feasibility: pinned source/license/models, local toolchain, ABI compatibility; prove model recognizes synthetic receipt. If blocked, record facts and do not ship fake OCR.
2. Core parser RED→GREEN: totals vs subtotal/change, grouped decimals, ambiguous currency symbols and dates, multiple currencies, invalid dates, bounded input, no false-price date parsing.
3. Android engine: bounded image decode, EXIF orientation, model integrity/install, one scan at a time, cancellation/timeout and engine cleanup. UI test must show review changes draft only after explicit Apply and cancellation changes nothing. All four locales.
4. Full suite/lint/build; native recognition test where environment permits, explicit physical-device limitations. One independent branch review, fix Important/Critical issues with regressions. Package APK only with real implementation and test evidence; preserve signing identity and direct APK delivery.

Pre-flight: parser output is suggestions only, editor owns persistence. No DB schema or backup format change needed. Bundled models affect APK size: verify delivery size and preserve original artwork IDs/quality.

## Evidence

2026-10-23: developers.google.com Drive/ML Kit/FAQ requests denied by configured proxy (CONNECT 403). Environment has no configured outbound identities or secrets. Official Tesseract4Android tag 4.9.0 README and source accessible; NDK 27.2.12479018 and CMake are build prerequisites. No Google OAuth project/client available.

User steering: keep design simple and minimalist. One text action in existing new-asset editor; focused review fields, raw OCR text collapsed behind Show details. No extra navigation tab, banners or decorative controls.

Progress: core parser RED (missing API) → GREEN (40 core tests). Receipt review RED (missing API) → GREEN (corrected test harness to use registered MainActivity); image bounds/orientation RED (missing API) → GREEN. Native test RED (missing scanner) then compiled. Full host suite/lint/build succeeded before packaging optimization. Engine compiled for all four existing ABIs; bundled English model correctly recognized a synthetic receipt in host Tesseract. Emulator boot succeeded without KVM; real JNI test underway.

Ruling: losslessly transcode legacy compatibility PNG icons to WebP to offset bundled OCR size and retain direct APK delivery. Every decoded RGBA pixel is asserted identical and stable saved IDs resolve to the new extension; original PNGs retained in toolchain backup, new original artwork untouched. Resolver regression RED observed before migration.

Final review: independent reviewer found no Critical and three Important parser findings (negative sign loss; ALL footer misclassified as Albanian currency; total item count as purchase total). Added three regression tests; all three failed against original parser, then all 43 core tests passed after fixes. Whole final suite: 79 host tests passed, zero failures/errors/skips; lint zero errors / 28 warnings; APK assembled. No deferred reviewer findings. Native recognition passed on Android 15 x86_64 emulator after package precompilation avoided emulator startup ANR; native English OCR plus parser correctly produced MYR 106.00 / 2026-10-23. Physical-device and Chinese accuracy benchmarking remain outstanding. Receipt review screenshot visually inspected; minimalist collapsed details retained.

APK packaging incremental splitter failed once after replacing legacy files; retry with stacktrace succeeded without further source changes. Final full build then succeeded. Format-only legacy migration verified identical RGBA pixels for all 1,284 images and saved 15,703,337 bytes. Three new lint recommendations (platform EXIF, fixed-rate timer, KTX) recorded in 0.4.1 notes; no runtime failure observed for them.

Final 0.4.1 x86_64 APK installed with adb -r over earlier testing build; receipt instrumentation passed again (1 test, 12.547 seconds), /workspace/downloads/041-native-recognition.log. ARM64 APK SHA-256 47b0a9eeb0fe8e8213ae69a34fa90727cce0680f3be7e8827fd4b4129f507b31; 98,028,963 bytes; code 7; same signing certificate. This establishes executable upgrade/install and OCR on emulator, not physical-device data-retention proof.

Delivery: source milestone f8a1801; public delivery repository main 4948b76. Fresh HTTPS download of https://raw.githubusercontent.com/YONG-BEGINNER/dayerize/main/downloads/Dayerize-0.4.1-arm64.apk matched the tested local APK byte-for-byte and SHA-256 47b0a9eeb0fe8e8213ae69a34fa90727cce0680f3be7e8827fd4b4129f507b31. Receipt preview and release scope notes published alongside it. Temporary emulator stopped after verification. Remaining roadmap prerequisites are unchanged except local receipt recognition is now implemented and emulator-tested.
