#!/usr/bin/env bash
# Gerçek Android emülatöründe Ekran 5 → Ekran 4 akışını dener ve ekran görüntüsü alır:
# uygulamayı aç (bildirim gönderilir) → ekranı kilitle → kilit ekranı → bildirime dokun →
# kilit açılmadan inceleme ekranı → basılı tut, kaydır.
set -euo pipefail
OUT=build/screenshots/android-cihaz
PAKET=com.salihcanbolat.secimanketi
mkdir -p "$OUT"

goruntu() { adb exec-out screencap -p > "$OUT/$1.png"; echo "görüntü: $1"; }

adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant "$PAKET" android.permission.POST_NOTIFICATIONS
adb shell settings put system screen_off_timeout 600000
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1

adb shell am start -W -n "$PAKET/.MainActivity"
sleep 5
goruntu 30-uygulama-acildi

# Ekranı kapat ve aç: kilit ekranı (emülatörde kaydırmalı kilit)
adb shell input keyevent KEYCODE_SLEEP
sleep 2
adb shell input keyevent KEYCODE_WAKEUP
sleep 4
goruntu 31-kilit-ekrani-bildirim

# Bildirimi bul ve dokun
adb shell uiautomator dump /sdcard/ekran.xml >/dev/null
adb pull /sdcard/ekran.xml "$OUT/kilit-ekrani-ogeleri.xml" >/dev/null
KONUM=$(python3 - "$OUT/kilit-ekrani-ogeleri.xml" <<'PY'
import re, sys
xml = open(sys.argv[1], encoding="utf-8").read()
for m in re.finditer(r'<node [^>]*>', xml):
    n = m.group(0)
    if "Canlı Anket" in n or "canli_anket" in n or "Seçim Anketi" in n:
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        if b:
            x1, y1, x2, y2 = map(int, b.groups())
            print((x1 + x2) // 2, (y1 + y2) // 2)
            break
PY
)
echo "bildirim konumu: ${KONUM:-bulunamadı}"
if [ -n "${KONUM:-}" ]; then
  # Kilit ekranında bildirimi genişletmek için aşağı kaydır, sonra dokun
  set -- $KONUM
  adb shell input swipe "$1" "$2" "$1" "$(( $2 + 400 ))" 300 || true
  sleep 2
  goruntu 32-kilit-ekrani-bildirim-genis
  adb shell input tap "$1" "$2"
  sleep 3
  goruntu 33-inceleme-kilit-ustunde
  echo "kilit durumu:"; adb shell dumpsys window | grep -iE "mDreamingLockscreen|isKeyguardShowing|mShowingLockscreen" || true

  # Ekran 4: şeridin ilk sütununa basılı tut, sağa kaydır
  W=$(adb shell wm size | grep -oE '[0-9]+x[0-9]+' | tail -1 | cut -dx -f1)
  D=$(adb shell wm density | grep -oE '[0-9]+' | tail -1)
  dp() { echo $(( $1 * D / 160 )); }
  Y=$(dp 300); X1=$(dp 40); X2=$(( W * 11 / 16 ))
  adb shell input motionevent DOWN "$X1" "$Y"
  sleep 0.6
  for x in $(seq "$X1" 40 "$X2"); do adb shell input motionevent MOVE "$x" "$Y"; done
  sleep 1
  goruntu 34-inceleme-basili-kaydirildi
  adb shell input motionevent UP "$X2" "$Y"
  sleep 1
  adb shell input keyevent KEYCODE_BACK
  sleep 2
  goruntu 35-geri-kilit-ekrani
fi
