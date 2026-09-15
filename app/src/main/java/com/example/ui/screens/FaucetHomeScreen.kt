package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
fun FaucetHomeScreen(
    state: FaucetUiState,
    onNavigateToCaptcha: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = state.timeRemainingMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)

    val maxIntervalMs = state.settings.reminderIntervalMinutes * 60 * 1000L
    val progress = if (maxIntervalMs > 0L) {
        1f - (state.timeRemainingMs.toFloat() / maxIntervalMs.toFloat()).coerceIn(0f, 1f)
    } else 1f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Graphic Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D24)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("hero_banner_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_bee_hero_banner),
                        contentDescription = "Bee Faucet Hive Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD121318))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🐝", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BEE CRYPTO FAUCET",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp,
                                color = HoneyGoldLight
                            )
                        }
                        Text(
                            text = "Browserless instant claims • Automated Payouts",
                            fontSize = 12.sp,
                            color = Color(0xFFD0D4E4)
                        )
                    }
                }
            }
        }

        // Faucet Timer & Claim Box
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (state.isFaucetReady) HoneyMintTertiary else HoneyGoldPrimary.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("faucet_timer_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = CyberAmberSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.settings.claimStreak} Claim Streak",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberAmberSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (state.isFaucetReady) HoneyMintTertiary.copy(alpha = 0.2f)
                                    else Color(0xFF282B36)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.isFaucetReady) "READY NOW" else "COOLING DOWN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (state.isFaucetReady) HoneyMintTertiary else HoneyGoldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Circular Progress Dial
                    Box(
                        modifier = Modifier.size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF242732),
                            strokeWidth = 10.dp
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (state.isFaucetReady) HoneyMintTertiary else HoneyGoldPrimary,
                            strokeWidth = 10.dp
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (state.isFaucetReady) {
                                Text(text = "🍯", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "READY",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = HoneyMintTertiary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = HoneyGoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = timeFormatted,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Next drop in",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Estimated Reward: 0.0050 ~ 0.0125 BEE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HoneyGoldLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Claim CTA
                    Button(
                        onClick = onNavigateToCaptcha,
                        enabled = state.isFaucetReady,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("claim_crypto_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HoneyGoldPrimary,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF262832),
                            disabledContentColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (state.isFaucetReady) "Solve Captcha & Claim BEE" else "Faucet Charging ($timeFormatted)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Go",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Test Helper
                    if (!state.isFaucetReady) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onResetTimer() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Test fast forward",
                                tint = HoneyAccentCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Fast-forward timer (Test Mode)",
                                fontSize = 11.sp,
                                color = HoneyAccentCyan
                            )
                        }
                    }
                }
            }
        }

        // Quick Wallet Stats Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWallet() }
                    .testTag("home_wallet_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INTEGRATED WALLET BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.4f".format(state.settings.walletBalance),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = HoneyGoldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BEE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberAmberSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(≈ $%.2f)".format(state.settings.walletBalance * 14.20),
                                fontSize = 12.sp,
                                color = HoneyMintTertiary
                            )
                        }

                        // Auto-withdrawal status line
                        if (state.settings.autoWithdrawalEnabled) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚡ Auto-Withdrawal Active (Threshold: %.4f BEE)".format(state.settings.autoWithdrawalThreshold),
                                fontSize = 11.sp,
                                color = HoneyAccentCyan
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = HoneyGoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Recent Claims Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Claims",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total: %.4f BEE".format(state.settings.totalClaimed),
                    fontSize = 12.sp,
                    color = HoneyGoldLight
                )
            }
        }

        if (state.claims.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🍯", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No claims yet! Solve your first captcha above.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.claims.take(5)) { claim ->
                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    .format(Date(claim.timestamp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_item_${claim.id}")
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🐝", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = claim.captchaType,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${dateStr} • ${claim.txHash.take(8)}...",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "+%.4f BEE".format(claim.amountBee),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HoneyMintTertiary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
