#!/usr/bin/env python3
"""Package locally compiled, pinned Tesseract4Android sources; no remote runtime calls.
See docs/ocr/README.md for the compiler and native build commands.
"""
from pathlib import Path
import subprocess, shutil, zipfile, hashlib, json
root=Path(__file__).resolve().parents[2]
tools=Path('/workspace/toolchains');src=tools/'Tesseract4Android';ndk=tools/'android-ndk-r27c'
assert subprocess.check_output(['git','-C',str(src),'rev-parse','HEAD'],text=True).strip()=='15c534717b1cb58261b58d4e4c1200c7f81f668c'
work=tools/'ocr-package';work.mkdir(exist_ok=True)
classes=work/'classes';classes.mkdir(exist_ok=True)
buildconfig=work/'BuildConfig.java';buildconfig.write_text('package cz.adaptech.tesseract4android; public final class BuildConfig { public static final String FLAVOR="standard"; }\n')
annotation=next((tools/'gradle-cache/caches/modules-2/files-2.1/androidx.annotation/annotation-jvm/1.9.1').glob('*/*.jar'))
java=list((src/'tesseract4android/src/main/java').rglob('*.java'))
subprocess.run([str(tools/'jdk-21.0.6+7/bin/javac'),'--release','17','-cp',str(tools/'android-sdk/platforms/android-35/android.jar')+':'+str(annotation),'-d',str(classes),*[str(p) for p in java],str(buildconfig)],check=True)
def write(z,name,data):
 info=zipfile.ZipInfo(name,(2026,10,23,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,data)
jar=work/'classes.jar'
with zipfile.ZipFile(jar,'w') as z:
 for p in sorted(classes.rglob('*.class')):write(z,str(p.relative_to(classes)),p.read_bytes())
out=root/'app/libs/tesseract4android-4.9.0-local.aar';out.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(out,'w') as z:
 write(z,'classes.jar',jar.read_bytes())
 write(z,'AndroidManifest.xml',b'<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="cz.adaptech.tesseract4android"><uses-sdk android:minSdkVersion="26"/></manifest>')
 write(z,'R.txt',b'')
 for abi,triple in [('arm64-v8a','aarch64-linux-android'),('armeabi-v7a','arm-linux-androideabi'),('x86','i686-linux-android'),('x86_64','x86_64-linux-android')]:
  for p in sorted((tools/'ocr-build'/abi).glob('*/*.so')):
   target=work/(abi+'-'+p.name);shutil.copy2(p,target)
   subprocess.run([str(ndk/'toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-strip'),'--strip-unneeded',str(target)],check=True)
   write(z,'jni/'+abi+'/'+p.name,target.read_bytes())
  assert len(list((tools/'ocr-build'/abi).glob('*/*.so')))==4,abi
  write(z,'jni/'+abi+'/libc++_shared.so',(ndk/'toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib'/triple/'libc++_shared.so').read_bytes())
print(out,hashlib.sha256(out.read_bytes()).hexdigest())
