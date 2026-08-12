package com.rich_beluga.richnote.ui.explorer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.core.FileTreeUiState
import java.io.File

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

                if (fileTreeState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    HorizontalDivider()
                }

                LazyColumn {
                    if (canGoUp) {
                        item(key = "..") {
                            FileTreeRow(
                                label = "..",
                                icon = { FileTypeIcon(FileTypeIcon.Vector(Icons.Filled.ArrowUpward), null) },
                                onClick = onNavigateUp
                            )
                        }
                    }
                    items(fileTreeState.entries, key = { it.file.absolutePath }) { entry ->
                        FileTreeRow(
                            label = entry.name,
                            icon = {
                                FileTypeIcon(
                                    icon = FileTypeIcons.forFile(entry.name, entry.isDirectory),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                if (entry.isDirectory) onNavigate(entry.file) else onFileSelected(entry.file)
                            }
                        )
                    }
                }
            }
        },
        content = content
    )
}

@Composable
private fun FileTreeRow(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
    }
}
