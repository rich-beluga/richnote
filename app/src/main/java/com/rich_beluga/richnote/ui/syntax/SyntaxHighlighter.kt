package com.rich_beluga.richnote.ui.syntax

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

fun interface SyntaxHighlighter {
    fun highlight(text: String): AnnotatedString
}

class RegexSyntaxHighlighter(private val rules: List<SyntaxRule>) : SyntaxHighlighter {

    override fun highlight(text: String): AnnotatedString = buildAnnotatedString {
        append(text)
        if (text.isEmpty()) return@buildAnnotatedString

        val claimed = BooleanArray(text.length)
        for (rule in rules) {
            for (match in rule.regex.findAll(text)) {
                val range = match.range
                if (range.isEmpty()) continue
                if ((range.first..range.last).any { claimed[it] }) continue
                for (i in range) claimed[i] = true
                addStyle(rule.style, range.first, range.last + 1)
            }
        }
    }
}
