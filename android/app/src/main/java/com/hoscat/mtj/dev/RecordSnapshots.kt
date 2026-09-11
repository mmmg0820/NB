package com.hoscat.mtj.dev

import com.google.gson.Gson
import com.mtj.tarot.MtjResultSnapshot
import mtj.records.*
import mtj.saju.MtjSajuEvaluation
import java.util.UUID

internal object RecordSnapshots {
    private val codec = RecordsJsonCodec()
    private val gson = Gson()
    private fun tree(value: Any): Value = codec.decodeValue(gson.toJson(value).toByteArray(Charsets.UTF_8))
    private fun origin() = Origin("mtj-native", UUID.randomUUID().toString())

    fun saju(result: MtjSajuEvaluation.Accepted): List<Envelope> {
        val profile = origin()
        val time = System.currentTimeMillis()
        return listOf(
            Envelope(profile, Kind.PROFILE, Value.Obj(mapOf("input" to tree(result.input))), snapshotAtEpochMillis = time),
            Envelope(origin(), Kind.SAJU, Value.Obj(mapOf(
                "title" to Value.Str(sajuReadingTitle(result.chart)),
                "summary" to Value.Str(result.chart.summary),
                "chart" to tree(result.chart), "input" to tree(result.input),
            )), listOf(profile), snapshotAtEpochMillis = time),
        )
    }

    fun tarot(result: MtjResultSnapshot): Envelope = Envelope(
        origin(), Kind.TAROT, Value.Obj(mapOf(
            "title" to Value.Str(
                result.reading.question
                    .takeUnless { it.isBlank() || it == TAROT_QUESTION_NOT_PROVIDED }
                    ?: result.reading.spreadTitle,
            ),
            "summary" to Value.Str(result.reading.cards.joinToString("\n\n") {
                "${it.positionLabel}\n${it.nameKr} · ${it.directionLabel}\n${it.meaningSnapshot}"
            }),
            "snapshot" to tree(result),
        )), snapshotAtEpochMillis = System.currentTimeMillis(),
    )
}

internal fun sajuReadingTitle(chart: com.hoscat.core.model.SajuChart): String =
    "${chart.monthPillar.branch}월${chart.dayPillar.stem}${chart.dayPillar.branch}"

internal fun Envelope.displayText(key: String): String =
    (payload.fields[key] as? Value.Str)?.text ?: ""
