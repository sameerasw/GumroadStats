package com.sameerasw.gumroadstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        enableEdgeToEdge()
        setContent {
            GumroadStatsTheme {
                var showSettings by remember { mutableStateOf(false) }

                val viewModel: PayoutsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return PayoutsViewModel(applicationContext) as T
                        }
                    }
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showSettings) {
                        val updateInterval by viewModel.updateInterval.collectAsState()
                        SettingsScreen(
                            currentInterval = updateInterval,
                            onIntervalChange = { viewModel.setUpdateInterval(it) },
                            onClearToken = { viewModel.clearAccessToken() },
                            onNavigateBack = { showSettings = false },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        PayoutsScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { showSettings = true },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
