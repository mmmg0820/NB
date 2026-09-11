# 사주 입력/계산 엔진 계약 핸드오프

작성일: 2026-09-11 KST  
대상 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3` (`Move Saju calculation metadata into localized details`)  
대상 제품: 샤로먕 Android / 패키지 `com.hoscat.mtj.dev`

## 목적과 범위

이 문서는 샤로먕 사주 화면에서 입력값이 계산 엔진까지 전달되는 의미 계약과, 계산 결과를 화면/저장/검증 상태로 다룰 때 지켜야 할 경계를 정리한다. 특히 생년월일, 양력/음력, 윤달, 출생 시각, 시간 모름, 출생 지역, 검증 상태가 UI 편의나 디자인 개편 중에 다른 의미로 바뀌지 않도록 하는 것이 목적이다.

범위는 다음으로 한정한다.

- 사주 입력 draft에서 `BirthProfileInput`으로 파싱되는 계약
- 음력 해석과 만세력 asset 의존성
- 시간 모름/null 보존과 시주 계산 제외 규칙
- 출생 지역 및 지역 시간 보정 필드의 현재 의미
- `SajuChart`와 `CalculationEvidence`의 결과/검증 상태 계약
- 커밋 `b4ffe87` 이후 결과 화면에 노출되는 계산 기준 정보의 현재 구현
- 재현 가능한 빌드/테스트/증거 경로

이 문서는 사주 이론 검증서가 아니다. 내장 데이터의 권위 검증 여부와 계산 정책의 한계는 별도 백로그로 남아 있다.

## 현재 구현 동작

### 입력 의미 보존

`BirthInputDraft`는 사용자가 입력한 원본 문자열과 선택 상태를 계산 전 단계에서 보존한다.

- `name`: 공백 제거 후 비어 있으면 거부한다.
- `calendarType`: `Solar` 또는 `Lunar`로 유지한다.
- `isLeapMonth`: 음력일 때만 결과 입력의 `isLeapMonth=true`로 보존한다. 양력에서는 false로 정규화된다.
- `year`, `month`, `day`: 문자열로 들어오며 정수 변환과 날짜 범위 검사를 통과해야 한다.
- `hour`, `minute`: 둘 다 비어 있으면 시간 모름이다. 하나만 비면 거부한다.
- `birthRegionId`: 알 수 없는 ID면 기본 지역 서울로 해석한다.
- `useBirthRegionSolarCorrection`: 켜져 있는데 지역을 찾지 못하면 거부한다.

시간 모름은 반드시 `BirthDateTime.hour = null`로 전달되어야 한다. `00:00`, `12:00`, 현재 시각, 또는 임의 fallback으로 바꾸면 안 된다.

### 날짜와 달력

양력 입력은 `LocalDate.of(year, month, day)`로 실제 존재 여부를 확인한다. 음력 입력은 일자가 1~30 범위인지 먼저 확인하고, 런타임에서는 음력-양력 resolver가 반드시 필요하다.

NB 어댑터 `MtjBirthInputAdapter`는 음력 draft에 resolver가 없으면 `LunarResolverRequired`를 반환한다. 런타임 `MtjSajuRuntime`은 asset 기반 `AssetLunarDateDataSource`로 resolver를 제공하므로 음력 날짜를 내부 양력 날짜로 정규화할 수 있다.

지원 범위는 현재 1900~2100년이다. 미래 출생일은 거부한다. 오늘 날짜의 미래 시각도, 시간이 알려진 경우에는 거부한다. 시간 모름인 경우에는 날짜까지만 미래 여부를 판단한다.

### 출생 시각과 시간 모름

입력 UI의 compact 시간 입력은 `HHmm` 형식에서 시/분을 분리한다. `0930`은 `hour="9"`, `minute="30"`으로 정규화된다. 시간 모름 체크가 켜져 있으면 숨겨진 기존 시간 텍스트가 잘못된 값이어도 validation을 통과하고, 제출 시 `hour=""`, `minute=""`로 draft를 만든다.

엔진은 `BirthProfileInput.birthDateTime.hour`가 null이면 시주를 만들지 않는다.

- `SajuChart.hourPillar = null`
- 화면의 `시주` 슬롯은 구조상 유지하되 값은 `시간 모름`
- 오행 표시는 `오행 산출 제외`
- 천간/지지 한자/한글을 꾸며내지 않는다

주의할 점: `calculationBirthDateTime()`은 날짜 기반 계산용 `LocalDateTime`을 만들 때 시간이 null이면 내부적으로 12시를 사용한다. 이 값은 날짜/절기 맥락 계산을 위한 내부 기준일 뿐, 출생 시각으로 노출하거나 시주 계산에 사용하면 안 된다. 시주는 오직 원래 입력의 `hour != null`일 때만 산출한다.

### 출생 지역과 시간 보정

현재 지역 데이터는 `KoreanBirthRegions`의 국내 지역 목록을 사용한다. 기본 지역은 `seoul`이다.

`BirthProfileInput`에는 다음 지역/시간대 필드가 들어간다.

- `timezoneId`
- `placeName`
- `birthCountryCode`
- `birthRegionId`
- `birthTimeZoneId`
- `useBirthRegionSolarCorrection`
- `solarCorrectionMinutes`
- `calculationBasisLabel`

지역 시간 보정을 켜면 경도 기준 보정분을 `(longitudeEast - 135.0) * 4.0`으로 계산한다. 현재 방식은 경도만 반영하며 균시차는 반영하지 않는다. 이 한계는 `CalculationEvidence.solarCorrectionMethod = "LONGITUDE_ONLY_NO_EQUATION_OF_TIME"`로 표시된다. 보정을 끄면 `"NONE"`이다.

### 계산 경계

계산 엔진은 `DefaultSajuCalculator`가 담당한다.

- 연주는 입춘 기준으로 산출한다.
- 월주는 절기 중 짝수 `termIndex`의 직전 절입을 기준으로 산출한다.
- 일주는 내장 음력 데이터의 `ganjiDay`를 우선 사용한다.
- 시주는 `birthDateTime.hour != null`일 때만 `HourPillarPolicy.StandardTwoHourBlocks` 기본 정책으로 산출한다.
- 분석 스냅샷의 오행/십성/지장간은 `listOfNotNull(year, month, day, hour)`에 들어간 기둥만 사용한다. 시간 모름이면 시주는 분석에서 빠진다.

절기 데이터가 비어 있지 않은데 직전 절기를 찾지 못하면 월주 계산을 중단하고 사용자 메시지로 거부한다. 내장 음력 데이터에서 날짜 또는 일주를 찾지 못해도 거부한다.

### 결과와 검증 상태

`SajuChart`는 계산 결과의 공개 모델이다.

- `yearPillar`, `monthPillar`, `dayPillar`는 필수다.
- `hourPillar`는 nullable이다.
- `dayMaster`는 일간 표시용 요약 문자열이다.
- `summary`는 원국/운 흐름 해석의 사용자용 요약이다.
- `evidence`는 계산 정책, 데이터 버전, 절기, 정규화 날짜, 검증 상태를 담는다.
- `analysis`는 오행/십성/지장간 등 파생 분석이며 nullable이다.

검증 상태는 `CalculationEvidence.isVerified`와 `DataTrustLevel`을 함께 봐야 한다. 외부 기관 검증 표시는 `isVerified == true`이고 `trustLevel == ExternalAuthorityVerified`일 때만 가능하다. `GeneratedUnverified`, `policyCode`, `dataVersion`, raw asset code, raw ISO timestamp 같은 내부값은 1차 결과 카드에 직접 노출하면 안 된다.

커밋 `b4ffe87`의 핵심 변경은 이 원칙을 반영한다.

- 결과 카드의 계산 기준은 `대한민국 표준시`만 표시한다.
- `계산 기준` 버튼으로 상세 팝업을 열면 이전 절입과 한국어 날짜/시간대 표기를 보여준다.
- raw ISO 문자열은 evidence에 남기되 사용자 화면에 그대로 노출하지 않는다.
- `검토 필요` 배지, 시간 모름 처리, 기둥 순서, 엔진 계산은 바꾸지 않았다.

## 정확한 소유 파일

### GitHub NB 저장소

검토 대상 커밋은 다음 checkout에서 확인했다.

- 저장소: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB`
- 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3`
- 변경된 주요 파일:
  - `README.md`
  - `android/app/src/main/java/com/hoscat/mtj/dev/SajuChartDisplay.kt`
  - `android/app/src/test/java/com/hoscat/mtj/dev/SajuChartDisplayTest.kt`
  - `evidence/saju-metadata-r2/**`
  - `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk`

### Android 전용 셀 원천 소스

계산 모델과 런타임 의존성 확인에 사용한 Android 전용 셀은 다음이다.

- 루트: `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android`
- 입력 계약: `vendor/com/hoscat/core/model/BirthInputContract.kt`
- 지역 계약: `vendor/com/hoscat/core/model/BirthRegions.kt`
- 모델 계약: `vendor/com/hoscat/core/model/ManseModels.kt`
- 계산 엔진: `vendor/com/hoscat/core/manse/SajuCalculator.kt`
- 런타임 asset 접근: `vendor/com/hoscat/core/manse/AssetLunarDateDataSource.kt`, `AssetSolarTermDataSource.kt`
- 화면 표시: `app/src/main/java/com/hoscat/mtj/dev/SajuChartDisplay.kt`
- 입력 validation: `app/src/main/java/com/hoscat/mtj/dev/BirthDateValidation.kt`, `BirthTimeValidation.kt`
- 관련 단위 테스트: `app/src/test/java/com/hoscat/mtj/dev/BirthDateValidationTest.kt`, `BirthTimeValidationTest.kt`, `SajuChartDisplayTest.kt`

### NB 사주 계약 모듈

- 루트: `/Volumes/뽀그리/Project 먕/샤로먕/repositories/NB/saju`
- 런타임 바인딩: `src/MtjSajuRuntime.kt`
- 입력 어댑터: `src/MtjBirthInputAdapter.kt`
- 과거 계약 테스트 참고: `/Volumes/뽀그리/Project 먕/샤로먕/evidence/batch-intake-2026-09-09T06-07-01.992Z/sources/SAJ-014-contract-runtime-binding/saju/tests/MtjSajuCalculationContractTest.kt`

## 데이터와 상태 계약

### BirthInputDraft

입력 화면과 상위 상태가 유지해야 하는 draft 계약이다.

| 필드 | 의미 | 주의 |
| --- | --- | --- |
| `name` | 사용자 표시 이름 | blank면 거부 |
| `calendarType` | 양력/음력 | 음력은 resolver 필수 |
| `isLeapMonth` | 윤달 여부 | 음력에서만 의미 있음 |
| `year/month/day` | 사용자가 선택/입력한 날짜 | 샘플 날짜나 화면 이미지의 날짜로 치환 금지 |
| `hour/minute` | 출생 시각 문자열 | 둘 다 blank면 unknown, 하나만 blank면 거부 |
| `birthRegionId` | 국내 출생 지역 ID | 없거나 unknown이면 기본 서울, 단 보정 on + unknown은 거부 |
| `useBirthRegionSolarCorrection` | 경도 기반 시간 보정 여부 | 보정 방식은 현재 경도 전용 |

### BirthProfileInput

파싱 성공 후 계산 엔진에 넘기는 정규화 모델이다.

- `birthDateTime.hour`는 nullable 계약이다.
- `calendarType`과 `isLeapMonth`는 계산 재현에 필요한 입력 의미다.
- `birthTimeZoneId`와 `timezoneId`는 현재 기본적으로 `Asia/Seoul`이다.
- `calculationBasisLabel`은 사용자 화면용 기준 라벨이며 현재 `대한민국 표준시`다.
- 지역 보정 필드는 켜짐/꺼짐과 보정분을 모두 보존해야 한다.

### CalculationEvidence

계산 증거는 개발자/검증/상세 정보용이다. 모든 필드를 1차 결과 화면에 보여주면 안 된다.

- 1차 결과 카드에 허용된 현재 표시: `calculationBasisLabel`
- 상세 팝업에 허용된 현재 표시: 이전 절입명, 한국어로 포맷된 절입 시각
- 내부 전용 또는 신중 노출 대상: `policyCode`, `yearBoundary`, `monthBoundary`, `hourPolicy`, `dataVersion`, `normalizedSolarDate`, `birthClockDateTime`, `calculationDateTime`, `dayPillarDate`, `solarCorrectionMethod`

상세 화면을 확장할 때도 raw code가 그대로 보이지 않도록 한국어 label과 explanation을 통과시켜야 한다.

## 알려진 한계

1. 현재 데이터 신뢰 상태는 기본적으로 `GeneratedUnverified`다. 외부 권위 자료와 교차 검증되기 전까지 `검증됨`으로 보여주면 안 된다.
2. `DayPillarEpoch.unverifiedPreview` fallback은 빈 데이터소스 preview 용도다. 실제 asset 사용 중에는 일주 누락 시 fallback하지 말고 오류로 막아야 한다.
3. 출생 지역 시간 보정은 경도만 사용하며 균시차를 반영하지 않는다.
4. 시간 모름일 때 날짜 계산용 내부 기준으로 12시가 쓰이는 부분은 문서화되어 있지만, 후속 리팩터링에서는 더 명시적인 타입으로 분리하는 편이 안전하다.
5. 원광만세력/8자어때 등 외부 reference app 직접 관찰은 이 SAJ 배치에서 수행하지 않았다. 필요하면 DevOps Pixel lease로 별도 증거를 남겨야 한다.
6. `b4ffe87` 검증은 metadata 노출 회귀와 Saju/Tarot 표적 QA에 한정된다. 전체 접근성 매트릭스, 결제/서버, Play release 승인을 의미하지 않는다.

## 우선순위 후속 작업

### P0

- 시간 모름 제출 경로가 모든 UI 진입점에서 `hour=""`, `minute=""`, 최종 `hour=null`로 끝나는지 instrumentation 또는 reducer 테스트로 고정한다.
- 1차 결과 카드에 raw `policyCode`, `dataVersion`, raw ISO timestamp, `GeneratedUnverified` 같은 내부 문자열이 노출되지 않는 회귀 테스트를 유지한다.
- 음력 입력은 resolver 없이 계산되지 않는다는 계약을 단위 테스트로 고정한다.

### P1

- 외부 권위 자료 기준으로 절기/일주/음력 변환 fixture를 구축하고 `DataTrustLevel.ExternalAuthorityVerified` 승격 기준을 문서화한다.
- 23시 자시 일주 경계 정책(`LocalMidnight`, `ZiHour23`) 변경 시 저장 스냅샷 diff가 발생하도록 계약 테스트를 확장한다.
- 지역 시간 보정 on/off 결과 차이를 fixture로 고정하고, 경도 보정 설명 문구를 사용자용으로 정리한다.

### P2

- 시간 모름 내부 12시 기준을 별도 `UnknownTimeDateContext` 같은 타입으로 분리해 오용 가능성을 줄인다.
- `CalculationEvidence`의 사용자 표시 가능 필드와 내부 전용 필드를 sealed/display model로 분리한다.
- 외부 reference app 관찰이 필요하면 앱명/버전/화면/기기/시간/캡처/XML을 포함한 비교 evidence를 만든다.

## 재현 명령

### 커밋 확인

```bash
cd '/Volumes/뽀그리/Project 먕/샤로먕/github/NB'
git show --stat --oneline b4ffe8718bf01b02930468451baa4ecab45f1db3
git show --name-only --format=fuller b4ffe8718bf01b02930468451baa4ecab45f1db3 --
```

참고: 이 checkout에는 macOS resource fork로 보이는 `._pack-*.idx` 경고가 발생할 수 있다. 위 명령은 경고를 출력하더라도 커밋 내용은 조회된다.

### Android 전용 셀 빌드/테스트

공유 빌드 래퍼는 canonical source를 staging으로 동기화한다. 소스 수정이 필요한 경우 staging이 아니라 canonical root를 확인해야 한다.

```bash
cd '/Volumes/MTJNativeBuild/cell/android'
node run-build.mjs :app:testDebugUnitTest --offline
node run-build.mjs :app:assembleDebug --offline
```

기기 설치/캡처/ADB 검증은 DevOps Pixel lease가 있을 때만 수행한다.

### NB evidence 확인

```bash
cd '/Volumes/뽀그리/Project 먕/샤로먕/github/NB'
sed -n '1,220p' evidence/saju-metadata-r2/REPORT.md
sed -n '1,220p' evidence/saju-metadata-r2/release-manifest.json
shasum -a 256 evidence/saju-metadata-r2/REPORT.md
```

## 수용 기준

- 양력/음력/윤달 선택이 계산 입력까지 보존된다.
- 시간 모름은 null로 보존되고 시주를 만들지 않는다.
- 시간 모름 결과의 시주 슬롯은 `시간 모름`과 `오행 산출 제외`를 보여준다.
- 알려진 시간은 0~23시, 0~59분 범위 안에서만 계산된다.
- 음력 계산은 asset resolver 없이 진행되지 않는다.
- 출생 지역 보정은 켜짐/꺼짐과 보정분이 evidence에 남는다.
- 1차 결과 카드에는 사용자용 계산 기준만 보이고 raw 내부 코드/버전/ISO timestamp가 보이지 않는다.
- 상세 계산 기준은 한국어 날짜/시간대 문구로 표시하며, 포맷 실패 시 raw 값을 노출하지 않는다.
- 검증 상태는 `isVerified`와 `trustLevel`을 함께 보며, 외부 검증 조건을 만족하지 않으면 `검토 필요` 또는 내부 상태로만 표시한다.
- 회귀 증거는 APK SHA, source snapshot SHA, unit test 결과, device capture 경로와 함께 남아야 한다.

## 증거 경로와 해시

### S-02 의미 보존 산출물

- 경로: `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-2-saj-saj-014-saj/outputs/S-02-input-calculation-preservation-20260911.md`
- SHA-256: `07e43fb33180da0b9c2c07cd6a19105c7f0028da92f90478e0e5314c5bc8ac00`

### GitHub NB metadata R2

- 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3`
- APK: `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk`
- APK SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- source snapshot SHA-256: `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683`
- source manifest SHA-256: `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85`
- signing certificate SHA-256: `c0cdb6586cafab0ae49a721e27a7a9da5543bc672d474fd5303561c10550d3ab`
- report: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/REPORT.md`
- report SHA-256: `341e2da52f960d138f8efb0b5ba3d81e5a66f3361a551e36fc1532f473c33ebd`
- QA verdict: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/qa-verdict.json`
- release manifest: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/release-manifest.json`

### Device QA 요약

커밋 `b4ffe87`의 NB report 기준:

- Pixel_10 AVD, serial `emulator-5556`
- Android API 37
- 1080x2424
- density 420
- font scale 1.0
- 단위 테스트 94개 PASS
- unknown/known time, light/dark Saju 결과 PASS
- localized calculation dialog PASS
- Tarot 0/3, 2/3, 3/3, retap 2/3 PASS

별도 OPS follow-up report 기준:

- Pixel 9a AVD, Android API 37, 1080x2424, density 420, fontScale 1.0, dark mode
- 동일 APK SHA `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- installed base APK SHA 동일
- 시간 모름/알려진 시간 결과, 계산 기준 dialog, 검토 필요 배지, 시주 `시간 모름`, `오행 산출 제외` PASS
- report: `/Volumes/뽀그리/Project 먕/샤로먕/evidence/nb-saju-metadata-r2-78277c5f-20260911/REPORT.md`

## 의존성과 핸드오프 메모

- `MtjSajuRuntime`은 Android `Context`로 asset data source를 만들고, lunar/solar data를 lazy load한다.
- 런타임은 `Mutex`로 asset load와 계산 경로를 보호한다.
- 계산은 `Dispatchers.Default`, asset load는 `Dispatchers.IO`를 사용한다.
- cancellation은 삼키지 않고 다시 throw한다.
- raw asset/JSON exception이나 birth input은 사용자 진단 메시지에 직접 노출하지 않는다.
- release APK와 설치된 base APK SHA가 같은지 확인하는 절차를 유지한다.
- GitHub NB의 `saju/` 계약 소스와 Android 전용 셀의 `vendor/com/hoscat/core/**`가 서로 다른 위치에 존재하므로, 후속 수정자는 어느 쪽을 배포 원천으로 삼는지 먼저 확인해야 한다.
- `/Volumes/MTJNativeBuild/cell/android/run-build.mjs`는 canonical source를 staging으로 rsync하므로 staging 단독 수정은 다음 빌드에서 사라질 수 있다.

## 새 유지보수자 첫날 체크리스트

- [ ] 이 문서의 대상 커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3`를 조회한다.
- [ ] `BirthInputContract.kt`에서 시간 모름이 `hour=null`로 끝나는지 확인한다.
- [ ] `MtjBirthInputAdapter`에서 음력 resolver 필수 계약을 확인한다.
- [ ] `SajuCalculator.kt`에서 시주 생성 조건이 `input.birthDateTime.hour?.let { ... }`인지 확인한다.
- [ ] `CalculationEvidence`의 raw 필드를 1차 UI에 직접 노출하지 않는지 확인한다.
- [ ] `SajuChartDisplayTest`의 raw metadata 비노출 테스트를 읽는다.
- [ ] APK SHA `78277c5f...`와 evidence report를 확인한다.
- [ ] 변경 전 `node run-build.mjs :app:testDebugUnitTest --offline` 재현 가능성을 확인한다.
- [ ] 기기 QA가 필요하면 DevOps Pixel lease를 먼저 확보한다.
- [ ] 외부 검증 또는 reference app 비교가 필요하면 앱 버전/화면/기기/시간/캡처/XML을 증거로 남긴다.
