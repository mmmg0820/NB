# 샤로먕 릴리스 운영 및 팀 문서 통합 매뉴얼

작성일: 2026-09-11. 담당: 릴리스 운영(DevOps/데봅픽셀). 검토 기준은 GitHub 커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3`이다. 이 문서는 배포된 코드를 변경하지 않고 작성한 인수인계 초안이다. 아래 명령은 후임자의 실행 절차이며, 이번 문서 작성에서 빌드·기기 조작·커밋·푸시는 수행하지 않았다.

## 1. 목적과 범위

검증한 소스, 설치한 APK, 게시한 APK가 서로 다른 사고를 방지한다. 소스 동결, 기기 단독 사용, 독립 QA 판정, 단일 게시 담당자, 실패 이력 보존을 하나의 배포 절차로 연결한다. 팀별 Markdown을 합치는 절차도 12절에서 정의한다.

대상은 `com.hoscat.mtj.dev` Android 개발판이다. Play Store 출시, 결제·구독, 서버 운영, 사주 계산의 도메인 정확성 전체 인증은 이번 PASS에 포함되지 않는다.

## 2. 현재 게시 상태와 식별자

| 항목 | 기준값 |
| --- | --- |
| 저장소 | https://github.com/mmmg0820/NB |
| 브랜치 | `main` |
| 커밋 | `b4ffe8718bf01b02930468451baa4ecab45f1db3` |
| 주석 태그 | `v0.1-dev-20260911-r2` |
| 태그 객체 | `dd715b702af99becaffcba851162bf41974c58e8` — 커밋 SHA와 다르며 역참조 결과가 위 커밋이어야 함 |
| APK | `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk` |
| 패키지/버전 | `com.hoscat.mtj.dev`, versionName `0.1-dev`, versionCode `1` |
| 빌드 | debug, compileSdk/targetSdk 36, minSdk 31 |
| QA | 승인된 S1 수정 및 지정 회귀 범위 `FINAL_PASS` |
| 단위 테스트 | 94개, 실패/오류/건너뜀 각 0 |

현재 보장하는 동작: 사주 시간 입력/미상 결과를 라이트·다크에서 확인했다. 기본 결과에는 `대한민국 표준시`만 표시하며 `검토 필요` 배지와 연주→월주→일주→시주 순서, 시간 미상의 시주 자리 및 오행 산출 제외를 보존한다. 계산 시각은 명시적인 `계산 기준` 팝업에서 한국어 날짜와 시간대로 보여 준다. 타로는 78장 8열×10행, 3장 선택 시 결과 서랍, 같은 세 번째 위치 재탭 시 2장으로 감소하고 서랍이 닫히는 동작을 확인했다.

홈 질문 공유, 8개 모양/27개 리딩, 기록 저장 화면 등은 구현돼 있으나 R2 검수로 전체 경로가 새롭게 인증된 것은 아니다. R2 소스 변경은 `SajuChartDisplay.kt`와 `SajuChartDisplayTest.kt` 두 파일이다.

```text
APK/기기에서 추출한 base.apk SHA-256
78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2

source-snapshot.tar.gz SHA-256
1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683

source-manifest.sha256 파일 자체의 SHA-256
58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85

서명 인증서 SHA-256
c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab
```

## 3. 소스 소유권과 인계 경계

아래 경로는 저장소 루트 기준이다. 운영 담당자는 빌드 결과와 해시를 책임지며 기능 코드의 계산 의미를 임의로 바꾸지 않는다. 기능 팀이 패치와 근거를 제출하고 Android 통합 담당이 공유 파일 충돌을 해소한다.

| 영역/일차 책임 | 정확한 파일 또는 경로 | 운영에서 확인할 계약 |
| --- | --- | --- |
| Android 통합 | `android/build.gradle.kts`, `android/settings.gradle.kts`, `android/gradle.properties`, `android/app/build.gradle.kts`, `android/app/src/main/AndroidManifest.xml` | SDK/버전/서명, 외부 sourceSets, 패키지 |
| 앱 상태/통합 | `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt` | 공통 질문과 사주 입력 상태, 탭 연결 |
| 사주 UI | 같은 디렉터리의 `SajuChartDisplay.kt`; `android/app/src/test/java/com/hoscat/mtj/dev/SajuChartDisplayTest.kt` | 검토 배지, 주 순서, 미상 시간, 계산 기준 상세 |
| 사주 계산 팀 | `saju/src/MtjBirthInputAdapter.kt`, `saju/src/MtjSajuRuntime.kt`, `android/vendor/com/hoscat/core/` | 원본 입력 의미, 만세력 데이터 계약 |
| 타로 팀 | `android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt`, `TarotRecreationState.kt`, `TarotSelectionFocusReducer.kt`, `TarotSelectionGridContract.kt`, `TarotSpreadCategoryContract.kt`; `tarot/src/`; `android/vendor/com/softcat/mystictarot/` | 순서 있는 선택, 복원, 덱/스프레드 키 |
| 기록 팀 | `android/app/src/main/java/com/hoscat/mtj/dev/RecordStore.kt`, `RecordSnapshots.kt`, `RecordsScreen.kt`; `records/src/RecordsJsonCodec.kt`, `MigrationPlanner.kt` | DB/저장 포맷과 업그레이드 호환성 |
| 디자인 팀 | `design/native/MtjTheme.kt`, `MtjComponents.kt`, `MtjBottomActionScaffold.kt`; `android/app/src/main/res/`; `android/app/src/main/assets/` | 런타임 자산 허용 목록과 배포 권한 |
| QA | `evidence/saju-metadata-r2/` 및 독립 QA 보고서 | 동일 APK의 테스트/화면/로그와 승인 범위 |
| 릴리스 운영 | `README.md`, `.gitignore`, `releases/`, `evidence/` 게시 구성과 태그 | 검증본만 게시, 원격 재검증, 대체 관계 |

`android/vendor/`는 실제 컴파일 입력이다. 앱 디렉터리만 복사하면 빌드가 재현되지 않는다. `* 2.kt`는 Gradle과 Git에서 제외된 중복 파일이다. 스냅샷 168개 검사와 Git 파일 수를 무조건 같다고 판정하지 말고, 제외 사유와 컴파일 입력을 비교한다.

## 4. 데이터·상태·배포 기록 계약

- 홈과 타로는 `MainActivity.kt`의 `tarotQuestion`을 공유한다. 해당 상태와 사주 입력은 `rememberSaveable`을 사용한다. 모든 계산 결과가 프로세스 재생성 후 자동 복원된다고 단정하지 않는다. 실제 테마 변경 검수에서는 입력이 남은 상태에서 명식 계산을 다시 실행했다.
- 타로 선택은 순서가 있다. 화면 위치와 실제 카드 ID는 구별한다. 같은 좌표의 재탭 증거만으로 모든 셔플 상황의 카드 ID 계약을 인증하지 않는다.
- 시간 미상은 00:00으로 대체하지 않는다. 시주 미정/산출 제외 의미를 보존한다. `검토 필요`는 오류 없이 숨겨도 되는 장식 문구가 아니다.
- 기록은 `mtj-records-v1.db`, SQLite 버전 1, `records(identity TEXT PRIMARY KEY NOT NULL, envelope BLOB NOT NULL)`이다. 현재 `onUpgrade`는 파괴적 업그레이드 대신 오류를 내므로 DB 변경은 기록 팀의 마이그레이션 계획이 먼저 필요하다.
- 배포 레코드는 커밋 SHA, 태그, APK SHA, 소스 스냅샷 SHA, 소스 manifest SHA, 서명 인증서 SHA, 테스트 집계, QA 판정/범위, 이전 버전 및 대체 사유를 함께 가진다. 인증서 SHA와 APK SHA는 서로 다른 값이다.
- `evidence/saju-metadata-r2/release-manifest.json`의 `status=SOURCE_FREEZE`는 소스 동결 상태다. 독립 `FINAL_PASS`나 게시 완료 상태로 해석하지 않는다. QA 보고서와 원격 검증을 별도로 연결한다.

## 5. Git/GitHub 운영과 단일 게시 담당자

릴리스별로 게시 담당자 한 명을 기록한다. 앞으로의 기본 게시 담당은 DevOps다. Android 담당이 대신 게시하려면 먼저 명시적으로 인계받고, 인계 후 이전 담당자는 푸시하지 않는다. 실제 R1/R2는 통합 담당이 게시했고 운영이 원격을 독립 확인했다. 이 이력과 앞으로 적용할 단일 게시 규칙을 혼동하지 않는다.

팀별 작업은 격리된 브랜치/작업본에서 진행한다. 게시 담당자는 `git status`, `git diff --cached --name-only`, `git diff --cached --check`로 정확한 게시 대상을 검토한다. 광범위한 `git add .` 대신 검토된 경로를 지정한다. 동결 이후 소스 변경이 하나라도 생기면 새 후보로 만들고 QA를 다시 연결한다.

태그 규칙: 현재 형식은 `v<앱버전>-<YYYYMMDD>[-rN]`, 예: `v0.1-dev-20260911-r2`. APK 보관은 `releases/<YYYY-MM-DD>[-rN]/<제품>-<변경>-debug.apk`. 이름은 게시 전에 확정하며 이미 게시한 태그를 이동/덮어쓰지 않는다. Android versionCode 변경은 별도 통합 작업이며 태그 번호가 versionCode를 대신하지 않는다.

저장소의 `releases/` 폴더와 Git 태그가 존재한다고 GitHub Releases 페이지까지 생성된 것은 아니다. 이번 확인은 Git 트리·태그·APK 기준이다. 링크는 태그 트리 또는 커밋에 고정된 파일 링크를 사용한다.

읽기 전용 확인 예시(저장소 루트에서 실행):

```bash
git status --short --branch
git rev-parse HEAD
git ls-remote --heads --tags https://github.com/mmmg0820/NB.git
git rev-parse 'v0.1-dev-20260911-r2^{commit}'
git show b4ffe8718bf01b02930468451baa4ecab45f1db3:releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk | shasum -a 256
```

## 6. 빌드 재현과 서명

검증 환경은 Gradle 8.11.1, AGP 8.10.1, Kotlin 2.1.21, Java 타깃 17, Android SDK 36이다. 저장소에 Gradle wrapper가 없으므로 별도로 같은 Gradle을 준비해야 한다. 로컬 `/tmp/gradle-8.11.1-dist/gradle-8.11.1/bin/gradle`는 이번 호스트에서 확인한 도구 위치이며 영구 경로가 아니다.

동결 아카이브를 별도 작업 디렉터리에 추출하고 파일 manifest를 검사한다. 아카이브 경로는 `work/cell/...` 접두사를 포함한다. 기존 변경이 있는 공유 checkout 위에 추출하지 않는다.

```bash
release_stage=$(mktemp -d '/Volumes/MTJNativeBuild/release-check.XXXXXX')
tar -xzf '/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/source-snapshot.tar.gz' -C "$release_stage"
cd "$release_stage"
shasum -a 256 -c '/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/source-manifest.sha256'
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export ANDROID_HOME='/Users/thomaslee/Library/Android/sdk'
export ANDROID_USER_HOME='/Volumes/MTJNativeBuild/android-user'
export GRADLE_USER_HOME='/Volumes/MTJNativeBuild/gradle'
export TMPDIR='/Volumes/MTJNativeBuild/tmp/'
cd "$release_stage/work/cell/android"
/tmp/gradle-8.11.1-dist/gradle-8.11.1/bin/gradle --no-daemon clean :app:testDebugUnitTest :app:assembleDebug --offline
```

`--offline`은 의존성 캐시가 준비된 환경 전제다. 비어 있는 환경에서는 의존성 준비를 별도 기록한다. 새 빌드가 기존 APK와 바이트까지 같다고 보장하지 않는다. 새 APK 해시를 산출하고 그 APK를 검수한다. 위 명령은 이번 문서 작성에서 재실행하지 않았다.

`ANDROID_USER_HOME/debug.keystore`가 실제 서명 파일이다. 키스토어를 Git에 넣지 않고 관리 담당자가 보관한다. 서명 불일치로 업데이트 설치가 실패하면 앱 삭제로 우회하지 말고 기존 인증서를 확인한다.

```bash
shasum -a 256 releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
# apksigner는 설치된 Android SDK build-tools의 경로를 PATH에 설정한 뒤 실행
apksigner verify --verbose --print-certs releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
```

## 7. Pixel 9a 단독 사용과 설치 검증

사용자 지정 기기는 Pixel 9a다. 과거 Pixel 10 증거는 별도 통합 담당의 이력이며 Pixel 9a 운영 지시를 대체하지 않는다. serial `emulator-5554`는 이번 실행의 식별자일 뿐 영구 기기명이 아니다. AVD 이름을 매번 확인한다.

기기 registry는 `/Volumes/뽀그리/Project 먕/_operations/device-reservations/registry.json`, 잠금 파일은 `locks/<serial>/LEASE.json`이다. 사용 전 소유자·만료·serial·AVD·정확한 APK SHA·ADB 포트·증거 경로를 확인하고 단독 임대를 기록한다. 만료 시각이 지났어도 다른 담당자의 프로세스 종료 근거로 삼지 않는다. 이 인계 시점 Pixel 9a 임대는 `RELEASED_FINAL_PASS`이고, 다른 물리 기기 항목은 별도 담당 소유다.

아래는 임대 취득 후 실행할 예시다. APK 경로와 serial을 실제 임대와 대조한다.

```bash
release_adb='/Users/thomaslee/Library/Android/sdk/platform-tools/adb'
release_serial='emulator-5554'
release_apk='/Volumes/뽀그리/Project 먕/샤로먕/github/NB/releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk'
release_capture=$(mktemp -d '/Volumes/MTJNativeBuild/installed-check.XXXXXX')
"$release_adb" -s "$release_serial" emu avd name
"$release_adb" -s "$release_serial" install -r "$release_apk"
"$release_adb" -s "$release_serial" shell pm path com.hoscat.mtj.dev
# 위 명령의 package: 뒤 실제 경로를 release_base에 넣는다. split APK가 있으면 모두 별도 기록한다.
release_base='REPLACE_WITH_OBSERVED_BASE_APK_PATH'
"$release_adb" -s "$release_serial" pull "$release_base" "$release_capture/base.apk"
shasum -a 256 "$release_apk" "$release_capture/base.apk"
"$release_adb" -s "$release_serial" shell wm size
"$release_adb" -s "$release_serial" shell wm density
"$release_adb" -s "$release_serial" shell settings get system font_scale
"$release_adb" -s "$release_serial" shell cmd uimode night
"$release_adb" -s "$release_serial" shell dumpsys window
```

각 PNG/XML에 시각, 임대 ID, serial/AVD, 패키지·포커스, density, 해상도, fontScale, theme, APK SHA를 연결한다. 화면이 실제 결과인지 XML 내용과 이미지로 확인한다. 테마 변경 뒤 입력 화면으로 돌아갔다면 계산 버튼을 다시 누른 뒤 캡처한다. 파일명에 result가 있다는 이유로 결과 화면으로 인정하지 않는다.

로그는 시간 범위를 제한한 전체 버퍼와 앱 PID 로그를 함께 보관한다. QA가 검사한 R2에는 샤로먕 관련 엄격 오류가 0이지만, 앞선 시각의 Google Messaging/자막/Play Store ANR 3건에서 나온 관련 줄 6개가 있었다. 전역 로그 0으로 보고하지 않는다. 작업 종료 시 자신이 시작한 명령만 종료하고 임대를 해제한다. 공유 에뮬레이터, 다른 먕 앱, 다른 팀 데이터를 종료·초기화하지 않는다.

## 8. 자산 및 게시 제외 검사

`mtj-design/outputs/secondary-v2-20260911`은 `shippingAllowed=false`이다. 그 원본 JPEG, manifest 대상 파일, 거기서 파생된 보드 등을 게시 허용으로 간주하지 않는다. 디자인 참조 계약을 읽을 수 있다는 사실과 이미지 파일 배포 권한은 다르다.

검사는 (1) Git 경로, (2) 동결 소스 참조/파일, (3) APK ZIP 엔트리 및 추출 파일 해시, (4) 제외 manifest에 기재된 정확한 자산 해시를 대조한다. 확장자/파일명 검색만으로 이름 바뀐 파일을 검출할 수 없다. 현재 독립 QA는 정확한 제외 자산 해시 미포함을 확인했으며 파생물 전체와 저작권 출처를 인증한 것은 아니다.

```bash
git ls-tree -r --name-only HEAD | rg 'secondary-v2|\.jks$|\.keystore$|local\.properties$|(^|/)\._|\.DS_Store|\.env$'
rg -n 'secondary-v2|mtj-design/outputs' android design saju tarot records
unzip -Z1 releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
```

`rg` 종료 코드 1은 일치 없음이다. 오류 코드 2와 구분한다. 허용 목록에는 실제 런타임 카드 AVIF 78개 및 승인된 PNG 리소스를 명시하고 디자인 담당의 권리 문서를 연결한다. 키스토어/인증정보/사용자 데이터는 별도 제외한다. 현재 `.gitignore`는 키스토어 전체를 차단하지 않으므로 ignore만 신뢰하지 않는다. 추가 ignore 강화는 후속 변경으로 제안하며 이 문서 작성에서는 수정하지 않았다.

## 9. 실패 후보, 대체, 복구 절차

기존 `0257e9f0d72da5c6f1879c138383b8f41ee846ee` / `v0.1-dev-20260911` / APK `89d40ac05d74988c0d6e348b60199e91bd57b8e4800c3c6a81fc94d752ef2a7c`는 사주 기본 카드의 내부 시각 문구 노출로 `FINAL_FAIL/HOLD`였다. R2가 그 S1을 닫았다. 이전 판정을 PASS로 바꾸지 않는다.

새 문제가 발견되면 현재 배포물·QA 보고서·실패 이유를 고정하고 추천 다운로드 링크를 명확히 바꾼다. 이미 공개한 태그와 파일의 해시를 덮어쓰지 않는다. 알려진 정상 동작을 복원해야 하면 호환성 검토 후 후속 수정 커밋, 새 versionCode가 필요한 경우 새 앱 버전, 새 태그와 APK를 만든다. 이전 APK로 직접 다운그레이드는 서명·versionCode·기록 스키마 문제를 일으킬 수 있다. `adb uninstall`, `pm clear`, 강제 다운그레이드로 해결하지 않는다. 데이터 마이그레이션과 백업/복구 검증 없이 운영 데이터를 되돌리지 않는다.

## 10. 외장 볼륨 저장 배치

| 경로 | 용도/주의 |
| --- | --- |
| `/Volumes/뽀그리/Project 먕/샤로먕` | 사용자용 대표 경로. 현재 실제 `사로먕` 디렉터리를 가리키는 심볼릭 링크 |
| `.../repositories/NB` | 조정 담당 문서에서 지정한 팀 통합 작업본. 존재 확인됨; 현재 HEAD/변경은 사용하는 담당자가 다시 확인 |
| `.../github/NB` | 이번 운영 감사에서 확인한 깨끗한 R2 복제본, HEAD b4ffe871… |
| `.../github/NB-local-packaging-draft-superseded` | 동시 게시 때문에 사용하지 않은 운영 초안 보존본. 현재 정본으로 재게시하지 않음 |
| `.../evidence/nb-saju-metadata-r2-78277c5f-20260911` | Pixel 9a 운영 캡처/설치본/스냅샷/보고서 |
| `.../handoff/github-docs-20260911/<팀>/README.md` | 이번 팀별 인수인계 문서 스테이징 |
| `/Volumes/MTJNativeBuild` | 외장 디스크 이미지에 기반한 빌드용 볼륨. gradle, android-user, tmp와 격리 스테이징 사용 |
| `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/work/cell` | 당시 Android 통합 소스 위치. 후임자가 자동으로 최신 정본이라고 가정하지 않음 |

외장 복제본이 둘 이상 있어도 쓰기 담당자는 한 명이다. 어떤 checkout을 게시에 사용할지 작업 시작 때 기록한다. 로컬 이관은 복사→SHA 검증→경로 인계 후 삭제를 별도 수행한다. 이번 문서는 맥 내부 자료를 삭제하지 않는다. 외장 파일시스템이 생성하는 `._*`는 Git pack 인덱스로 오인되어 오류를 낼 수 있다. `COPYFILE_DISABLE=1`을 사용하는 새로운 격리 복제본에서 검증하고, 문제가 있는 작업본은 우선 보존한다. 광범위한 메타데이터 삭제를 자동 실행하지 않는다.

## 11. 게시 체크리스트 및 수용 기준

- [ ] 게시 담당자 한 명과 대상 checkout, 브랜치, 변경 범위를 확정했다.
- [ ] 소스 동결 아카이브/manifest/빌드 로그/테스트 XML/APK/서명 SHA가 연결된다.
- [ ] 깨끗한 빌드가 성공하고 변경에 필요한 회귀가 통과했다. 기존 94라는 숫자를 영구 고정 조건으로 쓰지 않고 변화 사유를 기록한다.
- [ ] 지정 Pixel 9a 임대와 설치본 해시를 확인했다. 새 APK에 이전 SHA의 화면 증거를 붙이지 않았다.
- [ ] 시간 유/무·라이트/다크 실제 결과, 배지/시주/순서/상세 문구, 타로 재탭 상태를 검수했다.
- [ ] 독립 QA가 정확한 SHA에 `FINAL_PASS`와 검수 범위를 명시했다.
- [ ] 제외 자산/키/개인정보 검사를 하고 정확한 자산 해시 비교의 범위를 밝혔다.
- [ ] 선택된 게시 파일과 문서의 APK 경로/해시/이전 판정이 일치한다.
- [ ] 새 커밋과 새 태그만 게시했다. GitHub Release 페이지 생성 여부는 별도 기록했다.
- [ ] 새 복제본에서 커밋/태그 역참조/게시 APK SHA를 다시 계산했다.
- [ ] 임대 해제, 실행 명령 종료, 외장 증거 경로와 인계 기록을 남겼다.

수용 기준은 위 항목의 실제 근거가 연결되는 것이다. 문서에 PASS 문자열이 있다는 사실만으로 통과 처리하지 않는다.

## 12. 전체 팀 문서 최종 통합 계획

이 절은 통합 담당에게 넘기는 실행 계획이다. 아직 다른 팀 문서 수집·공유 checkout 수정·게시를 수행하지 않았다. 제출 경로는 조정 담당의 할당표를 기준으로 받고, 아래의 제안 문서명을 기존 팀 디렉터리로 오해하지 않는다.

| 통합 주제 | 제출 책임 | 권장 최종 경로 |
| --- | --- | --- |
| 프로젝트 개요, 업무 흐름 | 조정 담당 | `docs/README.md`, `docs/coordination/README.md` |
| 빌드·통합·화면 진입 | Android 통합 | `docs/android/README.md` |
| 디자인 승인·브랜드 | 제품 디자인 | `docs/product-design/README.md` |
| 토큰·컴포넌트·반응형 | 디자인 시스템 | `docs/design-system/README.md` |
| 질문·카드 선택·상태 | TAR 배치 1 | `docs/tarot-flow/README.md` |
| 스프레드 키·배치 | TAR 배치 2 | `docs/tarot-spreads/README.md` |
| 카드 자산·접근성 | TAR 확장 | `docs/tarot-assets-accessibility/README.md` |
| 사주 입력·표시 | SAJ 배치 1 | `docs/saju-ui/README.md` |
| 사주 계산 경계 | SAJ 배치 2 | `docs/saju-contracts/README.md` |
| 기록·마이그레이션 | 확장 배치 1 | `docs/records/README.md` |
| 요구사항 추적 | 확장 배치 2 | `docs/traceability/README.md` |
| QA 기준·실패 이력 | QA | `docs/qa/README.md` |
| 릴리스·보관 | 릴리스 운영 | `docs/release-operations/README.md` — 본 문서 |

통합 순서:

1. 조정 담당이 제출 목록을 고정한다. 각 항목은 팀/담당, 스테이징 절대경로, 원본 SHA-256, 기준 코드 커밋, 제출 상태, 최종 경로, 미완료 사유를 가진다. 아직 없는 문서는 `MISSING`으로 남긴다.
2. 각 팀 문서에서 목적, 구현, 정확한 파일, 데이터 계약, 한계, 우선순위, 재현 명령, 수용 기준, 증거, 의존팀, 첫날 체크리스트를 확인한다. 소스에 없는 기능을 구현 완료로 적은 문장은 담당자에게 돌려보낸다.
3. 공통 식별자(커밋/태그/패키지/94 테스트/4종 SHA)를 기준표와 대조한다. 동일성 정보의 원본은 릴리스 manifest이고 승인 범위는 독립 QA 보고서다. 오래된 태그와 최신 태그를 한 표에서 섞지 않는다.
4. 공유 파일마다 최종 쓰기 담당자를 하나로 정리한다. 동결 소스와 다른 파일명, `android/vendor/` 누락, 시간 미상 의미, DB 업그레이드, 타로 순서 계약의 충돌은 해당 팀과 Android 통합 담당이 해결한다. 운영이 추측해 계약을 재작성하지 않는다.
5. 단일 게시 담당자가 최신 원격 HEAD에서 문서 전용 격리 브랜치를 만든다. 수집 문서를 권장 `docs/` 트리로 옮기고 상대 링크를 정리한다. 공개 문서의 로컬 경로는 내부 참조임을 표시하고 가능하면 커밋 고정 GitHub 증거 링크도 제공한다.
6. `docs/README.md`에 처음 읽을 순서, 영역별 링크, 담당표, 주요 미완료 목록을 만든다. 한 곳에 완료/미완료 상태를 집계하고 문서마다 같은 긴 설명을 중복하지 않는다.
7. 문서 인덱스 JSON에 입력 SHA와 통합본 SHA를 별도 저장한다. 링크 변경으로 두 해시가 달라질 수 있음을 명시한다. JSON 스키마 필드는 `schemaVersion`, `baselineCodeCommit`, `documents[{team,sourcePath,sourceSha256,targetPath,integratedSha256,status,owner}]`, `unresolved`를 권장한다. JSON 자신의 해시를 자기 안에 넣지 않는다.
8. 링크 존재, 경로 대소문자/공백, JSON 파싱, SHA 재계산, Markdown diff를 검토한다. 게이트된 이미지·개인정보·키 포함을 다시 확인한다. 새 문서에 누락 팀을 PASS로 표시하지 않는다.
9. 문서만 바뀌었음을 diff 경로로 입증한다. 실행 코드/빌드 설정이 바뀌지 않았다면 APK 재빌드와 기기 QA를 반복하지 않는다. 예시 명령을 실제로 실행한 테스트처럼 적지 않는다.
10. 조정 담당의 제출 완료 판정 후 지정 게시 담당자만 문서 커밋을 푸시한다. 기존 앱 태그를 옮기지 않는다. README에는 앱 기준 커밋과 문서 통합 커밋을 구분해 기록한다. 새 복제본에서 docs 인덱스와 링크를 확인하고 각 팀에 경로/커밋/해시를 인계한다.

통합 완료 기준: 모든 할당 문서가 제출·해시 검증됐거나 누락 이유와 책임자가 명시돼 있고, 충돌하는 계약이 해결되며, 독자가 `docs/README.md`에서 모든 문서에 도달할 수 있어야 한다. 실제 제출 상태와 별개로 전체 완료를 선언하지 않는다.

## 13. 우선순위별 후속 과제

| 우선순위 | 과제/담당 | 완료 기준 |
| --- | --- | --- |
| P1 | 단일 게시 담당 인계 양식/운영 | 두 담당 동시 게시가 없고 QA 승인 전에 업로드하지 않는 절차를 기록 |
| P1 | 키스토어·환경파일 ignore 강화/운영+Android | 이미 추적된 파일까지 별도 확인하며 키/토큰 미포함 검사 성공 |
| P1 | 카드 자산 권리 및 파생 이미지 출처/디자인 | 파일별 출처·권리·허용 범위 문서 확보; 정확한 해시 제외만으로 완료하지 않음 |
| P1 | 스토어용 서명·versionCode·release 파이프라인/Android+운영 | debug 키와 분리된 키 관리, release APK/AAB, 업데이트 설치와 QA 근거 확보 |
| P1 | 저장 포맷 변경 전 마이그레이션/기록 | 기존 v1 데이터 유지·복구를 검증한 변경 계획 |
| P2 | Gradle wrapper와 재현 환경 문서/Android | 새 환경에서 문서 명령으로 빌드 가능, 도구 버전/검증 해시 명시 |
| P2 | 기기별 전체 반응형·27 스프레드·TalkBack/QA+기능팀 | 정해진 매트릭스를 해당 후보 SHA로 검증, 미검수 셀 명시 |
| P2 | 계산 정확성·결제·서버·장기 안정성/해당 팀 | 기능 범위 확정 후 별도 수용 기준과 QA. 이번 debug PASS에서 추론하지 않음 |
| P2 | 오래된 임대 항목 정리/운영 | 소유자 확인을 거쳐 종료 이력으로 이전; 시각만 보고 기기 종료 금지 |

## 14. 근거와 새 담당자의 첫날

공개 기준: [R2 커밋](https://github.com/mmmg0820/NB/commit/b4ffe8718bf01b02930468451baa4ecab45f1db3), [R2 태그 트리](https://github.com/mmmg0820/NB/tree/v0.1-dev-20260911-r2), [고정 APK](https://github.com/mmmg0820/NB/blob/b4ffe8718bf01b02930468451baa4ecab45f1db3/releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk), [게시된 R2 증거](https://github.com/mmmg0820/NB/tree/b4ffe8718bf01b02930468451baa4ecab45f1db3/evidence/saju-metadata-r2).

내부 근거:

- Pixel 9a 운영 보고서: `/Volumes/뽀그리/Project 먕/샤로먕/evidence/nb-saju-metadata-r2-78277c5f-20260911/REPORT.md`; `manifest.json`, `builds/`, `screens/`, `ui/`, `logs/` 동반.
- 독립 최종 QA: `/Users/thomaslee/Documents/Codex/2026-06-11/3-qa-ai-qa-qa-qa/outputs/successor-78277c5f-qa-20260911/REPORT.md`; `audit-results.json`, `SHA256SUMS`, 원격 검증 추가 기록. 내부 경로이며 공개 저장소에 있다고 가정하지 않는다.
- 이전 실패 QA: `/Users/thomaslee/Documents/Codex/2026-06-11/3-qa-ai-qa-qa-qa/outputs/final-89d40ac0-device-qa-20260911/REPORT.md`.
- Android 원본 증거: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/outputs/evidence/sharomyang-saju-metadata-r2-20260911/`. 공개된 동일 바이트 근거는 위 R2 증거 링크로 확인한다.

첫날 체크리스트:

- [ ] 저장소 README와 본 문서의 기준 커밋·debug 승인 범위를 이해한다.
- [ ] 본인 작업본 경로와 쓰기 담당자를 확인한다. `repositories/NB`와 `github/NB`를 혼용하지 않는다.
- [ ] 태그 역참조, APK SHA, 인증서 SHA를 대조한다.
- [ ] 담당 기능의 파일/데이터 계약과 QA 실패 이력을 읽는다.
- [ ] 빌드 도구·캐시·서명 보관 위치를 확인하고 키를 복사해 커밋하지 않는다.
- [ ] 기기 테스트 전에 Pixel 9a 임대와 기존 데이터 보존 조건을 확인한다.
- [ ] 문서 통합 시 본인 문서의 경로·SHA·미완료를 제출 목록에 기록한다.
- [ ] 첫 수정은 격리 작업본에서 시작하고 검증된 동결본을 단일 게시 담당자에게 넘긴다.
