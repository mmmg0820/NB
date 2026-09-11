package com.hoscat.core.manse

import android.content.Context
import android.util.LongSparseArray
import android.util.SparseArray
import org.json.JSONObject
import java.util.zip.GZIPInputStream

data class LunarDate(
    val solarDate: String,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val isLeapMonth: Boolean,
    val ganjiYear: String,
    val ganjiMonth: String,
    val ganjiDay: String,
)

data class LunarMonthOption(
    val month: Int,
    val isLeapMonth: Boolean,
    val dayCount: Int,
)

interface LunarDateDataSource {
    val version: ManseDataVersion
    fun dateBySolarDate(solarDate: String): LunarDate?
    fun dateByLunarDate(
        lunarYear: Int,
        lunarMonth: Int,
        lunarDay: Int,
        isLeapMonth: Boolean,
    ): LunarDate?

    fun monthOptions(lunarYear: Int): List<LunarMonthOption> = buildList {
        for (month in 1..12) {
            listOf(false, true).forEach { isLeapMonth ->
                val dayCount = when {
                    dateByLunarDate(lunarYear, month, 30, isLeapMonth) != null -> 30
                    dateByLunarDate(lunarYear, month, 29, isLeapMonth) != null -> 29
                    dateByLunarDate(lunarYear, month, 1, isLeapMonth) != null -> 1
                    else -> 0
                }
                if (dayCount > 0) {
                    add(LunarMonthOption(month, isLeapMonth, dayCount))
                }
            }
        }
    }
}

class EmptyLunarDateDataSource : LunarDateDataSource {
    override val version = ManseDataVersion(
        code = "empty-data-v0",
        solarTermRange = 1900..2100,
        lunarDateRange = 1900..2100,
        sourceNote = "No lunar data loaded.",
        verified = false,
    )

    override fun dateBySolarDate(solarDate: String): LunarDate? = null

    override fun dateByLunarDate(
        lunarYear: Int,
        lunarMonth: Int,
        lunarDay: Int,
        isLeapMonth: Boolean,
    ): LunarDate? = null
}

class AssetLunarDateDataSource(
    context: Context,
    assetPath: String = "manse/lunar_dates_1900_2100.json.gz.bin",
) : LunarDateDataSource {
    private val appContext = context.applicationContext

    private val parsed: ParsedLunarDates by lazy(LazyThreadSafetyMode.NONE) {
        appContext.loadAssetJson(assetPath).let { root ->
            ParsedLunarDates(
                version = parseVersion(root),
                dates = parseDates(root),
            )
        }
    }

    override val version: ManseDataVersion
        get() = parsed.version

    override fun dateBySolarDate(solarDate: String): LunarDate? =
        solarDateKeyOrNull(solarDate)?.let(parsed.dates::dateBySolarKey)

    override fun dateByLunarDate(
        lunarYear: Int,
        lunarMonth: Int,
        lunarDay: Int,
        isLeapMonth: Boolean,
    ): LunarDate? = parsed.dates.dateByLunarKey(
        lunarDateKey(lunarYear, lunarMonth, lunarDay, isLeapMonth),
    )

    private fun parseVersion(root: JSONObject): ManseDataVersion {
        val version = root.getJSONObject("version")
        val start = version.getInt("rangeStartYear")
        val end = version.getInt("rangeEndYear")
        return ManseDataVersion(
            code = version.getString("code"),
            solarTermRange = start..end,
            lunarDateRange = start..end,
            sourceNote = version.optString("note", ""),
            verified = version.optBoolean("verified", false),
        )
    }

    private fun parseDates(root: JSONObject): LunarDateIndexes {
        val items = root.getJSONArray("dates")
        val bySolarDate = SparseArray<LunarDate>(items.length())
        val ganjiPool = HashMap<String, String>(96)
        repeat(items.length()) { index ->
            val item = items.getJSONObject(index)
            val solarDate = item.getString("solarDate")
            val date = LunarDate(
                solarDate = solarDate,
                lunarYear = item.getInt("lunarYear"),
                lunarMonth = item.getInt("lunarMonth"),
                lunarDay = item.getInt("lunarDay"),
                isLeapMonth = item.getBoolean("isLeapMonth"),
                ganjiYear = ganjiPool.canonical(item.getString("ganjiYear")),
                ganjiMonth = ganjiPool.canonical(item.getString("ganjiMonth")),
                ganjiDay = ganjiPool.canonical(item.getString("ganjiDay")),
            )
            bySolarDate.append(solarDateKeyOrNull(solarDate) ?: error("Invalid solar date: $solarDate"), date)
        }
        return LunarDateIndexes(bySolarDate)
    }
}

private data class ParsedLunarDates(
    val version: ManseDataVersion,
    val dates: LunarDateIndexes,
)

private class LunarDateIndexes(
    private val bySolarDate: SparseArray<LunarDate>,
) {
    private val byLunarDate: LongSparseArray<LunarDate> by lazy(LazyThreadSafetyMode.NONE) {
        LongSparseArray<LunarDate>(bySolarDate.size()).apply {
            repeat(bySolarDate.size()) { index ->
                val date = bySolarDate.valueAt(index)
                put(lunarDateKey(date.lunarYear, date.lunarMonth, date.lunarDay, date.isLeapMonth), date)
            }
        }
    }

    fun dateBySolarKey(key: Int): LunarDate? = bySolarDate[key]

    fun dateByLunarKey(key: Long): LunarDate? = byLunarDate[key]
}

internal fun lunarDateKey(year: Int, month: Int, day: Int, isLeapMonth: Boolean): Long =
    year.toLong() * 100_000L + month * 1_000L + day * 10L + if (isLeapMonth) 1L else 0L

internal fun solarDateKeyOrNull(value: String): Int? {
    if (value.length != 10 || value[4] != '-' || value[7] != '-') return null
    var result = 0
    SOLAR_DATE_DIGIT_INDEXES.forEach { index ->
        val digit = value[index].digitToIntOrNull() ?: return null
        result = result * 10 + digit
    }
    return result
}

private val SOLAR_DATE_DIGIT_INDEXES = intArrayOf(0, 1, 2, 3, 5, 6, 8, 9)

private fun MutableMap<String, String>.canonical(value: String): String = getOrPut(value) { value }

private fun Context.loadAssetJson(assetPath: String): JSONObject {
    val json = assets.open(assetPath).use { stream ->
        val input = if (assetPath.endsWith(".gz") || assetPath.endsWith(".gz.bin")) GZIPInputStream(stream) else stream
        input.bufferedReader().use { it.readText() }
    }
    return JSONObject(json)
}
