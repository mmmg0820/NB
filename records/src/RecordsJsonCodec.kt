package mtj.records

import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.io.StringReader
import java.io.Writer
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

enum class CodecFailure { JSON_SYNTAX, UTF8, DUPLICATE_KEY, UNKNOWN_SCHEMA, FIELDS,
    TYPE, NUMBER, DEPTH, SIZE, NODES, IDENTITY, REFERENCES }

// Do not include parser messages, paths, keys or user data in diagnostics.
class RecordsCodecException(val failure: CodecFailure) : IllegalArgumentException(failure.name)

/** MTJ common wire format only. No domain schema inference, I/O or source-app conversion. */
class RecordsJsonCodec {
    companion object {
        const val MAX_BYTES = 2_000_000
        const val MAX_DEPTH = 64
        const val MAX_NODES = 10_000
        private val NUMBER = Regex("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")
        private val INTEGER = Regex("-?(0|[1-9][0-9]*)")
    }

    private fun fail(code: CodecFailure): Nothing = throw RecordsCodecException(code)

    fun decode(bytes: ByteArray): Envelope = fromTree(decodeValue(bytes))

    fun encode(envelope: Envelope): ByteArray {
        val tree = toTree(envelope)
        val bytes = encodeValue(tree)
        // Validate the same wire contract on both sides; no lossy serializer defaults.
        fromTree(tree)
        return bytes
    }

    fun decodeValue(bytes: ByteArray): Value {
        if (bytes.size > MAX_BYTES) fail(CodecFailure.SIZE)
        val text = try {
            Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) { fail(CodecFailure.UTF8) }
        if (text.startsWith('\uFEFF')) fail(CodecFailure.JSON_SYNTAX)
        try {
            JsonReader(StringReader(text)).use { reader ->
                reader.strictness = Strictness.STRICT
                val budget = Budget()
                fun read(depth: Int): Value {
                    budget.visit(depth)
                    return when (reader.peek()) {
                        JsonToken.BEGIN_OBJECT -> {
                            reader.beginObject()
                            val fields = linkedMapOf<String, Value>()
                            while (reader.hasNext()) {
                                val key = reader.nextName()
                                unicode(key)
                                if (fields.containsKey(key)) fail(CodecFailure.DUPLICATE_KEY)
                                fields[key] = read(depth + 1)
                            }
                            reader.endObject(); Value.Obj(fields)
                        }
                        JsonToken.BEGIN_ARRAY -> {
                            reader.beginArray()
                            val items = mutableListOf<Value>()
                            while (reader.hasNext()) items.add(read(depth + 1))
                            reader.endArray(); Value.Arr(items)
                        }
                        JsonToken.STRING -> Value.Str(reader.nextString().also(::unicode))
                        JsonToken.NUMBER -> Value.Num(reader.nextString().also(::number))
                        JsonToken.BOOLEAN -> Value.Bool(reader.nextBoolean())
                        JsonToken.NULL -> { reader.nextNull(); Value.Null }
                        else -> fail(CodecFailure.JSON_SYNTAX)
                    }
                }
                val result = read(0)
                if (reader.peek() != JsonToken.END_DOCUMENT) fail(CodecFailure.JSON_SYNTAX)
                return result
            }
        } catch (e: RecordsCodecException) { throw e }
        catch (_: IOException) { fail(CodecFailure.JSON_SYNTAX) }
        catch (_: IllegalStateException) { fail(CodecFailure.JSON_SYNTAX) }
        catch (_: NumberFormatException) { fail(CodecFailure.NUMBER) }
    }

    fun encodeValue(value: Value): ByteArray {
        val output = BoundedWriter()
        JsonWriter(output).use { writer ->
            writer.strictness = Strictness.STRICT
            writer.serializeNulls = true
            val budget = Budget()
            fun write(v: Value, depth: Int) {
                budget.visit(depth)
                when (v) {
                    is Value.Obj -> {
                        writer.beginObject()
                        v.fields.forEach { (key, child) -> unicode(key); writer.name(key); write(child, depth + 1) }
                        writer.endObject()
                    }
                    is Value.Arr -> {
                        writer.beginArray(); v.items.forEach { write(it, depth + 1) }; writer.endArray()
                    }
                    is Value.Str -> { unicode(v.text); writer.value(v.text) }
                    is Value.Num -> { number(v.token); writer.jsonValue(v.token) }
                    is Value.Bool -> writer.value(v.value)
                    Value.Null -> writer.nullValue()
                }
            }
            write(value, 0)
        }
        return output.text.toString().toByteArray(Charsets.UTF_8).also {
            if (it.size > MAX_BYTES) fail(CodecFailure.SIZE)
        }
    }

    private inner class Budget {
        private var nodes = 0
        fun visit(depth: Int) {
            if (depth > MAX_DEPTH) fail(CodecFailure.DEPTH)
            if (++nodes > MAX_NODES) fail(CodecFailure.NODES)
        }
    }

    private inner class BoundedWriter : Writer() {
        val text = StringBuilder()
        override fun write(chars: CharArray, offset: Int, length: Int) {
            if (length > MAX_BYTES - text.length) fail(CodecFailure.SIZE)
            text.append(chars, offset, length)
        }
        override fun flush() = Unit
        override fun close() = Unit
    }

    private fun unicode(text: String) {
        if (text.length > MAX_BYTES) fail(CodecFailure.SIZE)
        var i = 0
        while (i < text.length) {
            val c = text[i++]
            if (c.isHighSurrogate()) {
                if (i == text.length || !text[i++].isLowSurrogate()) fail(CodecFailure.UTF8)
            } else if (c.isLowSurrogate()) fail(CodecFailure.UTF8)
        }
    }

    private fun number(token: String) {
        if (token.length > 128 || !NUMBER.matches(token)) fail(CodecFailure.NUMBER)
        try { BigDecimal(token).stripTrailingZeros() }
        catch (_: NumberFormatException) { fail(CodecFailure.NUMBER) }
        catch (_: ArithmeticException) { fail(CodecFailure.NUMBER) }
    }

    private fun obj(value: Value): Map<String, Value> =
        (value as? Value.Obj)?.fields ?: fail(CodecFailure.TYPE)
    private fun string(value: Value): String = (value as? Value.Str)?.text ?: fail(CodecFailure.TYPE)
    private fun long(value: Value): Long {
        val token = (value as? Value.Num)?.token ?: fail(CodecFailure.TYPE)
        if (!INTEGER.matches(token)) fail(CodecFailure.NUMBER)
        return token.toLongOrNull() ?: fail(CodecFailure.NUMBER)
    }
    private fun fields(map: Map<String, Value>, expected: Set<String>) {
        if (map.keys != expected) fail(CodecFailure.FIELDS)
    }
    private fun origin(value: Value): Origin {
        val map = obj(value)
        fields(map, setOf("source", "id"))
        return Origin(string(map.getValue("source")), string(map.getValue("id"))).also {
            if (!it.valid()) fail(CodecFailure.IDENTITY)
        }
    }
    private fun fromTree(value: Value): Envelope {
        val map = obj(value)
        fields(map, setOf("schemaVersion", "origin", "kind", "payloadVersion", "snapshotAtEpochMillis", "profileRefs", "payload"))
        if (long(map.getValue("schemaVersion")) != 1L || long(map.getValue("payloadVersion")) != 1L)
            fail(CodecFailure.UNKNOWN_SCHEMA)
        val kind = Kind.entries.find { it.name == string(map.getValue("kind")) } ?: fail(CodecFailure.TYPE)
        val refs = (map.getValue("profileRefs") as? Value.Arr)?.items?.map(::origin) ?: fail(CodecFailure.TYPE)
        if (refs.distinct().size != refs.size ||
            (kind == Kind.PROFILE && refs.isNotEmpty()) || (kind == Kind.SAJU && refs.size != 1) ||
            (kind == Kind.COMPATIBILITY && refs.size != 2)) fail(CodecFailure.REFERENCES)
        val time = long(map.getValue("snapshotAtEpochMillis"))
        if (time < 0) fail(CodecFailure.NUMBER)
        val payload = map.getValue("payload") as? Value.Obj ?: fail(CodecFailure.TYPE)
        // Reuse the planner's canonical admission rules without resolving cross-record links here.
        try { Canonical.encode(payload) }
        // Number/depth/node checks already passed; the remaining canonical bound is text size.
        catch (_: IllegalArgumentException) { fail(CodecFailure.SIZE) }
        catch (_: ArithmeticException) { fail(CodecFailure.NUMBER) }
        return Envelope(origin(map.getValue("origin")), kind, payload, refs,
            snapshotAtEpochMillis = time)
    }

    private fun toTree(e: Envelope): Value.Obj {
        fun origin(o: Origin) = Value.Obj(linkedMapOf("source" to Value.Str(o.source), "id" to Value.Str(o.id)))
        return Value.Obj(linkedMapOf(
            "schemaVersion" to Value.Num(e.schemaVersion.toString()), "origin" to origin(e.origin),
            "kind" to Value.Str(e.kind.name), "payloadVersion" to Value.Num(e.payloadVersion.toString()),
            "snapshotAtEpochMillis" to Value.Num(e.snapshotAtEpochMillis.toString()),
            "profileRefs" to Value.Arr(e.profileRefs.map(::origin)), "payload" to e.payload,
        ))
    }
}
