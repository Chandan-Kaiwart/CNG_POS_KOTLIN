package com.apc.demo_pos.payment


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    saleData: SaleData,
    paymentMode: String,
    transactionId: String?,
    onBackToHome: () -> Unit
) {
    var showPrintPrompt by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("RIICO Station", color = Color.White)
                        Text(
                            "Transaction Complete",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E3A8A)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Receipt Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Final Receipt",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Surface(
                            color = Color(0xFF10B981),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "✓ Complete",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Transaction details
                    InfoRow("Order ID:", saleData.getTransactionId())

                    if (transactionId != null && transactionId != saleData.getTransactionId()) {
                        InfoRow(
                            "PhonePe Transaction ID:",
                            transactionId,
                            Color(0xFF5f259f)
                        )
                    }

                    InfoRow("Payment Mode:", paymentMode)
                    InfoRow("Date & Time:", formatDateTime(saleData.SaleDate ?: saleData.timestamp))

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    InfoRow("Dispenser:", saleData.getDispenserId())
                    InfoRow("Nozzle:", saleData.getNozzleId())
                    InfoRow("Fuel Type:", saleData.fuel_type ?: "CNG", Color(0xFF10B981))

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    InfoRow("Quantity (kg):", "${String.format("%.2f", saleData.getQuantity())} kg")
                    InfoRow("Price per kg:", "₹${String.format("%.2f", saleData.getPricePerUnit())}")

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Total
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Paid:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                "₹${String.format("%.2f", saleData.getTotalAmount())}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            // Print Prompt or Completion
            if (showPrintPrompt) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Print Customer Copy?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    // TODO: Implement print functionality
                                    showPrintPrompt = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981)
                                )
                            ) {
                                Text("Generate PDF")
                            }

                            OutlinedButton(
                                onClick = { showPrintPrompt = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Skip")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🎉 Transaction Complete!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Thank you for choosing clean CNG energy",
                            fontSize = 16.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = onBackToHome,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6)
                            )
                        ) {
                            Text("Back to Home", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}