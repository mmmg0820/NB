package com.mtj.tarot

import android.content.res.AssetManager
import com.softcat.mystictarot.TarotCard
import com.softcat.mystictarot.TarotDeck
import java.io.IOException
import java.io.InputStream
import java.util.Collections
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

public enum class CatalogError { CATALOG_UNREADABLE, INVALID_CATALOG, IMAGE_UNREADABLE, EMPTY_IMAGE }

public class MtjTarotCatalogException(
    public val code: CatalogError,
    public val assetPath: String,
    message: String,
    cause: Throwable? = null
) : IllegalStateException("$code: $assetPath: $message", cause)

/** Read-only asset loader. Call off the UI thread; no Repository or persistence is used. */
public object MtjTarotCatalog {
    public const val ASSET_DIRECTORY: String = "mtj/tarot"
    public const val CATALOG_ASSET: String = "$ASSET_DIRECTORY/tarot_cards_78.json"

    public fun imageAssetPath(cardId: Int): String {
        require(cardId in 0..77) { "Universal card ID must be in 0..77" }
        return "$ASSET_DIRECTORY/tarot_${cardId.toString().padStart(2, '0')}.avif"
    }

    @JvmStatic
    public fun load(assets: AssetManager): TarotDeck = loadFromAssets { assets.open(it) }

    internal fun loadFromAssets(open: (String) -> InputStream): TarotDeck {
        val raw = try {
            open(CATALOG_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: IOException) {
            throw MtjTarotCatalogException(CatalogError.CATALOG_UNREADABLE, CATALOG_ASSET, "Required 78-card JSON is missing or unreadable", e)
        }
        val cards = try {
            val tokener = JSONTokener(raw)
            val array = tokener.nextValue() as? JSONArray ?: error("Expected JSON array")
            check(tokener.nextClean() == '\u0000') { "Trailing JSON content" }
            val byId = mutableMapOf<Int, TarotCard>()
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val rawId = item.get("id")
                check(rawId is Int || rawId is Long) { "Row $index: integer ID required" }
                val number = (rawId as Number).toLong()
                check(number in 0L..77L) { "Row $index: out-of-range ID $number" }
                val id = number.toInt()
                check(!byId.containsKey(id)) { "Duplicate card ID $id" }
                val arcana = text(item, "arcana", id)
                check(arcana == if (id < 22) "Major Arcana" else "Minor Arcana") { "Card $id: arcana mismatch" }
                val upright = keywords(item, "upright_keywords", id)
                val reversed = keywords(item, "reversed_keywords", id)
                // Preserve MyangTarotRepository.buildMeaning formatting and keyword order.
                val meaning = buildString {
                    append(text(item, "basic_meaning", id))
                    if (upright.isNotEmpty()) append("\n정방향 키워드: ${upright.joinToString(", ")}")
                    if (reversed.isNotEmpty()) append("\n역방향 키워드: ${reversed.joinToString(", ")}")
                }
                val imageUri = "file:///android_asset/${imageAssetPath(id)}"
                val declaredImage = item.opt("image_uri")
                check(declaredImage == null || declaredImage == JSONObject.NULL || declaredImage == "" || declaredImage == imageUri) {
                    "Card $id: image_uri conflicts with bundled image mapping"
                }
                byId[id] = TarotCard(id, text(item, "name_en", id), text(item, "name_kr", id), arcana,
                    meaning, upright, reversed, imageUri)
            }
            val missing = (0..77).filterNot { byId.containsKey(it) }
            check(missing.isEmpty() && byId.size == 78) { "Expected 78 cards; missing IDs: $missing" }
            (0..77).map { byId.getValue(it) }
        } catch (e: Exception) {
            throw MtjTarotCatalogException(CatalogError.INVALID_CATALOG, CATALOG_ASSET, e.message ?: "Invalid 78-card catalog", e)
        }
        // Check every required image before exposing any partial deck. Decoding is a UI responsibility.
        cards.forEach { card ->
            val asset = imageAssetPath(card.id)
            val firstByte = try {
                open(asset).use { it.read() }
            } catch (e: IOException) {
                throw MtjTarotCatalogException(CatalogError.IMAGE_UNREADABLE, asset, "Required image for card ${card.id} is missing or unreadable", e)
            }
            if (firstByte == -1) throw MtjTarotCatalogException(CatalogError.EMPTY_IMAGE, asset, "Empty image for card ${card.id}")
        }
        return TarotDeck("standard", "유니버셜 타로", immutable(cards), enabled = true)
    }

    private fun text(item: JSONObject, key: String, id: Int): String {
        val value = item.get(key)
        check(value is String && value.isNotBlank()) { "Card $id: nonblank $key required" }
        return value
    }

    private fun keywords(item: JSONObject, key: String, id: Int): List<String> {
        val values = item.getJSONArray(key)
        return immutable((0 until values.length()).map { index ->
            val value = values.get(index)
            check(value is String && value.isNotBlank()) { "Card $id: invalid $key[$index]" }
            value
        })
    }

    private fun <T> immutable(values: List<T>): List<T> = Collections.unmodifiableList(ArrayList(values))
}
