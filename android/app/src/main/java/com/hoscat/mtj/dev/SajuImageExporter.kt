package com.hoscat.mtj.dev

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.provider.MediaStore
import com.hoscat.core.model.GanjiGlyphKind
import com.hoscat.core.model.Pillar
import com.hoscat.core.model.SajuChart
import com.hoscat.core.model.toGanjiHanja
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object SajuImageExporter {
    private const val WIDTH = 1080
    private const val HEIGHT = 1120

    suspend fun save(context: Context, chart: SajuChart): Uri = withContext(Dispatchers.IO) {
        val bitmap = render(chart)
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "sharomyang-saju-$timestamp.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Sharomyang")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            check(resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } == true)
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        } finally {
            bitmap.recycle()
        }
    }

    private fun render(chart: SajuChart): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
        canvas.drawColor(Color.rgb(255, 248, 246))
        drawText(canvas, paint, sajuReadingTitle(chart), 64f, 112f, 52f, true)
        drawText(canvas, paint, "${chart.name}님의 명식", 64f, 164f, 26f, false, Color.rgb(139, 107, 116))
        val pillars = listOf(
            "시주" to chart.hourPillar,
            "일주" to chart.dayPillar,
            "월주" to chart.monthPillar,
            "연주" to chart.yearPillar,
        )
        val gap = 18f
        val tileWidth = (WIDTH - 128f - gap * 3f) / 4f
        pillars.forEachIndexed { index, (label, pillar) ->
            val left = 64f + index * (tileWidth + gap)
            drawText(canvas, paint, label, left + tileWidth / 2f, 254f, 25f, true, alignCenter = true)
            drawPillar(canvas, paint, pillar, left, 286f, tileWidth, label == "일주")
        }
        drawText(canvas, paint, chart.evidence.calculationBasisLabel, 64f, 1022f, 22f, false, Color.rgb(139, 107, 116))
        drawText(canvas, paint, "샤로먕", 64f, 1070f, 24f, true, Color.rgb(214, 53, 106))
        return bitmap
    }

    private fun drawPillar(canvas: Canvas, paint: Paint, pillar: Pillar?, left: Float, top: Float, width: Float, emphasized: Boolean) {
        val height = 620f
        paint.color = if (emphasized) Color.rgb(252, 228, 236) else Color.WHITE
        canvas.drawRoundRect(RectF(left, top, left + width, top + height), 24f, 24f, paint)
        if (pillar == null) {
            drawText(canvas, paint, "시간\n모름", left + width / 2f, top + height / 2f, 32f, true, alignCenter = true)
            return
        }
        drawGlyph(canvas, paint, pillar.stem, GanjiGlyphKind.HeavenlyStem, left + width / 2f, top + 190f)
        drawGlyph(canvas, paint, pillar.branch, GanjiGlyphKind.EarthlyBranch, left + width / 2f, top + 470f)
    }

    private fun drawGlyph(canvas: Canvas, paint: Paint, value: String, kind: GanjiGlyphKind, x: Float, y: Float) {
        drawText(canvas, paint, value.toGanjiHanja(kind), x, y, 78f, true, alignCenter = true)
        drawText(canvas, paint, value, x, y + 66f, 30f, true, Color.rgb(139, 107, 116), alignCenter = true)
    }

    private fun drawText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        bold: Boolean,
        color: Int = Color.rgb(43, 22, 32),
        alignCenter: Boolean = false,
    ) {
        paint.color = color
        paint.textSize = size
        paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        paint.textAlign = if (alignCenter) Paint.Align.CENTER else Paint.Align.LEFT
        val lines = text.split('\n')
        lines.forEachIndexed { index, line -> canvas.drawText(line, x, y + index * size * 1.25f, paint) }
    }
}
