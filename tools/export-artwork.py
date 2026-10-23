#!/usr/bin/env python3
"""Export original source masters at mobile resolution without cropping or stretching.

Only packaging derivatives are produced. Generated source masters are left intact.
Run after reviewing generated masters and recording their paths in production.json.
"""
import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image

parser=argparse.ArgumentParser()
parser.add_argument('--size',type=int,choices=[256,384,512],default=512)
export_size=parser.parse_args().size
repo = Path(__file__).resolve().parents[1]
production_path = repo / 'design/artwork/production.json'
production = json.loads(production_path.read_text())
assets = repo / 'app/src/main/assets'
catalog_path = assets / 'artwork/catalog.json'
catalog = json.loads(catalog_path.read_text())
by_id = {entry['id']: entry for entry in catalog}
for entry in production['entries']:
    scales = []
    entry['exports'] = {}
    for style, source_path in entry['masterPaths'].items():
        source = Path(source_path)
        target = assets / entry['exportPaths'][style]
        with Image.open(source) as image:
            assert image.width == image.height, f'Non-square master: {source}'
            rgba = image.convert('RGBA')
            alpha = rgba.getchannel('A')
            assert alpha.getextrema() == (0, 255), f'Invalid alpha: {source}'
            bounds = alpha.point(lambda value: 255 if value > 128 else 0).getbbox()
            assert bounds
            fraction = max(bounds[2]-bounds[0], bounds[3]-bounds[1]) / image.width
            scales.append(min(1.0, .78 / fraction))
            # Uniform square-canvas downsampling only; no subject crop or alteration.
            export = rgba.resize((export_size, export_size), Image.Resampling.LANCZOS)
            export.save(target, format='PNG', optimize=True, compress_level=9)
        entry['exports'][style] = {
            'width': export_size, 'height': export_size,
            'masterSha256': hashlib.sha256(source.read_bytes()).hexdigest(),
            'exportSha256': hashlib.sha256(target.read_bytes()).hexdigest(),
            'bytes': target.stat().st_size,
        }
    by_id[entry['id']]['opticalScale'] = round(min(scales), 3)
production['currentIdentities'] = len(catalog)
production['currentStyleExports'] = sum(len(entry['files']) for entry in catalog)
production_path.write_text(json.dumps(production,ensure_ascii=False,indent=2)+'\n')
catalog_path.write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
print(f'Exported {production["currentStyleExports"]} icons at {export_size}×{export_size}; source masters preserved.')
