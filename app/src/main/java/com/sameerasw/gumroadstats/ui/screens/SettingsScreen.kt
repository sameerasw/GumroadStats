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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.res.stringResource
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.data.preferences.UpdateInterval
import com.sameerasw.gumroadstats.ui.components.RoundedCardContainer

import com.sameerasw.gumroadstats.ui.components.AirSyncFloatingToolbar
import com.sameerasw.gumroadstats.ui.components.sheets.AboutBottomSheet
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.sameerasw.gumroadstats.ui.modifiers.progressiveBlur
import com.sameerasw.gumroadstats.utils.HapticUtil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    currentInterval: UpdateInterval,
    startDate: Long?,
    groupByMonth: Boolean,
    onIntervalChange: (UpdateInterval) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onGroupByMonthChange: (Boolean) -> Unit,
    onClearStartDate: () -> Unit,
    onClearToken: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showIntervalMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    
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
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAboutSheet) {
        AboutBottomSheet(
            onDismissRequest = { showAboutSheet = false },
            onToggleDeveloperMode = {
                // Developer mode toggling logic
                Toast.makeText(context, "Developer options coming soon!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        topBar = { },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { padding ->
        val density = androidx.compose.ui.platform.LocalDensity.current
        val statusBarHeightPx = with(density) {
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx()
        }
        val bottomBlurHeightPx = with(density) { 150.dp.toPx() }

        val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
            exitDirection = FloatingToolbarExitDirection.Bottom
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .progressiveBlur(
                    blurRadius = 40f,
                    height = statusBarHeightPx * 1.2f,
                    direction = com.sameerasw.gumroadstats.ui.modifiers.BlurDirection.TOP
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .progressiveBlur(
                        blurRadius = 40f,
                        height = bottomBlurHeightPx,
                        direction = com.sameerasw.gumroadstats.ui.modifiers.BlurDirection.BOTTOM
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Auto-Update and Display Settings
                RoundedCardContainer {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
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
                                        text = stringResource(R.string.group_by_month),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.group_by_month_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = groupByMonth,
                                    onCheckedChange = { 
                                        HapticUtil.performClick(haptic)
                                        onGroupByMonthChange(it) 
                                    }
                                )
                            }
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
                                text = stringResource(R.string.update_interval),
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
                                    stringResource(R.string.update_interval_description_never)
                                } else {
                                    stringResource(R.string.update_interval_description_format, currentInterval.displayName)
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
                                text = stringResource(R.string.start_date),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val dateText = remember(startDate) {
                                if (startDate == null) context.getString(R.string.all_time)
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
                                    Text(stringResource(R.string.clear_date))
                                }
                            }

                            Text(
                                text = stringResource(R.string.fetch_payouts_starting_from),
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
                                        text = stringResource(R.string.clear_access_token),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = stringResource(R.string.remove_saved_token_and_cached_data),
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

                Spacer(modifier = Modifier.height(32.dp))

                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                Spacer(modifier = Modifier.height(150.dp))
            }

            AirSyncFloatingToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1f),
                onBackClick = {
                    HapticUtil.performClick(haptic)
                    onNavigateBack()
                },
                title = stringResource(R.string.settings_title),
                scrollBehavior = scrollBehavior,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            HapticUtil.performClick(haptic)
                            showAboutSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = MaterialTheme.shapes.large,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About"
                        )
                    }
                }
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showClearDialog = false
            },
            title = { Text(stringResource(R.string.clear_access_token_title)) },
            text = { Text(stringResource(R.string.clear_access_token_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClearToken()
                        showClearDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
