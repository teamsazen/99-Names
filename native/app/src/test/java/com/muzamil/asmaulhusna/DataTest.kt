package com.muzamil.asmaulhusna

import com.muzamil.asmaulhusna.data.Settings
import com.muzamil.asmaulhusna.data.matches
import com.muzamil.asmaulhusna.data.nameOfTheDay
import com.muzamil.asmaulhusna.data.names
import com.muzamil.asmaulhusna.data.normalizeForSearch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class DataTest {
    @Test fun exactly99NamesInOrder() {
        assertEquals(99, names.size)
        assertEquals((1..99).toList(), names.map { it.number })
        assertEquals("Ar-Rahman", names.first().transliteration)
    }

    @Test fun searchIgnoresSpellingAndDiacritics() {
        val q = normalizeForSearch("ar rahman")
        assertTrue(names.first().matches(q))
        assertTrue(names.first().matches(normalizeForSearch("الرحمن")))
        assertTrue(names[0].matches(normalizeForSearch("merciful")))
        assertTrue(names[6].matches("7"))
    }

    @Test fun nameOfTheDayMatchesTheWebsite() {
        // 21 November 2026 is Name 88, Al-Ghaniyy, on the website and in the 1.0.0 app.
        val day = GregorianCalendar(2026, Calendar.NOVEMBER, 21, 9, 30)
        assertEquals(88, nameOfTheDay(day).number)
    }

    @Test fun readsFlutterFavouritesFormat() {
        val raw = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu!" + "[\"1\",\"3\",\"14\"]"
        assertEquals(listOf("1", "3", "14"), Settings.decodeList(raw))
        assertEquals(emptyList<String>(), Settings.decodeList(null))
    }
}
