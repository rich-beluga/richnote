package com.rich_beluga.richnote.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.rich_beluga.richnote.BuildConfig
import com.rich_beluga.richnote.ui.components.InfoCard
import com.rich_beluga.richnote.ui.components.groupedCardShape
import com.rich_beluga.richnote.ui.shapes.morphCycleShapes
import com.rich_beluga.richnote.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onLibrariesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("О приложении") },
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
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = groupedCardShape(index = 0, count = 1),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MorphingCatBadge()

                    Text(
                        text = "RichNote",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "v${BuildConfig.VERSION_NAME} | ${BuildConfig.GIT_SHA}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Normal
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = "A feature-rich note-taking app based on Markdown",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text(
                text = "Developer",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .padding(top = 24.dp)
            )

            DeveloperAvatar(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = "rich_beluga",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = "nya :3",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoCard(
                    icon = painterResource(R.drawable.ic_github),
                    title = "GitHub",
                    description = "Source code",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        uriHandler.openUri("https://github.com/rich-beluga/richnote")
                    }
                )
                InfoCard(
                    icon = painterResource(R.drawable.ic_license),
                    title = "MIT License",
                    description = "License",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = null
                )
            }

            InfoCard(
                icon = painterResource(R.drawable.ic_markdown),
                title = "Markdown",
                description = "Markdown-based notes",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = null,
                modifier = Modifier.padding(top = 10.dp)
            )

            Card(
                shape = groupedCardShape(index = 0, count = 1),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Libraries") },
                    supportingContent = { Text("Открытый код, использованный в проекте") },
                    leadingContent = {
                        Icon(Icons.Filled.MenuBook, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLibrariesClick)
                )
            }
        }
    }
}

@Composable
private fun MorphingCatBadge(modifier: Modifier = Modifier) {
    var shapeIndex by remember { mutableStateOf(0) }
    val morph = remember(shapeIndex) {
        Morph(
            morphCycleShapes[shapeIndex],
            morphCycleShapes[(shapeIndex + 1) % morphCycleShapes.size]
        )
    }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val shapeColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(120.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .clickable(enabled = !progress.isRunning) {
                scope.launch {
                    progress.snapTo(0f)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    )
                    shapeIndex = (shapeIndex + 1) % morphCycleShapes.size
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .drawWithCache {
                    val path = morph.toPath(progress = progress.value).asComposePath()
                    val matrix = Matrix()
                    matrix.scale(x = size.minDimension / 2f, y = size.minDimension / 2f)
                    path.transform(matrix)
                    onDrawBehind {
                        translate(left = size.width / 2f, top = size.height / 2f) {
                            drawPath(path, color = shapeColor)
                        }
                    }
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_cat),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = MaterialTheme.colorScheme.onPrimary,
                blendMode = BlendMode.SrcIn
            ),
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
private fun DeveloperAvatar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val avatar = remember {
        runCatching {
            context.assets.open("rich_beluga.png").use { stream ->
                BitmapFactory.decodeStream(stream).asImageBitmap()
            }
        }.getOrNull()
    }

    avatar?.let {
        Image(
            bitmap = it,
            contentDescription = "developer avatar",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(128.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .padding(2.dp)
                .clip(CircleShape)
        )
    }
}
