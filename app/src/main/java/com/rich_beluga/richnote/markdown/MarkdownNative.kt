package com.rich_beluga.richnote.markdown

/**
 * Тонкая JNI-обвязка над нативной библиотекой `libmarkdown.so` (сурсы — в `markdown/`
 * в корне репозитория, Rust). Вся логика разбора Markdown теперь на native-стороне —
 * этот object отвечает только за загрузку `.so` и объявление native-метода.
 *
 * [isAvailable] специально не бросает исключение, если `.so` ещё не подложен —
 * `MarkdownParser` в этом случае откатывается на нераспарсенный текст вместо краша.
 */
internal object MarkdownNative {

    val isAvailable: Boolean = try {
        System.loadLibrary("markdown")
        true
    } catch (_: UnsatisfiedLinkError) {
        false
    }

    @JvmStatic
    external fun parseMarkdown(source: String): String?
}
