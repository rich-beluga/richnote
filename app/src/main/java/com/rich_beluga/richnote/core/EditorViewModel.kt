package com.rich_beluga.richnote.core

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EditorUiState(
    val uri: Uri? = null,
    val fileName: String = "Untitled.md",
    val content: TextFieldValue = TextFieldValue(""),
    val originalContent: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isDirty: Boolean get() = content.text != originalContent
    val isNewFile: Boolean get() = uri == null
}

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState

    fun openFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    FileIO.takePersistablePermission(context, uri)
                    val text = FileIO.readText(context, uri)
                    val name = FileIO.queryFileName(context, uri)
                    Triple(text, name, uri)
                }
            }.onSuccess { (text, name, openedUri) ->
                _uiState.update {
                    it.copy(
                        uri = openedUri,
                        fileName = name,
                        content = TextFieldValue(text),
                        originalContent = text,
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Ошибка открытия файла") }
            }
        }
    }

    fun save(context: Context) {
        val state = _uiState.value
        val uri = state.uri ?: return
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { FileIO.writeText(context, uri, state.content.text) }
            }.onSuccess {
                _uiState.update { it.copy(originalContent = state.content.text) }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Ошибка сохранения") }
            }
        }
    }

    fun saveAsNewUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val text = _uiState.value.content.text
            runCatching {
                withContext(Dispatchers.IO) {
                    FileIO.takePersistablePermission(context, uri)
                    FileIO.writeText(context, uri, text)
                    FileIO.queryFileName(context, uri)
                }
            }.onSuccess { name ->
                _uiState.update { it.copy(uri = uri, fileName = name, originalContent = text) }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Ошибка сохранения") }
            }
        }
    }

    fun onContentChange(newValue: TextFieldValue) {
        _uiState.update { it.copy(content = newValue) }
    }

    fun newFile() {
        _uiState.value = EditorUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
