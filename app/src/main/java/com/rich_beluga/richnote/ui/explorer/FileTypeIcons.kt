package com.rich_beluga.richnote.ui.explorer

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.rich_beluga.richnote.R

sealed interface FileTypeIcon {
    data class Vector(val icon: ImageVector) : FileTypeIcon
    data class Resource(@DrawableRes val id: Int) : FileTypeIcon
}

@Composable
fun FileTypeIcon(icon: FileTypeIcon, contentDescription: String?, modifier: Modifier = Modifier) {
    when (icon) {
        is FileTypeIcon.Vector -> Icon(icon.icon, contentDescription, modifier)
        is FileTypeIcon.Resource -> Icon(painterResource(icon.id), contentDescription, modifier)
    }
}

object FileTypeIcons {

    fun forFile(name: String, isDirectory: Boolean): FileTypeIcon {
        if (isDirectory) return FileTypeIcon.Vector(Icons.Filled.Folder)

        return when (name.substringAfterLast('.', missingDelimiterValue = "").lowercase()) {
            "md", "markdown" -> FileTypeIcon.Resource(R.drawable.ic_markdown)

            "kt", "kts", "java", "rs", "c", "h", "cpp", "hpp",
            "py", "js", "ts", "go", "rb", "swift", "gradle" ->
                FileTypeIcon.Vector(Icons.Filled.Code)

            "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg" ->
                FileTypeIcon.Vector(Icons.Filled.Image)

            "mp3", "wav", "ogg", "flac", "m4a" ->
                FileTypeIcon.Vector(Icons.Filled.AudioFile)

            "mp4", "mkv", "avi", "mov", "webm" ->
                FileTypeIcon.Vector(Icons.Filled.VideoFile)

            "zip", "tar", "gz", "7z", "rar", "xz" ->
                FileTypeIcon.Vector(Icons.Filled.FolderZip)

            "pdf" -> FileTypeIcon.Vector(Icons.Filled.PictureAsPdf)

            "json", "xml", "yaml", "yml", "toml" ->
                FileTypeIcon.Vector(Icons.Filled.DataObject)

            "apk" -> FileTypeIcon.Vector(Icons.Filled.Android)

            "txt", "log" -> FileTypeIcon.Vector(Icons.Filled.Description)

            else -> FileTypeIcon.Vector(Icons.AutoMirrored.Filled.InsertDriveFile)
        }
    }
}
