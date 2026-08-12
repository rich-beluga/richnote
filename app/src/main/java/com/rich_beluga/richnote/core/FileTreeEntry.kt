package com.rich_beluga.richnote.core

import java.io.File

data class FileTreeEntry(
    val file: File,
    val isDirectory: Boolean
) {
    val name: String get() = file.name
}
