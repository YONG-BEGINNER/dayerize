# Rounded visual polish

User requested a slightly rounder, smoother version of the existing compact Android interface, using suitable design tools. Superpowers and Product Design informed the review. Fresh native host screenshots show inconsistent clipped panels and rounded controls. No new product flow or visual-direction selection is needed for this scoped refinement.

- Replace cut panel corners with 16dp rounded corners.
- Use 14dp shared control corners and 20dp top navigation corners.
- Soften decorative dark panel outlines from #596066 to #424B50; keep text and input outline colors intact.
- Align Material component shapes with the shared tokens.
- Retain compact layout, font scaling, 48dp targets, dark/light themes, amber accents, statuses and existing motion behavior.
- Validate existing host interactions/layout tests, inspect fresh dark/light and large-text screenshots, run lint, build 0.4.3/code 9 and verify APK signing.

This is a visual-only change; existing behavior tests and native render comparison are appropriate validation rather than tests mirroring corner constants. Source work from the previous compact UI task remains intact. No publication or push is part of this task.

Completed: fresh before/after renders inspected; all 84 tests passed; lint 0 errors / 28 warnings; version 0.4.3 ARM64 APK built and signature verified. See docs/ENHANCEMENTS-0.4.3.md for review and artifact details.
