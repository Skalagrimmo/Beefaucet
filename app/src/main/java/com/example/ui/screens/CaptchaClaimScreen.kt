package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.CryptoMathCaptcha
import com.example.ui.components.HoneycombSliderCaptcha
import com.example.ui.components.PatternMatrixCaptcha
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyAccentCyan
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary
import com.example.ui.viewmodel.CaptchaMode
import com.example.ui.viewmodel.FaucetUiState

@Composable
fun CaptchaClaimScreen(
    state: FaucetUiState,
    onModeSelected: (CaptchaMode) -> Unit,
    onSolved: (Context) -> Unit,
    onDismissSuccess: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.testTag("back_to_faucet_home")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Native Faucet Claim",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Zero-browser in-app captcha verification",
                        fontSize = 12.sp,
                        color = HoneyGoldLight
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(HoneyGoldPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🔒 No Browser",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HoneyGoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feature highlights badge
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberAmberSecondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Fast",
                        tint = CyberAmberSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Instant Anti-Bot Authorization",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Complete the challenge below to prove your human identity and receive BEE tokens straight into your integrated wallet.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row for selecting captcha mode
        TabRow(
            selectedTabIndex = state.selectedCaptchaMode.ordinal,
            containerColor = Color(0xFF16181E),
            contentColor = HoneyGoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedCaptchaMode.ordinal]),
                    color = HoneyGoldPrimary
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .fillMaxWidth()
                .testTag("captcha_mode_tabrow")
        ) {
            Tab(
                selected = state.selectedCaptchaMode == CaptchaMode.SLIDER,
                onClick = { onModeSelected(CaptchaMode.SLIDER) },
                text = { Text("⬡ Slider", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.LinearScale, contentDescription = "Slider", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = state.selectedCaptchaMode == CaptchaMode.MATRIX,
                onClick = { onModeSelected(CaptchaMode.MATRIX) },
                text = { Text("▦ Matrix", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.GridOn, contentDescription = "Matrix", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = state.selectedCaptchaMode == CaptchaMode.MATH,
                onClick = { onModeSelected(CaptchaMode.MATH) },
                text = { Text("🔢 Checksum", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Pin, contentDescription = "Math", modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Active Captcha Component
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_captcha_container")
        ) {
            when (state.selectedCaptchaMode) {
                CaptchaMode.SLIDER -> {
                    HoneycombSliderCaptcha(
                        onSolved = { onSolved(context) }
                    )
                }
                CaptchaMode.MATRIX -> {
                    PatternMatrixCaptcha(
                        onSolved = { onSolved(context) }
                    )
                }
                CaptchaMode.MATH -> {
                    CryptoMathCaptcha(
                        onSolved = { onSolved(context) }
                    )
                }
            }
        }

        if (state.isClaiming) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = HoneyGoldPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Hashing cryptographic proof...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Signing claim & crediting wallet",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Success Dialog Modal
    if (state.showClaimSuccessDialog) {
        Dialog(onDismissRequest = onDismissSuccess) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181A22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, HoneyGoldPrimary, RoundedCornerShape(24.dp))
                    .testTag("claim_success_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐝", fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Claim Successful!",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = HoneyGoldLight
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "+%.4f BEE".format(state.lastClaimReward),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = HoneyMintTertiary
                    )

                    Text(
                        text = "Deposited directly to your integrated wallet",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transaction Hash Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0E0F14))
                            .border(1.dp, Color(0xFF2B2E3C), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "BLOCKCHAIN TX HASH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (state.lastTxHash.length > 22)
                                        "${state.lastTxHash.take(12)}...${state.lastTxHash.takeLast(8)}"
                                    else state.lastTxHash,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = HoneyAccentCyan
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy hash",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            val clipboard =
                                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Tx Hash", state.lastTxHash)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Tx Hash copied!", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                        }
                    }

                    // Automated withdrawal alert if triggered
                    state.lastAutoWithdrawal?.let { autoTx ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2417)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Auto Payout",
                                        tint = HoneyMintTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "⚡ Automated Payout Executed!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HoneyMintTertiary
                                    )
                                }
                                Text(
                                    text = "Auto-threshold reached! Transferred %.4f BEE to payout address.".format(autoTx.amount),
                                    fontSize = 11.sp,
                                    color = Color(0xFFC0EACF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onDismissSuccess()
                                onNavigateToWallet()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252936)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Wallet", fontSize = 13.sp, color = HoneyGoldLight)
                        }

                        Button(
                            onClick = {
                                onDismissSuccess()
                                onNavigateToHome()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
