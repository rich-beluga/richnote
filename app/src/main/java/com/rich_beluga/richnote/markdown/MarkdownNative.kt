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

    /**
     * Возвращает HTML, отрендеренный cmark-gfm (safe-режим: сырой HTML и опасные
     * ссылки в исходном markdown экранируются, см. markdown/src/cmark.rs), или null,
     * если разбор упал на native-стороне (см. catch_unwind в lib.rs). Разбор этого HTML
     * обратно в [BlockNode]/[InlineNode] — в MarkdownParser, тут только сырой вызов.
     *
     * @JvmStatic обязателен: метод зарегистрирован в Rust как статический
     * (JNI_OnLoad ожидает JClass вторым аргументом, а не JObject экземпляра object'а).
     */
    @JvmStatic
    external fun parseMarkdown(source: String): String?
}
