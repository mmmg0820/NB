package com.hoscat.mtj.dev

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription

internal fun Modifier.tarotCardChoiceSemantics(
    position: Int,
    selectedIndex: Int,
    canSelect: Boolean,
    onCardTapped: () -> Unit,
): Modifier = clearAndSetSemantics {
    // Keep the card name and action on one node even when its ordinal badge is present.
    role = Role.Button
    contentDescription = "카드 ${position + 1}"
    selected = selectedIndex >= 0
    stateDescription = when {
        selectedIndex >= 0 -> "${selectedIndex + 1}번째 선택"
        !canSelect -> "필요한 장수 선택 완료"
        else -> "선택 안 됨"
    }
    if (!canSelect) disabled()
    onClick(label = if (selectedIndex >= 0) "선택 취소" else "카드 선택") {
        if (canSelect) onCardTapped()
        canSelect
    }
}
