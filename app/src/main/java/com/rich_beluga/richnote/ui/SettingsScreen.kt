package com.rich_beluga.richnote.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

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
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            // Дефолтная анимация появления пункта при первом составлении экрана:
            // fadeIn()/expandVertically() без кастомных duration/easing — то есть
            // ровно default-спеки самого Compose, не подкрученные вручную.
            // MutableTransitionState(false) + targetState = true в LaunchedEffect —
            // стандартный паттерн "проиграть enter-анимацию один раз при появлении".
            // Когда пунктов станет больше одного — тот же паттерн просто
            // применяется к каждому из них (при желании с небольшим delay
            // между ними для лёгкого stagger-эффекта).
            val itemVisible = remember { MutableTransitionState(false) }
            LaunchedEffect(Unit) { itemVisible.targetState = true }

            AnimatedVisibility(
                visibleState = itemVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ListItem(
                    headlineContent = { Text("О приложении") },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAboutClick)
                )
            }
        }
    }
}
