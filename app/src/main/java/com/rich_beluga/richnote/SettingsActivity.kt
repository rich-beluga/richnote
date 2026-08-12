package com.rich_beluga.richnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.Modifier
import com.rich_beluga.richnote.ui.navigation.Navigation

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier) {
                    Navigation(onExitSettings = { finish() })
                }
            }
        }
    }
}
