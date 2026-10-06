package com.example.upisplitter.ui.details

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.upisplitter.domain.QrCodeGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val transactionWithChunks by viewModel.transactionState.collectAsState()
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Details", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingVals ->
        val txWithChunks = transactionWithChunks
        if (txWithChunks == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val tx = txWithChunks.transaction
            val chunks = txWithChunks.chunks
            val dateStr = dateFormatter.format(Date(tx.createdAt))
            val timeStr = timeFormatter.format(Date(tx.createdAt))

            val completedSum = chunks.filter { it.isCompleted }.sumOf { it.amount }
            val pendingSum = tx.originalAmount - completedSum
            val completedCount = chunks.count { it.isCompleted }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingVals)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow("Merchant:", tx.merchantName)
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailRow("UPI ID:", tx.merchantUpiId)
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailRow("Date:", dateStr)
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailRow("Time:", timeStr)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Total Amount:", "₹${tx.originalAmount}", isBold = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailRow("Completed:", "₹$completedSum")
                            Spacer(modifier = Modifier.height(6.dp))
                            DetailRow("Pending Amount:", "₹$pendingSum", isBold = true)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Breakdown & QRs ($completedCount/${chunks.size} completed)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chunks) { chunk ->
                            val isCompleted = chunk.isCompleted
                            val upiUri = "upi://pay?pa=${tx.merchantUpiId}&pn=${Uri.encode(tx.merchantName)}&am=${chunk.amount}&cu=INR"
                            val qrBitmap = remember(chunk.amount) {
                                QrCodeGenerator.generatePureQrBitmap(upiUri, 500)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCompleted) Color(0xFFF1F8F5) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 1.dp else 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "QR #${chunk.sequenceNumber}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                        )
                                        if (isCompleted) {
                                            Surface(
                                                color = Color(0xFFC8E6C9),
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text(
                                                    text = " COMPLETED ✓ ",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF1B5E20),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (!isCompleted && qrBitmap != null) {
                                        Surface(
                                            shape = MaterialTheme.shapes.medium,
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Image(
                                                bitmap = qrBitmap.asImageBitmap(),
                                                contentDescription = "QR #${chunk.sequenceNumber}",
                                                modifier = Modifier
                                                    .size(240.dp)
                                                    .padding(6.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (!isCompleted) {
                                        Text(
                                            text = "Pending Amount: ₹${chunk.amount}",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Completed Amount:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF2E7D32)
                                            )
                                            Text(
                                                text = "₹${chunk.amount}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (!isCompleted) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val brandedBmp = QrCodeGenerator.generateBrandedQrBitmap(
                                                        content = upiUri,
                                                        merchantName = tx.merchantName,
                                                        amount = chunk.amount,
                                                        sequenceNumber = chunk.sequenceNumber,
                                                        qrSize = 512
                                                    )
                                                    if (brandedBmp != null) {
                                                        val success = QrCodeGenerator.saveBitmapToGallery(context, brandedBmp)
                                                        if (success) {
                                                            Toast.makeText(context, "QR #${chunk.sequenceNumber} saved to Gallery!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to save QR", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp),
                                                shape = MaterialTheme.shapes.medium
                                            ) {
                                                Text("Save QR", fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { viewModel.markChunkComplete(chunk.id, true) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp),
                                                shape = MaterialTheme.shapes.medium,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text("Complete", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("BACK", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}
