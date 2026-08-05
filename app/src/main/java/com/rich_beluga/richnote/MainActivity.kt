package com.rich_beluga.richnote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import com.rich_beluga.richnote.core.EditorViewModel
import com.rich_beluga.richnote.core.FileTreeViewModel
import com.rich_beluga.richnote.ui.EditorScreen
import com.rich_beluga.richnote.ui.FileExplorerDrawer
import com.rich_beluga.richnote.ui.ManageStoragePermissionDialog
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    // ACTION_OPEN_DOCUMENT — выбрать существующий файл для чтения/записи
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.openFile(this, it) }
    }

    // ACTION_CREATE_DOCUMENT — создать новый файл ("Сохранить как" / первое сохранение)
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        uri?.let { viewModel.saveAsNewUri(this, it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dynamicColor = true
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = when {
                dynamicColor -> if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
                else -> MaterialTheme.colorScheme
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    val fileTreeViewModel: FileTreeViewModel = composeViewModel()
                    val fileTreeState by fileTreeViewModel.uiState.collectAsStateWithLifecycle()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()
                    var showStoragePermissionDialog by remember { mutableStateOf(false) }

                    if (showStoragePermissionDialog) {
                        ManageStoragePermissionDialog(
                            // "Отмена" — выйти, а не просто закрыть диалог.
                            onCancel = { finishAffinity() },
                            onContinue = {
                                showStoragePermissionDialog = false
                                openManageStorageSettings()
                            }
                        )
                    }

                    FileExplorerDrawer(
                        drawerState = drawerState,
                        fileTreeState = fileTreeState,
                        onNavigate = fileTreeViewModel::openDirectory,
                        onNavigateUp = fileTreeViewModel::goUp,
                        canGoUp = fileTreeViewModel.canGoUp,
                        onFileSelected = { file ->
                            viewModel.openFile(this, Uri.fromFile(file))
                            coroutineScope.launch { drawerState.close() }
                        }
                    ) {
                        EditorScreen(
                            state = state,
                            onContentChange = viewModel::onContentChange,
                            onOpenClick = { openDocumentLauncher.launch(arrayOf("text/*")) },
                            onSaveClick = {
                                if (state.isNewFile) {
                                    createDocumentLauncher.launch(state.fileName)
                                } else {
                                    viewModel.save(this)
                                }
                            },
                            onNewClick = { viewModel.newFile() },
                            onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
                            onFileExplorerClick = {
                                if (Environment.isExternalStorageManager()) {
                                    fileTreeViewModel.resetToRoot()
                                    coroutineScope.launch { drawerState.open() }
                                } else {
                                    showStoragePermissionDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    /** ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION — экран разрешения именно для
     *  этого приложения. Если недоступен на конкретном OEM/API — фолбэк на общий
     *  список "Доступ ко всем файлам" без привязки к пакету. */
    private fun openManageStorageSettings() {
        runCatching {
            startActivity(
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }.onFailure {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }
}
