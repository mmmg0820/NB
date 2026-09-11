# 2026-09-11 battery-deadline integration

WIP ONLY — preserved on `wip/saju-unavailable-state-20260911`, not merged to `main`. The emergency stop interrupted finalization. Git-only resumption does not authorize further builds, tests, device QA, or correction work. Known duplicate-copy and clipping caveats remain open.

This is a source/test handoff, not a new release or full visual acceptance. R2 release artifacts, tags, and existing release evidence remain unchanged.

## Scope and provenance

- Base: remote `main` at `812865cfff3c6726d0adc40d1830f5827f0307b7`.
- Included: SAJ-UI two-file Saju unavailable-hour presentation patch, applied independently to the base. Unknown-hour adaptive weighting and single-line text guards; one additional unit test. No calculation, Tarot selection, Records, new design asset, or signing configuration changes.
- Patch: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-1-saj-saj-001-saj/outputs/p0-saju-unavailable-state-20260911/p0-saju-unavailable-state.patch`; SHA256 `8ce4052cbd4d1e258e48eb82c3c819737aaa45a7f4205ca4d6cf0a7f86fa3f04`.
- Source report: same directory, `REPORT.md`; SHA256 at initial inspection `a928ac2af6db498a1febc2b13d3d5a1bfb124c3a6a0b6714113f7dd56a8b11de`. The owner subsequently reported updating this mutable report with the review caveats; the initial hash is historical provenance, not a claim about its current bytes.
- Integrated `SajuChartDisplay.kt`: SHA256 `d76772659a31658518137a89e1d4508442c79f86c2f06372504947c17afab022`.
- Integrated `SajuChartDisplayTest.kt`: SHA256 `5d023ad1f2aef9806b9faf8f647e365c3d9408a3bbb83ae1155156d2afe90b2c`.
- Team-reported candidate APK SHA256 `45b7fd490a4658ae7ba5e801ae9ad4ff2ee2d471bc2da40f23d005c47fbe1e5a`, independently hash-checked and frozen at `/Volumes/뽀그리/Project 먕/샤로먕/evidence/p0-saju-unavailable-state-45b7fd49-20260911/builds/sharomyang-45b7fd49-debug.apk`. This is NOT the independently rebuilt integration APK.

## Independent verification

Build root: `/Volumes/MTJNativeBuild/nb-p0-integration.ckaDV9/android`, a fresh external native-filesystem staging directory containing the reviewed integration source, excluding generated outputs and AppleDouble metadata. No shared Android worktree was modified.

Command: Gradle 8.11.1 `--offline --no-daemon :app:testDebugUnitTest :app:assembleDebug`, Android Studio JBR, SDK `/Users/thomaslee/Library/Android/sdk`, `GRADLE_USER_HOME=/Volumes/MTJNativeBuild/gradle`, `ANDROID_USER_HOME=/Volumes/MTJNativeBuild/android-user`.

Result before emergency stop: Gradle returned `BUILD SUCCESSFUL in 50s`, 41 actionable tasks executed, including unit-test and assemble tasks. Detailed XML totals and integration APK SHA were not audited before the stop. No test/build was rerun during Git-only resumption; no device/design PASS is claimed.

## Deferred and rejected from this commit

- Records/state: submitted `RecordsScreen.kt` SHA256 `8de549517e5ab4d27895137a09a97db4190532f1c8282e43d37e87b231025cd5`, test SHA256 `1229a0d68357a448dd077dd5bed1479b9bbeb4aa5b8c2a0741ce69cb36eb5da9`. Source root `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android`. Whole-file replacement removes `initialSelectedId` and its hydration effect, while current `MainActivity.kt` calls `RecordsScreen(store, selectedRecordId)`. Incompatible; excluded rather than breaking record deep links. Owner reported tests/build passing in a different tree, APK SHA256 `9e0ef5a99987d9073e13c366f8648b61bc9f306ece3f5a4c63a68ea3d4209cdb`. That does not verify compatibility here. Port minimal filter/state changes onto current API later.
- Tarot result P1: same source root; `TarotScreen.kt` SHA256 `2ce2de232a101e7e62f75f59173642c2240e1d26f903f63de736c8c1cdefae9d`, `TarotSpreadOverviewPolicy.kt` SHA256 `c6b045401aae5bcadf4c033b86c54afcf56e333b2695619a63bdabe1c31dc2b0`, test SHA256 `e84a7f1c9ad4403160536ae746d9997bba3231bf5f81936ac93143be353c3737`. Hashes checked. Whole-screen diff includes unrelated selection redesign, removes current BackHandlers/selection board, and changes question callback API. Entire batch deferred; isolate result-only hunks against current main before testing. No failed/undiscovered connected-test evidence or `TarotResultLayoutDeviceTest.kt` included.
- Unverified Tarot P0, incomplete corrections, and `shippingAllowed=false` PNG/JPEG assets: excluded.
- A-01 / T-05 / R-01 pending work remains pending as described in the existing backlog.

## Acceptance limits and next action

Device/visual QA is pending, not PASS. At handoff, SAJ-UI was assigned Pixel_9a `emulator-5554` lease `lease-20260911T102800KST-saj-ui-emulator-5554-45b7fd49`, 10:28–11:28 KST, restricted to source candidate SHA `45b7fd49...`. This historical note is not current device authorization. Release integrator issued no ADB/install/capture commands. Other apps, physical-device lease, and shared emulator remain untouched.

Source review notes requiring visual follow-up: in standard mode the unknown-hour element label appears both inside its new state component and in the existing outer footer. `softWrap=false` plus `TextOverflow.Clip` prevents vertical wrapping but does not prove the full label is visible. Verify normal-width four-pillar row, 320dp/fontScale2 2x2 fallback, full labels, duplicate supporting copy, first viewport and dark contrast. Do not claim P0 visual closure solely from unit tests. Any install of the integration APK needs a lease amended to its own SHA.

This additive status note does not revise the immutable earlier 13-team documentation import manifest or R2 release manifest. No new release tag is authorized.
