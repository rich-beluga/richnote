package com.rich_beluga.richnote.core

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * currentDirectory/entries — листинг текущей папки. Ничего не знает про Uri/SAF —
 * работает напрямую с java.io.File, поэтому и требует `MANAGE_EXTERNAL_STORAGE`
 * (без него directory.listFiles() на большинстве путей молча вернёт null на
 * Android 11+ из-за scoped storage).
 */
data class FileTreeUiState(
    val currentDirectory: File = Environment.getExternalStorageDirectory(),
    val entries: List<FileTreeEntry> = emptyList(),
    val isLoading: Boolean = false
)

class FileTreeViewModel : ViewModel() {

    // Мини-проводник специально ограничен корнем внешнего хранилища — вверх выше
    // него (в /storage/emulated и т.п.) подняться нельзя, это не root-explorer.
    private val rootDirectory: File = Environment.getExternalStorageDirectory()

    private val _uiState = MutableStateFlow(FileTreeUiState(currentDirectory = rootDirectory))
    val uiState: StateFlow<FileTreeUiState> = _uiState

    private var loadJob: Job? = null

    init {
        loadDirectory(rootDirectory)
    }

    fun openDirectory(directory: File) {
        loadDirectory(directory)
    }

    fun goUp() {
        val current = _uiState.value.currentDirectory
        if (current == rootDirectory) return
        val parent = current.parentFile ?: return
        loadDirectory(parent)
    }

    val canGoUp: Boolean get() = _uiState.value.currentDirectory != rootDirectory

    /** Сбросить проводник обратно в корень — вызывается при открытии шторки заново. */
    fun resetToRoot() {
        if (_uiState.value.currentDirectory != rootDirectory) loadDirectory(rootDirectory)
    }

    private fun loadDirectory(directory: File) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entries = withContext(Dispatchers.IO) { listEntries(directory) }
            _uiState.update {
                it.copy(currentDirectory = directory, entries = entries, isLoading = false)
            }
        }
    }

    private fun listEntries(directory: File): List<FileTreeEntry> {
        val files = directory.listFiles { file -> !file.isHidden } ?: return emptyList()

        // Ключ сортировки (lowercase-имя) считаем один раз на файл, а не внутри
        // компаратора — compareBy вызывает селекторы на каждое сравнение при
        // sortedWith (O(n log n) вызовов .lowercase() вместо O(n) на большой
        // директории это реально заметно, а не только "на бумаге").
        return files
            .map { file -> FileTreeEntry(file, file.isDirectory) to file.name.lowercase() }
            .sortedWith(compareBy({ (entry, _) -> !entry.isDirectory }, { (_, lowerName) -> lowerName }))
            .map { (entry, _) -> entry }
    }
}
