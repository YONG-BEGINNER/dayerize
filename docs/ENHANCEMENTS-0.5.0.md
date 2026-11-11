# Dayerize 0.5.0 — smoother lists and quicker everyday actions

## Fluency
- The collection, wishlist and analytics screens now remember their filtered, sorted and summarised results and only redo that work when the items, filters, rates or date actually change. Previously every keystroke, tab fade and unrelated settings write repeated it.
- Saved exchange rates are parsed once per change instead of once per item per redraw.
- Item artwork uses one shared catalog lookup for the whole app instead of re-reading the catalog for every tile.
- A settings write (layout, sort, theme) no longer re-decodes every stored item.
- Each visible card computes its running cost once and keeps it while the item and date are unchanged.

## Functionality
- **Due soon.** Collection, Wishlist and Analytics show items with a warranty end, return deadline, maintenance date or wishlist review date in the next 7 days, soonest first. Tapping one opens the item. The panel is hidden when nothing is due.
- **Long-press quick actions.** Long-pressing a collection card opens a sheet with View, Log use, Show in/Remove from widget, Duplicate item, Retire and Delete. Retire and Delete ask for confirmation.
- **Running cost in the reporting currency.** Cards show cost per day or per use in the reporting currency when a saved rate exists for the item's currency. Without a rate the item's own currency is kept. The purchase price always stays in its original currency.
- New labels localized for English, Malay, Simplified Chinese and Traditional Chinese.

## Not changed
- No data format, database or backup changes; 0.4.x data opens unchanged.
- Delete from the quick-action sheet uses a confirmation dialog rather than an undo snackbar.

## Delivery
Version 0.5.0 / code 11. Source only; no APK has been built or published for this version. Physical-device testing and on-device frame-rate measurement remain outstanding.
