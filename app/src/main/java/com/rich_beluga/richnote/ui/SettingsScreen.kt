package com.rich_beluga.richnote.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.ui.shapes.Cookie4MaterialShape
import com.rich_beluga.richnote.ui.shapes.DiamondMaterialShape
import com.rich_beluga.richnote.ui.shapes.PillMaterialShape
import com.rich_beluga.richnote.ui.shapes.CircleMaterialShape
import com.rich_beluga.richnote.ui.shapes.Cookie9MaterialShape
import com.rich_beluga.richnote.ui.shapes.ArrowMaterialShape
import com.rich_beluga.richnote.ui.shapes.GhostIshMaterialShape
import com.rich_beluga.richnote.R

/**
 * Один пункт списка настроек. Список сделан data-driven (а не захардкожен
 * пунктами в теле функции) специально: чтобы при добавлении второго/третьего
 * пункта скругление углов у соседних карточек само пересчиталось правильно —
 * см. groupedCardShape ниже.
 */
private data class SettingsItem(
    val icon: Painter,
    val title: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingsHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            )

            val uriHandler = LocalUriHandler.current

            val items = remember {
                listOf(
                    SettingsItem(
                        icon = rememberVectorPainter(Icons.Filled.Info),
                        title = "О приложении",
                        onClick = onAboutClick
                    )
                    SettingsItem(
                        icon = painterResource(R.drawable.github),
                        title = "GitHub репозиторий",
                        onClick = {
                            uriHandler.openUri("https://github.com/rich-beluga/richnote")
                        }
                    )
                )
            }

            val itemsVisible = remember { MutableTransitionState(false) }
            LaunchedEffect(Unit) { itemsVisible.targetState = true }

            AnimatedVisibility(
                visibleState = itemsVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.weight(0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items.forEachIndexed { index, item ->
                        Card(
                            shape = groupedCardShape(index = index, count = items.size),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = { Text(item.title) },
                                leadingContent = {
                                    Icon(item.icon, contentDescription = null)
                                },
                                trailingContent = {
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = item.onClick)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Скругление карточки внутри "сегментированной" группы по её позиции:
 * первая — большой радиус сверху, маленький снизу; последняя — наоборот;
 * средние — маленький со всех сторон. Единственный элемент в группе
 * (isFirst && isLast) получает большой радиус со всех сторон — то есть
 * при одном пункте выглядит как обычная цельная карточка, а как только
 * появляется второй/третий пункт, группа сама "сегментируется".
 */
private fun groupedCardShape(index: Int, count: Int): RoundedCornerShape {
    val isFirst = index == 0
    val isLast = index == count - 1
    val outer = 24.dp
    val inner = 4.dp
    val top = if (isFirst) outer else inner
    val bottom = if (isLast) outer else inner
    return RoundedCornerShape(
        topStart = top,
        topEnd = top,
        bottomStart = bottom,
        bottomEnd = bottom
    )
}

/**
 * Hero-зона над списком настроек: несколько Material Shapes из ui/shapes
 * лениво покачиваются вверх-вниз (RepeatMode.Reverse + синусоидальный easing —
 * без резких рывков), поверх — заголовок и короткое описание. clipToBounds()
 * гарантирует, что фигуры не вылезут за пределы этой зоны даже в крайней
 * точке анимации.
 */
@Composable
private fun SettingsHero(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "settings_hero_shapes")

    val drift1 by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift1"
    )
    val drift2 by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift2"
    )
    val drift3 by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift3"
    )

    Box(
        modifier = modifier.clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-12).dp + 6.dp * drift2, y = 12.dp + 8.dp * drift1)
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    shape = Cookie4MaterialShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 16.dp + 6.dp * drift3, y = 28.dp + 6.dp * drift2)
                .size(52.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    shape = DiamondMaterialShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp + 5.dp * drift1, y = (-8).dp + 8.dp * drift3)
                .size(60.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    shape = PillMaterialShape
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Внешний вид, о приложении и другие параметры",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
