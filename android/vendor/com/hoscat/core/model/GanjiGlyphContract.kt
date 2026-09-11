package com.hoscat.core.model

const val GANJI_GLYPH_CONTRACT_VERSION = 1

data class GanjiGlyph(
    val hangul: String,
    val hanja: String,
)

enum class GanjiGlyphKind {
    HeavenlyStem,
    EarthlyBranch,
}

val heavenlyStemGlyphs: List<GanjiGlyph> = listOf(
    GanjiGlyph("갑", "甲"),
    GanjiGlyph("을", "乙"),
    GanjiGlyph("병", "丙"),
    GanjiGlyph("정", "丁"),
    GanjiGlyph("무", "戊"),
    GanjiGlyph("기", "己"),
    GanjiGlyph("경", "庚"),
    GanjiGlyph("신", "辛"),
    GanjiGlyph("임", "壬"),
    GanjiGlyph("계", "癸"),
)

val earthlyBranchGlyphs: List<GanjiGlyph> = listOf(
    GanjiGlyph("자", "子"),
    GanjiGlyph("축", "丑"),
    GanjiGlyph("인", "寅"),
    GanjiGlyph("묘", "卯"),
    GanjiGlyph("진", "辰"),
    GanjiGlyph("사", "巳"),
    GanjiGlyph("오", "午"),
    GanjiGlyph("미", "未"),
    GanjiGlyph("신", "申"),
    GanjiGlyph("유", "酉"),
    GanjiGlyph("술", "戌"),
    GanjiGlyph("해", "亥"),
)

val allGanjiGlyphs: List<GanjiGlyph> = heavenlyStemGlyphs + earthlyBranchGlyphs

private val stemHanjaByHangul = heavenlyStemGlyphs.associate { it.hangul to it.hanja }
private val branchHanjaByHangul = earthlyBranchGlyphs.associate { it.hangul to it.hanja }
private val ganjiHangulByHanja = allGanjiGlyphs.associate { it.hanja to it.hangul }

fun String.toGanjiHanja(kind: GanjiGlyphKind): String = when (kind) {
    GanjiGlyphKind.HeavenlyStem -> stemHanjaByHangul[this]
    GanjiGlyphKind.EarthlyBranch -> branchHanjaByHangul[this]
} ?: this

fun String.toGanjiHangul(): String = ganjiHangulByHanja[this] ?: this

fun String.isSupportedGanjiGlyph(): Boolean =
    this in stemHanjaByHangul || this in branchHanjaByHangul || this in ganjiHangulByHanja
