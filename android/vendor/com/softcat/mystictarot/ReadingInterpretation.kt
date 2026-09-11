package com.softcat.mystictarot

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun buildSpreadLlmPrompt(
    spread: SpreadOption,
    question: String,
    drawnCards: List<DrawnCard>,
    deckAiPrompt: String = ""
): String {
    val trimmedQuestion = question.trim()
    val questionLine = trimmedQuestion.takeIf { it.isNotBlank() }?.let {
        "사용자 질문: $it"
    }.orEmpty()
    val cardLines = drawnCards.joinToString("\n") { drawnCard ->
        val position = positionMeaning(spread, drawnCard.order)
        "- ${drawnCard.order}. 위치: $position / 카드: ${drawnCard.card.nameKr} (${drawnCard.card.nameEn}) / 방향: ${drawnCard.direction.label} / 방향별 의미: ${directionalMeaningSnapshot(drawnCard.card, drawnCard.direction)}"
    }
    val deckPromptBlock = deckAiPrompt.trim().takeIf { it.isNotBlank() }?.let {
        """

[덱 전용 해석 원칙]
$it
""".trimEnd()
    }.orEmpty()
    return """
당신은 차분하고 통찰력 있는 한국어 타로 리더입니다.
아래 타로 스프레드를 바탕으로 사용자가 바로 이해할 수 있는 자연스러운 해석을 작성하세요.

[해석 원칙]
- 단정적인 예언처럼 말하지 말고, 카드가 보여주는 경향과 선택지를 중심으로 설명하세요.
- 카드 하나씩 따로 읽은 뒤, 마지막에 스프레드 전체 흐름을 통합하세요.
- 역방향은 무조건 나쁘게 보지 말고, 지연·과잉·내면화·재조정의 관점도 고려하세요.
- 현실적인 조언을 포함하되 의료, 법률, 투자 등 전문 판단이 필요한 영역은 단정하지 마세요.
- 말투는 신비롭지만 과장하지 말고, 따뜻하고 선명한 한국어로 작성하세요.
$deckPromptBlock

[스프레드]
배열: ${spread.layoutTitle}
	위치 의미: ${spread.positionPresetTitle}
	설명: ${spread.layoutSubtitle}
	카드 수: ${drawnCards.size}
	$questionLine

	[뽑힌 카드]
$cardLines

[출력 형식]
1. 전체 분위기 요약: 3~5문장
2. 위치별 카드 해석: 각 카드마다 위치 의미, 카드명, 방향, 해석, 조언
3. 카드 사이의 연결: 서로 강화하거나 충돌하는 흐름
4. 지금 가장 중요한 조언: 사용자가 오늘 바로 실천할 수 있는 문장 3개
5. 한 줄 메시지: 짧고 기억하기 쉬운 결론
""".trimIndent()
}

internal fun buildSavedReading(
    spread: SpreadOption,
    question: String,
    drawnCards: List<DrawnCard>,
    deck: TarotDeck
): SavedReading {
    val now = System.currentTimeMillis()
    val firstCard = drawnCards.firstOrNull()?.card?.nameKr ?: "리딩"
    val trimmedQuestion = question.trim()
    val title = "${spread.positionPresetTitle} · $firstCard"
    return SavedReading(
        id = now,
        savedAt = now,
        title = title,
        spreadTitle = spread.positionPresetTitle,
        layoutTitle = spread.layoutTitle,
        positionPresetTitle = spread.positionPresetTitle,
        question = trimmedQuestion,
        interpretation = buildLocalReadingInterpretation(spread, trimmedQuestion, drawnCards),
        deckId = deck.id,
        deckName = deck.name,
        deckAiPromptSnapshot = deck.aiPrompt,
        cards = drawnCards.map { drawnCard ->
            SavedReadingCard(
                order = drawnCard.order,
                nameKr = drawnCard.card.nameKr,
                nameEn = drawnCard.card.nameEn,
                directionLabel = directionText(drawnCard.direction),
                positionLabel = positionMeaning(spread, drawnCard.order),
                meaningSnapshot = directionalMeaningSnapshot(drawnCard.card, drawnCard.direction),
                cardId = drawnCard.card.id,
                imageUri = drawnCard.card.imageUri
            )
        }
    )
}

internal fun displaySavedReadingInterpretation(reading: SavedReading): String {
    val intro = buildString {
        append("${reading.layoutTitle}의 ${reading.positionPresetTitle} 흐름으로 저장한 리딩입니다.")
        if (reading.question.isNotBlank()) append("\n질문: ${reading.question}")
    }
    val cardNotes = reading.cards.joinToString("\n") { card ->
        val position = card.positionLabel.ifBlank { "${card.order}번째 위치" }
        val meaning = displaySavedCardMeaning(card).ifBlank { card.nameEn }
        "${card.order}. $position · ${card.nameKr} ${card.directionLabel}: $meaning"
    }
    return listOf(intro, cardNotes)
        .filter { it.isNotBlank() }
        .joinToString("\n\n")
}

internal fun displaySavedCardMeaning(card: SavedReadingCard): String {
    if (card.meaningSnapshot.trim() == DEFAULT_CUSTOM_CARD_MEANING) return ""
    return filterMeaningForDirection(card.meaningSnapshot, card.directionLabel)
}

internal fun formatReadingDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(Date(timestamp))
}

internal fun positionMeaning(spread: SpreadOption, order: Int): String {
    return spread.positionLabels.getOrNull(order - 1) ?: "${order}번째 위치"
}

private fun buildLocalReadingInterpretation(spread: SpreadOption, question: String, drawnCards: List<DrawnCard>): String {
    val intro = buildString {
        append("${spread.layoutTitle}의 ${spread.positionPresetTitle} 흐름으로 저장한 리딩입니다.")
        if (question.isNotBlank()) append("\n질문: $question")
    }
    val cardNotes = drawnCards.joinToString("\n") { drawnCard ->
        val position = positionMeaning(spread, drawnCard.order)
        "${drawnCard.order}. $position · ${drawnCard.card.nameKr} ${drawnCard.direction.label}: ${directionalMeaningSnapshot(drawnCard.card, drawnCard.direction)}"
    }
    return "$intro\n\n$cardNotes"
}

internal fun directionalMeaningSnapshot(card: TarotCard, direction: CardDirection): String {
    if (!hasDisplayableCardMeaning(card)) return ""
    return filterMeaningForDirection(card.basicMeaning, direction.label)
}

private fun filterMeaningForDirection(meaning: String, directionLabel: String): String {
    val reversed = directionLabel.contains("역방향")
    return meaning
        .lineSequence()
        .map { it.trimEnd() }
        .filterNot { line ->
            when {
                reversed -> line.startsWith("정방향 키워드:")
                else -> line.startsWith("역방향 키워드:")
            }
        }
        .joinToString("\n")
        .trim()
}

private fun directionText(direction: CardDirection): String {
    return when (direction) {
        CardDirection.Upright -> "정방향"
        CardDirection.Reversed -> "역방향"
    }
}
