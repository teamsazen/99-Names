package com.muzamil.asmaulhusna.data

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

/**
 * Lowercases and strips punctuation, Arabic diacritics and alif variants so
 * "ar rahman", "Ar-Rahman" and "الرحمن" all match.
 */
fun normalizeForSearch(s: String): String = s.lowercase()
    .replace(Regex("[ً-ٰٟۖ-ۭـ]"), "")
    .replace(Regex("[ٱآأإ]"), "ا")
    .replace(Regex("[\\s\\-'’ʿ،,.]"), "")

fun DivineName.matches(query: String): Boolean =
    "$number" == query ||
        normalizeForSearch(transliteration).contains(query) ||
        normalizeForSearch(english).contains(query) ||
        normalizeForSearch(arabic).contains(query) ||
        normalizeForSearch(urdu).contains(query)

/** The same name for everyone on a given day, cycling through all 99 (as the website). */
fun nameOfTheDay(now: Calendar = Calendar.getInstance()): DivineName {
    val utc = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    }
    val day = utc.timeInMillis / 86_400_000L
    return names[(day % 99).toInt()]
}
