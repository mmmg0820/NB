# 샤로먕 브랜드·제품 디자인 인수인계

문서 기준 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3`  
태그: `v0.1-dev-20260911-r2`  
커밋 제목: `Move Saju calculation metadata into localized details`  
원격 저장소: `https://github.com/mmmg0820/NB.git`  
작성 기준일: 2026-09-11

이 문서는 샤로먕 Android 앱의 브랜드 방향, 화면 문법, 현재 구현 상태, 승인된 디자인 자료, 검증 근거와 다음 작업을 한곳에 정리한다. 새 담당자는 디자인 이미지만 따라 그리지 말고, 아래의 상태 계약과 실제 Kotlin 소스를 함께 확인해야 한다.

## 1. 제품과 브랜드 방향

샤로먕은 사주의 구조 판독과 타로의 선택 리딩을 한 제품 안에서 연결하는 앱이다. HOSCAT이 최상위 브랜드이며, 샤로먕은 차분한 정밀 도구처럼 보여야 한다. 무속 장식, 별자리·수정구·연기 같은 상투적 상징, 과한 금색과 네온, 의미 없는 유리 카드 반복은 사용하지 않는다.

핵심 인상은 다음과 같다.

- 정보는 정확하고 단단하게 읽힌다.
- 조작부는 가볍고 즉시 반응한다.
- 사주는 정밀 판독, 타로는 직접 선택이라는 서로 다른 사용 감각을 유지한다.
- HOSCAT 고양이 심볼과 민트·로즈의 절제된 대비로 제품군 정체성을 만든다.
- 화면을 카드 대시보드처럼 잘게 쪼개지 않는다. 큰 정보면, 얇은 구분선, 명확한 제목과 실제 콘텐츠 길이로 위계를 만든다.
- 같은 배치의 타로 스프레드는 한 카테고리로 묶되, 각 리딩의 장수·좌표·회전·의미 순서는 보존한다.

내부 디자인 방향명은 `Oracle Lens`다. 여기서 Lens는 반투명 효과 자체가 아니라, 복잡한 정보를 분명하게 판독하게 해 주는 제품 태도를 뜻한다. 현재 Android 구현은 불투명 정보면과 제한적인 톤 표면을 사용한다. 실제 blur나 굴절 효과는 구현되어 있지 않으며, 정보면·CTA·타로 카드 이미지에 유리 효과를 추가하면 안 된다.

## 2. 현재 구현된 제품 동작

기준 커밋의 실제 구현을 요약하면 다음과 같다.

### 앱 구조

- 하단 탭은 `홈 / 사주 / 타로 / 기록 / 설정` 다섯 개다.
- `MainActivity.kt`가 탭과 공통 상태를 보유한다.
- 홈과 타로는 하나의 `tarotQuestion` 상태를 공유한다.
- 타로 화면은 `rememberSaveableStateHolder`와 `TarotRecreationState`로 선택 흐름을 복원한다.
- 테마는 `시스템 / 라이트 / 다크` 세 가지이며 `MtjTheme`이 실제 앱 테마다.

### 홈

- HOSCAT 고양이 심볼과 라이트·다크 헤더 소재를 사용한다.
- 질문 입력값을 홈에서 작성하고 타로 화면으로 전달한다.
- `과거 · 현재 · 미래` 영역 전체가 타로 진입 대상으로 구현되어 있다.
- 최근 기록을 홈에서 읽고 기록 상세로 이동한다.

### 사주

- 생년월일은 `YYYYMMDD` 한 필드, 태어난 시간은 `HHmm` 한 필드다.
- `시간 모름`을 명시적으로 지원하며, 시주를 임의 생성하지 않는다.
- 결과 순서는 명식, 일간, 연주, 월주, 일주, 시주다.
- 계산 기술 메타데이터는 주 결과면에서 제거되어 `계산 기준` 상세 팝업에 한국어 날짜·시간대로 표시된다.
- 작은 폭 또는 `fontScale >= 1.3`에서 명식 열 수를 줄여 핵심 글자를 보존한다.
- 예시 시안의 사주 글자는 레이아웃용 fixture다. 실제 결과는 반드시 사주 엔진 출력으로 표시한다.

### 타로

- 정상 선택 가능한 27개 리딩을 여덟 형태로 묶는다.
  - 한 장형 1
  - 가로형 6
  - 격자형 7
  - 십자형 5
  - V형 1
  - 곡선형 4
  - 원형 1
  - 두 갈래형 2
- 형태 카테고리 안에서 실제 리딩 목적을 고른다.
- 질문은 공백일 수 없고 최대 240 Unicode code point다.
- 카드 선택은 78장을 `8열 × 10행` 한 화면에 표시한다. 마지막 행 여섯 장은 가운데 정렬된다.
- 첫 탭은 선택, 같은 카드 재탭은 취소다. 선택 순서가 스프레드 의미 순서에 연결된다.
- 필요한 장수를 모두 골라도 자동으로 결과로 이동하지 않는다.
- 결과 서랍은 필요한 장수를 모두 골랐을 때만 나타나며, 탭 또는 위로 밀기로 연다.
- 다시 섞기는 선택한 카드의 ID와 순서를 유지하고 선택하지 않은 카드만 다시 배열한다.
- 뒤로 가면 질문, 스프레드, 카드 선택 상태를 복원한다.
- 결과 카드 이미지 78장은 `android/app/src/main/assets/mtj/tarot/`의 AVIF와 JSON 목록을 사용한다.

### 기록

- 저장된 사주·타로 결과를 기기 내부 저장소에서 불러오고 상세, 삭제, 오류, 빈 상태를 처리한다.
- 기준 커밋의 필터는 `전체 / 사주 / 타로` 세 개다.
- 승인된 디자인 기준은 `전체 / 사주 / 타로 / 기록` 네 개다. 이 차이는 아래 P0 디자인 부채로 남아 있다.

### 설정

- 화면 모드, 움직임 줄이기, 역방향 포함을 기기 설정에 저장한다.
- `움직임 줄이기` 값은 현재 저장만 된다. 커밋에는 실제 화면 전환·애니메이션 구현이 없고, 토큰의 90/190/80ms 지속 시간도 사용되지 않는다.
- 현재 설정 화면에는 앱 정보만 있다. 승인 시안의 테마 상세, 카드 덱, 개인정보 처리방침, 이용약관 목적지는 아직 구현 계약을 확인해야 한다.

## 3. 시각 언어와 사용 근거

### 색과 소재

실제 앱의 활성 색 체계는 `design/native/MtjTheme.kt`다.

| 역할 | 라이트 | 다크 | 사용 원칙 |
|---|---|---|---|
| 캔버스 | `#FAFBFA` | `#171B1A` | 긴 정보면의 기본 배경 |
| 표면 | `#FFFFFF` | `#202623` | 입력, 다이얼로그, 제한된 패널 |
| 본문 | `#151B18` | `#F3F6F3` | 제목·핵심 정보 |
| 보조 | `#536159` | `#BBC8C0` | 메타데이터·설명 |
| 주요 액센트 | `#7B2345` | `#F4BAD0` | 선택, 탭, 주요 동작 |
| 맥락 액센트 | `#226349` | `#96DCB7` 계열 | 사주·보조 상태 |
| 구분선 | `#DCE2DE` | `#3B4740` | 화면 구조 분리 |

다섯 오행색은 의미가 있는 명식 요소에만 사용한다. 앱 전체 배경이나 장식용 그라데이션으로 확장하지 않는다. 카드 결과 이미지는 색을 바꾸거나 유리 효과로 덮지 않는다.

`android/vendor/com/softcat/mystictarot/ui/theme/HarmonyTokens.kt`에는 별도의 타로 테마 체계가 남아 있지만 `MainActivity`는 `MtjTheme`을 사용한다. 새 작업에서 두 체계를 섞지 말고, 필요하면 별도 마이그레이션 작업으로 정리한다.

### 형태와 여백

- 기본 모서리 반경은 8dp 이하, 타로 프레임은 6dp다.
- 기본 좌우 여백은 16dp, 411dp 이상 20dp, 600dp 이상 24dp다.
- 콘텐츠 최대 폭은 552dp다.
- 일반 조작 목표는 최소 48dp, 주요 버튼 높이는 최소 52dp다.
- 하단 CTA와 내비게이션 사이 간격은 8dp다.
- 반복 목록은 둥근 카드 묶음보다 행과 구분선으로 구성한다.

78장 한 화면 선택에서는 카드 폭이 Pixel 10 증거 기준 약 40dp 수준으로 줄어든다. 사용자가 요청한 전체 조망을 유지하기 위한 예외다. 이 화면은 일반 48dp 목표를 충족하지 못하므로 실제 휴대폰 오탭률과 TalkBack 탐색을 별도 검증해야 한다. 투명 hit target을 서로 겹치게 확장하면 안 된다.

### 타이포그래피

- 화면 제목 24sp, 섹션 제목 20sp, 본문 16sp, 라벨 14sp, 하단 탭 12sp가 현재 기준이다.
- 사주 한자는 Serif Bold, 일반 UI는 Sans Serif를 사용한다.
- 글자 간격은 0sp다.
- 긴 한국어를 한 줄로 억지 축소하지 않는다. 줄바꿈 또는 열 재배치를 사용한다.
- 첨부된 Neutral Face 폰트는 기준 커밋에 번들되어 있지 않다. 브랜드 영문 워드마크에 적용하려면 라이선스, Android 리소스 추가, 한글 fallback과 렌더링 검증을 별도 수행한다.

### 브랜드 자산

- 공통 고양이 심볼: `android/app/src/main/res/drawable-nodpi/hoscat_common_layer.png`
- 홈 헤더 소재: `sharomyang_header_material_light.png`, `sharomyang_header_material_dark.png`
- 타로 카드 뒷면: `tarot_back_mint.png`
- One UI 적응형 앱 아이콘: `mipmap-anydpi-v26`, `mipmap-anydpi-v33`, `ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`

고양이 얼굴의 시각 크기는 제품군 아이콘 사이에서 같아야 한다. 배경이나 액센트가 달라도 심볼의 중심, 얼굴 너비, 귀 끝 안전영역을 임의 변경하지 않는다. monochrome 아이콘은 색이 없어도 HOSCAT 실루엣이 남아야 한다.

## 4. 인터랙션 원칙

1. 선택 상태는 형태와 순번, 접근성 문구로 함께 전달한다.
2. 결과 전환은 사용자가 결과 서랍을 직접 열 때 발생한다.
3. 다시 섞기는 선택 취소가 아니다. 선택 ID와 순서는 보존한다.
4. 뒤로 가기는 입력을 잃는 명령이 아니다. 질문·스프레드·선택 상태를 복원한다.
5. 스프레드를 변경할 때 기존 선택을 어떻게 처리할지는 명시적으로 결정해야 한다. 기존 카드 의미를 새 위치 라벨에 조용히 재매핑하면 안 된다.
6. 타로 선택 결과는 사용자가 선택한 ID 순서대로 스프레드 위치 의미와 연결한다.
7. 로딩, 빈 상태, 오류는 서로 다른 상태 화면이다. 데이터가 있는 목록 위에 장식처럼 겹치지 않는다.
8. 오류 후 재시도는 기존 기록과 필터 상태를 보존한다.
9. `움직임 줄이기`가 켜지면 이동·확대 애니메이션을 0ms 또는 즉시 상태 전환으로 바꾼다.
10. 필수 조작은 제스처만으로 제공하지 않는다. 결과 서랍은 탭과 위로 밀기를 모두 지원한다.

## 5. 정확한 소스 소유권과 파일

| 책임 | 기준 파일 |
|---|---|
| 앱 탭·공유 질문·사주 입력 상태 | `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt` |
| 홈 정보 구조·브랜드 헤더 | `android/app/src/main/java/com/hoscat/mtj/dev/MtjHomeScreen.kt` |
| 타로 화면·78장 선택·결과 | `android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt` |
| 타로 저장/복원·셔플·선택 잠금 | `TarotRecreationState.kt` |
| 8×10 그리드 계약 | `TarotSelectionGridContract.kt` |
| 8개 형태 카테고리 | `TarotSpreadCategoryContract.kt` |
| 스프레드 이름·장수·위치 라벨 | `android/vendor/com/softcat/mystictarot/SpreadDefinitions.kt` |
| 스프레드 실제 좌표 계산 | `android/vendor/com/softcat/mystictarot/SpreadGeometry.kt` |
| 타로 도메인 연결 | `tarot/src/MtjTarotBridge.kt`, `MtjTarotCatalog.kt`, `MtjTarotEntry.kt` |
| 사주 입력 계약·계산 | `saju/src/`, `android/vendor/com/hoscat/core/` |
| 사주 결과 조형 | `SajuChartDisplay.kt` |
| 기록 화면·필터 | `RecordsScreen.kt` |
| 기록 저장·직렬화 | `RecordStore.kt`, `RecordSnapshots.kt`, `records/src/` |
| 설정 | `SettingsScreen.kt` |
| 활성 공통 테마·토큰 | `design/native/MtjTheme.kt` |
| 공통 섹션·빈 상태 | `design/native/MtjComponents.kt` |
| 하단 CTA·하단 탭·인셋 | `design/native/MtjBottomActionScaffold.kt` |
| 런처·브랜드 이미지 | `android/app/src/main/res/` |
| 78장 출시용 타로 자산 | `android/app/src/main/assets/mtj/tarot/` |

제품 화면에서 도메인 코드를 직접 보여 주지 않는다. `layoutId`, `presetId`, `policyCode`, `dataVersion`은 내부 추적용이다. 사용자 화면에는 한국어 제목, 설명, 날짜·시간대로 번역된 계산 기준만 표시한다.

## 6. 데이터와 상태 계약

### 타로 세션

`TarotRecreationState`는 다음을 저장한다.

- `spreadKey`
- 시작 여부와 셔플 seed
- 시작 시점 질문과 역방향 설정
- 78장 셔플 ID와 선택 ID
- 결과 잠금 여부
- 읽기·기록 ID, 저장 시각, 결과 hash

복원 시 78개 ID가 0~77의 순열인지, 선택 ID가 중복되지 않는지, 질문이 240자 이하인지, 저장 결과 hash가 같은지 확인한다. 복원 실패를 새 세션처럼 조용히 초기화하지 않고 실패 상태로 남긴다.

### 스프레드

카테고리는 탐색용이다. 결과 위치는 항상 실제 `spreadKey`의 정의를 사용한다. `mini_celtic`과 `celtic_cross`는 중심 두 장의 의도적 90도 중첩을 보존한다. 이 중첩을 일반 겹침 오류로 제거하지 않는다.

선택 목록에서 제외된 정의는 다음과 같다.

- `final_one_from_ten:final`
- `six_cards:relationship`
- `yes_or_no:yes_no_signal`
- `hammer_nail:hammer_nail_flow`
- `decision_v:decision_flow`

### 사주

- `시간 모름`은 `null`로 제출한다.
- 이전 시간 입력값은 토글을 다시 끌 때 복원할 수 있도록 UI 상태에 보존한다.
- 시주가 없으면 `시간 모름` 슬롯을 표시하고 한자·오행을 만들지 않는다.
- 계산 결과와 근거는 실제 엔진에서 가져온다. 디자인 fixture를 복사하지 않는다.

### 기록

- 프로필 종류는 기록 목록에서 제외한다.
- 목록은 최신 저장 시각 순으로 정렬한다.
- 삭제는 확인 다이얼로그를 거치며 실패 시 원본을 유지한다.
- 네 번째 `기록` 필터를 구현할 경우 새 도메인 종류를 발명하지 말고, 사용자가 저장한 리딩·명식 기록이라는 UI 의미와 실제 저장 계약을 먼저 맞춘다.

## 7. 승인된 디자인 참조

### 주 화면 패키지

경로: `/Users/thomaslee/Documents/Codex/2026-06-09/pentagram-wolff-olins-collins-landor-mucho/outputs/ux-completion-20260911-0831/`

- `selection-states.png`: 78장 선택 전, 부분 선택, 완료, 재탭 취소
- `retap-executed.png`: 브라우저 상태 전환 검증
- `home-roundtrip.png`: 홈→타로→홈 질문 유지
- `geometry-examples.png`: 2장, 3장, 8장 격자, 양자택일 5장
- `spread-atlas.png`, `spread-mapping.json`: 27개 key와 원본 좌표
- `implementation-contract.md`: 상세 동작 계약
- `manifest.json` SHA-256: `e1f6e64e7b56ed3243076fff255167cd33e8e10e11fe68cac9d99c93e633a878`

### 보조 화면면 패키지

경로: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/secondary-v2-20260911/`

- 26개 개별 PNG
- 라이트·다크, 시간 입력·시간 모름, 기본·2배 글자
- 실제 카드 이미지가 있는 타로 결과와 기록 상세
- 기록 목록·빈 상태·로딩·오류
- 설정 라이트·다크
- `manifest.json` SHA-256: `5ce50e79104294144b74adac096cca084bb289643b0cde689e195fc00c7b29a1`

설정 비교 보드는 v2 파일 대신 다음 파일을 사용한다.

- `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/settings-board-v3-20260911/board-settings.png`
- SHA-256: `18b63ed905e753de8c70eeb4081470c5ccf3573d6b86a3cd42c5065f4831eaf9`

통합 검토 문서:

- `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/combined-design-packet-20260911.md`
- `/Users/thomaslee/Documents/Codex/2026-06-09/pentagram-wolff-olins-collins-landor-mucho/outputs/design-director-handoff-20260911/director-verification.json`
- 검증 JSON SHA-256: `d1dc187cfa293dd91da213b83312845cca2803b8e2fa5cfe68f7719f2620e224`

디자인 검토용 JPEG와 이를 포함한 보드는 출시 허용 자산 목록이 아니다. GitHub 또는 APK에 복사할 때는 Android 저장소의 출시 manifest와 78개 AVIF/JSON만 기준으로 삼는다.

## 8. 기준 커밋의 빌드와 검증

### 환경

- Android Gradle Plugin 8.10.1
- Kotlin 2.1.21
- Compose BOM 2025.05.01
- Java 17
- compileSdk/targetSdk 36, minSdk 31
- 증거 로그의 Gradle 8.11.1
- 디버그 패키지 `com.hoscat.mtj.dev`
- 버전 `0.1-dev`, versionCode 1

저장소에는 Gradle Wrapper가 없다. Gradle 8.11.1이 설치된 환경에서 실행한다. `ANDROID_USER_HOME/debug.keystore`가 있어야 현재 signing 설정이 동작한다.

```bash
export ANDROID_USER_HOME=/absolute/path/to/android-user-home
cd /absolute/path/to/NB/android
gradle --offline --no-daemon clean testDebugUnitTest assembleDebug
```

산출물 hash 확인:

```bash
shasum -a 256 app/build/outputs/apk/debug/app-debug.apk
```

기기 설치와 실행 예시:

```bash
adb -s emulator-5556 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5556 shell am force-stop com.hoscat.mtj.dev
adb -s emulator-5556 shell am start -n com.hoscat.mtj.dev/.MainActivity
```

기준 커밋 증거:

- 최신 디버그 APK: `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk`
- APK SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- 서명 인증서 SHA-256: `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab`
- 클린 빌드·단위 테스트: 94개 통과, 실패·오류·skip 0
- Pixel 10 AVD: API 37, 1080×2424, density 420, fontScale 1.0
- 설치된 base APK와 배포 APK SHA 일치
- QA 판정: `evidence/saju-metadata-r2/qa-verdict.json`
- 빌드 로그 SHA-256: `3c4439067262ad772f4cb1b45eae93ba7f832bb00a6c08e46a807d9f44205b6d`
- QA 판정 JSON SHA-256: `2fd15dcc373a9ad38fd4befe88e09d7fef5d34019a0d04a51fafa0df8d06c4d0`
- release manifest SHA-256: `4272f0f6576fd84b470433e37c58394fe6edab209886576ab82cb7962fd11dcc`

이전 `89d40ac0...` APK는 사주 결과에 기술 메타데이터가 노출되어 대체된 이력이다. 새 검증의 기준으로 사용하지 않는다.

## 9. 시각 검토 체크리스트

### 모든 화면

- 360dp, 411dp, 600dp에서 가로 잘림이 없는가.
- 시스템 inset, IME, 하단 탭과 CTA가 콘텐츠를 가리지 않는가.
- 라이트·다크에서 본문, 보조문구, 구분선, 비활성 상태가 읽히는가.
- 긴 한국어와 희귀 한자가 대체 글리프 없이 표시되는가.
- fontScale 1.3과 2.0에서 제목·버튼·명식이 겹치지 않는가.
- TalkBack 읽기 순서가 화면의 시선 순서와 같은가.
- 선택·오류·성공을 색만으로 구분하지 않는가.
- 빈 상태, 로딩, 오류, 저장됨이 실제 데이터 상태와 일치하는가.
- 같은 radius와 패널 스타일을 의미 없이 모든 영역에 반복하지 않았는가.
- HOSCAT 심볼을 장식처럼 중복 사용하지 않았는가.

### 타로

- 78장이 한 화면에 모두 보이고 마지막 여섯 장이 온전히 보이는가.
- 선택 전 0/N, 부분 선택, 완료 N/N, 재탭 N-1/N을 모두 확인했는가.
- 재탭 시 순번이 재정렬되고 결과 서랍이 즉시 사라지는가.
- 결과 서랍과 마지막 카드 행이 겹치지 않는가.
- 다시 섞은 뒤 선택 ID와 순서가 유지되는가.
- 홈의 질문이 타로와 홈 복귀 후 그대로 남는가.
- 여덟 카테고리 합계가 27개이며 누락·중복이 없는가.
- 리딩별 실제 좌표·회전·의미 순서를 사용했는가.
- 실제 카드 이미지를 자르거나 임의 색보정하지 않았는가.

### 사주

- 네 기둥과 여덟 글자가 첫 화면에서 판독 가능한가.
- `시간 모름`일 때 시주가 명시적으로 비어 있는가.
- 오행색이 장식이 아니라 글자의 실제 속성과 일치하는가.
- 큰 글자에서 2×2 재배치 후에도 연주·월주·일주·시주 순서가 분명한가.
- 계산 기준 상세에 기술 코드가 노출되지 않는가.

### 기록과 설정

- 기록 목록, 상세, 빈 상태, 로딩, 오류를 각각 확인했는가.
- 필터 문구가 승인된 네 항목과 구현 계약에 맞는가.
- 삭제 확인과 실패 후 데이터 보존을 확인했는가.
- 시스템·라이트·다크 설정이 즉시 반영되고 재실행 후 유지되는가.
- 움직임 줄이기가 실제 모션에 반영되는가.

## 10. 알려진 한계와 디자인 부채

### P0

1. 기록 필터 불일치: 현재 구현은 세 개, 승인 시안은 네 개다. 저장 계약과 `기록`의 의미를 확정한 뒤 `RecordsScreen.kt`와 테스트, 빈 상태 문구를 함께 수정한다.
2. 78장 카드 터치 정확도: 한 화면 요구 때문에 일반 48dp 목표보다 작다. 실제 휴대폰에서 오탭률, TalkBack 탐색, 마지막 행 가림을 검증한다. 사용자 승인 없이 페이지형으로 변경하지 않는다.
3. 출시 자산 경계: 디자인 패키지의 Short Hand JPEG는 출시 승인 자산이 아니다. 기준 결과·앱·GitHub에는 저장소의 출시 허용 78 AVIF와 JSON만 사용한다.

### P1

1. 움직임 줄이기 연결: 설정값은 저장되지만 실제 모션 코드가 없다. 홈→타로, 카테고리→목적, 결과 서랍, 저장 완료에 짧은 전환을 추가하고 reduced-motion을 연결한다.
2. 네이티브 큰 글자 검증: CSS 2배 시안과 Kotlin의 `fontScale >= 1.3` 분기는 실제 Android fontScale 2.0 증거가 아니다. 360/411/600dp에서 기기 캡처를 남긴다.
3. 스프레드 변경 정책: 카드 선택 중 형태 또는 목적을 바꿀 때 기존 선택을 유지할지 초기화할지 제품 정책과 테스트가 필요하다.
4. 설정 목적지: 개인정보 처리방침, 이용약관, 카드 덱 정보 화면의 실제 연결과 문구를 확정한다.
5. 다크 대비와 시스템 바: Pixel 10 외 실제 One UI 기기에서 상태·내비게이션 바, adaptive icon, dark theme를 확인한다.

### P2

1. 테마 중복 정리: `HarmonyTokens.kt`와 활성 `MtjTheme.kt`의 역할을 문서화하거나 사용하지 않는 체계를 제거한다.
2. 브랜드 폰트: Neutral Face를 영문 워드마크에 사용할 경우 라이선스와 Android font resource, fallback을 정식 도입한다.
3. 소재 표현: Oracle Lens의 제한적 조작층을 실제 기기 성능과 대비 조건에서 실험한다. 정보면과 CTA는 불투명을 유지한다.
4. 광학 보정: 앱 아이콘, 고 심볼, 탭 아이콘을 One UI launcher 크롭과 monochrome 모드에서 기기별로 보정한다.

## 11. 화면별 승인 기준

구현 또는 수정 PR은 다음 조건을 만족해야 디자인 완료로 볼 수 있다.

- 승인 이미지와 화면 정보 구조가 일치한다.
- 기준 커밋의 도메인 계약과 실제 데이터 연결을 훼손하지 않는다.
- 지정된 라이트·다크·시간 모름·큰 글자·빈 상태·오류 상태 증거가 있다.
- 타로 27개 key, 장수, 좌표, 회전, 의미 라벨에 누락과 중복이 없다.
- 78장 선택 0/N, 부분, 완료, 재탭 취소와 뒤로 복원을 자동 테스트와 기기 캡처로 증명한다.
- 사주에서 시주 미상과 실제 계산 결과를 혼동하지 않는다.
- 앱 아이콘이 adaptive, round, monochrome에서 같은 HOSCAT 실루엣을 유지한다.
- 화면마다 기능과 무관한 장식, 반복 카드, 네온 glow, 무속 상징이 없다.
- 컴포넌트 경계, 타이포, 문구 선택에 설명 가능한 이유가 있다.
- APK hash, 설치된 base APK hash, 기기 정보, fontScale, 테마가 증거 문서에 기록된다.

## 12. 의존성과 인수인계

- 제품 UI는 Compose Material3와 `design/native` 모듈 소스를 함께 컴파일한다.
- 사주는 `saju/src`와 `android/vendor/com/hoscat/core`에 의존한다.
- 타로는 `tarot/src`, `android/vendor/com/softcat/mystictarot`, 78개 AVIF/JSON에 의존한다.
- 기록은 `records/src`와 앱의 `RecordStore`/`RecordSnapshots`에 의존한다.
- debug signing은 `ANDROID_USER_HOME/debug.keystore`에 의존한다.
- 디자인 패키지의 절대 경로는 현재 작업 기기의 검토 경로다. 저장소로 옮길 때 출시 불가 카드 자산을 포함하지 않는다.
- 디자인 변경은 이미지 한 장만 전달하지 말고 상태 계약, 긴 문구, dark/fontScale, 접근성, 증거 hash를 함께 넘긴다.

## 13. 새 담당자 첫날 체크리스트

- [ ] `b4ffe8718bf01b02930468451baa4ecab45f1db3`와 `v0.1-dev-20260911-r2`의 관계를 확인한다.
- [ ] 저장소 `README.md`, `evidence/REPORT.md`, `evidence/saju-metadata-r2/REPORT.md`를 읽는다.
- [ ] 이 문서의 주·보조 디자인 패키지와 settings v3 보드를 직접 연다.
- [ ] `MainActivity.kt`에서 다섯 탭과 공유 질문 상태를 추적한다.
- [ ] `MtjTheme.kt`와 `MtjBottomActionScaffold.kt`에서 활성 토큰과 인셋 구조를 확인한다.
- [ ] `TarotScreen.kt`, `TarotRecreationState.kt`, `SpreadDefinitions.kt`로 질문→형태→목적→78장→결과 흐름을 따라간다.
- [ ] `spread-mapping.json`과 27개 실제 정의를 대조한다.
- [ ] `SajuChartDisplay.kt`에서 시간 모름과 계산 기준 상세를 확인한다.
- [ ] `RecordsScreen.kt`의 세 필터와 승인된 네 필터 차이를 이슈로 등록한다.
- [ ] `SettingsScreen.kt`의 움직임 줄이기 값이 모션에 연결되지 않은 점을 확인한다.
- [ ] Gradle 8.11.1, Java 17, Android SDK 36, debug keystore를 준비한다.
- [ ] 클린 단위 테스트와 debug assemble을 실행하고 APK SHA를 기록한다.
- [ ] Pixel 10 기준 증거를 재현한 뒤 One UI 실제 기기에서 라이트·다크·큰 글자·78장 터치를 추가 검증한다.
- [ ] 출시 manifest에 없는 디자인 자산을 APK나 공개 GitHub에 넣지 않는다.
- [ ] 완료 보고에는 커밋, APK SHA, 설치 SHA, 기기·테마·fontScale, PNG/XML 증거 경로를 함께 남긴다.

이 문서는 공유 Git checkout을 수정하지 않고, 기준 커밋과 동결된 디자인·QA 산출물을 읽어 작성했다.
