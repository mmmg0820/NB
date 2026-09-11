# Saju Metadata R2

The S1 primary-result metadata issue is fixed. The primary card shows only `대한민국 표준시`; `계산 기준` opens a dialog with the solar-term name and a Korean date/time including timezone. The exact timestamp remains in calculation evidence. The `검토 필요` badge, engine, pillar order, and unknown-hour behavior are unchanged.

## Frozen Candidate

- APK: `sharomyang-saju-metadata-r2-debug.apk`
- APK/installed-base SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- Source snapshot SHA-256: `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683`
- Source manifest SHA-256: `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85`
- Signing certificate SHA-256: `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab`
- Only `SajuChartDisplay.kt` and `SajuChartDisplayTest.kt` changed in application source.
- Prior APK `89d40ac0...` remains preserved as superseded history.

## Verification

- Clean offline full unit suite and assemble: PASS, 94 tests, zero failures/errors/skips. See `build-final.log` and `unit-results/TEST-*.xml`.
- Pixel_10, emulator-5556, API 37, 1080x2424, density 420, font scale 1.0.
- Known/unknown time in light/dark: PASS. See `saju-known-light`, `saju-unknown-light`, `saju-known-dark`, `saju-unknown-dark` PNG/XML pairs.
- Localized calculation dialog: PASS in light and dark (`saju-calculation-details-*`).
- 78-card grid: PASS. Three-card selection 0/3, 2/3, 3/3 with drawer, then same third grid card retapped to 2/3 with drawer absent: PASS (`tarot-*` PNG/XML pairs).
- The retap uses the same coordinate inside the third card without reshuffling; the first two cards retain selection order. Selected-state semantics expose inset child bounds, but that coordinate lies inside both states. Card-ID reducer behavior is covered by the unchanged JVM tests.
- OPS independently reported exact-SHA Pixel 9a dark unknown-result/dialog PASS.

## Evidence Integrity

`evidence-index.json` binds each PNG/XML to this APK SHA and shared device configuration in `runtime-metadata.txt`. Hierarchy packages establish application UI ownership. Device dimensions, density, and font scale were unchanged throughout; theme changes are named in each Saju capture. `SHA256SUMS` covers all evidence files and the raw log.

Unfiltered all-buffer Android log: [android-test-window.log](android-test-window.log). It includes app launches beginning at 09:16:37 and the successful regression captures through 09:23. No app `am_anr`, `am_crash`, or FATAL EXCEPTION was found. Three unrelated emulator startup ANRs (Messaging, captions, Play Store) occurred before the app test window and remain visible in the unfiltered log.

Initial harness attempts encountered UI hierarchy/engine readiness delays and a stale checkbox label in the test script. The harness was corrected, successful light captures retained, and dark plus Tarot cases resumed. These automation failures are recorded in `qa-run.log`, `qa-dark-run.log`, and the successful `qa-completion.log`; no application source changed during device QA.

This report covers the requested S1 follow-up regression, not an expanded device/accessibility matrix or store-release approval.
