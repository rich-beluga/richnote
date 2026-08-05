package com.rich_beluga.richnote.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.markdown.BlockNode
import com.rich_beluga.richnote.markdown.InlineNode

private fun AnnotatedString.Builder.appendInline(nodes: List<InlineNode>) {
    for (node in nodes) {
        when (node) {
            is InlineNode.Text -> append(node.value)
            is InlineNode.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInline(node.children)
            }
            is InlineNode.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInline(node.children)
            }
            is InlineNode.Code -> withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color.Black.copy(alpha = 0.06f)
                )
            ) {
                append(node.value)
            }
        }
    }
}

fun List<InlineNode>.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    appendInline(this@toAnnotatedString)
}

@Composable
fun MarkdownBlockView(block: BlockNode, modifier: Modifier = Modifier) {
    when (block) {
        is BlockNode.Paragraph -> Text(
            text = block.inline.toAnnotatedString(),
            modifier = modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        BlockNode.ThematicBreak -> HorizontalDivider(modifier = modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun MarkdownDocumentView(blocks: List<BlockNode>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        blocks.forEach { block -> MarkdownBlockView(block) }
    }
}
