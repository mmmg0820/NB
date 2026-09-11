# 샤로먕 QA 인수인계

기준일: 2026-09-11. 작성자: 먕콰 QA. 이 문서는 **S1 후속 수정 빌드의 제한된 최종 검수**와 향후 검수 방법을 설명한다. 문서 작성 중 앱 소스·공유 Git checkout·기기를 변경하거나 push하지 않았다.

## 1. 가장 먼저 알아야 할 판정

**현재 판정: 지정된 S1 수정 및 표적 회귀 범위 FINAL_PASS. 전체 제품 또는 스토어 Production 출시 인증이 아니다.**

| 항목 | 고정 기준 |
|---|---|
| 저장소 | https://github.com/mmmg0820/NB |
| 검토 커밋 | `b4ffe8718bf01b02930468451baa4ecab45f1db3` |
| 태그 | `v0.1-dev-20260911-r2`; annotated tag를 commit으로 해석해 비교한다 |
| APK 저장소 경로 | `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk` |
| APK SHA256 | `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2` |
| 패키지 / Activity | `com.hoscat.mtj.dev` / `.MainActivity` |
| 버전 / SDK | versionCode1, versionName0.1-dev; min31, target36, compile36 |
| 서명 | debug 인증서 SHA256 `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab` |
| 소스 snapshot SHA256 | `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683` |
| 소스 manifest SHA256 | `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85` |

독립 확인한 사항: APK와 OPS가 추출한 installed-base의 SHA 일치, snapshot168개 파일/manifest168개 일치, 인증서 검증, 제출 테스트 XML94건 중 실패·오류·skip0, GitHub에 올라간 후속 증거60개 파일과 검토한 로컬 파일의 바이트 일치. QA가 후속 빌드를 직접 재빌드하거나 테스트94건을 재실행한 것은 아니다.

이전 `89d40ac0...` 빌드는 사주 결과에 raw ISO 시각과 `검증 상태 별도 표시` 문구가 노출되어 FINAL_FAIL이었다. 해당 기록은 이력으로 보존하며 PASS로 고쳐 쓰지 않는다. 새 SHA의 수정 검수로만 차단 사유를 닫았다. 기존 `releases/2026-09-11/` APK를 최신 후보로 배포하지 않는다.

## 2. 구현과 검증의 구분

| 영역 | 현재 구현 또는 계약 | 이번에 실제로 닫힌 범위 |
|---|---|---|
| 앱 구조 | 홈/사주/타로/기록/설정5탭. 타로 선택 중에는 별도 고정 grid/drawer 화면 | 제출 결과·선택 화면 관찰. 모든 탭/상태 전수 PASS 아님 |
| 사주 입력 | 날짜1필드, 시간1필드, 시간 모름 상태와 입력 계약 | 제출 known/unknown 결과4개 상태와 입력 캡처 확인; 경계 날짜 전수 아님 |
| 사주 결과 | 명식·일간·연주/월주/일주/시주, 정확한 검토 상태; 시간 모름이면 시주를 임의 생성하지 않음 | known/unknown × light/dark 실제 결과, 순서·문구·잘림 표본 검수 |
| 계산 기준 | 첫 카드에는 간결한 기준명. 계산 기준 버튼으로 절입 이름과 한국 표준시로 포맷한 상세 시각 표시 | 밝음/어두움 dialog, raw ISO/내부 지시 문구 제거 확인 |
| 타로 |8가지 형태 분류와27개 리딩 계약;78장8열/10행, 마지막6장 중앙 배치 |3장 선택0/3→2/3→3/3→동일 카드 재탭2/3, drawer 닫힘 |
| 기록 | 전용 SQLite 저장소와 snapshot envelope; 다른 앱 저장소 자동 접근 없음 | 소스/테스트 계약 확인. 최신 SHA에서20개 누적·재실행·삭제 전수 미실시 |
| 테마·접근성 | light/dark 및 글꼴 확대 시 판독성 계약 | fontScale1.0 표본. TalkBack/감소된 모션/전체 확대 매트릭스 미완료 |

옛 먕타주 문서의4탭·7개 스프레드·5열 선택 grid·다른 package 기준은 이 커밋의 검수 기준이 아니다. 화면 계약과 기준 SHA를 먼저 확인하고, 과거 조건을 임의로 다시 적용하지 않는다. 제품 이름이 비슷해도 `com.hoscat.saju`, `com.softcat.mystictarot`, `com.myangtaju.app` 증거는 이 빌드의 PASS 증거로 사용하지 않는다.

## 3. 파일과 책임 경계

아래 경로는 고정 커밋 기준 저장소 상대경로이다. 로컬 구현 기준 디렉터리는 `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/work/cell`이며, 그 경로가 Git checkout이라는 뜻은 아니다. 최신 변경 여부는 커밋/snapshot으로 판단한다.

| 담당 | 정확한 주요 파일 | 인수인계 책임 |
|---|---|---|
| Android 통합 | `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt`, `MtjHomeScreen.kt`, `SettingsScreen.kt` | 탐색·화면 연결·빌드·새 SHA 제출 |
| Android 사주 표시 | 같은 디렉터리 `SajuChartDisplay.kt`; 테스트 `android/app/src/test/java/com/hoscat/mtj/dev/SajuChartDisplayTest.kt` | 이번 수정의 유일한 소스2개. 첫 카드 문구와 상세 dialog |
| 사주 모듈 담당 | `saju/src/MtjBirthInputAdapter.kt`, `saju/src/MtjSajuRuntime.kt`, `android/vendor/com/hoscat/core/manse/`, `android/vendor/com/hoscat/core/model/` | 입력/계산/검증 상태 계약. QA가 계산 정책을 임의 변경하지 않음 |
| 타로 모듈·Android | `tarot/src/MtjTarotBridge.kt`, `MtjTarotCatalog.kt`, `MtjTarotEntry.kt`; 앱의 `TarotScreen.kt`, `TarotSelectionFocusReducer.kt`, `TarotSelectionGridContract.kt`; `android/vendor/com/softcat/mystictarot/SpreadGeometry.kt`, `SpreadDefinitions.kt` | ID·배열·방향·선택 복원 및 렌더링 |
| 기록 모듈·Android | `records/src/MigrationPlanner.kt`, `RecordsJsonCodec.kt`; 앱의 `RecordStore.kt`, `RecordSnapshots.kt`, `RecordDetailRows.kt`, `RecordsScreen.kt` | 원자적 저장, snapshot, 삭제·참조 정합성 |
| 승인된 디자인 담당 | `design/native/MtjTheme.kt`, `MtjComponents.kt`, `MtjBottomActionScaffold.kt` 및 별도 승인 시안 | 상태·위계·색상 승인. 신규 디자인 변경은 조정 담당의 명시적 승인 필요 |
| OPS | `/Volumes/뽀그리/Project 먕/_operations/device-reservations/registry.json` | lease 발급·이관·반납, 기기 충돌 방지 |
| 먕콰 QA | 아래 QA 전용 outputs의 `audit.py`, `audit-results.json`, `REPORT.md` | 증거 독립 확인·판정. 제품 소스·레지스트리·타 팀 증거 수정 금지 |

연락 경로: 조정 담당 `01a0853c-43d3-7be3-8673-bcfc2ef46f82`, Android `01a084e2-4df6-7921-be01-4096a324d7ea`, OPS `01a03029-84c9-7523-90cd-c6e143ab8ab5`, MTJ 디자인 `01a084e4-49c2-7b31-aece-a85374f769ec`, 고급 디자인 `019ea81d-f09d-7ef1-a16c-9e26b1ffd764`. 역할 담당자는 바뀔 수 있으므로 업무 시작 때 확인한다. 내부 협의는 영어, 유지보수 문서와 대표 보고는 한국어 기준이다.

## 4. 데이터·상태 계약

### 사주

- 시간 모름 입력은 null 의미를 유지한다. 비활성화된 시간 필드의 이전 입력은 토글 복귀를 위해 보존할 수 있지만 계산 제출값과 혼동하지 않는다.
- unknown 결과는 시주 위치를 유지하고 `시간 모름`, `오행 산출 제외`를 표시한다. 가짜 천간/지지를 채우면 S0이다.
- `sajuChartAuthorityLabel`은 실제 evidence의 검증 여부/trustLevel로 상태를 구분한다. 모든 결과에 `검토 필요`를 강제로 넣거나, 미검증을 검증됨으로 승격하면 안 된다. 이번 검수 입력은 정확히 `검토 필요`가 맞는 상태다.
- `sajuChartBasisLabel`은 간결한 기준명, `sajuCalculationDetails`는 원본 evidence를 변경하지 않고 날짜를 사용자 형식으로 표시한다. 날짜 파싱 실패 시 원문 유출 대신 표시 불가 문구를 쓴다.
- 디자인 시안의 예시 팔자는 레이아웃 fixture다. 실제 계산 기대값으로 사용하지 않는다.

### 타로

- `TarotSelectionState`는 `deckOrder`, 순서 있는 `selectedIds`, `requiredCount`로 구성된다. reducer는 중복/존재하지 않는 선택을 정리하고 허용 개수로 제한한다.
- 첫 탭 선택, 선택 카드 재탭 취소, 정원 초과의 미선택 카드 탭은 상태 유지. 결과 자동 진입 금지; 완료 drawer의 명시적 조작으로 진입한다.
- 선택 수가 부족해지면 drawer가 사라지고 남은 선택 순번이 연속이어야 한다. 셔플은 선택 카드 ID/순서 보존, 미선택 카드만 변경하는 계약으로 회귀한다.
- XML의 `카드 3`은 UI 위치 의미다. canonical card_id와 같다고 단정하지 않는다. UI 위치·실제 ID·순번·방향은 필요한 경우 상태 snapshot으로 함께 검증한다.
- r2 재탭은 동일 위치3, 좌표356,382. 선택 semantic bounds `[301,289][417,475]`, 미선택 target bounds `[296,289][417,475]`이며 둘 안에 좌표가 들어간다. bounds가 완전히 같았다고 보고하지 않는다.

### 기록

- `RecordSnapshots.saju`는 PROFILE과 SAJU envelope를 함께 만들고 SAJU에서 profileRefs를 보존한다. TAROT에는 reading의 질문·카드·방향·의미 snapshot이 들어간다.
- `RecordStore`는 `mtj-records-v1.db`의 `records(identity PRIMARY KEY, envelope BLOB)`를 사용한다. Mutex와 DB transaction으로 접근/입력을 보호하며 입력을 codec 왕복으로 고정한다.
- MigrationPlanner의 admission 결과가 준비되지 않으면 입력을 거부한다. 참조 중인 profile 삭제도 거부한다. 기존 DB upgrade는 파괴적 초기화 대신 명시적 migration 필요 오류를 낸다.
- 저장 목록은 저장소에서 identity 순으로 읽는다. 사용자 화면 정렬 기준은 별도로 확인한다. 최신순·중복 없음·날짜 동률 처리는 실제 UI와 snapshot으로 검수해야 한다.

## 5. 기기 lease 규칙

1. **ADB 조회를 포함한 모든 기기 명령 전** OPS에 목적·package·APK SHA·serial·필요 시간·증거 경로를 제출하고 유효한 독점 lease를 받는다.
2. registry의 ACTIVE 문자열만 믿지 않는다. 만료 시각과 현재 owner를 함께 본다. 만료됐는데 ACTIVE가 남아 있으면 OPS가 정리할 때까지 사용하지 않는다.
3. 다른 작업자가 사용 중이면 명시적 handoff를 받는다. 한 serial에는 동시 작성자 한 명만 허용한다. AVD 이름과 serial은 영구 배정이 아니다.
4. `adb -P 5037 -s <승인 serial>`로 대상을 고정한다. focus가 예상 package가 아니면 앱 증거 채택과 추가 탭을 중지한다. 권한창 등 OS 화면은 따로 표기한다.
5. 무단 install, 다른 앱 삭제·force-stop, 데이터 초기화, `adb kill-server`, 공유 emulator 종료, 소유 불명 프로세스 kill 금지. 데이터 삭제가 필요한 시험은 별도 승인/테스트 데이터로 수행한다.
6. 완료 시 본인 명령만 종료하고 최종 화면·선택 상태·변경 사항·로그 범위를 보고한다. OPS가 lease를 반납 처리한다. QA는 registry를 직접 덮어쓰지 않는다.

과거 재현 예: `89d40ac0` 검수는 Pixel_9a/emulator-5554 독점 handoff를 받아 직접 수행 후 반납했다. r2는 OPS Pixel9a와 Android Pixel10 제출 증거를 **오프라인으로 독립 검토**했다. 후속 검사자가 이 차이를 지우면 안 된다.

## 6. exact-SHA와 증거 규격

검증 연결: 승인 디자인 manifest → source snapshot/manifest → build/test → 제출 APK → 설치 후 추출 base APK → 해당 실행의 screenshot/XML/logcat. 파일명이나 개발자의 PASS 요약만으로 연결을 대신하지 않는다.

권장 증거 경로: `<SHA 앞8자>/<lease-id>/<serial>/<화면>/<상태>/<theme>-font<scale>-<시각>.*`.

각 증거에는 lease ID, serial/AVD 또는 실기기 모델, API, package, mCurrentFocus, size, density, fontScale, theme, 촬영 시각과 테스트 시작/종료 시각, 제출/설치 APK SHA, PNG/XML SHA를 기록한다. 가능하면 캡처 전후 focus를 모두 남긴다. 공통 실행 metadata만 있으면 그 한계를 명시한다.

PNG는 직접 열어 실제 화면을 보고 XML은 label/state/bounds를 파싱한다. 예전에 `unknown-result-dark` 이름의 파일이 입력 화면이었던 문제가 있었으므로 이름만 보고 결과 PASS로 처리하지 않는다. 검사 스크립트의 초반 실패·재시도·중간 화면은 삭제하지 말고 유효 증거와 분리한다.

APK 파일 SHA와 서명 SHA는 다르다. 같은 키로 서명해도 APK 바이트가 달라지면 새 후보이며, 기존 런타임 PASS를 자동 승계하지 않는다. AAB와 APK 역시 동일 해시가 될 수 없으므로 각 배포/설치 산출물의 연결을 따로 기록한다.

## 7. 재현·빌드·검증 명령

다음은 유지보수자가 실행할 예시다. 이 문서 작성 중 실행한 build/ADB가 아니다. 경로 변수는 실제 승인된 경로로 바꾸고, 공유 checkout 대신 별도 검토 사본을 사용한다. 네트워크/빌드/기기 작업은 해당 범위 승인을 먼저 받는다.

```sh
# REPO는 별도 검토용 Git checkout의 절대경로
git -C "$REPO" rev-parse 'v0.1-dev-20260911-r2^{commit}'
git -C "$REPO" status --short
shasum -a 256 "$REPO/releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk"
```

이 저장소에는 Gradle wrapper가 없다. 확인된 빌드 로그는 외부 Gradle8.11.1, AGP8.10.1, Kotlin2.1.21을 사용했다. Java17 호환, Android SDK36과 `ANDROID_USER_HOME/debug.keystore`가 필요하다. 키스토어를 저장소에 넣지 않는다. 디버그키가 다르면 설치 업그레이드/서명이 달라지므로 기존 앱 데이터를 지워 해결하지 말고 owner에게 조정 요청한다.

```sh
# GRADLE_BIN은 승인된 Gradle8.11.1 실행파일 절대경로
# JAVA_HOME, ANDROID_HOME, ANDROID_USER_HOME은 팀 빌드환경에 맞게 먼저 지정
"$GRADLE_BIN" -p "$REPO/android" --no-daemon --offline :app:clean :app:testDebugUnitTest :app:assembleDebug
# dependency cache가 없으면 offline 실패: 임의 버전 변경 대신 Android owner에게 환경 요청
"$ANDROID_HOME/build-tools/36.1.0/apksigner" verify --print-certs "$APK"
```

기기 명령은 유효 lease 이후에만 실행한다. `BASE_PATH`는 pm path 출력에서 확인한 실제 base.apk 경로를 넣고 임의 추정하지 않는다. split 설치면 전체 split 목록도 기록한다.

```sh
ADB="$ANDROID_HOME/platform-tools/adb"
"$ADB" -P 5037 -s "$SERIAL" shell dumpsys window
"$ADB" -P 5037 -s "$SERIAL" shell wm size
"$ADB" -P 5037 -s "$SERIAL" shell wm density
"$ADB" -P 5037 -s "$SERIAL" shell settings get system font_scale
"$ADB" -P 5037 -s "$SERIAL" shell dumpsys uimode
"$ADB" -P 5037 -s "$SERIAL" shell pm path com.hoscat.mtj.dev
"$ADB" -P 5037 -s "$SERIAL" pull "$BASE_PATH" "$EVIDENCE/installed-base.apk"
shasum -a 256 "$APK" "$EVIDENCE/installed-base.apk"
"$ADB" -P 5037 -s "$SERIAL" logcat -d -b all -v threadtime -T "$DEVICE_START_TIME" > "$EVIDENCE/logcat-full.txt"
rg -n 'FATAL EXCEPTION|ANR in |OutOfMemoryError|am_crash|am_anr|Fatal signal' "$EVIDENCE/logcat-full.txt"
```

`rg` 종료코드1은 일치0건이지 수집 실패가 아니다. 원본 logcat 파일의 존재·내용·시간 범위도 검사한다. 로그를 지우는 `logcat -c`는 사용하지 않는다.

## 8. 테스트 매트릭스와 회귀 체크리스트

### 필수 시각 매트릭스

사주 결과: 논리 폭320/360/411/600dp × fontScale1.0/1.5/2.0 × light/dark × known/unknown =48조합. 각 화면 적용 범위를 기록하며 이48개가 이미 끝났다고 적지 않는다. 기본은4컬럼,320dp/font2는 순서 있는2×2; 그 외는 측정된 공간 부족에 따라 판독성 유지 여부를 승인 기준과 비교한다. 구현에는 fontScale1.3 이상에서2컬럼으로 전환하는 규칙이 있으므로 전체 반응형 일치 여부는 아직 별도 검증 대상이다.

Android API31부터 target36 및 제출 시험 API37까지의 지원 범위, 실제 Galaxy·작은 화면·제스처/3버튼 내비게이션·IME 노출·회전·백그라운드 복귀는 위험별로 조합을 선정한다. 에뮬레이터만으로 실기기 성능을 인증하지 않는다.

| 케이스 | 실행 | 합격 기준 | 현재 상태 |
|---|---|---|---|
| S-META | known/unknown × light/dark 결과 열기 | 간결한 기준명, 상태 정확, raw ISO/내부 지시 문구 없음 | r2 표본 PASS |
| S-DETAIL | 계산 기준 열기/닫기 | 한국어 날짜·시간대, 원본 불변, 잘림0 | r2 표본 PASS |
| S-UNKNOWN | 시간 모름 결과 | 시주 placeholder, 가짜 글자/오행 없음 | r2 PASS; 입력/저장 전체 전이 별도 |
| S-INPUT | 유효/무효 날짜, 윤달, 시간 경계, 토글 복귀 | 검증 오류가 명확하고 잘못된 값 계산/저장 없음 | 전체 경계 미실시 |
| S-RESP | 위48조합과 최장 별칭 | 한글/한자 말줄임·clipping·overlap0, 핵심 원국 첫 viewport | 전체 미실시 |
| T-RETAP |3장 선택 후 동일 카드 재탭 |3/3→2/3, 남은 순번 정합, drawer 없음 | r2 위치3 PASS |
| T-GRID |0/중간/완료78장 | 모든 이미지 한 viewport, 마지막6장 및 터치영역 가림0 | r2 3장 상태 표본; 전체 기기 별도 |
| T-FULL | 정원 찼을 때 미선택 카드 탭 | 선택/순서 변화와 자동 진입 없음 | 최신 SHA 추가 실행 필요 |
| T-SHUFFLE | 일부 선택 후 섞기 | 선택 ID/순서 보존, 미선택 카드만 재배열 | 최신 SHA 추가 실행 필요 |
| T-RESTORE | drawer tap/swipe, Back, 회전·재진입 | spread/question/선택 ID/방향/순서 복원 | 전체 경로 미실시 |
| T-MAP |27개 key와8개 분류 전수 | 승인 mapping의 수·좌표·회전·라벨 순서 일치 | 최신 SHA 전수 미실시 |
| H-QUESTION | 한국어 질문 입력→타로→편집→홈→재진입 | 입력 바이트/커서 기대 동작 보존, 재입력 불필요 | 전체 roundtrip 미완료 |
| R-20 | 사주/타로20개 이상 저장→재실행→상세→삭제 | 유실/중복/잘못된 정렬/참조 깨짐0 | 최신 SHA 미실시 |
| A11Y | TalkBack, 큰 글씨, reduced motion | 이름·선택 상태·포커스 정확, 필수 조작 가능 | 별도 검증 필요 |

### bounds 판정

- 두 rect 교차 면적은 `max(0,min(right)-max(left)) × max(0,min(bottom)-max(top))`. 서로 다른 텍스트/카드 의미 라벨의 양수 교차는 조사 대상이다. 부모/자식 semantic rect의 정상 중첩을 결함으로 세지 않는다.
- 카드와 의미 라벨 overlap0, 이미지 aspect ratio 유지, 회전 카드의 부모 clip0. 켈틱 중앙 세로/가로 카드의 **의도된 카드끼리 중첩**은 예외이며90도 회전을 보존한다.
- 이미지 bounds, semantic bounds, 실제 터치 target bounds를 구분한다. XML만으로 crop과 글자 clipping이 없다고 단정하지 않고 PNG를 같이 연다.
- 하단 gap은 승인된 화면의 visible action panel bottom부터 bottom tab container top을 측정한다. 내부 버튼이나 tab label bounds를 섞지 않는다. px→dp는 `px/(densityDpi/160)`이다.
-8dp target,8dp 미만/10dp 초과 실패라는 기존 action-stack 계약은 해당 컴포넌트 화면에 적용한다. 현재 타로 picker의 no-nav drawer를 과거 하단탭 레이아웃으로 억지 환산하지 않는다. 화면별 기준선과 예외를 명시한다.

## 9. 로그 해석과 판정 절차

PID 전용 로그는 해당 프로세스 오류 확인에는 유용하지만 system_server가 기록한 ANR/am_crash를 놓칠 수 있다. raw all-buffer log를 함께 보존한다. package·PID 수명·시간대·실행 시나리오를 기준으로 대상 오류와 환경 오류를 구분한다.

r2 all-buffer log에는09:15~09:16 Google Messaging/captions/Play의 기존 ANR3건에 대한 strict 일치6줄이 있다. 앱 시험보다 앞선 다른 package 오류이므로 샤로먕 결함으로 합치지 않는다. 반대로 전체 로그0건이라고 숨기지도 않는다. 올바른 보고는 **MTJ strict0, 별도 환경 ANR3건 보존**이다.

| 등급/상태 | 판정 방식 |
|---|---|
| S0 | 대상 앱 실행 불가, 크래시/ANR/OOM, 기록 유실·잘못된 사용자 데이터, 가짜 시주 등. 해당 후보 즉시 Hold |
| S1 | 핵심 조작 불가, 의미 훼손 clipping/overlap, 승인된 핵심 내용·상태 불일치. 재현/증거 고정 후 Hold |
| S2/S3 | 핵심 완료를 막지 않는 가독성·편의·다듬기. 수용 여부와 owner/일정 기록 |
| 증거 미완 | 버그 확정과 구분한다. 필요한 화면/SHA/log가 없으면 PASS 금지. 다음 제출물을 정확히 지정 |
| FINAL_PASS | 합의한 검수 범위의 필수 항목을 같은 후보 SHA에서 닫은 것. 범위를 제목·표·결론에 모두 적음 |

수정 절차: 발견 화면/입력/재현 순서/빈도/기기/SHA/PNG/XML/log를 고정 → QA 판단 → 디자인/모듈 owner와 조정 담당 승인 → Android 수정·새 freeze → 영향 범위 회귀 → 새 SHA 판정. 현장에서 몰래 고친 뒤 이전 증거를 그대로 쓰지 않는다. 새 S0/S1은 이미 올라간 dev 후보도 다시 Hold할 수 있다.

## 10. 미완료와 우선 후속 작업

| 우선순위 | 후속 작업 / owner | 선행조건과 종료 기준 |
|---|---|---|
| P0: Production 승격 전 | Android+QA: 지원 기기/글꼴48조합, 핵심 입력·기록·복원 회귀 | 승인 범위/lease/새 후보 SHA 고정 후 위 매트릭스 필수 상태를 증거로 닫기 |
| P0: 공개 자산 | 디자인+배포 owner: 카드 이미지 및 파생 이미지 배포 허가 | exact hash 미일치는 권리 증명이 아님. 파일→원본→사용 허가 연결 확보 |
| P0: 배포 owner | release 서명/AAB/스토어 선언 검증 | 현재는 debug APK다. 실제 release 산출물에서 package·서명·설치/업데이트 smoke 재확인 |
| P1 | 사주 owner+QA: 계산 경계·윤달·시간 모름 저장 비교 | 승인된 계산 정책/검증용 기대값 확보; 디자인 fixture 사용 금지 |
| P1 | 기록 owner+QA:20개 이상 누적·migration·중단 저장·삭제 참조 | 전용 테스트 데이터/백업·데이터 변경 승인; 유실0·원자성·복원 확인 |
| P1 | 타로 owner+QA:27개 geometry/셔플/뒤로/상태 복원 전수 | 승인 mapping hash와 stable ID 고정; 의미/방향 유지 증거 |
| P1 | Android+QA: 접근성·IME·시스템 테마·모션·장시간 안정성 | 상태별 PNG/XML/TalkBack 기록·성능 타임라인. 오류0만으로 성능 PASS 금지 |
| P2 | QA 자동화 정리 | 아래 현장 스크립트는 기기/경로가 고정되어 있으므로 재사용 전 인자화·lease 검사·읽기전용/기기 실행 모드 분리 |

이 목록은 미검증 작업이다. 실제 실패가 발견되지 않은 항목을 이미 발생한 결함으로 보고하지 않는다. 신규 기능 우선순위와 별도 회사 자원 배정은 대표/조정 담당이 정하며 QA가 혼자 제품 범위를 바꾸지 않는다.

## 11. 증거·스크립트 색인

| 자료 | 절대경로 / 의미 |
|---|---|
| 최종 QA 보고 | `/Users/thomaslee/Documents/Codex/2026-06-11/3-qa-ai-qa-qa-qa/outputs/successor-78277c5f-qa-20260911/REPORT.md` |
| 독립 기계 검증 | 같은 폴더 `audit.py`, `audit-results.json`, `github-verification.json`, `SHA256SUMS` |
| GitHub 최종 증거 | https://github.com/mmmg0820/NB/tree/b4ffe8718bf01b02930468451baa4ecab45f1db3/evidence/saju-metadata-r2 |
| Android 원본 증거 | `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/outputs/evidence/sharomyang-saju-metadata-r2-20260911/` |
| OPS 증거 | `/Volumes/뽀그리/Project 먕/샤로먕/evidence/nb-saju-metadata-r2-78277c5f-20260911/` |
| 이전 FAIL/직접 기기 증거 | `/Users/thomaslee/Documents/Codex/2026-06-11/3-qa-ai-qa-qa-qa/outputs/final-89d40ac0-device-qa-20260911/` |
| 전체 QA 게이트 | `/Users/thomaslee/Documents/Codex/2026-06-11/3-qa-ai-qa-qa-qa/outputs/mtj-design-approval-device-qa-checklist-20260911.md` |
| 보조 화면 승인 기준 | `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/secondary-v2-20260911/SPEC.md`, `manifest.json` |
| 핵심 타로 승인 기준 | `/Users/thomaslee/Documents/Codex/2026-06-09/pentagram-wolff-olins-collins-landor-mucho/outputs/ux-completion-20260911-0831/` |
| 디자인 통합 판정 | `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/combined-design-packet-20260911.md` |

핵심 manifest SHA `e1f6e64e7b56ed3243076fff255167cd33e8e10e11fe68cac9d99c93e633a878`, mapping SHA `1cbf5a938eb542b7af78d850ed7506557e6f0a83cdea9726ecc369eed0c43034`. secondary-v2 manifest SHA `5ce50e79104294144b74adac096cca084bb289643b0cde689e195fc00c7b29a1`. 설정 비교 board는 v3 승인 override이며 `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/settings-board-v3-20260911/board-settings.png`, SHA `18b63ed905e753de8c70eeb4081470c5ccf3573d6b86a3cd42c5065f4831eaf9`이다. 시안 승인과 앱 런타임 승인은 별개다.

`audit.py`는 해당 시점 절대경로를 읽어 QA 전용 JSON을 생성하는 현장 스크립트다. 읽기 검사 외에 보고 파일을 덮어쓸 수 있으므로 **보존 완료 폴더에서 무심코 재실행하지 않는다**. 새 QA 폴더 사본에서 ROOT/입력을 조정한다. GitHub 검증은 fetch한 고정 tag의 blob을 해시했으며 snapshot/기존 QA 증거를 수정하지 않았다.

`qa_capture.py`는 이전89d 빌드·serial·SHA에 고정되어 실제 탭을 수행한다. `qa_pixel10.py`도 기기 탭·입력·theme·force-stop을 수행한다. 둘 다 단순 오프라인 검사 도구가 아니며 새 lease와 코드 검토 없이 실행 금지다. 기계 PASS 요약만 믿지 말고 위 PNG/XML/해시 검사를 다시 수행한다.

`nb-release-df7d6d90-20260911`, 실패 상태의 `native-first-flow/latest-build.json`, package/SHA가 다른 구형 증거는 최종 PASS 입력으로 금지한다. 배포 허가 없는 secondary 이미지/board를 앱이나 공개 repo에 복사하지 않는다. 이번 검수의 exact-hash 배제는 파생물 권리를 인증하지 않는다.

## 12. 새 유지보수자 첫날 체크리스트

- [ ] 이 문서의 제한된 FINAL_PASS와 Production 미인증을 읽고 업무 범위를 조정 담당에게 확인한다.
- [ ] 별도 사본에서 고정 tag→commit과 APK SHA를 확인한다. 기존 버전 APK와 혼동하지 않는다.
- [ ] 최종 QA 보고, evidence-index, source manifest,94개 test XML 합계를 직접 확인한다.
- [ ] 수정 전/후 사주 결과 PNG와 계산 기준 dialog를 열어 S1이 무엇이었는지 파악한다.
- [ ] Tarot3/3 및 재탭2/3 XML을 비교해 UI 위치와 canonical ID 차이를 이해한다.
- [ ] 담당 소스/모듈 owner, Android 빌드 도구 경로, debug key 관리자를 확인한다.
- [ ] 전체48조합/기록20개/27개 배열/접근성 중 미완료 작업에서 승인된 다음 항목을 선택한다.
- [ ] 기기 작업이 필요할 때만 OPS lease를 발급받고 현재 owner·만료·SHA를 확인한다.
- [ ] 새 증거 폴더를 만들고 실행 전 focus/config/설치 SHA와 로그 시작을 기록한다.
- [ ] 완료·실패·미실행을 분리해서 보고하고 lease를 반납한다. 기준표만으로 PASS를 만들지 않는다.

## 13. 보고 형식

| case ID | 후보/설치 SHA | 기기·lease | 결과(PASS/FAIL/미실시) | 증거 절대경로 | S등급·영향 | 다음 액션·owner |
|---|---|---|---|---|---|---|
| 예: S-META-dark-unknown | 같은 SHA만 사용 | serial/AVD/font/theme | 관찰 근거로 작성 | PNG/XML/log/index | 새 결함과 증거 부족 구분 | 재현 또는 누락 증거를 구체화 |

문서 인수자는 현재 파일을 기준으로 새 검수 문서를 만들 수 있지만, 기존 감사 결과와 원본 증거를 소급 변경하지 않는다. 새 판정에는 날짜·SHA·범위·승인자를 항상 남긴다.
