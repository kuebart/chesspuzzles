package com.chesspuzzles.woodpecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.chesspuzzles.woodpecker.ui.navigation.NavGraph
import com.chesspuzzles.woodpecker.ui.theme.WoodpeckerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WoodpeckerTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
