# Dayerize — full Alloy visual presentation

User-selected direction: graphite, silver contours, small amber accents; realistic 3D items plus the approved cute 2D sticker family. Existing information architecture remains Collection, Wishlist, Analytics and Settings. No separate Sold tab.

This presentation comprises twelve representative screens and flows, not a working application, a completed catalog, or exhaustive error/loading-state coverage. It supersedes the older visual board as the full review reference. Native layout and accessibility still require implementation and rendered verification.

## Boards (left to right)

### A — Collection, Wishlist, item detail

![Collection, Wishlist and item detail](/workspace/generated_images/exec-cab69b7a-4cf5-41c0-9ebe-1427ba2d6106.png)

Compact bordered overview, two-column gallery with individually selected artwork, separate wishlist budget, and lifecycle detail with day cost and edit/retire/sale controls. Wishes never count toward owned totals. Asset values remain estimates unless explicitly recorded otherwise.

### B — Asset editor, artwork picker, photo cutout

![Editor, artwork picker and photo cutout](/workspace/generated_images/exec-35271799-322c-4ba5-8683-1cd452e4a675.png)

Pinned Save, original purchase currency, purchase date, category/tags, target rules and advanced metadata. Original 3D and cute 2D artwork choices plus user photos. Cutout is an on-device direction. Outline adjustment and image-fit controls are proposed refinements, not implemented capabilities. Preserve original images and allow recovery from processing failure. Generated Fill/Custom labels are exploratory; default Fit must never crop or stretch an asset silently.

### C — Analytics, Settings, proposed account/sync

![Analytics, Settings and account/sync](/workspace/generated_images/exec-cd69511e-bdbf-45c5-90d7-76e003cff6bc.png)

Category distribution, per-currency reporting, monthly flow, settings and optional profile. No historical trend should be fabricated; generated text suggesting purchases alone generate a historical value series must be replaced with an honest unavailable-history message until snapshot recording exists. Account screen depicts an illustrative signed-in identity with sync still disabled, not actual Google connectivity. Display name and birthday are optional user-entered profile fields. Existing sign-in concept is in the earlier board `/workspace/generated_images/exec-8e5f9488-11dd-42da-b0fe-be5ff04fe9ce.png`.

### D — Filters/sorting, sale settlement, backup/restore

![Filters, sale settlement and backup](/workspace/generated_images/exec-3462321d-c3d3-407e-ac85-ce9ca1a5e02f.png)

Status/category/tag filtering, candidate sort modes, sale reconciliation and local archive handling. The sample camera purchase is MYR2,400, resale MYR1,800, net cost MYR600 over 400 inclusive days, resulting in MYR1.50/day. The generator colored the negative gain/loss green; implementation must use neutral text for depreciation (or an explicit loss treatment), never suggest a negative gain is profit. Archive ZIP is the backup format; APK delivery remains an APK with no ZIP wrapper.

## Shared implementation constraints

- Baseline 390dp viewport, 16dp margins, 12dp gaps, 22sp headings, 14sp body and supporting 12sp labels. Minimum 48dp interactive targets. Target sizes are implementation specifications; image output is not proof of measured touch bounds.
- Preserve system font scaling, check 360/390/411dp widths, 100/130/200% fonts and all four supported locales. Use one column where needed instead of clipping currency values.
- Fit artwork proportionally with per-icon optical corrections and safe margins. Generated borders and image scale need validation; asset sheets are not production atlases.
- All four boards show dark theme. Light-theme adaptation, dialogs, validation, empty/loading/error states and restoration/sync conflict states require detailed implementation review; they are not claimed to be covered by these twelve views.
- Existing currency arithmetic, data storage and backup safety remain authoritative. Decorative mockup labels and generated numerical presentation do not redefine calculations.
- New sort options, default-artwork preference, photo-adjustment controls and profile/sync fields are proposed UX refinements; their presence in a visual does not establish implemented functionality.
- The category roadmap targets 600 identities with up to 1,200 3D/2D exports. This presentation uses selected artwork samples only. No complete catalog or updated APK has been delivered in this design phase.
- Google login/sync remains proposed with no billing enabled. Current cost/quotas and OAuth configuration must be verified before implementation and release; offline use remains independent.

## Approved artwork references

Cute 2D: `/workspace/generated_images/exec-868360df-3e0e-4d91-863c-a98693214cbd.png`.

Realistic 3D original direction: `/workspace/generated_images/exec-def42b92-d09e-4929-bb27-0d3e53d19499.png`.

Expanded home/outdoor/family 3D sample: `/workspace/generated_images/exec-e40919fc-9cb7-4681-a852-97854fc6d3f6.png`.
