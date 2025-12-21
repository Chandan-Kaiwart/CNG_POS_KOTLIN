package com.apc.demo_pos.payment


import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.web.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayScreen(
    paymentUrl: String,
    transactionId: String,
    authToken: String,
    saleData: SaleData,
    onPaymentComplete: (Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val webViewState = rememberWebViewState(url = paymentUrl)
    var showCancelDialog by remember { mutableStateOf(false) }
    var isPaymentDetected by remember { mutableStateOf(false) }

    // Monitor URL changes
    LaunchedEffect(webViewState.lastLoadedUrl) {
        val url = webViewState.lastLoadedUrl ?: ""

        if (url.contains("about:blank") ||
            url.contains("success") ||
            url.contains("failure") ||
            url.contains("redirect-callback")) {

            if (!isPaymentDetected) {
                isPaymentDetected = true
                // Wait 3 seconds then check status
                delay(3000)
                onPaymentComplete(true) // Let API determine actual status
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Payment") },
            text = { Text("Are you sure you want to cancel this payment?") },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PhonePe Payment", color = Color.White) },
                actions = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Default.Close, "Cancel", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5f259f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                onCreated = { webView ->
                    webView.settings.javaScriptEnabled = true
                    webView.settings.domStorageEnabled = true
                }
            )

            // Loading indicator
            if (webViewState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF5f259f))
                }
            }
        }
    }
}
