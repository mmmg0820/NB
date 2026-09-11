# 샤로먕 요구사항·소스·테스트·릴리스 추적 가이드

이 문서는 새 담당자가 샤로먕 Android 릴리스의 요구사항, 구현 소스, 테스트, APK, 기기 증거를 한 흐름으로 감사할 수 있도록 만든 추적성 문서다. 기준은 GitHub `mmmg0820/NB`의 커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3`이며, 이 문서는 공유 Git 체크아웃을 수정하거나 새 릴리스를 만드는 절차가 아니라 이미 게시된 R2 디버그 릴리스를 확인하고 다음 작업을 안전하게 시작하는 방법을 설명한다.

## 1. 기준 릴리스와 범위

| 항목 | 기준값 |
| --- | --- |
| 공개 정본 | `https://github.com/mmmg0820/NB` |
| 기준 브랜치 | `main` |
| 기준 커밋 | `b4ffe8718bf01b02930468451baa4ecab45f1db3` |
| 커밋 제목 | `Move Saju calculation metadata into localized details` |
| 커밋 시각 | `2026-09-11 09:25:24 +0900` |
| 기준 태그 | `v0.1-dev-20260911-r2` |
| 태그 대상 | `b4ffe8718bf01b02930468451baa4ecab45f1db3` |
| APK | `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk` |
| APK SHA-256 | `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2` |
| 패키지·버전 | `com.hoscat.mtj.dev`, `versionCode 1`, `versionName 0.1-dev` |
| 빌드 종류 | 디버그 APK |

2026-09-11 확인 시 원격 `main`은 위 커밋을 가리켰고, 주석 태그 `v0.1-dev-20260911-r2`도 같은 커밋으로 풀렸다. 다만 커밋과 태그에는 암호학적 Git 서명이 없다. 따라서 태그 이름만 신뢰하지 말고 커밋, APK, 소스 스냅샷, 서명 인증서, 증거 인덱스의 해시를 함께 확인해야 한다.

이 문서가 직접 추적하는 핵심 범위는 다음과 같다.

- S-01/S-02 사주 결과의 정보 위계, 시간 모름, 계산 기준과 검증 상태 표시
- D-01 승인 디자인에서 소스, 테스트, APK, 동일 SHA 기기 증거, GitHub 게시로 이어지는 연결
- R2가 대체한 이전 커밋·태그·APK 및 재사용하면 안 되는 역사적 증거
- 새 담당자가 기존 릴리스를 감사하고 다음 릴리스를 새 해시 체인으로 만드는 절차

스토어 출시, 프로덕션 서명, 외부 천문·역법 기관 자료와의 정확도 검증, 전체 접근성·반응형 행렬은 이 릴리스의 완료 범위가 아니다.

## 2. 현재 구현 동작

### 사주 입력과 계산

- 생년월일은 `YYYYMMDD`, 태어난 시간은 `HHmm` 한 필드로 받는다.
- `시간 모름`을 선택하면 시간 입력을 비활성화하고 제출 모델에서는 시각을 비워 둔다. `00:00`이나 `12:00`을 대신 넣지 않는다.
- 음력 입력은 실제 음력→양력 resolver가 없으면 승인하지 않는다.
- `MtjSajuRuntime`은 내장 음력·절기 자산을 지연 로드하고 mutex 안에서 계산한다. 내부 자산/JSON 예외는 사용자에게 그대로 노출하지 않는다.
- 계산 결과에는 원본 입력, 사주 네 기둥, 일간, 해석, `CalculationEvidence`가 포함된다. 계산 근거의 내부 식별자는 데이터 계약과 저장 스냅샷에 남지만 기본 사용자 화면에는 노출하지 않는다.

### 사주 결과

- 표시 순서는 `명식 → 일간 → 연주 → 월주 → 일주 → 시주 → 계산 기준`이다.
- 천간·지지는 한자와 한글 독음을 함께 표시하며, 색은 의미를 보조할 뿐 정보를 대신하지 않는다.
- 시각이 없으면 `시주` 제목은 유지하고 값 영역에는 `시간 모름`, `오행 산출 제외`를 표시한다. 시주나 시주 기반 값을 임의 생성하지 않는다.
- 미검증 결과는 사용자 언어인 `검토 필요`로 표시한다. `policyCode`, `dataVersion`, `GeneratedUnverified`, `myeongri_kr_v2`, 자산 버전 문자열 같은 내부 코드는 기본 결과 화면에 보이지 않는다.
- 기본 결과면에는 `대한민국 표준시`만 표시한다. `계산 기준`을 누르면 이전 절입명과 절입 시각을 `1995년 1월 6일 04:34:04 (한국 표준시, UTC+09:00)` 형식의 한국어 상세 팝업으로 보여 준다.
- 파싱할 수 없는 내부 시각 문자열은 원문을 노출하지 않고 `절입 시각을 표시할 수 없습니다.`로 대체한다.
- 폭이 좁거나 `fontScale >= 1.3`이면 네 기둥을 2열×2행으로 재배치한다. 3열+1열 배치는 허용하지 않는다.

### 기록

- `RecordSnapshots.saju()`는 입력과 계산 결과를 별도 프로필/사주 envelope로 저장한다.
- 저장 데이터에는 재현과 감사를 위한 `CalculationEvidence`가 포함될 수 있다.
- 기록 상세 화면은 저장된 입력 시각, 명식, 일간, 계산 기준, 검증 상태를 사람이 읽는 문구로 변환한다. 시간 없음은 `시간 모름`, 미검증은 `검토 필요`다.
- 내부 데이터를 저장하는 것과 내부 코드를 사용자에게 표시하는 것은 별도 계약이다. 저장 스키마의 필드를 삭제해 화면 노출 문제를 해결하지 않는다.

## 3. 소스 소유권과 주요 파일

경로는 모두 기준 커밋의 저장소 상대 경로다. SHA-256은 R2 소스 동결 manifest와 커밋 객체에서 대조했다.

| 영역·일차 소유자 | 파일 | 역할 | 파일 SHA-256 |
| --- | --- | --- | --- |
| Android 통합·사주 UI | `android/app/src/main/java/com/hoscat/mtj/dev/SajuChartDisplay.kt` | 결과 위계, 사용자용 검증 문구, 반응형 4열/2×2 배치, 계산 기준 팝업 | `eb00dfddaaeb7613db693e56bff024ebc7b8db802db4772517bba956878651f0` |
| Android 통합·사주 UI | `android/app/src/test/java/com/hoscat/mtj/dev/SajuChartDisplayTest.kt` | 결과 순서, 시간 모름, 배치, 내부 코드 비노출, 시각 현지화 단위 테스트 | `d8db7574c6c061295e3d30656c2f3261c75e2a28d22423fa6a3a923052945f14` |
| Android 통합 | `android/app/src/main/java/com/hoscat/mtj/dev/MainActivity.kt` | 입력 상태, 계산 호출, 결과/편집/저장 전환 | `0ec4245eca7fa88d3f255417635f0b525371406224bd4233b676b410aaa2cf7e` |
| 기록·표시 경계 | `android/app/src/main/java/com/hoscat/mtj/dev/RecordSnapshots.kt` | 입력과 계산 결과의 저장 스냅샷 생성 | `365290baf4940762e35264e12114c7dad5d7f90ac6299c455579e9178cb2f5e9` |
| 기록·표시 경계 | `android/app/src/main/java/com/hoscat/mtj/dev/RecordDetailRows.kt` | 저장 원문을 사용자용 한국어 행으로 변환 | `8525dc59de94ddc266fc9cd46a03b12752d5b66d86852a691fc96d0b8f51aae8` |
| 기록 테스트 | `android/app/src/test/java/com/hoscat/mtj/dev/RecordDetailRowsTest.kt` | 시간 모름, 명식, 검증 상태와 내부 코드 비노출 확인 | `a6e2dca58253be1f81b0db94d8a62fcb862f6fc56fdd817aa2f517624f3ac482` |
| 사주 입력 계약 | `saju/src/MtjBirthInputAdapter.kt` | 공용 입력 파서 연결, 음력 resolver 필수화 | `a036b8b087dd3dba97c8c686358442440ec86f4cc50e5be1c9e6d13c1119626f` |
| 사주 런타임 | `saju/src/MtjSajuRuntime.kt` | 자산 로딩, 입력 변환, 계산 직렬화, 사용자 오류 경계 | `597f8f9b29d84de7af0cfdaada42be7a018541f059805c4839d89a47de388a38` |
| 사주 데이터 계약 | `android/vendor/com/hoscat/core/model/ManseModels.kt` | `CalculationPolicy`, `DataTrustLevel`, `CalculationEvidence`, `SajuChart` | `59987af79092f31436d8f799edad82e9ad4b2efe43cac817974fe17a692122e1` |
| 사주 계산 | `android/vendor/com/hoscat/core/manse/SajuCalculator.kt` | 연·월·일·시주 계산과 증거 생성 | `2a6848a4d50885f3f87c6652af91b09213dc4e79a173a25007def08b378ab9e5` |
| 역법 자산 | `android/app/src/main/assets/manse/lunar_dates_1900_2100.json.gz.bin` | 음력·일주 데이터 | `6a28b4c8b8a478a04eca3a722f331c6188389146842a454546c6da6449b3f090` |
| 절기 자산 | `android/app/src/main/assets/manse/solar_terms_1900_2100.json.gz.bin` | 절기 시각 데이터 | `f9ef0af0ba157eeb823656e2402ade27b854add4cbda4d57d1eb0fb3670eadce` |

공유 Kotlin 파일의 최종 병합과 APK 생성은 Android 통합 담당이 소유한다. 사주 UI 담당은 위계·표시·큰 글씨 계약을, 사주 계산 계약 담당은 입력 의미·시간 모름·계산 경계를, 기록 담당은 저장·조회·마이그레이션을 검토한다. 추적성 담당은 어느 한 팀의 완료 문장 대신 커밋과 해시가 실제로 연결되는지 확인한다.

R2 커밋이 부모 커밋 `0257e9f0d72da5c6f1879c138383b8f41ee846ee`에서 바꾼 애플리케이션 소스는 `SajuChartDisplay.kt`와 `SajuChartDisplayTest.kt` 두 파일뿐이다. 나머지 현재 동작은 부모 릴리스에서 이어받았으며 R2 전체 회귀로 재확인됐다.

## 4. 데이터와 상태 계약

### 입력 상태

`BirthInputDraft → MtjBirthInputAdapter → BirthProfileInput → MtjSajuRuntime → SajuChart` 순서로 흐른다.

- `calendarType`, 윤달 여부, 이름, 성별, 지역/시간대, 생년월일을 보존한다.
- 시간 모름은 `hour == null`인 의미 상태다. 빈 값을 임의 시각으로 정규화하지 않는다.
- 음력은 resolver가 실제로 연결된 경우에만 계산 단계로 진행한다.
- 입력 오류와 런타임 오류는 사용자 수정이나 재시도가 가능한 한국어 문구로 변환한다.

### 계산 증거와 표시 상태

`CalculationEvidence`에는 다음 범주의 내부 감사 데이터가 들어간다.

- 시간대, 연주·월주·일주·시주 경계 정책
- 내부 policy code와 자산 data version
- 이전 절입명과 KST ISO 시각
- 입력 달력 종류, 정규화된 양력 날짜, 계산 시각
- 지역 보정 여부와 방식
- `GeneratedUnverified`, `InternalStructureChecked`, `ExternalAuthorityVerified` 신뢰 수준

표시 계층은 이를 그대로 출력하지 않는다.

| 내부 상태 | 사용자 표시 |
| --- | --- |
| `isVerified && ExternalAuthorityVerified` | `외부 기관 검증됨` |
| `isVerified` | `내부 검증됨` |
| `InternalStructureChecked` | `내부 구조 검토됨` |
| 그 밖의 미검증 상태 | `검토 필요` |

`ExternalAuthorityVerified`는 실제 외부 권위 자료와 비교한 근거가 있을 때만 사용할 수 있다. 현재 내장 자산의 생성·구조 테스트를 외부 기관 검증으로 승격하면 안 된다.

### 저장과 재현

- 저장된 기록은 원래 입력과 당시 계산 결과를 함께 가진다.
- 이후 계산 엔진이나 데이터가 바뀌어도 과거 기록을 현재 계산값으로 몰래 덮어쓰지 않는다.
- 내부 추적 필드는 삭제하지 않되 화면에서는 현지화된 계산 기준과 실제 검증 상태만 보여 준다.
- 새 스키마를 도입할 때는 이전 snapshot을 읽는 migration과 새 snapshot을 다시 열었을 때의 동일성 테스트를 함께 추가한다.

## 5. 요구사항 추적표

| 요구사항·결정 | 구현 파일 | 자동 테스트 | 기기·증거 | R2 판정 |
| --- | --- | --- | --- | --- |
| 결과 순서 `명식→일간→연→월→일→시` | `SajuChartDisplay.kt` | `chartOrderMatchesYearMonthDayHourContract` | `saju-known-*.png/xml`, `saju-unknown-*.png/xml` | PASS |
| 시간 모름을 명시하고 시주 미산출 | `MainActivity.kt`, `SajuChartDisplay.kt`, `SajuCalculator.kt` | `unknownHourIsExplicitAndExcludedFromElementCalculation`, 입력 시간 테스트 | `saju-unknown-light/dark.png/xml` | PASS |
| 알려진 시간의 시주 표시 | 같은 파일 | 사주 표시·입력 단위 테스트 | `saju-known-light/dark.png/xml`, `saju-known-input-light/dark.png/xml` | PASS |
| 미검증 결과를 `검토 필요`로 표시 | `SajuChartDisplay.kt`, `RecordDetailRows.kt` | `generatedUnverifiedAuthorityUsesUserFacingLanguage`, `sajuRowsExposeSavedSnapshotIdentity` | 모든 Saju 결과 XML | PASS |
| 내부 코드 기본 화면 비노출 | `SajuChartDisplay.kt`, `RecordDetailRows.kt` | `policyCode`, data version, ISO 원문 비노출 assertion | 결과 XML의 text 목록과 `evidence-index.json` | PASS |
| 계산 시각을 한국어 상세로 분리 | `SajuChartDisplay.kt` | `calculationDetailsFormatTimestampWithoutChangingEvidence`, `invalidCalculationTimestampNeverLeaksRawValue` | `saju-calculation-details-light/dark.png/xml` | PASS |
| 좁은 폭·큰 글씨에서 2×2, 3+1 금지 | `SajuChartDisplay.kt` | `pillarGridNeverUsesThreePlusOneLayout`, `pillarGridUsesCompactModeForNarrowOrLargeText` | R2 기기 실행은 1080×2424/fontScale 1.0만 수행 | 단위 PASS, 확장 기기 행렬 미완료 |
| 기록에 원래 입력·계산 결과 보존 | `RecordSnapshots.kt`, `RecordDetailRows.kt` | `RecordDetailRowsTest`, records 계약 테스트 | R2 Saju 캡처 범위에는 기록 재열기 없음 | 단위 PASS, 기기 회귀 필요 |
| APK와 설치본 동일 SHA | 릴리스·설치 절차 | 해당 없음 | `runtime-metadata.txt`, `evidence-index.json` | PASS |
| 앱 크래시·ANR 점검 | 앱 전체 | 94개 단위 테스트 | `android-test-window.log` | 앱 FATAL/ANR 없음; 테스트 전 시스템 앱 ANR 3건 존재 |

## 6. 소스→APK→증거→GitHub 해시 체인

R2 감사 시 다음 체인을 한 묶음으로 확인한다.

| 단계 | 경로 또는 식별자 | SHA-256/값 |
| --- | --- | --- |
| Git 커밋 | `b4ffe8718bf01b02930468451baa4ecab45f1db3` | Git object ID |
| Git tree | 커밋 tree | `383456799356b93452a59b7e7e281beb7a78f9d1` |
| 소스 스냅샷 | `evidence/saju-metadata-r2/source-snapshot.tar.gz` | `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683` |
| 소스 manifest | `evidence/saju-metadata-r2/source-manifest.sha256` | `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85` |
| 배포 APK | `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk` | `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2` |
| 증거 복사 APK | `evidence/saju-metadata-r2/sharomyang-saju-metadata-r2-debug.apk` | 같은 `78277c5f…` |
| 설치된 base APK | Pixel 10 `com.hoscat.mtj.dev` | 같은 `78277c5f…` |
| 서명 인증서 | Android Debug, RSA 2048, APK Signature Scheme v2 | `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab` |
| 증거 인덱스 | `evidence/saju-metadata-r2/evidence-index.json` | 각 PNG/XML에 APK SHA 포함 |
| 증거 전체 목록 | `evidence/saju-metadata-r2/SHA256SUMS` | 개별 파일 해시 목록 |

소스 스냅샷 manifest는 경로 앞에 역사적 작업 루트인 `work/cell/`을 붙인다. GitHub 저장소에서 대응 파일을 찾을 때는 이 접두사를 제거해 `android/`, `saju/`, `tarot/`, `records/`, `design/` 아래와 대조한다.

중요: 기준 커밋의 루트 `evidence/release-manifest.json`은 여전히 R1 APK `89d40ac0…`을 가리킨다. R2 감사의 정본 manifest는 반드시 `evidence/saju-metadata-r2/release-manifest.json`을 사용한다. 이 중복은 현재 가장 큰 문서 추적 위험이다.

## 7. 결정 이력과 대체 관계

1. 최초 통합 릴리스는 커밋 `0257e9f0d72da5c6f1879c138383b8f41ee846ee`, 태그 `v0.1-dev-20260911`, APK SHA `89d40ac05d74988c0d6e348b60199e91bd57b8e4800c3c6a81fc94d752ef2a7c`다.
2. 최초 릴리스의 사주 결과에 절입명, ISO 시각 등 기술 메타데이터가 기본 화면에 직접 노출되는 문제가 발견됐다.
3. 디자인 결정은 내부 엔진·자산 코드는 내부 추적으로만 유지하고, 사용자 화면에는 의미 있는 계산 기준과 실제 검증 상태만 표시하는 것이다. 시간 없음은 빈칸이 아니라 `시간 모름`으로 통일했다.
4. R2 커밋은 기본 화면을 `대한민국 표준시`로 단순화하고, 절입명과 현지화된 시각을 `계산 기준` 팝업으로 이동했다. 잘못된 시각 원문을 숨기는 테스트 두 건이 추가돼 전체 단위 테스트가 92건에서 94건으로 늘었다.
5. R2 커밋은 태그 `v0.1-dev-20260911-r2`와 APK SHA `78277c5f…`로 게시됐고, 원격 `main`도 R2를 가리킨다.

다음 항목은 삭제하지 말고 역사적 증거로만 취급한다.

- `v0.1-dev-20260911`, APK `89d40ac0…`: 기술 메타데이터 기본 노출로 R2가 대체함
- R1 source snapshot `61829673c1b1b13a2711f30ff18e66caf4ac1333acd3cb01f21c7f458e48c967`
- R1 source manifest `c9ef839d3e082f98d5df0a7d44e650f6997ae4848310c1097d037589e5830485`
- 이전 fedef·contract-only PASS: 현재 Android APK 완료 증거가 아니라 설계·계약 이력
- 외부 `nb-release-df7d6d90-20260911`: 홈 light/dark 중심 증거이며 Saju R2의 동일 SHA 검증 묶음이 아님
- `/Volumes/뽀그리/Project 먕/사로먕/.../S-02-D-01-reference-and-trace-support-20260911`: R2 게시 전 진행 상태와 디자인 결정 이력. 그 문서의 `PENDING_EVIDENCE` 상태는 현재 GitHub R2 증거가 대체함

승인 디자인의 역사적 기준은 다음 세 파일이다.

- `sharomyang-design-system-v1-20260911.md`, SHA-256 `73eb1527f523589769ece27aa292297123031d434dbc2414e37c54b1ea354958`
- `sharomyang-design-system-v1-20260911.json`, SHA-256 `f3c95c268e9034d7217d808e82141bac9aee6f2ce3555b1707d41ea51054ba8e`
- `sharomyang-saju-screens-preview-20260911.png`, SHA-256 `fdec22512501b2d275f9a52bf4719d249e7d94b561544717e8c6a9660435c2f5`

이 파일들은 `/Volumes/뽀그리/Project 먕/사로먕/handoff/from-premium-designer/sharomyang-rollout-focus-r2-20260911/`에 보관된 이전 staging 자료다. GitHub 커밋과 태그가 공개 소스 정본이며, staging 폴더 자체를 빌드 정본으로 사용하지 않는다.

## 8. 빌드와 테스트 재현

### 읽기 전용 릴리스 감사

```bash
git clone https://github.com/mmmg0820/NB.git NB-audit
cd NB-audit
git fetch --tags origin
git checkout --detach b4ffe8718bf01b02930468451baa4ecab45f1db3
git rev-parse HEAD
git rev-parse 'v0.1-dev-20260911-r2^{}'
shasum -a 256 releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
shasum -a 256 evidence/saju-metadata-r2/source-snapshot.tar.gz
shasum -a 256 evidence/saju-metadata-r2/source-manifest.sha256
(cd evidence/saju-metadata-r2 && shasum -a 256 -c SHA256SUMS)
```

기대값은 각각 커밋·태그 `b4ffe871…`, APK `78277c5f…`, source snapshot `1e0df154…`, source manifest `58d5599c…`이다. `git verify-tag v0.1-dev-20260911-r2`는 서명이 없으므로 성공하지 않는다. 이것을 파일 변조로 오해하지 말고, 해시 체인을 별도로 검증한다.

### Android 빌드와 단위 테스트

저장소에는 Gradle wrapper가 없다. JDK 17, Android SDK, Gradle 8.11.1 호환 환경과 별도 Android user home의 debug keystore가 필요하다.

```bash
cd NB-audit/android
JAVA_HOME=/absolute/path/to/jdk-17 \
ANDROID_SDK_ROOT=/absolute/path/to/android-sdk \
ANDROID_USER_HOME=/absolute/path/to/android-user-home \
gradle --offline --no-daemon clean :app:assembleDebug :app:testDebugUnitTest
```

R2 보존 로그의 결과는 42개 Gradle task 성공, 단위 테스트 94건, 실패·오류·건너뜀 0건이다. 새 환경에서 만든 debug APK는 도구 버전, 빌드 메타데이터, 키스토어가 다르면 SHA가 달라질 수 있다. 새 APK를 기존 `78277c5f…`의 증거에 연결하지 말고 새 source snapshot, manifest, certificate, evidence index를 만든다.

### 기기 회귀 재현

R2 보존 실행 환경은 Pixel 10 AVD, `emulator-5556`, API 37, 1080×2424, density 420, fontScale 1.0이다.

```bash
adb -s emulator-5556 install -r releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
adb -s emulator-5556 shell am start -n com.hoscat.mtj.dev/.MainActivity
python3 evidence/saju-metadata-r2/qa_pixel10.py
python3 evidence/saju-metadata-r2/index_evidence.py
```

`qa_pixel10.py`에는 원 실행기의 `adb` 절대경로와 `emulator-5556`가 고정돼 있다. 새 담당자는 폐기 가능한 작업 복사본에서 경로와 serial을 현재 환경에 맞춘 후 실행한다. 이 스크립트는 같은 폴더의 PNG/XML, verdict, index, SHA 목록을 다시 쓰므로 게시된 증거 디렉터리에서 직접 실행하지 않는다.

설치본 동일성은 다음 순서로 확인한다.

```bash
adb -s emulator-5556 shell pm path com.hoscat.mtj.dev
# 위 명령이 반환한 base.apk 경로를 사용한다.
adb -s emulator-5556 pull /data/app/.../com.hoscat.mtj.dev.../base.apk installed-base.apk
shasum -a 256 installed-base.apk releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
apksigner verify --verbose --print-certs releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk
```

## 9. 수용 기준

릴리스 감사 PASS에는 아래 항목이 모두 필요하다.

- 원격 `main`, R2 태그의 peeled commit, 로컬 HEAD가 같은 커밋을 가리킨다.
- 릴리스 APK와 증거 복사 APK, 설치된 base APK의 SHA가 `78277c5f…`로 일치한다.
- source snapshot과 source manifest SHA가 R2 manifest와 일치한다.
- 전체 단위 테스트가 94/94로 통과한다.
- 알려진 시간과 시간 모름 결과가 light/dark에서 각각 존재하고, 명식 위계와 시주 의미가 맞다.
- 기본 결과 화면에는 `대한민국 표준시`와 실제 검증 상태만 보이고 내부 code/data version/ISO 시각 원문은 없다.
- 계산 기준 팝업은 절입명과 한국어 KST 시각을 보여 주며 닫을 수 있다.
- 각 PNG/XML의 evidence index가 같은 APK SHA와 같은 기기 설정을 가리킨다.
- 앱 프로세스의 crash, FATAL EXCEPTION, ANR이 없다. 시스템 앱의 무관한 오류는 시간대와 package를 분리해 기록한다.
- 범위를 넘는 검증을 PASS로 확대 해석하지 않는다.

새 릴리스는 기존 SHA를 재사용하는 것이 아니라 위 항목을 새 커밋·소스 스냅샷·APK·기기 증거의 새 해시로 반복해야 한다.

## 10. 증거 위치와 주요 해시

GitHub 기준 경로는 `evidence/saju-metadata-r2/`다.

| 증거 | SHA-256 또는 내용 |
| --- | --- |
| `REPORT.md` | `341e2da52f960d138f8efb0b5ba3d81e5a66f3361a551e36fc1532f473c33ebd` |
| `build-final.log` | `3c4439067262ad772f4cb1b45eae93ba7f832bb00a6c08e46a807d9f44205b6d` |
| `qa-verdict.json` | `2fd15dcc373a9ad38fd4befe88e09d7fef5d34019a0d04a51fafa0df8d06c4d0`, status PASS |
| `runtime-metadata.txt` | `83bf5968e20677309264e90461f4e7ee908d157492b6c4fcff7cdcc5a3275d27` |
| `SajuChartDisplayTest` 결과 XML | `c0d75607998b15887f8928c1d0d2792a53699c71e2bd0b3ff1ffd30a71d1a534`, 10/10 PASS |
| 알려진 시간 light PNG | `cf3c76db16323c7cabb5c51ee53091544620ef5834a15b084d451d138a677abd` |
| 알려진 시간 dark PNG | `a30c1b169111849c0ed63952bbf34c9060df45af9a40668cac8ccf2e134e5b9d` |
| 시간 모름 light PNG | `5118e9f7a23a6f075938872f8abd06e56881e2339b7747f06f783b2a91bf047c` |
| 시간 모름 dark PNG | `0d047cff7b023ec678cd48b0e9869af12a3f7f1d4e98f5c59233cda5e7d1f725` |
| 계산 기준 light PNG | `0e0a29a2f7069b13c5547ccd66f6bc767b1b2ceec26c51740c06a1dcc3854b9a` |
| 계산 기준 dark PNG | `19a9dd94de76980ab5579771664b5416fc4e9bfee9087269c0da1f960d9e4626` |

전체 파일은 `SHA256SUMS`와 `evidence-index.json`으로 감사한다. `qa-run.log`와 `qa-dark-run.log`에는 UI hierarchy 지연, stale checkbox label, 계산 준비 중 assertion 같은 실패한 자동화 시도가 남아 있다. 최종 성공 로그만 보지 말고 이 기록도 읽어 테스트 도구 실패와 앱 실패를 구분한다.

## 11. 알려진 제한과 우선 후속 작업

### P0: 릴리스 식별과 신뢰

- 루트 `evidence/release-manifest.json`을 최신 릴리스 포인터 구조로 바꾸거나 `current-release.json`을 추가해 R1/R2 혼동을 제거한다.
- 프로덕션 keystore, release build type, Play/App Store 배포 절차를 별도 승인·보안 경계에서 구축한다. 현재 APK는 Android Debug 인증서다.
- 실제 외부 권위 역법 fixture와 도메인 전문가 검토를 추가하기 전 `ExternalAuthorityVerified`를 사용하지 않는다.
- 새 태그에는 조직 정책에 맞는 서명 또는 공급망 attestation을 적용한다.

### P1: 품질 행렬 확대

- 320/360/411/600dp, fontScale 1/1.5/2, light/dark의 Saju 입력·결과·계산 팝업을 실제 캡처한다.
- 큰 글씨에서 2×2 기둥의 제목·한자·독음·오행·시간 모름이 잘리거나 겹치지 않는지 bounds 기반으로 검증한다.
- TalkBack 읽기 순서, `계산 기준` 버튼/대화상자 포커스, 닫기 후 포커스 복귀, 회전 후 상태 보존을 기기에서 검증한다.
- 기록 저장 후 앱 재시작과 기록 상세 재열기를 기기 회귀에 넣어 입력 의미와 검증 상태가 보존되는지 확인한다.
- `qa_pixel10.py`의 adb 경로·serial·fixture를 인자로 받고, UI 준비 조건을 명시적으로 기다리도록 개선한다.

### P2: 유지보수 자동화

- Gradle wrapper를 저장소에 추가해 도구 버전을 고정한다.
- CI에서 단위 테스트 수, APK SHA, source manifest, 금지된 사용자 노출 문자열, evidence index 구조를 자동 검증한다.
- SBOM, 자산 출처·라이선스 manifest, 재현 빌드 조건을 릴리스 묶음에 추가한다.
- 릴리스 manifest schema를 버전화하고 `supersedes`, commit, tag, APK, source snapshot, certificate, QA matrix를 한 파일에서 연결한다.

## 12. 의존성과 인계 메모

- Android 통합 담당: 공유 Compose 파일의 최종 수정, 전체 빌드, 소스 동결, APK 생성 담당
- 사주 UI 담당: 결과 위계, 시간 모름 표현, 2×2 반응형, TalkBack/큰 글씨 수용 기준 담당
- 사주 계산 계약 담당: 입력 달력·시각 의미, 절기·경계 정책, 신뢰 수준과 외부 권위 검증 담당
- 기록 담당: snapshot 보존, migration, 상세 화면의 현지화 경계 담당
- QA: 같은 APK SHA를 설치한 실제 기기/AVD에서 캡처·semantics·logcat·접근성 판정 담당
- DevOps: GitHub 병합, 태그, 릴리스 파일, 원격 재감사 담당
- 추적성 담당: 디자인/요구사항→소스→테스트→증거→GitHub 연결과 대체 관계 유지 담당

기능 팀은 내부 코드 삭제나 표시 문구 변경만으로 계산 정확도 문제를 해결했다고 판단하지 않는다. UI 표시, 저장 계약, 엔진 신뢰 수준, 외부 검증은 서로 다른 책임 경계다. 어떤 팀이 새 APK를 만들면 QA는 이전 PNG/XML을 재사용하지 않고 새 APK SHA에 묶인 증거를 만든다.

## 13. 새 담당자의 첫날 체크리스트

- [ ] GitHub `main`과 `v0.1-dev-20260911-r2^{}`가 `b4ffe871…`인지 확인한다.
- [ ] 루트 README와 `evidence/saju-metadata-r2/REPORT.md`를 읽는다.
- [ ] R2 APK, source snapshot, source manifest, certificate SHA를 다시 계산한다.
- [ ] `SHA256SUMS` 전체를 검증하고 실패 파일이 있으면 작업을 중단한다.
- [ ] 루트 release manifest가 R1을 가리킨다는 예외를 확인하고 R2 manifest를 선택한다.
- [ ] `SajuChartDisplay.kt`, `SajuChartDisplayTest.kt`, `ManseModels.kt`, `SajuCalculator.kt`의 표시/데이터 경계를 읽는다.
- [ ] 시간 모름이 null 의미 상태이며 대체 시각을 생성하지 않는지 확인한다.
- [ ] 내부 policy/data version이 저장될 수 있으나 사용자 화면에 직접 나오지 않는지 확인한다.
- [ ] JDK 17, Android SDK, Gradle 8.11.1 호환 환경과 별도 debug keystore를 준비한다.
- [ ] 클린 빌드와 94개 단위 테스트를 재실행한다.
- [ ] 기기 테스트는 폐기 가능한 증거 복사본에서 실행하고 새 APK에 옛 증거를 연결하지 않는다.
- [ ] 알려진 시간/시간 모름, light/dark, 계산 기준 팝업을 직접 재현한다.
- [ ] 다음 변경의 소유자, 수용 기준, 새 commit/tag/APK/source/evidence 해시를 작업 시작 전에 기록한다.
- [ ] 프로덕션 배포라고 부르기 전에 release 서명, 전체 반응형·접근성 행렬, 외부 역법 검증 범위를 별도로 닫는다.

이 체크리스트를 마쳤을 때 새 담당자는 R2를 재현 가능한 기준선으로 사용할 수 있다. 하나라도 확인할 수 없는 항목이 있으면 이전 PASS를 승계하지 말고 정확한 누락 증거와 담당자를 기록한다.
