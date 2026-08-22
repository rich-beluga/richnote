package com.rich_beluga.richnote.ui.syntax.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.rich_beluga.richnote.ui.JetBrainsMono
import com.rich_beluga.richnote.ui.JetBrainsMonoItalic
import com.rich_beluga.richnote.ui.syntax.CodeLanguages
import com.rich_beluga.richnote.ui.syntax.RegexSyntaxHighlighter
import com.rich_beluga.richnote.ui.syntax.SyntaxRule

// группы: 1 = фенс, 2 = инфо-строка (+\n или $), 3 = тело блока, 4 = закрывающий фенс
internal val BACKTICK_FENCE_REGEX = Regex(
    """(?m)^ {0,3}(`{3,})(?!`)([^`\n]*?(?:\n|\z))([\s\S]*?)(^ {0,3}\1`*[ \t]*$\n?|\z)"""
)

internal val TILDE_FENCE_REGEX = Regex(
    """(?m)^ {0,3}(~{3,})(?!~)([^~\n]*?(?:\n|\z))([\s\S]*?)(^ {0,3}\1~*[ \t]*$\n?|\z)"""
)

/** Тело блока + инфо-строка для каждого фенс-блока (для вложенной подсветки языков). */
internal fun fencedCodeRegions(source: String): List<Pair<IntRange, String>> = buildList {
    for (regex in listOf(BACKTICK_FENCE_REGEX, TILDE_FENCE_REGEX)) {
        for (match in regex.findAll(source)) {
            val content = match.groups[3]?.range ?: continue
            val info = match.groupValues[2].trim()
            add(content to info)
        }
    }
}

val markdownSyntaxRules: List<SyntaxRule> = listOf(
    SyntaxRule(
        name = "codeblock",
        regex = BACKTICK_FENCE_REGEX,
        style = SpanStyle(
            color = Color(0xFFFFCB6B)
        )
    ),
    SyntaxRule(
        name = "codeblock",
        regex = TILDE_FENCE_REGEX,
        style = SpanStyle(
            color = Color(0xFFFFCB6B)
        )
    ),
    SyntaxRule(
        name = "comment",
        regex = Regex("""<!--[\s\S]*?-->"""),
        style = SpanStyle(
            color = Color(0xFF7D7D7F),
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "headline",
        regex = Regex("""(?m)^ {0,3}#{1,6}(?= |\t|$)"""),
        style = SpanStyle(
            color = Color(0xFFFF986C)
        )
    ),
    SyntaxRule(
        name = "inline code",
        regex = Regex("""(`+)(?!`)(?:[^`]|`(?!\1))*?\1"""),
        style = SpanStyle(
            color = Color(0xFF00897B),
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "escape",
        regex = Regex("""\\[-+*_~>#`\\|()\[\]{}.!]"""),
        style = SpanStyle()
    ),
    SyntaxRule(
        name = "bold italic",
        regex = Regex("""\*\*\*(?!\s)[^*\n]+?(?<!\s)\*\*\*|(?<![\w\\])___(?!\s)[^_\n]+?(?<!\s)___(?!\w)"""),
        style = SpanStyle(
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Bold
        )
    ),
    SyntaxRule(
        name = "bold",
        regex = Regex("""\*\*(?!\s)[^*\n]+?(?<!\s)\*\*|(?<![\w\\])__(?!\s)[^_\n]+?(?<!\s)__(?!\w)"""),
        style = SpanStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold
        )
    ),
    SyntaxRule(
        name = "italic",
        regex = Regex("""\*(?!\s)[^*\n]+?(?<!\s)\*|(?<![\w\\])_(?!\s)[^_\n]+?(?<!\s)_(?!\w)"""),
        style = SpanStyle(
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "strikethrough",
        regex = Regex("""~~(?!\s)[^~\n]+?(?<!\s)~~"""),
        style = SpanStyle(
            textDecoration = TextDecoration.LineThrough
        )
    ),
    SyntaxRule(
        name = "link",
        regex = Regex("""!?\[[^\]\n]*]\([^)\n]*\)"""),
        style = SpanStyle(
            color = Color(0xFF6CA8F0),
            textDecoration = TextDecoration.Underline
        )
    ),
    // divider выше points/numbered list: иначе "- - -" частично съедается маркером списка
    SyntaxRule(
        name = "divider",
        regex = Regex("""(?m)^ {0,3}(?:(?:\*[ \t]*){3,}|(?:-[ \t]*){3,}|(?:_[ \t]*){3,})$"""),
        style = SpanStyle(
            color = Color(0xFFFF9ACC)
        )
    ),
    SyntaxRule(
        name = "blockquote",
        regex = Regex("""(?m)^ {0,3}>+ ?"""),
        style = SpanStyle(
            color = Color(0xFFFF986C)
        )
    ),
    SyntaxRule(
        name = "points",
        regex = Regex("""(?m)^[ \t]*[-+*](?=\s|$)"""),
        style = SpanStyle(
            color = Color(0xFFEA5655)
        )
    ),
    SyntaxRule(
        name = "numbered list",
        regex = Regex("""(?m)^[ \t]*\d+\.(?=\s|$)"""),
        style = SpanStyle(
            color = Color(0xFFEA5655)
        )
    )
)

class MarkdownVisualTransformation(
    private val highlighter: RegexSyntaxHighlighter = RegexSyntaxHighlighter(markdownSyntaxRules)
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text
        val annotated = highlighter.highlight(source)

        val result = buildAnnotatedString {
            append(annotated)
            // вложенная подсветка языков внутри ```-блоков поверх общей рамки
            for ((contentRange, info) in fencedCodeRegions(source)) {
                val rules = CodeLanguages.rulesFor(info) ?: continue
                val inner = RegexSyntaxHighlighter(rules).highlight(source.substring(contentRange))
                for (span in inner.spanStyles) {
                    addStyle(span.style, contentRange.first + span.start, contentRange.first + span.end)
                }
            }
        }

        return TransformedText(result, OffsetMapping.Identity)
    }
}
