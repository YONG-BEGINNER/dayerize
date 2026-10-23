#!/usr/bin/env python3
"""Build read-only visual QA contact sheets; never modifies product artwork."""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
parser=argparse.ArgumentParser()
parser.add_argument('--prefix',default='catalog-031')
prefix=parser.parse_args().prefix
repo=Path(__file__).resolve().parents[1]
assets=repo/'app/src/main/assets'
entries=json.loads((assets/'artwork/catalog.json').read_text())
font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',13)
out=repo/'docs/redesign/screenshots'
for style in ('3d','cute2d'):
    for theme,bg,fg in [('dark','#171a1c','#eeeeee'),('light','#f5f5f2','#202528')]:
        sheet=Image.new('RGBA',(6*256,((len(entries)+5)//6)*150),bg)
        draw=ImageDraw.Draw(sheet)
        for index,entry in enumerate(entries):
            x=(index%6)*256;y=(index//6)*150
            draw.text((x+8,y+8),entry['names']['en'],fill=fg,font=font)
            start=x+8
            for size in (48,64,96):
                tile=Image.new('RGBA',(size,size))
                with Image.open(assets/entry['files'][style]) as im:
                    im=im.convert('RGBA');actual=round(size*entry['opticalScale'])
                    im.thumbnail((actual,actual),Image.Resampling.LANCZOS)
                    tile.alpha_composite(im,((size-im.width)//2,(size-im.height)//2))
                sheet.alpha_composite(tile,(start,y+28+(96-size)//2))
                start+=size+10
            draw.line((x,y+148,x+255,y+148),fill='#596066',width=1)
        path=out/f'{prefix}-{style}-{theme}.png'
        sheet.convert('RGB').save(path)
        print(path)
