#!/usr/bin/env python3
"""Losslessly encode generated PNG exports; assert every RGBA pixel is unchanged."""
from pathlib import Path
from PIL import Image
import io
root = Path(__file__).resolve().parents[1] / 'app/src/main/assets/artwork'
saved = 0
for path in root.glob('*.png'):
    with Image.open(path) as original:
        pixels = original.convert('RGBA').tobytes()
        output = io.BytesIO()
        original.save(output, format='PNG', optimize=True, compress_level=9)
        encoded = output.getvalue()
        with Image.open(io.BytesIO(encoded)) as decoded:
            assert decoded.size == original.size
            assert decoded.convert('RGBA').tobytes() == pixels
    if len(encoded) < path.stat().st_size:
        saved += path.stat().st_size - len(encoded)
        path.write_bytes(encoded)
print(f'Saved {saved:,} bytes; all RGBA pixels unchanged.')
