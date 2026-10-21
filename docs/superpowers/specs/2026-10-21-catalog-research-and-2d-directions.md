# Catalog coverage research and 2D style selection

Status: design research and style drafts only; no new catalog installed in the app. User explicitly retains the existing realistic 3D style and requests greater breadth plus a choice of 2D styles.

## Evidence, retrieved 2026-10-21

- Google Product Taxonomy: https://www.google.com/basepages/producttype/taxonomy-with-ids.en-US.txt — fetched successfully, 482,896 bytes, served header `Google_Product_Taxonomy_Version: 2021-09-21`. Supports category breadth, not current sales popularity. Relevant durable-goods branches include Cameras & Optics, Electronics, Furniture, Hardware, Home & Garden, Luggage & Bags, Sporting Goods, Toys & Games, Vehicles & Parts, Personal Care, Baby Transport and Pet Supplies.
- Shopify Product Taxonomy: https://raw.githubusercontent.com/Shopify/product-taxonomy/main/dist/en/categories.txt — fetched successfully, 2,091,739 bytes, header `2026-11-unstable`. This is an unstable forward-version development snapshot, not a released November taxonomy or evidence of future demand. Used only as a secondary category cross-check.
- eBay global markets page: https://www.ebay.com/globalmarkets/ — proxy HTTP 403; not used as evidence. No live sales rankings were verified.

This is a taxonomy-grounded coverage plan. Priority is our product judgment, based on personal ownership, long useful lives, meaningful holding cost and the user's emphasis on electronics, cameras and cars—not a claimed global popularity ranking. Consumables and industrial-only categories are deliberately deprioritized.

## Revised target: 600 identities, up to 1,200 style exports

The 600 identities include distinct product forms and reference-verified model families; colour swaps do not inflate the count. Both styles are planned, but the 2D visual language awaits selection. This supersedes the earlier 192-identity target, not the model-reference/proportion requirements.

| Category | Target identities | Coverage examples |
|---|---:|---|
| Cameras, lenses and optics | 60 | Mirrorless, DSLR, rangefinder, instant, action, gimbal, drone, telescope, binoculars, lenses, tripod |
| Computers and peripherals | 50 | Ultrabook, workstation, gaming laptop, mini PC, tower, monitor, printer, keyboard, mouse, dock, SSD |
| Phones, tablets and readers | 40 | Phone body/camera families, foldables, tablets, stylus tablets, e-readers |
| Audio and home entertainment | 40 | Headphones, earbuds, microphone, speakers, turntable, amplifier, TV, projector |
| Gaming and streaming | 30 | Consoles, handhelds, controllers, racing wheel, arcade stick, VR, capture device |
| Vehicles and personal mobility | 50 | Hatchback, sedan, wagon, SUV, MPV, coupe, EV, pickup, motorcycle, scooter, bicycles |
| Home appliances and smart home | 45 | Washer, dryer, vacuum, robot cleaner, purifier, fan, heater, smart lock, cameras, router |
| Kitchen and coffee | 35 | Refrigerator, oven, cooker, blender, mixer, toaster, kettle, espresso machine, grinder, cookware |
| Furniture and lighting | 25 | Desk, task chair, sofa, bed, shelving, wardrobe, lamps |
| Bags and travel | 30 | Backpacks, totes, crossbody, briefcase, luggage, camera bags, carriers |
| Clothing, shoes and accessories | 35 | Jackets, coats, dresses, trousers, trainers, boots, sandals, belts, sunglasses |
| Watches, jewellery and valuables | 25 | Analog, digital, smart and dive watches; rings, necklaces, earrings, bullion, coins |
| Fitness and sports | 35 | Rackets, clubs, weights, exercise bike, treadmill, yoga gear, balls, helmets |
| Outdoors and camping | 25 | Tent, sleeping bag, stove, cooler, lantern, fishing equipment, climbing gear |
| Tools, DIY and garden | 25 | Drill, saw, driver, measuring tools, multimeter, soldering iron, mower, pressure washer |
| Music and recording | 15 | Guitars, bass, piano, synthesizer, violin, drums, saxophone, interface, mixer |
| Collectibles, arts and office | 15 | Figures, models, cards, craft/sewing tools, drawing tablet, office equipment |
| Personal-care and health devices | 10 | Hair dryer, styler, shaver, trimmer, electric toothbrush, massager, scale, thermometer |
| Baby equipment | 5 | Stroller, child car seat, carrier, high chair, baby monitor |
| Pet equipment | 5 | Carrier, automatic feeder, water fountain, grooming device, aquarium |
| Total | 600 | 1,200 exports if every identity receives both styles |

## Prioritization and delivery

Deliver reviewed themed batches, prioritizing cameras/computers/phones/audio/vehicles and common household items. Keep an explicit manifest of planned, generated, reference-verified, exported and shipped states. Do not report all target icons as generated or shipped. Avoid packing large full-resolution masters into the APK: use measured optimized exports and track download-size impact before choosing optional packs. Any optional downloading must be separately designed and must not compromise already-downloaded offline access.

Model-specific entries require source photos, generation/year and distinguishing geometry. Generated family samples alone do not justify an exact model label. Support user photos for missing models. Retain the approved 3D rendering; only 2D style is under selection.

## Controlled 2D comparison

Three independent draft sheets use the same twelve subjects in the same order on a graphite background: camera, laptop, sedan, headphones / watch, backpack, shoe, phone / coffee machine, drill, guitar, bicycle. This isolates the effect of visual style.

- Precision Line: minimal light monoline with amber accents; technical and restrained. Needs contrast adaptation in light mode and careful stroke handling at small sizes.
- Colour Block: simple filled silhouettes and a small multicolour palette; prioritizes fast recognition, with less exact model detail.
- Illustrated Sticker: bold contour, offwhite cutline and characterful muted colours; more expressive, potentially more visual weight on compact cards.

These are design references. Final exports need transparent backgrounds, consistent optical scale and 48/64/96dp checks. Every 2D style must stay flat instead of inheriting photoreal 3D shading. The existing 3D style remains selected regardless of the 2D choice.

## Displayed option mapping for this iteration

Only this latest three-image set determines the user's next 2D selection:
1. Precision Line: /workspace/generated_images/exec-e5186470-c80e-407a-8e0d-4ae4574c60ed.png
2. Colour Block: /workspace/generated_images/exec-83ddfaae-efa7-4dac-94f2-87302cb654d9.png
3. Illustrated Sticker: /workspace/generated_images/exec-23df8817-6238-496f-a357-601ca3fdfa40.png

All three are draft sheets, not per-icon exports. Background texture/shading introduced by generation is not a production transparency requirement; normalize these during individual asset production after style selection. No 2D style has yet been chosen from this set.
