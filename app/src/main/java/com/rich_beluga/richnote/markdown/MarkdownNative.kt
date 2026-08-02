package com.rich_beluga.richnote.markdown

/**
 * Тонкая JNI-обвязка над нативной библиотекой `libmarkdown.so` (сурсы — в `/markdown`
 * в корне репозитория, Rust). Вся логика разбора Markdown теперь на native-стороне —
 * этот object отвечает только за загрузку `.so` и объявление native-метода.
 *
 * `.so` собирается вручную (`cargo ndk ...`, см. `markdown/README.md` в корне репо) и
 * кладётся в `app/src/main/jniLibs/<abi>/libmarkdown.so` — Gradle подхватывает файлы
 * из этой директории в APK автоматически, никакой конфигурации в build.gradle.kts
 * добавлять не нужно.
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
     * Возвращает JSON-массив блоков (формат — см. "Контракт JSON" в markdown/README.md)
     * или null, если разбор упал на native-стороне (см. catch_unwind в lib.rs).
     *
     * @JvmStatic обязателен: метод зарегистрирован в Rust как статический
     * (JNI_OnLoad ожидает JClass вторым аргументом, а не JObject экземпляра object'а).
     */
    @JvmStatic
    external fun parseMarkdown(source: String): String?
}
