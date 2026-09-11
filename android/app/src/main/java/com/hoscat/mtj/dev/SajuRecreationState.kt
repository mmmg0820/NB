package com.hoscat.mtj.dev

import androidx.compose.runtime.saveable.Saver
import com.google.gson.Gson
import mtj.records.Envelope
import mtj.records.Kind
import mtj.records.RecordsJsonCodec
import mtj.saju.MtjSajuEvaluation

/** Activity saved state only; retain the computed snapshot and record identity without recalculating. */
internal data class SajuRecreationState(
    val evaluation: MtjSajuEvaluation? = null,
    val saveBatch: List<Envelope>? = null,
) {
    fun save(): List<Any> = when (val result = evaluation) {
        null -> listOf(1, "input")
        is MtjSajuEvaluation.Rejected -> listOf(1, "rejected", result.message)
        is MtjSajuEvaluation.Accepted -> listOf(1, "accepted", gson.toJson(result)) +
            checkNotNull(saveBatch).map { codec.encode(it).toString(Charsets.UTF_8) }
    }

    companion object {
        private val gson = Gson()
        private val codec = RecordsJsonCodec()

        fun from(evaluation: MtjSajuEvaluation): SajuRecreationState = SajuRecreationState(
            evaluation,
            (evaluation as? MtjSajuEvaluation.Accepted)?.let(RecordSnapshots::saju),
        )

        fun restore(value: Any): SajuRecreationState = try {
            val saved = value as List<*>
            require(saved.firstOrNull() == 1)
            when (saved.getOrNull(1)) {
                "input" -> {
                    require(saved.size == 2)
                    SajuRecreationState()
                }
                "rejected" -> {
                    require(saved.size == 3)
                    from(MtjSajuEvaluation.Rejected(saved[2] as String))
                }
                "accepted" -> {
                    require(saved.size == 5)
                    val result = checkNotNull(gson.fromJson(saved[2] as String, MtjSajuEvaluation.Accepted::class.java))
                    val batch = saved.drop(3).map { codec.decode((it as String).toByteArray(Charsets.UTF_8)) }
                    require(batch[0].kind == Kind.PROFILE && batch[1].kind == Kind.SAJU)
                    require(batch[1].profileRefs == listOf(batch[0].origin))
                    require((result.input.birthDateTime.hour == null) == (result.chart.hourPillar == null))
                    SajuRecreationState(result, batch)
                }
                else -> error("Unknown saved Saju state")
            }
        } catch (_: Exception) {
            from(MtjSajuEvaluation.Rejected("명식을 복원하지 못했습니다. 입력한 정보로 다시 계산해주세요."))
        }
    }
}

internal val SajuRecreationSaver = Saver<SajuRecreationState, Any>(
    save = { it.save() },
    restore = SajuRecreationState::restore,
)
