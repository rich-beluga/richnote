package com.rich_beluga.richnote.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.R
import com.rich_beluga.richnote.ui.JetBrainsMono
import com.rich_beluga.richnote.core.EditorUiState
import com.rich_beluga.richnote.markdown.MarkdownParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorUiState,
    onContentChange: (TextFieldValue) -> Unit,
    onOpenClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNewClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFileExplorerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isDirty) "${state.fileName} •" else state.fileName,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onFileExplorerClick) {
                        Icon(painterResource(R.drawable.ic_file_explorer), contentDescription = "Проводник")
                    }
                },
                actions = {
                    IconButton(onClick = onNewClick) {
                        Icon(Icons.Filled.InsertDriveFile, contentDescription = "Новый файл")
                    }
                    IconButton(onClick = onOpenClick) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = "Открыть")
                    }
                    IconButton(onClick = { showPreview = !showPreview }) {
                        Icon(
                            if (showPreview) Icons.Filled.Edit else Icons.Filled.Visibility,
                            contentDescription = if (showPreview) "Редактировать" else "Просмотр Markdown"
                        )
                    }
                    IconButton(onClick = onSaveClick, enabled = state.isDirty || state.isNewFile) {
                        Icon(Icons.Filled.Save, contentDescription = "Сохранить")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                showPreview -> MarkdownPreview(text = state.content.text)
                else -> EditorTextArea(state = state, onContentChange = onContentChange)
            }
        }
    }
}

@Composable
private fun MarkdownPreview(text: String) {
    val blocks = remember(text) { MarkdownParser.parse(text) }
    MarkdownDocumentView(
        blocks = blocks,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    )
}

@Composable
private fun EditorTextArea(
    state: EditorUiState,
    onContentChange: (TextFieldValue) -> Unit
) {
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(vScroll)
    ) {
        layoutResult?.let { result ->
            val safeOffset = state.content.selection.start.coerceIn(0, result.layoutInput.text.length)
            val line = result.getLineForOffset(safeOffset)
            val top = result.getLineTop(line)
            val bottom = result.getLineBottom(line)
            with(density) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = top.toDp())
                        .height((bottom - top).toDp())
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                )
            }
        }

        Box(modifier = Modifier.horizontalScroll(hScroll)) {
            BasicTextField(
                value = state.content,
                onValueChange = onContentChange,
                onTextLayout = { layoutResult = it },
                textStyle = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Normal,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
