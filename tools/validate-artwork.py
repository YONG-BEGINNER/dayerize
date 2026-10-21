#!/usr/bin/env python3
"""Read-only validation of bundled originals. Does not generate or edit artwork."""
import json
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1] / 'app/src/main/assets'
entries = json.loads((root / 'artwork/catalog.json').read_text())
assert len({e['id'] for e in entries}) == len(entries), 'Duplicate artwork identity'
paths = [path for e in entries for path in e['files'].values()]
assert len(set(paths)) == len(paths), 'Duplicate style file'
for entry in entries:
    assert .5 <= entry['opticalScale'] <= 1
    assert all(entry['names'].get(locale) for locale in ('en', 'zh-CN', 'zh-TW', 'ms'))
    for style, path in entry['files'].items():
        with Image.open(root / path) as image:
            assert image.width == image.height, f'{path}: non-square canvas'
            assert 'A' in image.getbands(), f'{path}: no alpha channel'
            alpha = image.getchannel('A')
            assert alpha.getextrema() == (0,255), f'{path}: missing transparent/opaque pixels'
            bounds = alpha.point(lambda value: 255 if value > 128 else 0).getbbox()
            assert bounds and bounds[2] > bounds[0] and bounds[3] > bounds[1]
            print(path, image.size, 'subject bounds', bounds, 'optical scale', entry['opticalScale'])
print(f'{len(entries)} selectable identities; {len(paths)} original style exports; {sum((root/p).stat().st_size for p in paths):,} bytes')
