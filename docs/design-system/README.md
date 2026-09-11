# 샤로먕 디자인 시스템 인수인계

## 기준과 범위

- 코드 기준: `b4ffe8718bf01b02930468451baa4ecab45f1db3` (`Move Saju calculation metadata into localized details`).
- 읽기 전용 확인 경로: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB`.
- 이 문서는 디자인 토큰, 화면 구조, 상태 계약, 접근성 및 검수 기준을 설명한다. 사주 계산 정확성이나 타로 해석 엔진의 검증을 대체하지 않는다.
- 실제 구현은 위 커밋의 소스가 기준이다. 디자인 시안에만 있는 요소를 구현 완료로 간주하지 않는다.
- 최신 사용자 요청인 타로 요약 패널 통합은 아래 별도 계약으로 명시한다. Android 담당에게 전달했으며, 수정 커밋 및 APK 확인 전에는 완료로 표시하지 않는다.

## 최신 필수 계약: 타로 요약 패널

실시간 타로 결과와 저장한 타로 기록 상세에 동일한 단일 요약 패널을 사용한다. 패널 안의 논리적 텍스트 행은 정확히 다음 두 개다.

```text
스프레드: {장수}장 | {스프레드명} | {스프레드 설명}
질문: {사용자가 입력한 질문 원문}
```

사용자가 요청한 형식의 예:

```text
스프레드: 1장 | 오늘의 메시지 | 선택한 카드의 흐름
질문: career
```

- 장수, 이름, 설명은 현재 리딩의 스프레드 데이터에서 가져온다. 예시를 모든 스프레드에 하드코딩하지 않는다.
- 사용자 요청의 `xxxxxx`는 설명 자리 표시자이지 실제 표시 문구가 아니다.
- `career` 같은 영어 질문도 입력 원문 그대로 유지한다. 번역하거나 시안의 한국어 질문으로 바꾸지 않는다.
- 기존 별도 브랜드 헤더와 질문 패널을 합친다. 이 요약 영역의 `샤로먕 Tarot` 표제, 반복되는 `오늘의 메시지` 눈썹 제목, 독립된 `질문` 제목을 제거한다.
- 화면 전체의 필요한 탐색 제목까지 무조건 삭제하라는 뜻은 아니다. 요약 영역 내부의 중복을 제거한다.
- 하나의 외곽 패널만 사용하고 내부에 카드/패널을 다시 넣지 않는다. 별도 질문 박스나 질문 전용 테두리를 남기지 않는다.
- 두 개의 논리적 행은 좁은 화면과 큰 글자에서 자연스럽게 줄바꿈할 수 있다. 고정 높이, 한 줄 강제, 말줄임, 질문 잘림을 사용하지 않는다.
- 라이브 결과와 저장 기록은 가능한 한 같은 컴포넌트 또는 포매터를 사용한다. 저장 기록은 해당 기록의 스냅샷을 읽고 다른 세션의 현재 질문을 가져오지 않는다.
- 이 변경으로 78장 선택판의 배치, 카드 선택 상태, 결과 진입 조건, 해석 데이터는 바꾸지 않는다.

### 통합 패널 수용 검사

1. 1장 리딩에서 위 예시처럼 장수/이름/설명/질문이 한 패널 안에 나타난다.
2. 여러 장 스프레드로 바꾸면 장수, 이름, 설명이 함께 정확하게 바뀐다.
3. 요약 영역의 브랜드 표제, 반복 스프레드 제목, 독립 질문 제목 및 두 번째 패널이 모두 사라진다.
4. `career`, 한국어 질문, 긴 질문을 원문 보존하여 표시한다.
5. 저장 후 기록 상세에서도 같은 두 행 구조와 저장된 값이 유지된다.
6. 라이트/다크, 좁은 화면, 글자 배율 1.0/1.3/2.0에서 겹침, 잘림, 가로 넘침이 없다.
7. TalkBack이 동일 제목이나 질문을 중복 낭독하지 않으며 읽기 순서는 스프레드 다음 질문이다.
8. 결과 진입과 돌아가기 후 카드 선택 상태가 보존된다. 카드 배치 회귀가 없다.

증빙: 수정 커밋, 관련 테스트 결과, 라이브/기록의 라이트·다크 캡처를 첨부한다. 전달 사실만으로 통과 처리하지 않는다.

Android 담당 확인: 기준 커밋의 `TarotScreen.kt:149`는 이미 한 패널이지만 장수/제목, 별도 설명, 구분선, 질문 라벨/값 구조로 위 두 행 계약과 다르다. `RecordsScreen.kt:133`, `:153`, `:160` 및 `RecordDetailRows.kt`의 저장 상세는 별도 제목/행 구조이며 공통 요약 컴포넌트가 없다. 현재 문서화 단계에서 네이티브 변경은 하지 않았다. 공통 `TarotReadingSummary.kt`와 저장 스냅샷 어댑터는 제안이며 아직 구현된 파일로 취급하지 않는다.

기존 세션의 trim 계약과 레거시 저장 기록의 설명 누락 처리는 구현 시 명시적으로 정해야 한다. 질문 원문 보존은 번역·대체 금지를 포함하며 기존 저장 시점에 이미 제거된 공백을 복원했다고 주장하지 않는다. 레거시 설명을 임의로 만들어 채우지 않는다. Android 구현 계획은 같은 인수인계 루트의 `android-platform/README.md` 12절을 참조한다.

## 소유권과 파일

저장소 루트 기준 상대 경로다. 디자인 담당은 독립 디자인 산출물과 이 문서를 관리하며 공유 네이티브 파일을 동시에 수정하지 않는다.

| 담당 | 파일 및 책임 |
| --- | --- |
| 디자인 전담 / 고급 프리미엄 디자이너 | 시안, 상태별 화면, 토큰 의도, 인수 기준 |
| Android | `design/native/MtjTheme.kt`, `MtjComponents.kt`, `MtjBottomActionScaffold.kt`: 테마·공통 컴포넌트·하단 구조 |
| Android | `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt`, `MtjHomeScreen.kt`: 화면 상태·탐색·공유 질문 |
| Android / 사주 담당 | 같은 디렉터리의 `SajuChartDisplay.kt`, `BirthDateValidation.kt`, `BirthTimeValidation.kt`, `SolarDatePickerSupport.kt`: 표시·입력 검증 |
| Android / 타로 담당 | 같은 디렉터리의 `TarotScreen.kt`, `TarotSelectionGridContract.kt`, `TarotSelectionFocusReducer.kt`, `TarotSpreadLabelFormatter.kt`, `TarotSpreadOverviewPolicy.kt`: 선택·요약·결과 표시 |
| Android / 기록 담당 | 같은 디렉터리의 `RecordsScreen.kt`, `RecordStore.kt`, `RecordSnapshots.kt`, `RecordDetailRows.kt`: 저장 원문·기록 표시 |
| Android | 같은 디렉터리의 `SettingsScreen.kt`: 저장 설정과 접근성 |
| 타로 담당 | `android/vendor/com/softcat/mystictarot/SpreadDefinitions.kt`, `SpreadGeometry.kt`: 스프레드 정의·좌표 |
| QA / 기기 검증 담당 | 변경 APK의 기능·테마·입력·접근성 회귀 및 해시 일치 |
| 조정 담당 | 최종 범위, 릴리스 판단, 자산 권리 및 배포 승인 |

## 구현 토큰

`design/native/MtjTheme.kt`의 실제 값이다. 시안의 유사한 색을 그대로 구현값으로 간주하지 않는다. 동적 색상은 사용하지 않으며 다크 모드 surfaceTint는 투명하다.

| 역할 | 라이트 | 다크 |
| --- | --- | --- |
| Canvas | `#FAFBFA` | `#171B1A` |
| Surface | `#FFFFFF` | `#202623` |
| Ink | `#151B18` | `#F3F6F3` |
| Muted | `#536159` | `#BBC8C0` |
| Disabled | `#6E7872` | `#9AA79F` |
| Primary | `#7B2345` | `#F4BAD0` |
| Border | `#738178` | `#87968D` |
| Divider | `#DCE2DE` | `#3B4740` |
| Error | `#AE2638` | `#FFACB4` |

라이트 Context `#226349`, ContextSoft `#D7E6DF`, SurfaceMuted `#F0F3F1`; 다크 SurfaceHigh `#28322C`, OnPrimary `#29151E`, PrimaryContainer `#492D39`, Success `#96DCB7`.

| 글자 역할 | 크기/행간(sp) |
| --- | --- |
| Brand | 28/36 |
| ScreenTitle | 24/32 |
| SectionTitle | 20/28 |
| Body / PrimaryAction | 16/24 |
| Label / Supporting | 14/20 |
| Navigation | 12/18 |
| PillarGlyph | 24/32 |
| Ganji | 48/60 |

자간은 0이다. 일반 텍스트는 Sans, 명리 글리프는 Serif 계열이다. 실제 폰트 배율에서 재검증한다.

- 패널·버튼·컨트롤 모서리 8dp, 타로 프레임 6dp, 페이지 섹션 0dp.
- 좌우 여백 16/20/24dp, 분기점 411/600dp, 콘텐츠 최대 폭 552dp.
- 일반 터치 최소 48dp, 주요 버튼 최소 높이 52dp. 글자가 커지면 높이가 늘어나야 한다.
- 하단 탭 최소 64dp, 콘텐츠와 액션 간격 18dp, 액션과 탭 간격 8dp.
- 모션 시간 gather/spread/settle 90/190/80ms, 동작 줄이기 0ms.

## 공통 컴포넌트

- `MtjQuietPanel`: Surface 기반, 8dp, 1dp outlineVariant, 그림자 없음. 내부 가로 18dp/세로 16dp 및 간격 10dp.
- `MtjSectionHeader`: 선택적 눈썹 제목과 heading semantics. 통합 타로 요약에는 중복 헤더를 다시 넣지 않는다.
- `MtjEmptyState`: 빈 상태 제목·본문. 실제 오류와 빈 상태를 섞지 않는다.
- `MtjBottomActionScaffold`: 오버레이가 아닌 흐름 레이아웃. safe drawing/IME inset은 한 번만 소유한다. navigationBars와 IME는 합산이 아니라 union 처리한다.
- 키보드 표시 중 탭을 숨긴다. 탭은 홈/사주/타로/기록/설정이며 Role.Tab 및 선택 상태를 제공한다.
- 테스트 태그: `mtj-content`, `mtj-actions`, `mtj-tabs`, `mtj-tab-0`부터 `mtj-tab-4`, `mtj-bottom-host`.

## 화면과 상태 계약

| 화면 | 구현/확인 범위와 유지할 계약 |
| --- | --- |
| 홈 | 홈과 타로의 질문 상태를 공유한다. 이동 중 원문을 잃지 않는다. |
| 사주 입력 | 날짜·시간 검증 모듈을 사용한다. 숫자 연속 입력 UX, 시간 모름, 음력·윤달, 날짜 선택기를 회귀 검사한다. |
| 사주 결과 | 연주/월주/일주/시주 순서. 시간 미상은 `시간 모름`과 `오행 산출 제외`로 표시하고 가짜 시주를 만들지 않는다. |
| 계산 기준 | 별도 `계산 기준` 버튼과 대화상자에 정보를 표시한다. 본문에 긴 원시 메타데이터를 노출하지 않는다. |
| 타로 선택 | 78장은 8열 10행, 마지막 6장 중앙 정렬. 선택·재탭 해제 및 필요한 장수 제한을 유지한다. |
| 타로 결과 | 완료 후 결과 진입 컨트롤 제공. 통합 두 행 요약은 위 최신 계약을 적용해야 한다. |
| 기록 | 목록/상세/빈 상태/오류와 삭제 확인을 구분한다. 저장된 질문·카드 스냅샷을 유지한다. |
| 설정 | 현재 System/Light/Dark, 동작 줄이기, 역방향 포함, 정보가 구현되어 있다. 시안의 추가 테마·덱 선택·법적 메뉴는 구현 완료로 보지 않는다. |

사주 검증 문구는 근거별로 `외부 기관 검증됨`, `내부 검증됨`, `내부 구조 검토됨`, `검토 필요`를 구분한다. 내부 검증을 외부 공인으로 격상하지 않는다.

계산 기준의 절입 시각은 OffsetDateTime 파싱 후 Asia/Seoul로 변환하여 `yyyy년 M월 d일 HH:mm:ss (한국 표준시, UTC+09:00)`로 표시한다. 파싱 실패 시 `절입 시각을 표시할 수 없습니다.`이며 원시 ISO 문자열로 되돌리지 않는다. 내부 원본 데이터는 유지한다.

## 반응형과 접근성

- 사주 기둥은 fontScale 1.3 이상에서 2열이다. 그 외에는 가용 폭과 측정 폭에 따라 4열 또는 2열이다. 폭 360dp 미만 또는 큰 글자에서 compact 모드, 최소 높이 104dp/일반 132dp이며 내용에 따라 늘어난다.
- 사주 페이지 자체는 스크롤 가능하다. 핵심 첫 화면 가독성과 페이지 전체 스크롤 금지를 혼동하지 않는다.
- 타로 카드의 현재 구현 종횡비는 0.62이며 시안의 2:3과 완전히 같지 않다.
- 78장을 한 화면에 배치하는 요구에는 개별 카드 터치 크기 제약이 있다. 일반 48dp 기준을 충족한다고 일괄 주장하지 말고 실제 기기에서 오선택·재선택·TalkBack 탐색을 검증한다.
- 큰 글자에서 타로 결과 개요는 순서형 fallback을 사용한다. 스프레드의 의미와 카드 순서는 보존한다.
- 설정 행은 Role.Switch와 상태를 제공하고 내부 Switch의 중복 낭독을 제거한다.
- 결과 열기는 스와이프뿐 아니라 접근 가능한 탭 동작으로도 가능해야 한다. 선택 완료가 자동 화면 이동을 일으키지 않도록 한다.
- 모든 본문·보조 텍스트·버튼·오류 상태를 두 테마에서 확인한다. 웹 시안 캡처를 Android 글자 배율 검증으로 대체하지 않는다.

## 디자인 산출물과 자산 정책

- 기본 시안: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/secondary-v2-20260911/`의 `SPEC.md`, `source.html`, 상태별 PNG 및 검증 기록.
- 설정 보드만 교체: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/settings-board-v3-20260911/board-settings.png`.
- 통합 색인: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-design/outputs/combined-design-packet-20260911.md`.
- 프리미엄 스프레드/상태 자료: `/Users/thomaslee/Documents/Codex/2026-06-09/pentagram-wolff-olins-collins-landor-mucho/outputs/ux-completion-20260911-0831/`.
- 시안은 360×800 CSS 기반이며 네이티브 캡처가 아니다. 목업 사주 날짜와 간지는 엔진 검산 자료가 아니다.
- 시안 색상, 글자 크기, 오행 타일과 실제 구현 토큰 사이에 차이가 있다. 실제 코드와 시안의 의도를 각각 확인한다.
- secondary 패키지는 `shippingAllowed=false`다. 디자인용 Short Hand Tarot JPEG 및 파생 보드를 그대로 앱 배포 자산으로 복사하지 않는다.
- 원본 자산 출처 자료: `/Volumes/뽀그리/Project 먕/사로먕/design-assets/The_Short_Hand_Tarot_20260909/source-manifest.json`. 사용자 허가 진술과 체결된 사용권 증빙은 구분한다.
- 앱 런타임은 `android/app/src/main/assets/mtj/tarot/`의 AVIF 78장 및 JSON 카탈로그를 사용한다. 디자인 JPEG와 런타임 자산의 권리 확인 여부를 혼동하지 말고 조정 담당이 배포 권리 증빙을 확정한다.
- 앱 아이콘은 리소스의 adaptive/monochrome launcher 구성, 사주/타로 탭 아이콘과 함께 실제 런처에서 확인한다.

## 검증 근거와 해시

아래는 기존 보고서의 결과이며 이 문서 작성 중 새로 실행한 테스트가 아니다.

| 대상 | SHA-256 |
| --- | --- |
| r2 APK | `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2` |
| 소스 스냅샷 | `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683` |
| 소스 매니페스트 | `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85` |
| secondary-v2 매니페스트 | `5ce50e79104294144b74adac096cca084bb289643b0cde689e195fc00c7b29a1` |
| 설정 v3 보드 | `18b63ed905e753de8c70eeb4081470c5ccf3573d6b86a3cd42c5065f4831eaf9` |
| 프리미엄 매니페스트 | `e1f6e64e7b56ed3243076fff255167cd33e8e10e11fe68cac9d99c93e633a878` |
| 스프레드 매핑 | `1cbf5a938eb542b7af78d850ed7506557e6f0a83cdea9726ecc369eed0c43034` |

저장소 `evidence/saju-metadata-r2/REPORT.md`, `qa-verdict.json`, `release-manifest.json`, `evidence-index.json`, `SHA256SUMS`를 함께 확인한다. 해당 보고서는 단위 테스트 94개, 실패/오류/건너뜀 0개와 지정 APK의 검증을 기록한다. APK 경로는 `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk`다.

Pixel 10 에뮬레이터 API 37, 1080×2424, density 420, fontScale 1에서 시간 유무/두 테마/계산 기준 대화상자 및 타로 0→2→3→재탭 2장 상태가 보고되었다. Pixel 9a의 동일 해시 다크 시간 미상/대화상자도 별도 통과 보고가 있다. 이 결과는 모든 접근성·배율 조합이나 최신 통합 패널 변경을 검증한 것은 아니다.

## 재현 명령

저장소 루트에서 실행한다. 아래 빌드 명령은 문서화한 재현 절차이며 이번 문서 작업에서는 실행하지 않았다.

```sh
git show b4ffe8718bf01b02930468451baa4ecab45f1db3:design/native/MtjTheme.kt
git show b4ffe8718bf01b02930468451baa4ecab45f1db3:android/app/src/main/java/com/hoscat/mtj/dev/SajuChartDisplay.kt
gradle -p android :app:testDebugUnitTest :app:assembleDebug --offline
shasum -a 256 releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
```

전제: JDK 17, SDK 36, AGP 8.10.1과 호환되는 Gradle, 설정된 Android SDK, `ANDROID_USER_HOME/debug.keystore`. offline은 의존성이 캐시되어 있어야 한다. 저장소에 없는 Gradle wrapper가 있다고 가정하지 않는다. 실제 기기 설치 APK 해시는 빌드 산출물과 별도로 대조한다.

## 우선순위와 첫날 점검

1. 최우선: 통합 타로 요약을 라이브/기록에 적용하고 위 8개 수용 검사를 증빙한다. 담당 Android, 디자인 검토, QA 회귀.
2. 최우선: 다크 글자 대비, 실제 입력 원문, 선택/재탭 및 저장 기록 일관성을 확인한다.
3. 후속: 1.3/2.0 글자 배율, 좁은 기기, TalkBack 및 78장 터치 제약의 네이티브 증빙을 보강한다.
4. 후속: 설정의 추가 테마/덱/법적 메뉴, 실제 시안과 토큰·타일 차이는 별도 범위 확정 후 구현한다.
5. 배포 전: 런타임 자산 권리, 아이콘, 릴리스 APK 해시와 검증 APK 해시를 확인한다.

첫날에는 기준 커밋과 변경 커밋을 구분하고, 위 파일 소유자를 확인하고, 보고서와 APK 해시를 맞춘다. 이어 라이트/다크 핵심 화면을 실행하고 통합 요약의 라이브/기록 비교, 시간 미상/계산 기준 표시, 카드 재탭 상태 보존을 확인한다. 실패 항목은 화면·입력·배율·APK 해시와 함께 Android/QA에 전달한다. 디자인용 이미지가 런타임 패키지로 유입되지 않았는지도 확인한다.

이 문서 작성 작업은 공유 Git 체크아웃을 변경하거나 커밋·푸시하지 않는다. 수정 구현 및 배포 완료 여부는 담당자의 코드/테스트/APK 증빙으로 별도 갱신한다.
