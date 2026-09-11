package com.softcat.mystictarot

internal data class SpreadSlot(
    val x: Float,
    val y: Float,
    val rotation: Float = 0f
)

internal data class PositionPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val labels: List<String>
)

internal data class SpreadLayoutDefinition(
    val id: String,
    val title: String,
    val subtitle: String,
    val cardCount: Int,
    val drawMode: SpreadDrawMode = SpreadDrawMode.Normal,
    val presets: List<PositionPreset>
)

internal fun spreadKey(layoutId: String, presetId: String): String = "$layoutId:$presetId"

internal fun spreadSlots(spread: SpreadOption, count: Int): List<SpreadSlot> {
    if (spread.layoutId == "celtic_cross" && count >= 10) {
        return listOf(
            SpreadSlot(1f, 1f),
            SpreadSlot(1f, 1f, 90f),
            SpreadSlot(1f, 0f),
            SpreadSlot(1f, 2f),
            SpreadSlot(0f, 1f),
            SpreadSlot(2f, 1f),
            SpreadSlot(3.5f, 3f),
            SpreadSlot(3.5f, 2f),
            SpreadSlot(3.5f, 1f),
            SpreadSlot(3.5f, 0f)
        )
    }
    if (spread.layoutId == "mini_celtic" && count >= 6) {
        return listOf(
            SpreadSlot(1f, 1f),
            SpreadSlot(1f, 1f, 90f),
            SpreadSlot(1f, 2f),
            SpreadSlot(0f, 1f),
            SpreadSlot(1f, 0f),
            SpreadSlot(2f, 1f)
        )
    }
    if (spread.layoutId == "relationship_clearing" && count >= 6) {
        return listOf(
            SpreadSlot(0f, 0.25f, -16f),
            SpreadSlot(2f, 0f),
            SpreadSlot(0f, 1.25f),
            SpreadSlot(2f, 1.25f),
            SpreadSlot(0f, 2.5f),
            SpreadSlot(2f, 2.5f)
        )
    }
    if (spread.key == spreadKey("six_cards", "relationship") && count >= 6) {
        return listOf(
            SpreadSlot(0f, 0f),
            SpreadSlot(2f, 0f),
            SpreadSlot(1f, 1f),
            SpreadSlot(0f, 2f),
            SpreadSlot(2f, 2f),
            SpreadSlot(1f, 3f)
        )
    }
    if (spread.layoutId == "horseshoe" && count >= 7) {
        return listOf(
            SpreadSlot(0f, 2.2f),
            SpreadSlot(1f, 1.2f),
            SpreadSlot(2f, 0.35f),
            SpreadSlot(3f, 0f),
            SpreadSlot(4f, 0.35f),
            SpreadSlot(5f, 1.2f),
            SpreadSlot(6f, 2.2f)
        )
    }
    if (spread.layoutId == "magic_seven" && count >= 7) {
        return listOf(
            SpreadSlot(1.5f, 0f),
            SpreadSlot(0.5f, 0.85f),
            SpreadSlot(2.5f, 0.85f),
            SpreadSlot(0f, 1.95f),
            SpreadSlot(3f, 1.95f),
            SpreadSlot(0.8f, 3f),
            SpreadSlot(2.2f, 3f)
        )
    }
    if (spread.layoutId == "wheel_of_fortune" && count >= 8) {
        return listOf(
            SpreadSlot(1.5f, 0f),
            SpreadSlot(2.6f, 0.45f),
            SpreadSlot(3f, 1.5f),
            SpreadSlot(2.6f, 2.55f),
            SpreadSlot(1.5f, 3f),
            SpreadSlot(0.4f, 2.55f),
            SpreadSlot(0f, 1.5f),
            SpreadSlot(0.4f, 0.45f)
        )
    }
    if (spread.layoutId == "crow_seven" && count >= 7) {
        return listOf(
            SpreadSlot(0f, 0.15f, -10f),
            SpreadSlot(1f, 0.8f, -6f),
            SpreadSlot(2f, 1.25f),
            SpreadSlot(3f, 0.8f, 6f),
            SpreadSlot(4f, 0.15f, 10f),
            SpreadSlot(1.4f, 2.45f),
            SpreadSlot(2.6f, 2.45f)
        )
    }
    if (spread.layoutId == "crow_eight" && count >= 8) {
        return listOf(
            SpreadSlot(0f, 0.25f, -12f),
            SpreadSlot(1f, 0.95f, -7f),
            SpreadSlot(2f, 1.35f),
            SpreadSlot(3f, 0.95f, 7f),
            SpreadSlot(4f, 0.25f, 12f),
            SpreadSlot(0.8f, 2.55f),
            SpreadSlot(2f, 3f),
            SpreadSlot(3.2f, 2.55f)
        )
    }
    if (spread.layoutId == "either_or_five" && count >= 5) {
        return listOf(
            SpreadSlot(1f, 0f),
            SpreadSlot(0f, 1.2f),
            SpreadSlot(2f, 1.2f),
            SpreadSlot(0f, 2.4f),
            SpreadSlot(2f, 2.4f)
        )
    }
    if (spread.layoutId == "decision_v" && count >= 5) {
        return listOf(
            SpreadSlot(0f, 0f, -8f),
            SpreadSlot(1f, 1f, -4f),
            SpreadSlot(2f, 2f),
            SpreadSlot(3f, 1f, 4f),
            SpreadSlot(4f, 0f, 8f)
        )
    }
    if (spread.layoutId == "tarot_v" && count >= 5) {
        return listOf(
            SpreadSlot(0f, 0f, -10f),
            SpreadSlot(1f, 0.9f, -6f),
            SpreadSlot(2f, 1.8f),
            SpreadSlot(3f, 0.9f, 6f),
            SpreadSlot(4f, 0f, 10f)
        )
    }
    if (spread.layoutId == "hammer_nail" && count >= 5) {
        return listOf(
            SpreadSlot(1f, 0f),
            SpreadSlot(0f, 1f),
            SpreadSlot(1f, 1f),
            SpreadSlot(2f, 1f),
            SpreadSlot(1f, 2f)
        )
    }
    if (count == 5) {
        return listOf(
            SpreadSlot(1f, 1f),
            SpreadSlot(1f, 0f),
            SpreadSlot(2f, 1f),
            SpreadSlot(1f, 2f),
            SpreadSlot(0f, 1f)
        )
    }
    if (spread.layoutId == "ten_cards" && count >= 10) {
        return List(10) { index ->
            SpreadSlot((index % 5).toFloat(), (index / 5).toFloat())
        }
    }
    if (count == 3) {
        return listOf(SpreadSlot(0f, 0f), SpreadSlot(1f, 0f), SpreadSlot(2f, 0f))
    }
    if (count == 1) {
        return listOf(SpreadSlot(0f, 0f))
    }
    return List(count) { index ->
        val columns = if (count <= 4) 2 else 3
        SpreadSlot((index % columns).toFloat(), (index / columns).toFloat())
    }
}

internal val spreadLayouts = listOf(
    SpreadLayoutDefinition(
        id = "one_card",
        title = "1장 배열",
        subtitle = "가장 빠른 오늘의 메시지",
        cardCount = 1,
        presets = listOf(
            PositionPreset("daily", "오늘의 메시지", "오늘 가장 먼저 볼 흐름", listOf("오늘의 메시지"))
        )
    ),
    SpreadLayoutDefinition(
        id = "final_one_from_ten",
        title = "최종 1장 뽑기",
        subtitle = "후보 10장 중 마지막 한 장 선택",
        cardCount = 1,
        drawMode = SpreadDrawMode.FinalOneFromTen,
        presets = listOf(
            PositionPreset("final", "최종 메시지", "마지막에 남는 핵심 신호", listOf("최종 메시지"))
        )
    ),
    SpreadLayoutDefinition(
        id = "two_cards",
        title = "2장 배열",
        subtitle = "두 흐름을 조용히 비교",
        cardCount = 2,
        presets = listOf(
            PositionPreset("now_next", "현재 · 다음", "지금과 다음 흐름", listOf("현재", "다음")),
            PositionPreset("choice_pair", "선택 A · 선택 B", "두 선택지를 나란히 보기", listOf("선택 A", "선택 B"))
        )
    ),
    SpreadLayoutDefinition(
        id = "three_cards",
        title = "3장 배열",
        subtitle = "빠르게 흐름을 나누어 보기",
        cardCount = 3,
        presets = listOf(
            PositionPreset("past_present_future", "과거 · 현재 · 미래", "시간의 흐름으로 보기", listOf("과거", "현재", "미래")),
            PositionPreset("situation_action_result", "상황 · 행동 · 결과", "지금 할 일 중심", listOf("상황", "행동", "결과")),
            PositionPreset("problem_advice_result", "문제 · 조언 · 결과", "막힌 지점 풀기", listOf("문제", "조언", "결과")),
            PositionPreset("mind_body_spirit", "마음 · 몸 · 영혼", "내면 균형 점검", listOf("마음", "몸", "영혼"))
        )
    ),
    SpreadLayoutDefinition(
        id = "four_cards",
        title = "4장 배열",
        subtitle = "상황을 네 방향으로 정리",
        cardCount = 4,
        presets = listOf(
            PositionPreset("situation_obstacle_advice_result", "상황 · 장애 · 조언 · 결과", "핵심 흐름을 압축해서 보기", listOf("상황", "장애", "조언", "결과")),
            PositionPreset("mind_heart_body_action", "생각 · 감정 · 몸 · 행동", "내 상태를 네 층위로 점검", listOf("생각", "감정", "몸", "행동"))
        )
    ),
    SpreadLayoutDefinition(
        id = "five_cross",
        title = "5장 배열",
        subtitle = "핵심을 중심으로 조금 넓게 보기",
        cardCount = 5,
        presets = listOf(
            PositionPreset("core_flow", "핵심 · 영향 · 장애 · 조언 · 결과", "상황 분석 기본형", listOf("핵심", "영향", "장애", "조언", "결과")),
            PositionPreset("relationship_dynamic", "관계 다이나믹", "감정과 소통의 패턴", listOf("나", "상대", "감정", "소통", "흐름")),
            PositionPreset("shadow_work", "섀도우 워크", "숨은 감정과 회복 포인트", listOf("드러난 감정", "숨은 감정", "저항", "회복", "통합"))
        )
    ),
    SpreadLayoutDefinition(
        id = "six_cards",
        title = "6장 배열",
        subtitle = "관계, 일, 선택지를 비교하기",
        cardCount = 6,
        presets = listOf(
            PositionPreset("relationship", "관계 배열법", "나와 상대, 관계 흐름", listOf("나", "상대", "관계", "욕구", "장애", "흐름")),
            PositionPreset("career_path", "커리어 패스", "일과 기회의 방향", listOf("현재", "강점", "기회", "장애", "조언", "결과")),
            PositionPreset("choice_compare", "선택지 비교", "A와 B의 장단점 비교", listOf("A 현재", "A 장점", "A 결과", "B 현재", "B 장점", "B 결과"))
        )
    ),
    SpreadLayoutDefinition(
        id = "relationship_clearing",
        title = "관계 정리",
        subtitle = "마음을 비우고 남길 것을 보기",
        cardCount = 6,
        presets = listOf(
            PositionPreset(
                "release_flow",
                "관계 정리 및 마음 비워내기",
                "관계의 집착과 가능성을 분리해서 보기",
                listOf("당신이 바라본 관계", "상대의 현재 마음", "버려야 할 것", "앞으로 열릴 가능성", "그럼에도 지켜야 할 것", "비우고 남길 것")
            )
        )
    ),
    SpreadLayoutDefinition(
        id = "horseshoe",
        title = "호스슈 배열",
        subtitle = "7장 말굽형 흐름 읽기",
        cardCount = 7,
        presets = listOf(
            PositionPreset("horseshoe_flow", "호스슈", "과거부터 조언까지", listOf("과거", "현재", "숨은 영향", "장애", "주변", "조언", "결과"))
        )
    ),
    SpreadLayoutDefinition(
        id = "eight_cards",
        title = "8장 배열",
        subtitle = "상황과 주변 흐름을 넓게 보기",
        cardCount = 8,
        presets = listOf(
            PositionPreset("wide_flow", "8카드 배열법", "핵심부터 결과까지 단계별 보기", listOf("핵심", "과거", "현재", "장애", "도움", "주변", "조언", "결과"))
        )
    ),
    SpreadLayoutDefinition(
        id = "magic_seven",
        title = "매직 세븐",
        subtitle = "7장의 흐름을 단계별로 읽기",
        cardCount = 7,
        presets = listOf(
            PositionPreset("magic_seven_flow", "매직 세븐 배열법", "핵심 흐름과 조언", listOf("현재", "숨은 영향", "장애", "도움", "선택", "조언", "결과"))
        )
    ),
    SpreadLayoutDefinition(
        id = "wheel_of_fortune",
        title = "운명의 수레바퀴",
        subtitle = "순환과 전환점을 읽기",
        cardCount = 8,
        presets = listOf(
            PositionPreset("wheel_flow", "운명의 수레바퀴", "반복되는 흐름과 전환", listOf("현재 축", "반복 패턴", "올라오는 기회", "내려놓을 것", "외부 영향", "내 선택", "전환점", "다음 흐름"))
        )
    ),
    SpreadLayoutDefinition(
        id = "yes_or_no",
        title = "Yes or No",
        subtitle = "가능성이 기우는 방향 보기",
        cardCount = 5,
        presets = listOf(
            PositionPreset("yes_no_signal", "Yes or No", "단정 대신 가능성과 조건 보기", listOf("질문 핵심", "Yes 신호", "No 신호", "조건", "조언"))
        )
    ),
    SpreadLayoutDefinition(
        id = "hammer_nail",
        title = "망치와 못",
        subtitle = "문제와 해결 방식을 분리하기",
        cardCount = 5,
        presets = listOf(
            PositionPreset("hammer_nail_flow", "망치와 못", "걸림돌과 실행 조언", listOf("박힌 문제", "반복 원인", "사용할 힘", "주의점", "실행 조언"))
        )
    ),
    SpreadLayoutDefinition(
        id = "crow_seven",
        title = "까마귀 7장",
        subtitle = "숨은 신호와 경고를 보기",
        cardCount = 7,
        presets = listOf(
            PositionPreset("crow_seven_flow", "까마귀 스프레드 7장", "보이는 것과 숨은 신호", listOf("겉으로 보이는 것", "숨은 신호", "놓친 단서", "두려움", "도움", "주의", "결론"))
        )
    ),
    SpreadLayoutDefinition(
        id = "crow_eight",
        title = "까마귀 8장",
        subtitle = "관찰과 선택을 넓게 보기",
        cardCount = 8,
        presets = listOf(
            PositionPreset("crow_eight_flow", "까마귀 스프레드 8장", "숨은 흐름과 선택", listOf("현재 장면", "숨은 동기", "과거의 그림자", "가까운 변수", "먼 변수", "내가 볼 것", "피할 것", "결론"))
        )
    ),
    SpreadLayoutDefinition(
        id = "either_or_five",
        title = "양자택일 5장",
        subtitle = "두 선택지와 최종 조언",
        cardCount = 5,
        presets = listOf(
            PositionPreset("either_or_flow", "양자택일 5장", "A와 B를 비교하고 조언 받기", listOf("현재 상황", "선택 A", "선택 B", "숨은 변수", "최종 조언"))
        )
    ),
    SpreadLayoutDefinition(
        id = "decision_v",
        title = "결정 V",
        subtitle = "망치와 못 · 관계 · Yes or No 통합",
        cardCount = 5,
        presets = listOf(
            PositionPreset(
                "decision_flow",
                "망치와 못 · 관계 · Yes or No",
                "걸림돌, 관계 변수, 가능성 신호를 한 번에 보기",
                listOf("핵심 질문", "박힌 문제", "관계 변수", "가능성 신호", "실행 조언")
            )
        )
    ),
    SpreadLayoutDefinition(
        id = "tarot_v",
        title = "타로 V 스프레드",
        subtitle = "두 흐름이 한 지점으로 모이는 선택형 배열",
        cardCount = 5,
        presets = listOf(
            PositionPreset(
                "v_flow",
                "타로 V 스프레드",
                "왼쪽 흐름과 오른쪽 흐름을 비교해 결론 보기",
                listOf("왼쪽 흐름", "왼쪽 영향", "선택 지점", "오른쪽 영향", "오른쪽 흐름")
            )
        )
    ),
    SpreadLayoutDefinition(
        id = "nine_cards",
        title = "9장 배열",
        subtitle = "마음과 현실을 격자로 정리",
        cardCount = 9,
        presets = listOf(
            PositionPreset("nine_grid", "9칸 흐름", "내면과 현실, 결과를 함께 보기", listOf("과거 마음", "현재 마음", "미래 마음", "과거 현실", "현재 현실", "미래 현실", "숨은 영향", "조언", "결과"))
        )
    ),
    SpreadLayoutDefinition(
        id = "mini_celtic",
        title = "미니 켈틱",
        subtitle = "켈틱 크로스를 6장으로 압축",
        cardCount = 6,
        presets = listOf(
            PositionPreset(
                "mini_celtic_cross",
                "미니 켈틱",
                "현재와 장애, 과거와 가까운 미래를 빠르게 보기",
                listOf("내면", "장애", "현재", "과거", "외면", "가까운 미래")
            )
        )
    ),
    SpreadLayoutDefinition(
        id = "celtic_cross",
        title = "켈틱 크로스",
        subtitle = "가장 유명한 10장 심층 배열",
        cardCount = 10,
        presets = listOf(
            PositionPreset("classic", "클래식 켈틱", "현재와 결과를 깊게 보기", listOf("현재", "장애", "의식", "기반", "과거", "미래", "태도", "환경", "희망/두려움", "결과"))
        )
    ),
    SpreadLayoutDefinition(
        id = "ten_cards",
        title = "10장 배열",
        subtitle = "10장의 흐름을 순서대로 보기",
        cardCount = 10,
        presets = listOf(
            PositionPreset("ten_step", "10단계 흐름", "시작부터 마무리까지 순서대로 보기", listOf("시작", "동기", "현재", "장애", "도움", "전환", "선택", "주변", "조언", "결과"))
        )
    )
)

internal val spreadOptions = spreadLayouts.flatMap { layout ->
    layout.presets.map { preset ->
        SpreadOption(
            key = spreadKey(layout.id, preset.id),
            title = preset.title,
            subtitle = preset.subtitle,
            cardCount = layout.cardCount,
            drawMode = layout.drawMode,
            layoutId = layout.id,
            layoutTitle = layout.title,
            layoutSubtitle = layout.subtitle,
            positionPresetTitle = preset.title,
            positionLabels = preset.labels
        )
    }
}

internal val hiddenFromNewSpreadSelectionKeys = setOf(
    spreadKey("hammer_nail", "hammer_nail_flow"),
    spreadKey("six_cards", "relationship"),
    spreadKey("yes_or_no", "yes_no_signal"),
    spreadKey("decision_v", "decision_flow")
)

internal fun SpreadOption.isSelectableSpreadOption(): Boolean {
    return key !in hiddenFromNewSpreadSelectionKeys
}

internal val selectableSpreadOptions = spreadOptions.filter { it.isSelectableSpreadOption() }

internal fun compatiblePositionPresets(
    spread: SpreadOption,
    options: List<SpreadOption> = selectableSpreadOptions
): List<SpreadOption> = options
    .filter {
        it.layoutId == spread.layoutId &&
            it.cardCount == spread.cardCount &&
            it.drawMode == spread.drawMode
    }
    .distinctBy { it.key }
