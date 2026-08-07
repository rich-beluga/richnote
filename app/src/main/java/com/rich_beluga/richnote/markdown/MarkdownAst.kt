package com.rich_beluga.richnote.markdown

sealed interface InlineNode {
    data class Text(val value: String) : InlineNode
    data class Bold(val children: List<InlineNode>) : InlineNode
    data class Italic(val children: List<InlineNode>) : InlineNode
    data class Code(val value: String) : InlineNode
    data class Link(val children: List<InlineNode>, val url: String) : InlineNode
    data class Image(val alt: String, val url: String) : InlineNode
    data object LineBreak : InlineNode
}

sealed interface BlockNode {
    data class Paragraph(val inline: List<InlineNode>) : BlockNode
    data class Heading(val level: Int, val inline: List<InlineNode>) : BlockNode
    data object ThematicBreak : BlockNode
    data class CodeBlock(val text: String, val language: String? = null) : BlockNode
    data class BlockQuote(val children: List<BlockNode>) : BlockNode
    data class BulletList(val items: List<List<BlockNode>>) : BlockNode
    data class OrderedList(val start: Int, val items: List<List<BlockNode>>) : BlockNode
}
