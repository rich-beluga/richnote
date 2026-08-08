package com.rich_beluga.richnote.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.markdown.BlockNode
import com.rich_beluga.richnote.markdown.InlineNode
import com.rich_beluga.richnote.markdown.TableAlignment
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
                    fontFamily = JetBrainsMono,
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
            is InlineNode.Strikethrough -> withStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough)
            ) {
                appendInline(node.children, linkColor)
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal
                ),
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

        is BlockNode.Table -> Column(
            modifier = modifier
                .width(IntrinsicSize.Max)
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
        ) {
            TableRowView(cells = block.header, alignments = block.alignments, isHeader = true)
            block.rows.forEach { row ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                TableRowView(cells = row, alignments = block.alignments, isHeader = false)
            }
        }
    }
}

@Composable
private fun TableRowView(cells: List<List<InlineNode>>, alignments: List<TableAlignment>, isHeader: Boolean) {
    val linkColor = MaterialTheme.colorScheme.primary
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                if (isHeader) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
            )
    ) {
        cells.forEachIndexed { index, cell ->
            if (index > 0) {
                VerticalDivider(color = dividerColor)
            }
            val alignment = alignments.getOrElse(index) { TableAlignment.NONE }
            Text(
                text = cell.toAnnotatedString(linkColor),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                ),
                textAlign = when (alignment) {
                    TableAlignment.CENTER -> TextAlign.Center
                    TableAlignment.RIGHT -> TextAlign.End
                    TableAlignment.LEFT, TableAlignment.NONE -> TextAlign.Start
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(cellPadding(alignment))
            )
        }
    }
}

/**
 * Базовый отступ 8dp со всех сторон. При явном выравнивании (не NONE) текст
 * прижимается к одному краю ячейки — добавляем на эту сторону чуть больше
 * воздуха (12dp вместо 8dp), чтобы не липло к VerticalDivider/краю таблицы.
 */
private fun cellPadding(alignment: TableAlignment): PaddingValues = when (alignment) {
    TableAlignment.LEFT -> PaddingValues(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    TableAlignment.RIGHT -> PaddingValues(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    TableAlignment.CENTER -> PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    TableAlignment.NONE -> PaddingValues(8.dp)
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
