package com.sameerasw.gumroadstats.ui.components.sheets

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sameerasw.gumroadstats.data.model.Sale
import com.sameerasw.gumroadstats.ui.components.RoundedCardContainer
import androidx.compose.ui.res.stringResource
import com.sameerasw.gumroadstats.R
import com.sameerasw.gumroadstats.utils.formatDate
import com.sameerasw.gumroadstats.viewmodel.SaleDetailsState

@Composable
fun SaleDetailsSheet(
    detailsState: SaleDetailsState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.sale_details),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        when (detailsState) {
            is SaleDetailsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SaleDetailsState.Error -> {
                Text(
                    text = detailsState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is SaleDetailsState.Success -> {
                SaleDetailsContent(sale = detailsState.sale)
            }
            else -> {}
        }
    }
}

@Composable
private fun SaleDetailsContent(sale: Sale) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    RoundedCardContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        spacing = 2.dp
    ) {
        // Product Details
        DetailCard(
            title = stringResource(R.string.sale_product),
            value = sale.productName
        )
        
        // Amount Details
        DetailCard(
            title = stringResource(R.string.sale_amount),
            value = sale.formattedTotalPrice,
            valueColor = MaterialTheme.colorScheme.primary
        )

        // Customer Details
        DetailCard(
            title = stringResource(R.string.sale_customer_email),
            value = sale.email
        )

        // Date Details
        DetailCard(
            title = stringResource(R.string.sale_date),
            value = formatDate(sale.createdAt, is24Hour)
        )
        
        // Order ID if available
        DetailCard(
            title = stringResource(R.string.sale_order_id),
            value = sale.orderId.toString()
        )
        
        // Gumroad Fee
        DetailCard(
            title = stringResource(R.string.sale_gumroad_fee),
            value = "${sale.gumroadFee} cents" // Could format this better if currency symbol logic was robust
        )
        
        // Refunded Status
        if (sale.refunded) {
             DetailCard(
                title = stringResource(R.string.payout_status),
                value = stringResource(R.string.sale_status_refunded),
                valueColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor
            )
        }
    }
}
