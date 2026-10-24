"""Check the actual Kotlin palette tokens against WCAG and docs/research/ui-style.md (no third-party deps).

For every mode: text pairs at least 4.5:1 (3:1 where a hue is marked large-text only), and control
boundaries at least 3:1. The colourful mode must match the doc's palette table, muted must keep the
doc's lips with fills moved 55% toward their tints, and high contrast must use the deep shades as fills.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[2]
source = (root / 'android/app/src/main/kotlin/com/nuha/app/ui/identity/Identity.kt').read_text()
doc = (root / 'docs/research/ui-style.md').read_text()
ROLES = ('fill', 'lip', 'onFill', 'deep', 'tint')
CONSTANTS = dict(re.findall(r'private const val (\w+) = 0xFF([0-9A-F]{6})', source))
LIFT = .08  # IdentityPalette.lift outside high contrast

def block(name):
    match = re.search(rf'private val {name} = \w+(?:\.copy)?\((.*?)\n\)', source, re.S)
    assert match, name
    return match.group(1)

def parse(body, base=None):
    tokens = {k: dict(v) if isinstance(v, dict) else v for k, v in (base or {}).items()}
    tokens.update(re.findall(r'(ink|paper) = Color\(0xFF([0-9A-F]{6})\)', body))
    for name, args in re.findall(r'(\w+) = hue\(([^)]*)\)', body):
        parts = [a.strip() for a in args.split(',')]
        values = [CONSTANTS.get(a, a.removeprefix('0xFF')) for a in parts[:5]]
        tokens[name] = dict(zip(ROLES, values), largeOnly='largeOnly = true' in parts)
    return tokens

def luminance(hex_colour):
    rgb = [int(hex_colour[i:i+2], 16) / 255 for i in (0, 2, 4)]
    linear = [v / 12.92 if v <= .04045 else ((v + .055) / 1.055) ** 2.4 for v in rgb]
    return sum(v * w for v, w in zip(linear, [.2126, .7152, .0722]))

def ratio(a, b):
    light, dark = sorted((luminance(a), luminance(b)), reverse=True)
    return (light + .05) / (dark + .05)

def mix(a, b, t):
    """sRGB mix, as Color.lifted() does in Kotlin."""
    return ''.join('%02X' % round(int(a[i:i+2], 16) + (int(b[i:i+2], 16) - int(a[i:i+2], 16)) * t) for i in (0, 2, 4))

def camel(name):
    first, *rest = name.split()
    return first.lower() + ''.join(w.title() for w in rest)

modes = {'colourful': parse(block('Colourful'))}
modes['muted'] = parse(block('Muted'), modes['colourful'])
modes['contrast'] = parse(block('HighContrast'))
hues = [k for k, v in modes['colourful'].items() if isinstance(v, dict)]
assert len(hues) == 8, hues

# The colourful mode is the doc's table, value for value.
table = re.findall(r'^\| (\w[\w ]*?) \| #([0-9A-F]{6}) \| #([0-9A-F]{6}) .*?\| #([0-9A-F]{6}) .*?\| #([0-9A-F]{6}) .*?\|$', doc, re.M)
assert len(table) == 8, table
for name, fill, lip, deep, tint in table:
    t = modes['colourful'][camel(name)]
    assert (t['fill'], t['lip'], t['deep'], t['tint']) == (fill, lip, deep, tint), (name, t)
neutrals = dict(re.findall(r'\*\*(ink|paper) #([0-9A-F]{6})\*\*', doc))
assert (modes['colourful']['ink'], modes['colourful']['paper']) == (neutrals['ink'], neutrals['paper']), neutrals
for name in hues:
    base, muted, high = modes['colourful'][name], modes['muted'][name], modes['contrast'][name]
    assert muted['fill'] == mix(base['fill'], base['tint'], .55), (name, muted['fill'])
    assert (muted['lip'], muted['deep'], muted['tint']) == (base['lip'], base['deep'], base['tint']), name
    assert high['fill'] == base['deep'] and high['lip'] == modes['contrast']['ink'], name

failures = []
def need(mode, label, a, b, minimum):
    value = ratio(a, b)
    if value < minimum:
        failures.append(f'{mode}: {label} {value:.2f} < {minimum}')
    return value

for mode, t in modes.items():
    lift = 0 if mode == 'contrast' else LIFT
    worst = {}
    need(mode, 'ink on paper', t['ink'], t['paper'], 4.5)
    need(mode, 'action lip text on paper', t['amethyst']['lip'], t['paper'], 4.5)
    for name in hues:
        h = t[name]
        text = 3 if h['largeOnly'] else 4.5
        worst[name] = round(min(
            need(mode, f'{name} onFill on fill', h['onFill'], h['fill'], text),
            need(mode, f'{name} onFill on lifted fill', h['onFill'], mix(h['fill'], 'FFFFFF', lift), text),
            need(mode, f'{name} ink on tint', t['ink'], h['tint'], 4.5),
            need(mode, f'{name} white on deep', 'FFFFFF', h['deep'], 4.5),
            need(mode, f'{name} lip edge vs paper', h['lip'], t['paper'], 3),
        ), 2)
    print(mode, worst)

assert not failures, '\n'.join(failures)
print('All text pairs >= 4.5:1 (3:1 large-only) and boundaries >= 3:1 in all three modes')
