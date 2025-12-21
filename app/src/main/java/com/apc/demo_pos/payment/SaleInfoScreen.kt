package com.apc.demo_pos.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SaleInfoScreen(
//    saleData: SaleData,
//    onBackPressed: () -> Unit,
//    onPaymentInitiated: (String, String, String) -> Unit,
//    viewModel: SaleInfoViewModel = viewModel()
//) {
//    val paymentState by viewModel.paymentState.collectAsState()
//
//    LaunchedEffect(paymentState) {
//        when (val state = paymentState) {
//            is PaymentState.Success -> {
//                onPaymentInitiated(state.paymentUrl, state.transactionId, state.authToken)
//                viewModel.resetPaymentState()
//            }
//            is PaymentState.Error -> {
//                // Show error (handled in UI)
//            }
//            else -> {}
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Column {
//                        Text("RIICO Station")
//                        Text(
//                            text = if (saleData.isManualTransaction)
//                                "Manual Transaction" else "Sale Information",
//                            fontSize = 14.sp,
//                            color = Color.White.copy(alpha = 0.7f)
//                        )
//                    }
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackPressed) {
//                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color(0xFF1E3A8A),
//                    titleContentColor = Color.White
//                )
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .verticalScroll(rememberScrollState())
//        ) {
//            // Receipt Card
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                elevation = CardDefaults.cardElevation(4.dp)
//            ) {
//                Column(modifier = Modifier.padding(24.dp)) {
//                    // Header
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            "Transaction Preview",
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF1E3A8A)
//                        )
//                        Surface(
//                            color = Color(0xFF10B981),
//                            shape = MaterialTheme.shapes.medium
//                        ) {
//                            Text(
//                                "Ready",
//                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                                color = Color.White,
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
//                    }
//
//                    Divider(modifier = Modifier.padding(vertical = 16.dp))
//
//                    // Transaction Type
//                    InfoRow(
//                        label = "Transaction Type:",
//                        value = if (saleData.isManualTransaction) "Manual Entry" else "Automatic",
//                        valueColor = if (saleData.isManualTransaction)
//                            Color(0xFFF59E0B) else Color(0xFF10B981)
//                    )
//
//                    // Order ID
//                    InfoRow("Order ID:", saleData.getTransactionId())
//
//                    // Date & Time
//                    InfoRow("Date & Time:", formatDateTime(saleData.SaleDate ?: saleData.timestamp))
//
//                    Divider(modifier = Modifier.padding(vertical = 16.dp))
//
//                    // Dispenser & Nozzle
//                    InfoRow("Dispenser:", saleData.getDispenserId())
//                    InfoRow("Nozzle:", saleData.getNozzleId())
//                    InfoRow("Fuel Type:", saleData.fuel_type ?: "CNG", Color(0xFF10B981))
//
//                    Divider(modifier = Modifier.padding(vertical = 16.dp))
//
//                    // Quantity & Price
//                    InfoRow("Quantity (kg):", "${String.format("%.2f", saleData.getQuantity())} kg")
//                    InfoRow("Price per kg:", "₹${String.format("%.2f", saleData.getPricePerUnit())}")
//
//                    Divider(modifier = Modifier.padding(vertical = 16.dp))
//
//                    // Total
//                    Surface(
//                        color = Color(0xFFF0FDF4),
//                        shape = MaterialTheme.shapes.medium
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                "Total Amount:",
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF1E3A8A)
//                            )
//                            Text(
//                                "₹${String.format("%.2f", saleData.getTotalAmount())}",
//                                fontSize = 24.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF10B981)
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            // Payment Button
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                OutlinedButton(
//                    onClick = onBackPressed,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("← Back")
//                }
//
//                Button(
//                    onClick = { viewModel.initiatePayment(saleData) },
//                    modifier = Modifier.weight(2f),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF5f259f)
//                    ),
//                    enabled = paymentState !is PaymentState.Loading
//                ) {
//                    if (paymentState is PaymentState.Loading) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(20.dp),
//                            color = Color.White
//                        )
//                        Spacer(Modifier.width(8.dp))
//                    }
//                    Text(
//                        if (paymentState is PaymentState.Loading) "Processing..." else "Pay with PhonePe",
//                        fontSize = 16.sp
//                    )
//                }
//            }
//
//            // Error Message
//            if (paymentState is PaymentState.Error) {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp),
//                    color = Color(0xFFFEF2F2),
//                    shape = MaterialTheme.shapes.medium
//                ) {
//                    Text(
//                        (paymentState as PaymentState.Error).message,
//                        modifier = Modifier.padding(16.dp),
//                        color = Color(0xFFDC2626)
//                    )
//                }
//            }
//        }
//    }
//}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1F2937)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 16.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

fun formatDateTime(dateString: String?): String {
    if (dateString == null) {
        val now = Date()
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
        return formatter.format(now)
    }

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

