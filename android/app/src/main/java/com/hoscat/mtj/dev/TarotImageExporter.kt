package com.hoscat.mtj.dev

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.provider.MediaStore
import com.mtj.tarot.MtjTarotCatalog
import com.mtj.tarot.MtjResultSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

internal data class TarotExportGrid(val columns: Int, val rows: Int)

internal fun tarotExportGrid(cardCount: Int): TarotExportGrid {
    require(cardCount > 0)
    val columns = when (cardCount) {
        1 -> 1
        2, 4 -> 2
        else -> 3
    }
    return TarotExportGrid(columns, ceil(cardCount / columns.toDouble()).toInt())
}

internal object TarotImageExporter {
    private const val IMAGE_WIDTH = 1080
    private const val PAGE_PADDING = 72
    private const val QUESTION_TEXT_SIZE = 42f
    private const val CARD_GAP = 28
    private const val CARD_RATIO = 1.68f

    suspend fun save(context: Context, snapshot: MtjResultSnapshot): Uri = withContext(Dispatchers.IO) {
        val cards = snapshot.reading.cards
        val grid = tarotExportGrid(cards.size)
        val cardWidth = (IMAGE_WIDTH - PAGE_PADDING * 2 - CARD_GAP * (grid.columns - 1)) / grid.columns
        val cardHeight = (cardWidth * CARD_RATIO).toInt()
        val question = snapshot.reading.question
            .takeUnless { it.isBlank() || it == TAROT_QUESTION_NOT_PROVIDED }
            ?: "질문 없음"
        val questionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(43, 22, 32)
            textSize = QUESTION_TEXT_SIZE
        }
        val questionLines = wrapText(question, questionPaint, IMAGE_WIDTH - PAGE_PADDING * 2)
        val lineHeight = (QUESTION_TEXT_SIZE * 1.45f).toInt()
        val questionHeight = questionLines.size * lineHeight
        val cardsTop = PAGE_PADDING + questionHeight + 48
        val imageHeight = cardsTop + grid.rows * cardHeight + (grid.rows - 1) * CARD_GAP + PAGE_PADDING
        val output = Bitmap.createBitmap(IMAGE_WIDTH, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.rgb(255, 248, 246))

        questionLines.forEachIndexed { index, line ->
            canvas.drawText(line, PAGE_PADDING.toFloat(), (PAGE_PADDING + lineHeight * (index + 1)).toFloat(), questionPaint)
        }

        cards.forEachIndexed { index, card ->
            val source = ImageDecoder.createSource(context.assets, MtjTarotCatalog.imageAssetPath(card.cardId))
            val decoded = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
            val cardBitmap = if (detailTarotCardRotation(card.directionLabel) == 180f) {
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(180f) }, true)
                    .also { if (it !== decoded) decoded.recycle() }
            } else {
                decoded
            }
            val column = index % grid.columns
            val row = index / grid.columns
            val left = PAGE_PADDING + column * (cardWidth + CARD_GAP)
            val top = cardsTop + row * (cardHeight + CARD_GAP)
            canvas.drawBitmap(
                cardBitmap,
                Rect(0, 0, cardBitmap.width, cardBitmap.height),
                RectF(left.toFloat(), top.toFloat(), (left + cardWidth).toFloat(), (top + cardHeight).toFloat()),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
            cardBitmap.recycle()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "sharomyang-tarot-$timestamp.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Sharomyang")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            check(resolver.openOutputStream(uri)?.use { output.compress(Bitmap.CompressFormat.PNG, 100, it) } == true)
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        } finally {
            output.recycle()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        var current = ""
        text.trim().split(Regex("\\s+")).forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (current.isNotEmpty() && paint.measureText(candidate) > maxWidth) {
                lines += current
                current = word
            } else {
                current = candidate
            }
        }
        if (current.isNotEmpty()) lines += current
        return lines.ifEmpty { listOf("질문 없음") }
    }
}
