package com.sameerasw.gumroadstats

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sameerasw.gumroadstats.ui.screens.MainScreen
import com.sameerasw.gumroadstats.ui.screens.PayoutsScreen
import com.sameerasw.gumroadstats.ui.theme.GumroadStatsTheme
import com.sameerasw.gumroadstats.viewmodel.PayoutsViewModel
import com.sameerasw.gumroadstats.viewmodel.SalesViewModel
import com.sameerasw.gumroadstats.viewmodel.ProductsViewModel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.core.view.WindowCompat
import com.sameerasw.gumroadstats.utils.BiometricHelper
import com.sameerasw.gumroadstats.data.preferences.PreferencesManager
import com.sameerasw.gumroadstats.R

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val preferencesManager = PreferencesManager(this)

        setContent {
            val isBiometricEnabled by preferencesManager.isBiometricLockEnabled.collectAsState(initial = false)
            var authSuccessful by rememberSaveable { mutableStateOf(false) }

            fun performBiometricAuth() {
                BiometricHelper.authenticate(
                    activity = this@MainActivity,
                    title = getString(R.string.biometric_lock),
                    subtitle = getString(R.string.authenticate_to_unlock),
                    onSuccess = {
                        authSuccessful = true
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(this@MainActivity, error, android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }

            GumroadStatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLocked = isBiometricEnabled && !authSuccessful

                    AnimatedContent(
                        targetState = isLocked,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith 
                            fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 1.1f, animationSpec = tween(500))
                        },
                        label = "AuthTransition"
                    ) { targetLocked ->
                        if (targetLocked) {
                            // Locked Screen
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.rounded_lock_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.authenticate_to_unlock),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Button(
                                        onClick = { performBiometricAuth() },
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(stringResource(R.string.unlock))
                                    }
                                }

                                LaunchedEffect(Unit) {
                                    performBiometricAuth()
                                }
                            }
                        } else {
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
    }
}
