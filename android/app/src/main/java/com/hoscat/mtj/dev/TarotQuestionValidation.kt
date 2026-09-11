package com.hoscat.mtj.dev

internal const val MAX_TAROT_QUESTION_CODE_POINTS = 240
internal const val MAX_TAROT_QUESTION_INPUT_CODE_POINTS = 4096

internal data class TarotQuestionValidation(
    val codePointCount: Int,
    val error: String?,
) {
    val canStart: Boolean get() = error == null
}

internal fun validateTarotQuestion(question: String): TarotQuestionValidation {
    val codePointCount = question.codePointCount(0, question.length)
    val error = when {
        question.isBlank() -> "질문을 입력해주세요."
        codePointCount > MAX_TAROT_QUESTION_CODE_POINTS -> "질문은 최대 240자까지 입력할 수 있습니다."
        else -> null
    }
    return TarotQuestionValidation(codePointCount, error)
}

internal fun acceptsTarotQuestionInput(question: String): Boolean =
    question.codePointCount(0, question.length) <= MAX_TAROT_QUESTION_INPUT_CODE_POINTS
