import json
import pathlib
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path(__file__).parent
ADB = '/Users/thomaslee/Library/Android/sdk/platform-tools/adb'
SERIAL = 'emulator-5556'
PACKAGE = 'com.hoscat.mtj.dev'

def adb(*args):
    return subprocess.check_output([ADB, '-s', SERIAL, *args], timeout=45)

def capture(name=None):
    for attempt in range(8):
        result = adb('shell', 'uiautomator', 'dump', '/sdcard/mtj-r2.xml')
        if b'dumped' in result:
            break
        time.sleep(2)
    else:
        raise AssertionError('No fresh UI hierarchy')
    raw = adb('shell', 'cat', '/sdcard/mtj-r2.xml')
    if name:
        (OUT / (name + '.xml')).write_bytes(raw)
        (OUT / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))
    return list(ET.fromstring(raw).iter('node'))

def click_node(node):
    left, top, right, bottom = map(int, re.findall(r'\d+', node.get('bounds')))
    adb('shell', 'input', 'tap', str((left + right) // 2), str((top + bottom) // 2))
    time.sleep(.5)

def tap(label):
    nodes = capture()
    click_node(next(n for n in nodes if n.get('text') == label))

def enter(label, value):
    nodes = capture()
    field = next(n for n in nodes if n.get('class') == 'android.widget.EditText'
                 and any(c.get('text') == label for c in n.iter('node')))
    click_node(field)
    adb('shell', 'input', 'keycombination', '113', '29')
    adb('shell', 'input', 'text', value)
    adb('shell', 'input', 'keyevent', '4')
    time.sleep(.5)

def texts(nodes):
    return [n.get('text', '') for n in nodes]

def verify_chart(name, unknown):
    for attempt in range(12):
        if '명식' in texts(capture()):
            break
        time.sleep(2)
    nodes = capture(name)
    labels = texts(nodes)
    assert '명식' in labels and '검토 필요' in labels, labels
    positions = [labels.index(label) for label in ['명식', '일간', '연주', '월주', '일주', '시주']]
    assert positions == sorted(positions)
    assert ('시간 모름' in labels) == unknown
    raw = '\n'.join(labels)
    for forbidden in ['검증 상태 별도 표시', '이전 절입', '1995-01-06T', 'policyCode', 'dataVersion']:
        assert forbidden not in raw, forbidden
    assert '대한민국 표준시' in labels
    print(name + ': PASS', flush=True)

def scroll_to_time():
    adb('shell', 'input', 'swipe', '540', '1780', '540', '700', '500')
    time.sleep(.7)

def run():
    for mode in (['dark'] if '--dark-only' in sys.argv or '--resume-dark' in sys.argv else ['light', 'dark']):
        if '--resume-dark' not in sys.argv:
            adb('shell', 'cmd', 'uimode', 'night', 'no' if mode == 'light' else 'yes')
            adb('shell', 'am', 'force-stop', PACKAGE)
            adb('shell', 'am', 'start', '-n', PACKAGE + '/.MainActivity')
            time.sleep(8)
            tap('사주')
            enter('별칭', 'QA')
            enter('생년월일', '19950106')
            tap('내 명식 보기')
            time.sleep(3)
        verify_chart('saju-unknown-' + mode, True)
        tap('계산 기준')
        details = '\n'.join(texts(capture('saju-calculation-details-' + mode)))
        assert '한국 표준시, UTC+09:00' in details and '1995-01-06T' not in details
        tap('닫기')
        tap('정보 수정')
        scroll_to_time()
        click_node(next(n for n in capture() if n.get('checkable') == 'true'
                        and any(c.get('text') == '시간 모름' for c in n.iter('node'))))
        enter('태어난 시간', '1636')
        capture('saju-known-input-' + mode)
        tap('내 명식 보기')
        time.sleep(3)
        verify_chart('saju-known-' + mode, False)
    tap('타로')
    tap('가로형')
    enter('질문', 'Today')
    tap('과거 · 현재 · 미래')
    time.sleep(2)
    nodes = capture('tarot-0of3')
    assert '0 / 3' in texts(nodes)
    cards = [n for n in nodes if re.match(r'카드 \d+,', n.get('content-desc', ''))]
    assert len(cards) == 78
    assert not any(n.get('content-desc', '').endswith(' 탭') for n in nodes)
    for node in cards[:2]:
        click_node(node)
    assert '2 / 3' in texts(capture('tarot-2of3'))
    click_node(cards[2])
    labels = texts(capture('tarot-3of3'))
    assert '3 / 3' in labels and '결과 서랍' in labels
    click_node(cards[2])
    labels = texts(capture('tarot-retap-2of3'))
    assert '2 / 3' in labels and '결과 서랍' not in labels
    print('tarot-retap: PASS', flush=True)

if __name__ == '__main__':
    run()
    (OUT / 'qa-verdict.json').write_text(json.dumps({'status': 'PASS', 'serial': SERIAL, 'saju': ['unknown-light', 'known-light', 'unknown-dark', 'known-dark'], 'tarot': '0/3,2/3,3/3,retap2/3'}, indent=2))
