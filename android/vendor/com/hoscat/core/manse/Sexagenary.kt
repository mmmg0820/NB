package com.hoscat.core.manse

import com.hoscat.core.model.Pillar

internal object Sexagenary {
    private val stems = listOf("갑", "을", "병", "정", "무", "기", "경", "신", "임", "계")
    private val branches = listOf("자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해")

    fun pillarAt(index: Int, label: String): Pillar {
        val normalized = index.floorMod(60)
        return Pillar(
            stem = stems[normalized % 10],
            branch = branches[normalized % 12],
            label = label,
        )
    }

    fun yearPillar(year: Int): Pillar = pillarAt(year - 4, "년주")

    fun monthPillar(yearStem: String, monthBranchIndex: Int): Pillar {
        val firstTigerStemIndex = when (yearStem) {
            "갑", "기" -> 2
            "을", "경" -> 4
            "병", "신" -> 6
            "정", "임" -> 8
            else -> 0
        }
        val stemIndex = (firstTigerStemIndex + monthBranchIndex).floorMod(10)
        val branchIndex = (2 + monthBranchIndex).floorMod(12)
        return Pillar(stems[stemIndex], branches[branchIndex], "월주")
    }

    fun hourPillar(dayStem: String, hourBranchIndex: Int): Pillar {
        val firstZiStemIndex = when (dayStem) {
            "갑", "기" -> 0
            "을", "경" -> 2
            "병", "신" -> 4
            "정", "임" -> 6
            else -> 8
        }
        val stemIndex = (firstZiStemIndex + hourBranchIndex).floorMod(10)
        return Pillar(stems[stemIndex], branches[hourBranchIndex.floorMod(12)], "시주")
    }

    private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus
}
