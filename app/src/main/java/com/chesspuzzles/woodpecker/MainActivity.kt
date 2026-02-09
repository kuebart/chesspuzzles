package com.chesspuzzles.woodpecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.chesspuzzles.woodpecker.data.preferences.AppPreferences
import com.chesspuzzles.woodpecker.ui.navigation.NavGraph
import com.chesspuzzles.woodpecker.ui.strings.DeStrings
import com.chesspuzzles.woodpecker.ui.strings.EnStrings
import com.chesspuzzles.woodpecker.ui.strings.LocalStrings
import com.chesspuzzles.woodpecker.ui.theme.WoodpeckerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var language by remember { mutableStateOf(appPreferences.getLanguage()) }
            val strings = if (language == "en") EnStrings else DeStrings

            WoodpeckerTheme {
                CompositionLocalProvider(LocalStrings provides strings) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        onLanguageChanged = { lang -> language = lang }
                    )
                }
            }
        }
    }
}
