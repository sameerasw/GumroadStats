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
import com.sameerasw.gumroadstats.ui.screens.MainScreen
import com.sameerasw.gumroadstats.ui.screens.PayoutsScreen
import com.sameerasw.gumroadstats.ui.theme.GumroadStatsTheme
import com.sameerasw.gumroadstats.viewmodel.PayoutsViewModel
import com.sameerasw.gumroadstats.viewmodel.SalesViewModel
import com.sameerasw.gumroadstats.viewmodel.ProductsViewModel

import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            GumroadStatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val payoutsViewModel: PayoutsViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return PayoutsViewModel(application) as T
                            }
                        }
                    )

                    val salesViewModel: SalesViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return SalesViewModel(application) as T
                            }
                        }
                    )

                    val productsViewModel: ProductsViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return ProductsViewModel(application) as T
                            }
                        }
                    )

                    MainScreen(
                        payoutsViewModel = payoutsViewModel,
                        salesViewModel = salesViewModel,
                        productsViewModel = productsViewModel,
                        onNavigateToSettings = {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}
