# Offline receipt recognition

No paid API, billing enrollment, account, internet permission or server is used. Recognition uses bundled open-source Tesseract locally. Receipt images and recognized text are temporary; only explicitly applied price/currency/date/retailer fields enter the unsaved editor draft. Raw receipts do not enter archives or the asset store. The source image remains wherever the user originally selected it.

The review UI is intentionally minimal: one editor action; editable fields; ambiguous dates/currencies require review; raw text collapsed under Show details. A receipt total may cover multiple items. Do not equate it automatically with the purchase price of one asset.

## Provenance and build

- Tesseract4Android tag 4.9.0, commit `15c534717b1cb58261b58d4e4c1200c7f81f668c`, https://github.com/adaptech-cz/Tesseract4Android. Includes Tesseract 5.5.1 and its native dependencies. Licenses bundled in `app/src/main/assets/ocr/licenses`.
- Language data: https://github.com/tesseract-ocr/tessdata_fast at `87416418657359cb625c412a48b6e1d6d41c29bd`, eng, chi_sim, chi_tra (Apache-2.0). Latin recognition uses the English model, not a dedicated Malay dictionary. Mixed Chinese modes combine Chinese + English. No exact accuracy or universal language support promised.
- Android NDK r27c (`27.2.12479018`), CMake 3.31.6, Ninja 1.11.1.3, JDK 21 compiling Java 17. API floor 26, all four existing ABIs, flexible 16 KiB page-size native build flag.
- Source tree remains unchanged. `tools/ocr/AndroidNdkModules.cmake` replaces the removed CMake helper by importing the same NDK cpu-features source for ARM32.
- Configure each ABI with `cmake -S <upstream>/tesseract4android/src/main/cpp -B <native-build>/<abi> -G Ninja -DCMAKE_MAKE_PROGRAM=<ninja> -DCMAKE_MODULE_PATH=<repo>/tools/ocr -DCMAKE_TOOLCHAIN_FILE=<ndk>/build/cmake/android.toolchain.cmake -DANDROID_ABI=<abi> -DANDROID_PLATFORM=android-26 -DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON -DCMAKE_BUILD_TYPE=Release`, then `cmake --build <native-build>/<abi> -j 4`.
- `python3 tools/ocr/package-library.py` compiles the upstream Java bridge and packages stripped native libraries into the local AAR. Paths at top of script document the cloud toolchain layout. Pinned source commit is checked, language model SHA-256 digests are checked on installation.
- Local AAR SHA-256: `5c560b14b0352f8b728ce70a531b0bab700d0049cc68c2a73834f940e4ffd20c`.

## Bounds and limitations

Input maximum 20 MiB / dimensions 20,000 pixels, decoded down to a maximum 2,400-pixel side with EXIF orientation. Transparent inputs flatten onto white. One scan runs at a time. Recognition is interruptible through hOCR and cancellation/45-second deadline monitoring; cleanup happens after native work returns. The native engine checks cancellation between internal work units, so cancellation is not an instantaneous hard kill.

Downloaded models are bundled in the APK and copied with checksum verification to no-backup internal storage. Corruption or storage failure gives an error and leaves the draft unchanged. No Google or external AI processing is added.
