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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
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
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialSelectedId) {
        if (initialSelectedId != null) selectedId = initialSelectedId
    }
    LaunchedEffect(refresh) {
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
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RecordFilter.entries.forEach { filter ->
                            TextButton(
                                onClick = { selectedFilter = filter },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(
                                    filter.label,
                                    color = if (selectedFilter == filter) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { selectedId = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("목록으로")
                        }
                        MtjSectionHeader(selected.displayText("title"))
                        Text(
                            if (selected.kind == Kind.SAJU) "사주 기록" else "타로 기록",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            error?.let { message ->
                item {
                    Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { refresh++ }, modifier = Modifier.heightIn(min = 48.dp)) { Text("다시 시도") }
                    }
                }
            }
            val detail = selected
            if (detail != null) {
                item {
                    Text(
                        detail.displayText("summary"),
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                items(detail.detailRows()) { row ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(row.label, style = MaterialTheme.typography.labelMedium)
                        Text(row.value, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            } else if (records == null && error == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (records?.isEmpty() == true && error == null) {
                item {
                    MtjEmptyState(
                        title = "아직 저장된 기록이 없습니다.",
                        body = "사주 명식과 타로 결과를 저장하면 이곳에서 이어볼 수 있습니다.",
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            } else {
                val filteredRecords = records.orEmpty().filter(selectedFilter::accepts)
                if (filteredRecords.isEmpty()) {
                    item {
                        MtjEmptyState(
                            title = "해당하는 기록이 없습니다.",
                            body = "다른 종류를 선택하거나 새 기록을 저장해 주세요.",
                            modifier = Modifier.padding(top = 24.dp),
                        )
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

private enum class RecordFilter(val label: String) {
    ALL("전체"),
    SAJU("사주"),
    TAROT("타로");

    fun accepts(record: Envelope): Boolean = when (this) {
        ALL -> true
        SAJU -> record.kind == Kind.SAJU
        TAROT -> record.kind == Kind.TAROT
    }
}

@Composable
private fun RecordRow(record: Envelope, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
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
                    Icon(
                        painter = painterResource(
                            if (record.kind == Kind.SAJU) R.drawable.ic_saju_chart
                            else R.drawable.ic_tarot_cards,
                        ),
                        contentDescription = null,
                        tint = if (record.kind == Kind.SAJU) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (record.kind == Kind.SAJU) "사주 · ${record.displayText("title")}" else "타로 · ${record.displayText("title")}",
                    style = MaterialTheme.typography.titleSmall,
                )
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
