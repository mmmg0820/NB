package com.hoscat.mtj.dev

internal data class TarotSelectionState(
    val deckOrder: List<Int>,
    val selectedIds: List<Int>,
    val requiredCount: Int,
)

internal fun toggleTarotSelection(
    state: TarotSelectionState,
    cardId: Int,
): TarotSelectionState {
    require(state.requiredCount >= 0)
    val normalized = state.copy(
        selectedIds = state.selectedIds
            .distinct()
            .filter { it in state.deckOrder }
            .take(state.requiredCount),
    )
    if (cardId !in normalized.deckOrder) return normalized
    val nextSelected = when {
        cardId in normalized.selectedIds -> normalized.selectedIds - cardId
        normalized.selectedIds.size < normalized.requiredCount -> normalized.selectedIds + cardId
        else -> normalized.selectedIds
    }
    return normalized.copy(selectedIds = nextSelected)
}
