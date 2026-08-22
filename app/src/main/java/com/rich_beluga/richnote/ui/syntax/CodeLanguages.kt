package com.rich_beluga.richnote.ui.syntax

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.rich_beluga.richnote.ui.syntax.kotlin.kotlinSyntaxRules

/**
 * Реестр подсветок кодовых блоков по инфо-строке фенса:
 * первая лексема инфо-строки, без учёта регистра и префикса "language-".
 */
object CodeLanguages {

    private val kotlinRules = kotlinSyntaxRules

    private val aliases: Map<String, List<SyntaxRule>> = mapOf(
        // kotlin
        "kt" to kotlinRules,
        "kotlin" to kotlinRules,
        "kts" to kotlinRules,
        // java
        "java" to emptyList(),
        // python
        "py" to emptyList(),
        "python" to emptyList(),
        "python3" to emptyList(),
        "py3" to emptyList(),
        // yaml
        "yaml" to emptyList(),
        "yml" to emptyList(),
        // markdown
        "md" to emptyList(),
        "markdown" to emptyList(),
        "mkd" to emptyList(),
        // javascript
        "js" to emptyList(),
        "javascript" to emptyList(),
        "jsx" to emptyList(),
        "mjs" to emptyList(),
        // css
        "css" to emptyList(),
        // html
        "html" to emptyList(),
        "htm" to emptyList(),
        // c
        "c" to emptyList(),
        // c++
        "c++" to emptyList(),
        "cpp" to emptyList(),
        "cxx" to emptyList(),
        "cc" to emptyList(),
        // csv
        "csv" to emptyList(),
        // asm
        "asm" to emptyList(),
        "assembly" to emptyList(),
        "nasm" to emptyList(),
        // shell
        "bash" to emptyList(),
        "sh" to emptyList(),
        "zsh" to emptyList(),
        "shell" to emptyList(),
        // rust
        "rust" to emptyList(),
        "rs" to emptyList(),
        // go
        "go" to emptyList(),
        "golang" to emptyList(),
        // batch
        "bat" to emptyList(),
        "batch" to emptyList(),
        "cmd" to emptyList(),
        // json
        "json" to emptyList(),
        "jsonc" to emptyList(),
        "json5" to emptyList()
    )

    /** Нормализованный канонический язык инфо-строки или null. */
    fun resolve(infoString: String?): String? {
        val token = infoString?.trim()
            ?.split(Regex("\\s+"))
            ?.firstOrNull()
            ?.lowercase()
            ?: return null
        val lang = token.removePrefix("language-")
        return lang.takeIf { it in aliases }
    }

    internal fun rulesFor(infoString: String): List<SyntaxRule>? =
        aliases[resolve(infoString) ?: return null]?.takeIf { it.isNotEmpty() }

    /** Подсветка текста кодового блока; без поддержки языка — без стилей. */
    fun highlight(infoString: String?, text: String): AnnotatedString {
        val rules = infoString?.let(::rulesFor)
            ?: return buildAnnotatedString { append(text) }
        return RegexSyntaxHighlighter(rules).highlight(text)
    }
}
