"""Uygulama simgesini ve açılış simgesini tek kaynaktan üretir.

Kullanım: python3 android/tools/simge_uret.py android/app/src/main/res <önizleme klasörü>
"""
import sys, os
RES = sys.argv[1]; OUT = sys.argv[2]

PL, PR, PT, PB = 41, 54, 34, 62      # parti mumu
LL, LR, LT, LB = 54, 67, 28, 62      # lider mumu
TL, TR, TT, TB = 43, 65, 66, 84      # logo kutusu

def mum_yolu(l, r, t, b, sol):
    if sol:   # sol üst 5, sol alt 2
        return f"M{l+5},{t}h{r-l-5}v{b-t}h{-(r-l-2)}a2,2 0,0 1,-2 -2v{-(b-t-7)}a5,5 0,0 1,5 -5z"
    else:     # sağ üst 5, sağ alt 2
        return f"M{l},{t}h{r-l-5}a5,5 0,0 1,5 5v{b-t-7}a2,2 0,0 1,-2 2h{-(r-l-2)}z"

def cizgiler(l, r, t, b):
    parca = []
    c = l + t
    while c <= r + b + 8:
        x1, x2 = l - 30, r + 30
        parca.append(f"M{x1},{c - x1}L{x2},{c - x2}")
        c += 8
    return "".join(parca)

parti, lider = mum_yolu(PL, PR, PT, PB, True), mum_yolu(LL, LR, LT, LB, False)
kutu = f"M{TL+5},{TT}h{TR-TL-10}a5,5 0,0 1,5 5v{TB-TT-10}a5,5 0,0 1,-5 5h{-(TR-TL-10)}a5,5 0,0 1,-5 -5v{-(TB-TT-10)}a5,5 0,0 1,5 -5z"
onay = "M49,75.5l3.6,3.6l7.4,-7.6"
fitil_p = f"M{(PL+PR)/2-0.8},{PT-4}h1.6v4h-1.6z"
fitil_l = f"M{(LL+LR)/2-0.8},{LT-4}h1.6v4h-1.6z"

on = f'''<?xml version="1.0" encoding="utf-8"?>
<!--
  Uygulama simgesi ön yüzü (android/tools/simge_uret.py ile üretilir, elle düzenlenmez): kilit ekranı şeridinin bir sütunu.
  Yapışık iki mum (solda parti, fildişi; sağda lider, mavi), fitilleri ve altlarında onaylı logo
  kutusu. Güvenli alan: ortadaki 66'lık daire.
-->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M20,54a34,34 0,1 1,68 0a34,34 0,1 1,-68 0z">
        <aapt:attr name="android:fillColor">
            <gradient android:type="radial" android:centerX="54" android:centerY="50" android:gradientRadius="34">
                <item android:offset="0" android:color="#405B87FF" />
                <item android:offset="1" android:color="#005B87FF" />
            </gradient>
        </aapt:attr>
    </path>
    <group android:name="parti">
        <clip-path android:pathData="{parti}" />
        <path android:fillColor="#99F5F1E8" android:pathData="M{PL-2},{PT-2}h{PR-PL+4}v{PB-PT+4}h{-(PR-PL+4)}z" />
        <path android:strokeColor="#F2F5F1E8" android:strokeWidth="3" android:pathData="{cizgiler(PL, PR, PT, PB)}" />
    </group>
    <path android:fillColor="#B3F5F1E8" android:pathData="{fitil_p}" />
    <group android:name="lider">
        <clip-path android:pathData="{lider}" />
        <path android:fillColor="#805B87FF" android:pathData="M{LL-2},{LT-2}h{LR-LL+4}v{LB-LT+4}h{-(LR-LL+4)}z" />
        <path android:strokeColor="#F25B87FF" android:strokeWidth="3" android:pathData="{cizgiler(LL, LR, LT, LB)}" />
    </group>
    <path android:fillColor="#E65B87FF" android:pathData="{fitil_l}" />
    <path android:fillColor="#FFF7F5EF" android:pathData="{kutu}" />
    <path android:strokeColor="#FF0A1030" android:strokeWidth="2.6" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="{onay}" />
</vector>
'''
tek = f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Android 13+ temalı simgeler için tek renkli katman (android/tools/simge_uret.py ile üretilir): aynı biçim, düz dolgu. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#FFFFFFFF" android:pathData="{parti}" />
    <path android:fillColor="#FFFFFFFF" android:pathData="M{LL+0.8},{LT}h{LR-LL-5.8}a5,5 0,0 1,5 5v{LB-LT-7}a2,2 0,0 1,-2 2h{-(LR-LL-2.8)}z" />
    <path android:fillColor="#FFFFFFFF" android:pathData="{fitil_p}{fitil_l}" />
    <path android:fillColor="#FFFFFFFF" android:pathData="{kutu}" />
</vector>
'''
open(os.path.join(RES, "drawable/ic_simge_on.xml"), "w").write(on)
open(os.path.join(RES, "drawable/ic_simge_tek_renk.xml"), "w").write(tek)

svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="432" height="432" viewBox="0 0 108 108">
<defs>
<radialGradient id="z" cx="54" cy="30" r="92" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#22337A"/><stop offset="0.45" stop-color="#101A45"/><stop offset="1" stop-color="#050817"/></radialGradient>
<radialGradient id="i" cx="54" cy="50" r="34" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="#5B87FF" stop-opacity="0.25"/><stop offset="1" stop-color="#5B87FF" stop-opacity="0"/></radialGradient>
<clipPath id="p"><path d="{parti}"/></clipPath><clipPath id="l"><path d="{lider}"/></clipPath>
<clipPath id="m"><circle cx="54" cy="54" r="54"/></clipPath></defs>
<g clip-path="url(#m)"><rect width="108" height="108" fill="url(#z)"/>
<circle cx="54" cy="54" r="34" fill="url(#i)"/>
<g clip-path="url(#p)"><rect x="{PL-2}" y="{PT-2}" width="{PR-PL+4}" height="{PB-PT+4}" fill="#F5F1E8" fill-opacity="0.6"/><path stroke="#F5F1E8" stroke-opacity="0.95" stroke-width="3" d="{cizgiler(PL, PR, PT, PB)}"/></g>
<path d="{fitil_p}" fill="#F5F1E8" fill-opacity="0.7"/>
<g clip-path="url(#l)"><rect x="{LL-2}" y="{LT-2}" width="{LR-LL+4}" height="{LB-LT+4}" fill="#5B87FF" fill-opacity="0.5"/><path stroke="#5B87FF" stroke-opacity="0.95" stroke-width="3" d="{cizgiler(LL, LR, LT, LB)}"/></g>
<path d="{fitil_l}" fill="#5B87FF" fill-opacity="0.9"/>
<path d="{kutu}" fill="#F7F5EF"/><path d="{onay}" stroke="#0A1030" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
<circle cx="54" cy="54" r="33" fill="none" stroke="#ff0000" stroke-opacity="0.0"/>
</g></svg>'''
open(os.path.join(OUT, "simge.svg"), "w").write(svg)

# ---- Açılış ekranı: mumlar aşağıdan yükselir, logo kutusu belirir (Android 12+ hareketli simge) ----
acilis = f'''<?xml version="1.0" encoding="utf-8"?>
<!--
  Açılış ekranı simgesi (android/tools/simge_uret.py ile üretilir): uygulama simgesinin ön yüzü, hareketli. Önce logo
  kutusu belirir, sonra parti ve lider mumları sırayla aşağıdan yükselir (yaklaşık 750 ms).
  Android 12 öncesinde hareketsiz son kare gösterilir.
-->
<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt">
    <aapt:attr name="android:drawable">
        <vector
            android:width="108dp"
            android:height="108dp"
            android:viewportWidth="108"
            android:viewportHeight="108">
            <group android:name="kutu" android:pivotX="54" android:pivotY="75">
                <path android:fillColor="#FFF7F5EF" android:pathData="{kutu}" />
                <path android:strokeColor="#FF0A1030" android:strokeWidth="2.6" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="{onay}" />
            </group>
            <group android:name="parti" android:pivotX="{(PL+PR)/2}" android:pivotY="{PB}">
                <group>
                    <clip-path android:pathData="{parti}" />
                    <path android:fillColor="#99F5F1E8" android:pathData="M{PL-2},{PT-2}h{PR-PL+4}v{PB-PT+4}h{-(PR-PL+4)}z" />
                    <path android:strokeColor="#F2F5F1E8" android:strokeWidth="3" android:pathData="{cizgiler(PL, PR, PT, PB)}" />
                </group>
                <path android:fillColor="#B3F5F1E8" android:pathData="{fitil_p}" />
            </group>
            <group android:name="lider" android:pivotX="{(LL+LR)/2}" android:pivotY="{LB}">
                <group>
                    <clip-path android:pathData="{lider}" />
                    <path android:fillColor="#805B87FF" android:pathData="M{LL-2},{LT-2}h{LR-LL+4}v{LB-LT+4}h{-(LR-LL+4)}z" />
                    <path android:strokeColor="#F25B87FF" android:strokeWidth="3" android:pathData="{cizgiler(LL, LR, LT, LB)}" />
                </group>
                <path android:fillColor="#E65B87FF" android:pathData="{fitil_l}" />
            </group>
        </vector>
    </aapt:attr>
    <target android:name="kutu">
        <aapt:attr name="android:animation">
            <set>
                <objectAnimator android:propertyName="scaleX" android:valueFrom="0.6" android:valueTo="1" android:duration="380" android:interpolator="@android:interpolator/overshoot" />
                <objectAnimator android:propertyName="scaleY" android:valueFrom="0.6" android:valueTo="1" android:duration="380" android:interpolator="@android:interpolator/overshoot" />
            </set>
        </aapt:attr>
    </target>
    <target android:name="parti">
        <aapt:attr name="android:animation">
            <objectAnimator android:propertyName="scaleY" android:valueFrom="0" android:valueTo="1" android:startOffset="160" android:duration="520" android:interpolator="@android:interpolator/decelerate_cubic" />
        </aapt:attr>
    </target>
    <target android:name="lider">
        <aapt:attr name="android:animation">
            <objectAnimator android:propertyName="scaleY" android:valueFrom="0" android:valueTo="1" android:startOffset="240" android:duration="520" android:interpolator="@android:interpolator/decelerate_cubic" />
        </aapt:attr>
    </target>
</animated-vector>
'''
open(os.path.join(RES, "drawable/acilis_simgesi.xml"), "w").write(acilis)
