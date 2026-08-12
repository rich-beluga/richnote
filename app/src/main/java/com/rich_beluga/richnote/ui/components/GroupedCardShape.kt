package com.rich_beluga.richnote.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun groupedCardShape(
    index: Int,
    count: Int,
    outerRadius: Dp = 24.dp,
    innerRadius: Dp = 4.dp
): RoundedCornerShape {
    val isFirst = index == 0
    val isLast = index == count - 1
    val top = if (isFirst) outerRadius else innerRadius
    val bottom = if (isLast) outerRadius else innerRadius
    return RoundedCornerShape(
        topStart = top,
        topEnd = top,
        bottomStart = bottom,
        bottomEnd = bottom
    )
}
