package com.hoscat.mtj.dev

import android.graphics.ImageDecoder
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mtj.design.MtjBottomActionScaffold
import com.mtj.design.MtjQuietPanel
import com.mtj.design.MtjSectionHeader
import com.mtj.design.MtjTokens
import com.mtj.tarot.*
import com.softcat.mystictarot.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The question is optional in the UI (validateTarotQuestion no longer requires it), but
 * [TarotRecreationState]'s own invariant still requires `questionAtStart` to be non-blank
 * and pre-trimmed (see its `validate()`). Rather than relaxing that state-machine contract —
 * which also guards serialization/restoration — a neutral placeholder is substituted here
 * only when the user left the field blank.
 */
private fun startableQuestion(question: String): String = question.trim().ifBlank { TAROT_QUESTION_NOT_PROVIDED }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TarotScreen(
    store: RecordStore,
    question: String,
    onQuestionChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    pendingSpreadKey: String? = null,
    onPendingSpreadKeyConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    var deck by remember { mutableStateOf<TarotDeck?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    val questionValidation = remember(question) { validateTarotQuestion(question) }
    var questionInputError by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingQuestionPreset by rememberSaveable { mutableStateOf<String?>(null) }
    var reversed by rememberSaveable { mutableStateOf(readIncludeReversedDefault(context)) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var session by rememberSaveable(stateSaver = TarotRecreationSaver) {
        mutableStateOf(TarotRecreationState())
    }
    val restoration = remember(deck, session) { deck?.let { runCatching { session.materialize(it) } } }
    val restorationFailed = session.failed || restoration?.isFailure == true
    val restored = restoration?.getOrNull()
    val spread = restored?.spread
    val bridge = restored?.bridge
    val selected = session.selectedIds
    val result = restored?.result
    val saveRecord = restored?.record
    var saveMessage by remember(result) { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val normalSpreadOptions = remember {
        selectableSpreadOptions.filter { it.drawMode == SpreadDrawMode.Normal }
    }
    LaunchedEffect(retry) {
        error = null
        try { deck = withContext(Dispatchers.IO) { MtjTarotCatalog.load(context.assets) } }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (_: Exception) { error = "카드 데이터를 불러오지 못했습니다." }
    }
    LaunchedEffect(deck, pendingSpreadKey) {
        val readyDeck = deck ?: return@LaunchedEffect
        val key = pendingSpreadKey ?: return@LaunchedEffect
        val option = normalSpreadOptions.firstOrNull { it.key == key }
        if (option != null && !session.started) {
            session = TarotRecreationState.start(readyDeck, option, startableQuestion(question), reversed)
            selectedCategoryId = tarotCategoryFor(option)?.id
        }
        onPendingSpreadKeyConsumed()
    }
    fun restart() {
        selectedCategoryId = spread?.let(::tarotCategoryFor)?.id
        session = TarotRecreationState(spreadKey = spread?.key.orEmpty())
    }
    val selectingCards = bridge != null && result == null
    BackHandler(enabled = selectingCards) {
        selectedCategoryId = spread?.let(::tarotCategoryFor)?.id
        session = TarotRecreationState(spreadKey = spread?.key.orEmpty())
    }
    BackHandler(enabled = result != null) {
        session = session.reopenSelection()
    }
    MtjBottomActionScaffold(
        selectedTab = 2,
        onTabSelected = onTabSelected,
        showActionDock = restorationFailed || result != null,
        showNavigation = !selectingCards,
        actions = {
        if (restorationFailed) {
            OutlinedButton({ restart() }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("새로 시작") }
        } else if (result != null) {
            OutlinedButton({ restart() }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("새로 뽑기") }
            Spacer(Modifier.height(8.dp))
            Button({
                val record = saveRecord ?: return@Button
                saving = true
                scope.launch {
                    try { store.insert(listOf(record)); saveMessage = "기록에 저장했습니다." }
                    catch (e: kotlinx.coroutines.CancellationException) { throw e }
                    catch (_: Exception) { saveMessage = "저장하지 못했습니다. 다시 시도해주세요." }
                    finally { saving = false }
                }
            }, enabled = !saving, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (saving) "저장 중" else "저장") }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                restorationFailed -> Text("이전 카드 선택이나 결과를 복원하지 못했습니다. 새로 시작해주세요.")
                error != null -> { Text(checkNotNull(error)); TextButton({ retry++ }) { Text("다시 시도") } }
                deck == null -> CircularProgressIndicator()
                result != null -> {
                    val snapshot = checkNotNull(result)
                    val reduceMotion = remember { readReduceMotionDefault(context) }
                    var revealed by remember(snapshot) { mutableStateOf(reduceMotion) }
                    LaunchedEffect(snapshot) { revealed = true }
                    val revealProgress by animateFloatAsState(
                        targetValue = if (revealed) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = if (reduceMotion) {
                                MtjTokens.ReducedMotionMillis
                            } else {
                                MtjTokens.GatherMillis + MtjTokens.SpreadMillis + MtjTokens.SettleMillis
                            },
                        ),
                        label = "tarot-result-reveal",
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .graphicsLayer {
                                alpha = revealProgress
                                val scale = 0.96f + 0.04f * revealProgress
                                scaleX = scale
                                scaleY = scale
                            },
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            TarotReadingSummaryPanel(tarotReadingSummaryLines(snapshot))
                        }
                        saveMessage?.let { message -> item { Text(message) } }
                        item {
                            MtjQuietPanel(Modifier.fillParentMaxWidth()) {
                                TarotSpreadOverview(snapshot, Modifier.fillMaxWidth())
                            }
                        }
                        items(snapshot.reading.cards, key = { it.order }) { card ->
                            var meaningOpen by rememberSaveable(card.order) { mutableStateOf(false) }
                            MtjQuietPanel {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("${card.order}. ${card.positionLabel}", style = MaterialTheme.typography.titleMedium)
                                    TextButton({ meaningOpen = !meaningOpen }) {
                                        Text(if (meaningOpen) "접기" else "해석 보기")
                                    }
                                }
                                TarotArt(
                                    card.cardId,
                                    card.nameKr,
                                    imageRotationDegrees = detailTarotCardRotation(card.directionLabel),
                                    detailHeight = 248.dp,
                                )
                                Text("${card.nameKr} · ${card.directionLabel}", style = MaterialTheme.typography.titleMedium)
                                if (meaningOpen) {
                                    Text(card.meaningSnapshot, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                bridge != null -> {
                    val current = checkNotNull(bridge)
                    val currentSpread = checkNotNull(spread)
                    val deckOrder = remember(current.shuffledCards) { current.shuffledCards.map { it.id } }
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "카드 고르기",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${selected.size} / ${currentSpread.cardCount}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.semantics {
                                        contentDescription = "${selected.size}장 선택됨, ${currentSpread.cardCount}장 필요"
                                    },
                                )
                                IconButton(
                                    onClick = {
                                    session = session.reshufflePreservingSelection(checkNotNull(deck))
                                    },
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "다시 섞기")
                                }
                            }
                        }
                        BoxWithConstraints(
                            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 2.dp),
                        ) {
                            val gap = 2.dp
                            val spec = remember(maxWidth, maxHeight, current.shuffledCards.size) {
                                tarotOverviewGridSpec(maxWidth.value, maxHeight.value, current.shuffledCards.size)
                            }
                            val rows = remember(current.shuffledCards, spec.columns) {
                                current.shuffledCards.chunked(spec.columns)
                            }
                            val cellWidth = ((maxWidth - gap * (spec.columns - 1)) / spec.columns).coerceAtLeast(1.dp)
                            val cellHeight = ((maxHeight - gap * (spec.rows - 1)) / spec.rows).coerceAtLeast(1.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                                rows.forEachIndexed { rowIndex, rowCards ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                                    ) {
                                        rowCards.forEachIndexed { columnIndex, card ->
                                            TarotDeckCardChoice(
                                                card = card,
                                                position = rowIndex * spec.columns + columnIndex,
                                                selectedIds = selected,
                                                requiredCount = currentSpread.cardCount,
                                                onCardTapped = { tapped ->
                                                    val next = toggleTarotSelection(
                                                        TarotSelectionState(
                                                            deckOrder = deckOrder,
                                                            selectedIds = session.selectedIds,
                                                            requiredCount = currentSpread.cardCount,
                                                        ),
                                                        tapped.id,
                                                    )
                                                    session = session.copy(selectedIds = next.selectedIds)
                                                },
                                                modifier = Modifier.size(cellWidth, cellHeight),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(56.dp)) {
                            if (selected.size == currentSpread.cardCount) {
                                var drawerDrag by remember { mutableFloatStateOf(0f) }
                                val swipeThreshold = with(LocalDensity.current) { 24.dp.toPx() }
                                val openResult = { session = session.finish(checkNotNull(deck)) }
                                Surface(
                                    onClick = openResult,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(session.selectedIds) {
                                            detectVerticalDragGestures(
                                                onDragStart = { drawerDrag = 0f },
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    drawerDrag += dragAmount
                                                },
                                                onDragEnd = {
                                                    if (drawerDrag <= -swipeThreshold) openResult()
                                                },
                                            )
                                        }
                                        .testTag("tarot-result-drawer-peek"),
                                    shape = RoundedCornerShape(
                                        topStart = MtjTokens.SheetCorner,
                                        topEnd = MtjTokens.SheetCorner,
                                    ),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Spacer(
                                            Modifier
                                                .padding(top = 8.dp)
                                                .size(width = 32.dp, height = 4.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.35f),
                                                    RoundedCornerShape(2.dp),
                                                ),
                                        )
                                        Row(
                                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text("결과 서랍", style = MaterialTheme.typography.titleSmall)
                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "결과 열기")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(top = 18.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (selectedCategoryId != null) {
                        item {
                            TextButton(
                                onClick = { selectedCategoryId = null },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                Text("모양 다시 선택")
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                selectedCategoryId?.let { id ->
                                    tarotSpreadCategories.firstOrNull { it.id == id }?.title
                                } ?: "스프레드 모양",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                if (selectedCategoryId == null) "8개 형태 · 27개 리딩" else "리딩 목적을 선택하세요",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (selectedCategoryId != null) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(question, {
                                if (acceptsTarotQuestionInput(it)) {
                                    onQuestionChange(it)
                                    questionInputError = null
                                } else {
                                    questionInputError = "붙여넣을 내용이 너무 깁니다. 240자 이내로 줄여 다시 입력해주세요."
                                }
                            }, isError = questionInputError != null || (question.isNotBlank() && questionValidation.error != null),
                                label = { Text("질문 (선택)") },
                                placeholder = { Text("오늘 무엇을 묻고 싶나요?") },
                                supportingText = if (question.isNotBlank() || questionInputError != null) {{
                                    Text(questionInputError ?: questionValidation.error ?: "${questionValidation.codePointCount}/$MAX_TAROT_QUESTION_CODE_POINTS")
                                }} else null,
                                modifier = Modifier.fillMaxWidth())
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(reversed, { reversed = it })
                                    Text("역방향 포함", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    if (selectedCategoryId == null) {
                        items(tarotSpreadCategories, key = { it.id }) { category ->
                            val options = tarotOptionsForCategory(category, normalSpreadOptions)
                            val representative = options.first()
                            Surface(
                                onClick = { selectedCategoryId = category.id },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(0.dp),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(category.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${category.subtitle} · ${options.size}개 리딩",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    TarotSpreadMiniPreview(representative, Modifier.width(92.dp).height(48.dp))
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                                }
                            }
                        }
                    } else {
                        val selectedCategory = tarotSpreadCategories.first { it.id == selectedCategoryId }
                        items(
                            tarotOptionsForCategory(selectedCategory, normalSpreadOptions),
                            key = { it.key },
                        ) { option ->
                            Surface(
                                onClick = {
                                    if (questionValidation.canStart && questionInputError == null) {
                                        session = TarotRecreationState.start(
                                            checkNotNull(deck),
                                            option,
                                            startableQuestion(question),
                                            reversed,
                                        )
                                    } else {
                                        questionInputError = questionValidation.error
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 82.dp),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(0.dp),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(option.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${option.cardCount}장 · ${option.subtitle}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    TarotSpreadMiniPreview(option, Modifier.width(112.dp).height(64.dp))
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarotSelectionBoard(
    spread: SpreadOption,
    shuffledCards: List<TarotCard>,
    selectedIds: List<Int>,
    modifier: Modifier = Modifier,
) {
    val slots = remember(spread.key, spread.cardCount) { spreadSlots(spread, spread.cardCount) }
    val selectedCards = remember(shuffledCards, selectedIds) {
        selectedIds.mapNotNull { id -> shuffledCards.firstOrNull { it.id == id } }
    }
    val maxX = slots.maxOfOrNull { it.x }?.coerceAtLeast(0f) ?: 0f
    val maxY = slots.maxOfOrNull { it.y }?.coerceAtLeast(0f) ?: 0f
    BoxWithConstraints(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(MtjTokens.ControlCorner))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(MtjTokens.ControlCorner))
            .padding(10.dp)
            .semantics { contentDescription = "${spread.title}, ${spread.cardCount}장 스프레드 무대" },
    ) {
        val slotWidth = 34.dp
        val slotHeight = slotWidth / TAROT_CARD_ASPECT_RATIO
        val travelX = (maxWidth - slotWidth).coerceAtLeast(0.dp)
        val travelY = (maxHeight - slotHeight).coerceAtLeast(0.dp)
        slots.forEachIndexed { index, slot ->
            val xFraction = if (maxX == 0f) 0.5f else slot.x / maxX
            val yFraction = if (maxY == 0f) 0.5f else slot.y / maxY
            val card = selectedCards.getOrNull(index)
            val label = spread.positionLabels.getOrNull(index) ?: "${index + 1}번째 위치"
            Box(
                Modifier
                    .offset(x = travelX * xFraction, y = travelY * yFraction)
                    .size(slotWidth, slotHeight)
                    .rotate(slot.rotation)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(MtjTokens.TarotFrameCorner))
                    .border(
                        if (card == null) 1.dp else 2.dp,
                        if (card == null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(MtjTokens.TarotFrameCorner),
                    )
                    .semantics {
                        contentDescription = if (card == null) {
                            "${index + 1}번 슬롯, $label, 비어 있음"
                        } else {
                            "${index + 1}번 슬롯, $label, ${card.nameKr}, ${index + 1}번째 선택"
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (card == null) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelSmall)
                } else {
                    TarotArt(card.id, card.nameKr, Modifier.fillMaxSize().padding(2.dp), fixedBounds = true)
                    TarotSpreadNumberBadge(
                        order = index + 1,
                        modifier = Modifier.align(Alignment.TopStart).size(18.dp).zIndex(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TarotDeckCardChoice(
    card: TarotCard,
    position: Int,
    selectedIds: List<Int>,
    requiredCount: Int,
    onCardTapped: (TarotCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = selectedIds.indexOf(card.id)
    val canSelect = selectedIndex >= 0 || selectedIds.size < requiredCount
    Surface(
        onClick = { if (canSelect) onCardTapped(card) },
        enabled = canSelect,
        shape = RoundedCornerShape(MtjTokens.TarotFrameCorner),
        color = Color.Transparent,
        modifier = modifier.semantics {
            role = Role.Button
            selected = selectedIndex >= 0
            contentDescription = "카드 ${position + 1}"
            stateDescription = if (selectedIndex >= 0) {
                "${selectedIndex + 1}번째 선택"
            } else if (!canSelect) {
                "필요한 장수 선택 완료"
            } else {
                "선택 안 됨"
            }
        },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(1.dp), contentAlignment = Alignment.Center) {
            val cardWidth = minOf(maxWidth, maxHeight * TAROT_CARD_ASPECT_RATIO)
            val cardHeight = cardWidth / TAROT_CARD_ASPECT_RATIO
            Box(
                Modifier
                    .size(cardWidth, cardHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .border(
                        if (selectedIndex >= 0) 2.dp else 1.dp,
                        if (selectedIndex >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(3.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                TarotCardBack(Modifier.fillMaxSize())
                if (selectedIndex >= 0) {
                    Text(
                        "${selectedIndex + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun TarotCardBack(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.tarot_back_mint),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun TarotSpreadOverview(snapshot: MtjResultSnapshot, modifier: Modifier = Modifier) {
    val cards = snapshot.reading.cards
    val labels = remember(cards) {
        cards.map { card ->
            formatTarotSpreadOverviewLabel(
                order = card.order,
                positionLabel = card.positionLabel,
                cardName = card.nameKr,
                directionLabel = card.directionLabel,
            )
        }
    }
    val spreadContractMatches = cards.size == snapshot.spread.cardCount &&
        cards.map { it.order } == (1..snapshot.spread.cardCount).toList() &&
        cards.map { it.positionLabel } == snapshot.spread.positionLabels
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall
    val minimumReadableLabelWidth = (listOf("가나다라") + labels.flatMap { it.readableWidthCandidates })
        .maxOf { candidate ->
            textMeasurer.measure(
                text = AnnotatedString(candidate),
                style = labelStyle,
                softWrap = false,
                maxLines = 1,
            ).size.width
        }
        .toFloat()
    BoxWithConstraints(modifier) {
        val availableWidthPx = constraints.maxWidth.toFloat()
        val gaps = with(density) {
            SpreadGeometryGaps(
                horizontal = 8.dp.toPx(),
                vertical = 10.dp.toPx(),
                cardToLabel = 6.dp.toPx(),
                labelToLabel = 4.dp.toPx(),
            )
        }
        val initialFirstPass = remember(snapshot.spread, availableWidthPx, gaps, spreadContractMatches) {
            if (spreadContractMatches) {
                calculateSpreadGeometry(
                    snapshot.spread,
                    availableWidthPx,
                    TAROT_CARD_ASPECT_RATIO,
                    List(snapshot.spread.cardCount) { 0f },
                    gaps,
                )
            } else {
                SpreadGeometryResult.Unsupported(SpreadGeometryStatus.DefinitionMismatch)
            }
        }
        val initialGeometry = (initialFirstPass as? SpreadGeometryResult.Success)?.geometry
        val geometryWidthPx = effectiveTarotSpreadGeometryWidth(
            availableWidth = availableWidthPx,
            cardCount = snapshot.spread.cardCount,
            firstCardWidth = initialGeometry?.cardWidth ?: 0f,
            singleCardMaximumWidth = with(density) { 180.dp.toPx() },
        )
        val firstPass = remember(snapshot.spread, geometryWidthPx, gaps, initialFirstPass) {
            if (geometryWidthPx < availableWidthPx && initialGeometry != null) {
                calculateSpreadGeometry(
                    snapshot.spread,
                    geometryWidthPx,
                    TAROT_CARD_ASPECT_RATIO,
                    List(snapshot.spread.cardCount) { 0f },
                    gaps,
                )
            } else {
                initialFirstPass
            }
        }
        val firstGeometry = (firstPass as? SpreadGeometryResult.Success)?.geometry
        val labelHeights = firstGeometry?.let { geometry ->
            val labelWidth = geometry.cardWidth.roundToInt().coerceAtLeast(1)
            labels.map { label ->
                textMeasurer.measure(
                    text = AnnotatedString(label.text),
                    style = labelStyle,
                    overflow = TextOverflow.Clip,
                    softWrap = true,
                    maxLines = Int.MAX_VALUE,
                    constraints = Constraints(maxWidth = labelWidth),
                ).size.height.toFloat()
            }
        }
        val secondPass = remember(snapshot.spread, geometryWidthPx, gaps, labelHeights) {
            if (labelHeights != null) {
                calculateSpreadGeometry(
                    snapshot.spread,
                    geometryWidthPx,
                    TAROT_CARD_ASPECT_RATIO,
                    labelHeights,
                    gaps,
                )
            } else {
                firstPass
            }
        }
        val limits = with(density) {
            TarotSpreadOverviewLimits(
                minimumCardWidth = 40.dp.toPx(),
                maximumCanvasHeight = min(1000.dp.toPx(), availableWidthPx * 4f),
                maximumLabelSeparation = 24.dp.toPx(),
            )
        }
        val mode = chooseTarotSpreadOverviewMode(
            result = secondPass,
            availableWidth = geometryWidthPx,
            expectedCardCount = cards.size,
            limits = limits,
            overlapAllowance = tarotSpreadOverviewOverlapAllowance(snapshot.spread.layoutId),
            minimumReadableLabelWidth = minimumReadableLabelWidth,
        ).let { selectedMode ->
            if (density.fontScale >= 1.3f) TarotSpreadOverviewMode.OrderedFallback else selectedMode
        }
        val geometry = (secondPass as? SpreadGeometryResult.Success)?.geometry
        if (mode == TarotSpreadOverviewMode.Spatial && geometry != null) {
            SpatialTarotSpreadOverview(
                snapshot = snapshot,
                labels = labels,
                geometry = geometry,
                labelStyle = labelStyle,
                horizontalOffsetPx = ((availableWidthPx - geometry.canvasExtent.width) / 2f).coerceAtLeast(0f),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OrderedTarotSpreadOverview(snapshot, labels, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SpatialTarotSpreadOverview(
    snapshot: MtjResultSnapshot,
    labels: List<TarotSpreadOverviewLabel>,
    geometry: SpreadGeometry,
    labelStyle: androidx.compose.ui.text.TextStyle,
    horizontalOffsetPx: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val canvasHeight = with(density) { ceil(geometry.canvasExtent.height.toDouble()).toFloat().toDp() }
    val celticCenterPair = snapshot.spread.layoutId in setOf("mini_celtic", "celtic_cross") &&
        geometry.placements.size >= 2
    Box(modifier.height(canvasHeight)) {
        geometry.placements.forEachIndexed { index, placement ->
            val card = snapshot.reading.cards[index]
            val rotation = combinedTarotCardRotation(placement.rotationDegrees, card.directionLabel)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (placement.cardBounds.left + horizontalOffsetPx).roundToInt(),
                            placement.cardBounds.top.roundToInt(),
                        )
                    }
                    .size(
                        with(density) { geometry.cardWidth.toDp() },
                        with(density) { geometry.cardHeight.toDp() },
                    )
                    .zIndex(index.toFloat())
                    .clearAndSetSemantics { },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = rotation
                            clip = false
                        }
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(MtjTokens.TarotFrameCorner))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(MtjTokens.TarotFrameCorner)),
                ) {
                    TarotArt(card.cardId, card.nameKr, Modifier.fillMaxSize().padding(2.dp), fixedBounds = true)
                }
                if (!celticCenterPair || index !in 0..1) {
                    TarotSpreadNumberBadge(
                        order = card.order,
                        modifier = Modifier.align(Alignment.TopStart).size(20.dp).zIndex(1f),
                    )
                }
            }
            Text(
                labels[index].positionLine,
                style = labelStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (placement.labelBounds.left + horizontalOffsetPx).roundToInt(),
                            placement.labelBounds.top.roundToInt(),
                        )
                    }
                    .width(with(density) { placement.labelBounds.width.toDp() })
                    .clearAndSetSemantics { },
            )
        }
        if (celticCenterPair) {
            val badgeSizePx = with(density) { 20.dp.toPx() }
            geometry.placements.take(2).forEachIndexed { index, placement ->
                val badgeLeft = if (index == 0) {
                    placement.cardBounds.left
                } else {
                    placement.cardBounds.right - badgeSizePx
                }
                TarotSpreadNumberBadge(
                    order = snapshot.reading.cards[index].order,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (badgeLeft + horizontalOffsetPx).roundToInt(),
                                placement.cardBounds.top.roundToInt(),
                            )
                        }
                        .size(20.dp)
                        .zIndex((geometry.placements.size + index).toFloat())
                        .clearAndSetSemantics { },
                )
            }
        }
    }
}

@Composable
private fun TarotSpreadMiniPreview(option: SpreadOption, modifier: Modifier = Modifier) {
    val slots = remember(option.key) { spreadSlots(option, option.cardCount) }
    val maxX = slots.maxOfOrNull { it.x }?.coerceAtLeast(0f) ?: 0f
    val maxY = slots.maxOfOrNull { it.y }?.coerceAtLeast(0f) ?: 0f
    BoxWithConstraints(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(MtjTokens.ControlCorner))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(MtjTokens.ControlCorner))
            .padding(5.dp),
    ) {
        val cardWidth = 11.dp
        val cardHeight = 18.dp
        val travelX = (maxWidth - cardWidth).coerceAtLeast(0.dp)
        val travelY = (maxHeight - cardHeight).coerceAtLeast(0.dp)
        slots.forEachIndexed { index, slot ->
            val xFraction = if (maxX == 0f) 0.5f else slot.x / maxX
            val yFraction = if (maxY == 0f) 0.5f else slot.y / maxY
            Box(
                Modifier
                    .offset(x = travelX * xFraction, y = travelY * yFraction)
                    .size(cardWidth, cardHeight)
                    .rotate(slot.rotation)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(2.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TarotSpreadNumberBadge(order: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("$order", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun OrderedTarotSpreadOverview(
    snapshot: MtjResultSnapshot,
    labels: List<TarotSpreadOverviewLabel>,
    modifier: Modifier = Modifier,
) {
    val cardWidth = 40.dp
    val rotationReserve = (
        minimumSquareReserveForRotatingCard(40f, TAROT_CARD_ASPECT_RATIO) + 4f
    ).dp
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        snapshot.reading.cards.forEachIndexed { index, card ->
            val label = labels[index]
            val rotation = combinedTarotCardRotation(
                snapshot.slots.getOrNull(index)?.rotation ?: 0f,
                card.directionLabel,
            )
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp).semantics(mergeDescendants = true) {
                    contentDescription = "${card.order}번 카드, ${card.positionLabel}, ${card.nameKr}, ${card.directionLabel}"
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(rotationReserve), contentAlignment = Alignment.Center) {
                    TarotArt(
                        card.cardId,
                        card.nameKr,
                        modifier = Modifier
                            .width(cardWidth)
                            .aspectRatio(TAROT_CARD_ASPECT_RATIO)
                            .graphicsLayer { rotationZ = rotation },
                        fixedBounds = true,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label.positionLine, style = MaterialTheme.typography.titleSmall)
                    Text(label.cardNameLine, style = MaterialTheme.typography.bodyMedium)
                    Text(label.directionLine, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


private val TarotRecreationSaver = Saver<TarotRecreationState, Any>(
    save = { it.save() },
    restore = { TarotRecreationState.restore(it) },
)

/**
 * Small bounded in-memory cache so the same card's artwork is decoded once per process,
 * not once per place it happens to be drawn (spread overview thumbnail, detail panel, a
 * revisited saved record can all show the same card.id within one session).
 */
private object TarotArtCache {
    private const val MAX_ENTRIES = 32
    private val cache = object : LinkedHashMap<Int, ImageBitmap>(MAX_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, ImageBitmap>?): Boolean =
            size > MAX_ENTRIES
    }

    @Synchronized
    fun get(id: Int): ImageBitmap? = cache[id]

    @Synchronized
    fun put(id: Int, bitmap: ImageBitmap) {
        cache[id] = bitmap
    }
}

@Composable
private fun TarotArt(
    id: Int,
    name: String,
    modifier: Modifier = Modifier,
    fixedBounds: Boolean = false,
    imageRotationDegrees: Float = 0f,
    detailHeight: Dp = 360.dp,
) {
    val assets = LocalContext.current.assets
    var bitmap by remember(id) { mutableStateOf(TarotArtCache.get(id)) }
    var failed by remember(id) { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (bitmap != null) return@LaunchedEffect
        try {
            val decoded = withContext(Dispatchers.IO) {
                val source = ImageDecoder.createSource(assets, MtjTarotCatalog.imageAssetPath(id))
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val scale = minOf(1f, 720f / maxOf(info.size.width, info.size.height))
                    decoder.setTargetSize(maxOf(1, (info.size.width * scale).toInt()), maxOf(1, (info.size.height * scale).toInt()))
                }.asImageBitmap()
            }
            TarotArtCache.put(id, decoded)
            bitmap = decoded
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (_: Exception) { failed = true }
    }
    if (fixedBounds) {
        Box(
            modifier = modifier.semantics {
                contentDescription = when {
                    bitmap != null -> name
                    failed -> "카드 이미지를 불러오지 못했습니다."
                    else -> "카드 이미지 불러오는 중"
                }
            },
            contentAlignment = Alignment.Center,
        ) {
            bitmap?.let { art ->
                Image(art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } ?: if (failed) {
                Text(
                    "이미지\n없음",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            } else {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(detailHeight)
                .semantics {
                    contentDescription = when {
                        bitmap != null -> name
                        failed -> "카드 이미지를 불러오지 못했습니다."
                        else -> "카드 이미지 불러오는 중"
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            bitmap?.let { art ->
                Image(
                    art,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = imageRotationDegrees },
                    contentScale = ContentScale.Fit,
                )
            } ?: Text(if (failed) "카드 이미지를 불러오지 못했습니다." else "카드 불러오는 중")
        }
    }
}
