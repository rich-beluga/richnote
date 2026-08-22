package com.rich_beluga.richnote.ui.syntax.kotlin

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.rich_beluga.richnote.ui.JetBrainsMonoItalic
import com.rich_beluga.richnote.ui.syntax.SyntaxRule

// порядок = приоритет: сначала строки (чтобы "//" в URL не ломал комментарии),
// потом комментарии, затем остальное
private val KEYWORDS = listOf(
    "package", "import", "class", "object", "interface", "fun", "val", "var",
    "typealias", "constructor", "init", "if", "else", "when", "for", "while",
    "do", "return", "throw", "try", "catch", "finally", "break", "continue",
    "in", "is", "as", "this", "super", "null", "true", "false", "where", "by",
    "get", "set", "field", "it", "suspend", "override", "open", "abstract",
    "final", "sealed", "enum", "annotation", "companion", "inner", "lateinit",
    "const", "vararg", "reified", "inline", "noinline", "crossinline",
    "operator", "infix", "external", "public", "private", "protected",
    "internal", "expect", "actual", "data", "out", "value", "dynamic"
)

val kotlinSyntaxRules: List<SyntaxRule> = listOf(
    SyntaxRule(
        name = "raw string",
        regex = Regex("\"\"\"[\\s\\S]*?\"\"\""),
        style = SpanStyle(color = Color(0xFF6AAB73))
    ),
    SyntaxRule(
        name = "string",
        regex = Regex("\"(?:\\\\.|[^\\\\\"\n])*\""),
        style = SpanStyle(color = Color(0xFF6AAB73))
    ),
    SyntaxRule(
        name = "char",
        regex = Regex("'(?:\\\\.|[^\\\\'\n])'"),
        style = SpanStyle(color = Color(0xFF6AAB73))
    ),
    SyntaxRule(
        name = "kdoc",
        regex = Regex("/\\*\\*[\\s\\S]*?\\*/"),
        style = SpanStyle(
            color = Color(0xFF7D7D7F),
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "block comment",
        regex = Regex("/\\*[\\s\\S]*?\\*/"),
        style = SpanStyle(
            color = Color(0xFF7D7D7F),
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "line comment",
        regex = Regex("//[^\n]*"),
        style = SpanStyle(
            color = Color(0xFF7D7D7F),
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "annotation",
        regex = Regex("@[A-Za-z_]\\w*"),
        style = SpanStyle(color = Color(0xFFB3AE60))
    ),
    SyntaxRule(
        name = "number",
        regex = Regex("\\b(?:0[xX][0-9a-fA-F_]+|0[bB][01_]+|\\d[\\d_]*(?:\\.[\\d_]+)?(?:[eE][+-]?\\d+)?)[fFlLuU]*\\b"),
        style = SpanStyle(color = Color(0xFF2AACB8))
    ),
    SyntaxRule(
        name = "keyword",
        regex = Regex("\\b(?:${KEYWORDS.joinToString("|")})\\b"),
        style = SpanStyle(color = Color(0xFFCF8E6D))
    ),
    SyntaxRule(
        name = "type",
        regex = Regex("\\b[A-Z]\\w*\\b"),
        style = SpanStyle(color = Color(0xFF56A8F5))
    )
)
