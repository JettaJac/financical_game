package com.example.financial_game.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financial_game.ui.theme.Financial_gameTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: PetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Financial_gameTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                LaunchedEffect(state.exitRequested) {
                    if (state.exitRequested) finishAffinity()
                }
                PetScreen(state, viewModel::onAction)
            }
        }
    }
}
