package com.cutener.raising

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.cutener.raising.game.engine.GameInitializer
import com.cutener.raising.navigation.CutenerNavGraph
import com.cutener.raising.ui.theme.CutenerRaisingTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var gameInitializer: GameInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameInitializer.initialize()
        enableEdgeToEdge()
        
        setContent {
            CutenerRaisingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CutenerNavGraph(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
