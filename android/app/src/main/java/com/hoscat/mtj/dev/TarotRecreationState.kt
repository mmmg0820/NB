package com.hoscat.mtj.dev

import com.google.gson.Gson
import com.mtj.tarot.MtjResultSnapshot
import com.mtj.tarot.MtjTarotBridge
import com.softcat.mystictarot.*
import mtj.records.Envelope
import mtj.records.Origin
import java.security.MessageDigest
import kotlin.random.Random

/** Activity saved state only. No durable draft storage or domain/record schema changes. */
internal data class TarotRecreationState(
    val spreadKey: String = "",
    val started: Boolean = false,
    val seed: Long = 0,
    val questionAtStart: String = "",
    val useReversedAtStart: Boolean = false,
    val catalogHash: String = "",
    val shuffledIds: List<Int> = emptyList(),
    val selectedIds: List<Int> = emptyList(),
    val locked: Boolean = false,
    val readingId: Long = 0,
    val readingSavedAt: Long = 0,
    val recordId: String = "",
    val recordSavedAt: Long = 0,
    val resultHash: String = "",
    val failed: Boolean = false,
) {
    private fun validate(): SpreadOption? {
        require(!failed) { "Tarot restoration failed" }
        val spread = selectableSpreadOptions.singleOrNull {
            it.key == spreadKey && it.drawMode == SpreadDrawMode.Normal
        }
        require(spreadKey.isEmpty() || spread != null) { "Unknown saved spread" }
        if (!started) {
            require(seed == 0L && questionAtStart.isEmpty() && !useReversedAtStart &&
                catalogHash.isEmpty() && shuffledIds.isEmpty() && selectedIds.isEmpty() && !locked)
        } else {
            require(spread != null && spread.cardCount in 1..10)
            require(questionAtStart.isNotBlank() && questionAtStart.length <= 240 &&
                questionAtStart == questionAtStart.trim())
            require(HASH.matches(catalogHash))
            require(shuffledIds.size == 78 && shuffledIds.toSet() == (0..77).toSet())
            require(selectedIds.size <= spread.cardCount && selectedIds.distinct() == selectedIds &&
                selectedIds.all { it in shuffledIds })
        }
        if (locked) {
            require(started && selectedIds.size == spread?.cardCount)
            require(readingId > 0 && readingSavedAt > 0 && recordSavedAt > 0)
            require(UUID.matches(recordId) && HASH.matches(resultHash))
        } else {
            require(readingId == 0L && readingSavedAt == 0L && recordId.isEmpty() &&
                recordSavedAt == 0L && resultHash.isEmpty())
        }
        return spread
    }

    fun materialize(deck: TarotDeck): TarotRecreated {
        val spread = validate()
        if (!started) return TarotRecreated(spread)
        check(fingerprint(listOf(deck, spread, seed, shuffledIds)) == catalogHash) {
            "Saved tarot catalog or shuffle changed"
        }
        // Replay only the saved seed. Verify both permutation and frozen output before display.
        val bridge = MtjTarotBridge(
            deck,
            checkNotNull(spread),
            questionAtStart,
            useReversedAtStart,
            Random(seed),
            shuffledIds,
        )
        if (!locked) return TarotRecreated(spread, bridge)
        bridge.lockSelection(selectedIds)
        val replay = bridge.snapshot()
        val result = replay.copy(reading = replay.reading.copy(id = readingId, savedAt = readingSavedAt))
        val record = RecordSnapshots.tarot(result).copy(
            origin = Origin("mtj-native", recordId), snapshotAtEpochMillis = recordSavedAt,
        )
        check(fingerprint(record) == resultHash) { "Saved tarot result changed" }
        return TarotRecreated(spread, bridge, result, record)
    }

    fun finish(deck: TarotDeck): TarotRecreationState {
        if (locked) {
            materialize(deck)
            return this
        }
        val bridge = checkNotNull(materialize(deck).bridge) { "Start a reading first" }
        bridge.lockSelection(selectedIds)
        val result = bridge.snapshot()
        val record = RecordSnapshots.tarot(result)
        return copy(locked = true, readingId = result.reading.id, readingSavedAt = result.reading.savedAt,
            recordId = record.origin.id, recordSavedAt = record.snapshotAtEpochMillis,
            resultHash = fingerprint(record)).also { it.validate() }
    }

    fun reopenSelection(): TarotRecreationState {
        require(locked) { "Only a completed reading can be reopened" }
        return copy(
            locked = false,
            readingId = 0,
            readingSavedAt = 0,
            recordId = "",
            recordSavedAt = 0,
            resultHash = "",
        ).also { it.validate() }
    }

    fun reshufflePreservingSelection(deck: TarotDeck, seed: Long = Random.nextLong()): TarotRecreationState {
        val spread = checkNotNull(validate()) { "Start a reading first" }
        require(started && !locked)
        val fresh = start(deck, spread, questionAtStart, useReversedAtStart, seed)
        val selectedSet = selectedIds.toSet()
        val unselected = fresh.shuffledIds.filterNot(selectedSet::contains).iterator()
        val preservedOrder = shuffledIds.map { id -> if (id in selectedSet) id else unselected.next() }
        return fresh.copy(
            shuffledIds = preservedOrder,
            selectedIds = selectedIds,
            catalogHash = fingerprint(listOf(deck, spread, fresh.seed, preservedOrder)),
        ).also { it.validate() }
    }

    /** Flat Bundle-safe primitives, at most 78 + 10 IDs; never serialize catalog/results/images. */
    fun save(): List<Any> {
        if (failed) return listOf(-1)
        validate()
        return arrayListOf(VERSION, spreadKey, started, seed, questionAtStart, useReversedAtStart,
            catalogHash, shuffledIds.toIntArray(), selectedIds.toIntArray(), locked, readingId,
            readingSavedAt, recordId, recordSavedAt, resultHash)
    }

    companion object {
        private const val VERSION = 1
        private val HASH = Regex("[0-9a-f]{64}")
        private val UUID = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        private val gson = Gson()

        private fun fingerprint(value: Any): String = MessageDigest.getInstance("SHA-256")
            .digest(gson.toJson(value).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }

        fun start(deck: TarotDeck, spread: SpreadOption, question: String, reversed: Boolean,
            seed: Long = Random.nextLong()): TarotRecreationState {
            val bridge = MtjTarotBridge(deck, spread, question.trim(), reversed, Random(seed))
            val shuffledIds = bridge.shuffledCards.map { it.id }
            return TarotRecreationState(spreadKey = spread.key, started = true, seed = seed,
                questionAtStart = question.trim(), useReversedAtStart = reversed,
                catalogHash = fingerprint(listOf(deck, spread, seed, shuffledIds)),
                shuffledIds = shuffledIds).also { it.validate() }
        }

        /** Never return null: a failed Saver restore must not invoke the fresh-session initializer. */
        fun restore(value: Any): TarotRecreationState = try {
            val values = value as? List<*> ?: throw IllegalArgumentException("Invalid saved state")
            require(values.size == 15 && values[0] == VERSION)
            fun text(index: Int, max: Int): String = (values[index] as String).also { require(it.length <= max) }
            fun ids(index: Int, max: Int): List<Int> = (values[index] as IntArray)
                .also { require(it.size <= max) }.toList()
            TarotRecreationState(
                spreadKey = text(1, 128), started = values[2] as Boolean, seed = values[3] as Long,
                questionAtStart = text(4, 240), useReversedAtStart = values[5] as Boolean,
                catalogHash = text(6, 64), shuffledIds = ids(7, 78), selectedIds = ids(8, 10),
                locked = values[9] as Boolean, readingId = values[10] as Long,
                readingSavedAt = values[11] as Long, recordId = text(12, 36),
                recordSavedAt = values[13] as Long, resultHash = text(14, 64),
            ).also { it.validate() }
        } catch (_: Exception) {
            TarotRecreationState(failed = true)
        }
    }
}

internal data class TarotRecreated(
    val spread: SpreadOption?,
    val bridge: MtjTarotBridge? = null,
    val result: MtjResultSnapshot? = null,
    val record: Envelope? = null,
)
