package com.sameerasw.gumroadstats.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.data.model.Product
import com.sameerasw.gumroadstats.ui.components.RoundedCardContainer
import com.sameerasw.gumroadstats.viewmodel.ProductDetailsState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProductDetailsSheet(
    detailsState: ProductDetailsState,
    onToggleStatus: (Product) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showStatusConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.product_details),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        when (detailsState) {
            is ProductDetailsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is ProductDetailsState.Error -> {
                Text(
                    text = detailsState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is ProductDetailsState.Success -> {
                ProductDetailsContent(
                    product = detailsState.product,
                    onToggleStatusClick = { showStatusConfirmation = true },
                    onDeleteClick = { showDeleteConfirmation = true }
                )
            }
            else -> {}
        }
    }

    if (showStatusConfirmation) {
        val product = (detailsState as? ProductDetailsState.Success)?.product
        if (product != null) {
            val title = if (product.published) stringResource(R.string.disable_product) else stringResource(R.string.enable_product)
            val message = if (product.published) stringResource(R.string.hide_from_store) else stringResource(R.string.make_visible_in_store)
            
            AlertDialog(
                onDismissRequest = { showStatusConfirmation = false },
                title = { Text(title) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onToggleStatus(product)
                            showStatusConfirmation = false
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStatusConfirmation = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_product_title)) },
            text = { Text(stringResource(R.string.delete_product_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val product = (detailsState as? ProductDetailsState.Success)?.product
                        product?.let { onDelete(it.id) }
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ProductDetailsContent(
    product: Product,
    onToggleStatusClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Info Sections
        RoundedCardContainer(spacing = 2.dp) {
            DetailCard(title = stringResource(R.string.product_name), value = product.name)
            DetailCard(title = stringResource(R.string.product_price), value = product.formattedPrice)
            DetailCard(title = stringResource(R.string.product_currency), value = product.currency.uppercase())
            DetailCard(title = stringResource(R.string.product_short_url), value = product.shortUrl)
            if (!product.salesCount.isNullOrEmpty()) {
                DetailCard(title = stringResource(R.string.total_sales), value = product.salesCount)
            }
            if (!product.salesUsdCents.isNullOrEmpty()) {
                val usdVal = product.salesUsdCents.toDouble() / 100
                DetailCard(title = stringResource(R.string.total_revenue), value = "$${String.format("%.2f", usdVal)}")
            }
        }

        if (product.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            RoundedCardContainer {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (!product.tags.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.tags),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                product.tags.forEach { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(tag) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions 
        Text(
            text = stringResource(R.string.actions),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        RoundedCardContainer(spacing = 2.dp) {
            ActionCard(
                title = if (product.published) stringResource(R.string.disable_product) else stringResource(R.string.enable_product),
                subtitle = if (product.published) stringResource(R.string.hide_from_store) else stringResource(R.string.make_visible_in_store),
                icon = if (product.published) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                onClick = onToggleStatusClick,
                color = MaterialTheme.colorScheme.primaryContainer
            )
            ActionCard(
                title = stringResource(R.string.delete_product_title),
                subtitle = stringResource(R.string.permanently_remove_this_product),
                icon = Icons.Default.Delete,
                onClick = onDeleteClick,
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
