package com.hoscat.mtj.dev

import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hoscat.core.model.GanjiGlyphKind
import com.hoscat.core.model.SajuChart
import com.hoscat.core.model.toGanjiHanja
import com.mtj.design.MtjBottomActionScaffold
import com.mtj.design.MtjEmptyState
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import mtj.records.Envelope
import mtj.records.Kind

@Composable
internal fun MtjHomeScreen(
    chart: SajuChart?,
    store: RecordStore,
    question: String,
    onQuestionChange: (String) -> Unit,
    onStartTarot: () -> Unit,
    onOpenRecord: (String) -> Unit,
    onNavigate: (Int) -> Unit,
) {
    var records by remember { mutableStateOf<List<Envelope>?>(null) }
    var loadError by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val questionValidation = remember(question) { validateTarotQuestion(question) }

    LaunchedEffect(refresh) {
        loadError = false
        try {
            records = store.list()
                .filter { it.kind != Kind.PROFILE }
                .sortedByDescending { it.snapshotAtEpochMillis }
                .take(3)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            loadError = true
        }
    }

    val date = LocalDate.now(ZoneId.of("Asia/Seoul"))
    MtjBottomActionScaffold(0, onNavigate, showActionDock = false, actions = {}) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("mtj-home"),
            contentPadding = PaddingValues(top = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 250.dp),
                ) {
                    HomeHeaderMaterial(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(320.dp)
                            .aspectRatio(1.5f),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "샤로먕",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            HoscatBrandMark()
                        }
                        Text(
                            "${date.monthValue}월 ${date.dayOfMonth}일",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            if (chart == null) "사주 흐름" else "나의 일주",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            if (chart == null) {
                                "사주 정보 입력"
                            } else {
                                val stem = chart.dayPillar.stem.toGanjiHanja(GanjiGlyphKind.HeavenlyStem)
                                val branch = chart.dayPillar.branch.toGanjiHanja(GanjiGlyphKind.EarthlyBranch)
                                "$stem$branch  ${chart.dayPillar.stem}${chart.dayPillar.branch}"
                            },
                            style = if (chart == null) MaterialTheme.typography.headlineLarge
                            else MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            if (chart == null) "생년월일을 입력하면 나의 명식과 흐름을 볼 수 있어요."
                            else chart.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(
                            onClick = { onNavigate(1) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(if (chart == null) "사주 정보 입력" else "명식 보기")
                        }
                    }
                }
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("home-choice"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("지금 궁금한 것은?", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = question,
                        onValueChange = { next ->
                            if (acceptsTarotQuestionInput(next)) onQuestionChange(next)
                        },
                        label = { Text("질문") },
                        placeholder = { Text("질문을 입력해 주세요") },
                        supportingText = {
                            val visibleError = questionValidation.error.takeIf { question.isNotEmpty() }
                            Text(
                                visibleError ?: "최대 240자",
                                color = if (visibleError == null) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                            )
                        },
                        isError = questionValidation.error != null && question.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp)
                            .clickable(onClick = onStartTarot)
                            .padding(vertical = 8.dp)
                            .testTag("home-three-card-entry"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("과거 · 현재 · 미래", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "3장",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HomeTarotFan()
                    }
                }
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                Text(
                    "최근 기록",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("home-records"),
                )
            }
            when {
                loadError -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("기록을 불러오지 못했습니다. 저장한 내용은 유지됩니다.")
                        TextButton(
                            onClick = { refresh++ },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) { Text("다시 시도") }
                    }
                }
                records == null -> item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                records?.isEmpty() == true -> item {
                    MtjEmptyState(
                        title = "아직 저장한 기록이 없습니다.",
                        body = "명식이나 타로 결과를 저장하면 이곳에서 이어볼 수 있습니다.",
                    )
                }
            }
            items(records.orEmpty(), key = { it.origin.commonId }) { record ->
                Surface(
                    onClick = { onOpenRecord(record.origin.commonId) },
                    shape = RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
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
                                    tint = if (record.kind == Kind.SAJU) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                )
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                if (record.kind == Kind.SAJU) "사주 · 나의 명식" else "타로 · 지금의 선택",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                record.displayText("title"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text("›", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            item {
                TextButton(
                    onClick = { onNavigate(3) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("모든 기록 보기") }
            }
        }
    }
}

@Composable
private fun HomeHeaderMaterial(modifier: Modifier = Modifier) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val canvas = MaterialTheme.colorScheme.background
    Box(modifier) {
        Image(
            painter = painterResource(
                if (dark) R.drawable.sharomyang_header_material_dark
                else R.drawable.sharomyang_header_material_light,
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0f to canvas,
                    0.08f to canvas,
                    0.24f to canvas.copy(alpha = 0f),
                ),
            ),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to canvas,
                    0.08f to canvas.copy(alpha = 0f),
                    0.92f to canvas.copy(alpha = 0f),
                    1f to canvas,
                ),
            ),
        )
    }
}

@Composable
private fun HoscatBrandMark() {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val source = ImageBitmap.imageResource(id = R.drawable.hoscat_common_layer)
    Surface(
        modifier = Modifier.size(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (dark) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.surface,
    ) {
        Canvas(Modifier.fillMaxSize().padding(5.dp)) {
            val sourceWidth = 480
            val sourceHeight = 523
            val scale = minOf(size.width / sourceWidth, size.height / sourceHeight)
            val targetWidth = (sourceWidth * scale).toInt()
            val targetHeight = (sourceHeight * scale).toInt()
            drawImage(
                image = source,
                srcOffset = IntOffset(387, 325),
                srcSize = IntSize(sourceWidth, sourceHeight),
                dstOffset = IntOffset(
                    ((size.width - targetWidth) / 2f).toInt(),
                    ((size.height - targetHeight) / 2f).toInt(),
                ),
                dstSize = IntSize(targetWidth, targetHeight),
            )
        }
    }
}

@Composable
private fun HomeTarotFan() {
    Box(Modifier.width(132.dp).height(92.dp)) {
        listOf(-10f, 0f, 10f).forEachIndexed { index, angle ->
            Box(
                Modifier
                    .align(Alignment.Center)
                    .offset(x = ((index - 1) * 24).dp)
                    .rotate(angle)
                    .size(width = 44.dp, height = 66.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(5.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.tarot_back_mint),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
