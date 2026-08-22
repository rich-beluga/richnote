package com.rich_beluga.richnote.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.markdown.BlockNode
import com.rich_beluga.richnote.markdown.InlineNode
import com.rich_beluga.richnote.markdown.TableAlignment
import com.rich_beluga.richnote.ui.JetBrainsMono
import com.rich_beluga.richnote.ui.syntax.CodeLanguages

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
                // картинки глубоко внутри вложенных инлайнов (не на верхнем
                // уровне блока) — заглушка; блочный рендер см. InlineContentView
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

private fun InlineNode.containsImage(): Boolean = when (this) {
    is InlineNode.Image -> true
    is InlineNode.Bold -> children.any { it.containsImage() }
    is InlineNode.Italic -> children.any { it.containsImage() }
    is InlineNode.Link -> children.any { it.containsImage() }
    is InlineNode.Strikethrough -> children.any { it.containsImage() }
    else -> false
}

private fun InlineNode.asStandaloneImage(): InlineNode.Image? = when (this) {
    is InlineNode.Image -> this
    // [![alt](img)](ссылка) — картинка-обёрнутая-в-ссылку рендерится как картинка
    is InlineNode.Link -> children
        .filterNot { it is InlineNode.Text && it.value.isBlank() }
        .singleOrNull() as? InlineNode.Image
    else -> null
}

/**
 * Рендерит список инлайнов: чистый текст — одним Text, наличие картинок —
 * чередованием Text и MarkdownImage в колонке.
 */
@Composable
private fun InlineContentView(
    inlines: List<InlineNode>,
    linkColor: Color,
    style: TextStyle,
    baseDir: String?,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxImageHeight: Dp = 320.dp
) {
    if (inlines.none { it.containsImage() }) {
        Text(
            text = inlines.toAnnotatedString(linkColor),
            style = style,
            textAlign = textAlign,
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier) {
        val textRun = mutableListOf<InlineNode>()

        @Composable
        fun flushText() {
            if (textRun.isNotEmpty()) {
                Text(
                    text = textRun.toList().toAnnotatedString(linkColor),
                    style = style,
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth()
                )
                textRun.clear()
            }
        }

        for (node in inlines) {
            val image = node.asStandaloneImage()
            if (image != null) {
                flushText()
                MarkdownImage(
                    url = image.url,
                    alt = image.alt,
                    baseDir = baseDir,
                    maxHeight = maxImageHeight,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            } else {
                textRun += node
            }
        }
        flushText()
    }
}

@Composable
private fun headingStyle(level: Int): TextStyle {
    val typography = MaterialTheme.typography
    val base = when (level) {
        1 -> typography.headlineLarge
        2 -> typography.headlineMedium
        3 -> typography.headlineSmall
        4 -> typography.titleLarge
        5 -> typography.titleMedium
        6 -> typography.titleSmall
        else -> typography.titleSmall
    }
    return base.copy(fontWeight = FontWeight.Bold)
}

@Composable
fun MarkdownBlockView(
    block: BlockNode,
    modifier: Modifier = Modifier,
    baseDir: String? = null
) {
    val linkColor = MaterialTheme.colorScheme.primary

    when (block) {
        is BlockNode.Paragraph -> InlineContentView(
            inlines = block.inline,
            linkColor = linkColor,
            style = MaterialTheme.typography.bodyLarge,
            baseDir = baseDir,
            modifier = modifier.padding(vertical = 4.dp)
        )

        is BlockNode.Heading -> InlineContentView(
            inlines = block.inline,
            linkColor = linkColor,
            style = headingStyle(block.level),
            baseDir = baseDir,
            modifier = modifier.padding(top = 12.dp, bottom = 4.dp)
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
            val highlightedCode = remember(block.text, block.language) {
                CodeLanguages.highlight(block.language, block.text)
            }
            Text(
                text = highlightedCode,
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
                block.children.forEach { child -> MarkdownBlockView(child, baseDir = baseDir) }
            }
        }

        is BlockNode.BulletList -> Column(modifier = modifier.padding(vertical = 4.dp)) {
            block.items.forEach { item -> ListItemRow(marker = "•", children = item, baseDir = baseDir) }
        }

        is BlockNode.OrderedList -> Column(modifier = modifier.padding(vertical = 4.dp)) {
            block.items.forEachIndexed { index, item ->
                ListItemRow(marker = "${block.start + index}.", children = item, baseDir = baseDir)
            }
        }

        is BlockNode.Table -> BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            val minTableWidth = maxWidth
            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .width(IntrinsicSize.Max)
                    .widthIn(min = minTableWidth)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            ) {
                TableRowView(cells = block.header, alignments = block.alignments, isHeader = true, baseDir = baseDir)
                block.rows.forEach { row ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    TableRowView(cells = row, alignments = block.alignments, isHeader = false, baseDir = baseDir)
                }
            }
        }
    }
}

@Composable
private fun TableRowView(
    cells: List<List<InlineNode>>,
    alignments: List<TableAlignment>,
    isHeader: Boolean,
    baseDir: String? = null
) {
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
            InlineContentView(
                inlines = cell,
                linkColor = linkColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                ),
                baseDir = baseDir,
                textAlign = when (alignment) {
                    TableAlignment.CENTER -> TextAlign.Center
                    TableAlignment.RIGHT -> TextAlign.End
                    TableAlignment.LEFT, TableAlignment.NONE -> TextAlign.Start
                },
                maxImageHeight = 160.dp,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 120.dp)
                    .padding(cellPadding(alignment))
            )
        }
    }
}

private fun cellPadding(alignment: TableAlignment): PaddingValues = when (alignment) {
    TableAlignment.LEFT -> PaddingValues(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    TableAlignment.RIGHT -> PaddingValues(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    TableAlignment.CENTER -> PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    TableAlignment.NONE -> PaddingValues(8.dp)
}

@Composable
private fun ListItemRow(marker: String, children: List<BlockNode>, baseDir: String? = null) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = marker,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 8.dp)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            children.forEach { child -> MarkdownBlockView(child, baseDir = baseDir) }
        }
    }
}

@Composable
fun MarkdownDocumentView(
    blocks: List<BlockNode>,
    modifier: Modifier = Modifier,
    baseDir: String? = null
) {
    Column(modifier = modifier) {
        blocks.forEach { block -> MarkdownBlockView(block, baseDir = baseDir) }
    }
}
