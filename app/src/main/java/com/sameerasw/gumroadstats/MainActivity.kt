package com.sameerasw.gumroadstats

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sameerasw.gumroadstats.ui.screens.PayoutsScreen
import com.sameerasw.gumroadstats.ui.theme.GumroadStatsTheme
import com.sameerasw.gumroadstats.viewmodel.PayoutsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable full edge-to-edge drawing for both status and navigation bars
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // On Android 10+ disable forced high-contrast nav bar, so app can draw beneath gesture bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            GumroadStatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: PayoutsViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return PayoutsViewModel(applicationContext) as T
                            }
                        }
                    )

                    PayoutsScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}
