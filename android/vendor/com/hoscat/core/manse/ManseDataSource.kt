package com.hoscat.core.manse

data class SolarTerm(
    val gregorianYear: Int,
    val termIndex: Int,
    val nameKo: String,
    val solarLongitudeDeg: Double,
    val instantUtcIso: String,
    val instantKstIso: String,
    val source: String,
    val sourceVersion: String,
)

data class ManseDataVersion(
    val code: String,
    val solarTermRange: IntRange,
    val lunarDateRange: IntRange,
    val sourceNote: String,
    val verified: Boolean,
)

interface SolarTermDataSource {
    val version: ManseDataVersion
    fun termsForYear(gregorianYear: Int): List<SolarTerm>
}

class EmptySolarTermDataSource : SolarTermDataSource {
    override val version = ManseDataVersion(
        code = "empty-data-v0",
        solarTermRange = 1900..2100,
        lunarDateRange = 1900..2100,
        sourceNote = "No solar term data loaded.",
        verified = false,
    )

    override fun termsForYear(gregorianYear: Int): List<SolarTerm> = emptyList()
}
