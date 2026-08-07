package com.rich_beluga.richnote.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.markdown.BlockNode
import com.rich_beluga.richnote.markdown.InlineNode
import com.rich_beluga.richnote.ui.JetBrainsMono

private fun AnnotatedString.Builder.appendInline(nodes: List<InlineNode>, linkColor: Color) {
    for (node in nodes) {
        when (node) {
            is InlineNode.Text -> append(node.value)
            is InlineNode.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInline(node.children, linkColor)
            }
            is InlineNode.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInline(node.children, linkColor)
            }
            is InlineNode.Code -> withStyle(
                SpanStyle(
                    fontFamily = FontFamily.JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    background = Color.Black.copy(alpha = 0.06f)
                )
            ) {
                append(node.value)
            }
            is InlineNode.Link -> withStyle(
                SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
            ) {
                appendInline(node.children, linkColor)
            }
            is InlineNode.Image -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                // загрузчик изображений отсутствует -> плейсхолдер
                // TODO: добавить поддержку отрисовки изображений
                append("🖼 ${node.alt.ifBlank { "изображение" }}")
            }
            InlineNode.LineBreak -> append("\n")
        }
    }
}

fun List<InlineNode>.toAnnotatedString(linkColor: Color): AnnotatedString = buildAnnotatedString {
    appendInline(this@toAnnotatedString, linkColor)
}

@Composable
private fun headingStyle(level: Int): TextStyle {
    val typography = MaterialTheme.typography
    val base = when (level) {
        1 -> typography.headlineMedium
        2 -> typography.headlineSmall
        3 -> typography.titleLarge
        4 -> typography.titleMedium
        else -> typography.titleSmall
    }
    return base.copy(fontWeight = FontWeight.Bold)
}

@Composable
fun MarkdownBlockView(block: BlockNode, modifier: Modifier = Modifier) {
    val linkColor = MaterialTheme.colorScheme.primary

    when (block) {
        is BlockNode.Paragraph -> Text(
            text = block.inline.toAnnotatedString(linkColor),
            modifier = modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        is BlockNode.Heading -> Text(
            text = block.inline.toAnnotatedString(linkColor),
            modifier = modifier.padding(top = 12.dp, bottom = 4.dp),
            style = headingStyle(block.level)
        )

        BlockNode.ThematicBreak -> HorizontalDivider(modifier = modifier.padding(vertical = 8.dp))

        is BlockNode.CodeBlock -> Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        is BlockNode.BlockQuote -> Row(
            modifier = modifier
                .padding(vertical = 4.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                block.children.forEach { child -> MarkdownBlockView(child) }
            }
        }

        is BlockNode.BulletList -> Column(modifier = modifier.padding(vertical = 4.dp)) {
            block.items.forEach { item -> ListItemRow(marker = "•", children = item) }
        }

        is BlockNode.OrderedList -> Column(modifier = modifier.padding(vertical = 4.dp)) {
            block.items.forEachIndexed { index, item ->
                ListItemRow(marker = "${block.start + index}.", children = item)
            }
        }
    }
}

@Composable
private fun ListItemRow(marker: String, children: List<BlockNode>) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = marker,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 8.dp)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            children.forEach { child -> MarkdownBlockView(child) }
        }
    }
}

@Composable
fun MarkdownDocumentView(blocks: List<BlockNode>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        blocks.forEach { block -> MarkdownBlockView(block) }
    }
}
