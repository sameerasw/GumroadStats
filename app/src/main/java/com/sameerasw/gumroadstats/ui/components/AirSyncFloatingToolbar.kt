package com.sameerasw.gumroadstats.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.ui.model.AirSyncTab
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AirSyncFloatingToolbar(
    modifier: Modifier = Modifier,
    currentPage: Int = 0,
    tabs: List<AirSyncTab> = emptyList(),
    onTabSelected: (Int) -> Unit = {},
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    scrollBehavior: FloatingToolbarScrollBehavior,
    floatingActionButton: (@Composable () -> Unit)? = null
) {
    HorizontalFloatingToolbar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp),
        expanded = true,
        floatingActionButton = floatingActionButton ?: {},
        scrollBehavior = scrollBehavior,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
            toolbarContentColor = MaterialTheme.colorScheme.onSurface,
            toolbarContainerColor = MaterialTheme.colorScheme.primary,
        ),
        content = {
            if (onBackClick != null) {
                IconButton(
                    onClick = {
                        onBackClick()
                    },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (title != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            } else {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = currentPage == index

                    IconButton(
                        onClick = {
                            onTabSelected(index)
                        },
                        modifier = Modifier
                            .size(48.dp),
                        colors = if (isSelected) {
                            IconButtonDefaults.filledIconButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        } else {
                            IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.background,
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Spacing between buttons
                    if (index < tabs.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    )
}
