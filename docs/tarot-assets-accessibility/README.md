# 타로 자산·접근성 인수인계

작성일: 2026-09-11 KST. 기준 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3`.

## 1. 목적과 검증 범위

이 문서는 샤로먕 Android의 덱 식별자, 이미지 연결, 78장 선택, 접근성 및 회귀 검증을 이어받는 개발자·디자이너·QA 담당자를 위한 문서다. 지정 커밋의 Git 객체와 기존 증거를 읽어 작성했다. 이번 문서 작업에서 앱 소스 수정, 재빌드, 기기 조작, Git push는 하지 않았다.

- 저장소: [mmmg0820/NB 기준 커밋](https://github.com/mmmg0820/NB/tree/b4ffe8718bf01b02930468451baa4ecab45f1db3)
- 조사한 로컬 체크아웃: `/Volumes/뽀그리/Project 먕/샤로먕/repositories/NB`
- 아래 소스·증거 상대 경로는 모두 이 커밋의 저장소 루트 기준이다. 변하는 작업 트리 대신 `git show <커밋>:<경로>`로 확인한다.
- 현재 증거로 확인할 수 있는 것은 단위 테스트와 지정 Pixel 10 환경의 선택 회귀다. 모든 화면 크기, TalkBack, 키보드, 동작 줄이기, 스토어 배포 적합성까지 통과했다는 뜻은 아니다.

## 2. 현재 구현

1. `MtjTarotCatalog`가 IO 스레드에서 JSON과 필수 이미지 78개의 존재·비어 있지 않음을 확인한다. 실패 시 부분 덱을 내보내지 않고 화면에 오류와 재시도를 제공한다.
2. UI는 일반 추첨 모드인 `SpreadDrawMode.Normal`만 노출한다. 1장 스프레드도 78장 선택 화면을 거친다. 브리지에 있는 `FinalOneFromTen` 지원을 현재 UI 기능으로 오인하지 않는다.
3. 78장은 폭과 무관하게 8열·10행으로 배치한다. 마지막 행은 6장이고 가운데 정렬한다. 행·열 간격은 2dp, 그림 종횡비는 0.62다. 전체 그리드는 남은 높이에 맞추며 선택 화면에는 스크롤이나 페이지 이동이 없다.
4. 카드는 공통 뒷면을 보여준다. 첫 탭은 선택, 같은 카드 재탭은 취소다. 선택 번호는 선택 ID 목록의 순서로 표시하며, 취소하면 남은 번호가 앞으로 당겨진다.
5. 필요한 장수를 채우면 미선택 카드는 비활성화되지만 선택 카드는 계속 취소할 수 있다. 하단에 예약된 56dp 영역에 `결과 서랍`이 나타난다. 탭 또는 24dp 이상 위쪽 드래그로 결과를 연다. 마지막 선택 직후 자동 전환하지 않는다.
6. `다시 섞기`는 이미 선택한 ID와 그 위치를 보존하면서 미선택 카드만 다시 배치한다. 선택 중 하단 탐색을 숨긴다. 결과에서 뒤로 가면 선택이 유지된 상태로 돌아오며, 선택 화면에서 뒤로 가면 진행 중 세션을 끝내고 스프레드 문맥으로 돌아간다.
7. 결과 이미지는 `ImageDecoder`로 IO 스레드에서 디코딩하고 긴 변을 최대 720px로 제한한다. `ContentScale.Fit`을 사용하며 디코딩 실패는 텍스트로 표시한다. 카탈로그의 첫 바이트 검사만으로 AVIF 디코딩 성공을 보장하지 않는다.

## 3. 소스 소유권과 변경 경계

아래는 현재 협업상의 변경 조정 책임이다. 저장소의 CODEOWNERS 선언을 대신하지 않는다. 구현 파일을 변경하기 전에 담당 작업과 파일 소유권을 맞춘다.

| 책임 | 정확한 소스 경로 | 변경 담당·의존성 |
| --- | --- | --- |
| 자산 로딩·정규 ID | `tarot/src/MtjTarotCatalog.kt` | 타로 도메인 담당과 Android 통합 담당 공동 조정 |
| 추첨 고정·스냅샷 | `tarot/src/MtjTarotBridge.kt`, `tarot/src/MtjTarotEntry.kt` | 타로 도메인 담당; records 계약과 연결 |
| 모델·스프레드·정역방향 | `android/vendor/com/softcat/mystictarot/Models.kt`, `SpreadDefinitions.kt`, `SpreadGeometry.kt`, `ReadingSessionIntegrity.kt`, `ReadingInterpretation.kt` (모두 같은 디렉터리) | vendored 도메인 소스; 원천 변경과 Android 통합을 함께 검토 |
| 화면·터치·이미지·접근성 | `android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt` | MTJ Android 개발 전담 |
| 선택 규칙·그리드 | 같은 Java 패키지의 `TarotSelectionFocusReducer.kt`, `TarotSelectionGridContract.kt` | MTJ Android 개발 전담. 파일 이름에 Focus가 남아 있어도 현재는 선택 토글 함수다 |
| 복원·결과 표시 정책 | 같은 Java 패키지의 `TarotRecreationState.kt`, `TarotSpreadOverviewPolicy.kt`, `TarotSpreadLabelFormatter.kt`, `TarotDetailDirectionPolicy.kt` | Android 통합; 타로·records 계약 유지 |
| 앞면·카탈로그 | `android/app/src/main/assets/mtj/tarot/tarot_00.avif`부터 `tarot_77.avif`, `tarot_cards_78.json` | 자산 담당의 출처·매핑 확인 후 Android 통합 |
| 뒷면·기능 아이콘 | `android/app/src/main/res/drawable-nodpi/tarot_back_mint.png`, `android/app/src/main/res/drawable/ic_tarot_cards.xml` | 디자인 담당 산출물과 Android 통합; 두 자산의 권리 근거는 별개 |
| 공통 하단 여백·탐색 | `design/native/MtjBottomActionScaffold.kt`, `design/native/MtjComponents.kt`, `design/native/MtjTheme.kt` | 공통 디자인/Android 담당; 다른 화면 영향 검토 |
| 선택·복원 회귀 | `android/app/src/test/java/com/hoscat/mtj/dev/TarotSelectionGridContractTest.kt`, 같은 디렉터리의 `TarotUiFlowContractTest.kt`, `TarotRecreationStateTest.kt` | Android 테스트 담당; 실제 접근성 검증은 QA/OPS |

Android 통합 작업 ID는 `01a084e2-4df6-7921-be01-4096a324d7ea`, MTJ 디자인 전담은 `01a084e4-49c2-7b31-aece-a85374f769ec`, 고급 프리미엄 디자이너는 `019ea81d-f09d-7ef1-a16c-9e26b1ffd764`, 조정 작업은 `01a0853c-43d3-7be3-8673-bcfc2ef46f82`다. 현재 디자인 판단은 이 MTJ 디자인/프리미엄 담당과 조정하며, 제외된 먕디자인TF 자료를 새로운 승인 근거로 삼지 않는다.

이 문서 담당의 기존 AUX 변경 범위는 `RecordsScreen.kt`, `SettingsScreen.kt`, `ic_saju_chart.xml`, `ic_tarot_cards.xml`이었다. 타로 핵심 화면 변경 권한을 뜻하지 않는다. 기존 Android 개발 루트는 `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/work/cell/android`이며, `/Volumes/MTJNativeBuild/cell`과 GitHub 체크아웃을 같은 최신 상태로 가정하면 안 된다.

## 4. 덱 ID·카드 ID 매핑

현재 로더가 반환하는 덱은 `TarotDeck(id="standard", name="유니버셜 타로", enabled=true)`다. 덱 ID는 카드 번호나 외부 사이트의 덱 번호와 다른 식별자다.

- 정규 `card.id`는 0..77이다. 이미지 경로는 `mtj/tarot/tarot_XX.avif`이며 XX는 2자리 0 채움 숫자다. 예: ID 8 → `tarot_08.avif`.
- 런타임 URI는 `file:///android_asset/mtj/tarot/tarot_XX.avif`다. JSON의 `image_uri`는 없거나 null/빈 문자열이거나 이 URI와 정확히 같아야 한다.
- 화면에서 읽는 `카드 N`은 섞인 그리드의 1부터 시작하는 위치다. 그림의 정규 ID가 아니다. 선택 번호 역시 정규 ID가 아니라 선택 순서다. 클릭은 위치 숫자가 아닌 `card.id`를 reducer에 전달한다.

| 정규 ID | 카드 |
| --- | --- |
| 0–7 | 바보, 마법사, 여사제, 여황제, 황제, 교황, 연인, 전차 |
| 8–14 | 힘, 은둔자, 운명의 수레바퀴, 정의, 매달린 사람, 죽음, 절제 |
| 15–21 | 악마, 탑, 별, 달, 태양, 심판, 세계 |
| 22–35 | 완드: 에이스, 2–10, 페이지, 기사, 여왕, 왕 |
| 36–49 | 컵: 에이스, 2–10, 페이지, 기사, 여왕, 왕 |
| 50–63 | 소드: 에이스, 2–10, 페이지, 기사, 여왕, 왕 |
| 64–77 | 펜타클: 에이스, 2–10, 페이지, 기사, 여왕, 왕 |

특히 힘=8, 정의=11이다. 다른 덱의 번호 관행이나 파일 정렬을 그대로 적용하지 않는다. 외부 후보 `The Short Hand Tarot`의 사이트 덱 번호 `6`은 앱의 `standard`와 연결된 식별자가 아니다. 외부 해시 파일명/사이트 카드 번호 → 정규 ID → 이름 → 최종 파일 SHA의 명시적인 대조표를 작성하고 78장 그림을 검수한 뒤 통합해야 한다. 이 커밋에 그 후보 덱이 통합됐다는 근거는 없다.

## 5. 데이터·상태 계약

카탈로그는 UTF-8 JSON 배열이다. 각 행에 정수형 `id`, 비어 있지 않은 문자열 `name_en`, `name_kr`, `arcana`, `basic_meaning`, 문자열 배열 `upright_keywords`, `reversed_keywords`가 필요하다. 키워드 배열은 비어 있을 수 있지만 포함된 각 문자열은 비어 있으면 안 된다. ID 중복·누락·범위 초과를 거부하고, 0..21은 `Major Arcana`, 22..77은 `Minor Arcana`여야 한다. 반환 순서는 JSON 행 순서와 관계없이 ID 순서다.

`basic_meaning` 뒤에 정방향/역방향 키워드를 원래 순서대로 붙여 meaning을 만든다. 기록 스냅샷과 표시가 연결되므로 키워드 정렬이나 문구 정규화도 데이터 변경으로 취급한다. 로더 오류 코드는 `CATALOG_UNREADABLE`, `INVALID_CATALOG`, `IMAGE_UNREADABLE`, `EMPTY_IMAGE`다. SHA·라이선스·실제 그림과 이름 일치는 로더가 검사하지 않는다.

선택 계약은 `TarotSelectionState(deckOrder, selectedIds, requiredCount)`다. reducer는 기존 선택의 중복과 덱 밖 ID를 제거하고 필요한 개수까지만 정규화한다. 덱에 없는 탭은 무시한다. 이미 선택된 ID는 제거, 여유가 있으면 뒤에 추가, 가득 차 있으면 새 ID 추가를 무시한다. 음수 requiredCount는 거부한다. reducer만으로 덱 전체가 올바른 78장 순열인지 검증하는 구조는 아니다.

`TarotRecreationState`는 Activity 저장 상태용이며 영구 초안 저장소가 아니다. 버전 1의 15개 평탄화 필드에 spreadKey, 시작 여부, seed, 시작 질문·역방향 설정, catalogHash, 섞인 ID 78개, 선택 ID 최대 10개, 잠금 여부와 결과/기록 식별자·시각·해시를 담는다. 카탈로그나 비트맵 전체를 Bundle에 넣지 않는다. 시작 질문은 trim된 비어 있지 않은 최대 240자다.

복원은 저장한 ID 순서·seed와 카탈로그 fingerprint를 검증하고 결과 해시까지 확인한다. 실패하면 `failed=true`를 반환하여 새 무작위 결과로 조용히 교체하지 않는다. 결과를 확정한 뒤 같은 브리지에 다른 선택을 잠그면 오류다. 결과에서 선택으로 돌아올 때는 `reopenSelection()`으로 잠금과 결과 식별자를 해제한다. `MtjResultSnapshot`의 spread/slots/buttons/aiPromptSnapshot/useReversedAtStart도 함께 유지해야 하며, 구형 SavedReading JSON만 저장하면 동반 정보가 사라진다는 코드 주석이 있다.

## 6. 배포 가능한 자산의 요건과 남은 출처 확인

현재 Git 트리에는 AVIF 78장, JSON, PNG 뒷면이 있지만 이 범위에서 앞면·뒷면 각각의 배포 허가를 연결하는 증빙 문서는 확인하지 못했다. 파일 포함이나 빌드 성공만으로 권리 확인이 끝났다고 표시하지 않는다. `android/app/src/main/assets/licenses/material-design-icons-LICENSE.txt`는 기능 아이콘 라이선스이며 덱 그림·뒷면의 허가 증거가 아니다.

후보 덱 자료는 `/Volumes/뽀그리/Project 먕/샤로먕/design-assets/The_Short_Hand_Tarot_20260909/`에 있다. 이 패키지의 README와 `source-manifest.json`은 제작자 Markus Pfeil, JPEG 78장(295×496), 사용자 진술상 허가 합의 완료, 실행된 허가 문서는 패키지에 없음으로 기록한다. 이는 보존된 로컬 기록을 확인한 것이며 이번 작업에서 외부 사이트나 계약을 새로 검증하지 않았다.

배포 담당에게 전달할 자산 명세에는 다음을 포함한다.

- 덱 식별자·버전, 제작자, 원본 URL/입수 경로·날짜, 원본 파일 SHA-256.
- 파일별 정규 카드 ID·이름·최종 파일 SHA-256, 원본에서 변환한 크기·형식·색상·자르기 내역, 변환 도구/버전.
- 허가 문서 위치와 승인 담당자. 상업적 모바일 배포, 수정·현지화, 홍보 캡처, 배포 지역, 앱스토어 및 저장/CDN 제공 범위와 표시 의무를 담당자가 확인한 기록. 이 목록은 인수인계 확인 항목이며 법률 판단을 대신하지 않는다.
- 앞면 78장과 뒷면을 각각 식별하는 승인 범위, 필요한 크레딧/라이선스 파일의 앱 패키징 위치.
- 78장 이미지 실제 디코딩 결과, ID·그림·이름 전수 대조 결과, 기기별 색상·축소 가독성 검수 결과.

미해결: 현재 번들 AVIF의 원천과 허가 연결, PNG 뒷면의 원본/제작·허가 연결, 외부 후보 덱의 계약 파일과 정규 ID 대조표. 다른 외부 레퍼런스의 시각적 참고와 앱에 이미지 파일을 포함할 권리는 별도로 기록한다.

## 7. 접근성·78장 터치 위험

현재 카드는 `Role.Button`과 위치/선택 순서를 포함한 contentDescription을 가진다. 뒷면 Image 자체의 contentDescription은 null이다. 진행 숫자는 `N장 선택됨, M장 필요`로 설명한다. 다만 카드에는 명시적인 `selected`/`stateDescription`, live region, 키보드 방향키 매핑, `FocusRequester` 기반 복귀 정책이 구현돼 있지 않다. Compose 기본 클릭·포커스 동작이 있을 수 있으므로 키보드가 전혀 작동하지 않는다고 단정하지 않는다.

| 위험·요구 | 현재 근거와 필요한 확인 |
| --- | --- |
| 작은 터치 영역 | 그리드 내부 폭 360dp만 가정해도 셀 폭은 `(360-14)/8 = 43.25dp`다. 실제 바깥 여백을 빼면 더 작다. 실제 hit target과 이웃 카드 영역을 측정해야 한다. 최소 48×48dp의 독립 터치 영역을 목표로 대안 탐색 방식도 디자인 담당과 검토한다 |
| 높이·마지막 행 | 높이 420dp 가정 시 셀 높이는 `(420-18)/10 = 40.2dp`다. 600dp 폭이어도 높이가 부족하면 그림은 작다. 마지막 6장이 system inset, 완료 서랍과 겹치지 않는지 확인한다 |
| 선택 전후 hit target | 기존 기기 보고서는 선택 상태 semantics의 자식 bounds가 안쪽으로 줄어드는 현상을 기록한다. 한 좌표의 재탭 통과로 경계 전체가 안정적이라고 결론내리지 않는다 |
| TalkBack | 1..78 위치 순서 탐색, 중복 낭독 여부, 선택/취소와 순번 재배열 알림, 완료 후 미선택 카드 상태, 마지막 선택 취소를 실제 음성으로 확인한다. 목표 문구 예: `카드 12, 선택됨, 두 번째 선택` |
| 포커스 | 재탭·재섞기·회전·결과 열기/복귀 때 focus가 사라지거나 엉뚱한 카드로 이동하지 않아야 한다. 일반 반복문에 카드 ID 기반 Compose key가 없어 재섞기 시 위치와 ID의 포커스 정책을 명시할 필요가 있다 |
| 키보드·스위치 | Tab/Shift+Tab으로 모든 조작에 도달하고 Enter/Space로 선택·취소·결과 열기가 가능해야 한다. 방향키의 행 이동 및 마지막 6장 처리, 결과에서 Back/Escape의 일관성은 별도 설계·검증 항목이다 |
| 결과 서랍 | 탭 경로가 이미 있어 스와이프만 강요하지 않는다. TalkBack의 클릭 액션/라벨과 키보드 작동, 완료 알림을 검증한다 |
| 큰 글자·색상 | fontScale 1.3/2.0과 밝음/어두움에서 1..10 번호가 잘리지 않고 뒷면과 구분돼야 한다. 색뿐 아니라 번호·상태 텍스트도 유지한다 |
| 동작 줄이기 | 현재 선택/재섞기에 명시적 사용자 정의 전환 애니메이션은 없다. 정역방향 회전은 정적 정보다. 시스템 애니메이션 비활성화에서 로딩 표시와 Material 효과를 확인하고, 향후 플립/셔플 애니메이션에는 생략 경로를 둔다 |

48dp 목표와 고정 8열 전체보기의 충돌을 단순히 이웃과 겹치는 투명 터치 영역으로 해결하지 않는다. 확대된 접근 가능한 탐색 모드 등 구체적인 대안을 디자인/Android가 결정하고 검증한다. 기존 focus-only 첫 탭 모델을 되살리는 변경은 현재 선택 계약과 충돌한다.

## 8. 재현·빌드·테스트

기준 환경은 JDK 17, Gradle 8.11.1, AGP 8.10.1, Kotlin 2.1.21, compile/target SDK 36, min SDK 31이다. 커밋에 Gradle wrapper는 없다. `ANDROID_USER_HOME/debug.keystore`가 있어야 하며 `ANDROID_USER_HOME` 자체도 필수다. Android Studio SDK 경로나 로컬 Gradle 설치는 개발 환경에 맞춘다.

공유 체크아웃에 쓰지 않고 아래처럼 커밋에서 소스만 새 임시 디렉터리로 추출한다. `android`만 가져오면 sibling 소스가 빠진다. 이 저장소는 vendor와 `tarot/src`, `saju/src`, `records/src`, `design/native`를 app의 같은 Kotlin 모듈에 포함한다.

```sh
MTJ_REPO='/Volumes/뽀그리/Project 먕/샤로먕/repositories/NB'
MTJ_REV='b4ffe8718bf01b02930468451baa4ecab45f1db3'
MTJ_VERIFY_ROOT=$(mktemp -d /tmp/mtj-tarot-doc-repro.XXXXXX)
git -C "$MTJ_REPO" archive "$MTJ_REV" android tarot saju records design | tar -x -C "$MTJ_VERIFY_ROOT"
cd "$MTJ_VERIFY_ROOT/android"
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/thomaslee/Library/Android/sdk'
export ANDROID_USER_HOME='/Volumes/MTJNativeBuild/android-user'
export GRADLE_USER_HOME='/Volumes/MTJNativeBuild/gradle'
export TMPDIR='/Volumes/MTJNativeBuild/tmp'
export XDG_CACHE_HOME='/Volumes/MTJNativeBuild/cache'
export KOTLIN_DAEMON_RUN_FILES_PATH='/Volumes/MTJNativeBuild/kotlin'
export JAVA_TOOL_OPTIONS='-XX:-UsePerfData -Djava.io.tmpdir=/Volumes/MTJNativeBuild/tmp'
/tmp/gradle-8.11.1-dist/gradle-8.11.1/bin/gradle --no-daemon --no-parallel --no-build-cache -Dorg.gradle.workers.max=1 :app:testDebugUnitTest --tests com.hoscat.mtj.dev.TarotSelectionGridContractTest --tests com.hoscat.mtj.dev.TarotUiFlowContractTest --tests com.hoscat.mtj.dev.TarotRecreationStateTest :app:assembleDebug
shasum -a 256 app/build/outputs/apk/debug/app-debug.apk
```

JBR가 실제로 사용하는 Java 버전은 `"$JAVA_HOME/bin/java" -version`으로 확인한다. 재현 환경에는 SDK 36, 실행 가능한 Gradle, 위 캐시/임시 경로와 준비된 디버그 키스토어가 필요하다. 전체 회귀가 필요한 소스 변경 후에는 `:app:testDebugUnitTest :app:assembleDebug`를 실행한다. APK 재빌드는 환경·서명 차이로 기존 배포 SHA와 달라질 수 있으므로 새 빌드 SHA로 새 증거를 묶는다.

### 카드 선택 회귀 순서

1. 질문을 입력하고 일반 3장 스프레드에 진입한다. 78장 전부, 0/3, 마지막 행 6장, 완료 서랍 없음 상태를 캡처한다.
2. 서로 다른 카드 A, B를 한 번씩 탭한다. 2/3이고 배지가 1, 2인지 확인한다. C를 탭하면 3/3과 서랍이 나타나고 화면은 선택 단계에 남아야 한다.
3. 섞지 않은 상태에서 C의 동일한 위치를 재탭한다. 2/3, 서랍 숨김, A/B 순서 유지가 기대값이다. 중심뿐 아니라 선택 전후 테두리 근처도 확인한다.
4. A를 취소해 B의 번호가 1로 바뀌는지 확인한다. 빈 자리를 다시 채운 뒤 미선택 카드를 탭해 초과 선택이 안 되는지 확인한다. 선택한 마지막 카드의 취소는 가능해야 한다.
5. 선택 일부를 유지하고 재섞기한다. 선택 ID·순서·위치 유지, 미선택 덱 중복/누락 없음, 포커스 정책을 확인한다.
6. 서랍을 탭해 결과를 연 뒤 뒤로 간다. 같은 선택 상태로 복귀해야 한다. 1장·5장·10장 스프레드도 확인하고 회전/Activity 복원 후 무단 재추첨이 없는지 검사한다.
7. 360dp, 411dp, 600dp 폭, 짧은 높이/가로 방향, 밝음·어두움, fontScale 1.0/1.3/2.0에서 마지막 행과 번호·조작을 검사한다. TalkBack, 키보드, 동작 줄이기를 각각 별도 실행 기록으로 남긴다.

ADB 설치·캡처·logcat은 QA/OPS의 지정 기기와 대여 시간에 수행한다. 이 문서는 기기 점유를 시작하지 않는다. 실행 담당은 APK SHA, 설치된 base APK SHA, serial/API/해상도/density/fontScale/테마, PNG/XML, 음성/키보드 관찰 결과를 함께 기록한다. 기존 `evidence/saju-metadata-r2/qa_pixel10.py`는 특정 좌표와 화면 문구에 의존하므로 다른 기기에 그대로 실행하지 않는다.

## 9. 증거와 해시

아래 APK 및 주요 소스/보고서 SHA는 이번 문서 작업에서 지정 커밋의 blob을 `git show ... | shasum -a 256`으로 다시 계산했다.

| 파일 | SHA-256 |
| --- | --- |
| `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk` | `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2` |
| `tarot/src/MtjTarotCatalog.kt` | `9be92e1f9a96422afa3eb7b89feb280f1cea8a0a59fbd6562656de1b3250d2b0` |
| `android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt` | `fcd9df0e63469e6a4aeb1dedf3b50465cc7cdeb8eeeba38d55a378afb5d506c3` |
| `android/app/src/main/assets/mtj/tarot/tarot_cards_78.json` | `c3950a93c61dbea0321ad8e41cf4f15f0f7deee02884ce1f8ffe9a62e3803697` |
| `android/app/src/main/res/drawable-nodpi/tarot_back_mint.png` | `f609d693ded0d24506d9ea7784d09f59c793427510ab063b8f1f6185d9423ceb` |
| `evidence/saju-metadata-r2/REPORT.md` | `341e2da52f960d138f8efb0b5ba3d81e5a66f3361a551e36fc1532f473c33ebd` |

기준 커밋의 `evidence/saju-metadata-r2/REPORT.md`, `release-manifest.json`, `qa-verdict.json`은 전체 단위 테스트 94건, 실패/오류 0건과 Pixel 10 API 37, 1080×2424, density 420, fontScale 1.0의 회귀 통과를 기록한다. `unit-results/TEST-com.hoscat.mtj.dev.TarotSelectionGridContractTest.xml`에서 선택 계약 4건 통과를 직접 확인했다. 이 문서 작성자가 기기 테스트를 다시 실행한 것은 아니다.

해당 디렉터리의 `tarot-0of3`, `tarot-2of3`, `tarot-3of3`, `tarot-retap-2of3` PNG/XML 쌍이 3장 회귀 증거다. `evidence-index.json`과 `runtime-metadata.txt`가 기기 환경·APK 연결을, `SHA256SUMS`가 증거 파일의 해시를 기록한다. `source-manifest.sha256`에는 AVIF 78장 각각과 소스 해시가 있다. 문서 작업에서 모든 PNG를 육안 검수하거나 전체 SHA256SUMS를 재검증하지는 않았다.

기존 보조 작업 증거는 다음과 같다. 이들은 최신 커밋 검증을 대체하지 않는다.

- `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-expansion-3/outputs/t03-t04-android-handoff-20260911.md`: SHA `97975f13d01b6b548545ffa9675ce4c6fc14e14f24743badd082a849b1b7b819`.
- 같은 outputs의 `t03-t04-card-first-design-brief-20260911.md`: SHA `864e09bc410352eeb1a56420f5a97201f96a1bdf361c67041d3c91478c7fb549`. 초기 승인 대기와 focus-only 관련 지적은 역사적 기록이며, 현재 토글 reducer·결과 서랍 구현과 최신 위임이 우선한다.
- `/tmp/mtj-t03t04-cell-regression-20260911084045/android/app/build/outputs/apk/debug/app-debug.apk`: 기존 보고 SHA `212e4f85b0cb7a4ca5971fdd29719c891de1c5b462349fd01fced0783ee27503`. 과거 복사본의 단위/빌드 통과이며 현재 배포 후보가 아니다. `/tmp`는 없어질 수 있다.
- `/Volumes/뽀그리/Project 먕/샤로먕/design-assets/The_Short_Hand_Tarot_20260909/source-manifest.json`: 이번에 계산한 SHA `ee9fb39432b9e00ad45dea35d26b6755f9bc227857b8cc1d50992f049e21d22c`.

## 10. 우선순위·인수 조건

| 우선순위 | 다음 작업 | 담당·완료 조건 |
| --- | --- | --- |
| P0 배포 전 | 현재 앞면/뒷면의 출처·허가와 최종 SHA 연결 | 자산/배포 담당: 증빙 경로와 승인 범위가 최종 파일에 연결됨. 후보 덱 사용 시 별도 ID 매핑 완료 |
| P1 | 작은 화면 터치 영역 및 마지막 행 검증/수정 | 디자인·Android·QA: 360/411/600dp, 짧은 높이에서 각 카드의 독립 조작 가능성과 가림 없음 증거 확보 |
| P1 | TalkBack·키보드·포커스 보완 | Android·접근성 QA: 78장 전부 선택/취소, 완료/복귀, 재섞기, 순번 변화가 음성/키보드로 이해·조작 가능 |
| P1 | 실제 자산 검증 자동화 | Android·자산 담당: 78장 디코딩, 중복/누락/잘못된 ID·URI·손상 파일 거부 테스트 추가. 현재 조사한 app 테스트 목록에는 카탈로그 전용 테스트가 없음 |
| P2 | 큰 글자·동작 줄이기 및 복원 행렬 확대 | Android·QA: fontScale 1.3/2.0, 애니메이션 비활성화, 회전과 결과 복귀에서도 계약 유지 |
| P2 | 후속 릴리스 증거 묶음 보존 | 조정/배포 담당: 변경 소스 SHA, APK/설치 SHA, 환경·PNG/XML·접근성 관찰과 판정을 한 묶음으로 전달 |

인수 완료는 선택 회귀 테스트 통과, ID/이미지 전수 대조, 담당자별 자산 확인, 위 접근성 행렬 증거와 미해결 항목의 명시적 이관을 기준으로 판단한다. 현재 상태는 핵심 선택 회귀 근거가 있는 개발 APK이며, 전체 접근성·자산 배포 확인은 미완료다.

## 11. 새 유지보수 담당자의 첫날 체크리스트

- [ ] 기준 커밋과 로컬 origin을 확인하고, 기존 작업 트리와 증거용 스냅샷을 구분한다.
- [ ] 이 문서의 소유권 표를 읽고 Android/타로/디자인/QA 담당을 연결한다.
- [ ] 카탈로그 78개 ID와 이미지 파일을 대조한다. 정규 ID, 그리드 위치, 선택 순번, 외부 덱 번호의 차이를 설명할 수 있다.
- [ ] 최신 R2 보고서·선택 테스트 XML·APK SHA를 확인한다. 과거 `/tmp` APK를 최신으로 사용하지 않는다.
- [ ] 분리된 소스 추출본에서 선택·복원 테스트와 빌드를 재현한다. 결과 로그와 새 APK SHA를 보존한다.
- [ ] QA/OPS 기기 일정에 맞춰 3장 선택/재탭/결과 복귀를 확인하고, TalkBack·키보드 검증 시간을 확보한다.
- [ ] AVIF 앞면·PNG 뒷면·외부 후보 덱의 증빙 위치와 미해결 사항을 자산 담당에게 확인한다.
- [ ] P0/P1 후속 작업을 담당자·인수 조건·증거 경로와 함께 넘긴다. 내부 조정 메시지는 영어, 유지보수 문서는 한국어로 기록한다.
