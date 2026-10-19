# Android asset manager: design proposal

Status: proposed for user review; no application implementation has started.
Date: 2026-10-19

## Goal and scope

Build a personal Android application from the user's supplied 有数 functional brief. Deliver asset entry, cost tracking, resale, and a dashboard first; then automated photo cutouts and item artwork; then enhancements. This is implementation of the supplied requirements, not a claim of verified equivalence to an independently inspected app.

Android first and functionality before visual polish are user decisions. Local-only storage, manual backup, technology choices, and calculation conventions below are proposed defaults. Automatic multi-device synchronization is not assumed approved.

## Architecture recommendation

Use Kotlin, Jetpack Compose, and Room with Android 8/API 26 as the proposed minimum. Use a single application module with separate UI, domain, persistence, and image/backup packages. Pin compatible stable tool versions during implementation planning.

- Compose screens collect state from ViewModels. UI events invoke domain operations, and Room-backed repositories persist results.
- Cost calculations are pure functions with an injected date/clock for deterministic tests. Formatting belongs to the UI.
- Room transactions protect asset and usage/state updates. Store imported photos in app-private files; record their identifiers in the database.
- Use Android's system photo picker/document interfaces. Preserve the original photo when generating a cutout.
- Use local preferences for locale, reporting timezone, and currency. No account, server, analytics, or remote image upload is needed for the first milestone.

Alternatives considered: Flutter offers easier future iOS reuse but adds another framework without a current iOS requirement. A web application is easier to distribute across desktops but requires extra work around browser storage, installation, and native photo workflows. Native Android is the recommendation for this personal Android-first application.

## First functional milestone

Three main destinations: Gallery, Dashboard, and Disposal Ledger. Selecting an item opens Detail; Add/Edit opens an asset form. Settings contains currency, reporting timezone, and backup/restore.

An asset has a stable identifier, name, category, tags, purchase date, purchase cost, optional photo, notes, cost mode, a positive planned duration or use count, and lifecycle status. Model one owned item per record. Categories and tags can be created and renamed. Required fields have inline validation; cancelled edits do not persist.

For use-based assets, record dated positive usage increments and allow correcting/deleting those entries. The detail screen shows actual usage and target progress. Never display an invented cost per use before the first use.

Gallery cards show photo or accessible category fallback, name, status, and current cost per day/use. Detail shows purchase information, planned progress, current costs, and lifecycle actions. Dashboard shows retained asset value and category/status breakdowns. Disposal Ledger shows sold/scrapped items with original cost, proceeds, signed gain/loss, and final cost per day/use.

## Lifecycle

Statuses: in service, retired, sold, scrapped. Retirement means retained but not currently used; holding-day costs continue until disposal. This avoids confusing days owned with days actively used. In-service and retired assets may be sold or scrapped. Retired assets can return to service.

Selling requires date and proceeds, including zero if explicitly entered. Scrapping records zero proceeds. Disposal dates cannot precede purchase or exceed today; existing usage dates must not exceed disposal. Sold/scrapped assets freeze holding time at disposal and reject new usage. Reopening a disposal requires confirmation, clears disposal fields, and returns the asset to retired status so it can be corrected safely.

Deletion is distinct from retirement/disposal. Show confirmation before permanent deletion. Editing purchase dates must preserve consistency with recorded usage and disposal dates.

## Cost and value conventions

- Store monetary amounts as integer minor units with an ISO currency code; calculate ratios with decimal arithmetic. Round only for display using the currency's fraction digits.
- Choose one currency at first launch using device locale as a suggestion, not a forced choice. Do not change currency once assets exist or silently reinterpret values; multi-currency conversion is later scope.
- Store calendar dates without time-of-day. Persist a reporting timezone initialized from the device; travel does not silently change it. Reject future purchase/usage/disposal dates in that timezone.
- Holding days = calendar-day difference from purchase to today/disposal, plus one. A purchase made today has one holding day.
- Target daily cost = purchase cost / planned days. Current gross daily cost = purchase cost / holding days. Retirement does not pause holding days.
- Target per-use cost = purchase cost / planned uses. Current per-use cost = purchase cost / recorded uses; show “No uses yet” for zero uses.
- Expected end date = purchase date + planned days − 1. Remaining days = max(0, planned days − holding days). Cap progress at 100% and show days beyond target separately. Usage mode uses recorded/planned usage instead of an end date.
- Final net ownership cost = purchase cost − sale proceeds. Realized gain/loss = sale proceeds − purchase cost. Final daily/use cost uses net ownership cost and the final holding days/recorded uses. Preserve negative values for profitable sales. Fees and refunds are outside the first milestone.
- Provide a manually entered current value, defaulting to purchase cost and visibly labeled as an estimate. Retained value totals include in-service and retired assets only; sold/scrapped items contribute zero. Sale proceeds belong in the ledger, not the retained-asset total.
- Label the dashboard “Managed asset value,” since the app does not account for all cash and liabilities.

Example: a 1,000-unit asset bought on January 1 and sold for 400 on January 10 has 10 holding days, 600 net ownership cost, −600 realized gain/loss, and 60 final cost/day. A 100-day target cost is 10/day. These are proposed project semantics, not verified reference-app formulas.

## Privacy and backup

All first-milestone records and images stay in app-private storage. Disable automatic OS cloud backup for this data so storage behavior matches the local-only proposal. No app lock or encrypted database is promised in this milestone; device security protects app-private data.

Provide user-initiated export/import of a versioned archive containing records, settings, and photos through Android's document picker. Clearly disclose that the archive is unencrypted and the selected destination may itself synchronize to a cloud service.

Before replacing local data, validate archive version, identifiers, amounts, dates, referenced files, and safe paths. Reject invalid archives without changing current data. Restore requires a clear replacement confirmation and a recoverable backup of existing data. Stage files before the database transaction; maintain a recovery journal so interruption can be recovered on the next launch. Bound archive sizes and extracted paths; never extract arbitrary paths supplied by an archive.

## Subsequent baseline milestones

Automatic cutouts remain required for the intended full baseline. Evaluate an on-device segmentation model for ordinary objects, licensing, model download requirements, device compatibility, and image quality before choosing a provider. Keep the original image and offer retry/original-image fallback when processing fails. Do not claim automatic cutouts work using a decorative placeholder or a manual-only substitute.

Category-specific depreciation follows the basic manually valued dashboard. Before implementation, specify rule inheritance, per-item overrides, residual values, and interaction with manual valuations. It remains a named baseline gap until delivered.

After those functions work, establish original/licensed item visuals, transparent image composition, consistent gallery spacing, fallback icons, and small-screen readability. Do not copy the reference mascot or assume launcher artwork represents item images.

## Validation and completion

Test cost formulas for same-day purchase, leap days, over-target duration, zero usage, zero-price assets, profitable resale, and retirement/disposal transitions. Test validation and local persistence, process restart, usage corrections, and dashboard exclusion of disposed assets.

Exercise the complete Android journey: create → restart → edit → record use → retire/reactivate → sell → inspect ledger/dashboard → export → restore. Corrupt or incompatible imports must preserve existing data. Test Unicode names, English and Simplified Chinese resources, currency formatting, large text, and labeled controls; charts and gains/losses must remain readable without color alone.

Use unit tests for domain calculations, database tests for transactions/migrations, and instrumented tests for critical UI and persistence flows. Produce a debug APK for personal installation and record whether emulator/device checks actually ran. A successful build alone is not a passing device test.

The first milestone is a usable functional subset. Full-baseline status remains open until cutouts, category depreciation, and agreed visual work are completed and verified. No public publication, hosted backend, or paid services are included in this proposal.
