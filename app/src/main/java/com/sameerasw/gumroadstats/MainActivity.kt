package com.sameerasw.gumroadstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sameerasw.gumroadstats.ui.screens.PayoutsScreen
import com.sameerasw.gumroadstats.ui.screens.SettingsScreen
import com.sameerasw.gumroadstats.ui.theme.GumroadStatsTheme
import com.sameerasw.gumroadstats.viewmodel.PayoutsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            GumroadStatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSettings by remember { mutableStateOf(false) }

                    val viewModel: PayoutsViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return PayoutsViewModel(applicationContext) as T
                            }
                        }
                    )

                    if (showSettings) {
                        val updateInterval by viewModel.updateInterval.collectAsState()
                        SettingsScreen(
                            currentInterval = updateInterval,
                            onIntervalChange = { viewModel.setUpdateInterval(it) },
                            onClearToken = { viewModel.clearAccessToken() },
                            onNavigateBack = { showSettings = false }
                        )
                    } else {
                        PayoutsScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}
