package com.rich_beluga.richnote.ui.syntax

import androidx.compose.ui.text.SpanStyle

data class SyntaxRule(
    val name: String,
    val regex: Regex,
    val style: SpanStyle
)
