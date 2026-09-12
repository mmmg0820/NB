# 기록 저장소와 데이터 계약 인수인계

이 문서는 샤로먕 Android 앱의 기록 저장, 스키마, 필터, 스냅샷, 상세 화면, 마이그레이션/복구 검증 기준을 새 담당자가 바로 이어받을 수 있도록 정리한다.

## 기준 범위

- 기준 저장소: `https://github.com/mmmg0820/NB`
- 기준 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3`
- 기준 태그: `v0.1-dev-20260911-r2`
- 기준 APK: `releases/2026-09-11-r2/sharomyang-saju-metadata-r2-debug.apk`
- 기준 APK SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- 패키지: `com.hoscat.mtj.dev`
- 이 문서 작성 범위: 기록 저장소와 데이터 계층 설명, 유지보수 체크리스트, 후속 작업 정리

이 문서 작성 중 공유 Git 체크아웃은 수정하지 않았다. 이 문서는 `/Volumes/뽀그리/Project 먕/샤로먕/handoff/github-docs-20260911/records-data/README.md`에만 새로 작성된 인수인계 문서다.

## 목적

기록 계층은 사용자가 생성한 사주 명식과 타로 리딩 결과를 앱 안에서 다시 볼 수 있게 보존한다. 목표는 세 가지다.

1. 저장 당시의 입력과 결과 스냅샷을 그대로 보존한다.
2. 같은 기록을 다시 저장하거나 다시 가져와도 중복과 덮어쓰기를 막는다.
3. 손상된 데이터나 미구현 마이그레이션을 조용히 지우지 않고 명시적으로 실패시킨다.

현재 구현은 로컬 MTJ 앱 샌드박스 전용 저장소다. 기존 먕사주, 먕타주, 다른 앱의 DB를 직접 열거나 변환하지 않는다. 실제 원본 앱 import/export, 백업/복원, 클라우드 동기화, 출시용 마이그레이션 실행기는 아직 구현 범위 밖이다.

## 현재 구현 동작

- Android 앱 시작 시 `RecordStore(context)`가 생성되고, 홈/사주/타로/기록 화면이 같은 store 인스턴스를 공유한다.
- 사주 결과가 `MtjSajuEvaluation.Accepted`이면 `RecordSnapshots.saju(result)`가 profile envelope 1개와 saju envelope 1개를 만든다.
- 사주 결과 화면의 `명식 저장` 버튼은 두 envelope를 한 트랜잭션으로 저장한다.
- 타로 결과가 완성되면 `RecordSnapshots.tarot(result)`가 tarot envelope 1개를 만들고, 타로 결과 화면의 `저장` 버튼이 저장한다.
- 기록 탭은 DB에서 모든 envelope를 읽은 뒤 `Kind.PROFILE`을 목록에서 제외하고 `snapshotAtEpochMillis` 내림차순으로 보여준다.
- 목록 필터는 `전체`, `사주`, `타로` 세 가지다.
- 목록 항목은 종류별 아이콘, 제목, 저장 시각을 보여주고 탭하면 텍스트 상세로 이동한다.
- 상세 화면은 summary와 `RecordDetailRows.detailRows()`가 만든 핵심 행을 보여준다.
- 삭제는 확인 다이얼로그를 거친다. 확인 시 선택 envelope만 삭제한다.
- 삭제하려는 origin이 다른 envelope의 `profileRefs`에 있으면 삭제를 거부한다.
- SQLite upgrade는 아직 구현하지 않았고, destructive upgrade 대신 즉시 실패한다.

## 정확한 소스 소유권과 파일

기록 계층의 일차 소유권은 `MTJ 확장 배치 1`이다. Android 정본 반영, 충돌 정리, 전체 빌드는 `MTJ Android 개발 전담`이 수행한다.

| 파일 | 책임 | 기준 커밋 blob |
|---|---|---|
| `records/src/MigrationPlanner.kt` | 공통 기록 envelope 모델, identity, canonical payload, admission plan | `3546b4767f4f1ccfa98b6b836f5b9156ae3dfb7a` |
| `records/src/RecordsJsonCodec.kt` | strict JSON wire codec, decode/encode 실패 코드, 크기/깊이/노드 제한 | `a3420c4e83bb35e9a1ada479876e439000788385` |
| `android/app/src/main/java/com/hoscat/mtj/dev/RecordStore.kt` | Android SQLite 저장소, 트랜잭션, delete guard | `c345fe73f584086a68822cd7db1a7ab7060cc749` |
| `android/app/src/main/java/com/hoscat/mtj/dev/RecordSnapshots.kt` | 사주/타로 결과를 envelope payload로 스냅샷화 | `51e119568245820107ccbde9346158842c823d38` |
| `android/app/src/main/java/com/hoscat/mtj/dev/RecordsScreen.kt` | 기록 목록, 필터, 상세, 삭제 UI | `8b2403bc94b9ad74b678e2511867b2a53ded37eb` |
| `android/app/src/main/java/com/hoscat/mtj/dev/RecordDetailRows.kt` | 저장된 payload에서 사용자용 상세 행 추출 | `71f1c79d8c0579019615ba64c94a2b914cc5db80` |
| `android/app/src/test/java/com/hoscat/mtj/dev/RecordAdmissionTest.kt` | Android 단위 admission 회귀 | `d4300f7c9c3d9639c9f8d6206caa25d7b1ecfe13` |
| `android/app/src/test/java/com/hoscat/mtj/dev/RecordDetailRowsTest.kt` | 상세 행 노출/비노출 회귀 | `cc988ab97edf8ad04639fd5fa18d655a1c600c63` |

`android/app/build.gradle.kts`는 `sourceSets["main"].java.srcDirs("../vendor", "../../saju/src", "../../tarot/src", "../../records/src", "../../design/native")`로 `records/src`를 앱 빌드에 포함한다. records 모듈은 별도 Gradle module이 아니라 앱 source set에 포함된 Kotlin 소스다.

## 저장소 스키마

Android 저장소는 `RecordStore.kt`의 `SQLiteOpenHelper`가 관리한다.

| 항목 | 값 |
|---|---|
| DB 파일 | `mtj-records-v1.db` |
| SQLite version | `1` |
| table | `records` |
| primary key | `identity TEXT PRIMARY KEY NOT NULL` |
| payload column | `envelope BLOB NOT NULL` |
| 정렬 | 읽기 시 `ORDER BY identity`, 화면 표시 시 `snapshotAtEpochMillis` 내림차순 |

`identity`에는 `Envelope.origin.commonId`를 저장한다. 현재 형식은 `mtj1:` + length-prefixed source + length-prefixed id 이다. 예를 들어 source와 id에 콜론이 들어가도 단순 구분자 충돌이 나지 않도록 길이 접두를 사용한다.

`envelope`는 `RecordsJsonCodec.encode(envelope)` 결과의 UTF-8 JSON byte array다. DB column은 BLOB이지만 내용은 strict JSON wire format이다. SQLite에는 domain별 column을 두지 않는다. 검색, 인덱싱, 날짜별 조회, 종류별 인덱스는 아직 없다.

## Envelope 계약

공통 envelope 모델은 `records/src/MigrationPlanner.kt`에 있다.

```kotlin
data class Envelope(
    val origin: Origin,
    val kind: Kind,
    val payload: Value.Obj,
    val profileRefs: List<Origin> = emptyList(),
    val schemaVersion: Int = 1,
    val payloadVersion: Int = 1,
    val snapshotAtEpochMillis: Long = 0,
)
```

`Kind`는 `PROFILE`, `SAJU`, `TAROT`, `COMPATIBILITY`를 정의한다. 현재 Android 화면은 `PROFILE`을 목록에서 숨기고 `SAJU`, `TAROT`을 표시한다. `COMPATIBILITY`는 모델과 planner 계약에는 있지만 현재 앱 UI 상세는 없다.

참조 규칙은 아래와 같다.

- `PROFILE`: `profileRefs`가 비어 있어야 한다.
- `SAJU`: `profileRefs`가 정확히 1개 있어야 한다.
- `TAROT`: 현재 참조가 없어도 된다.
- `COMPATIBILITY`: `profileRefs`가 정확히 2개 있어야 한다.
- 모든 `Origin.source`와 `Origin.id`는 빈 문자열이면 안 된다. 공백 문자열은 trimming하지 않으므로 잘못된 identity로 취급해야 한다.

## 값과 canonical 비교

`Value`는 object, array, string, number token, boolean, null만 지원한다. planner는 payload를 canonical string으로 바꿔 기존 기록과 incoming 기록을 비교한다.

- object key는 정렬해서 비교한다.
- array order는 의미가 있으므로 순서가 바뀌면 다른 payload다.
- number token은 `BigDecimal.stripTrailingZeros()`로 정규화한다. `1.0`, `1e0`, `1`은 같은 숫자로 비교되고, `-0`은 `0`으로 비교된다.
- string의 공백과 Unicode normalization form은 보존한다. `"x"`와 `" x"`, `é`와 `e + combining acute`는 다르다.
- null, absent field, string `"1"`, number `1`은 모두 다르게 본다.
- 최대 깊이 64, 최대 노드 10000, canonical output 1,000,000자 제한이 있다.

이 비교는 hash만 믿지 않는다. 기존 테스트는 Java hash collision 예시도 content conflict로 유지되는지 검증한다.

## JSON codec 계약

`RecordsJsonCodec`는 envelope wire format을 엄격하게 읽고 쓴다.

- 최대 byte 수: `2_000_000`
- 최대 depth: `64`
- 최대 nodes: `10_000`
- strict JSON만 허용한다. 주석, trailing comma, single quote, unquoted key, NaN, malformed number는 거부한다.
- UTF-8은 replacement 없이 decode하며 malformed UTF-8, unpaired surrogate, BOM 시작을 실패 처리한다.
- object duplicate key는 map에 덮어쓰기 전에 검출해 실패시킨다.
- envelope top-level field set은 정확히 `schemaVersion`, `origin`, `kind`, `payloadVersion`, `snapshotAtEpochMillis`, `profileRefs`, `payload`이어야 한다.
- `schemaVersion`과 `payloadVersion`은 현재 `1`만 허용한다.
- metadata integer는 fraction/exponent/overflow/negative timestamp를 허용하지 않는다.
- 실패 예외 `RecordsCodecException`은 사용자 데이터, JSON path, field name, parser 원문을 메시지에 넣지 않고 `CodecFailure` code만 노출한다.

codec은 domain schema를 추론하거나 I/O를 수행하지 않는다. source app conversion도 여기서 하지 않는다.

## 저장 admission과 마이그레이션 정책

`RecordStore.insert(batch)`는 caller-owned map/list 변형 영향을 피하려고 각 envelope를 encode/decode로 한 번 freeze한 뒤 transaction에 들어간다. 이후 `admittedRecords(existing, incoming)`을 통해 insert 가능한 envelope만 골라 `insertOrThrow`한다.

`MigrationPlanner.plan(existing, incoming)`의 핵심 판정은 아래와 같다.

| 상황 | 판정 |
|---|---|
| 새 origin과 valid payload/reference | `INSERT`, `NEW` |
| 기존과 같은 origin, 같은 metadata, 같은 canonical payload | `SKIP`, `IDENTICAL` |
| 같은 origin이지만 metadata 또는 payload가 다름 | `CONFLICT`, `CONTENT_DIFFERS` |
| incoming batch 안 같은 origin이 여러 개이며 내용이 같음 | 첫 항목 `INSERT`, 이후 `SKIP` |
| incoming batch 안 같은 origin이 여러 개이며 내용이 다름 | 모두 `CONFLICT` |
| 기존 target DB에 duplicate origin이 있음 | `TargetIssue(DUPLICATE_TARGET)`, plan not ready |
| 기존 target DB가 invalid envelope를 포함 | target issue, plan not ready |
| profile 참조가 없거나 잘못된 kind를 참조 | `INVALID`, `REFERENCES` |
| profile이 incoming batch 뒤쪽에 있어도 batch 안에서 resolve 가능 | order-independent `INSERT` |

`admittedRecords`는 `plan.ready`가 아니면 전체 batch를 거부한다. 따라서 사주 저장에서 profile은 들어가고 chart만 빠지는 partial admission을 막아야 한다. 실제 DB transaction도 profile+chart 저장을 함께 감싼다.

현재 구현은 "기존 DB v1 안에서 envelope batch를 안전히 admission"하는 수준이다. 외부 앱 원본 스키마에서 MTJ envelope로 변환하는 migration executor, backup restore, schema v2 upgrade, conflict UI는 없다.

## 사주 스냅샷 계약

`RecordSnapshots.saju(result)`는 두 개의 envelope를 만든다.

1. `Kind.PROFILE`
   - origin: `Origin("mtj-native", UUID)`
   - payload: `{ "input": <BirthProfileInput tree> }`
   - refs: empty
   - `snapshotAtEpochMillis`: saju envelope와 같은 timestamp
2. `Kind.SAJU`
   - origin: 새 `Origin("mtj-native", UUID)`
   - refs: profile origin 1개
   - payload fields:
     - `title`: `"${result.chart.name}님의 명식"`
     - `summary`: `result.chart.summary`
     - `chart`: `result.chart` 전체 tree
     - `input`: `result.input` 전체 tree

사주 도메인 handoff의 의미 보존 기준은 아래와 같다.

- `BirthProfileInput`의 원본 필드를 보존한다.
- 양력/음력/윤달 입력 의미를 바꾸지 않는다.
- 시간모름은 `unknown hour = null`, `minute = 0` 형태로 보존될 수 있다. `minute = 0`만 보고 자정으로 해석하면 안 된다.
- `CalculationEvidence`의 `dataVersion`, `policyCode`, `trustLevel`, `isVerified`, 보정/정규화 필드는 chart snapshot 안에서 보존한다.
- 단, 사용자 상세 화면에는 raw `policyCode`, `dataVersion`, `GeneratedUnverified`, `myeongri_kr_v2`, `manse-seed` 같은 내부 문자열을 직접 노출하지 않는다.

현재 상세 화면은 저장된 chart/input에서 생년월일, 태어난 시간, 명식, 일간, 계산 기준, 검증 상태를 텍스트 행으로 만든다. 전체 명식 시각화 복원은 아직 완료되지 않았다.

## 타로 스냅샷 계약

`RecordSnapshots.tarot(result)`는 하나의 `Kind.TAROT` envelope를 만든다.

- origin: `Origin("mtj-native", UUID)`
- refs: empty
- payload fields:
  - `title`: 질문이 있으면 질문, 없으면 spread title
  - `summary`: 선택 카드의 위치, 카드명, 방향, meaning snapshot을 줄바꿈으로 합친 텍스트
  - `snapshot`: `MtjResultSnapshot` 전체 tree
- `snapshotAtEpochMillis`: 저장 시각

`TarotRecreationState`는 result를 durable draft로 저장하지 않는다. Compose saved state용 flat primitive 목록만 저장한다. 완료된 타로 result는 `resultHash`로 재생성 record fingerprint를 확인한 뒤 보여준다. 앱 프로세스 종료 후 durable하게 남기는 것은 `RecordStore`에 들어간 envelope다.

### R-01 통합 요약과 레거시 fallback

2026-09-11 R-01 후속 검토에서 Tarot live result와 stored record detail이 같은 통합 요약 모델을 사용해야 한다는 요구가 추가됐다. Android owner가 `/Users/thomaslee/Documents/Codex/2026-09-09/mtj-android-development/work/a01-tarot-summary` isolated lane에서 구현을 소유하므로 records-data는 schema 검토와 fallback 기준만 제공한다.

핵심 계약은 schema migration 없는 render-time fallback이다. SQLite version, `Envelope.schemaVersion`, `payloadVersion`, 기존 envelope BLOB은 바꾸지 않는다. 레거시 기록에 original question 또는 description/subtitle이 없어도 기록을 rewrite하지 않고, 저장된 snapshot field만 읽어 표시한다.

Fallback 우선순위:

- 카드 장수: `snapshot.spread.cardCount` positive integer, 없으면 `snapshot.reading.cardCount`, 없으면 `장수 정보 없음`
- 스프레드 이름: nonblank `snapshot.spread.title`, 없으면 nonblank `snapshot.reading.spreadTitle`, 없으면 `이름 정보 없음`
- 설명: nonblank `snapshot.spread.subtitle`, 없으면 `설명 정보 없음`
- 질문: present `snapshot.reading.question` string 그대로, 없거나 string이 아니면 `질문 정보 없음`

질문은 값이 있으면 trim, 번역, normalize, blank replacement를 하지 않는다. 빈 문자열이나 공백 문자열도 레거시 원본이면 그대로 둔다. 설명이 없을 때 현재 catalog에서 subtitle을 가져오면 저장 당시 snapshot이 아닌 현재 앱 데이터에 따라 옛 기록이 바뀌므로 금지한다.

상세 기준:

- live Tarot result와 record detail은 같은 formatter/component를 사용한다.
- record detail 상단에 통합 summary panel을 보여준다.
- 아래 detail rows는 `선택 카드`처럼 snapshot 고유 상세만 남기고 `스프레드`/`질문`을 중복하지 않는다.
- legacy `summary`가 비어 있으면 빈 `Text("")`로 불필요한 세로 여백을 만들지 않는다.

세부 인계 문서: `/Volumes/뽀그리/Project 먕/샤로먕/handoff/github-docs-20260911/records-data/R-01-tarot-summary-legacy-fallback.md`

## 상세 화면 상태

기록 탭의 상태는 `RecordsScreen.kt`가 관리한다.

- `records: List<Envelope>?`: null이면 loading, 값이 있으면 loaded.
- `selectedId: String?`: null이면 목록, 값이 있으면 상세.
- `selectedFilter: RecordFilter`: `ALL`, `SAJU`, `TAROT`.
- `error: String?`: read/delete 실패 메시지.
- `refresh: Int`: 재시도와 삭제 후 reload trigger.
- `confirm: Boolean`: 삭제 확인 다이얼로그.
- `deleting: Boolean`: 삭제 중 버튼 disable 및 label 변경.

`initialSelectedId`가 들어오면 `LaunchedEffect(initialSelectedId)`로 selectedId를 갱신한다. 이후 reload에서 selectedId가 더 이상 존재하지 않으면 목록으로 돌아가고 `"이 기록을 찾을 수 없습니다."`를 보여준다.

주의할 점: 과거 device-records smoke에서 configuration change 이후 record detail이 목록으로 되돌아가는 결함이 발견됐다. 기준 커밋의 `selectedId`는 `rememberSaveable`이지만, 실제 detail route 보존은 같은 SHA에서 다시 검증해야 한다. 새 담당자는 이 항목을 먼저 재현해야 한다.

## Loading, empty, error 동작

| 상태 | 사용자 화면 |
|---|---|
| `records == null && error == null` | 가운데 `CircularProgressIndicator` |
| 읽기 실패 | `"기록을 읽지 못했습니다. 원본 데이터는 유지됩니다."`와 `다시 시도` |
| 전체 records가 비어 있음 | `"아직 저장된 기록이 없습니다."` empty state |
| 필터 결과만 비어 있음 | `"해당하는 기록이 없습니다."` empty state |
| 선택한 기록이 reload 후 없음 | `"이 기록을 찾을 수 없습니다."` |
| 삭제 실패 | `"삭제하지 못했습니다. 다시 시도해 주세요."` |

읽기 실패 시 DB를 지우거나 빈 기록으로 대체하지 않는 것이 중요하다. 손상된 envelope가 있으면 `read(db)`의 codec/planner 검증에서 예외가 나고 UI는 오류 상태로 남아야 한다.

## 삭제와 복구 정책

삭제는 현재 선택 envelope에만 적용된다.

- 삭제 취소: DB 내용이 그대로 남아야 한다.
- 삭제 확인: 선택 envelope가 삭제되고 목록으로 돌아가야 한다.
- profile 삭제: 그 profile을 참조하는 SAJU/COMPATIBILITY가 있으면 실패해야 한다.
- orphan profile: chart 삭제 후 남는 profile은 현재 정책상 retained 상태다. 자동 cascade나 silent cleanup을 하지 않는다.

복구와 재생성은 아직 제한적이다.

- 저장된 사주 상세는 텍스트 요약/행 중심이다. 전체 `SajuChartDisplay`를 stored snapshot에서 다시 구성하는 기능은 backlog다.
- 저장된 타로 상세도 spread/card 텍스트 행 중심이다. 전체 spread visualization 복원은 backlog다.
- corrupt DB/envelope injection, forced write failure, app upgrade migration은 실제 기기/SQLite 수준에서 아직 충분히 자동화되지 않았다.

## 빌드와 테스트 명령

기준 커밋에는 Gradle wrapper 파일이 포함되어 있지 않다. 검증 환경에서는 Gradle 8.11.1이 사용된 흔적이 있으며, Android plugin은 8.10.1, Kotlin은 2.1.21, JVM target은 17이다. `ANDROID_USER_HOME` 아래 debug keystore가 필요하다.

권장 재현 절차:

```bash
cd '/Volumes/뽀그리/Project 먕/샤로먕/repositories/NB/android'
export ANDROID_USER_HOME="$HOME/.android"
gradle clean :app:testDebugUnitTest :app:assembleDebug
```

기록 관련 단위 테스트만 빠르게 볼 때:

```bash
cd '/Volumes/뽀그리/Project 먕/샤로먕/repositories/NB/android'
export ANDROID_USER_HOME="$HOME/.android"
gradle :app:testDebugUnitTest --tests 'com.hoscat.mtj.dev.RecordAdmissionTest' --tests 'com.hoscat.mtj.dev.RecordDetailRowsTest'
```

기준 커밋의 release evidence는 clean assemble과 unit test 94건 통과를 보고한다. 이 문서 작성 중에는 새 빌드나 새 테스트를 실행하지 않았다.

## 기록 데이터 무결성 테스트

기준 커밋에 포함된 Android 단위 테스트:

- `RecordAdmissionTest`
  - profile+chart batch가 함께 admission되는지 확인
  - 같은 기록 재시도 시 추가 삽입이 없는지 확인
  - profile 없는 chart를 거부하는지 확인
  - 같은 identity의 다른 content overwrite를 거부하는지 확인
  - source namespace가 다르면 같은 id라도 별도 identity인지 확인
  - incoming batch 내부 동일 기록 반복은 1회만 admission되는지 확인
- `RecordDetailRowsTest`
  - SAJ 상세에서 생년월일, 시간모름, 명식, 검증 상태가 사용자용으로 나오는지 확인
  - SAJ 상세에서 raw 정책/데이터 label을 별도 행으로 노출하지 않는지 확인
  - TAROT 상세에서 스프레드, 질문, 카드 순서/방향이 나오는지 확인

기존 records-cell 작업물에는 더 넓은 planner/codec 테스트가 있다. 이 테스트들은 기준 커밋에는 test source로 포함되어 있지 않지만, contract 유지보수 참고로 중요하다.

- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/records/test/MigrationPlannerTest.kt`
  - SHA-256: `4203d445307c08cd06c03bc8f35d155d1e752d86826afe8d4d140479c3b996dc`
  - object order, decimal normalization, array order, null/absent/string/number 구분, hash collision, invalid version, depth/text/node limits, refs, duplicate target, deterministic replan 등을 검증한다.
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/records/test/RecordsJsonCodecTest.kt`
  - SHA-256: `d08caa60814bd997b3df8a0d10e28a1f8978ebaaba03b272b27dbaba95a61e93`
  - strict JSON, UTF-8, duplicate key, unknown schema, wrong types, number syntax, depth/size/node bounds, unresolved refs 보존 등을 검증한다.
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/test/java/com/hoscat/mtj/dev/RecordsMigrationPlannerTest.kt`
  - SHA-256: `1ba720758db7867c58be72d1ea6c7502f84cadf221062744370a76337d8c6ca0`
  - 두 번째 migration idempotence, duplicate target block, forward reference order-independence를 Android test suite에 붙이기 위한 후보다.

후속 담당자는 위 records-cell 테스트 중 기준 저장소에 아직 없는 항목을 정본 테스트로 승격할지 Android 통합 담당과 정해야 한다.

## 수용 기준

기록 저장/복원은 아래 조건을 만족해야 PASS로 볼 수 있다.

- 사주 저장은 profile 1건과 chart 1건이 같은 transaction으로 들어간다.
- 사주 저장 재시도는 visible chart를 중복 생성하지 않는다.
- 타로 저장 재시도는 visible tarot record를 중복 생성하지 않는다.
- 같은 origin의 다른 payload는 overwrite하지 않고 실패한다.
- 손상된 기존 envelope가 있으면 기록 화면은 오류를 보여주고 DB를 지우지 않는다.
- profile 참조가 없는 SAJU는 admission되지 않는다.
- 참조 중인 profile 삭제는 거부된다.
- 삭제 취소는 byte-for-byte 또는 canonical-equivalent DB 상태를 보존한다.
- 삭제 확인 후 앱 재시작해도 삭제된 visible record는 돌아오지 않는다.
- `PROFILE`은 records 목록에 노출되지 않는다.
- `전체/사주/타로` 필터가 kind별로 정확히 동작한다.
- 상세 화면에는 저장 당시 title/summary/snapshot 기반 정보가 나오며, 최신 계산 결과로 자동 재계산하지 않는다.
- 사용자 화면/XML에 내부 식별자, raw policy/dataVersion/provenance 문자열이 노출되지 않는다.
- configuration change, process recreation, force-stop/reopen 뒤 selected detail과 filter 상태가 요구사항대로 복원된다.
- 동일 SHA 기기 증거에는 APK SHA, package focus, storage-before/after, screenshot/XML, bounds, strict log가 함께 있어야 한다.

## 재현 시나리오

최소 기기 검증 시나리오는 아래 순서로 실행한다.

1. 전용 emulator 또는 실제 기기를 확보하고 다른 팀 세션과 섞이지 않게 한다.
2. 기준 APK 또는 검증 대상 APK를 clean install한다.
3. 설치된 base APK SHA가 artifact APK SHA와 같은지 확인한다.
4. 사주 탭에서 synthetic 별칭, 날짜, 시간모름으로 명식을 만든다.
5. `명식 저장`을 두 번 탭한다.
6. 앱을 force-stop/reopen하고 기록 탭에서 visible SAJ record가 1건인지 확인한다.
7. 상세로 들어가 생년월일, 시간모름, 명식, 일간, 계산 기준, 검증 상태를 확인한다.
8. 상세 화면에서 회전 또는 configuration change를 수행하고 같은 detail이 유지되는지 확인한다.
9. 삭제 다이얼로그에서 취소하고 storage가 변하지 않았는지 확인한다.
10. 삭제를 확인하고 force-stop/reopen 뒤 visible SAJ record가 사라졌는지 확인한다.
11. 타로 탭에서 한 리딩을 끝내고 `저장`을 두 번 탭한다.
12. force-stop/reopen 뒤 visible TAROT record가 1건인지 확인한다.
13. `전체`, `사주`, `타로` 필터별 목록 수와 empty state를 확인한다.
14. strict log에서 MTJ 앱의 `FATAL EXCEPTION`, `ANR in`, `OutOfMemoryError`, `am_crash`, `am_anr`가 0인지 확인한다.

큰 글씨/작은 화면 검증은 최소 360dp, 411dp, 600dp와 fontScale 1.0, 1.3, 1.5, 2.0 조합 중 핵심 결과/기록 화면을 포함해야 한다.

## 증거와 해시

기준 커밋의 release evidence:

- report: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/REPORT.md`
  - SHA-256: `341e2da52f960d138f8efb0b5ba3d81e5a66f3361a551e36fc1532f473c33ebd`
- QA verdict: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/qa-verdict.json`
  - SHA-256: `2fd15dcc373a9ad38fd4befe88e09d7fef5d34019a0d04a51fafa0df8d06c4d0`
- APK: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/sharomyang-saju-metadata-r2-debug.apk`
  - SHA-256: `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`
- source snapshot: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/source-snapshot.tar.gz`
  - SHA-256: `1e0df154380a6517630395150c3b00f56380de99e6681b4b05c37b3f0f0e5683`
- source manifest: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/source-manifest.sha256`
  - SHA-256: `58d5599c7542034bf0d30faa98e3d729a5426423e4ba85ab1747ac3ea47f1c85`
- release manifest: `/Volumes/뽀그리/Project 먕/샤로먕/github/NB/evidence/saju-metadata-r2/release-manifest.json`
  - SHA-256: `4272f0f6576fd84b470433e37c58394fe6edab209886576ab82cb7962fd11dcc`

이 evidence는 clean build/unit tests와 SAJ metadata/Tarot selection regression을 증명한다. records-specific 기기 smoke 전체를 이 APK SHA에서 다시 수행했다는 의미는 아니다.

기존 records-device smoke evidence:

- README: `/Volumes/뽀그리/Project 먕/사로먕/evidence/device-records/README.md`
  - SHA-256: `1cf7fe17bf62ae3f6e9c9663b6f56957fcbf7235dad7f370438f3d5de7668176`
- verdict: `/Volumes/뽀그리/Project 먕/사로먕/evidence/device-records/verdict.json`
  - SHA-256: `9c6b5c937aae6751c5f483159b583c17a5921f7ba5732ec5e3650eb901fd628a`
- tested package: `com.hoscat.mtj.dev`
- tested APK SHA-256: `9024c93c4e2544f05d6797bcdc56539b5dd7d245b92f9b46cf16c33690bfd0bd`
- scope: synthetic SAJ unknown-time save/retry/restart/delete, one-card TAROT save/retry/restart, sampled 8dp button-to-tab gap

이 evidence는 records flow의 초기 smoke 참고 자료다. 기준 커밋 APK SHA `78277c5...`의 records release PASS로 재사용하면 안 된다.

기존 records handoff:

- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/RECORDS-HANDOFF.md`
  - SHA-256: `de8073672796281f7ca582fdf66a47e7f5d9e03f766764cfc4190bb940f44a6a`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/saju/RECORDS-PAYLOAD.md`
  - SHA-256: `2b84887eddc91612f3fcf2a66ed3183cf59d5e94347ef4ce1398a46b5dd1df48`

## 알려진 제한

- records DB는 v1 단일 테이블이며 검색/인덱스/페이지네이션이 없다.
- 실제 원본 앱 DB import, backup restore, external export는 없다.
- schema upgrade는 `error("Migration required; refusing destructive upgrade")`로 막혀 있다.
- full saved chart/spread visualization re-entry는 완료되지 않았다.
- configuration change 뒤 detail route 보존은 defect evidence가 있어 재검증이 필요하다.
- malformed stored envelope, write failure, low-storage, concurrent save storm은 자동화된 기기 테스트가 부족하다.
- orphan profile cleanup 정책이 없다. 현재는 chart 삭제 후 profile을 보존한다.
- `COMPATIBILITY` kind는 planner 계약에 있지만 사용자 UI/detail이 없다.
- records 목록은 `ORDER BY identity`로 DB를 읽은 뒤 메모리에서 정렬하므로 대량 데이터 성능 기준이 없다.
- PII redaction/export/logging 정책은 payload가 birth/time/place/name을 포함할 수 있다는 경고 수준이며 제품 정책 확정이 필요하다.
- 기준 커밋의 evidence는 Pixel_10 AVD fontScale 1.0 중심이다. 큰 글씨와 여러 폭의 records matrix는 별도 닫힘이 필요하다.

## 우선순위 후속 작업

P0:

1. 기준 커밋 APK SHA `78277c5...` 또는 다음 승인 APK에서 records-specific same-SHA smoke를 다시 실행한다.
2. configuration change/process recreation 뒤 selected detail과 selected filter가 유지되는지 고친다.
3. corrupt envelope 주입 시 오류 화면과 DB 보존을 기기/SQLite 수준으로 검증한다.
4. forced write failure에서 profile+chart partial insert가 없는지 검증한다.
5. R-01 Tarot 통합 요약을 same-SHA에서 검증한다. 특히 original question exact preservation, 레거시 missing question/description fallback, record detail 중복 제거, 빈 summary 여백 제거를 확인한다.

P1:

1. stored SAJ snapshot에서 전체 명식 화면을 다시 구성하는 re-entry를 구현한다.
2. stored TAROT snapshot에서 spread/card visualization을 다시 구성하는 re-entry를 구현한다.
3. records-cell planner/codec 테스트를 기준 저장소의 정식 test source로 승격한다.
4. orphan profile 관리 정책을 정한다. 예: 명시적 cleanup, hidden retained profile, profile detail UI 중 하나.
5. records detail에서 raw provenance 노출 금지 regression을 XML 기준으로 자동화한다.

P2:

1. records 검색, 날짜 범위, kind별 count, sort option을 설계한다.
2. encrypted backup/export/import 정책을 설계한다.
3. `COMPATIBILITY` record UI/detail/re-entry를 설계한다.
4. 대량 기록 성능 테스트와 paging/Room 전환 여부를 결정한다.
5. 사용자 데이터 삭제 요청, 로그 redaction, privacy review checklist를 문서화한다.

## 의존성과 인계 메모

- 사주 입력 의미, 시간모름, 계산 증거 보존은 SAJ engine/UI 담당과 맞춰야 한다.
- 타로 result snapshot 구조와 card/spread definition version은 TAR 담당과 맞춰야 한다.
- Android 통합 담당은 shared source set 충돌과 전체 unit/build를 최종 책임진다.
- QA는 같은 APK SHA, 같은 package focus, 같은 device run evidence만 PASS 근거로 써야 한다.
- DevOps는 GitHub push/tag/release만 담당한다. handoff 경로의 문서는 정본 소스가 아니다.
- 사용자 승인 전 디자인 변경 작업에서는 이전 APK PASS나 내부 PASS를 새 cycle 승인으로 쓰지 않는다.

## 새 담당자 첫날 체크리스트

1. GitHub에서 `b4ffe8718bf01b02930468451baa4ecab45f1db3`를 체크아웃하고 태그 `v0.1-dev-20260911-r2`를 확인한다.
2. `README.md`와 이 문서를 읽고 기준 APK SHA `78277c5f3e1e031934469dcb10108988726c1e31caee52294b3df0d2f14041c2`를 재계산한다.
3. `records/src/MigrationPlanner.kt`와 `records/src/RecordsJsonCodec.kt`를 먼저 읽고 identity/reference/admission 계약을 이해한다.
4. `RecordStore.kt`, `RecordSnapshots.kt`, `RecordsScreen.kt`, `RecordDetailRows.kt` 순서로 Android integration path를 따라간다.
5. `RecordAdmissionTest`와 `RecordDetailRowsTest`를 실행해 기준 회귀가 깨지지 않는지 확인한다.
6. 기존 records-cell planner/codec 테스트를 정본에 승격할지 Android 통합 담당과 결정한다.
7. 전용 기기에서 사주 저장 두 번, force-stop/reopen, 상세, 삭제 취소/확인, 타로 저장 두 번, 필터 empty state를 같은 SHA로 검증한다.
8. configuration change detail reset defect를 먼저 재현하고, 고치면 same-SHA PNG/XML/storage evidence를 남긴다.
9. 저장소 upgrade 또는 import를 만지기 전 schema version, backup, rollback, corrupt DB policy를 문서로 승인받는다.
10. 사용자의 실제 개인정보가 들어간 payload를 로그, issue, 외부 전송, screenshot에 남기지 않는다.
