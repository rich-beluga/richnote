package com.rich_beluga.richnote.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.core.FileTreeUiState
import java.io.File

/**
 * Мини-проводник в боковой шторке — только листинг и выбор файла, никаких
 * операций с файлами (переименование/удаление/копирование). Открывается
 * снаружи через drawerState (см. MainActivity: проверка MANAGE_EXTERNAL_STORAGE
 * идёт ДО открытия, сама шторка ничего не знает про разрешения).
 */
@Composable
fun FileExplorerDrawer(
    drawerState: DrawerState,
    fileTreeState: FileTreeUiState,
    onNavigate: (File) -> Unit,
    onNavigateUp: () -> Unit,
    canGoUp: Boolean,
    onFileSelected: (File) -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = fileTreeState.currentDirectory.absolutePath,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                HorizontalDivider()

                LazyColumn {
                    if (canGoUp) {
                        item(key = "..") {
                            NavigationDrawerItem(
                                label = { Text("..") },
                                selected = false,
                                icon = { Icon(Icons.Filled.ArrowUpward, contentDescription = null) },
                                onClick = onNavigateUp,
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                    items(fileTreeState.entries, key = { it.file.absolutePath }) { entry ->
                        NavigationDrawerItem(
                            label = { Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = false,
                            icon = {
                                Icon(
                                    if (entry.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                if (entry.isDirectory) onNavigate(entry.file) else onFileSelected(entry.file)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        },
        content = content
    )
}
