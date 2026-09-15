package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary

@Composable
fun CryptoMathCaptcha(
    onSolved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val a = remember { 12 + (0..15).random() }
    val b = remember { 8 + (0..12).random() }
    val correctSum = a + b

    val choices = remember {
        val set = mutableSetOf(correctSum)
        while (set.size < 4) {
            val delta = (-6..6).random()
            if (delta != 0) set.add(correctSum + delta)
        }
        set.toList().shuffled()
    }

    var selectedChoice by remember { mutableIntStateOf(-1) }
    var isSolved by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        if (isSolved) HoneyMintTertiary else HoneyGoldPrimary.copy(alpha = 0.5f),
                        CyberAmberSecondary.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSolved) Icons.Default.CheckCircle else Icons.Default.Pin,
                    contentDescription = "Crypto Checksum",
                    tint = if (isSolved) HoneyMintTertiary else HoneyGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hex Checksum Challenge",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Anti-Bot Proof",
                fontSize = 11.sp,
                color = HoneyGoldPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Solve the crypto block nonce equation to verify your claim:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Equation Card
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F1014))
                .border(1.dp, Color(0xFF2E323E), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "0x%02X + 0x%02X = ? (%d + %d)".format(a, b, a, b),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isSolved) HoneyMintTertiary else HoneyGoldPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 options in 2x2 grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (idx in 0..1) {
                val choice = choices[idx]
                val isSelected = selectedChoice == choice
                OutlinedButton(
                    onClick = {
                        if (!isSolved) {
                            selectedChoice = choice
                            showError = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("math_choice_$idx"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) HoneyGoldPrimary.copy(alpha = 0.2f) else Color.Transparent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (isSelected) HoneyGoldPrimary else Color(0xFF373A47),
                                if (isSelected) CyberAmberSecondary else Color(0xFF2E323E)
                            )
                        )
                    )
                ) {
                    Text(
                        text = "$choice (0x%02X)".format(choice),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (idx in 2..3) {
                val choice = choices[idx]
                val isSelected = selectedChoice == choice
                OutlinedButton(
                    onClick = {
                        if (!isSolved) {
                            selectedChoice = choice
                            showError = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("math_choice_$idx"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) HoneyGoldPrimary.copy(alpha = 0.2f) else Color.Transparent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (isSelected) HoneyGoldPrimary else Color(0xFF373A47),
                                if (isSelected) CyberAmberSecondary else Color(0xFF2E323E)
                            )
                        )
                    )
                ) {
                    Text(
                        text = "$choice (0x%02X)".format(choice),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (showError) {
            Text(
                text = "Incorrect checksum! Check math calculation and try again.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (selectedChoice == correctSum) {
                    isSolved = true
                    showError = false
                    onSolved()
                } else {
                    showError = true
                }
            },
            enabled = !isSolved && selectedChoice != -1,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSolved) HoneyMintTertiary else HoneyGoldPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verify_math_button")
        ) {
            Text(
                text = if (isSolved) "Verified Successfully ✓" else "Verify Checksum",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
