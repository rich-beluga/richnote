package com.rich_beluga.richnote.markdown

import org.json.JSONArray
import org.json.JSONObject

/**
 * Публичный вход в разбор Markdown — сигнатура та же, что была у чисто-Kotlin версии
 * (`parse(source: String): List<BlockNode>`), поэтому вызывающий код (ui/EditorScreen.kt)
 * не поменялся ни на строчку.
 *
 * Вся логика разбора теперь на native-стороне (Rust, `libmarkdown.so` — сурсы в
 * `/markdown` в корне репозитория, см. там README.md). Этот object только:
 *  1. вызывает [MarkdownNative.parseMarkdown], получая JSON;
 *  2. декодирует JSON обратно в [InlineNode]/[BlockNode] — те же типы, которыми
 *     уже пользуется ui/MarkdownView.kt, там ничего менять не пришлось.
 *
 * Если `.so` ещё не подложен в jniLibs, native вернул null, или JSON оказался
 * неожиданной формы — тихо откатывается на "весь текст одним нераспарсенным
 * параграфом" вместо падения приложения.
 */
object MarkdownParser {

    fun parse(source: String): List<BlockNode> {
        if (!MarkdownNative.isAvailable) {
            return plainFallback(source)
        }
        val json = runCatching { MarkdownNative.parseMarkdown(source) }.getOrNull()
            ?: return plainFallback(source)
        return runCatching { decodeBlocks(json) }.getOrElse { plainFallback(source) }
    }

    private fun plainFallback(source: String): List<BlockNode> =
        listOf(BlockNode.Paragraph(listOf(InlineNode.Text(source))))

    private fun decodeBlocks(json: String): List<BlockNode> {
        val array = JSONArray(json)
        return List(array.length()) { i -> decodeBlock(array.getJSONObject(i)) }
    }

    private fun decodeBlock(obj: JSONObject): BlockNode = when (obj.getString("type")) {
        "paragraph" -> BlockNode.Paragraph(decodeInlineArray(obj.getJSONArray("inline")))
        "thematic_break" -> BlockNode.ThematicBreak
        else -> BlockNode.Paragraph(emptyList())
    }

    private fun decodeInlineArray(array: JSONArray): List<InlineNode> =
        List(array.length()) { i -> decodeInline(array.getJSONObject(i)) }

    private fun decodeInline(obj: JSONObject): InlineNode = when (obj.getString("type")) {
        "text" -> InlineNode.Text(obj.getString("value"))
        "bold" -> InlineNode.Bold(decodeInlineArray(obj.getJSONArray("children")))
        "italic" -> InlineNode.Italic(decodeInlineArray(obj.getJSONArray("children")))
        "code" -> InlineNode.Code(obj.getString("value"))
        else -> InlineNode.Text("")
    }
}
