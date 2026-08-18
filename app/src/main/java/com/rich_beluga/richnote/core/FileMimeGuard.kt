package com.rich_beluga.richnote.core

import android.webkit.MimeTypeMap

object FileMimeGuard {

    private val blockedMimePrefixes = listOf("image/", "audio/", "video/")

    private val blockedMimeTypes = setOf(
        "application/octet-stream",
        "application/gzip",
        "application/x-bzip",
        "application/x-bzip2",
        "application/zstd",
        "application/zip",
        "application/vnd.rar",
        "application/x-7z-compressed",
        "application/x-tar",
        "application/x-freearc",
        "application/java-archive",
        "application/vnd.android.package-archive",
        "application/x-apple-diskimage",
        "application/x-iso9660-image",
        "application/vnd.ms-cab-compressed",
        "application/wasm",
        "application/pdf",
        "application/epub+zip"
    )

    private val blockedExtensions = setOf(
        // Archives / compression
        "zip", "rar", "7z",
        "tar", "gz", "tgz", "bz", "bz2",
        "xz", "txz", "zst", "lz", "lz4",
        "lzh", "cab", "arj", "arc",
        "ace", "z", "cpio",
        "jar", "war", "ear", "aar",
        "apk", "apks", "xapk",

        // Disk / filesystem images
        "iso", "img", "raw", "dd",
        "dmg", "sparseimage", "vhd", "vhdx",
        "vmdk", "qcow", "qcow2", "ova", "ovf",

        // Native / executable
        "exe", "dll", "sys", "scr",
        "msi", "com", "so", "dylib",
        "elf", "a", "o", "obj", "ko",
        "class", "dex", "odex", "vdex", "cdex",

        // Windows / binary document formats
        "doc", "docx", "xls", "xlsx",
        "ppt", "pptx", "xlsm", "xltx",
        "docm", "dotx", "pptm", "potx",

        // Media
        "png", "jpg", "jpeg", "gif", "webp",
        "bmp", "ico", "tif", "tiff",
        "heic", "heif", "avif", "jxl",
        "cr2", "cr3", "nef", "arw", "dng",
        "mp3", "wav", "ogg", "oga", "opus",
        "flac", "m4a", "aac", "wma",
        "aiff", "aif", "amr", "mid", "midi",
        "ape", "wv", "mp4", "m4v", "mkv",
        "webm", "avi", "mov", "wmv", "flv",
        "mpeg", "mpg", "mpe", "3gp", "3g2",
        "ts", "mts", "m2ts", "vob",

        // Documents that shouldn't be treated as Markdown/text
        "pdf", "epub", "djvu", "chm",

        // Database / binary data
        "db", "sqlite", "sqlite3",
        "mdb", "accdb", "dbf",

        // Fonts
        "ttf", "otf", "woff", "woff2",

        // Other binary formats
        "dat", "pak", "bin", "pdb", "dmp",
        "pcap", "pcapng", "wasm"
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
