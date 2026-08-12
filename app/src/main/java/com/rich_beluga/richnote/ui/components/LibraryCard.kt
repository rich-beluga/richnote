package com.rich_beluga.richnote.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.ui.JetBrainsMono

data class LibraryLink(
    val icon: Painter,
    val contentDescription: String,
    val onClick: () -> Unit
)

@Composable
fun LibraryCard(
    title: String,
    description: String,
    license: String,
    author: String,
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    licenseLinks: List<LibraryLink> = emptyList(),
    authorLinks: List<LibraryLink> = emptyList(),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )

            LibraryMetaRow(
                label = "License:",
                value = license,
                links = licenseLinks,
                contentColor = contentColor,
                modifier = Modifier.padding(top = 10.dp)
            )
            LibraryMetaRow(
                label = "Author:",
                value = author,
                links = authorLinks,
                contentColor = contentColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    Card(
        onClick = { onClick?.invoke() },
        shape = shape,
        modifier = modifier,
        colors = colors
    ) {
        content()
    }
}

@Composable
private fun LibraryMetaRow(
    label: String,
    value: String,
    links: List<LibraryLink>,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold
            ),
            color = contentColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            color = contentColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 4.dp)
        )
        links.forEach { link ->
            IconButton(
                onClick = link.onClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = link.icon,
                    contentDescription = link.contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
