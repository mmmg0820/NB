# Sharomyang HyperOS Final Integration

## Build

- Clean offline build: PASS
- Unit tests: 92 PASS
- APK: `sharomyang-hyperos-final-debug.apk`
- APK SHA-256: `89d40ac05d74988c0d6e348b60199e91bd57b8e4800c3c6a81fc94d752ef2a7c`
- Package: `com.hoscat.mtj.dev`
- Version: `1` / `0.1-dev`
- Signing certificate SHA-256: `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab`

## Implemented

- HyperOS Design Standard light/dark contrast, semantic tokens, one bottom navigation, and launcher icon resources.
- One-field `YYYYMMDD` and one-field `HHmm` input with `BirthInputDraft` mapping and updated Korean validation copy.
- Saju result order: 명식, 일간, 연주, 월주, 일주, 시주. Unknown time is explicit and does not fabricate an hour pillar.
- Internal `policyCode` and `dataVersion` remain limited to the information destination.
- Shared Home/Tarot question, 8 spread-shape categories covering 27 readings, and actual spread previews.
- 78-card fixed 8x10 picker, direct select/deselect, no picker bottom navigation, manual result drawer, and Back restoration.
- Record filters: 전체, 사주, 타로.

## Pixel 10 Evidence

- Light Home: `pixel10-home-light.png`
- Saju input: `pixel10-saju-input.png`
- Unknown-time Saju result: `pixel10-saju-unknown-result.png`
- Dark Saju/input state: `pixel10-saju-unknown-result-dark.png`
- Dark Tarot categories: `pixel10-tarot-categories-dark.png`
- Dark 78-card picker: `pixel10-tarot-picker-dark.png`
- Select/deselect state: `pixel10-tarot-picked.xml`, `pixel10-tarot-unpicked.xml`
- Completed picker drawer: `pixel10-tarot-complete-peek-dark.png`
- Three-card picker: `pixel10-tarot-picker-0of3-dark.png`, `pixel10-tarot-picker-3of3-dark.png`
- Same-card deselection closes the drawer: `pixel10-tarot-picker-2of3-after-retap-dark.png`
- Tarot result and Back restoration: `pixel10-tarot-result-dark.png`, `pixel10-tarot-back-restored.xml`
- Record filters: `pixel10-records-filters-dark.png`
- Runtime/install identity: `pixel10-runtime-metadata.txt`
- Clean-launch logcat: `pixel10-launch-logcat.txt`, `pixel10-logcat-summary.txt`

## Source Freeze

- Snapshot: `source-snapshot.tar.gz`
- Snapshot SHA-256: `61829673c1b1b13a2711f30ff18e66caf4ac1333acd3cb01f21c7f458e48c967`
- Source manifest: `source-manifest.sha256`
- Source manifest SHA-256: `c9ef839d3e082f98d5df0a7d44e650f6997ae4848310c1097d037589e5830485`
- Build log: `build-test.log`
- Changed file inventory: `changed-files.txt`
