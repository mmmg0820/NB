package com.hoscat.mtj.dev

internal const val MAX_TAROT_QUESTION_CODE_POINTS = 240
internal const val MAX_TAROT_QUESTION_INPUT_CODE_POINTS = 4096

/**
 * [TarotRecreationState] requires a non-blank, pre-trimmed `questionAtStart` (its own
 * `validate()` invariant), even though the question is optional in the UI. Screens that
 * start a reading with a blank question substitute this sentinel instead of relaxing that
 * state-machine contract. Anything that later treats "question" as optional (saved-record
 * titles, the reading summary line) should compare against this constant the same way it
 * would check for blank.
 */
internal const val TAROT_QUESTION_NOT_PROVIDED = "남기지 않음"

internal data class TarotQuestionValidation(
    val codePointCount: Int,
    val error: String?,
) {
    val canStart: Boolean get() = error == null
}

/** 질문은 선택 사항이다 — 비어 있어도 리딩을 시작할 수 있다. 길이 제한만 막는다. */
internal fun validateTarotQuestion(question: String): TarotQuestionValidation {
    val codePointCount = question.codePointCount(0, question.length)
    val error = when {
        codePointCount > MAX_TAROT_QUESTION_CODE_POINTS -> "질문은 최대 240자까지 입력할 수 있습니다."
        else -> null
    }
    return TarotQuestionValidation(codePointCount, error)
}

internal fun acceptsTarotQuestionInput(question: String): Boolean =
    question.codePointCount(0, question.length) <= MAX_TAROT_QUESTION_INPUT_CODE_POINTS
