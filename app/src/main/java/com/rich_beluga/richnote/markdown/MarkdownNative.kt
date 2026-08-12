package com.rich_beluga.richnote.markdown

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
