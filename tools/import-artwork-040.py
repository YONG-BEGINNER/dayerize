#!/usr/bin/env python3
"""Import a complete, reviewed original-art batch; preserve existing item IDs."""
import json
from pathlib import Path
from PIL import Image
root=Path(__file__).resolve().parents[1]
batch=json.loads((root/'design/artwork/batch-040.json').read_text())
progress=root/'design/artwork/batch-040-progress.jsonl'
records=batch['results']+[json.loads(line) for line in progress.read_text().splitlines() if line.strip()]
by_key={(r['id'],r['style']):r for r in records}
assert len(batch['items'])==64
expected={(row[0],style) for row in batch['items'] for style in ('3d','cute2d')}
assert expected==set(by_key),f'Missing artwork: {expected-set(by_key)}'
catalog_path=root/'app/src/main/assets/artwork/catalog.json'
production_path=root/'design/artwork/production.json'
catalog=json.loads(catalog_path.read_text());production=json.loads(production_path.read_text())
ids={row[0] for row in batch['items']}
catalog=[e for e in catalog if e['id'] not in ids]
production['entries']=[e for e in production['entries'] if e['id'] not in ids]
for row in batch['items']:
    ident,category,en,cn,tw,ms,subject=row
    names=dict(zip(['en','zh-CN','zh-TW','ms'],[en,cn,tw,ms]))
    masters={style:by_key[ident,style]['path'] for style in ('3d','cute2d')}
    for style,path in masters.items():
        with Image.open(path) as im:
            assert im.width==im.height,(ident,style,'non-square')
            assert im.convert('RGBA').getchannel('A').getextrema()==(0,255),(ident,style,'missing transparency')
    exports={style:f'artwork/{ident}_{style}.png' for style in masters}
    catalog.append(dict(id=ident,category=category,names=names,aliases=list(dict.fromkeys([ident.replace('_',' '),en.lower()])),files=exports,opticalScale=1.0))
    production['entries'].append(dict(id=ident,category=category,names=names,modelFamily='generic',referenceConfidence='generic',sourceRefs=['User-approved 2026-10-22 enhancement roadmap','Original image_gen production; batch-040.json and batch-040-progress.jsonl'],styleStatuses={s:'ready-0.4.0' for s in masters},masterPaths=masters,exportPaths=exports,reviewNotes='Original generic illustrations, not verified branded models. Full masters retained; runtime packaging preserves aspect ratio. See 0.4.0 delivery report for visual QA.'))
assert len(catalog)==100 and len({e['id'] for e in catalog})==100
catalog_path.write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
production_path.write_text(json.dumps(production,ensure_ascii=False,indent=2)+'\n')
print('Imported 64 original item pairs; catalog now 100 identities / 200 style files pending export.')
