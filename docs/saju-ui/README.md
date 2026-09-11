# 사주 입력·결과 UI 인수인계

작성일: 2026-09-11  
담당 산출물: `saju-ui/README.md`  
기준 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3` (`v0.1-dev-20260911-r2`)  
패키지: `com.hoscat.mtj.dev`

## 목적과 범위

이 문서는 샤로먕 Android의 사주 입력 화면과 사주 결과 첫 화면을 새 유지보수자가 안전하게 이어받기 위한 설명서다. 범위는 사주 입력값 수집, 시간 모름 처리, 결과 명식 표시, 검토 배지와 계산 기준 상세 문구, 접근성·반응형 기준, 시각 회귀 검증 절차다.

이 문서는 공유 Git checkout을 수정하거나 배포하지 않는다. 실제 Android 소스 적용과 APK 생성은 Android 개발 전담이 소유한다.

## 현재 구현 동작

사주 입력은 `MainActivity.kt`의 사주 탭에서 처리한다. 별칭, `생년월일`, 양력/음력, 윤달, `태어난 시간`, `시간 모름`, 성별을 입력받고, CTA는 `내 명식 보기`다. 결과가 아직 없을 때만 입력 폼에 세로 스크롤이 붙고, 결과가 생성되면 결과 첫 화면은 스크롤 없는 상태로 `SajuChartDisplay(chart)`를 보여준다.

시간 모름이 선택되면 `BirthInputDraft.hour`와 `BirthInputDraft.minute`는 빈 문자열로 전달된다. 화면의 시간 입력 필드는 비활성화되고 supporting text와 checkbox label 모두 `시간 모름`을 사용한다. 이 상태에서 시주를 추정 생성하면 안 된다.

결과 화면은 `SajuChartDisplay.kt`가 담당한다. 상단에는 제목 `명식`, 사용자 별칭, 검토 배지(`검토 필요`, `내부 구조 검토됨`, `내부 검증됨`, `외부 기관 검증됨`)가 표시된다. 이어서 `일간`과 한자·한글 일간, 오행 보조 문구가 나오고, 명식 표는 `연주 / 월주 / 일주 / 시주` 순서를 유지한다.

명식 글자는 한자 위, 한글 아래 구조다. 예를 들어 `戊`와 `무`를 분리해 보여준다. 시주가 없으면 `시주` 칸은 `시간 모름`을 표시하고 천간·지지 한자를 비워 둔다. 보조 문구는 `오행 산출 제외`다.

기준 커밋 `b4ffe87`에서 계산 메타데이터 노출 방식이 바뀌었다. 결과 본문에는 `대한민국 표준시`만 짧게 표시하고, `계산 기준` 버튼을 누르면 dialog에서 `이전 절입: 소한`, `절입 시각: 1995년 1월 6일 04:34:04 (한국 표준시, UTC+09:00)`처럼 지역화된 상세를 보여준다. `policyCode`, `dataVersion`, `manse-seed`, ISO 원문 timestamp 같은 내부값은 사용자 화면에 노출하지 않는다.

## 소스 소유권과 파일

Android 전담 정본 경로는 `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/work/cell/android`다. 이 문서 작성자는 해당 소스를 직접 수정하지 않았고, S-01 관련 격리 patch와 문서만 제공했다.

주요 파일:

- `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt`: 사주 입력 폼, 결과/입력 전환, 시간 모름 입력 계약, 결과 첫 화면 스크롤 제어
- `android/app/src/main/java/com/hoscat/mtj/dev/SajuChartDisplay.kt`: 결과 명식 UI, 4주 표시, 검토 배지, 계산 기준 상세 dialog, 반응형 column rule
- `android/app/src/main/java/com/hoscat/mtj/dev/BirthDateValidation.kt`: 생년월일 검증과 사용자 표시용 오류 문구
- `android/app/src/main/java/com/hoscat/mtj/dev/BirthTimeValidation.kt`: 시간 검증과 사용자 표시용 오류 문구
- `android/app/src/main/java/com/hoscat/mtj/dev/RecordDetailRows.kt`: 기록 상세에서 시간 모름과 명식 요약 표시
- `android/app/src/test/java/com/hoscat/mtj/dev/SajuChartDisplayTest.kt`: 명식 순서, 시간 모름, 검토 배지, 계산 기준 상세, 반응형 column rule 단위 테스트
- `android/app/src/test/java/com/hoscat/mtj/dev/BirthDateValidationTest.kt`: 생년월일 표시 오류 계약
- `android/app/src/test/java/com/hoscat/mtj/dev/BirthTimeValidationTest.kt`: 시간 표시 오류 계약

현재 작업 copy에서 확인한 주요 SHA-256:

- `SajuChartDisplay.kt`: `eb00dfddaaeb7613db693e56bff024ebc7b8db802db4772517bba956878651f0`
- `MainActivity.kt`: `0ec4245eca7fa88d3f255417635f0b525371406224bd4233b676b410aaa2cf7e`
- `SajuChartDisplayTest.kt`: `d8db7574c6c061295e3d30656c2f3261c75e2a28d22423fa6a3a923052945f14`

## 데이터와 상태 계약

사주 결과 UI는 `SajuChart`를 입력으로 받는다. `yearPillar`, `monthPillar`, `dayPillar`, `hourPillar`, `dayMaster`, `evidence`가 표시 계약의 핵심이다.

`sajuChartPillarDisplays(chart)`는 항상 `연주`, `월주`, `일주`, `시주` 순서의 4개 display를 반환해야 한다. `일주`만 `isDayMaster = true`다. `chart.hourPillar == null`이면 `시주` display는 `isUnknownHour = true`, `stateText = "시간 모름"`, `stemHanjaText = ""`, `branchHanjaText = ""`, `elementText = "오행 산출 제외"`가 되어야 한다.

검토 배지는 `sajuChartAuthorityLabel(chart)`가 결정한다. 외부 검증과 내부 검증, 내부 구조 검토, 검토 필요 상태를 사용자 문구로만 반환해야 한다. 내부 enum 이름이나 증거 필드 이름을 그대로 보여주면 안 된다.

계산 기준 요약은 `sajuChartBasisLabel(chart)`가 `calculationBasisLabel`만 반환하는 구조다. 상세 dialog는 `sajuCalculationDetails(chart)`가 만들며, 절입 timestamp는 한국어 날짜 형식으로 변환한다. 파싱 실패 시 raw value 대신 `절입 시각을 표시할 수 없습니다.`를 보여준다.

## 반응형 규칙과 첫 viewport 계약

정상 글자 크기에서는 4주가 한 줄에 표시되어야 한다. 즉 `연주 / 월주 / 일주 / 시주`가 한 행의 4열로 읽혀야 한다.

접근성 큰글씨에서는 가독성을 우선한다. 최신 승인 기준은 `320dp` 폭과 `fontScale 2.0`에서 2x2 fallback을 허용하고 요구한다. 이 fallback은 다음 조건을 모두 만족할 때만 통과다.

- `명식`, `일간`, `연주`, `월주`, `일주`, `시주`가 첫 viewport 안에 보인다.
- 8글자 핵심 한자·한글이 잘리거나 겹치지 않는다.
- 시간 미상 시 `시간 모름`이 첫 viewport 안에 명확히 보인다.
- 시간 미상 시 시주 천간·지지를 만들지 않는다.
- 첫 결과 확인에 세로 스크롤이 필요하지 않다.

현재 기준 커밋의 단위 테스트는 `fontScale >= 1.3f`에서 2열을 반환하도록 검증한다. 따라서 `411dp/fontScale 1.5`도 2열이 될 수 있다. 디자인이 “넓은 화면의 큰글씨는 4열 유지”로 바뀌면 `sajuPillarColumnCount`와 테스트를 함께 조정해야 한다.

## 접근성 매트릭스

필수 확인 조합:

- 360dp, fontScale 1.0, light: 4주 one-row, clipping 0
- 360dp, fontScale 1.0, dark: 4주 one-row, clipping 0
- 411dp, fontScale 1.0, light/dark: 4주 one-row, clipping 0
- 600dp, fontScale 1.0, light/dark: 4주 one-row, clipping 0
- 320dp, fontScale 2.0, light/dark: 2x2 fallback, 첫 viewport 안에 핵심 정보 전체 표시
- 시간 모름 on/off 양쪽: 시주 생성 여부와 `시간 모름` 표시 확인
- TalkBack 또는 UIAutomator XML: `시주, 시간 모름, 오행 산출 제외` 의미가 content description으로 읽히는지 확인

기준 커밋의 실제 device evidence는 Pixel 10 AVD, API 37, 1080x2424, density 420, fontScale 1.0이다. 320dp/fontScale 2.0 실기기 또는 emulator evidence는 별도 follow-up으로 남아 있다.

## 빌드와 테스트 명령

NB checkout 기준:

```bash
cd "/Volumes/뽀그리/Project 먕/사로먕/github/NB/android"
ANDROID_USER_HOME=/Volumes/MTJNativeBuild/android-user \
GRADLE_USER_HOME=/Volumes/MTJNativeBuild/gradle \
./gradlew --offline --no-daemon clean assembleDebug testDebugUnitTest
```

Android 전담 working copy 기준:

```bash
cd "/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/work/cell/android"
ANDROID_USER_HOME=/Volumes/MTJNativeBuild/android-user \
GRADLE_USER_HOME=/Volumes/MTJNativeBuild/gradle \
./gradlew --offline --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

시각 회귀는 APK 설치 뒤 device lease가 잡힌 상태에서만 실행한다. 기준 커밋의 예시는 다음 스크립트다.

```bash
python3 "/Volumes/뽀그리/Project 먕/사로먕/github/NB/evidence/saju-metadata-r2/qa_pixel10.py"
```

스크립트는 `emulator-5556`과 `/Users/thomaslee/Library/Android/sdk/platform-tools/adb`를 하드코딩한다. 다른 device lease를 쓰면 serial과 출력 경로를 반드시 바꾸고 evidence metadata에 lease id를 기록한다.

## 시각 회귀 절차

1. 깨끗한 APK를 빌드하고 SHA-256을 계산한다.
2. DevOps Pixel 또는 emulator lease를 확보한다. lease 없이 adb, install, capture, connected test를 수행하지 않는다.
3. APK를 설치한 뒤 `/data/app/.../base.apk`의 SHA-256이 배포 APK와 같은지 확인한다.
4. light mode에서 사주 탭 진입, 별칭 입력, `생년월일` 입력, 시간 모름 on 상태로 `내 명식 보기`를 누른다.
5. 결과 첫 화면에서 `명식`, `검토 필요`, `일간`, `연주`, `월주`, `일주`, `시주`, `시간 모름`, `대한민국 표준시`가 있는지 XML과 PNG를 함께 캡처한다.
6. `계산 기준` dialog를 열어 한국어 절입 시각과 timezone 문구를 확인하고 raw timestamp가 없는지 검사한다.
7. `정보 수정`으로 돌아가 시간 모름을 off로 바꾸고 `태어난 시간`을 입력한 뒤 known-time 결과를 캡처한다.
8. dark mode에서도 unknown/known 결과와 계산 기준 dialog를 반복한다.
9. 320dp/fontScale 2.0 조합에서 2x2 fallback이 첫 viewport 계약을 만족하는지 별도 캡처한다.
10. 모든 PNG/XML, build log, unit XML, release manifest, runtime metadata, APK SHA를 같은 evidence bundle에 묶는다.

## 인수 기준

- 입력 copy가 `생년월일`, `태어난 시간`, `시간 모름`, `내 명식 보기`로 유지된다.
- 시간 모름 on일 때 hour/minute가 빈 문자열로 전달되고, 결과 시주가 임의 생성되지 않는다.
- 결과 제목은 `명식`이고 사용자명은 별도 보조 문구로 표시된다.
- 4주 순서는 `연주 / 월주 / 일주 / 시주`다.
- 정상 글자 크기에서는 4주가 한 줄이다.
- `320dp/fontScale 2.0`에서는 2x2 fallback이 허용되며 첫 viewport 안에서 잘림, 겹침, 스크롤, 조작된 시주 문자가 없어야 한다.
- `검토 필요` 등 검토 배지는 사용자 문구로만 표시된다.
- 결과 본문에는 `대한민국 표준시`만 짧게 보이고, 상세는 `계산 기준` dialog로 분리된다.
- `policyCode`, `dataVersion`, `myeongri_kr_v2`, `manse-seed`, raw ISO timestamp가 사용자 화면과 접근성 XML에 노출되지 않는다.
- light/dark 캡처와 XML이 같은 APK SHA에 묶여 있어야 한다.

## 증거 경로와 해시

기준 커밋:

- `b4ffe8718bf01b02930468451baa4ecab45f1db3`
- 태그: `v0.1-dev-20260911-r2`
- 커밋 메시지: `Move Saju calculation metadata into localized details`

NB evidence:

- Report: `/Volumes/뽀그리/Project 먕/사로먕/github/NB/evidence/saju-metadata-r2/REPORT.md`
- Report SHA-256: `341e2da52f960d138f8efb0b5ba3d81e5a66f3361a551e36fc1532f473c33ebd`
- APK: `evidence/saju-metadata-r2/sharomyang-saju-metadata-r2-debug.apk`
- APK SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- Source snapshot SHA-256: `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683`
- Source manifest SHA-256: `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85`
- Signing certificate SHA-256: `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab`
- QA verdict: `/Volumes/뽀그리/Project 먕/사로먕/github/NB/evidence/saju-metadata-r2/qa-verdict.json`
- QA verdict SHA-256: `2fd15dcc373a9ad38fd4befe88e09d7fef5d34019a0d04a51fafa0df8d06c4d0`
- Runtime metadata SHA-256: `83bf5968e20677309264e90461f4e7ee908d157492b6c4fcff7cdcc5a3275d27`
- Full SHA list: `/Volumes/뽀그리/Project 먕/사로먕/github/NB/evidence/saju-metadata-r2/SHA256SUMS`

SAJ-UI-1 handoff artifacts:

- S-01 handoff: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-1-saj-saj-001-saj/outputs/saj-s01-result-first-viewport-handoff-20260911.md`
- S-01 handoff SHA-256: `f053735893702bd3792a4eb25705d1ebcb7218837ad85171cf6feaff5af1907a`
- S-01 isolated patch: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-1-saj-saj-001-saj/outputs/saj-s01-result-first-viewport-r4-isolated.patch`
- S-01 patch SHA-256: `b2d4522c0806c41129a17a629e0e28b72512e20bd1a2a719e2564908beb05bbc`
- Status ledger: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-1-saj-saj-001-saj/outputs/design-rollout-status-20260911.json`
- Status ledger SHA-256: `25ddf29dcbca3858fabe94bec61878e96c443350c6024ff7c00c825443f0d758`

## 알려진 한계와 우선순위 follow-up

우선순위 1: 320dp/fontScale 2.0 evidence가 아직 기준 커밋 evidence bundle에 없다. 반드시 light/dark PNG와 XML을 추가하고 첫 viewport 계약을 확인해야 한다.

우선순위 2: 현재 column rule은 `fontScale >= 1.3f`면 폭과 무관하게 2열을 반환한다. 큰글씨에서도 411dp 이상은 4열을 유지해야 한다는 디자인 결정이 나오면 rule과 테스트를 바꿔야 한다.

우선순위 3: 기존 evidence는 Pixel 10 AVD, fontScale 1.0 중심이다. Pixel 9a 또는 실제 대상 기기에서 같은 APK SHA로 재검증해야 한다.

우선순위 4: UIAutomator XML은 텍스트 존재와 순서를 잘 잡지만 실제 clipping은 PNG 육안·이미지 분석이 필요하다. QA는 XML pass만으로 시각 승인 처리하지 않는다.

우선순위 5: `RecordSnapshots.kt`는 기록 envelope title에 `${name}님의 명식`을 유지한다. 현재 primary result UI 문제는 아니지만 기록 화면 copy 정책과 맞춰볼 필요가 있다.

## 의존성과 핸드오프 메모

- Android 개발 전담이 소스 적용, 빌드, APK 생성, GitHub push를 소유한다.
- SAJ-UI-1은 문서와 격리 patch 산출물을 제공한다.
- DevOps Pixel lease 없이 adb/install/capture/connected test를 실행하지 않는다.
- 디자인 판단 소스는 `고급 프리미엄 디자이너`와 `MTJ 디자인 전담`이다. `먕디자인TF`는 MTJ 디자인 승인 근거로 사용하지 않는다.
- 증거는 APK SHA와 source snapshot SHA, runtime metadata, device serial, density, size, fontScale, capture time을 함께 보관해야 한다.
- GitHub NB checkout에서 `git show`를 실행할 때 AppleDouble pack index 경고(`non-monotonic index .git/objects/pack/._pack...idx`)가 보일 수 있었다. 기준 커밋 조회는 성공했지만, 추후 repo hygiene 작업에서 정리 대상이다.

## 새 유지보수자 첫날 체크리스트

- [ ] 기준 커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3`와 태그 `v0.1-dev-20260911-r2`를 확인한다.
- [ ] `SajuChartDisplay.kt`의 `sajuChartPillarDisplays`, `sajuPillarColumnCount`, `sajuPillarGridMode`, `sajuChartAuthorityLabel`, `sajuCalculationDetails`를 먼저 읽는다.
- [ ] `MainActivity.kt`에서 결과가 있을 때 content scroll을 제거하는 분기와 시간 모름 입력 계약을 확인한다.
- [ ] `SajuChartDisplayTest.kt`의 320dp/fontScale 테스트가 최신 acceptance와 맞는지 본다.
- [ ] `REPORT.md`, `qa-verdict.json`, `runtime-metadata.txt`, `SHA256SUMS`를 열어 APK SHA와 device evidence를 대조한다.
- [ ] 320dp/fontScale 2.0 light/dark 캡처가 없으면 가장 먼저 추가한다.
- [ ] raw metadata 문자열이 화면, XML, 접근성 설명에 노출되지 않는지 검색한다.
- [ ] 소스 변경 전 Android 전담 owner와 파일 범위를 맞춘다.
- [ ] 빌드 후 APK SHA와 installed base APK SHA가 같은지 확인한다.
- [ ] 새 evidence를 만들면 이 README의 evidence section 또는 다음 handoff 문서에 SHA를 추가한다.
