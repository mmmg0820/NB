package com.hoscat.mtj.dev

import mtj.records.Envelope
import mtj.records.Kind
import mtj.records.Value

internal data class RecordDetailRow(val label: String, val value: String)

internal fun Envelope.tarotResultSummary(): TarotResultSummary? {
    if (kind != Kind.TAROT) return null
    val snapshot = payload.obj("snapshot") ?: return null
    val spread = snapshot.obj("spread") ?: return null
    val reading = snapshot.obj("reading") ?: return null
    return TarotResultSummary(
        cardCount = spread.num("cardCount") ?: return null,
        title = spread.str("title") ?: return null,
        description = spread.str("subtitle") ?: return null,
        question = reading.str("question") ?: return null,
    )
}

internal fun Envelope.detailRows(): List<RecordDetailRow> = when (kind) {
    Kind.SAJU -> sajuDetailRows(payload)
    Kind.TAROT -> tarotDetailRows(payload)
    else -> emptyList()
}

private fun sajuDetailRows(payload: Value.Obj): List<RecordDetailRow> {
    val chart = payload.obj("chart")
    val input = payload.obj("input")
    val birth = input?.obj("birthDateTime")
    val evidence = chart?.obj("evidence")
    val pillars = listOfNotNull(
        chart?.pillar("yearPillar"),
        chart?.pillar("monthPillar"),
        chart?.pillar("dayPillar"),
        chart?.pillar("hourPillar"),
    )
    val birthDate = listOfNotNull(
        birth?.num("year"),
        birth?.num("month")?.padStart(2, '0'),
        birth?.num("day")?.padStart(2, '0'),
    ).takeIf { it.size == 3 }?.joinToString("-")
    val birthTime = birth?.num("hour")?.let { hour ->
        val minute = birth.num("minute") ?: "0"
        "${hour.padStart(2, '0')}:${minute.padStart(2, '0')}"
    } ?: "시간 모름"
    return listOfNotNull(
        birthDate?.let { RecordDetailRow("생년월일", it) },
        RecordDetailRow("태어난 시간", birthTime),
        pillars.takeIf { it.isNotEmpty() }?.let { RecordDetailRow("명식", it.joinToString(" / ")) },
        chart?.str("dayMaster")?.let { RecordDetailRow("일간", it) },
        evidence?.str("calculationBasisLabel")?.let { RecordDetailRow("계산 기준", it) },
        evidence?.str("trustLevel")?.let { trust ->
            val verified = evidence.bool("isVerified") == true
            RecordDetailRow("검증 상태", verificationStateLabel(trust, verified))
        },
    )
}

private fun verificationStateLabel(trust: String, isVerified: Boolean): String = when {
    isVerified && trust == "ExternalAuthorityVerified" -> "외부 기관 검증됨"
    isVerified -> "내부 검증됨"
    trust == "InternalStructureChecked" -> "내부 구조 검토됨"
    else -> "검토 필요"
}

private fun tarotDetailRows(payload: Value.Obj): List<RecordDetailRow> {
    val snapshot = payload.obj("snapshot")
    val reading = snapshot?.obj("reading")
    val spread = snapshot?.obj("spread")
    val cards = reading?.arr("cards").orEmpty().mapNotNull { card ->
        val item = card as? Value.Obj ?: return@mapNotNull null
        val order = item.num("order") ?: return@mapNotNull null
        val position = item.str("positionLabel") ?: return@mapNotNull null
        val name = item.str("nameKr") ?: return@mapNotNull null
        val direction = item.str("directionLabel") ?: return@mapNotNull null
        "$order. $position - $name · $direction"
    }
    return listOfNotNull(
        (spread?.str("title") ?: reading?.str("spreadTitle"))?.let { title ->
            val cardCount = spread?.num("cardCount") ?: reading?.num("cardCount")
            RecordDetailRow("스프레드", if (cardCount == null) title else "$title · ${cardCount}장")
        },
        reading?.str("question")?.ifBlank { null }?.let { RecordDetailRow("질문", it) },
        cards.takeIf { it.isNotEmpty() }?.let { RecordDetailRow("선택 카드", it.joinToString("\n")) },
    )
}

private fun Value.Obj.obj(key: String): Value.Obj? = fields[key] as? Value.Obj
private fun Value.Obj.arr(key: String): List<Value>? = (fields[key] as? Value.Arr)?.items
private fun Value.Obj.str(key: String): String? = (fields[key] as? Value.Str)?.text
private fun Value.Obj.num(key: String): String? = (fields[key] as? Value.Num)?.token
private fun Value.Obj.bool(key: String): Boolean? = (fields[key] as? Value.Bool)?.value

private fun Value.Obj.pillar(key: String): String? {
    val pillar = obj(key) ?: return null
    val label = pillar.str("label").orEmpty()
    val stem = pillar.str("stem").orEmpty()
    val branch = pillar.str("branch").orEmpty()
    val ganji = stem + branch
    return when {
        label.isBlank() -> ganji.ifBlank { null }
        ganji.isBlank() -> label
        else -> "$label $ganji"
    }
}
