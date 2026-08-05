package com.rich_beluga.richnote.core

import java.io.File

/** Одна запись в мини-проводнике: файл или папка. */
data class FileTreeEntry(
    val file: File,
    val isDirectory: Boolean
) {
    val name: String get() = file.name
}
