package com.rich_beluga.richnote.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rich_beluga.richnote.R

/**
 * JetBrains Mono, вариативный шрифт
 * регистрируется несколько раз с разными весами (`Normal`, `Medium`,
 * `Bold` и `ExtraBold`
 */

val JetBrainsMono = FontFamily(
    Font(r.font.jetbrains_mono_variable, weight = FontWeight.Normal)
    Font(r.font.jetbrains_mono_variable, weight = FontWeight.Medium)
    Font(r.font.jetbrains_mono_variable, weight = FontWeight.Bold)
    Font(r.font.jetbrains_mono_variable, weight = FontWeight.ExtraBold)
)