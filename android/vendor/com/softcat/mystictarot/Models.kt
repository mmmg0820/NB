package com.softcat.mystictarot

import com.softcat.mystictarot.ui.theme.ThemeChoice

const val APP_SETTINGS_SCHEMA_VERSION = 4
const val CUSTOM_DECK_SCHEMA_VERSION = 3
const val SAVED_READING_SCHEMA_VERSION = 3
const val DEFAULT_CUSTOM_CARD_MEANING = "개인 덱 카드입니다. 카드 이미지와 질문, 스프레드 위치를 함께 보고 해석하세요."

data class TarotCard(
    val id: Int,
    val nameEn: String,
    val nameKr: String,
    val arcana: String,
    val basicMeaning: String,
    val uprightKeywords: List<String> = emptyList(),
    val reversedKeywords: List<String> = emptyList(),
    val imageUri: String? = null
)

fun hasDisplayableCardMeaning(card: TarotCard): Boolean {
    val meaning = card.basicMeaning.trim()
    return meaning.isNotBlank() && meaning != DEFAULT_CUSTOM_CARD_MEANING
}

data class TarotDeck(
    val id: String,
    val name: String,
    val cards: List<TarotCard>,
    val enabled: Boolean = true,
    val aiPrompt: String = "",
    val schemaVersion: Int = CUSTOM_DECK_SCHEMA_VERSION
)

data class SpreadOption(
    val key: String,
    val title: String,
    val subtitle: String,
    val cardCount: Int,
    val drawMode: SpreadDrawMode = SpreadDrawMode.Normal,
    val layoutId: String,
    val layoutTitle: String,
    val layoutSubtitle: String,
    val positionPresetTitle: String,
    val positionLabels: List<String>
)

enum class SpreadDrawMode {
    Normal,
    FinalOneFromTen
}

enum class CardDirection(val label: String) {
    Upright("정방향"),
    Reversed("역방향")
}

data class DrawnCard(
    val card: TarotCard,
    val direction: CardDirection,
    val order: Int
)

data class SavedReading(
    val id: Long,
    val savedAt: Long,
    val title: String,
    val spreadTitle: String,
    val layoutTitle: String = spreadTitle,
    val positionPresetTitle: String = spreadTitle,
    val question: String = "",
    val interpretation: String = "",
    val deckId: String = "standard",
    val deckName: String = "유니버셜 타로",
    val deckAiPromptSnapshot: String = "",
    val cards: List<SavedReadingCard>,
    val schemaVersion: Int = SAVED_READING_SCHEMA_VERSION
)

data class SavedReadingCard(
    val order: Int,
    val nameKr: String,
    val nameEn: String,
    val directionLabel: String,
    val positionLabel: String = "",
    val meaningSnapshot: String = "",
    val cardId: Int = -1,
    val imageUri: String? = null
)

data class SavedSpreadPreset(
    val id: String,
    val name: String,
    val baseSpreadKey: String,
    val cardCount: Int,
    val drawMode: SpreadDrawMode,
    val layoutId: String,
    val layoutTitle: String,
    val positionPresetTitle: String,
    val positionLabels: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)

enum class CardBackStyle(val label: String, val subtitle: String) {
    ThemeDefault("테마 기본", "선택한 테마 색을 따라갑니다"),
    HoscatInk("호스캣 잉크", "검은 로고와 아이보리 대비"),
    ClassicSlate("클래식 슬레이트", "차분한 안드로이드 기본 계열"),
    CrimsonSigil("홍염 인장", "정유 테마와 어울리는 절제된 홍염"),
    ForestWave("흑림 물결", "임인 테마와 어울리는 깊은 숲"),
    CustomImage("개인 이미지", "사용자가 고른 이미지")
}

data class AppSettings(
    val schemaVersion: Int = APP_SETTINGS_SCHEMA_VERSION,
    val themeChoice: ThemeChoice = ThemeChoice.HoscatSignature,
    val selectedDeckId: String? = null,
    val useReversed: Boolean = true,
    val recentSpreadKey: String? = null,
    val spreadUseCounts: Map<String, Int> = emptyMap(),
    val adsDisabled: Boolean = false,
    val activatedPurchaseCodeLabels: Set<String> = emptySet(),
    val cardBackStyle: CardBackStyle = CardBackStyle.ThemeDefault,
    val customCardBackUri: String? = null,
    val hapticsEnabled: Boolean = true,
    val customPositionLabelsBySpreadKey: Map<String, List<String>> = emptyMap()
)
