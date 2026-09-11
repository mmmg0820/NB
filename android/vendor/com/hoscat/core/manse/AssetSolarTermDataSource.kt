package com.hoscat.core.manse

import android.content.Context
import org.json.JSONObject
import java.util.zip.GZIPInputStream

class AssetSolarTermDataSource(
    context: Context,
    assetPath: String = "manse/solar_terms_1900_2100.json.gz.bin",
) : SolarTermDataSource {
    private val appContext = context.applicationContext

    private val parsed: ParsedSolarTerms by lazy(LazyThreadSafetyMode.NONE) {
        appContext.loadAssetJson(assetPath).let { root ->
            val terms = parseTerms(root)
            ParsedSolarTerms(
                version = parseVersion(root, terms.yearRange),
                termsByYear = terms.byYear,
            )
        }
    }

    override val version: ManseDataVersion
        get() = parsed.version

    override fun termsForYear(gregorianYear: Int): List<SolarTerm> =
        parsed.termsByYear[gregorianYear].orEmpty()

    private fun parseVersion(root: JSONObject, termsYearRange: IntRange): ManseDataVersion {
        val version = root.getJSONObject("version")
        return ManseDataVersion(
            code = version.getString("code"),
            solarTermRange = termsYearRange,
            lunarDateRange = 1900..2100,
            sourceNote = version.optString("note", ""),
            verified = version.optBoolean("verified", false),
        )
    }

    private fun parseTerms(root: JSONObject): SolarTermIndexes {
        val items = root.getJSONArray("terms")
        val byYear = LinkedHashMap<Int, MutableList<SolarTerm>>()
        var minYear = Int.MAX_VALUE
        var maxYear = Int.MIN_VALUE
        repeat(items.length()) { index ->
            val item = items.getJSONObject(index)
            val year = item.getInt("gregorianYear")
            minYear = minOf(minYear, year)
            maxYear = maxOf(maxYear, year)
            val term = SolarTerm(
                gregorianYear = item.getInt("gregorianYear"),
                termIndex = item.getInt("termIndex"),
                nameKo = item.getString("nameKo"),
                solarLongitudeDeg = item.getDouble("solarLongitudeDeg"),
                instantUtcIso = item.getString("instantUtcIso"),
                instantKstIso = item.optString("instantKstIso", item.getString("instantUtcIso")),
                source = item.getString("source"),
                sourceVersion = item.getString("sourceVersion"),
            )
            byYear.getOrPut(year) { mutableListOf() }.add(term)
        }
        return SolarTermIndexes(
            byYear = byYear,
            yearRange = if (items.length() == 0) 1900..2100 else minYear..maxYear,
        )
    }
}

private data class ParsedSolarTerms(
    val version: ManseDataVersion,
    val termsByYear: Map<Int, List<SolarTerm>>,
)

private data class SolarTermIndexes(
    val byYear: Map<Int, List<SolarTerm>>,
    val yearRange: IntRange,
)

private fun Context.loadAssetJson(assetPath: String): JSONObject {
    val json = assets.open(assetPath).use { stream ->
        val input = if (assetPath.endsWith(".gz") || assetPath.endsWith(".gz.bin")) GZIPInputStream(stream) else stream
        input.bufferedReader().use { it.readText() }
    }
    return JSONObject(json)
}
