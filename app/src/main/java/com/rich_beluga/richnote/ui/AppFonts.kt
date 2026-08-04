package com.rich_beluga.richnote.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rich_beluga.richnote.R

/**
 * JetBrains Mono, файлы в res/fonts/. jetbrains_mono_variable.ttf — variable-шрифт,
 * поэтому вес (Bold) задаётся прямо на Font() — система рендерит нужную ось веса
 * из того же файла, отдельный bold-файл не нужен.
 */
val JetBrainsMonoVariableBold = FontFamily(
    Font(R.font.jetbrains_mono_variable, weight = FontWeight.Bold)
)

val JetBrainsMonoRegular = FontFamily(
    Font(R.font.jetbrains_mono_regular, weight = FontWeight.Normal)
)
