# 타로 스프레드 유지보수 핸드오프

> 통합 정정: 아래 제출 원문의 78장 적응형/5열 모드 설명은 기준 커밋 b4ffe871과 불일치한다. 실제 `android/app/src/main/java/com/hoscat/mtj/dev/TarotSelectionGridContract.kt`는 78장에 8열×10행을 반환하며 해당 커밋에 `TarotSelectionLayoutMode`는 없다. 현재 동작은 8열×10행이다. 원문의 두 모드 구현 완료 설명을 현재 제품 상태로 해석하지 않는다. 상세 정정은 [후속 목록](../BACKLOG.md)에 남기고 제출본/통합본 SHA를 별도로 기록했다.

작성일: 2026-09-11  
대상 커밋: `b4ffe8718bf01b02930468451baa4ecab45f1db3` (`Move Saju calculation metadata into localized details`, 2026-09-11 09:25:24 +09:00)  
작성 범위: 샤로먕/MTJ 타로 스프레드 정의, 선택 목록, 카드 선택 그리드, 결과 스프레드 렌더링 계약

## 목적과 범위

이 문서는 다음 유지보수자가 타로 스프레드 기능을 다시 열었을 때 "어떤 파일이 권위인지", "무엇을 깨면 안 되는지", "스프레드를 추가하거나 노출 정책을 바꿀 때 어떤 테스트를 돌려야 하는지"를 바로 확인하도록 만든 작업 노트다.

핵심 범위는 27개 일반 리딩용 선택 가능 스프레드, 8개 카테고리 분류, 위치 라벨/카드 수/좌표 계약, 결과 화면의 공간형 렌더링과 순서형 fallback, 그리고 카드 선택 화면의 두 가지 카드 스프레드 옵션이다. 카드 해석 문구, AI 프롬프트 품질, 결제/광고, 저장소 배포는 이 문서의 직접 범위가 아니다.

## 현재 구현 요약

스프레드의 원천 데이터는 MyangTarot에서 가져온 `com.softcat.mystictarot` vendor 코드다. `SpreadDefinitions.kt`가 layout, preset, key, card count, label list, slot coordinate를 만들고 `SpreadOption`으로 평탄화한다. MTJ 화면은 이 목록을 직접 소비하고, 선택 목록에는 `selectableSpreadOptions.filter { it.drawMode == SpreadDrawMode.Normal }`만 보여준다.

결과 화면은 먼저 실제 스프레드 좌표로 공간형 요약을 시도한다. 카드/라벨 충돌, 너무 작은 카드, 캔버스 과대, 라벨 가독 폭 부족, 글자 배율 확대 같은 조건에서 안전하지 않으면 순서형 목록으로 떨어진다. 미니 켈틱과 켈틱 크로스만 중앙 1-2번 카드의 의도적 겹침을 허용한다.

카드 선택 화면의 카드스프레드는 두 옵션을 유지한다. 기본은 먕타로와 같은 adaptive overview spread이고, rollout/옵션용 5열 모드는 78장을 `5 * 15 + 3` 행으로 고정한다. 즉 "먕타로랑 똑같이"와 "5열"을 별도 모드로 가져간다.

## 권위 소스와 소유권

권위 데이터:

- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/vendor/com/softcat/mystictarot/SpreadDefinitions.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/vendor/com/softcat/mystictarot/SpreadGeometry.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/vendor/com/softcat/mystictarot/Models.kt`

MTJ 어댑터/화면:

- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/main/java/com/hoscat/mtj/dev/TarotSelectionGridContract.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/main/java/com/hoscat/mtj/dev/TarotSpreadOverviewPolicy.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/main/java/com/hoscat/mtj/dev/TarotSpreadLabelFormatter.kt`

테스트:

- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/test/java/com/softcat/mystictarot/SpreadGeometryTest.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/test/java/com/hoscat/mtj/dev/TarotSelectionGridContractTest.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/test/java/com/hoscat/mtj/dev/TarotSpreadOverviewPolicyTest.kt`
- `/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android/app/src/test/java/com/hoscat/mtj/dev/TarotSpreadLabelFormatterTest.kt`

참조 핸드오프:

- `/Volumes/뽀그리/Project 먕/사로먕/handoff/from-myang/20260911-tarot-ui-reference/docs/08-tarot-deck-spread-contract.md`
- `/Volumes/뽀그리/Project 먕/사로먕/handoff/from-myang/20260911-tarot-ui-reference/docs/09-reading-state-machine-and-restoration.md`
- `/Volumes/뽀그리/Project 먕/사로먕/handoff/from-myang/20260911-tarot-ui-reference/source/com/softcat/mystictarot/SpreadDefinitions.kt`

커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3`에는 `android/app/src/main/java/com/hoscat/mtj/dev/TarotSpreadCategoryContract.kt`와 `TarotSpreadCategoryContractTest.kt`가 있으며, 8개 카테고리가 27개 일반 선택 스프레드를 정확히 한 번씩 덮는다는 계약을 제공한다.

## 데이터와 상태 계약

`SpreadOption.key`는 항상 `layoutId:presetId` 형식이다. `SpreadDefinitions.kt`의 `spreadKey(layoutId, presetId)`가 이 문자열을 만들며, 저장 상태와 화면 선택은 이 key를 기준으로 재조회해야 한다.

`SpreadOption.cardCount == SpreadOption.positionLabels.size`는 불변조건이다. 이 값이 어긋나면 결과 렌더러는 `DefinitionMismatch` 또는 ordered fallback으로 빠져야 하며, 임의로 라벨을 채우거나 버리면 안 된다.

`SpreadOption.layoutId`는 좌표 알고리즘의 기준이고, preset은 위치 의미 라벨의 기준이다. 같은 layout에 여러 preset이 있을 수 있으므로 UI/저장/로그에서 title만으로 스프레드를 식별하면 안 된다.

`SpreadDrawMode.FinalOneFromTen`은 일반 리딩 카테고리 27개에 포함하지 않는다. 신규 스프레드 선택 목록은 현재 `Normal` draw mode만 표시한다.

숨김 키는 정의에는 남아 있지만 신규 목록에서는 빠진다.

| 숨김 key | 이유/상태 |
|---|---|
| `hammer_nail:hammer_nail_flow` | 기존 특수 배열 보존, 신규 목록 비노출 |
| `six_cards:relationship` | 관계 특수 배열 보존, 신규 목록 비노출 |
| `yes_or_no:yes_no_signal` | 단정형 UX 위험 때문에 신규 목록 비노출 |
| `decision_v:decision_flow` | 통합형 실험 배열 보존, 신규 목록 비노출 |

## 8개 카테고리 계약

커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3` 기준 카테고리 계약은 다음과 같다.

| id | 표시명 | 부제 | layoutIds |
|---|---|---|---|
| `single` | 한 장형 | 1장 | `one_card` |
| `horizontal` | 가로형 | 2~3장 | `two_cards`, `three_cards` |
| `grid` | 격자형 | 4~10장 | `four_cards`, `six_cards`, `eight_cards`, `nine_cards`, `ten_cards` |
| `cross` | 십자형 | 5~10장 | `five_cross`, `mini_celtic`, `celtic_cross` |
| `v` | V형 | 5장 | `tarot_v` |
| `curve` | 곡선형 | 7~8장 | `horseshoe`, `magic_seven`, `crow_seven`, `crow_eight` |
| `circle` | 원형 | 8장 | `wheel_of_fortune` |
| `branches` | 두 갈래형 | 5~6장 | `relationship_clearing`, `either_or_five` |

카테고리 테스트의 기대값은 `8 categories`, `27 normal reading keys`, `each key exactly once`다. 새 layout을 추가하면 카테고리 누락 또는 중복이 가장 먼저 깨져야 한다.

## 27개 선택 가능 일반 스프레드

아래 목록은 `selectableSpreadOptions.filter { drawMode == Normal }` 기준이다.

| key | 표시 제목 | 카테고리 | 장수 | 위치 라벨 |
|---|---|---:|---:|---|
| `one_card:daily` | 오늘의 메시지 | 한 장형 | 1 | 오늘의 메시지 |
| `two_cards:now_next` | 현재 · 다음 | 가로형 | 2 | 현재, 다음 |
| `two_cards:choice_pair` | 선택 A · 선택 B | 가로형 | 2 | 선택 A, 선택 B |
| `three_cards:past_present_future` | 과거 · 현재 · 미래 | 가로형 | 3 | 과거, 현재, 미래 |
| `three_cards:situation_action_result` | 상황 · 행동 · 결과 | 가로형 | 3 | 상황, 행동, 결과 |
| `three_cards:problem_advice_result` | 문제 · 조언 · 결과 | 가로형 | 3 | 문제, 조언, 결과 |
| `three_cards:mind_body_spirit` | 마음 · 몸 · 영혼 | 가로형 | 3 | 마음, 몸, 영혼 |
| `four_cards:situation_obstacle_advice_result` | 상황 · 장애 · 조언 · 결과 | 격자형 | 4 | 상황, 장애, 조언, 결과 |
| `four_cards:mind_heart_body_action` | 생각 · 감정 · 몸 · 행동 | 격자형 | 4 | 생각, 감정, 몸, 행동 |
| `five_cross:core_flow` | 핵심 · 영향 · 장애 · 조언 · 결과 | 십자형 | 5 | 핵심, 영향, 장애, 조언, 결과 |
| `five_cross:relationship_dynamic` | 관계 다이나믹 | 십자형 | 5 | 나, 상대, 감정, 소통, 흐름 |
| `five_cross:shadow_work` | 섀도우 워크 | 십자형 | 5 | 드러난 감정, 숨은 감정, 저항, 회복, 통합 |
| `six_cards:career_path` | 커리어 패스 | 격자형 | 6 | 현재, 강점, 기회, 장애, 조언, 결과 |
| `six_cards:choice_compare` | 선택지 비교 | 격자형 | 6 | A 현재, A 장점, A 결과, B 현재, B 장점, B 결과 |
| `relationship_clearing:release_flow` | 관계 정리 및 마음 비워내기 | 두 갈래형 | 6 | 당신이 바라본 관계, 상대의 현재 마음, 버려야 할 것, 앞으로 열릴 가능성, 그럼에도 지켜야 할 것, 비우고 남길 것 |
| `horseshoe:horseshoe_flow` | 호스슈 | 곡선형 | 7 | 과거, 현재, 숨은 영향, 장애, 주변, 조언, 결과 |
| `eight_cards:wide_flow` | 8카드 배열법 | 격자형 | 8 | 핵심, 과거, 현재, 장애, 도움, 주변, 조언, 결과 |
| `magic_seven:magic_seven_flow` | 매직 세븐 배열법 | 곡선형 | 7 | 현재, 숨은 영향, 장애, 도움, 선택, 조언, 결과 |
| `wheel_of_fortune:wheel_flow` | 운명의 수레바퀴 | 원형 | 8 | 현재 축, 반복 패턴, 올라오는 기회, 내려놓을 것, 외부 영향, 내 선택, 전환점, 다음 흐름 |
| `crow_seven:crow_seven_flow` | 까마귀 스프레드 7장 | 곡선형 | 7 | 겉으로 보이는 것, 숨은 신호, 놓친 단서, 두려움, 도움, 주의, 결론 |
| `crow_eight:crow_eight_flow` | 까마귀 스프레드 8장 | 곡선형 | 8 | 현재 장면, 숨은 동기, 과거의 그림자, 가까운 변수, 먼 변수, 내가 볼 것, 피할 것, 결론 |
| `either_or_five:either_or_flow` | 양자택일 5장 | 두 갈래형 | 5 | 현재 상황, 선택 A, 선택 B, 숨은 변수, 최종 조언 |
| `tarot_v:v_flow` | 타로 V 스프레드 | V형 | 5 | 왼쪽 흐름, 왼쪽 영향, 선택 지점, 오른쪽 영향, 오른쪽 흐름 |
| `nine_cards:nine_grid` | 9칸 흐름 | 격자형 | 9 | 과거 마음, 현재 마음, 미래 마음, 과거 현실, 현재 현실, 미래 현실, 숨은 영향, 조언, 결과 |
| `mini_celtic:mini_celtic_cross` | 미니 켈틱 | 십자형 | 6 | 내면, 장애, 현재, 과거, 외면, 가까운 미래 |
| `celtic_cross:classic` | 클래식 켈틱 | 십자형 | 10 | 현재, 장애, 의식, 기반, 과거, 미래, 태도, 환경, 희망/두려움, 결과 |
| `ten_cards:ten_step` | 10단계 흐름 | 격자형 | 10 | 시작, 동기, 현재, 장애, 도움, 전환, 선택, 주변, 조언, 결과 |

## 좌표와 라벨 계약

`SpreadSlot(x, y, rotation)`은 논리 좌표다. 렌더러는 논리 좌표를 직접 dp로 쓰지 않고, 카드 크기와 gap을 계산한 뒤 `xStep`, `yStep`으로 변환한다.

특수 좌표를 가진 layout은 다음과 같다.

| layoutId | 규칙 |
|---|---|
| `celtic_cross` | 1번과 2번이 같은 중심 좌표를 공유하고 2번만 90도 회전한다. 우측 7~10번은 `x = 3.5` 세로열이다. |
| `mini_celtic` | 1번과 2번이 같은 중심 좌표를 공유하고 2번만 90도 회전한다. |
| `relationship_clearing` | 좌우 2열, 1번만 `-16도` 회전하고 y offset이 다르다. |
| `six_cards:relationship` | 숨김 key지만 관계 전용 다이아몬드형 좌표가 남아 있다. |
| `horseshoe` | 7장 말굽 곡선 좌표다. |
| `magic_seven` | 7장 계단/마름모형 좌표다. |
| `wheel_of_fortune` | 8장 원형 좌표다. |
| `crow_seven`, `crow_eight` | 상단 카드들에 -12도~12도 계열 회전이 있다. |
| `either_or_five`, `decision_v`, `tarot_v`, `hammer_nail` | 5장 특수 좌표다. |
| `ten_cards` | 5열 x 2행 고정 좌표다. |
| 그 외 `count == 5` | 기본 십자형 좌표다. |
| `count == 3` | 3장 가로 좌표다. |
| `count == 1` | 단일 중앙 좌표다. |
| 기타 | 4장 이하는 2열, 5장 이상은 3열 fallback 좌표다. |

라벨은 카드와 같은 index/order를 공유한다. `SavedReadingCard.order`는 1부터 시작하고, 결과 요약의 `placement.index`는 0부터 시작한다. 둘을 섞을 때는 `order == index + 1`을 유지해야 한다.

## 결과 렌더링 규칙

`calculateSpreadGeometry`는 입력 검증에 실패하면 `InvalidParameters`, 정의가 맞지 않으면 `DefinitionMismatch`, 주어진 폭에서 안전 배치가 불가능하면 `NoCollisionFreeLayout`을 반환한다. 화면은 실패 상태를 크래시로 다루지 말고 ordered fallback으로 처리해야 한다.

공간형 렌더링의 통과 조건은 다음이다.

- placement 수가 기대 카드 수와 같다.
- card/canvas 크기가 finite이고 `canvasExtent.width <= availableWidth`다.
- card width가 최소 40dp 이상이다.
- canvas height가 `min(1000dp, availableWidth * 4)` 이하이다.
- 라벨이 비어 있지 않고 자기 카드와 수평으로 겹치는 폭이 있다.
- 라벨과 카드 사이가 0 이상, 최대 24dp 이하이다.
- 라벨끼리 겹치지 않는다.
- 모든 라벨은 모든 회전 카드 bounds와 겹치지 않는다.
- 라벨 폭은 현재 typography에서 읽을 수 있는 최소 후보 폭 이상이다.
- 카드끼리 겹치는 것은 미니 켈틱/켈틱 크로스의 1-2번 중앙 pair만 허용한다.
- `fontScale >= 1.3f`에서는 ordered fallback을 우선한다.

회전 규칙은 `slotRotation + directionRotation`이다. 역방향은 180도를 추가하고, 결과는 0~360 범위로 normalize한다. 회전 카드의 ordered fallback은 대각선 길이 기반의 square reserve를 사용해 clipping을 막는다.

## 카드 선택 스프레드 옵션

카드 선택 화면은 두 가지 행 배치 계약을 유지한다.

| 모드 | 의미 | 행 규칙 |
|---|---|---|
| `TarotSelectionLayoutMode.MyangSpread` | 먕타로와 같은 기본 카드스프레드. 화면 가용 폭/높이에 맞춰 5~12열 중 실제 카드 폭이 가장 큰 spec을 선택하는 adaptive overview와 함께 운용한다. | 기본 `tarotSelectionRowSizes`에서는 8열 행 배치를 사용한다. overview 쪽은 `tarotOverviewGridSpec`가 5~12열을 탐색한다. |
| `TarotSelectionLayoutMode.FiveColumn` | rollout/옵션용 5열 카드스프레드. | 78장일 때 `List(15) { 5 } + listOf(3)`, 즉 16행, 총 78장이다. |

현재 `TarotScreen.kt`의 실제 선택 화면은 `tarotOverviewGridSpec(maxWidth, maxHeight, shuffledCards.size)`를 사용해 가용 공간 기반 adaptive grid를 그린다. 5열 모드는 계약과 테스트가 준비되어 있으므로 UI 토글/실험 플래그를 붙일 때는 `TarotSelectionLayoutMode`를 명시적으로 wiring해야 한다.

## reset 정책 권장

스프레드 선택, 질문, 카드 선택, 결과는 같은 리딩 session에 묶인다. 다음 동작에서는 session을 새로 만들어야 한다.

- 사용자가 스프레드 key를 바꿀 때
- 필요한 카드 수가 달라질 때
- 덱이 바뀌어 `shuffledCards`의 ID 집합이 바뀔 때
- 5열/먕타로 카드 선택 모드를 바꾸면서 이미 선택된 카드의 위치 의미가 혼란스러워질 때

다음 값은 가능하면 보존한다.

- 질문 draft
- 역방향 포함 여부
- 최근 선택한 spread key
- 저장된 기록 snapshot

중요한 원칙은 "스프레드 key가 상태의 anchor"라는 점이다. `title` 또는 index로 복원하지 않는다.

## 새 스프레드 추가 절차

1. `SpreadDefinitions.kt`에 `SpreadLayoutDefinition` 또는 기존 layout의 `PositionPreset`을 추가한다.
2. `key = layoutId:presetId`가 기존 key와 충돌하지 않는지 확인한다.
3. `cardCount`와 `positionLabels.size`를 맞춘다.
4. 공간 배치가 특별하면 `spreadSlots`에 layoutId 또는 key 기반 좌표를 추가한다.
5. 신규 목록에 숨길 실험/레거시 스프레드라면 `hiddenFromNewSpreadSelectionKeys`에 추가한다.
6. 신규 목록에 노출할 스프레드라면 8개 카테고리 중 하나에 layoutId를 추가한다.
7. `SpreadGeometryTest`로 320px/840px 폭, 긴 라벨, 켈틱 예외, invalid/fallback 상태를 검증한다.
8. `TarotSpreadCategoryContractTest`의 27 기대값을 의도한 새 총수로 갱신한다.
9. 선택 화면 mini preview, 결과 화면 spatial overview, ordered fallback을 Pixel 10급 compact width와 tablet width에서 확인한다.
10. 저장/복원 경로가 key 기반으로 돌아오는지 확인한다.

## 빌드와 테스트

권장 실행 위치:

```bash
cd '/Volumes/뽀그리/Project 먕/사로먕/work/mtj-dedicated-cell/android'
```

핵심 단위 테스트:

```bash
./gradlew testDebugUnitTest --tests 'com.softcat.mystictarot.SpreadGeometryTest'
./gradlew testDebugUnitTest --tests 'com.hoscat.mtj.dev.TarotSelectionGridContractTest'
./gradlew testDebugUnitTest --tests 'com.hoscat.mtj.dev.TarotSpreadOverviewPolicyTest'
./gradlew testDebugUnitTest --tests 'com.hoscat.mtj.dev.TarotSpreadLabelFormatterTest'
```

카테고리 계약 테스트는 커밋 `b4ffe8718bf01b02930468451baa4ecab45f1db3`의 `TarotSpreadCategoryContractTest`를 기준으로 한다. 현재 작업 tree에 해당 파일이 없으면 먼저 파일 존재 여부와 병합 상태를 확인한다.

빠른 회귀 빌드:

```bash
./gradlew assembleDebug testDebugUnitTest
```

수동 확인:

- 신규 리딩 시작 화면에서 27개 일반 스프레드만 노출되는지 확인한다.
- `망치와 못`, `관계 배열법`, `Yes or No`, `망치와 못 · 관계 · Yes or No`가 신규 목록에 재노출되지 않는지 확인한다.
- 1장, 5장 십자형, 미니 켈틱, 켈틱 크로스, 운명의 수레바퀴를 결과 화면에서 확인한다.
- 글자 크기 확대 상태에서 spatial이 무리하게 유지되지 않고 ordered fallback으로 전환되는지 확인한다.
- 카드 선택 화면에서 기본 먕타로 adaptive spread와 5열 옵션이 둘 다 78장을 잃지 않는지 확인한다.

## 인수 기준

- 일반 신규 선택 목록은 27개 key를 포함하고, 각 key는 8개 카테고리 중 정확히 하나에 속한다.
- 모든 `SpreadOption`은 `cardCount == positionLabels.size`를 만족한다.
- 모든 결과 요약은 spatial 또는 ordered fallback 중 하나로 안전하게 렌더링된다.
- 미니 켈틱/켈틱 크로스 외 layout에서 카드 겹침이 발생하면 spatial을 포기한다.
- 라벨은 카드와 충돌하지 않고, 라벨끼리 충돌하지 않으며, 순서/position/card/direction을 보존한다.
- 카드 선택 모드는 기본 먕타로형과 5열형을 독립적으로 선택할 수 있고, 두 모드 모두 78장 총합을 보존한다.
- 스프레드 key 변경 시 선택 카드 상태가 새 장수/라벨 계약에 맞게 reset된다.
- 저장/복원은 key와 card ID 기반으로 동작하며 title/index 기반으로 의존하지 않는다.

## 알려진 제한과 후속 작업

P0:

- 현재 작업 tree에서 `TarotSpreadCategoryContract.kt` 파일 존재 여부가 commit evidence와 다르다. 병합 전에 카테고리 파일이 실제 앱 소스에 들어오는지 확인해야 한다.
- 5열 카드 선택 모드는 계약/테스트가 있으나 현재 `TarotScreen.kt` 선택 UI는 adaptive overview를 직접 사용한다. 실제 제품 옵션으로 노출하려면 UI 상태, 저장 정책, reset 정책을 연결해야 한다.

P1:

- `FinalOneFromTen`은 일반 27개에 포함하지 않았지만 selectable 필터에는 drawMode 기준을 같이 써야 안전하다. 목록을 만지는 코드는 항상 `drawMode == Normal` 조건을 명시한다.
- 카테고리 기대 총수 27은 신규 스프레드가 들어갈 때 의도적으로 깨지는 테스트다. 단순히 숫자만 바꾸지 말고 카테고리 누락/중복을 먼저 확인한다.
- 관계/YesNo/망치 계열 숨김 스프레드는 기록 복원 또는 deep link를 통해 들어올 수 있으므로 정의 삭제 대신 비노출 유지가 안전하다.
- 긴 한국어 라벨, `fontScale >= 1.3f`, 작은 화면, tablet 폭을 포함한 screenshot evidence가 추가로 필요하다.

P2:

- 위치 라벨 커스터마이징이 다시 들어오면 `customPositionLabelsBySpreadKey`와 geometry/fallback 조건을 함께 테스트해야 한다.
- mini preview는 작은 카드 숫자만 보여주므로 복잡한 layout의 실제 결과와 차이가 생길 수 있다. UX 이슈가 있으면 preview도 geometry 계산 기반으로 통일한다.

## 증거 경로와 해시

검토한 커밋:

- `b4ffe8718bf01b02930468451baa4ecab45f1db3`
- Author/Commit: `mmmg0820`
- Date: `2026-09-11T09:25:24+09:00`
- Subject: `Move Saju calculation metadata into localized details`

소스 SHA-256:

| 파일 | SHA-256 |
|---|---|
| `vendor/com/softcat/mystictarot/SpreadDefinitions.kt` | `00d741adb15988338dbe9c54a6b2922e318e0b7a50f4370051bfc2e2271a9f90` |
| `vendor/com/softcat/mystictarot/SpreadGeometry.kt` | `3ee0865555e706161114fc1cad982a42b1375822318f8da1ae3c5794283f04d5` |
| `vendor/com/softcat/mystictarot/Models.kt` | `af1317e6ba73b092a9b118a35877169b7844cd3b3811770bd86c56e4073f6cc7` |
| `app/src/main/java/com/hoscat/mtj/dev/TarotSelectionGridContract.kt` | `b7f84428bfd03a7b25a4a60c26c951f55a748113aae9e78378beacc6e2c29090` |
| `app/src/main/java/com/hoscat/mtj/dev/TarotSpreadOverviewPolicy.kt` | `36275d310008aa9afdcae6c3ff317d0deb9c0db58c7bfe9bdfae318ad6311cf0` |
| `app/src/main/java/com/hoscat/mtj/dev/TarotScreen.kt` | `73bd2b7fd6c32166b3af3af840a62f2c211be4e2b14a00d66148fa1297bce348` |

MyangTarot handoff SHA-256:

| 파일 | SHA-256 |
|---|---|
| `from-myang/.../source/com/softcat/mystictarot/SpreadDefinitions.kt` | `00d741adb15988338dbe9c54a6b2922e318e0b7a50f4370051bfc2e2271a9f90` |
| `from-myang/.../docs/08-tarot-deck-spread-contract.md` | `09b3f31b50745bb26cca526ad3e7b947352680eabb488972451246c583198df4` |
| `from-myang/.../docs/09-reading-state-machine-and-restoration.md` | `51389e3245d0f89b0bf5ccf9d9f532eb82559b41cd6f0138659c60c58385afd7` |

기존 통합 evidence:

- `/Volumes/뽀그리/Project 먕/사로먕/evidence/final-integration-tar-board-20260911/tar-board-source-build-summary.json`
- `/Volumes/뽀그리/Project 먕/사로먕/evidence/final-integration-tar-board-20260911/tar2-h04-t02-t03-implementation-handoff-20260911.md`
- `/Volumes/뽀그리/Project 먕/사로먕/evidence/final-integration-tar-board-20260911/and00-tar2-isolated-patch-handoff-20260911.md`

## 인수인계 메모

- 이 문서는 소스 코드를 수정하지 않는다. 실제 변경은 MTJ Android worktree에서 별도 PR/patch로 진행한다.
- vendor `com.softcat.mystictarot` 파일은 MyangTarot 계약과 SHA가 맞는지 먼저 확인하고 수정한다.
- `TarotScreen.kt`는 UI 조합이 많으므로 스프레드 데이터 변경, 카드 선택 행 변경, 결과 요약 변경을 한 PR에 섞지 않는 편이 좋다.
- 카드 선택 모드 토글을 넣을 때는 "기본: 먕타로형, 옵션: 5열"을 명확히 유지한다. 5열을 기본값으로 바꾸려면 디자인 TF 승인이 필요하다.
- 숨김 스프레드는 삭제하지 말고 비노출 정책으로 관리한다. 저장 기록이나 구버전 상태 복원을 위해 key가 살아 있는 편이 안전하다.

## 새 유지보수자 첫날 체크리스트

- `SpreadDefinitions.kt`에서 27개 일반 key와 4개 숨김 key를 눈으로 확인한다.
- `TarotSelectionGridContract.kt`에서 `MyangSpread`와 `FiveColumn` 두 모드가 모두 살아 있는지 확인한다.
- `TarotScreen.kt`의 신규 스프레드 목록이 `selectableSpreadOptions`와 `drawMode == Normal`을 함께 쓰는지 확인한다.
- 미니 켈틱/켈틱 크로스 결과 화면에서 1번/2번 중앙 crossing이 보이고 라벨이 겹치지 않는지 확인한다.
- 1장 결과에서 카드가 과하게 커지지 않고 `singleCardMaximumWidth = 180dp` 정책이 유지되는지 확인한다.
- `./gradlew testDebugUnitTest --tests 'com.softcat.mystictarot.SpreadGeometryTest'`를 먼저 돌린다.
- 5열 카드 선택 옵션을 만졌다면 `TarotSelectionGridContractTest`의 `5 * 15 + 3` 계약을 먼저 확인한다.
- 카테고리 UI를 만졌다면 8개 카테고리/27개 key/중복 없음 계약을 확인한다.
- 저장/복원 테스트에서 spread key, card ID, order, positionLabel이 살아 있는지 확인한다.
- 디자인 TF 지시가 새로 오면 이 README의 "카드 선택 스프레드 옵션"과 "reset 정책 권장" 섹션을 먼저 갱신한다.
