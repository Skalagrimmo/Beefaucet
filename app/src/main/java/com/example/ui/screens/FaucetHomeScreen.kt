package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyAccentCyan
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary
import com.example.ui.viewmodel.FaucetItem
import com.example.ui.viewmodel.FaucetUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FaucetHomeScreen(
    state: FaucetUiState,
    onSelectFaucet: (FaucetItem) -> Unit,
    onResetCooldown: (String) -> Unit,
    onResetAllCooldowns: () -> Unit,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Graphic Banner
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D24)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
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
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🐝", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BEEFAUCET.ORG HUB",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp,
                                color = HoneyGoldLight
                            )
                        }
                        Text(
                            text = "Real multi-coin faucet • 60s cooldown • 10 claims/day",
                            fontSize = 11.sp,
                            color = Color(0xFFD0D4E4)
                        )
                    }
                }
            }
        }

        // Section Title & Test Reset Button
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Faucet Claim Grid",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any coin to open real site in-app WebView",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onResetAllCooldowns() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Cooldowns",
                        tint = HoneyAccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset Timers",
                        fontSize = 11.sp,
                        color = HoneyAccentCyan
                    )
                }
            }
        }

        // Faucet Grid Items
        items(state.faucets, key = { it.id }) { faucet ->
            FaucetGridCard(
                faucet = faucet,
                onClick = { onSelectFaucet(faucet) },
                onResetCooldown = { onResetCooldown(faucet.id) }
            )
        }

        // Bottom Claim Activity Summary
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Claim Activity Logs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.claims.size} recorded",
                            fontSize = 11.sp,
                            color = HoneyGoldLight
                        )
                    }

                    if (state.claims.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No claims recorded yet. Select any faucet above to claim on beefaucet.org!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        state.claims.take(3).forEach { claim ->
                            val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                .format(Date(claim.timestamp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "✔ ${claim.captchaType}",
                                    fontSize = 11.sp,
                                    color = HoneyMintTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaucetGridCard(
    faucet: FaucetItem,
    onClick: () -> Unit,
    onResetCooldown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressToLimit = (faucet.dailyClaims.toFloat() / faucet.maxDailyClaims.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171922)
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                when {
                    faucet.isLimitReached -> Color(0xFF282B36)
                    faucet.isReady -> HoneyMintTertiary.copy(alpha = 0.6f)
                    else -> HoneyGoldPrimary.copy(alpha = 0.4f)
                },
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("faucet_card_${faucet.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Coin Icon & Symbol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = faucet.coinIcon, fontSize = 18.sp)
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                faucet.isLimitReached -> Color(0xFF252732)
                                faucet.isReady -> HoneyMintTertiary.copy(alpha = 0.2f)
                                else -> CyberAmberSecondary.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            faucet.isLimitReached -> "MAXED"
                            faucet.isReady -> "READY"
                            else -> "${faucet.cooldownSecondsRemaining}s"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            faucet.isLimitReached -> Color(0xFF7E8394)
                            faucet.isReady -> HoneyMintTertiary
                            else -> CyberAmberSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = faucet.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = faucet.coinSymbol,
                fontSize = 11.sp,
                color = HoneyGoldLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Daily Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daily Claims",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${faucet.dailyClaims}/${faucet.maxDailyClaims}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (faucet.isLimitReached) Color(0xFFFF7043) else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progressToLimit },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (faucet.isLimitReached) Color(0xFFFF7043) else HoneyGoldPrimary,
                trackColor = Color(0xFF262936)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (faucet.isReady) HoneyGoldPrimary else Color(0xFF232632),
                    contentColor = if (faucet.isReady) Color.Black else Color(0xFF8A8F9E)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            faucet.isLimitReached -> "Limit 10/10"
                            faucet.cooldownSecondsRemaining > 0 -> "Wait ${faucet.cooldownSecondsRemaining}s"
                            else -> "Claim Now"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (faucet.cooldownSecondsRemaining > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Skip timer (Test)",
                    fontSize = 9.sp,
                    color = HoneyAccentCyan,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onResetCooldown() }
                        .padding(2.dp)
                )
            }
        }
    }
}
