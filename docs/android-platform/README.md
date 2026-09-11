# 샤로먕 Android 플랫폼 인수인계

작성일: 2026-09-11. 담당 범위: Android 앱 통합, Compose 화면 연결, 상태 소유권, 빌드 및 APK 증거 관리.

## 1. 목적과 기준 버전

이 문서는 새 유지보수자가 검증된 디버그 버전을 재현하고, 계산·기록 계약을 훼손하지 않으면서 다음 변경을 통합하기 위한 안내서다. 향후 설계안과 현재 구현을 구분한다. 문서 작성 과정에서 공유 Git 체크아웃, 앱 소스, 기기 및 원격 저장소는 수정하지 않았다.

| 항목 | 기준 |
| --- | --- |
| 저장소 | https://github.com/mmmg0820/NB |
| 고정 커밋 | `b4ffe8718bf01b02930468451baa4ecab45f1db3` |
| 태그 | `v0.1-dev-20260911-r2` |
| APK 경로 | `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk` |
| 패키지 | `com.hoscat.mtj.dev` |
| 표시 이름 | 샤로먕 |
| 버전 | `versionCode=1`, `versionName=0.1-dev` |
| 검증 상태 | 사주 메타데이터 S1 수정 및 지정 회귀 범위 `FINAL_PASS` |

`0257e9f0d72da5c6f1879c138383b8f41ee846ee`와 `v0.1-dev-20260911`의 APK는 사주 첫 화면에 기술 메타데이터가 노출되어 실패한 이전 후보다. 이력은 보존하되 배포 기준으로 사용하지 않는다. 현 버전은 디버그 산출물이며 스토어 출시, 전체 제품 기능, 전체 접근성·반응형 매트릭스의 인증을 의미하지 않는다.

## 2. 실제 아키텍처와 소스 지도

Gradle 프로젝트에는 `:app` 하나만 있다. `saju`, `tarot`, `records`, `design/native`, `android/vendor`는 현재 독립 Gradle 모듈이 아니다. `android/app/build.gradle.kts`의 `sourceSets["main"].java.srcDirs(...)`로 앱 기본 소스와 함께 컴파일한다. 폴더를 이동하면 상대 경로도 함께 검토해야 한다. `design/native`도 앱의 `R`을 참조하므로 독립 라이브러리처럼 취급하면 안 된다.

아래 경로는 Git 저장소 루트 기준이다. 담당은 변경 조율의 책임 경계이며, 저장소에 자동 강제되는 CODEOWNERS 체계가 있다는 뜻은 아니다.

| 책임 경계 | 정확한 파일·폴더 | 역할 및 협의 대상 |
| --- | --- | --- |
| Android 플랫폼 | `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt` | Activity, 테마 연결, 루트 탭, 사주 초안·계산 호출, 화면 간 공유 상태 |
| 홈 통합 | `android/app/src/main/java/com/hoscat/mtj/dev/MtjHomeScreen.kt` | 명식 진입, 공유 질문, 3장 영역, 최근 기록 |
| 입력 UI 계약 | `android/app/src/main/java/com/hoscat/mtj/dev/BirthDateValidation.kt`, `BirthTimeValidation.kt`, `SolarDatePickerSupport.kt` | 한 칸 입력 검증, 날짜 선택 변환, 오류 표시; 사주 계약 담당과 협의 |
| 사주 결과 표현 | `android/app/src/main/java/com/hoscat/mtj/dev/SajuChartDisplay.kt` | 명식·일간·기둥 순서, 시간 모름, 검토 상태, 계산 기준 팝업 |
| 사주 연결 계층 | `saju/src/MtjBirthInputAdapter.kt`, `saju/src/MtjSajuRuntime.kt` | 도메인 입력 변환, 자산 로딩, 음력 변환, 계산 및 오류 경계 |
| 사주 도메인 | `android/vendor/com/hoscat/core/model/`, `android/vendor/com/hoscat/core/manse/` | `BirthInputDraft`, 명식 모델, 달력·절입·간지 계산; UI 수정으로 임의 변경하지 않음 |
| 타로 화면 | `android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt` | 카테고리·목적 선택, 덱 선택, 서랍, 결과, 기록 저장 |
| 타로 상태·계약 | 같은 패키지의 `TarotRecreationState.kt`, `TarotSelectionFocusReducer.kt`, `TarotSelectionGridContract.kt`, `TarotSpreadCategoryContract.kt`, `TarotQuestionValidation.kt` | 복원 검증, 직접 선택/해제, 격자, 카테고리, 질문 제한 |
| 타로 배치·표시 정책 | 같은 패키지의 `TarotSpreadLabelFormatter.kt`, `TarotSpreadOverviewPolicy.kt`, `TarotDetailDirectionPolicy.kt` | 라벨 줄바꿈, 배치 표시, 역방향 회전 |
| 타로 도메인 연결 | `tarot/src/MtjTarotBridge.kt`, `MtjTarotCatalog.kt`, `MtjTarotEntry.kt` | 덱 로딩, 셔플, 선택 고정, 결과 스냅샷 |
| 타로 기반 정의 | `android/vendor/com/softcat/mystictarot/` | 모델, 스프레드 정의·좌표·해석 및 무결성 |
| 기록 계약 | `records/src/MigrationPlanner.kt`, `RecordsJsonCodec.kt` | Envelope, 참조·중복·충돌 판정, 구조화 직렬화 |
| Android 기록 저장/UI | `android/app/src/main/java/com/hoscat/mtj/dev/RecordStore.kt`, `RecordSnapshots.kt`, `RecordDetailRows.kt`, `RecordsScreen.kt` | SQLite 저장, 도메인 결과 변환, 상세 행, 필터·삭제 |
| 테마·인셋·공통 UI | `design/native/MtjTheme.kt`, `MtjComponents.kt`, `MtjBottomActionScaffold.kt` | HyperOS Design Standard 색상·토큰·레이아웃; 디자인 담당과 공동 검토 |
| 설정 | `android/app/src/main/java/com/hoscat/mtj/dev/SettingsScreen.kt` | 테마 선택, 두 설정 스위치, 정적 앱 정보 |
| Android 리소스 | `android/app/src/main/AndroidManifest.xml`, `android/app/src/main/res/` | edge-to-edge 테마, 라이트/다크 리소스, 런처·단색 아이콘 |
| 자산 | `android/app/src/main/assets/manse/`, `android/app/src/main/assets/mtj/tarot/` | 압축 만세력 데이터, 78장 카드 카탈로그·이미지 |
| 테스트 | `android/app/src/test/java/com/hoscat/mtj/dev/`, `android/app/src/test/java/com/softcat/mystictarot/` | JVM 계약·상태·기하 테스트; 장치 시각 검증과 구분 |

읽기 순서는 `MainActivity` → 해당 화면 → 연결 계층 → vendor 계약 → 테스트가 효율적이다. 현재 Navigation Compose, ViewModel 계층, DI 프레임워크를 사용하는 구조로 설명해서는 안 된다.

## 3. 화면과 상태 소유권

| 상태 | 소유자·보관 방식 | 복원 범위와 주의점 |
| --- | --- | --- |
| 현재 탭 | `MtjApp.tab`, `rememberSaveable` | 0 홈, 1 사주, 2 타로, 3 기록, 4 설정 |
| 홈·타로 질문 | `MtjApp.tarotQuestion`, `rememberSaveable` | 두 화면이 같은 값과 변경 콜백을 사용; 타로 시작 시 세션에 별도 고정 |
| 사주 입력 | `MtjApp`의 별칭·날짜·시간·음력·윤달·성별·시간 모름 | `rememberSaveable`; 한 화면 안의 폼 구성요소가 소유권을 나누지 않음 |
| 사주 계산 결과 | `evaluation`, 일반 `remember` | 구성 변경·프로세스 재생성 뒤 결과 복원은 보장하지 않음; 초안 보존과 결과 보존을 혼동하지 않음 |
| 사주 실행·저장 진행 | `busy`, `saving`, 코루틴 스코프 | 재구성 상태일 뿐 영속 작업 큐가 아님 |
| 타로 화면 상태 | 루트 `SaveableStateProvider("tarot")`, 화면의 `rememberSaveable` | 탭 이동 시 타로 상태 보존을 위한 별도 경계 |
| 타로 세션 | `TarotRecreationState` 및 Saver | seed, 덱 순서, 선택 ID, 시작 질문, 결과 무결성 필드; DB 초안 저장은 아님 |
| 기록 선택·필터 | 루트 `selectedRecordId`와 `RecordsScreen` 내부 상태 | 홈에서 상세 진입용 ID를 전달. 필터가 모든 탭 이탈에 걸쳐 영속 유지된다고 가정하지 않음 |
| 사용자 설정 | `mtj-settings-v1` SharedPreferences | `theme-mode`, `reduce-motion`, `include-reversed` 저장. 실제 기능 연결 범위는 후속 과제 참고 |
| 저장된 결과 | `RecordStore`의 앱 전용 SQLite | 명시적 저장 이후 영속화; 다른 앱 DB를 열거나 자동 가져오기 하지 않음 |

공통 하단 UI는 `MtjBottomActionScaffold`가 소유한다. 상태 바·좌우 safe drawing 인셋과 하단 navigation/IME의 합집합을 처리한다. 호출자가 추가로 같은 시스템 패딩을 중복 적용하면 간격이 틀어진다. IME 표시 중 탭은 숨기며, 카드 선택 화면은 `showNavigation=false`, `showActionDock=false`로 표시한다. 시스템 제스처 인셋까지 제거하라는 뜻은 아니다.

## 4. 현재 구현과 데이터 계약

### 사주

1. 별칭과 `YYYYMMDD` 8자리 한 칸을 입력한다. 시간은 `HHmm` 4자리 한 칸이다. 양력/음력과 윤달 의미를 보존한다.
2. `validateBirthDateText`가 `year/month/day`, `validateBirthTimeText`가 `hour/minute` 문자열을 만든다. 예를 들어 `19950106`, `1636`은 `1995/1/6`, `16/36`으로 연결한다.
3. UI가 `BirthInputDraft`를 구성하고 `MtjSajuRuntime.evaluate`를 호출한다. 시간 모름이면 UI 초안의 기존 시각은 남겨 두고 제출 시 시·분은 빈 문자열로 보낸다. 도메인 파서와 계산 결과에서 미상 시각/시주는 null 의미를 유지한다. 00:00으로 대체하지 않는다.
4. `MtjBirthInputAdapter`는 음력 변환기가 없으면 음력 입력을 받아들이지 않는다. Runtime은 실제 자산 기반 변환기를 공급한다.
5. Runtime은 Default 디스패처에서 계산하고 IO 디스패처에서 자산을 읽는다. Mutex가 지연 로딩 데이터와 계산 접근을 보호한다. 취소 예외를 삼키지 않고, 사용자에게 원시 JSON/자산 예외를 보여주지 않는다.
6. 결과 순서는 명식 → 일간 → 연주 → 월주 → 일주 → 시주다. 시간 미상 슬롯은 `시간 모름`, `오행 산출 제외`이며 가짜 간지 글자가 없다.
7. 일반 크기에서는 네 기둥을 가로 배치하고 작은 폭/큰 글씨에는 순서를 지킨 2열 대안을 사용한다. 실제 신규 APK에서 검증된 크기는 아래 증거 범위에 한정한다.
8. 첫 화면 계산 근거는 `대한민국 표준시`다. `계산 기준` 팝업에서 절입명과 `1995년 1월 6일 04:34:04 (한국 표준시, UTC+09:00)` 형태의 시각을 표시한다. `OffsetDateTime`을 Asia/Seoul로 변환하며 원본 evidence 문자열은 수정하지 않는다. 파싱 실패 시 원시 문자열 대신 표시 불가 문구를 사용한다.
9. 현재 미검증 결과의 배지는 정확히 `검토 필요`다. 테스트 통과를 이유로 계산 데이터의 신뢰 등급을 올리지 않는다. `policyCode`, `dataVersion` 등 내부 식별자는 모델/스냅샷 계약의 일부이지 주 결과 화면 텍스트가 아니다. 설정 화면에 이 두 식별자를 탐색하는 완성된 기능이 있다고 가정하지 않는다.

### 타로

홈의 미리보기 문구·관계/일 프리셋은 제거되어 있다. 3장 영역을 누르면 타로 흐름으로 이동한다. 8개 형태 카테고리는 27개 Normal 리딩 옵션을 분류하고 실제 배치 미리보기를 사용한다.

`selectedIds`는 선택 순서를 가진 중복 없는 카드 ID 목록이다. 덱은 0..77의 78개 ID를 정확히 한 번 포함한다. 첫 탭은 선택, 선택한 카드의 재탭은 해제이며 필요한 장수를 초과하지 않는다. 격자는 8열×10행, 마지막 6장은 중앙에 놓는다. 완료 시 결과 서랍을 노출하지만 자동으로 결과로 이동하지 않는다. 서랍 탭/위로 끌기로 결과를 열며, 결과에서 뒤로 가면 같은 덱과 선택으로 돌아온다. 셔플은 이미 선택한 카드의 위치와 순서를 보존한다.

`TarotRecreationState`는 저장된 seed·순열·카탈로그와 결과의 SHA-256 fingerprint를 검증한다. 질문과 역방향 여부는 시작 시 고정된다. 저장 상태 버전은 1이고 Bundle에 기본형과 ID 배열만 넣는다. 손상된 복원을 새 무작위 결과로 조용히 치환하지 않고 `failed` 상태로 보낸다. 이 fingerprint는 재현/무결성 확인용이며 비밀키 기반 인증이 아니다. 카탈로그·직렬화·복원 계약을 변경하면 기존 상태 호환성을 함께 검토한다.

### 기록

`Envelope`에는 `origin`, `kind`, 구조화 payload, profile 참조, schema/payload 버전, 스냅샷 시각이 있다. `Origin.commonId`가 저장 식별자이며 `SAJU`는 PROFILE 참조 1개, `COMPATIBILITY`는 2개를 요구한다. 호환성 kind가 정의되어 있다는 이유로 해당 UI가 완성됐다고 판단하지 않는다.

`mtj-records-v1.db`의 `records(identity PRIMARY KEY, envelope BLOB)`에 저장한다. `RecordsJsonCodec`으로 깊은 사본을 만든 뒤 Mutex와 트랜잭션 안에서 승인된 INSERT만 반영한다. 동일 데이터는 SKIP, 충돌·잘못된 데이터는 배치 거절이다. 참조 중인 PROFILE 삭제는 막는다. DB 업그레이드는 현재 명시적 오류이며 파괴적 초기화 경로가 없다. 전체/사주/타로 필터와 목록·상세·삭제 확인을 제공한다.

## 5. 빌드 환경과 재현 명령

| 구성 | 검증된 값 |
| --- | --- |
| Gradle | 8.11.1, 저장소 내 Wrapper 없음 |
| Android Gradle Plugin | 8.10.1 |
| Kotlin 및 Compose plugin | 2.1.21 |
| Compose BOM | 2025.05.01 |
| Activity Compose | 1.10.1 |
| Coroutines Android / Gson / JUnit | 1.10.2 / 2.11.0 / 4.13.2 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 31 |
| 실제 실행 JDK | Android Studio JBR OpenJDK 21.0.10 |
| Java/Kotlin 출력 대상 | Java 17 / JVM target 17 |
| 서명 검사 도구 | SDK build-tools 36.1.0의 apksigner |

새 담당자는 전용 클론을 만들고 먼저 고정 커밋을 확인한다. 아래 예시는 새 디렉터리에만 적용한다. 이후 작업은 별도 브랜치에서 시작한다.

```sh
git clone https://github.com/mmmg0820/NB.git NB-maintainer
cd NB-maintainer
git switch --detach b4ffe8718bf01b02930468451baa4ecab45f1db3
git status --short --branch
git show --stat --oneline HEAD
```

기존 검증 호스트에서 사용한 명령은 다음과 같다. 다른 호스트에서는 경로를 실제 설치 위치로 치환한다. `--offline`은 의존성이 이미 캐시에 있을 때만 동작한다. 빈 환경에서의 의존성 부트스트랩까지 검증한 것은 아니다.

```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/thomaslee/Library/Android/sdk'
export ANDROID_USER_HOME='/Volumes/MTJNativeBuild/android-user'
export GRADLE_USER_HOME='/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/gradle-home'
MTJ_GRADLE='/private/tmp/gradle-8.11.1-dist/gradle-8.11.1/bin/gradle'
cd android
set -o pipefail
"$MTJ_GRADLE" --no-daemon --offline clean :app:testDebugUnitTest :app:assembleDebug
```

생성 APK는 `android/app/build/outputs/apk/debug/app-debug.apk`, 테스트 결과는 `android/app/build/test-results/testDebugUnitTest/TEST-*.xml`이다. 이번 기준은 테스트 94개, 실패·오류·건너뜀 모두 0이다. `clean`은 개인 체크아웃의 생성물에만 적용한다. 공유 빌드 중 동시에 실행하지 않는다.

Gradle 속성은 heap 3GB, daemon 비활성화, Kotlin in-process 컴파일, VFS watch 비활성화다. 과거 생성 디렉터리에 `* 2.dex` 복사본이 생겨 D8 중복 오류가 난 적이 있다. 우선 소스 중복인지 생성물 중복인지 구분하고 전용 체크아웃의 clean build로 재현한다. 소스 파일을 임의 삭제해 통과시키지 않는다. `. _`가 아닌 실제 AppleDouble 이름 `._*`와 `* 2.kt` 제외 설정은 근본적인 소스 동기화 정책을 대신하지 않는다.

### 디버그 서명

빌드 스크립트가 `ANDROID_USER_HOME`을 필수로 요구하고 그 안의 `debug.keystore`를 사용한다. 동일 패키지를 기존 기기에 업데이트하려면 동일 서명이 필요하다. 키 파일은 소스·문서·공개 Git에 넣지 않는다. 새 키를 임의 생성해 기존 검증 키를 대체하지 않는다. 서명이 다르면 OPS와 키/테스트 기기 사용 방식을 조율한다. 키 파일 내용이나 암호를 로그로 출력하지 않는다.

```sh
"$ANDROID_HOME/build-tools/36.1.0/apksigner" verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk
shasum -a 256 app/build/outputs/apk/debug/app-debug.apk
```

릴리스 서명, Play App Signing, AAB 배포 파이프라인은 현재 완료 범위에 없다. `versionCode=1`도 계속 재사용 중이므로 실제 배포 전에 버전 증가 정책이 필요하다. 동일 소스를 다시 빌드했다는 사실만으로 바이트 SHA가 동일하다고 가정하지 않는다.

## 6. 장치 재현과 수락 기준

먼저 OPS와 기기 소유권을 확인한다. 기기 작업은 반드시 `adb -s <serial>`을 사용한다. 다른 팀이 쓰는 AVD를 종료하거나 앱 저장소를 지우지 않는다. `install -r`로 갱신하고 설치된 base.apk의 SHA를 비교한다. 현재 Pixel 10 증거는 `emulator-5556`, Pixel 9a는 별도 OPS 관리 대상이었다. 다음 실행에서 serial이 같다고 단정하지 않는다.

```sh
ADB="$ANDROID_HOME/platform-tools/adb"
"$ADB" devices -l
"$ANDROID_HOME/emulator/emulator" -list-avds
# 전용 포트와 AVD 사용이 조율된 뒤 실행한다.
"$ANDROID_HOME/emulator/emulator" -avd Pixel_10 -port 5556 -no-snapshot -no-audio -gpu swiftshader_indirect
```

에뮬레이터 명령은 계속 실행되므로 별도 터미널에서 설치한다. 부팅 완료, 잠금 해제와 앱 화면 준비를 확인하고 다음 단계로 진행한다.

```sh
MTJ_SERIAL=emulator-5556
"$ADB" -s "$MTJ_SERIAL" shell getprop sys.boot_completed
"$ADB" -s "$MTJ_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
"$ADB" -s "$MTJ_SERIAL" shell am start -n com.hoscat.mtj.dev/.MainActivity
"$ADB" -s "$MTJ_SERIAL" shell wm size
"$ADB" -s "$MTJ_SERIAL" shell wm density
"$ADB" -s "$MTJ_SERIAL" shell settings get system font_scale
"$ADB" -s "$MTJ_SERIAL" shell dumpsys window
"$ADB" -s "$MTJ_SERIAL" shell pm path com.hoscat.mtj.dev
```

`pm path`가 반환한 실제 `base.apk` 경로를 대상으로 장치의 `sha256sum` 또는 pull 후 호스트 `shasum`을 실행한다. 빌드 APK, 설치 APK, 캡처, 원격 다운로드 APK를 같은 SHA로 연결한다.

| 지정 회귀 시나리오 | 통과 기준 |
| --- | --- |
| `QA`, `19950106`, 시간 모름 | 시주 슬롯은 남고 간지 생성 없음, `검토 필요`, 순서 일치 |
| 같은 날짜, `1636` | 정상 시주 표시, 알려진 시각 결과에 시간 모름 문구 없음 |
| 위 두 경우의 라이트/다크 | 첫 화면에 raw ISO·`검증 상태 별도 표시`·내부 식별자 없음, `대한민국 표준시` 표시 |
| 계산 기준 열기/닫기 | 별도 팝업에서 한국어 날짜·시각·시간대 표시, 원시 evidence 보존, 닫기 동작 |
| 가로형 → 과거·현재·미래 | 78장 한 화면, 8×10, 마지막 6장 중앙, 카드 선택 중 하단 탭 없음 |
| 0/3 → 2/3 → 3/3 → 같은 세 번째 카드 재탭 | 3/3에서만 서랍 표시, 재탭 후 2/3·서랍 없음, 앞의 선택 순서 보존 |
| 결과 진입과 뒤로가기 | 자동 진입 없음, 명시적 진입 후 뒤로가면 덱·선택 복원 |
| 실행 로그 | 앱 FATAL·am_crash·am_anr 0, 다른 시스템 앱 이벤트는 별도 기록 |

`evidence/saju-metadata-r2/qa_pixel10.py`는 당시 전용 장치 회귀 도구다. `ADB`, `SERIAL`이 고정되어 있고 결과를 스크립트 옆에 쓴다. 원본 증거 폴더에서 재실행하면 증거를 덮어쓰므로 반드시 개인 작업 폴더로 복사하고 장치·출력 경로를 검토한다. 첫 실행은 라이트/다크 전체, `--resume-dark`는 이전 결과가 존재하는 중간 상태에서만 사용했다. 신규 QA에서는 중간 재개를 최초 재현 명령으로 쓰지 않는다.

고정 시간 대기만으로 결과를 캡처하지 않는다. 실제 계층의 `명식` 표시를 확인한 뒤 PNG/XML을 함께 저장한다. selected 카드의 의미 노드는 부모 버튼과 inset bounds가 다를 수 있다. 같은 좌표가 선택 전후 카드 안에 있는지, 셔플 없이 같은 위치를 다시 탭했는지 기록한다. UI의 `카드 3`은 덱의 세 번째 위치이며 내부 cardId=3이라는 뜻이 아니다.

## 7. 동결·통합·배포 절차

1. 기준 커밋·작업 브랜치·변경 파일·담당자를 명시한다. 공유 canonical에 여러 팀이 동시에 쓰지 않는다. 각 팀은 전용 체크아웃에서 변경하고 플랫폼 담당이 순차 통합한다.
2. 입력/도메인/기록 계약 변경은 관련 담당과 먼저 비교한다. UI만 바꾸는 작업에서 엔진·카탈로그·DB 스키마를 함께 바꾸지 않는다.
3. 통합 diff와 소스 목록을 확인하고 개인 빌드 디렉터리에서 필요한 테스트 및 전체 clean build를 실행한다. 문서 작업만 수행할 때 APK 재빌드나 원격 push는 필요하지 않다.
4. 새 이름의 증거 디렉터리를 만든다. APK, 소스 스냅샷, 파일별 manifest, 서명, 빌드 로그, JUnit XML, 변경 파일 목록을 고정한다. 이전 증거는 덮어쓰지 않는다.
5. `SOURCE_FREEZE`와 APK/소스/manifest SHA를 공유한 뒤 소스 쓰기를 중단한다. 이후 수정은 새 후보 버전·새 APK·새 증거로 다시 시작한다.
6. OPS는 기기 사용을 배정하고 동일 SHA 설치를 확인한다. 플랫폼/QA는 같은 산출물로 화면·상태 회귀를 검증한다. 전체 버퍼 로그와 엄격한 앱 이벤트 검사를 보관한다. PID 로그만으로 시스템 ANR 부재를 주장하지 않는다.
7. QA 판정을 해당 범위와 함께 기록한다. 통과 후 승인된 배포 흐름에서 새 커밋·새 태그로 게시하고, 이전 후보를 superseded로 설명한다. 기존 태그를 옮기거나 기록을 강제로 재작성하지 않는다.
8. 원격 태그가 가리키는 커밋과 다운로드 APK의 SHA를 다시 비교한다. 원격 증거 파일도 로컬 고정본과 바이트 일치 여부를 확인한다.

기존 로컬 canonical은 `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/work/cell`이며 당시 자체 `.git`이 없었다. Git 저장소에서는 이 `cell` 아래 구조가 루트로 옮겨졌다. 스냅샷 manifest의 `work/cell/...`를 Git 루트 상대 경로로 곧바로 검사하면 경로가 맞지 않는다. 별도 빈 디렉터리에 스냅샷을 풀고 그 디렉터리에서 검증한다.

```sh
MTJ_VERIFY_DIR=$(mktemp -d)
tar -xzf /절대경로/source-snapshot.tar.gz -C "$MTJ_VERIFY_DIR"
cd "$MTJ_VERIFY_DIR"
shasum -a 256 -c /절대경로/source-manifest.sha256
```

## 8. 알려진 한계와 우선순위

아래는 이번 문서 작성의 코드 확인으로 드러난 후속 작업이다. 기존 S1 승인 범위를 소급 확대하거나 새로운 장치 테스트를 수행했다는 뜻은 아니다.

| 우선순위 | 현재 한계·근거 | 후속 작업과 완료 기준 |
| --- | --- | --- |
| P1 | 새 요청: 타로 실시간·저장 결과의 두 줄 공통 요약 형식이 현재 구현과 다름 | 아래 12절의 소유 파일·공통 컴포넌트·회귀 계획으로 통합. 기존 r2 PASS 범위에는 포함되지 않음 |
| P1 | 질문 UI는 Unicode code point 240자를 허용하지만 `TarotRecreationState.validate/restore`는 UTF-16 `length<=240`을 요구 | 이모지 등 보조 평면 문자로 시작/회전/복원 테스트. 입력·시작·Saver에 같은 길이 단위를 적용하고 정상 입력이 예외로 끝나지 않아야 함 |
| P1 | 설정의 `include-reversed`, `reduce-motion`은 저장되지만 타로는 자체 `reversed=false` 상태로 시작; 실행부의 설정 소비 연결이 확인되지 않음 | 설정의 실제 적용 범위를 명시하고 단일 설정 저장소 연결. 앱 재시작 및 새 세션에서 설정값과 실제 동작이 일치해야 함 |
| P1 | 사주 `evaluation`은 `remember`여서 테마/Activity 재생성 시 결과가 입력 폼으로 돌아갈 수 있음 | 계산 입력·결과 복원 요구사항 합의 후 ViewModel 또는 재계산/스냅샷 전략 적용. 저장 결과와 미저장 결과 수명을 별도 테스트 |
| P1 | 320dp/fontScale 2, 태블릿, 회전, TalkBack, IME 및 물리 HyperOS 전 범위는 이번 r2의 검증 범위 밖 | approved 2×2 기둥 순서, 핵심 글자/버튼의 잘림·겹침·접근성 검증. 현재 짧은 회귀 PASS를 전체 디자인 승인으로 확대하지 않음 |
| P1 | 안정적인 키/도구가 로컬 절대 경로·캐시에 의존하고 Gradle Wrapper/CI 부트스트랩이 없음 | Wrapper·SDK/JDK 고정·의존성 준비 절차·키 관리·버전 증가를 정리. 깨끗한 전용 환경에서 빌드 가능, 키는 저장소 밖에서 공급 |
| P1 | DB 버전 업그레이드 구현 없음 | 스키마 변경 전 버전별 비파괴 migration과 기존 레코드/참조 보존 테스트. 실패 시 원본 보존 |
| P2 | 단일 Activity에 입력·탭·저장 상태가 집중되고 폴더 경계가 Gradle 경계가 아님 | 변경량이 커질 때 화면 상태/도메인 경계부터 분리. 동결 수정에 대규모 모듈화 끼워 넣지 않음 |
| P2 | 질문 지문 fingerprint는 Gson 직렬화와 객체 구조에 의존; Saver 버전은 1 | 카탈로그나 상태 변경 시 호환성·안내 정책과 버전 전환 테스트 추가 |
| P2 | 날짜/시간 상세 오류가 표시 함수에서 일반 문구로 축약되고, 음력 UI의 미래 날짜 판정은 변환 전 월 비교를 포함 | 실제 사용 입력과 달력 경계 fixture로 문구·검증 책임을 점검. 도메인 변환 결과와 UI 거절이 모순되지 않아야 함 |
| P2 | 현재 문구는 주로 Kotlin 내 한국어이며 언어 리소스 체계·설정의 법적/덱/테마 상세 목적지는 완성 범위가 아님 | 사용자 언어 요구와 필요한 목적지 확정 후 실제 연결을 구현. 정적 디자인의 링크를 기능 완료로 표시하지 않음 |
| P2 | 런처/홈 아이콘과 카드 자산의 권리·전체 브랜드 일치성은 r2 인증 밖 | 디자인 담당·자산 담당이 파일별 출처와 shipping 허용 범위를 확인. 새 승인 없이 검토용 이미지 전체를 패키징하지 않음 |

이번 수정은 사주 표현 파일과 해당 테스트만 바꿨다. 엔진 정확도, AI 상담, 결제·구독, 계정·동기화, 서버 연동은 완료된 앱 기능으로 인수인계하지 않는다.

## 9. 증거와 정확한 해시

| 대상 | SHA-256 |
| --- | --- |
| APK 및 설치본·원격 다운로드 | `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2` |
| `source-snapshot.tar.gz` | `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683` |
| `source-manifest.sha256` | `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85` |
| 서명 인증서 | `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab` |
| 수정된 `SajuChartDisplay.kt` | `eb00dfddaaeb7613db693e56bff024ebc7b8db802db4772517bba956878651f0` |
| 수정된 `SajuChartDisplayTest.kt` | `d8db7574c6c061295e3d30656c2f3261c75e2a28d22423fa6a3a923052945f14` |

- 원격 고정 증거: https://github.com/mmmg0820/NB/tree/b4ffe8718bf01b02930468451baa4ecab45f1db3/evidence/saju-metadata-r2
- 플랫폼 로컬 증거: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/outputs/evidence/sharomyang-saju-metadata-r2-20260911`
- 독립 QA 보고서: `/Users/thomaslee/Documents/Codex/2026-06-11/3-qa-ai-qa-qa-qa/outputs/successor-78277c5f-qa-20260911/REPORT.md`
- 독립 원격 검증: 위 QA 폴더의 `github-verification.json`, `audit-results.json`, `SHA256SUMS`.
- OPS Pixel 9a 증거: `/Volumes/뽀그리/Project 먕/샤로먕/evidence/nb-saju-metadata-r2-78277c5f-20260911/REPORT.md`.

QA는 소스 manifest 168/168 파일, 변경 파일 2개, 94/0/0/0 테스트, 12쌍 PNG/XML, 원격 증거 60개 바이트 일치를 확인했다. 장치 구성은 Pixel 10/9a API 37, 1080×2424, density 420, fontScale 1.0이다. 캡처별 PNG/XML 해시와 공통 실행 설정은 `evidence-index.json`, `runtime-metadata.txt`에 있다. 설정은 실행 공유 자료이며 모든 캡처마다 독립 장치 조회를 했다는 뜻이 아니다.

`android-test-window.log`는 모든 버퍼의 비필터 로그다. 앱 관련 FATAL/ANR/crash는 없었지만, 앞선 에뮬레이터 부팅 중 Messaging·captions·Play Store의 3개 ANR이 존재한다. 따라서 “전역 로그 0”으로 쓰지 않는다. 초기 harness의 화면 준비 지연·체크박스 탐색 실패와 최종 재개 성공도 로그에 남아 있다.

## 10. 의존 관계와 팀 인계

Android 플랫폼은 화면·상태 통합과 소스 동결을 담당한다. 사주/타로 담당은 입력·계산·카탈로그 계약을, 기록 담당은 Envelope와 migration을 검토한다. 디자인 담당은 HyperOS Design Standard와 승인 화면의 의미/배치를 판정한다. OPS는 SDK/JDK·서명·장치 사용·배포 산출물을 관리하고 QA는 지정 범위의 독립 판정 및 증거 동일성을 확인한다.

사주 S1 판정 참조는 `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/secondary-v2-20260911/SPEC.md`와 `saju-dark-unknown-1.png`다. 이 폴더는 내부 디자인 검토 자료이며 `shippingAllowed=false` 자산을 Git/앱에 넣을 근거가 아니다. 현재 r2 변경은 새로운 디자인 자산을 추가하지 않았다.

다음 담당자에게는 커밋/태그, 변경 파일, APK·소스·manifest·서명 SHA, 테스트 로그, 설치 SHA, 장치 소유권, 증거 경로, 미해결 항목을 함께 전달한다. “최신 APK”라는 파일명만 전달하지 않는다. 내부 팀 간 조율은 영어, 미래 유지보수 문서는 한국어로 작성한다.

## 11. 새 담당자 첫날 체크리스트

- [ ] 태그가 `b4ffe8718bf01b02930468451baa4ecab45f1db3`를 가리키는지 확인하고 개인 클론/작업 브랜치를 준비한다.
- [ ] 앱이 독립 Gradle 모듈 여러 개가 아닌 단일 `:app` 소스 집합임을 확인한다.
- [ ] `MainActivity`, 사주/타로 상태 계약, `RecordStore`를 순서대로 읽고 소유권 표와 맞춰 본다.
- [ ] JDK 실행 버전과 JVM 출력 버전의 차이, Gradle 8.11.1, SDK 36, 캐시 위치를 확인한다.
- [ ] 디버그 키를 OPS와 확인하고 인증서 SHA를 대조한다. 키를 복사해 공개하거나 새 키로 덮어쓰지 않는다.
- [ ] 개인 환경에서 clean test/assemble을 실행하고 94개 기준과 차이를 설명한다.
- [ ] 기기 소유권을 배정받고 serial·AVD·API·크기·density·fontScale·앱 focus를 기록한다.
- [ ] 설치 APK SHA와 빌드 APK SHA를 맞춘 뒤 사주 네 상태·상세 팝업·타로 재탭 회귀를 수행한다.
- [ ] 새 증거 디렉터리에 PNG/XML·전체 로그·JUnit 결과·해시를 보관한다.
- [ ] P1 목록에서 담당 과제를 선택하고 합의된 파일 범위만 변경한다.
- [ ] 소스 변경 시 새 SOURCE_FREEZE 후보를 만들며 이전 태그·증거를 보존한다.
- [ ] QA의 제한된 PASS와 제품 전체 출시 승인을 구분해 인계한다.

## 12. 추가 요구: 타로 공통 요약 패널 작업 분해

문서 작성 중 전달된 최신 요구다. 아래는 고정 커밋 `b4ffe871`을 읽고 확인한 차이와 후속 패치 계획이며, 아직 적용된 변경이 아니다. Android 통합 담당이 소유하고 문서 취합 이후 별도 변경으로 진행한다. 독립 push를 하지 않는다.

요약 패널은 실시간 리딩과 저장된 리딩 상세에서 동일한 공통 컴포넌트/포맷터를 사용하며 다음 두 개의 논리적 텍스트 행만 가진다. 긴 문장은 자연스럽게 여러 화면 줄로 줄바꿈할 수 있으므로 `maxLines=2`로 잘라서는 안 된다.

```text
스프레드: {N}장 | {name} | {description}
질문: {original input}
```

`career`와 같은 영어 질문은 번역하거나 예시로 대체하지 않는다. description은 실제 스프레드 데이터이며 `xxxxxx` 같은 자리표시자를 출력하지 않는다. 요약 안에 브랜드·eyebrow·제목을 반복하거나 별도 질문 패널을 만들지 않는다. 카드 배치/해석 상세는 이 요약 계약과 별개다.

### 현재 커밋과의 차이

- `android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt:149`: 이미 `MtjQuietPanel` 한 개에 요약을 묶었지만 `N장 | title`, subtitle, 구분선, 별도 `질문` 라벨과 질문 값으로 렌더링한다. 요구한 두 개의 접두사 포함 행과 다르다.
- `android/app/src/main/java/com/hoscat/mtj/dev/RecordsScreen.kt:133`: 저장된 질문을 title로 다시 표시하고 기록 종류 라벨을 붙인다. `:153`에서는 summary 해석을, `:160`에서는 스프레드/질문을 각각 일반 상세 행으로 표시한다. 실시간 화면과 공유하는 요약 컴포넌트가 없다.
- `android/app/src/main/java/com/hoscat/mtj/dev/RecordDetailRows.kt`의 `tarotDetailRows`: 스프레드 title/cardCount를 조합하지만 description을 요약에 포함하지 않는다. 질문은 저장된 `snapshot.reading.question`에서 읽는다.
- `android/app/src/main/java/com/hoscat/mtj/dev/RecordSnapshots.kt`는 전체 snapshot을 저장하므로 정상 신규 기록에는 `snapshot.spread.subtitle`을 읽을 수 있다. 기존/불완전 레코드에 이 필드가 없을 때의 표시 정책은 별도 테스트가 필요하다.

### 소유 파일별 후속 패치

| 파일 | 계획한 변경 |
| --- | --- |
| 신규 `android/app/src/main/java/com/hoscat/mtj/dev/TarotReadingSummary.kt` | 공통 표시 모델과 두 행 포맷터, 하나의 Compose 요약 패널. 동적 장수·이름·설명·질문을 입력받고 줄바꿈을 허용 |
| `TarotScreen.kt` | 현재 요약 item을 공통 패널 호출로 치환. 시작 스냅샷의 질문과 spread 데이터를 직접 전달 |
| `RecordDetailRows.kt` | 저장된 Value 트리를 구조적으로 읽어 같은 요약 모델로 변환하는 어댑터 추가. 일반 상세 행에서 스프레드/질문 중복 제거 여부를 호출부와 함께 결정 |
| `RecordsScreen.kt` | TAROT 상세 분기에서 공통 패널 사용. 질문을 title로 중복 출력하는 부분과 별도 질문 행 정리. 기존 카드 상세·저장/삭제·사주 기록 동작 보존 |
| 신규 `android/app/src/test/java/com/hoscat/mtj/dev/TarotReadingSummaryTest.kt` | 두 행의 정확한 형식, 원문 질문, 실시간/저장 변환의 동등성 테스트 |
| 기존 `RecordDetailRowsTest.kt` | 새 분리 계약에 맞춰 저장된 질문·선택 카드 순서·누락 필드 처리를 검증 |

위 신규 파일명은 계획이며 현재 커밋에는 존재하지 않는다. 덱 기하, 카드 ID, 셔플/재탭, 사주 엔진 및 기록 스키마 버전 변경은 이 패치에 포함하지 않는다. 레코드의 description이 없는 경우 현재 카탈로그를 무조건 대입해 과거 결과를 다시 해석하지 말고, 저장된 필드 우선과 읽을 수 없는 정보의 표시 정책을 기록 담당·디자인 담당과 확정한다.

질문은 표시 단계에서 추가 trim/번역/치환하지 않는다. 다만 현재 `TarotRecreationState.start`가 질문을 `trim()`하여 저장한다는 기존 계약이 있다. 이미 저장된 질문의 앞뒤 공백을 복구할 수는 없다. “입력 원문”이 공백까지 완전 일치를 뜻한다면 세션 검증·저장 계약의 추가 변경이 필요하므로 이 차이를 조용히 숨기지 않는다.

### 수락 테스트

1. 완전한 1장 fixture의 요약은 `스프레드: 1장 | 오늘의 메시지 | 선택한 카드의 흐름`, `질문: career` 두 논리 행으로 출력된다. 실제 화면에서는 fixture의 설명으로 하드코딩하지 않고 실제 spread description을 사용한다.
2. 3장 등 다른 스프레드에서도 N/name/description이 정확히 바뀐다. live 스냅샷을 저장한 뒤 상세를 열어 두 행이 동일한지 확인한다.
3. `career`, 한국어·영어 혼합, 개행·긴 질문을 유지하며 번역·자리표시자·생략 부호·잘림이 없어야 한다. 기존 시작 시 trim 계약과 표시 단계의 원문 보존을 구분한다.
4. 두 화면의 요약 내부에 브랜드/eyebrow/별도 질문 제목·패널이 중복되지 않는다. 동일 공통 컴포넌트를 호출하는 것을 코드 리뷰로 확인한다.
5. 라이트/다크, 일반·큰 글씨, 좁은 폭에서 자연 줄바꿈과 스크롤 접근성을 확인한다. 두 논리 행 요구를 두 개의 물리적 화면 줄 제한으로 구현하지 않는다.
6. subtitle/cardCount 등 일부 누락된 기존 기록을 열어도 크래시나 새로 계산한 과거 설명이 생기지 않는다. 확정한 누락 정책을 테스트로 고정한다.
7. 기존 사주 S1 회귀와 타로 선택/재탭·뒤로가기·카드 배치 테스트를 보존한다. 새 테스트를 포함한 전체 suite의 실제 건수를 기록하고 새 APK SHA로 live/저장 화면 증거를 수집한다.
