package com.rich_beluga.richnote.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rich_beluga.richnote.R

/**
 * JetBrains Mono, вариативный шрифт.
 * Регистрируется несколько раз с разными весами:
 * `Normal`, `Medium`, `Bold` и `ExtraBold`.
 */

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_variable, weight = FontWeight.Normal),
    Font(R.font.jetbrains_mono_variable, weight = FontWeight.Medium),
    Font(R.font.jetbrains_mono_variable, weight = FontWeight.Bold),
    Font(R.font.jetbrains_mono_variable, weight = FontWeight.ExtraBold),
)