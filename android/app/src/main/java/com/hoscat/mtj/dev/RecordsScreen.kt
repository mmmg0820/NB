package com.hoscat.mtj.dev

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mtj.design.MtjBottomActionScaffold
import com.mtj.design.MtjEmptyState
import com.mtj.design.MtjSectionHeader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mtj.records.Envelope
import mtj.records.Kind

@Composable
internal fun RecordsScreen(
    store: RecordStore,
    initialSelectedId: String? = null,
    onTabSelected: (Int) -> Unit,
) {
    var records by remember { mutableStateOf<List<Envelope>?>(null) }
    var selectedId by rememberSaveable { mutableStateOf(initialSelectedId) }
    var selectedFilter by rememberSaveable { mutableStateOf(RecordFilter.ALL) }
    val selected = records?.firstOrNull { it.origin.commonId == selectedId }
    val tarotSummary = selected?.tarotResultSummary()
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refresh by remember { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialSelectedId) {
        if (initialSelectedId != null) selectedId = initialSelectedId
    }
    LaunchedEffect(refresh) {
        loading = true
        error = null
        try {
            val loaded = store.list()
                .filter { it.kind != Kind.PROFILE }
                .sortedByDescending { it.snapshotAtEpochMillis }
            records = loaded
            if (selectedId != null && loaded.none { it.origin.commonId == selectedId }) {
                selectedId = null
                error = "이 기록을 찾을 수 없습니다."
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            error = "기록을 읽지 못했습니다. 원본 데이터는 유지됩니다."
        } finally {
            loading = false
        }
    }

    MtjBottomActionScaffold(3, onTabSelected, actions = {
        if (selected != null) {
            OutlinedButton(
                onClick = { confirm = true },
                enabled = !deleting,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("기록 삭제") }
        }
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp),
        ) {
            item {
                if (selected == null) {
                    MtjSectionHeader("기록")
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).selectableGroup(),
                    ) {
                        RecordFilter.entries.forEach { filter ->
                            val active = selectedFilter == filter
                            val indicatorColor = MaterialTheme.colorScheme.primary
                            TextButton(
                                onClick = { selectedFilter = filter },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                    .testTag("records-filter-${filter.name.lowercase(Locale.ROOT)}")
                                    .semantics { role = Role.Tab; this.selected = active }
                                    .drawBehind {
                                        if (active) {
                                            val stroke = 2.dp.toPx()
                                            drawLine(indicatorColor, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
                                        }
                                    },
                            ) {
                                Text(filter.label, color = if (active) indicatorColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    if (selectedFilter == RecordFilter.SAVED) {
                        Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MtjSectionHeader("저장한 기록")
                            Text("이 앱에서 저장한 명식과 타로 리딩", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { selectedId = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("목록으로")
                        }
                        if (tarotSummary == null) MtjSectionHeader(selected.displayText("title"))
                        Text(
                            "${recordsKindLabel(selected.kind)} · 저장된 결과",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            error?.let { message ->
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp).testTag("records-error"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { refresh++ }, modifier = Modifier.heightIn(min = 48.dp)) { Text("다시 시도") }
                    }
                }
            }
            if (loading) {
                item {
                    Text(
                        "기록을 불러오는 중",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).testTag("records-loading"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val detail = selected
            if (detail != null) {
                tarotSummary?.let { summary ->
                    item { TarotResultSummaryPanel(summary) }
                }
                item {
                    Text(
                        detail.displayText("summary"),
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                items(detail.detailRows().filterNot { row ->
                    tarotSummary != null && row.label in setOf("스프레드", "질문")
                }) { row ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(row.label, style = MaterialTheme.typography.labelMedium)
                        Text(row.value, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            } else {
                val filteredRecords = records.orEmpty().filter(selectedFilter::accepts)
                if (filteredRecords.isEmpty() && !loading && error == null) {
                    item {
                        val empty = selectedFilter.emptyState()
                        Column(Modifier.fillMaxWidth().padding(top = 24.dp).testTag("records-empty")) {
                            MtjEmptyState(title = empty.title, body = empty.body)
                            TextButton(
                                onClick = { onTabSelected(empty.targetTab) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) { Text(empty.actionLabel) }
                        }
                    }
                }
                items(filteredRecords, key = { it.origin.commonId }) { record ->
                    RecordRow(record = record, onClick = { selectedId = record.origin.commonId })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { if (!deleting) confirm = false },
            title = { Text("이 기록을 삭제할까요?") },
            text = { Text("‘${selected?.displayText("title").orEmpty()}’ 기록은 삭제 후 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        val origin = selected?.origin ?: return@TextButton
                        deleting = true
                        scope.launch {
                            try {
                                store.delete(origin)
                                selectedId = null
                                confirm = false
                                refresh++
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                error = "삭제하지 못했습니다. 다시 시도해 주세요."
                                confirm = false
                            } finally {
                                deleting = false
                            }
                        }
                    },
                ) { Text(if (deleting) "삭제 중" else "삭제") }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }, enabled = !deleting) { Text("취소") }
            },
        )
    }
}

internal enum class RecordFilter(val label: String) {
    ALL("전체"),
    SAJU("사주"),
    TAROT("타로"),
    SAVED("기록");

    fun accepts(record: Envelope): Boolean = when (this) {
        ALL -> record.kind != Kind.PROFILE
        SAJU -> record.kind == Kind.SAJU
        TAROT -> record.kind == Kind.TAROT
        SAVED -> record.origin.source == "mtj-native" && (record.kind == Kind.SAJU || record.kind == Kind.TAROT)
    }
}

internal data class RecordsEmptyState(
    val title: String,
    val body: String,
    val actionLabel: String,
    val targetTab: Int,
)

internal fun RecordFilter.emptyState(): RecordsEmptyState = when (this) {
    RecordFilter.ALL -> RecordsEmptyState(
        "아직 저장된 기록이 없습니다.", "사주 명식과 타로 결과를 저장하면 이곳에서 이어볼 수 있습니다.", "사주 보기", 1,
    )
    RecordFilter.SAJU -> RecordsEmptyState(
        "저장한 사주가 없습니다.", "저장한 사주 명식을 이곳에서 다시 볼 수 있습니다.", "사주 보기", 1,
    )
    RecordFilter.TAROT -> RecordsEmptyState(
        "저장한 타로가 없습니다.", "저장한 타로 리딩을 이곳에서 다시 볼 수 있습니다.", "타로 보기", 2,
    )
    RecordFilter.SAVED -> RecordsEmptyState(
        "직접 저장한 기록이 없습니다.", "이 앱에서 저장한 명식과 타로 리딩을 이곳에서 다시 볼 수 있습니다.", "사주 보기", 1,
    )
}

internal fun recordsKindLabel(kind: Kind): String = when (kind) {
    Kind.SAJU -> "사주"
    Kind.TAROT -> "타로"
    Kind.PROFILE -> "프로필"
    else -> "기록"
}

@Composable
private fun RecordRow(record: Envelope, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().testTag("records-saved-row"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (record.kind == Kind.SAJU) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (record.kind == Kind.SAJU || record.kind == Kind.TAROT) {
                        Icon(
                            painter = painterResource(
                                if (record.kind == Kind.SAJU) R.drawable.ic_saju_chart else R.drawable.ic_tarot_cards,
                            ),
                            contentDescription = null,
                            tint = if (record.kind == Kind.SAJU) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${recordsKindLabel(record.kind)} · ${record.displayText("title")}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(record.displayText("summary"), style = MaterialTheme.typography.bodyMedium)
                Text(
                    record.savedAtLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "상세 보기")
        }
    }
}

private val savedAtFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy. M. d. a h:mm", Locale.KOREAN)

private fun Envelope.savedAtLabel(): String = Instant.ofEpochMilli(snapshotAtEpochMillis)
    .atZone(ZoneId.of("Asia/Seoul"))
    .format(savedAtFormatter)
