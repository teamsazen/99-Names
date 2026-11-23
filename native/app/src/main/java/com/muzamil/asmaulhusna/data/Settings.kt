package com.muzamil.asmaulhusna.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

/** System / light / dark, stored as the index the Flutter app used. */
enum class ThemeMode { System, Light, Dark }

/**
 * App preferences and favourites, kept on the device. They live in the same file and
 * format the Flutter app's shared_preferences plugin used ("FlutterSharedPreferences",
 * keys prefixed "flutter."), so an update from the Flutter app keeps everything.
 */
object Settings {
    private lateinit var prefs: SharedPreferences

    @set:JvmName("assignThemeMode")
    var themeMode by mutableStateOf(ThemeMode.System); private set
    @set:JvmName("assignShowTransliteration")
    var showTransliteration by mutableStateOf(true); private set
    @set:JvmName("assignShowEnglish")
    var showEnglish by mutableStateOf(true); private set
    @set:JvmName("assignShowUrdu")
    var showUrdu by mutableStateOf(true); private set
    @set:JvmName("assignGridLayout")
    var gridLayout by mutableStateOf(true); private set
    @set:JvmName("assignArabicScale")
    var arabicScale by mutableFloatStateOf(1f); private set
    var favourites by mutableStateOf(emptySet<Int>()); private set

    /** Bumped on every change, for screens that only need "something changed". */
    var version by mutableIntStateOf(0); private set

    fun load(context: Context) {
        prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        themeMode = ThemeMode.entries.getOrElse(long("themeMode")?.toInt() ?: 0) { ThemeMode.System }
        showTransliteration = bool("showTransliteration") ?: true
        showEnglish = bool("showEnglish") ?: true
        showUrdu = bool("showUrdu") ?: true
        gridLayout = bool("gridLayout") ?: true
        arabicScale = (double("arabicScale") ?: 1.0).toFloat()
        favourites = stringList("favourites").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setThemeMode(mode: ThemeMode) { themeMode = mode; put { putLong(k("themeMode"), mode.ordinal.toLong()) } }
    fun setShowTransliteration(v: Boolean) { showTransliteration = v; put { putBoolean(k("showTransliteration"), v) } }
    fun setShowEnglish(v: Boolean) { showEnglish = v; put { putBoolean(k("showEnglish"), v) } }
    fun setShowUrdu(v: Boolean) { showUrdu = v; put { putBoolean(k("showUrdu"), v) } }
    fun setGridLayout(v: Boolean) { gridLayout = v; put { putBoolean(k("gridLayout"), v) } }
    fun setArabicScale(v: Float) { arabicScale = v; put { putString(k("arabicScale"), DOUBLE_PREFIX + v.toDouble()) } }

    fun isFavourite(number: Int) = number in favourites

    fun toggleFavourite(number: Int) {
        favourites = if (number in favourites) favourites - number else favourites + number
        val json = JSONArray(favourites.map { it.toString() }).toString()
        put { putString(k("favourites"), "$LIST_PREFIX!$json") }
    }

    // --- shared_preferences (Android, legacy API) storage format ---

    private const val LIST_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"
    private const val DOUBLE_PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBEb3VibGUu"

    private fun k(key: String) = "flutter.$key"

    private inline fun put(edit: SharedPreferences.Editor.() -> Unit) {
        (prefs.edit() as SharedPreferences.Editor?)?.apply(edit)?.apply()
        version++
    }

    /** All stored values; empty when the store has nothing (or no backing file). */
    private fun all(): Map<String, *> = prefs.all ?: emptyMap<String, Any>()

    private fun long(key: String): Long? = (all()[k(key)] as? Number)?.toLong()
    private fun bool(key: String): Boolean? = all()[k(key)] as? Boolean

    private fun double(key: String): Double? = when (val v = all()[k(key)]) {
        is String -> v.removePrefix(DOUBLE_PREFIX).toDoubleOrNull()
        is Number -> v.toDouble()
        else -> null
    }

    /** A string list, in the plugin's current JSON form or its older serialized form. */
    internal fun decodeList(raw: String?): List<String> {
        if (raw == null || !raw.startsWith(LIST_PREFIX)) return emptyList()
        val body = raw.removePrefix(LIST_PREFIX)
        return runCatching {
            if (body.startsWith("!")) {
                val a = JSONArray(body.substring(1))
                List(a.length()) { a.getString(it) }
            } else {
                ObjectInputStream(ByteArrayInputStream(Base64.decode(body, Base64.DEFAULT))).use { s ->
                    (s.readObject() as List<*>).map { it.toString() }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun stringList(key: String) = decodeList(all()[k(key)] as? String)
}
