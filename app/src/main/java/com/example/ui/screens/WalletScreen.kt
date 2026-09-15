package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.RpcNetwork
import com.example.data.remote.availableRpcNetworks
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyAccentCyan
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary
import com.example.ui.viewmodel.FaucetUiState
import com.example.util.QrCodeGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    state: FaucetUiState,
    onUpdateAddress: (String) -> Unit,
    onSelectNetwork: (RpcNetwork) -> Unit,
    onRefreshBalance: () -> Unit,
    onRefreshPrices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showEditAddressDialog by remember { mutableStateOf(false) }
    var tempAddressInput by remember { mutableStateOf(state.settings.walletAddress) }
    var showQrDialog by remember { mutableStateOf(false) }

    // Map network symbol to CoinGecko coin ID
    val coinGeckoKey = when (state.selectedRpcNetwork.coinSymbol) {
        "BNB" -> "binancecoin"
        "ETH" -> "ethereum"
        "POL" -> "polygon-ecosystem-token"
        else -> "binancecoin"
    }
    val currentUsdRate = state.coinGeckoPrices[coinGeckoKey] ?: state.coinGeckoPrices["binancecoin"]
    val estimatedUsd = if (currentUsdRate != null) state.settings.walletBalance * currentUsdRate else null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Wallet & On-Chain Balance",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Real Web3j RPC node queries & CoinGecko live market pricing",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // On-Chain RPC Balance Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D25)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HoneyGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .testTag("wallet_balance_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Network selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = HoneyGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EVM Network:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onRefreshBalance(); onRefreshPrices() },
                            modifier = Modifier.size(32.dp).testTag("refresh_balance_button")
                        ) {
                            if (state.isBalanceLoading || state.isPriceLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = HoneyGoldPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh On-chain Balance",
                                    tint = HoneyGoldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Networks list
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableRpcNetworks) { net ->
                            val isSelected = net.id == state.selectedRpcNetwork.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) HoneyGoldPrimary.copy(alpha = 0.2f) else Color(0xFF242733)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) HoneyGoldPrimary else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectNetwork(net) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = net.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ON-CHAIN RPC BALANCE (${state.selectedRpcNetwork.name.uppercase()})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%.4f".format(state.settings.walletBalance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = HoneyGoldPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.selectedRpcNetwork.coinSymbol,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmberSecondary
                        )
                    }

                    // Live CoinGecko Price Feed
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (currentUsdRate != null) HoneyMintTertiary else Color.Gray)
                        )
                        if (estimatedUsd != null && currentUsdRate != null) {
                            Text(
                                text = "≈ $%.2f USD  (1 %s = $%.2f via CoinGecko)".format(
                                    estimatedUsd,
                                    state.selectedRpcNetwork.coinSymbol,
                                    currentUsdRate
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = HoneyMintTertiary
                            )
                        } else if (state.isPriceLoading) {
                            Text(
                                text = "Loading CoinGecko market price...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "CoinGecko price feed active",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Balance error banner if any
                    if (state.balanceErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF331F1F))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color(0xFFFF6E6E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.balanceErrorMessage,
                                fontSize = 11.sp,
                                color = Color(0xFFFF8E8E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons: Refresh RPC & View QR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onRefreshBalance()
                                onRefreshPrices()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("query_rpc_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "RPC Query",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Query RPC", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showQrDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("receive_qr_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Receive QR",
                                tint = HoneyGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Receive QR", color = HoneyGoldLight)
                        }
                    }
                }
            }
        }

        // Wallet Address Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (state.settings.walletAddress.isNotBlank()) HoneyAccentCyan.copy(alpha = 0.5f) else Color(0xFF2E323E),
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("address_settings_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recipient Wallet Address",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Rewards are automatically sent to your personal address",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = {
                            tempAddressInput = state.settings.walletAddress
                            showEditAddressDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Address",
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.settings.walletAddress.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF101217))
                                .clickable {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Wallet Address", state.settings.walletAddress)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Address copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.settings.walletAddress,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Address",
                                    tint = HoneyGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF222530))
                                .clickable {
                                    tempAddressInput = ""
                                    showEditAddressDialog = true
                                }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Click to set your public EVM address (0x...)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = HoneyGoldPrimary
                            )
                        }
                    }
                }
            }
        }

        // Faucet Claim / Transaction Records
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Faucet Reward History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.transactions.size} claims",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No faucet claims recorded yet",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(state.transactions) { tx ->
                val dateStr = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                    .format(Date(tx.timestamp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_item_${tx.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(HoneyMintTertiary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SouthWest,
                                    contentDescription = "Reward",
                                    tint = HoneyMintTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Faucet Drop Reward",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+%.4f BEE".format(tx.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = HoneyMintTertiary
                            )
                            Text(
                                text = tx.status.ifBlank { "CLAIMED" },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HoneyMintTertiary
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Edit Address Dialog
    if (showEditAddressDialog) {
        AlertDialog(
            onDismissRequest = { showEditAddressDialog = false },
            title = { Text("Set Wallet Address", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter your personal external EVM address (BNB Smart Chain, Ethereum, Polygon):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempAddressInput,
                        onValueChange = { tempAddressInput = it },
                        label = { Text("Address (0x...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = tempAddressInput.trim()
                        if (clean.startsWith("0x") && clean.length == 42) {
                            onUpdateAddress(clean)
                            showEditAddressDialog = false
                            Toast.makeText(context, "Wallet address updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Must be a valid 42-character 0x EVM address", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                ) {
                    Text("Save Address", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ZXing QR Code Dialog
    if (showQrDialog) {
        val qrBitmap = remember(state.settings.walletAddress) {
            QrCodeGenerator.generateQrBitmap(state.settings.walletAddress, 512)
        }

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Receive Address QR Code", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.settings.walletAddress.isNotBlank()) {
                        if (qrBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Wallet Address QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Text(
                                text = "Unable to generate QR code bitmap",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = state.settings.walletAddress,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "No wallet address entered yet. Please enter your address above to view your receive QR code.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                if (state.settings.walletAddress.isNotBlank()) {
                    Button(
                        onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Wallet Address", state.settings.walletAddress)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Address copied!", Toast.LENGTH_SHORT).show()
                            showQrDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                    ) {
                        Text("Copy Address", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
