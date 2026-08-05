package com.rich_beluga.richnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rich_beluga.richnote.ui.AboutScreen
import com.rich_beluga.richnote.ui.SettingsScreen

private enum class SettingsScreenState { Settings, About }

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier) {
                    var screen by remember { mutableStateOf(SettingsScreenState.Settings) }

                    BackHandler(enabled = screen == SettingsScreenState.About) {
                        screen = SettingsScreenState.Settings
                    }

                    when (screen) {
                        SettingsScreenState.Settings -> SettingsScreen(
                            onBackClick = { finish() },
                            onAboutClick = { screen = SettingsScreenState.About }
                        )
                        SettingsScreenState.About -> AboutScreen(
                            onBackClick = { screen = SettingsScreenState.Settings }
                        )
                    }
                }
            }
        }
    }
}
