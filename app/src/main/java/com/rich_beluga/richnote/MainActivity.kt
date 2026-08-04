package com.rich_beluga.richnote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rich_beluga.richnote.core.EditorViewModel
import com.rich_beluga.richnote.ui.EditorScreen

/**
 * Настройки (и вложенное "О приложении") теперь отдельная Activity — см.
 * SettingsActivity. Здесь только редактор, поэтому никакого локального
 * Screen-enum/переключения состояния больше не нужно.
 */
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
        ActivityResultContracts.CreateDocument("text/plain")
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
                        // Обычный startActivity — никаких overridePendingTransition/
                        // overrideActivityTransition, чтобы анимация открытия была
                        // ровно системная дефолтная, как просили.
                        onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) }
                    )
                }
            }
        }
    }
}
