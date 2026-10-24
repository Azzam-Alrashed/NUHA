"""Emulator acceptance checks. Never clears app data; restores font scale on exit.

Run after installing the debug APK: python3 tools/android/verify_onboarding.py
Requires the tablet emulator, adb, and NUHA open in onboarding or the lab.
Captures each screen separately at 1.0x and 1.5x. Uses visible Arabic labels,
not fixed tap coordinates. Changes only NUHA preferences and emulator font scale.
"""
from pathlib import Path
import os
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ADB = os.environ.get('ADB', str(Path.home() / 'Library/Android/sdk/platform-tools/adb'))
OUT = Path(__file__).resolve().parents[2] / 'output/onboarding'
OUT.mkdir(parents=True, exist_ok=True)

def adb(*args):
    return subprocess.check_output([ADB, '-s', 'emulator-5554', *args], timeout=30)

def tree():
    adb('shell', 'uiautomator', 'dump', '/sdcard/nuha-window.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/nuha-window.xml'))

def has(text):
    assert any(e.get('text') == text for e in tree().iter('node')), text

def tap(text):
    nodes = [e for e in tree().iter('node') if e.get('text') == text]
    assert len(nodes) == 1, (text, len(nodes))
    x1,y1,x2,y2 = map(int, re.findall(r'\d+', nodes[0].get('bounds')))
    adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(.5)  # Let the software-rendered emulator present the updated frame.

def shot(name):
    tree()
    time.sleep(.4)
    (OUT / name).write_bytes(adb('exec-out', 'screencap', '-p'))

def restart():
    adb('shell', 'am', 'force-stop', 'com.nuha.app')
    adb('shell', 'am', 'start', '-W', '-n', 'com.nuha.app/.MainActivity')

def state():
    root=tree()
    return [(e.get('text'),e.get('checked'),e.get('selected'),e.get('content-desc')) for e in root.iter('node') if e.get('checkable')=='true' or e.get('selected')=='true']

original_scale=adb('shell','settings','get','system','font_scale').decode().strip()
try:
    texts = {e.get('text') for e in tree().iter('node')}
    if 'نُهى — مختبر التجارب التقنية' in texts:
        tap('مقدمة نُهى والإعدادات')
    elif '٣ من ٣' in texts:
        tap('السابق'); tap('السابق')
    elif '٢ من ٣' in texts:
        tap('السابق')
    has('١ من ٣'); shot('01-welcome.png')
    tap('لنكتشف نُهى'); has('٢ من ٣'); shot('02-play.png')
    tap('السابق');has('١ من ٣')
    tap('مساعدة');has('نكتشف نُهى معاً');tap('فهمت')
    tap('Ⅱ  استراحة');has('خذ وقتك');tap('متابعة')
    tap('تخطّي');has('٣ من ٣');shot('03-comfort.png')
    tap('هادئ');shot('03-muted.png')
    tap('تباين عالٍ');shot('03-high-contrast.png')
    tap('التعليمات الصوتية');tap('المؤثرات الصوتية');tap('الموسيقى');tap('تقليل الحركة')
    changed = state()
    print('Changed settings:', changed, flush=True)
    restart()
    if any(e.get('text') == 'نُهى — مختبر التجارب التقنية' for e in tree().iter('node')):
        tap('مقدمة نُهى والإعدادات')
    has('١ من ٣');tap('تخطّي');has('٣ من ٣')
    assert state() == changed, 'Preferences changed across process restart'
    print('Restored after process restart:', state(), flush=True)
    shot('03-restored-preferences.png')
    # Restore the standard appearance and defaults before completing.
    tap('ملوّن');tap('التعليمات الصوتية');tap('المؤثرات الصوتية');tap('الموسيقى');tap('تقليل الحركة')
    tap('لنبدأ');has('نُهى — مختبر التجارب التقنية')
    restart();has('نُهى — مختبر التجارب التقنية')
    print('Finish and completion persistence passed', flush=True)
    tap('مقدمة نُهى والإعدادات');has('١ من ٣')
    adb('shell','settings','put','system','font_scale','1.5')
    has('١ من ٣');shot('01-welcome-large.png')
    tap('لنكتشف نُهى');has('٢ من ٣');shot('02-play-large.png')
    tap('التالي');has('٣ من ٣');shot('03-comfort-large.png')
    adb('shell','input','swipe','650','1170','650','620','500')
    shot('03-comfort-large-scrolled.png')
    print('All three screens captured at 1.5x; settings overflow captured', flush=True)
    tap('السابق'); tap('السابق')
finally:
    adb('shell','settings','put','system','font_scale',original_scale if original_scale != 'null' else '1.0')
