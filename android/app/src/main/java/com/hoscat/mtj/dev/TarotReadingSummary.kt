package com.hoscat.mtj.dev

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mtj.design.MtjQuietPanel
import com.mtj.tarot.MtjResultSnapshot
import mtj.records.Value

/**
 * Single-source formatter for the two-line tarot summary contract
 * (docs/design-system §"최신 필수 계약: 타로 요약 패널", 2026-09-11):
 * `스프레드: {장수}장 | {스프레드명} | {설명}` / `질문: {원문}`.
 * Shared by the live result screen and the saved-record detail screen so the
 * two never drift into different structures again.
 */
internal data class TarotReadingSummaryLines(val spreadLine: String, val questionLine: String)

internal fun tarotReadingSummaryLines(snapshot: MtjResultSnapshot): TarotReadingSummaryLines =
    TarotReadingSummaryLines(
        spreadLine = spreadSummaryLine(
            cardCount = snapshot.spread.cardCount.toString(),
            title = snapshot.spread.title,
            subtitle = snapshot.spread.subtitle,
        ),
        questionLine = questionSummaryLine(snapshot.reading.question),
    )

/**
 * Legacy-safe variant for saved records: the full [MtjResultSnapshot] is stored under
 * `snapshot` in the envelope payload, so the same fields are read back from that generic
 * tree. Older saved records that predate a field simply omit that segment of the line.
 */
internal fun tarotReadingSummaryLines(payload: Value.Obj): TarotReadingSummaryLines? {
    val snapshot = payload.obj("snapshot") ?: return null
    val spread = snapshot.obj("spread")
    val reading = snapshot.obj("reading")
    val title = spread?.str("title") ?: reading?.str("spreadTitle") ?: return null
    return TarotReadingSummaryLines(
        spreadLine = spreadSummaryLine(
            cardCount = spread?.num("cardCount") ?: reading?.num("cardCount"),
            title = title,
            subtitle = spread?.str("subtitle"),
        ),
        questionLine = questionSummaryLine(reading?.str("question").orEmpty()),
    )
}

private fun spreadSummaryLine(cardCount: String?, title: String, subtitle: String?): String = buildString {
    append("스프레드: ")
    if (cardCount != null) append(cardCount).append("장 | ")
    append(title)
    if (!subtitle.isNullOrBlank()) append(" | ").append(subtitle)
}

// 질문은 선택 사항이다. 새 리딩은 TAROT_QUESTION_NOT_PROVIDED로 저장되지만, 그 기능이 생기기
// 전에 저장된 레거시 기록은 실제로 빈 문자열일 수 있어 여기서도 방어적으로 처리한다.
private fun questionSummaryLine(question: String): String =
    "질문: " + question.ifBlank { TAROT_QUESTION_NOT_PROVIDED }

@Composable
internal fun TarotReadingSummaryPanel(lines: TarotReadingSummaryLines, modifier: Modifier = Modifier) {
    MtjQuietPanel(modifier) {
        Text(
            lines.spreadLine,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(lines.questionLine, style = MaterialTheme.typography.bodyLarge)
    }
}
