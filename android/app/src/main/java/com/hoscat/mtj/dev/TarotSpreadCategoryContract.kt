package com.hoscat.mtj.dev

import com.softcat.mystictarot.SpreadOption

internal data class TarotSpreadCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val layoutIds: Set<String>,
)

internal val tarotSpreadCategories = listOf(
    TarotSpreadCategory("single", "한 장형", "1장", setOf("one_card")),
    TarotSpreadCategory("horizontal", "가로형", "2~3장", setOf("two_cards", "three_cards")),
    TarotSpreadCategory(
        "grid",
        "격자형",
        "4~10장",
        setOf("four_cards", "six_cards", "eight_cards", "nine_cards", "ten_cards"),
    ),
    TarotSpreadCategory("cross", "십자형", "5~10장", setOf("five_cross", "mini_celtic", "celtic_cross")),
    TarotSpreadCategory("v", "V형", "5장", setOf("tarot_v")),
    TarotSpreadCategory(
        "curve",
        "곡선형",
        "7~8장",
        setOf("horseshoe", "magic_seven", "crow_seven", "crow_eight"),
    ),
    TarotSpreadCategory("circle", "원형", "8장", setOf("wheel_of_fortune")),
    TarotSpreadCategory(
        "branches",
        "두 갈래형",
        "5~6장",
        setOf("relationship_clearing", "either_or_five"),
    ),
)

internal fun tarotOptionsForCategory(
    category: TarotSpreadCategory,
    options: List<SpreadOption>,
): List<SpreadOption> = options.filter { it.layoutId in category.layoutIds }

internal fun tarotCategoryFor(option: SpreadOption): TarotSpreadCategory? =
    tarotSpreadCategories.singleOrNull { option.layoutId in it.layoutIds }
