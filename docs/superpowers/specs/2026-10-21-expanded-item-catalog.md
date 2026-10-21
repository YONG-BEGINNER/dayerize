# Expanded original item artwork catalog

Status: design expansion requested by the user; samples generated, production catalog not yet exported or installed. Selected Alloy direction and existing information architecture remain unchanged. The user accepted the previous design and requested substantially more choices, including differences between actual product models.

## Scope: 192 identities, 384 original exports

Each identity has a custom 3D and custom 2D representation. Recolors do not count as new identities. Counts are production targets, not completed assets.

| Category | Identities | Coverage |
|---|---:|---|
| Cameras and lenses | 32 | Canon EOS RP/R6/R5 families, Sony A7/A7C/ZV families, Nikon Z6/Z8/Zf families, Fujifilm X-T/X100 families, rangefinder, DSLR, instant, film, action, pocket gimbal, drone, compact/prime/zoom/telephoto lens variants |
| Computers and accessories | 24 | Thin 13/15-inch notebooks, 14/16-inch professional notebooks, business laptop, gaming laptop, convertible, detachable tablet, mini PC, desktop tower, all-in-one, flat/ultrawide monitor, keyboard, mouse, dock, storage |
| Vehicles and mobility | 24 | Myvi/Axia-class hatchbacks, Civic/City/Camry-class sedans, X50/CR-V-class SUVs, Model 3/BYD Seal-class EVs, pickup, MPV, wagon, coupe, convertible, scooter, motorcycle, road/mountain/folding bicycles |
| Phones, tablets and wearables | 20 | Phone body/camera-array families, foldables, small/large tablets, e-reader, round/square smartwatch, fitness band, analog dress/sport watch |
| Audio and gaming | 16 | Over-ear/on-ear headphones, stemmed/stemless earbuds, wired in-ear, portable/bookshelf speakers, soundbar, microphone, console, controller, handheld console, VR headset |
| Home and appliances | 16 | Refrigerator, washer, dryer, rice cooker, multicooker, oven, microwave, kettle, espresso/filter coffee machine, vacuum, robot cleaner, purifier, fan, desk lamp, television |
| Bags, footwear and accessories | 16 | Structured/tote/crossbody bags, daypack/travelpack, soft/hard luggage, briefcase, wallet, running shoes, trainers, boots, sandals, sunglasses, belt |
| Tools and workshop | 12 | Drill, impact driver, screwdriver set, pliers, wrench, hammer, measuring tool, multimeter, soldering station, tool case, sewing machine, 3D printer |
| Sports and outdoors | 12 | Badminton/tennis rackets, golf clubs, dumbbells, yoga mat, helmet, tent, hiking pack, fishing rod, skateboard, binoculars, camping stove |
| Musical instruments | 8 | Acoustic/electric/bass guitar, digital piano, synthesizer, violin, drum kit, saxophone |
| Jewellery and valuables | 6 | Ring, necklace, bracelet, earrings, bullion bar, collectible coin |
| Furniture and collectibles | 6 | Office chair, desk, sofa, shelving, display figure, model kit |
| Total | 192 | 384 exports across two styles |

## Model-aware selection

Picker supports category, brand/model search, style and recent/favorite choices. A search for Canon should not return one generic camera relabeled as multiple models. Store brand, model family, generation/year where relevant, body size, material, searchable aliases and source-reference provenance separately from the stable artwork ID. Brand names are descriptive metadata, not claims of endorsement.

Three confidence levels:
1. Reference-verified model: approved front/side/three-quarter source photos support the silhouette and key proportions. Specific model name is permitted after visual review.
2. Model-family inspired: credible broad shape, but no exact-model validation. Label as a family/body type, not an exact replica.
3. Generic category: fallback artwork with no model claim.

The new generated sheets are level 2 or 3. They must not be promoted to exact-model artwork solely because a model name appeared in the generation prompt. Camera grip/finder/dials, laptop chassis/hinge/port placement and car generation/headlights/roofline are distinguishing features to verify. If a model is missing, the user can choose a close family or import their own photo; no paid runtime image generation is required.

## New samples generated in this iteration

48 sample positions across three sheets (not 48 finalized production exports):
- 16 camera/lens 3D: /workspace/generated_images/exec-0467c82b-f5d0-4ee7-8aa7-03c355a94461.png
- 8 computer + 8 vehicle 3D: /workspace/generated_images/exec-4516e1cc-4b27-4e4c-b350-8ab9a16acb63.png
- 16 everyday-item 2D: /workspace/generated_images/exec-f551e99f-97fb-4fb9-b098-29d1c1ae6233.png

Known sample issues: generated padding is not uniform; similar camera silhouettes still need stronger reference-guided differentiation; some flat illustrations include shaded or perspective detail. These sheets communicate catalog breadth. Production should generate/export one item per file and review it rather than ship a sliced atlas unchecked.

## Production prompt contract

For each item, supply verified references when available and the existing approved style sample. Use this structure:

“Create one original [3D / flat 2D] item illustration for Dayerize: [identity, generation, distinguishing features]. Follow attached product references for proportions and the style reference for rendering only. Transparent 512×512 canvas. Entire silhouette inside a centered 72%-long-dimension safe box, no cropping, no captions, no UI frame. [3D: consistent three-quarter view, soft upper-left light, restrained contact shadow.] [2D: simplified front/side silhouette, consistent contour weight, solid fills, no photoreal materials or gradients.] Preserve [critical shape differences]. Do not substitute a generic object or copy an existing icon-pack illustration.”

Review actual output dimensions and alpha instead of trusting prompt compliance. Keep asset files losslessly proportioned; fit within slots without stretching. Set individual optical scale and offset when thin/tall items feel undersized. Check at 48/64/96dp, dark/light backgrounds and both styles. Validate no edge clipping, halo, unwanted text, distorted geometry or confusing shape. Remove accidental logos and generated pseudo-lettering unless specifically required and verified. Bundle optimized variants and stable manifest IDs; source masters remain outside the APK.

## Delivery sequence

Produce and validate small themed batches, beginning with model-family cameras, computers and vehicles requested by the user. Expand the catalog without blocking the core app on completion of all 384 exports. The 192-identity target is independent of login/sync work and does not change the pending verification of cloud costs. Artwork is bundled offline; no per-selection AI call. Development generation may consume the user's existing tool allowance and is not represented as universally free.


Update: the user subsequently requested broader coverage. The 600-identity roadmap in `2026-10-21-catalog-research-and-2d-directions.md` supersedes this document's count; model verification and technical constraints remain applicable.
