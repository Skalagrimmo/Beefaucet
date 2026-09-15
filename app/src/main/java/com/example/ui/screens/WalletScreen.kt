package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WalletTransactionEntity
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyAccentCyan
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary
import com.example.ui.viewmodel.FaucetUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    state: FaucetUiState,
    onToggleAutoWithdrawal: (Boolean) -> Unit,
    onUpdateThreshold: (Double) -> Unit,
    onUpdateDestination: (String) -> Unit,
    onManualWithdraw: (Double, String, (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showEditDestinationDialog by remember { mutableStateOf(false) }
    var tempDestination by remember { mutableStateOf(state.settings.autoWithdrawalDestination) }

    var showManualWithdrawDialog by remember { mutableStateOf(false) }
    var manualAmountText by remember { mutableStateOf("") }
    var manualDestText by remember { mutableStateOf("") }

    var showQrDialog by remember { mutableStateOf(false) }

    val progressToThreshold = if (state.settings.autoWithdrawalThreshold > 0.0) {
        (state.settings.walletBalance / state.settings.autoWithdrawalThreshold).toFloat().coerceIn(0f, 1f)
    } else 1f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Secure Integrated Wallet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Automated payouts and self-custodial key vault",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Primary Balance Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security",
                                tint = HoneyMintTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Keystore Encrypted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HoneyMintTertiary
                            )
                        }

                        IconButton(
                            onClick = { showQrDialog = true },
                            modifier = Modifier.size(32.dp).testTag("view_qr_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "View QR",
                                tint = HoneyGoldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "TOTAL WALLET BALANCE",
                        fontSize = 11.sp,
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
                            text = "BEE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmberSecondary
                        )
                    }

                    Text(
                        text = "≈ $%.2f USD  •  1 BEE = $14.20".format(state.settings.walletBalance * 14.20),
                        fontSize = 13.sp,
                        color = HoneyMintTertiary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Address snippet
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
                                Toast.makeText(context, "Wallet Address copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.settings.walletAddress.length > 20)
                                    "${state.settings.walletAddress.take(12)}...${state.settings.walletAddress.takeLast(8)}"
                                else state.settings.walletAddress,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                manualAmountText = "%.4f".format(state.settings.walletBalance)
                                manualDestText = state.settings.autoWithdrawalDestination
                                showManualWithdrawDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("manual_withdraw_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NorthEast,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showQrDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SouthWest,
                                contentDescription = "Receive",
                                tint = HoneyGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Receive", color = HoneyGoldLight)
                        }
                    }
                }
            }
        }

        // Automated Withdrawals Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (state.settings.autoWithdrawalEnabled) HoneyAccentCyan.copy(alpha = 0.5f) else Color(0xFF2E323E),
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("auto_withdrawal_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ Automated Withdrawals",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Automatically payouts once threshold is reached",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = state.settings.autoWithdrawalEnabled,
                            onCheckedChange = { onToggleAutoWithdrawal(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = HoneyAccentCyan
                            ),
                            modifier = Modifier.testTag("auto_withdrawal_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress to Threshold
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Auto-Payout Threshold",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.4f / %.4f BEE (%d%%)".format(
                                state.settings.walletBalance,
                                state.settings.autoWithdrawalThreshold,
                                (progressToThreshold * 100).toInt()
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (progressToThreshold >= 1f) HoneyMintTertiary else HoneyAccentCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressToThreshold },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progressToThreshold >= 1f) HoneyMintTertiary else HoneyAccentCyan,
                        trackColor = Color(0xFF262833)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick threshold chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.025, 0.050, 0.100).forEach { th ->
                            val isSelected = state.settings.autoWithdrawalThreshold == th
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) HoneyAccentCyan.copy(alpha = 0.2f) else Color(0xFF22242D)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) HoneyAccentCyan else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onUpdateThreshold(th) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "%.3f BEE".format(th),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) HoneyAccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF262934))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Destination Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Payout Destination Address",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (state.settings.autoWithdrawalDestination.length > 20)
                                    "${state.settings.autoWithdrawalDestination.take(10)}...${state.settings.autoWithdrawalDestination.takeLast(6)}"
                                else state.settings.autoWithdrawalDestination,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = {
                            tempDestination = state.settings.autoWithdrawalDestination
                            showEditDestinationDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit destination",
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Transaction History Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.transactions.size} records",
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
                        text = "No wallet transactions yet",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(state.transactions) { tx ->
                val dateStr = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                    .format(Date(tx.timestamp))

                val isCredit = tx.type == "FAUCET_CLAIM"
                val isAuto = tx.type == "AUTO_WITHDRAWAL"

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
                                    .background(
                                        when {
                                            isCredit -> HoneyMintTertiary.copy(alpha = 0.15f)
                                            isAuto -> HoneyAccentCyan.copy(alpha = 0.15f)
                                            else -> CyberAmberSecondary.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isCredit -> Icons.Default.SouthWest
                                        isAuto -> Icons.Default.SyncAlt
                                        else -> Icons.Default.NorthEast
                                    },
                                    contentDescription = tx.type,
                                    tint = when {
                                        isCredit -> HoneyMintTertiary
                                        isAuto -> HoneyAccentCyan
                                        else -> CyberAmberSecondary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (tx.type) {
                                        "FAUCET_CLAIM" -> "Faucet Drop Claim"
                                        "AUTO_WITHDRAWAL" -> "Automated Payout"
                                        "MANUAL_WITHDRAWAL" -> "Manual Withdrawal"
                                        else -> tx.type
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$dateStr • ${tx.txHash.take(8)}...",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%s%.4f BEE".format(if (isCredit) "+" else "-", tx.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isCredit) HoneyMintTertiary else Color(0xFFFF7043)
                            )
                            Text(
                                text = tx.status,
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

    // Edit Destination Dialog
    if (showEditDestinationDialog) {
        AlertDialog(
            onDismissRequest = { showEditDestinationDialog = false },
            title = { Text("Set Payout Address", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Enter your personal external BEP-20 or ERC-20 crypto wallet address for automated withdrawals:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempDestination,
                        onValueChange = { tempDestination = it },
                        label = { Text("Destination Address (0x...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempDestination.startsWith("0x") && tempDestination.length >= 10) {
                            onUpdateDestination(tempDestination)
                            showEditDestinationDialog = false
                            Toast.makeText(context, "Payout address saved!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid 0x address", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                ) {
                    Text("Save Address", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDestinationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Manual Withdraw Dialog
    if (showManualWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showManualWithdrawDialog = false },
            title = { Text("Manual Withdrawal", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Available Balance: %.4f BEE".format(state.settings.walletBalance),
                        fontSize = 12.sp,
                        color = HoneyGoldLight
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualAmountText,
                        onValueChange = { manualAmountText = it },
                        label = { Text("Amount (BEE)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualDestText,
                        onValueChange = { manualDestText = it },
                        label = { Text("Destination Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = manualAmountText.toDoubleOrNull() ?: 0.0
                        onManualWithdraw(amount, manualDestText) { success, msg ->
                            if (success) {
                                showManualWithdrawDialog = false
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                ) {
                    Text("Send Payout", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualWithdrawDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // QR Code Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Receive BEE Tokens", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Stylized QR pattern
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val step = size.width / 15f
                            for (r in 0 until 15) {
                                for (c in 0 until 15) {
                                    val isCorner = (r < 4 && c < 4) || (r < 4 && c > 10) || (r > 10 && c < 4)
                                    val shouldFill = isCorner || ((r * 7 + c * 13 + 5) % 3 == 0)
                                    if (shouldFill) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(c * step, r * step),
                                            size = Size(step * 0.9f, step * 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = state.settings.walletAddress,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
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
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
