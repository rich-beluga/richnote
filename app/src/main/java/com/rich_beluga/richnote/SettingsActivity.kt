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

/**
 * Настройки — отдельная Activity, а не composable-состояние внутри MainActivity.
 * Ради этого и затевалось: MainActivity.startActivity(Intent(...)) без
 * overridePendingTransition/overrideActivityTransition даёт стандартный системный
 * Activity-transition открытия бесплатно — как раз "дефолтные анимации Android".
 *
 * "О приложении" остаётся вложенной локальной навигацией внутри ЭТОЙ Activity
 * (Settings ⇄ About), а не отдельной Activity — так и было раньше, только весь
 * узел целиком переехал сюда из MainActivity вместе с настройками.
 */
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

                    // Системный back (кнопка/жест) на About должен вести на Settings,
                    // а не сразу закрывать Activity в обход экрана настроек — иначе
                    // жест ведёт себя иначе, чем стрелка "назад" в шапке.
                    BackHandler(enabled = screen == SettingsScreenState.About) {
                        screen = SettingsScreenState.Settings
                    }

                    when (screen) {
                        SettingsScreenState.Settings -> SettingsScreen(
                            // finish() — обычное закрытие Activity, тоже с дефолтной
                            // системной анимацией (никаких переопределений).
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
