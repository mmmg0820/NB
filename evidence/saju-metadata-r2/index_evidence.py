import hashlib
import json
import pathlib
import re
import xml.etree.ElementTree as ET

root = pathlib.Path(__file__).parent
apk = root / 'sharomyang-saju-metadata-r2-debug.apk'
digest = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
apk_sha = digest(apk)
captures = []
for xml in sorted(root.glob('*.xml')):
    nodes = list(ET.parse(xml).getroot().iter('node'))
    png = xml.with_suffix('.png')
    captures.append({
        'xml': xml.name, 'xmlSha256': digest(xml),
        'png': png.name, 'pngSha256': digest(png),
        'apkSha256': apk_sha,
        'hierarchyPackages': sorted({n.get('package') for n in nodes if n.get('package')}),
        'configurationEvidence': 'runtime-metadata.txt',
        'configurationScope': 'Shared device configuration; no size/density/font changes during this run. Theme varies by capture name.',
        'text': [n.get('text') for n in nodes if n.get('text')],
    })
suites = [ET.parse(p).getroot() for p in (root / 'unit-results').glob('TEST-*.xml')]
tests = {key: sum(int(s.get(key, 0)) for s in suites) for key in ['tests', 'failures', 'errors', 'skipped']}
assert tests == {'tests': 94, 'failures': 0, 'errors': 0, 'skipped': 0}, tests
trace = {}
for name in ['tarot-0of3', 'tarot-2of3', 'tarot-3of3', 'tarot-retap-2of3']:
    nodes = list(ET.parse(root / (name + '.xml')).getroot().iter('node'))
    trace[name] = [{'description': n.get('content-desc'), 'bounds': n.get('bounds')} for n in nodes
                   if n.get('content-desc', '').startswith(('카드 1,', '카드 2,', '카드 3,'))]
original = list(map(int, re.findall(r'\d+', trace['tarot-0of3'][2]['bounds'])))
tap_x, tap_y = (original[0] + original[2]) // 2, (original[1] + original[3]) // 2
for stage in ['tarot-3of3', 'tarot-retap-2of3']:
    left, top, right, bottom = map(int, re.findall(r'\d+', trace[stage][2]['bounds']))
    assert left <= tap_x <= right and top <= tap_y <= bottom
assert '3번째 선택' in trace['tarot-3of3'][2]['description']
assert '선택 안 됨' in trace['tarot-retap-2of3'][2]['description']
(root / 'evidence-index.json').write_text(json.dumps({
    'apkSha256': apk_sha, 'unitTests': tests, 'captures': captures,
    'retapTrace': trace,
    'retapIdentity': 'Same third grid position, no reshuffle or navigation between selection and retap; card-ID reducer behavior covered by JVM tests. Selected-state semantics use inset child bounds, while the same tap coordinate lies inside both states.',
    'retapCoordinate': [tap_x, tap_y],
    'systemLog': {'file': 'android-test-window.log', 'sha256': digest(root / 'android-test-window.log'), 'filter': 'none; logcat -b all -d -v threadtime'},
}, ensure_ascii=False, indent=2))
with (root / 'SHA256SUMS').open('w') as output:
    for path in sorted(root.rglob('*')):
        if path.is_file() and path.name != 'SHA256SUMS':
            output.write(f'{digest(path)}  {path.relative_to(root)}\n')
print(json.dumps(tests))
