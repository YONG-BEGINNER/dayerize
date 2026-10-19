# Asset provenance

## Offline icon catalog

Microsoft Fluent Emoji, https://github.com/microsoft/fluentui-emoji, commit `1ffb34c752ecf5d402f04cfb4b392c77f57c54bc`, MIT license. Copyright Microsoft Corporation. License bundled in `app/src/main/assets/FLUENT-LICENSE.txt`.

1,284 distinct named catalog items, each available in 3D and Flat (2D), totaling 2,568 images. Nested skin-tone variants are excluded from the count. The 0.2.0 build retains original 256px 3D PNG artwork; Flat SVG artwork is rendered to 160px WebP; stable filename-derived IDs are in `icons.json`. All catalog images are bundled offline; no server or icon-download permissions are needed.

## Segmentation candidate

U²-Net small model (`u2netp`), architecture/source https://github.com/xuebinqin/U-2-Net, Apache-2.0 license copied into `app/src/main/assets/models/LICENSE-U2NET.txt`.

Published ONNX weights: https://github.com/danielgatis/rembg/releases/download/v0.0.0/u2netp.onnx

Downloaded through verified HTTPS. Observed SHA-256: `309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`. Size: 4,574,861 bytes. This observed hash pins these bytes; it is not an independently published checksum. Device inference and representative image-quality validation must be recorded before claiming cutout readiness.

Illustrated 3D icons are static raster artwork. Photo tilt is not 3D reconstruction.
