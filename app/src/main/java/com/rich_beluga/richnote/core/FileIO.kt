package com.rich_beluga.richnote.core

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object FileIO {

    /** Читает весь файл как текст в UTF-8. */
    fun readText(context: Context, uri: Uri): String {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Не удалось открыть InputStream для $uri" }
            BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                return reader.readText()
            }
        }
    }

    /** Перезаписывает файл целиком новым содержимым. */
    fun writeText(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
            output.write(content.toByteArray(StandardCharsets.UTF_8))
            output.flush()
        } ?: error("Не удалось открыть OutputStream для $uri")
    }

    /** Достаёт человекочитаемое имя файла из Uri (для заголовка редактора). */
    fun queryFileName(context: Context, uri: Uri): String {
        var name = uri.lastPathSegment ?: "Untitled.md"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    /** Сохраняет права на Uri между запусками приложения (обязательно после ACTION_OPEN_DOCUMENT). */
    fun takePersistablePermission(context: Context, uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
    }
}
