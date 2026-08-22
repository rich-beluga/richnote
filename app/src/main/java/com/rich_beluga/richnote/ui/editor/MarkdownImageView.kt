package com.rich_beluga.richnote.ui.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import coil.compose.AsyncImage
import java.io.File

/**
 * Модель для Coil из url картинки в markdown. Локальные пути (абсолютные и
 * относительные к baseDir — папке открытой заметки) превращаются в File,
 * схемы (http/https/file/content/data) передаются как есть.
 */
internal fun resolveImageModel(url: String, baseDir: String?): Any? {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return null
    return when {
        trimmed.startsWith("file://") ||
            trimmed.startsWith("content://") ||
            trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.startsWith("data:") -> trimmed
        trimmed.startsWith("/") -> File(Uri.decode(trimmed))
        else -> baseDir?.let { dir ->
            val resolved = File(dir).resolve(Uri.decode(trimmed))
            if (resolved.exists()) resolved else null
        }
    }
}

@Composable
internal fun MarkdownImage(
    url: String,
    alt: String,
    baseDir: String?,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 320.dp
) {
    val model = remember(url, baseDir) { resolveImageModel(url, baseDir) }
    var failed by remember(model) { mutableStateOf(false) }

    if (model == null || failed) {
        MissingImageCaption(alt = alt, modifier = modifier)
        return
    }

    AsyncImage(
        model = model,
        contentDescription = alt.ifBlank { null },
        onError = { failed = true },
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    )
}

@Composable
private fun MissingImageCaption(alt: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = alt.ifBlank { "изображение недоступно" },
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
