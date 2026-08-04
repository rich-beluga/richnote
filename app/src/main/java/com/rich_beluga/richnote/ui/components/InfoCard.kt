package com.rich_beluga.richnote.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.ui.JetBrainsMono

/**
 * Небольшая карточка Material 3: иконка слева, справа — заголовок и более
 * мелкое описание строкой ниже. Сама карточка не знает, что за иконка,
 * какой текст или какой цвет — всё это приходит снаружи (AboutScreen),
 * здесь только компоновка и типографика.
 *
 * icon — Painter, а не ImageVector: подходит и под painterResource(R.drawable...)
 * для своих иконок из res/drawable, и под rememberVectorPainter() для готовых
 * Material-иконок (последним пользуются плейсхолдеры в AboutScreen).
 */
@Composable
fun InfoCard(
    icon: Painter,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Normal
                    ),
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
