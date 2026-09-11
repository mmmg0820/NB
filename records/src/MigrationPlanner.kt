package mtj.records

import java.math.BigDecimal

data class Origin(val source: String, val id: String) {
    val commonId: String get() = "mtj1:" + token(source) + token(id)
    internal fun valid() = source.isNotBlank() && id.isNotBlank()
}

enum class Kind { PROFILE, SAJU, TAROT, COMPATIBILITY }

sealed interface Value {
    data class Obj(val fields: Map<String, Value>) : Value
    data class Arr(val items: List<Value>) : Value
    data class Str(val text: String) : Value
    data class Num(val token: String) : Value
    data class Bool(val value: Boolean) : Value
    object Null : Value
}

data class Envelope(
    val origin: Origin,
    val kind: Kind,
    val payload: Value.Obj,
    val profileRefs: List<Origin> = emptyList(),
    val schemaVersion: Int = 1,
    val payloadVersion: Int = 1,
    val snapshotAtEpochMillis: Long = 0,
)

enum class Action { INSERT, SKIP, CONFLICT, INVALID }
enum class Reason { NEW, IDENTICAL, CONTENT_DIFFERS, SCHEMA, IDENTITY, REFERENCES,
    PAYLOAD, DUPLICATE_TARGET, BLOCKED_ORIGIN }
data class Decision(val index: Int, val origin: Origin, val action: Action, val reason: Reason)
data class TargetIssue(val index: Int, val reason: Reason)
data class Summary(val insert: Int, val skip: Int, val conflict: Int, val invalid: Int,
                   val targetIssues: Int)
data class Plan(val decisions: List<Decision>, val targetIssues: List<TargetIssue>) {
    val summary: Summary get() = Summary(
        decisions.count { it.action == Action.INSERT }, decisions.count { it.action == Action.SKIP },
        decisions.count { it.action == Action.CONFLICT }, decisions.count { it.action == Action.INVALID },
        targetIssues.size,
    )
    val ready: Boolean get() = targetIssues.isEmpty() &&
        decisions.none { it.action == Action.CONFLICT || it.action == Action.INVALID }
}

private fun token(value: String) = "${value.length}:$value"

internal object Canonical {
    private val number = Regex("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")
    fun encode(root: Value): String {
        var nodes = 0
        val out = StringBuilder()
        fun visit(value: Value, depth: Int) {
            require(depth <= 64 && ++nodes <= 10000)
            when (value) {
                is Value.Obj -> {
                    out.append("o${value.fields.size}:")
                    value.fields.toSortedMap().forEach { (key, child) ->
                        require(key.length <= 1000000)
                        out.append(token(key)); visit(child, depth + 1)
                    }
                }
                is Value.Arr -> {
                    out.append("a${value.items.size}:")
                    value.items.forEach { visit(it, depth + 1) }
                }
                is Value.Str -> {
                    require(value.text.length <= 1000000)
                    out.append('s').append(token(value.text))
                }
                is Value.Num -> {
                    require(value.token.length <= 128 && number.matches(value.token))
                    val decimal = BigDecimal(value.token).stripTrailingZeros()
                    out.append('n').append(token(if (decimal.signum() == 0) "0" else decimal.toString()))
                }
                is Value.Bool -> out.append(if (value.value) "t" else "f")
                Value.Null -> out.append('z')
            }
            require(out.length <= 1000000)
        }
        visit(root, 0)
        return out.toString()
    }
}

class MigrationPlanner {
    private data class Checked(val envelope: Envelope, val body: String?, val error: Reason?)

    private fun check(e: Envelope): Checked {
        val reason = when {
            !e.origin.valid() -> Reason.IDENTITY
            e.schemaVersion != 1 || e.payloadVersion != 1 || e.snapshotAtEpochMillis < 0 -> Reason.SCHEMA
            e.profileRefs.any { !it.valid() } || e.profileRefs.distinct().size != e.profileRefs.size -> Reason.REFERENCES
            e.kind == Kind.PROFILE && e.profileRefs.isNotEmpty() -> Reason.REFERENCES
            e.kind == Kind.SAJU && e.profileRefs.size != 1 -> Reason.REFERENCES
            e.kind == Kind.COMPATIBILITY && e.profileRefs.size != 2 -> Reason.REFERENCES
            else -> null
        }
        if (reason != null) return Checked(e, null, reason)
        return try { Checked(e, Canonical.encode(e.payload), null) }
        catch (_: IllegalArgumentException) { Checked(e, null, Reason.PAYLOAD) }
        catch (_: ArithmeticException) { Checked(e, null, Reason.PAYLOAD) }
    }

    private fun equal(a: Checked, b: Checked): Boolean =
        a.body == b.body && a.envelope.copy(payload = Value.Obj(emptyMap())) ==
            b.envelope.copy(payload = Value.Obj(emptyMap()))

    fun plan(existing: List<Envelope>, incoming: List<Envelope>): Plan {
        val target = existing.map(::check)
        val batch = incoming.map(::check)
        val targetGroups = target.indices.groupBy { target[it].envelope.origin }
        val batchGroups = batch.indices.groupBy { batch[it].envelope.origin }
        val issues = mutableListOf<TargetIssue>()
        target.forEachIndexed { index, item -> item.error?.let { issues.add(TargetIssue(index, it)) } }
        targetGroups.values.filter { it.size > 1 }.flatten().forEach {
            issues.add(TargetIssue(it, Reason.DUPLICATE_TARGET))
        }
        val validTarget = targetGroups.filterValues { it.size == 1 && target[it.single()].error == null }
            .mapValues { target[it.value.single()] }
        target.forEachIndexed { i, item ->
            if (item.envelope.profileRefs.any { validTarget[it]?.envelope?.kind != Kind.PROFILE })
                issues.add(TargetIssue(i, Reason.REFERENCES))
        }
        val decisions = arrayOfNulls<Decision>(batch.size)
        fun decide(i: Int, action: Action, reason: Reason) {
            decisions[i] = Decision(i, batch[i].envelope.origin, action, reason)
        }
        batchGroups.forEach { (origin, indices) ->
            val first = batch[indices.first()]
            val old = validTarget[origin]
            when {
                indices.any { batch[it].error != null } -> indices.forEach {
                    decide(it, Action.INVALID, batch[it].error ?: Reason.BLOCKED_ORIGIN)
                }
                targetGroups.containsKey(origin) && old == null -> indices.forEach {
                    decide(it, Action.INVALID, Reason.BLOCKED_ORIGIN)
                }
                indices.any { !equal(first, batch[it]) } || (old != null && !equal(first, old)) ->
                    indices.forEach { decide(it, Action.CONFLICT, Reason.CONTENT_DIFFERS) }
                else -> indices.forEachIndexed { offset, index ->
                    if (old != null || offset > 0) decide(index, Action.SKIP, Reason.IDENTICAL)
                    else decide(index, Action.INSERT, Reason.NEW)
                }
            }
        }
        // Profiles cannot have refs, so one pass resolves all dependencies, independent of order.
        fun profileAvailable(origin: Origin): Boolean {
            val indices = batchGroups[origin]
            if (indices != null) return indices.all {
                decisions[it]!!.action in setOf(Action.INSERT, Action.SKIP) &&
                    batch[it].envelope.kind == Kind.PROFILE
            }
            return validTarget[origin]?.envelope?.kind == Kind.PROFILE
        }
        batch.forEachIndexed { i, item ->
            if (decisions[i]!!.action in setOf(Action.INSERT, Action.SKIP) &&
                item.envelope.profileRefs.any { !profileAvailable(it) })
                decide(i, Action.INVALID, Reason.REFERENCES)
        }
        return Plan(java.util.Collections.unmodifiableList(decisions.map { it!! }),
            java.util.Collections.unmodifiableList(issues.toList()))
    }
}
