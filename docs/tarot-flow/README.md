# 샤로먕 타로 플로우 유지보수 문서

기준 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3`  
태그: `v0.1-dev-20260911-r2`  
대상 앱 패키지: `com.hoscat.mtj.dev`

이 문서는 홈에서 타로로 이어지는 질문 유지, 스프레드 카테고리/리딩 선택, 78장 카드 선택 상태 머신, 선택/해제/결과 서랍/뒤로가기 동작을 새 유지보수자가 빠르게 이해하고 검증할 수 있도록 정리한 것이다. 공유 Git 체크아웃은 이 문서 작성 과정에서 수정하지 않았다.

## 목적과 범위

- 홈 화면의 질문 입력이 타로 탭에서도 같은 상태로 유지되는 구조를 설명한다.
- 타로 시작 화면이 27개 스프레드를 8개 형태 카테고리로 묶어 선택하는 현재 UX를 설명한다.
- 78장 카드 피커의 선택 상태, 재탭 취소, 선택 완료 후 결과 서랍, 뒤로가기 동작을 설명한다.
- 관련 Kotlin 파일, 테스트 파일, 데이터 계약, 증거 경로와 SHA를 연결한다.
- 아직 남은 한계와 우선순위 높은 후속 작업을 기록한다.

범위 밖:

- 사주 결과 화면 구현 세부사항.
- 기록/설정 화면의 별도 UX.
- 스토어 릴리스 승인 또는 운영 배포 판단.
- 새로운 디자인 시안 제작.

## 현재 구현 동작

### 홈에서 타로 질문 유지

`MainActivity.kt`가 `tarotQuestion`을 `rememberSaveable` 상태로 보관한다. 이 상태는 홈 화면 `MtjHomeScreen`과 타로 화면 `TarotScreen`에 같은 값과 변경 콜백으로 전달된다.

- 홈의 질문 입력 필드에서 입력한 값은 `tarotQuestion`에 저장된다.
- 홈의 `과거 · 현재 · 미래 / 3장` 영역을 탭하면 `onStartTarot`가 실행되어 `tab = 2`가 되고 타로 화면으로 이동한다.
- 타로 화면의 질문 입력도 같은 `tarotQuestion` 상태를 읽고 갱신한다.
- 홈으로 돌아갔다가 다시 타로로 이동해도 같은 saveable 상태가 유지된다.

현재 구현상 홈의 3장 영역은 타로 탭으로 이동시키지만, 즉시 `three_cards:past_present_future`를 자동 시작하거나 해당 스프레드를 강제 선택하지는 않는다. 사용자는 타로 화면에서 카테고리와 리딩 목적을 선택한다. 이 점은 요구가 “홈 3장 영역 탭 즉시 과거·현재·미래 스프레드 하이라이트”로 다시 확정되면 후속 수정 대상이다.

### 카테고리와 스프레드 선택

타로 시작 상태에서 `selectedCategoryId == null`이면 8개 형태 카테고리를 보여준다.

- `한 장형`: `one_card`
- `가로형`: `two_cards`, `three_cards`
- `격자형`: `four_cards`, `six_cards`, `eight_cards`, `nine_cards`, `ten_cards`
- `십자형`: `five_cross`, `mini_celtic`, `celtic_cross`
- `V형`: `tarot_v`
- `곡선형`: `horseshoe`, `magic_seven`, `crow_seven`, `crow_eight`
- `원형`: `wheel_of_fortune`
- `두 갈래형`: `relationship_clearing`, `either_or_five`

카테고리를 탭하면 질문 입력, 역방향 포함 체크박스, 해당 카테고리의 리딩 목적 목록이 나온다. 각 리딩 행은 장수, subtitle, 미니 배치 프리뷰를 함께 보여준다. 리딩 행을 탭하면 질문 검증이 통과할 때 `TarotRecreationState.start(...)`로 세션을 시작한다.

정상 선택 가능 스프레드는 27개이며, `SpreadDrawMode.Normal`인 항목만 시작 화면에 노출된다. `final_one_from_ten:final`, `six_cards:relationship`, `yes_or_no:yes_no_signal`, `hammer_nail:hammer_nail_flow`, `decision_v:decision_flow`는 일반 선택 목록에 포함하지 않는다.

### 78장 카드 피커

카드 선택 화면은 `카드 고르기` 제목과 `선택수 / 필요장수`만 상단에 보여준다. 질문 반복, 스프레드 도식 반복, `N장을 골라주세요` 문구, 별도 선택/다음 버튼은 현재 화면에 없다.

`tarotOverviewGridSpec(...)`는 78장일 때 항상 8열 x 10행을 반환한다. 마지막 행은 6장이다. 이 고정 구조는 작은 화면에서도 모든 카드가 한 피커 화면 안에 배치되도록 하는 계약이다.

카드 탭은 `toggleTarotSelection(...)` 상태 머신으로 처리한다.

- 첫 탭: 해당 카드 id를 `selectedIds` 끝에 추가한다.
- 같은 카드를 다시 탭: 해당 카드 id를 `selectedIds`에서 제거한다.
- 필요 장수에 도달한 상태에서 미선택 카드를 탭: 무시한다.
- 필요 장수에 도달한 상태에서 이미 선택한 카드를 다시 탭: 취소된다.
- 취소 후 남은 선택 번호는 `selectedIds` 순서에 따라 다시 표시된다.
- deck에 없는 id는 무시된다.

선택된 카드는 카드 뒷면 위에 선택 순서 배지를 표시한다. 접근성 설명은 `카드 N, 1번째 선택` 또는 `카드 N, 선택 안 됨` 형태로 노출된다.

### 결과 서랍, 결과 화면, 뒤로가기

필요 장수만큼 선택하면 하단에 `결과 서랍` peek가 나타난다.

- 결과 서랍 탭 또는 위로 스와이프: `session.finish(deck)`로 결과를 잠그고 결과 화면으로 이동한다.
- 결과 서랍이 열린 상태에서도 아직 결과 화면으로 들어가기 전이면 선택된 카드를 재탭해 취소할 수 있다. 선택 수가 필요 장수보다 작아지면 서랍은 사라진다.
- 카드 선택 화면에서 Android 뒤로가기: 현재 스프레드의 카테고리를 다시 선택된 상태로 두고 스프레드 선택 화면으로 돌아간다.
- 결과 화면에서 Android 뒤로가기: `session.reopenSelection()`으로 잠금 상태를 풀고 같은 선택 상태의 카드 피커로 돌아간다.
- 결과 화면에서 `새로 뽑기`: 현재 스프레드 기준으로 새 시작 상태를 만든다.

## 정확한 소스 소유권과 파일

기준 커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3`의 주요 파일과 역할:

| 파일 | 역할 | 기준 SHA-256 |
|---|---|---:|
| `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt` | 홈/타로가 공유하는 `tarotQuestion` 상태와 탭 전환 소유 | `0ec4245eca7fa88d3f255417635f0b525371406224bd4233b676b410aaa2cf7e` |
| `android/app/src/main/java/com/hoscat/mtj/dev/MtjHomeScreen.kt` | 홈 질문 입력, 3장 영역 탭 진입 UI | source manifest에 개별 라인이 없으므로 커밋 기준 파일로 확인 |
| `android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt` | 타로 시작/선택/결과 화면 전체 Compose 플로우 | `fcd9df0e63469e6a4aeb1dedf3b50465cc7cdeb8eeeba38d55a378afb5d506c3` |
| `android/app/src/main/java/com/hoscat/mtj/dev/TarotSelectionFocusReducer.kt` | 실제로는 focus reducer가 아니라 `TarotSelectionState`와 `toggleTarotSelection` 보유 | `f27e2bc5d98ad39dab74ed4e77a83d72e825d1a9d9c6a7264b6b80d883c250ef` |
| `android/app/src/main/java/com/hoscat/mtj/dev/TarotSelectionGridContract.kt` | 카드 비율, 78장 8x10 grid 계약 | `361793725bf4f10e6731682826cd62c9e10dc984ee073b90847e84bb18d37935` |
| `android/app/src/main/java/com/hoscat/mtj/dev/TarotSpreadCategoryContract.kt` | 8개 카테고리와 27개 normal spread 매핑 | `37cd4590c854a3521ac3261a41bfdad3f82a6dfa005d540227ca297f7510467f` |
| `android/app/src/main/java/com/hoscat/mtj/dev/TarotRecreationState.kt` | 세션 저장/복원, 셔플, 선택, 잠금 결과, 재오픈 계약 | source manifest 전체에서 확인 필요 |
| `android/vendor/com/softcat/mystictarot/SpreadDefinitions.kt` | 원본 스프레드 정의, 좌표, 라벨, 숨김 목록 | `00d741adb15988338dbe9c54a6b2922e318e0b7a50f4370051bfc2e2271a9f90` |
| `tarot/src/MtjTarotEntry.kt` | 타로 엔진 entry와 결과 스냅샷 계층 | `960c170eb0567dcb19ec54dabc62cc3b3b40634c083a259f55c78e52172f9641` |

테스트 파일:

| 파일 | 검증 내용 | 기준 SHA-256 |
|---|---|---:|
| `android/app/src/test/java/com/hoscat/mtj/dev/TarotSelectionGridContractTest.kt` | 8x10 grid, 첫 탭 선택, 재탭 취소, limit/unknown id 무시 | `31a195eae88ac20c59d2d87c76bc78d322712e6a3db410f83cfe312ee90ac0f5` |
| `android/app/src/test/java/com/hoscat/mtj/dev/TarotSpreadCategoryContractTest.kt` | 8개 카테고리가 27개 normal key를 중복 없이 모두 커버 | `6177595b38bcd516aeffab41d0679a36361a68ea89068a352fd2e7ce8a68d576` |
| `android/app/src/test/java/com/hoscat/mtj/dev/TarotUiFlowContractTest.kt` | 빈 질문 차단, 1장도 78장 선택 플로우 사용, 선택 id 순서와 position label 유지 | unit XML 기준 PASS |
| `android/app/src/test/java/com/hoscat/mtj/dev/TarotRecreationStateTest.kt` | 세션 저장/복원, locked result, back/reopen, malformed state fail-closed 등 | unit XML 기준 PASS |

## 데이터와 상태 계약

### 질문 상태

- `tarotQuestion`은 `MainActivity`의 `rememberSaveable` 문자열이다.
- 홈과 타로 모두 같은 상태를 직접 읽고 갱신한다.
- 질문 검증은 `validateTarotQuestion(...)`와 `acceptsTarotQuestionInput(...)`이 담당한다.
- 질문은 시작 시 `TarotRecreationState.start(...)`에서 `trim()`되어 `questionAtStart`로 고정된다.

### 스프레드 계약

- `SpreadOption.key`는 결과/복원/기록의 식별 계약이므로 변경 금지.
- `layoutId`, `cardCount`, `positionLabels`, `drawMode`는 UI 선택과 결과 매핑을 동시에 결정한다.
- 일반 시작 화면은 `drawMode == SpreadDrawMode.Normal`만 사용한다.
- 카테고리 매핑은 layoutId 기준이다. 새 spread를 추가하면 `TarotSpreadCategoryContractTest`가 27개 고정 기대와 충돌할 수 있으므로 테스트도 의도적으로 갱신해야 한다.

### 카드 선택 상태

`TarotSelectionState`:

- `deckOrder`: 현재 셔플된 78장 카드 id 순서.
- `selectedIds`: 사용자가 선택한 카드 id 순서. 결과 position label은 이 순서를 따른다.
- `requiredCount`: 현재 스프레드가 요구하는 카드 수.

`toggleTarotSelection(...)`은 순수 함수다. UI에 의존하지 않으며, 단위 테스트로 선택/취소/limit/unknown id를 검증한다.

### 세션 저장/복원

`TarotRecreationState`는 Activity saved state 용도다. 내구성 있는 draft 저장소나 기록 schema를 추가하지 않는다.

저장되는 값:

- spread key
- 시작 여부
- seed
- 시작 질문
- 역방향 사용 여부
- catalog hash
- shuffled card ids 78개
- selected ids 최대 10개
- locked result metadata
- result hash
- failed flag

검증 규칙:

- 알 수 없는 spread key는 복원 실패 처리.
- 시작된 세션은 질문이 비어 있으면 안 된다.
- shuffled ids는 0..77 전체 permutation이어야 한다.
- selected ids는 spread card count 이하이고 중복이 없어야 한다.
- locked result는 reading/record id, timestamp, hash가 모두 유효해야 한다.
- restore 실패 시 fresh initializer로 조용히 떨어지지 않고 `failed = true` 상태로 닫힌다.

## 재현, 빌드, 테스트 명령

커밋 `b4ffe87`의 README 기준:

```sh
cd /Volumes/뽀그리/Project\ 먕/샤로먕/github/NB/android
ANDROID_USER_HOME=/path/to/android-user \
/tmp/gradle-8.11.1-dist/gradle-8.11.1/bin/gradle --no-daemon clean assembleDebug testDebugUnitTest
```

현장 빌드 로그의 실제 결과:

- `evidence/saju-metadata-r2/build-final.log`
- 결과: `BUILD SUCCESSFUL in 31s`
- 단위 테스트: 94건, 실패/오류/스킵 0

타로 플로우만 빠르게 확인할 때 유용한 테스트:

```sh
cd /Volumes/뽀그리/Project\ 먕/샤로먕/github/NB/android
/tmp/gradle-8.11.1-dist/gradle-8.11.1/bin/gradle --no-daemon \
  :app:testDebugUnitTest \
  --tests 'com.hoscat.mtj.dev.TarotSelectionGridContractTest' \
  --tests 'com.hoscat.mtj.dev.TarotSpreadCategoryContractTest' \
  --tests 'com.hoscat.mtj.dev.TarotUiFlowContractTest' \
  --tests 'com.hoscat.mtj.dev.TarotRecreationStateTest'
```

기기/에뮬레이터 QA 기준 증거:

- Device: Pixel_10 AVD
- Serial: `emulator-5556`
- API: 37
- Size: 1080x2424
- Density: 420
- Font scale: 1.0
- 설치 base APK SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`

## 인수 기준

홈/질문:

- 홈 질문 필드에 입력한 문자열이 타로 탭 질문 필드에 그대로 보인다.
- 타로에서 질문을 수정하면 홈 복귀 후에도 같은 값이 보인다.
- 빈 질문으로 리딩을 시작할 수 없다.
- 240자 초과 입력은 차단되거나 오류 상태로 표시된다.

스프레드 선택:

- 첫 화면에는 8개 형태 카테고리가 보인다.
- 8개 카테고리의 option 합계는 normal spread 27개와 정확히 일치한다.
- 숨김/특수 draw mode spread는 일반 선택 화면에 나오지 않는다.
- 카테고리 진입 후 질문, 역방향 체크, 리딩 목적 목록이 보인다.
- 리딩 선택 시 질문이 유효하면 78장 카드 피커로 진입한다.

카드 피커:

- 78장이 8열 x 10행으로 배치되고 마지막 행 6장이 접근 가능하다.
- 첫 탭은 선택이며, 별도 선택 버튼이 필요 없다.
- 같은 카드 재탭은 취소다.
- 선택 완료 상태에서 미선택 카드 탭은 무시한다.
- 선택 완료 상태에서도 이미 선택된 카드는 재탭 취소할 수 있다.
- 취소 후 선택 번호는 남은 선택 순서대로 재정렬된다.
- 선택 완료 전에는 결과 서랍이 보이지 않는다.
- 선택 완료 후에는 결과 서랍이 보인다.
- 결과 서랍 탭 또는 위로 스와이프로 결과 화면에 진입한다.
- 카드 선택 화면의 뒤로가기는 스프레드 선택으로 돌아간다.
- 결과 화면의 뒤로가기는 같은 선택 상태의 카드 피커로 돌아간다.

결과/기록:

- 결과 카드 position label은 `selectedIds` 순서와 spread `positionLabels` 순서를 따른다.
- 저장 결과는 `RecordSnapshots.tarot(...)` 경로를 통해 기록 envelope로 만들어진다.
- locked result 복원은 같은 deck/spread/seed/shuffledIds/result hash일 때만 성공한다.

## 증거 경로와 해시

GitHub/NB 커밋:

- Repo: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB`
- Commit: `b4ffe8718bf01b02930468451baa4ecab45f1db3`
- Commit message: `Move Saju calculation metadata into localized details`
- 비고: 커밋의 앱 소스 변경은 사주 메타데이터 쪽이지만, `evidence/saju-metadata-r2/`에 타로 플로우 회귀 증거와 단위 테스트 결과가 함께 포함되어 있다.

릴리스/스냅샷:

- APK: `evidence/saju-metadata-r2/sharomyang-saju-metadata-r2-debug.apk`
- Release copy: `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk`
- APK SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- Source snapshot SHA-256: `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683`
- Source manifest SHA-256: `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85`
- Signing certificate SHA-256: `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab`

타로 화면 증거:

| 상태 | 파일 | SHA-256 |
|---|---|---:|
| 0/3 선택 | `evidence/saju-metadata-r2/tarot-0of3.png` | `c869172c80b75d2a68fa12e0b54b2c93aa86e53a8e4f6d35153b3c5673c9a13d` |
| 2/3 선택 | `evidence/saju-metadata-r2/tarot-2of3.png` | `18f81db221a6b65e5a8b3df0eecd3c03e4a31123198e736048ce506953aee84e` |
| 3/3 선택 및 결과 서랍 | `evidence/saju-metadata-r2/tarot-3of3.png` | `96cc7d6ecd9bd9dcec4640e09b11ebf69ecfdd835d564769cf00182f20838080` |
| 세 번째 카드 재탭 후 2/3 | `evidence/saju-metadata-r2/tarot-retap-2of3.png` | `0fdaac0d11f19a7c59c78b32e8502f93503e6bdee10cc835ab01ce052d45c770` |

타로 테스트 증거:

| 테스트 XML | 의미 | SHA-256 |
|---|---|---:|
| `unit-results/TEST-com.hoscat.mtj.dev.TarotSelectionGridContractTest.xml` | 8x10, 첫 탭 선택, 재탭 취소, limit/unknown id | `27f1de22075dc0d962f550620dee41680bbb14db1fa2e52f4f1d19b3f97353db` |
| `unit-results/TEST-com.hoscat.mtj.dev.TarotSpreadCategoryContractTest.xml` | 8개 카테고리/27개 normal key 커버 | `759f42b12c20c5ec35f389ca34440ac73916efbe54350f1f99f804660a602de4` |
| `unit-results/TEST-com.hoscat.mtj.dev.TarotUiFlowContractTest.xml` | 질문/1장 flow/position identity | `SHA256SUMS`에서 필요 시 확인 |
| `unit-results/TEST-com.hoscat.mtj.dev.TarotRecreationStateTest.xml` | 세션 저장/복원/잠금/재오픈 | `SHA256SUMS`에서 필요 시 확인 |

TAR1 선행 산출물:

- 스프레드 설계 인풋: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-3-tar-tar-001-tar/outputs/tar1-spread-flow-design-input-20260911.md`
- SHA-256: `f69977d89570339af7b6bdbc310a9c936d333c6f75bf9ee30db709ab30227a98`
- 중간 구현 상태: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-3-tar-tar-001-tar/outputs/tar1-implementation-status-20260911.md`
- SHA-256: `b112439620763870a501594c199ff6f6374c148c14b8f53282aea4d648c02a30`
- 주의: 중간 구현 상태의 파일 SHA는 최종 NB 커밋과 다를 수 있다. 유지보수 기준은 반드시 `b4ffe87`의 source manifest와 NB checkout이다.

## 알려진 한계와 우선순위 후속 작업

P0:

- 새 교차 요구사항: 실시간 타로 결과와 저장된 타로 상세는 같은 요약 패널/포매터를 사용해야 하며, 패널의 content line은 정확히 두 줄이어야 한다. 형식은 `스프레드: {N}장 | {name} | {description}`와 `질문: {original input}`이다. 기준 커밋 `b4ffe87`은 실시간 결과에서 spread/title/subtitle과 별도 질문 라벨을 나누어 보여주고, 저장 상세에서도 `스프레드`와 `질문`을 별도 row로 보여주므로 이 요구와 다르다. 제안 패치: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-3-tar-tar-001-tar/outputs/tarot-unified-summary-panel-b4ffe871.patch`.
- 홈의 `과거 · 현재 · 미래 / 3장` 진입이 현재는 카테고리 선택 화면으로 이동한다. 요구가 “3장 과거·현재·미래 스프레드 자동 하이라이트 또는 바로 해당 리딩 목적 선택”으로 확정되면 `MainActivity`, `MtjHomeScreen`, `TarotScreen` 사이에 명시적 intent/initial category/spread 전달 계약을 추가해야 한다.
- `TarotSelectionFocusReducer.kt` 파일명은 현재 내용과 맞지 않는다. 실제 내용은 `TarotSelectionState`와 `toggleTarotSelection`이다. 혼란을 줄이려면 Android owner가 파일명을 `TarotSelectionReducer.kt` 등으로 바꾸고 참조를 정리하는 것이 좋다.

P1:

- 78장 8x10 grid는 Pixel_10 1080x2424/fontScale 1.0에서 검증됐다. 360dp, 411dp, 600dp, fontScale 1.5/2.0, dark/light 전체 매트릭스는 추가 캡처가 필요하다.
- 결과 서랍이 3/3 상태에서 마지막 행 카드 또는 시스템 내비게이션을 가리지 않는지 작은 화면에서 다시 확인해야 한다.
- 결과 화면 뒤로가기 후 선택 피커로 돌아왔을 때, 같은 카드 재탭 취소와 결과 서랍 닫힘이 모든 스프레드 장수에서 동일하게 동작하는지 장수별 회귀 테스트를 추가하면 좋다.
- 홈 질문과 타로 질문이 같은 상태를 공유하므로, 타로에서 질문을 수정한 뒤 홈 기록/사주 입력 화면을 오갈 때 의도치 않은 초기화가 없는지 긴 탐색 시나리오를 추가해야 한다.

P2:

- `TarotSelectionBoard(...)` 같은 더 이상 화면에서 직접 쓰지 않는 보조 composable이 남아 있을 수 있다. 실제 미사용 여부를 IDE/컴파일 경고 기준으로 정리할 수 있다.
- 선택 완료 후 결과 서랍 문구 `결과 서랍`은 기능적으로 명확하지만 최종 브랜드 톤에 맞춰 카피 검토 가능하다.
- 스프레드 카테고리의 대표 미니 프리뷰는 각 카테고리 첫 option을 사용한다. 카테고리 대표성이 더 좋은 spread를 명시적으로 지정하려면 별도 mapping을 추가한다.

## 의존성과 인계 메모

- 스프레드 원본 정의는 `SpreadDefinitions.kt`가 소유한다. UI에서 key/count/label/coordinate를 임의로 재해석하지 않는다.
- 타로 결과 저장은 records 계약과 연결되어 있으므로, 결과 schema 변경은 records owner와 함께 해야 한다.
- 홈 질문 continuity는 `MainActivity` 상태가 중심이다. 홈 또는 타로 중 한쪽에서 독립 상태를 만들면 H-04가 깨진다.
- 최종 증거는 `evidence/saju-metadata-r2/`가 APK SHA와 PNG/XML/SHA를 묶고 있다. 새 APK를 만들면 evidence-index와 source manifest를 함께 새로 생성해야 한다.
- `먕디자인TF` 산출물은 이 구현/승인 근거로 사용하지 않는다. 디자인 변경은 고급 프리미엄 디자이너와 MTJ 디자인 전담 경로의 최신 결정만 따른다.

## 새 유지보수자 첫날 체크리스트

1. `/Volumes/뽀그리/Project 먕/샤로먕/github/NB`에서 `git rev-parse HEAD`가 `b4ffe8718bf01b02930468451baa4ecab45f1db3`인지 확인한다.
2. `README.md`의 APK SHA와 `evidence/saju-metadata-r2/release-manifest.json`의 APK SHA가 같은지 확인한다.
3. `evidence/saju-metadata-r2/source-manifest.sha256`에서 `TarotScreen.kt`, `TarotSelectionFocusReducer.kt`, `TarotSelectionGridContract.kt`, `TarotSpreadCategoryContract.kt`, `SpreadDefinitions.kt` SHA를 확인한다.
4. `TarotSelectionGridContractTest`를 읽고 8x10 grid, 첫 탭 선택, 재탭 취소, limit/unknown id 무시 계약을 이해한다.
5. `TarotSpreadCategoryContractTest`를 읽고 8개 카테고리와 27개 normal spread coverage를 확인한다.
6. `MainActivity.kt`의 `tarotQuestion` 공유 상태를 확인하고, 홈/타로 양쪽이 같은 state/callback을 쓰는지 본다.
7. `MtjHomeScreen.kt`의 `home-three-card-entry`가 `onStartTarot`만 호출한다는 점을 확인한다.
8. `TarotScreen.kt`에서 `selectedCategoryId == null`, category selected, `bridge != null`, `result != null` 네 주요 화면 상태를 따라간다.
9. `TarotRecreationState.kt`의 `start`, `finish`, `reopenSelection`, `reshufflePreservingSelection`, `restore`를 읽고 saved-state fail-closed 설계를 확인한다.
10. `evidence/saju-metadata-r2/tarot-0of3.png`, `tarot-2of3.png`, `tarot-3of3.png`, `tarot-retap-2of3.png`를 열어 실제 화면 상태를 눈으로 확인한다.
11. 새 수정 전에는 `testDebugUnitTest`의 타로 관련 테스트를 먼저 실행한다.
12. 새 APK를 만들면 source snapshot, source manifest, APK SHA, PNG/XML evidence index를 모두 새 이름으로 생성한다.
13. 홈 3장 진입이 “카테고리 화면”인지 “과거·현재·미래 스프레드 직접 하이라이트”인지 최신 제품 결정을 확인한 뒤 수정한다.
14. 작은 화면/fontScale/dark 모드 추가 QA 없이 78장 피커를 PASS로 확장 보고하지 않는다.
