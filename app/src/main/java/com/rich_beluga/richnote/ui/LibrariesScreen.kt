package com.rich_beluga.richnote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rich_beluga.richnote.R
import com.rich_beluga.richnote.ui.components.LibraryCard
import com.rich_beluga.richnote.ui.components.LibraryLink
import com.rich_beluga.richnote.ui.components.groupedCardShape

/**
 * Данные одной библиотеки. Список ниже — реальные зависимости проекта: артефакты
 * из app/build.gradle.kts плюс нативные из markdown/Cargo.toml и вендоренный
 * markdown/cmark-gfm.
 */
private data class LibraryInfo(
    val title: String,
    val description: String,
    val license: String,
    val licenseUrl: String,
    val author: String,
    val authorUrl: String
)

private val libraries = listOf(
    LibraryInfo(
        title = "Jetpack Compose",
        description = "Декларативный UI-тулкит для Android: ui, foundation, animation, " +
            "material3, material-icons-extended — все экраны приложения.",
        license = "Apache-2.0",
        licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
        author = "Google",
        authorUrl = "https://developer.android.com/jetpack/compose"
    ),
    LibraryInfo(
        title = "AndroidX Core KTX",
        description = "Kotlin-расширения над базовыми Android Framework API.",
        license = "Apache-2.0",
        licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
        author = "Google",
        authorUrl = "https://developer.android.com/jetpack/androidx/releases/core"
    ),
    LibraryInfo(
        title = "AndroidX Activity Compose",
        description = "Мост между ComponentActivity и Compose: setContent, " +
            "registerForActivityResult.",
        license = "Apache-2.0",
        licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
        author = "Google",
        authorUrl = "https://developer.android.com/jetpack/androidx/releases/activity"
    ),
    LibraryInfo(
        title = "AndroidX Lifecycle",
        description = "ViewModel и collectAsStateWithLifecycle — интеграция " +
            "жизненного цикла с Compose (lifecycle-viewmodel-compose, " +
            "lifecycle-runtime-compose).",
        license = "Apache-2.0",
        licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
        author = "Google",
        authorUrl = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
    ),
    LibraryInfo(
        title = "Kotlin",
        description = "Язык и стандартная библиотека, на которых написан весь " +
            "Kotlin-код приложения.",
        license = "Apache-2.0",
        licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
        author = "JetBrains",
        authorUrl = "https://github.com/JetBrains/kotlin"
    ),
    LibraryInfo(
        title = "jni",
        description = "JNI-биндинги для Rust — мост между Kotlin (MarkdownNative) и " +
            "нативным парсером (markdown/src/lib.rs).",
        license = "MIT / Apache-2.0",
        licenseUrl = "https://opensource.org/license/mit",
        author = "jni-rs",
        authorUrl = "https://github.com/jni-rs/jni-rs"
    ),
    LibraryInfo(
        title = "cmark-gfm",
        description = "cmark-gfm 0.29.0.gfm.13 - CommonMark with GitHub Flavored Markdown converter" +
            "(C) 2014-2016 John MacFarlane",
        license = "BSD-2-Clause",
        licenseUrl = "https://opensource.org/license/bsd-2-clause",
        author = "GitHub / John MacFarlane",
        authorUrl = "https://github.com/github/cmark-gfm"
    )
)

private data class LibraryCardData(
    val info: LibraryInfo,
    val licenseLinks: List<LibraryLink>,
    val authorLinks: List<LibraryLink>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val licenseIcon = painterResource(R.drawable.ic_license)
    val homepageIcon = painterResource(R.drawable.ic_link)

    val items = remember {
        libraries.map { lib ->
            LibraryCardData(
                info = lib,
                licenseLinks = listOf(
                    LibraryLink(
                        icon = licenseIcon,
                        contentDescription = "Открыть текст лицензии",
                        onClick = { uriHandler.openUri(lib.licenseUrl) }
                    )
                ),
                authorLinks = listOf(
                    LibraryLink(
                        icon = homepageIcon,
                        contentDescription = "Открыть страницу проекта",
                        onClick = { uriHandler.openUri(lib.authorUrl) }
                    )
                )
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Libraries") },
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.forEachIndexed { index, item ->
                LibraryCard(
                    title = item.info.title,
                    description = item.info.description,
                    license = item.info.license,
                    author = item.info.author,
                    shape = groupedCardShape(index = index, count = items.size),
                    licenseLinks = item.licenseLinks,
                    authorLinks = item.authorLinks,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
