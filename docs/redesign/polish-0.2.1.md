# Whole-app refinement — 0.2.1

Scope approved by the user: refine every existing screen using the supplied 有数 screenshots and the approved black/charcoal/lime direction. This is a bounded visual iteration; accounting, storage, locale and original currency rules remain intact.

- Shared typography, restrained tinted charcoal surfaces, fine borders, icon wells, rounded row feedback and consistent panel spacing apply to collection, wishes, editor, detail, analytics, settings and sheets.
- Collection artwork is larger and separated from status badges. List mode now uses compact horizontal rows with price, duration and daily cost.
- The floating dock has a clear selected background and an accented add action.
- Editor Save stays outside the scrolling form and above keyboard insets. Existing save tests now assert it is displayed without scrolling; they still verify stored data.
- Icon selection prioritizes common personal assets, with consistent artwork bounds and bordered tiles. All 1,284 identifiers and both styles remain available.
- Detail metrics and lifecycle action sizes have been aligned with the shared hierarchy.
- Native Material press feedback is retained; no new motion was added, preserving the reduced-motion experience.

Verification: core 21, imaging 1, app 8 tests passed (30 total). Android lint: 0 errors, 16 warnings. Debug APK build and signing verification passed, preserving the previous signing certificate. Additional rendered navigation checks cover light theme, compact list and settings appearance. Native host screenshots are evidence of Compose layout, not physical-device testing. Device camera/cutout and interrupted restore limitations from the previous milestone remain.

APK: versionCode 3 / versionName 0.2.1; ARM64 SHA-256 `3e073088fa2794624e1c1820e86867a731515cc9e3d1fbab1598666ad6fd8190`.
