package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QrViewModel
import com.example.ui.viewmodel.ThemeMode

class MainActivity : ComponentActivity() {
    private val viewModel: QrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            var showSplash by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(350))
                    },
                    label = "SplashTransition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onTimeout = { showSplash = false }
                        )
                    } else {
                        MainScreen(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = {
                                val nextMode = if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                                viewModel.setThemeMode(nextMode)
                            }
                        )
                    }
                }
            }
        }
    }
}

