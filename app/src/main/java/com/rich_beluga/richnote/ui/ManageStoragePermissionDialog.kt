package com.rich_beluga.richnote.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ManageStoragePermissionDialog(
    onCancel: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Отсутствует разрешение",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Необходимо разрешение MANAGE_EXTERNAL_STORAGE " +
                    "для редактирования файлов на месте. " +
                    "Нажмите кнопку \"Продолжить\" ниже, чтобы предоставить разрешение",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Normal
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Продолжить", fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Отмена", fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal)
            }
        }
    )
}
