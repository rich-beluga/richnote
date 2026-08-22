package com.rich_beluga.richnote.markdown

object MarkdownParser {

    fun parse(source: String): List<BlockNode> {
        if (!MarkdownNative.isAvailable) {
            return plainFallback(source)
        }
        val html = runCatching { MarkdownNative.parseMarkdown(source) }.getOrNull()
            ?: return plainFallback(source)
        return runCatching { htmlToBlocks(html) }.getOrElse { plainFallback(source) }
    }

    private fun plainFallback(source: String): List<BlockNode> =
        listOf(BlockNode.Paragraph(listOf(InlineNode.Text(source))))

    private sealed interface HtmlNode {
        data class Element(val tag: String, val attrs: Map<String, String>, val children: List<HtmlNode>) : HtmlNode
        data class Text(val text: String) : HtmlNode
    }

    private sealed interface HtmlToken {
        data class Open(val tag: String, val attrs: Map<String, String>) : HtmlToken
        data class Close(val tag: String) : HtmlToken
        data class SelfClosing(val tag: String, val attrs: Map<String, String>) : HtmlToken
        data class Text(val text: String) : HtmlToken
    }

    private val VOID_TAGS = setOf("hr", "br", "img")

    private val commentRegex = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)
    private val tagRegex = Regex(
        "<(/?)([a-zA-Z][a-zA-Z0-9]*)((?:\\s+[a-zA-Z_:][-a-zA-Z0-9_:.]*"
            + "(?:=(?:\"[^\"]*\"|'[^']*'|[^\\s\"'=<>`]+))?)*)\\s*(/?)>"
    )
    private val attrRegex = Regex(
        "([a-zA-Z_:][-a-zA-Z0-9_:.]*)=(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'=<>`]+))"
    )
    private val numericEntityRegex = Regex("&#(x?[0-9a-fA-F]+);")

    private fun tokenize(html: String): List<HtmlToken> {
        val src = commentRegex.replace(html, "")
        val tokens = mutableListOf<HtmlToken>()
        var pos = 0
        for (match in tagRegex.findAll(src)) {
            if (match.range.first > pos) {
                val text = src.substring(pos, match.range.first)
                if (text.isNotEmpty()) tokens += HtmlToken.Text(unescapeHtml(text))
            }
            val isClose = match.groupValues[1] == "/"
            val tag = match.groupValues[2].lowercase()
            val attrs = attrRegex.findAll(match.groupValues[3]).associate { m ->
                val value = m.groups[2]?.value ?: m.groups[3]?.value ?: m.groups[4]?.value ?: ""
                m.groupValues[1] to unescapeHtml(value)
            }
            val selfClosing = match.groupValues[4] == "/" || tag in VOID_TAGS
            tokens += when {
                isClose -> HtmlToken.Close(tag)
                selfClosing -> HtmlToken.SelfClosing(tag, attrs)
                else -> HtmlToken.Open(tag, attrs)
            }
            pos = match.range.last + 1
        }
        if (pos < src.length) {
            val text = src.substring(pos)
            if (text.isNotEmpty()) tokens += HtmlToken.Text(unescapeHtml(text))
        }
        return tokens
    }

    private class Frame(val tag: String?, val attrs: Map<String, String>) {
        val children = mutableListOf<HtmlNode>()
    }

    private fun buildTree(tokens: List<HtmlToken>): List<HtmlNode> {
        val root = Frame(null, emptyMap())
        val stack = ArrayDeque<Frame>()
        stack.addLast(root)

        for (token in tokens) {
            when (token) {
                is HtmlToken.Text -> stack.last().children += HtmlNode.Text(token.text)
                is HtmlToken.SelfClosing ->
                    stack.last().children += HtmlNode.Element(token.tag, token.attrs, emptyList())
                is HtmlToken.Open -> stack.addLast(Frame(token.tag, token.attrs))
                is HtmlToken.Close -> {
                    val idx = stack.indexOfLast { it.tag == token.tag }
                    if (idx > 0) {
                        while (stack.size > idx) {
                            val finished = stack.removeLast()
                            stack.last().children += HtmlNode.Element(finished.tag!!, finished.attrs, finished.children)
                        }
                    }
                }
            }
        }
        while (stack.size > 1) {
            val finished = stack.removeLast()
            stack.last().children += HtmlNode.Element(finished.tag!!, finished.attrs, finished.children)
        }
        return root.children
    }

    private fun unescapeHtml(text: String): String {
        var result = text
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
        result = numericEntityRegex.replace(result) { m ->
            val body = m.groupValues[1]
            val code = if (body.startsWith("x") || body.startsWith("X")) {
                body.substring(1).toIntOrNull(16)
            } else {
                body.toIntOrNull()
            }
            code?.let { runCatching { String(Character.toChars(it)) }.getOrNull() } ?: m.value
        }
        return result.replace("&amp;", "&")
    }

    private val BLOCK_TAGS = setOf("p", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "ul", "ol", "pre", "hr", "table")

    // сырой HTML проходит сквозь cmark-gfm (unsafe): незнакомые теги прозрачно
    // разворачиваем, непоказываемые/потенциально опасные содержимые — выбрасываем
    private val DROP_TAGS = setOf(
        "script", "style", "head", "title", "template",
        "iframe", "object", "embed", "noscript", "svg", "math"
    )

    private fun htmlToBlocks(html: String): List<BlockNode> = elementsToBlocks(buildTree(tokenize(html)))

    private fun elementsToBlocks(nodes: List<HtmlNode>): List<BlockNode> = nodes.flatMap { node ->
        when (node) {
            is HtmlNode.Element -> elementToBlocks(node)
            is HtmlNode.Text -> textToBlocks(node)
        }
    }

    private fun textToBlocks(text: HtmlNode.Text): List<BlockNode> {
        val trimmed = text.text.trim()
        return if (trimmed.isEmpty()) {
            emptyList()
        } else {
            listOf(BlockNode.Paragraph(listOf(InlineNode.Text(trimmed))))
        }
    }

    private fun elementToBlocks(el: HtmlNode.Element): List<BlockNode> = when (el.tag) {
        "p" -> listOf(BlockNode.Paragraph(childrenToInline(el.children)))
        "hr" -> listOf(BlockNode.ThematicBreak)
        "h1", "h2", "h3", "h4", "h5", "h6" ->
            listOf(BlockNode.Heading(el.tag.substring(1).toInt(), childrenToInline(el.children)))
        "blockquote" -> listOf(BlockNode.BlockQuote(elementsToBlocks(el.children)))
        "ul" -> listOf(BlockNode.BulletList(listItems(el)))
        "ol" -> listOf(BlockNode.OrderedList(start = el.attrs["start"]?.toIntOrNull() ?: 1, items = listItems(el)))
        "pre" -> listOf(codeBlockFrom(el))
        "table" -> listOf(tableFrom(el))
        // блочный <img>: cmark не заворачивает его в <p>
        "img" -> listOf(BlockNode.Paragraph(listOf(imageInline(el.attrs))))
        in DROP_TAGS -> emptyList()
        else -> elementsToBlocks(el.children)
    }

    private fun listItems(list: HtmlNode.Element): List<List<BlockNode>> =
        list.children.filterIsInstance<HtmlNode.Element>()
            .filter { it.tag == "li" }
            .map(::listItemToBlocks)

    private fun listItemToBlocks(li: HtmlNode.Element): List<BlockNode> {
        val hasBlockChildren = li.children.any { it is HtmlNode.Element && it.tag in BLOCK_TAGS }
        return if (hasBlockChildren) {
            elementsToBlocks(li.children)
        } else {
            listOf(BlockNode.Paragraph(childrenToInline(li.children)))
        }
    }

    private fun codeBlockFrom(pre: HtmlNode.Element): BlockNode.CodeBlock {
        val codeEl = pre.children.filterIsInstance<HtmlNode.Element>().firstOrNull { it.tag == "code" }
        val text = codeEl?.children.orEmpty().filterIsInstance<HtmlNode.Text>().joinToString("") { it.text }
        val language = codeEl?.attrs?.get("class")?.removePrefix("language-")
        return BlockNode.CodeBlock(text.removeSuffix("\n"), language)
    }

    private fun tableFrom(table: HtmlNode.Element): BlockNode.Table {
        val children = table.children.filterIsInstance<HtmlNode.Element>()
        val thead = children.firstOrNull { it.tag == "thead" }
        val tbody = children.firstOrNull { it.tag == "tbody" }

        val headerCells = thead?.children.orEmpty().filterIsInstance<HtmlNode.Element>()
            .firstOrNull { it.tag == "tr" }
            ?.children.orEmpty().filterIsInstance<HtmlNode.Element>()
            .filter { it.tag == "th" }

        val alignments = headerCells.map(::cellAlignment)
        val header = headerCells.map { childrenToInline(it.children) }

        val rows = tbody?.children.orEmpty().filterIsInstance<HtmlNode.Element>()
            .filter { it.tag == "tr" }
            .map { row ->
                row.children.filterIsInstance<HtmlNode.Element>()
                    .filter { it.tag == "td" }
                    .map { cell -> childrenToInline(cell.children) }
            }

        return BlockNode.Table(alignments = alignments, header = header, rows = rows)
    }

    private fun cellAlignment(cell: HtmlNode.Element): TableAlignment = when (cell.attrs["align"]) {
        "left" -> TableAlignment.LEFT
        "center" -> TableAlignment.CENTER
        "right" -> TableAlignment.RIGHT
        else -> TableAlignment.NONE
    }

    private fun childrenToInline(children: List<HtmlNode>): List<InlineNode> =
        children.flatMap(::nodeToInline)

    private fun nodeToInline(node: HtmlNode): List<InlineNode> = when (node) {
        is HtmlNode.Text -> listOf(InlineNode.Text(node.text))
        is HtmlNode.Element -> when (node.tag) {
            "strong" -> listOf(InlineNode.Bold(childrenToInline(node.children)))
            "em" -> listOf(InlineNode.Italic(childrenToInline(node.children)))
            "code" -> listOf(
                InlineNode.Code(
                    node.children.filterIsInstance<HtmlNode.Text>().joinToString("") { it.text }
                )
            )
            "a" -> listOf(InlineNode.Link(childrenToInline(node.children), node.attrs["href"].orEmpty()))
            "img" -> listOf(imageInline(node.attrs))
            "br" -> listOf(InlineNode.LineBreak)
            "del" -> listOf(InlineNode.Strikethrough(childrenToInline(node.children)))
            in DROP_TAGS -> emptyList()
            else -> childrenToInline(node.children)
        }
    }

    private fun imageInline(attrs: Map<String, String>): InlineNode.Image =
        InlineNode.Image(alt = attrs["alt"].orEmpty(), url = attrs["src"].orEmpty())
}
