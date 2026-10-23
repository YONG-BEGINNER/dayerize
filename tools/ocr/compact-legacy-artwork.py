#!/usr/bin/env python3
"""Lossless format-only packaging; stable legacy IDs and every decoded RGBA pixel retained."""
from pathlib import Path
from PIL import Image
import hashlib,io,json,shutil
root=Path(__file__).resolve().parents[2];assets=root/'app/src/main/assets/icons'
backup=Path('/workspace/toolchains/legacy-png-before-041');backup.mkdir(exist_ok=True)
records=[]
for path in sorted(assets.glob('*_3d.png')):
    original=path.read_bytes();shutil.copy2(path,backup/path.name)
    with Image.open(io.BytesIO(original)) as image:
        rgba=image.convert('RGBA');out=io.BytesIO();rgba.save(out,format='WEBP',lossless=True,exact=True,method=4)
        encoded=out.getvalue()
        with Image.open(io.BytesIO(encoded)) as decoded:
            assert decoded.size==rgba.size and decoded.convert('RGBA').tobytes()==rgba.tobytes(),path
        records.append(dict(id=path.stem,rgbaSha256=hashlib.sha256(rgba.tobytes()).hexdigest(),originalBytes=len(original),webpBytes=len(encoded)))
    path.with_suffix('.webp').write_bytes(encoded);path.unlink()
(root/'design/artwork/legacy-encoding-041.json').write_text(json.dumps(records,indent=2)+'\n')
print(len(records),'legacy files; identical RGBA pixels; saved',sum(r['originalBytes']-r['webpBytes'] for r in records),'bytes')
