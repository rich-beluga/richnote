package com.rich_beluga.richnote.ui.syntax.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.rich_beluga.richnote.ui.JetBrainsMono
import com.rich_beluga.richnote.ui.JetBrainsMonoItalic
import com.rich_beluga.richnote.ui.syntax.RegexSyntaxHighlighter
import com.rich_beluga.richnote.ui.syntax.SyntaxRule

val markdownSyntaxRules: List<SyntaxRule> = listOf(
    SyntaxRule(
        name = "comment",
        regex = Regex("""\<!--[\s\S]*?-->"""),
        style = SpanStyle(
            color = Color(0xFF7D7D7F),
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "headline",
        regex = Regex("""(?m)^#{1,6}\s"""),
        style = SpanStyle(
            color = Color(0xFFFF986C)
        )
    ),
    SyntaxRule(
        name = "code",
        regex = Regex("""`[^`\n]+`"""),
        style = SpanStyle(
            color = Color(0xFF00897B),
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "bold italic",
        regex = Regex("""\*\*\*(.+?)\*\*\*|___(.+?)___"""),
        style = SpanStyle(
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Bold
        )
    ),
    SyntaxRule(
        name = "bold",
        regex = Regex("""\*\*(.+?)\*\*|__(.+?)__"""),
        style = SpanStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold
        )
    ),
    SyntaxRule(
        name = "italic",
        regex = Regex("""\*(.+?)\*|_(.+?)_"""),
        style = SpanStyle(
            fontFamily = JetBrainsMonoItalic,
            fontWeight = FontWeight.Normal
        )
    ),
    SyntaxRule(
        name = "strikethrough",
        regex = Regex("""~~(.+?)~~"""),
        style = SpanStyle(
            textDecoration = TextDecoration.LineThrough
        )
    ),
    SyntaxRule(
        name = "points",
        regex = Regex("""(?m)^[ \t]*[-+*](?=\s)"""),
        style = SpanStyle(
            color = Color(0xFFEA5655)
        )
    ),
    SyntaxRule(
        name = "numbered list",
        regex = Regex("""(?m)^[ \t]*\d+\.(?=\s)"""),
        style = SpanStyle(
            color = Color(0xFFEA5655)
        )
    )
)

class MarkdownVisualTransformation(
    private val highlighter: RegexSyntaxHighlighter = RegexSyntaxHighlighter(markdownSyntaxRules)
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(highlighter.highlight(text.text), OffsetMapping.Identity)
    }
}
