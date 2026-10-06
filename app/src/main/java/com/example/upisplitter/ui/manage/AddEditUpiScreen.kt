package com.example.upisplitter.ui.manage

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUpiScreen(
    viewModel: ManageUpiViewModel,
    editingId: Long = -1L,
    initialMerchantName: String = "",
    initialUpiId: String = "",
    onScanCameraClick: (Long, String, String) -> Unit,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var merchantNameInput by remember(initialMerchantName) { mutableStateOf(initialMerchantName) }
    var upiIdInput by remember(initialUpiId) { mutableStateOf(initialUpiId) }

    LaunchedEffect(initialMerchantName, initialUpiId) {
        if (initialMerchantName.isNotBlank()) {
            merchantNameInput = initialMerchantName
        }
        if (initialUpiId.isNotBlank()) {
            upiIdInput = initialUpiId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingId == -1L) "Add UPI ID" else "Edit UPI ID", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Scan or Select QR Button with QrCodeScanner Icon
                OutlinedButton(
                    onClick = { onScanCameraClick(editingId, merchantNameInput, upiIdInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Tap to Scan or Select QR",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                OutlinedTextField(
                    value = merchantNameInput,
                    onValueChange = { merchantNameInput = it },
                    label = { Text("Merchant Name") },
                    placeholder = { Text("e.g. Tea Stall") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = upiIdInput,
                    onValueChange = { upiIdInput = it },
                    label = { Text("UPI ID") },
                    placeholder = { Text("e.g. merchant@upi") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    val name = merchantNameInput.trim()
                    val upi = upiIdInput.trim()
                    if (name.isNotBlank() && upi.contains("@")) {
                        scope.launch {
                            if (viewModel.isDuplicate(upi, editingId)) {
                                Toast.makeText(context, "This UPI ID is already saved!", Toast.LENGTH_SHORT).show()
                            } else {
                                val actualId = if (editingId == -1L) 0L else editingId
                                viewModel.saveUpiId(actualId, name, upi)
                                Toast.makeText(context, "UPI ID saved successfully!", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter a valid merchant name and UPI ID", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (editingId == -1L) "SAVE UPI ID" else "UPDATE UPI ID",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
