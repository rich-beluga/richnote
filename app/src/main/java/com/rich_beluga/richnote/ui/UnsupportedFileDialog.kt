package com.rich_beluga.richnote.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Показывается вместо открытия файла, когда [com.rich_beluga.richnote.core.FileMimeGuard]
 * решает, что файл — архив/бинарник, а не текст (см. MainActivity.onFileSelected).
 * Одна кнопка — закрыть диалог, без варианта "всё равно открыть".
 *
 * Текст (title/message/confirmButtonText) — без дефолтов специально, чтобы
 * задать его самостоятельно на месте вызова, а не гадать за вас.
 */
@Composable
fun UnsupportedFileDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(confirmButtonText)
            }
        }
    )
}
