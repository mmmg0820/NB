package com.hoscat.core.manse

import com.hoscat.core.model.ElementCount
import com.hoscat.core.model.DayStrengthLevel
import com.hoscat.core.model.DayStrengthSnapshot
import com.hoscat.core.model.FiveElement
import com.hoscat.core.model.HiddenStemSet
import com.hoscat.core.model.Pillar
import com.hoscat.core.model.SajuAnalysisSnapshot
import com.hoscat.core.model.TenGodRelation

internal object SajuAnalysis {
    fun analyze(pillars: List<Pillar>, dayStem: String): SajuAnalysisSnapshot {
        val elementCounts = FiveElement.entries.map { element ->
            ElementCount(
                element = element,
                count = pillars.sumOf { pillar ->
                    listOfNotNull(stemElement(pillar.stem), branchElement(pillar.branch)).count { it == element }
                },
            )
        }
        return SajuAnalysisSnapshot(
            elementCounts = elementCounts,
            visibleTenGods = pillars.map { pillar ->
                TenGodRelation(
                    pillarLabel = pillar.label,
                    stem = pillar.stem,
                    relation = if (pillar.label == "일주") {
                        "일원"
                    } else {
                        tenGod(dayStem = dayStem, otherStem = pillar.stem)
                    },
                )
            },
            hiddenStems = pillars.map { pillar ->
                HiddenStemSet(
                    branch = pillar.branch,
                    stems = hiddenStems[pillar.branch].orEmpty(),
                )
            },
            dayStrength = dayStrength(pillars = pillars, dayStem = dayStem),
        )
    }

    private fun dayStrength(pillars: List<Pillar>, dayStem: String): DayStrengthSnapshot? {
        val day = stemInfo(dayStem) ?: return null
        val monthBranch = pillars.firstOrNull { it.label == "월주" }?.branch ?: return null
        val monthElement = branchElement(monthBranch)
        val seasonSupport = when {
            monthElement == day.element -> 3
            monthElement != null && generates(monthElement, day.element) -> 2
            monthElement != null && controls(monthElement, day.element) -> -2
            else -> 0
        }
        val rootSupport: Int = pillars.sumOf { pillar ->
            hiddenStems[pillar.branch].orEmpty().rootScoreFor(day.element)
        }
        val peerSupport: Int = pillars
            .filterNot { it.label == "일주" }
            .count { stemInfo(it.stem)?.element == day.element }
        val visibleResourceSupport: Int = pillars
            .filterNot { it.label == "일주" }
            .count { stemInfo(it.stem)?.let { other -> generates(other.element, day.element) } == true }
        val hiddenResourceSupport: Int = pillars
            .sumOf { pillar ->
                hiddenStems[pillar.branch].orEmpty()
                    .count { stem -> stemInfo(stem)?.let { other -> generates(other.element, day.element) } == true }
            }
            .coerceAtMost(2)
        val resourceSupport = visibleResourceSupport + hiddenResourceSupport
        val drainScore = pillars.sumOf { pillar ->
            val visible = stemInfo(pillar.stem)?.element?.let { if (generates(day.element, it) || controls(day.element, it)) 1 else 0 } ?: 0
            val hidden = hiddenStems[pillar.branch].orEmpty()
                .count { stem -> stemInfo(stem)?.element?.let { generates(day.element, it) || controls(day.element, it) } == true }
            visible + hidden.coerceAtMost(1)
        }
        val controlScore = pillars.sumOf { pillar ->
            val visible = stemInfo(pillar.stem)?.element?.let { if (controls(it, day.element)) 1 else 0 } ?: 0
            val hidden = hiddenStems[pillar.branch].orEmpty()
                .count { stem -> stemInfo(stem)?.element?.let { controls(it, day.element) } == true }
            visible + hidden.coerceAtMost(1)
        }
        val totalScore: Int = seasonSupport + rootSupport + peerSupport + resourceSupport - drainScore - controlScore
        val level = when {
            totalScore <= -2 -> DayStrengthLevel.VeryWeak
            totalScore <= 0 -> DayStrengthLevel.WeakLeaning
            totalScore <= 4 -> DayStrengthLevel.BalancedLeaning
            totalScore <= 7 -> DayStrengthLevel.StrongLeaning
            else -> DayStrengthLevel.VeryStrong
        }
        val factors = listOf(
            "월령 ${monthBranch}${monthElement?.let { "(${it.label()})" }.orEmpty()} ${seasonSupport.signed()}",
            "통근 ${rootSupport.signed()}",
            "비겁 ${peerSupport.signed()}",
            "인성 ${resourceSupport.signed()}",
            "설기·재성 -$drainScore",
            "관성 압박 -$controlScore",
        )
        return DayStrengthSnapshot(
            dayStem = dayStem,
            dayElement = day.element,
            monthBranch = monthBranch,
            monthElement = monthElement,
            seasonSupport = seasonSupport,
            rootSupport = rootSupport,
            peerSupport = peerSupport,
            resourceSupport = resourceSupport,
            drainScore = drainScore,
            controlScore = controlScore,
            totalScore = totalScore,
            level = level,
            note = "월령, 통근, 인성·비겁의 생조와 식상·재성·관성의 소모를 나눠 본 일간 힘 분석입니다. 용신/기신 확정값이 아니라 원국을 읽기 위한 기준값입니다.",
            factors = factors,
        )
    }

    private fun List<String>.rootScoreFor(dayElement: FiveElement): Int {
        val main = firstOrNull()
        val subRoots = drop(1)
        val mainScore = if (stemInfo(main.orEmpty())?.element == dayElement) 2 else 0
        val subScore = subRoots.count { stemInfo(it)?.element == dayElement }
        return (mainScore + subScore).coerceAtMost(3)
    }

    private fun tenGod(dayStem: String, otherStem: String): String {
        val day = stemInfo(dayStem) ?: return "미상"
        val other = stemInfo(otherStem) ?: return "미상"
        val samePolarity = day.isYang == other.isYang
        return when {
            day.element == other.element && samePolarity -> "비견"
            day.element == other.element -> "겁재"
            generates(day.element, other.element) && samePolarity -> "식신"
            generates(day.element, other.element) -> "상관"
            generates(other.element, day.element) && samePolarity -> "편인"
            generates(other.element, day.element) -> "정인"
            controls(day.element, other.element) && samePolarity -> "편재"
            controls(day.element, other.element) -> "정재"
            controls(other.element, day.element) && samePolarity -> "편관"
            controls(other.element, day.element) -> "정관"
            else -> "미상"
        }
    }

    private fun stemInfo(stem: String): StemInfo? = when (stem) {
        "갑" -> StemInfo(FiveElement.Wood, true)
        "을" -> StemInfo(FiveElement.Wood, false)
        "병" -> StemInfo(FiveElement.Fire, true)
        "정" -> StemInfo(FiveElement.Fire, false)
        "무" -> StemInfo(FiveElement.Earth, true)
        "기" -> StemInfo(FiveElement.Earth, false)
        "경" -> StemInfo(FiveElement.Metal, true)
        "신" -> StemInfo(FiveElement.Metal, false)
        "임" -> StemInfo(FiveElement.Water, true)
        "계" -> StemInfo(FiveElement.Water, false)
        else -> null
    }

    private fun stemElement(stem: String): FiveElement? = stemInfo(stem)?.element

    private fun branchElement(branch: String): FiveElement? = when (branch) {
        "인", "묘" -> FiveElement.Wood
        "사", "오" -> FiveElement.Fire
        "진", "술", "축", "미" -> FiveElement.Earth
        "신", "유" -> FiveElement.Metal
        "해", "자" -> FiveElement.Water
        else -> null
    }

    private fun generates(source: FiveElement, target: FiveElement): Boolean = when (source) {
        FiveElement.Wood -> target == FiveElement.Fire
        FiveElement.Fire -> target == FiveElement.Earth
        FiveElement.Earth -> target == FiveElement.Metal
        FiveElement.Metal -> target == FiveElement.Water
        FiveElement.Water -> target == FiveElement.Wood
    }

    private fun controls(source: FiveElement, target: FiveElement): Boolean = when (source) {
        FiveElement.Wood -> target == FiveElement.Earth
        FiveElement.Fire -> target == FiveElement.Metal
        FiveElement.Earth -> target == FiveElement.Water
        FiveElement.Metal -> target == FiveElement.Wood
        FiveElement.Water -> target == FiveElement.Fire
    }

    private fun Int.signed(): String = if (this > 0) "+$this" else toString()

    private fun FiveElement.label(): String = when (this) {
        FiveElement.Wood -> "목"
        FiveElement.Fire -> "화"
        FiveElement.Earth -> "토"
        FiveElement.Metal -> "금"
        FiveElement.Water -> "수"
    }

    private data class StemInfo(
        val element: FiveElement,
        val isYang: Boolean,
    )

    private val hiddenStems = mapOf(
        "자" to listOf("계"),
        "축" to listOf("기", "계", "신"),
        "인" to listOf("갑", "병", "무"),
        "묘" to listOf("을"),
        "진" to listOf("무", "을", "계"),
        "사" to listOf("병", "무", "경"),
        "오" to listOf("정", "기"),
        "미" to listOf("기", "정", "을"),
        "신" to listOf("경", "임", "무"),
        "유" to listOf("신"),
        "술" to listOf("무", "신", "정"),
        "해" to listOf("임", "갑"),
    )
}
