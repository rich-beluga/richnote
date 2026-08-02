package com.rich_beluga.richnote.markdown

/**
 * Модель распарсенного Markdown-документа. Никакой зависимости от Android/Compose —
 * чистый Kotlin (собирается и тестируется как обычный JVM-код). Слой отрисовки
 * (Compose) лежит отдельно, в ui/MarkdownView.kt, и ничего не знает про сам парсер,
 * кроме этих двух sealed-иерархий.
 */

/** Инлайн-узлы — то, что может встретиться внутри одной строки/параграфа. */
sealed interface InlineNode {
    data class Text(val value: String) : InlineNode
    data class Bold(val children: List<InlineNode>) : InlineNode
    data class Italic(val children: List<InlineNode>) : InlineNode
    /** Код внутри `code` НЕ разбирается дальше — по спеке Markdown это буквальный текст. */
    data class Code(val value: String) : InlineNode
}

/** Блочные узлы — то, из чего состоит документ построчно/по абзацам. */
sealed interface BlockNode {
    data class Paragraph(val inline: List<InlineNode>) : BlockNode
    /** Разделитель --- / *** / ___ (thematic break в терминах CommonMark). */
    data object ThematicBreak : BlockNode
}
