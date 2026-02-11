package com.sameerasw.gumroadstats.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.data.preferences.UpdateInterval
import com.sameerasw.gumroadstats.ui.components.RoundedCardContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentInterval: UpdateInterval,
    startDate: Long?,
    onIntervalChange: (UpdateInterval) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onClearStartDate: () -> Unit,
    onClearToken: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showIntervalMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate ?: System.currentTimeMillis(),
        initialDisplayMode = DisplayMode.Picker
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        datePickerState.selectedDateMillis?.let { onStartDateChange(it) }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val versionName = try {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
    } catch (_: Exception) {
        "Unknown"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Auto-Update Settings
            RoundedCardContainer {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Update Interval",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showIntervalMenu = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(currentInterval.displayName)
                            }

                            DropdownMenu(
                                expanded = showIntervalMenu,
                                onDismissRequest = { showIntervalMenu = false }
                            ) {
                                UpdateInterval.entries.forEach { interval ->
                                    DropdownMenuItem(
                                        text = { Text(interval.displayName) },
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onIntervalChange(interval)
                                            showIntervalMenu = false
                                        },
                                        leadingIcon = if (interval == currentInterval) {
                                            { Icon(Icons.Default.Check, contentDescription = null) }
                                        } else null
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (currentInterval == UpdateInterval.NEVER) {
                                "Payouts will only refresh manually"
                            } else {
                                "Payouts will automatically refresh every ${currentInterval.displayName}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Start Date",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val dateText = remember(startDate) {
                            if (startDate == null) "All time"
                            else java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(startDate))
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDatePicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(dateText)
                        }

                        if (startDate != null) {
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onClearStartDate()
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Clear Date")
                            }
                        }

                        Text(
                            text = "Fetch payouts starting from this date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 0.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            

            // Account Settings
            RoundedCardContainer {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showClearDialog = true
                        },
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                         Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Clear Access Token",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Remove saved token and cached data",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                             Icon(
                                 painter = painterResource(id = R.drawable.rounded_expand_circle_right_24),
                                 contentDescription = null,
                                 tint = MaterialTheme.colorScheme.onErrorContainer
                             )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            // About Section
            RoundedCardContainer {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar),
                            contentDescription = "Developer Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Made with ❤️ by Sameera",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                    )
                                }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "GumroadStats",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Version $versionName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Track your Gumroad payouts and sales data directly from your Android device.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // GitHub button
                        Button(
                            onClick = {
                                val githubUrl = "https://github.com/sameerasw/GumroadStats"
                                val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View on GitHub")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // My website button
                        OutlinedButton(
                            onClick = {
                                val websiteUrl = "https://www.sameerasw.com"
                                val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Visit My Website")
                        }
                        
                         Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Developed by Sameera Wijerathna",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showClearDialog = false
            },
            title = { Text("Clear Access Token?") },
            text = { Text("This will remove your saved access token and cached data. You'll need to enter it again next time.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClearToken()
                        showClearDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showClearDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
