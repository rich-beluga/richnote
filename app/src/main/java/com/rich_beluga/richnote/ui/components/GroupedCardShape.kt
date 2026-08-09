package com.rich_beluga.richnote.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Скругление карточки внутри "сегментированной" MD3-группы по её позиции: первая —
 * большой радиус сверху, маленький снизу; последняя — наоборот; средние — маленький
 * со всех сторон. Единственный элемент группы (isFirst && isLast) получает большой
 * радиус со всех сторон — то есть при одном пункте выглядит как обычная цельная
 * карточка, а как только появляется второй/третий — группа сама "сегментируется".
 */
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
