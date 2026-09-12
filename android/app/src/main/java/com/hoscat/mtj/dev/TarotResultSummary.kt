package com.hoscat.mtj.dev

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mtj.design.MtjQuietPanel
import com.mtj.tarot.MtjResultSnapshot

internal data class TarotResultSummary(
    val cardCount: String,
    val title: String,
    val description: String,
    val question: String,
) {
    val spreadRow: String get() = "스프레드: ${cardCount}장 | $title | $description"
    val questionRow: String get() = "질문: $question"
}

internal fun MtjResultSnapshot.resultSummary() = TarotResultSummary(
    cardCount = spread.cardCount.toString(),
    title = spread.title,
    description = spread.subtitle,
    question = reading.question,
)

@Composable
internal fun TarotResultSummaryPanel(summary: TarotResultSummary) {
    MtjQuietPanel {
        Text(summary.spreadRow, style = MaterialTheme.typography.bodyMedium)
        Text(summary.questionRow, style = MaterialTheme.typography.bodyMedium)
    }
}
