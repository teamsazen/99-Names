package com.muzamil.asmaulhusna.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muzamil.asmaulhusna.data.DivineName
import com.muzamil.asmaulhusna.data.Settings
import com.muzamil.asmaulhusna.hero
import com.muzamil.asmaulhusna.ui.AppIcon
import com.muzamil.asmaulhusna.ui.AppMotion
import com.muzamil.asmaulhusna.ui.Card
import com.muzamil.asmaulhusna.ui.Glyph
import com.muzamil.asmaulhusna.ui.NumberRing
import com.muzamil.asmaulhusna.ui.FitTxt
import com.muzamil.asmaulhusna.ui.Txt
import com.muzamil.asmaulhusna.ui.arabicStyle
import com.muzamil.asmaulhusna.ui.palette
import com.muzamil.asmaulhusna.ui.r
import com.muzamil.asmaulhusna.ui.rs
import com.muzamil.asmaulhusna.ui.style
import com.muzamil.asmaulhusna.ui.urduStyle

/** Compact card for the two-column grid: number, Arabic, transliteration and meaning. */
@Composable
fun NameGridCard(name: DivineName, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = palette
    val favourite = Settings.isFavourite(name.number)
    val heart by animateFloatAsState(if (favourite) 1f else 0f, tween(AppMotion.FAST), label = "fav")
    Card(modifier.height(r(200f)), onClick = onClick) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumberRing(name.number, 30.dp)
                Spacer(Modifier.weight(1f))
                AppIcon(Glyph.HeartFilled, p.accent, 16.dp, Modifier.alpha(heart))
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                FitTxt(name.arabic, arabicStyle(rs(32f) * Settings.arabicScale, p.title), Modifier.hero("all-${name.number}"))
            }
            if (Settings.showTransliteration) {
                Txt(name.transliteration, style(rs(15.5f), p.title, FontWeight.Bold, letterSpacing = (-.3).sp, align = TextAlign.Center), Modifier.fillMaxWidth(), maxLines = 1)
            }
            if (Settings.showEnglish) {
                Spacer(Modifier.height(2.dp))
                Txt(name.english, style(rs(12.5f), p.muted, align = TextAlign.Center), Modifier.fillMaxWidth(), maxLines = 1)
            } else if (Settings.showUrdu) {
                Txt(name.urdu, urduStyle(rs(18f), p.muted), Modifier.fillMaxWidth(), maxLines = 1)
            }
        }
    }
}
