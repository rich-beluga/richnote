package com.rich_beluga.richnote.core

import android.webkit.MimeTypeMap

object FileMimeGuard {

    private val blockedMimePrefixes = listOf("image/", "audio/", "video/")

    private val blockedMimeTypes = setOf(
        "application/zip",
        "application/x-7z-compressed",
        "application/x-rar-compressed",
        "application/vnd.rar",
        "application/x-tar",
        "application/gzip",
        "application/x-gzip",
        "application/x-bzip2",
        "application/vnd.android.package-archive",
        "application/pdf",
        "application/x-executable",
        "application/x-sharedlib",
        "application/java-archive",
        "application/octet-stream"
    )

    private val blockedExtensions = setOf(
        "zip", "rar", "7z", "tar", "gz", "tgz", "xz", "bz2",
        "apk", "aab", "exe", "dll", "so", "jar", "aar", "iso", "dmg", "class",
        "png", "jpg", "jpeg", "gif", "webp", "bmp", "ico",
        "mp3", "wav", "ogg", "flac", "m4a", "mp4", "mkv", "avi", "mov", "webm",
        "pdf"
    )

    fun isBlocked(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        if (extension.isEmpty()) return false

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (mimeType != null) {
            if (blockedMimePrefixes.any { mimeType.startsWith(it) }) return true
            if (mimeType in blockedMimeTypes) return true
        }

        return extension in blockedExtensions
    }
}
