package com.hoscat.core.model

data class Pillar(
    val stem: String,
    val branch: String,
    val label: String,
)

enum class FiveElement {
    Wood,
    Fire,
    Earth,
    Metal,
    Water,
}

data class ElementCount(
    val element: FiveElement,
    val count: Int,
)

data class TenGodRelation(
    val pillarLabel: String,
    val stem: String,
    val relation: String,
)

data class HiddenStemSet(
    val branch: String,
    val stems: List<String>,
)

data class SajuAnalysisSnapshot(
    val elementCounts: List<ElementCount>,
    val visibleTenGods: List<TenGodRelation>,
    val hiddenStems: List<HiddenStemSet>,
    val dayStrength: DayStrengthSnapshot? = null,
)

data class DayStrengthSnapshot(
    val dayStem: String,
    val dayElement: FiveElement,
    val monthBranch: String,
    val monthElement: FiveElement?,
    val seasonSupport: Int,
    val rootSupport: Int,
    val peerSupport: Int,
    val resourceSupport: Int = 0,
    val drainScore: Int = 0,
    val controlScore: Int = 0,
    val totalScore: Int,
    val level: DayStrengthLevel,
    val note: String,
    val factors: List<String> = emptyList(),
)

enum class DayStrengthLevel {
    VeryWeak,
    WeakLeaning,
    BalancedLeaning,
    StrongLeaning,
    VeryStrong,
}

enum class CalendarType {
    Solar,
    Lunar,
}

enum class Gender {
    Female,
    Male,
    Unknown,
}

enum class YearPillarBoundary {
    Lichun,
}

enum class MonthPillarBoundary {
    SolarJieTerms,
}

enum class DayPillarBoundary {
    LocalMidnight,
    ZiHour23,
}

enum class HourPillarPolicy {
    StandardTwoHourBlocks,
    LateZiHour,
    EarlyLateZiSplit,
}

data class BirthDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int?,
    val minute: Int = 0,
)

data class BirthProfileInput(
    val name: String,
    val birthDateTime: BirthDateTime,
    val calendarType: CalendarType = CalendarType.Solar,
    val isLeapMonth: Boolean = false,
    val gender: Gender = Gender.Unknown,
    val timezoneId: String = KOREA_STANDARD_TIME_ZONE_ID,
    val placeName: String = "서울",
    val birthCountryCode: String = "KR",
    val birthRegionId: String? = null,
    val birthTimeZoneId: String = KOREA_STANDARD_TIME_ZONE_ID,
    val useBirthRegionSolarCorrection: Boolean = false,
    val solarCorrectionMinutes: Double = 0.0,
    val calculationBasisLabel: String = KOREA_STANDARD_TIME_LABEL,
)

data class CalculationPolicy(
    val code: String = "myeongri_kr_v2",
    val yearPillarBoundary: YearPillarBoundary = YearPillarBoundary.Lichun,
    val monthPillarBoundary: MonthPillarBoundary = MonthPillarBoundary.SolarJieTerms,
    val dayPillarBoundary: DayPillarBoundary = DayPillarBoundary.LocalMidnight,
    val hourPillarPolicy: HourPillarPolicy = HourPillarPolicy.StandardTwoHourBlocks,
    val useTrueSolarTime: Boolean = false,
)

enum class DataTrustLevel {
    GeneratedUnverified,
    InternalStructureChecked,
    ExternalAuthorityVerified,
}

data class CalculationEvidence(
    val timezoneId: String,
    val policyCode: String,
    val yearBoundary: String,
    val monthBoundary: String,
    val hourPolicy: String,
    val dataVersion: String,
    val isVerified: Boolean,
    val note: String,
    val previousSolarTermName: String? = null,
    val previousSolarTermAtKst: String? = null,
    val trustLevel: DataTrustLevel = DataTrustLevel.GeneratedUnverified,
    val externalVerificationSource: String? = null,
    val normalizedSolarDate: String? = null,
    val inputCalendarType: String? = null,
    val calculationBasisLabel: String = KOREA_STANDARD_TIME_LABEL,
    val birthRegionLabel: String? = null,
    val birthRegionSolarCorrectionMinutes: Double = 0.0,
    val birthRegionSolarCorrectionApplied: Boolean = false,
    val birthClockDateTime: String? = null,
    val calculationDateTime: String? = null,
    val dayPillarDate: String? = null,
    val solarCorrectionMethod: String = "NONE",
)

data class SajuChart(
    val name: String,
    val yearPillar: Pillar,
    val monthPillar: Pillar,
    val dayPillar: Pillar,
    val hourPillar: Pillar?,
    val dayMaster: String,
    val summary: String,
    val evidence: CalculationEvidence,
    val analysis: SajuAnalysisSnapshot? = null,
)

enum class FortuneScope {
    MajorLuck,
    YearLuck,
    MonthLuck,
    DayLuck,
    CycleRelation,
}

data class FortuneItem(
    val scope: FortuneScope,
    val title: String,
    val subtitle: String,
    val body: String,
    val startYear: Int? = null,
    val endYear: Int? = null,
    val startAge: Int? = null,
    val endAge: Int? = null,
    val ganji: String? = null,
)

data class MajorLuckStart(
    val isForward: Boolean,
    val startAge: Int,
    val startMonth: Int,
    val distanceDays: Long?,
    val distanceHours: Long?,
    val basisTermName: String?,
    val basisTermAtKst: String?,
    val note: String,
) {
    val label: String = "${startAge}세 ${startMonth}개월"
    val directionLabel: String = if (isForward) "순행" else "역행"
}

data class FortuneSnapshot(
    val generatedAtEpochMillis: Long,
    val currentMajorLuck: FortuneItem?,
    val currentYearLuck: FortuneItem?,
    val majorLuckStart: MajorLuckStart? = null,
    val majorLuckTimeline: List<FortuneItem> = emptyList(),
    val yearLuckTimeline: List<FortuneItem> = emptyList(),
    val cycleRelations: List<FortuneItem> = emptyList(),
    val majorLuckDirectionResolved: Boolean = true,
    val majorLuckDirectionNote: String? = null,
)

data class SavedSajuProfile(
    val schemaVersion: Int = 2,
    val id: String,
    val displayName: String,
    val subtitle: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val updatedAtLabel: String,
    val input: BirthProfileInput,
    val chartSnapshot: SajuChart,
    val fortuneSnapshot: FortuneSnapshot? = null,
    val isDefault: Boolean = false,
    val memo: String = "",
    val tags: List<String> = emptyList(),
)

data class CompatibilitySnapshot(
    val title: String,
    val summary: String,
    val dayRelation: String,
    val hourRelation: String? = null,
)

data class SavedCompatibilityRecord(
    val schemaVersion: Int = 2,
    val id: String,
    val firstProfileSnapshot: SavedSajuProfile,
    val secondProfileSnapshot: SavedSajuProfile,
    val resultSnapshot: CompatibilitySnapshot,
    val createdAtEpochMillis: Long,
    val updatedAtLabel: String,
)
