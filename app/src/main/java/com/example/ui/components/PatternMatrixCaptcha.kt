package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary

data class CaptchaCell(
    val id: Int,
    val emoji: String,
    val isTarget: Boolean
)

@Composable
fun PatternMatrixCaptcha(
    onSolved: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun generateCells(): List<CaptchaCell> {
        val targets = listOf("🐝", "🍯", "🐝")
        val distractors = listOf("🌸", "💎", "🍃", "⚡", "🌻", "🪙")
        val all = (targets.map { it to true } + distractors.map { it to false }).shuffled()
        return all.mapIndexed { idx, pair ->
            CaptchaCell(id = idx, emoji = pair.first, isTarget = pair.second)
        }
    }

    var cells by remember { mutableStateOf(generateCells()) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
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
                    imageVector = if (isSolved) Icons.Default.CheckCircle else Icons.Default.GridOn,
                    contentDescription = "Pattern Matrix",
                    tint = if (isSolved) HoneyMintTertiary else HoneyGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Honey Pattern Matrix",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = {
                    if (!isSolved) {
                        cells = generateCells()
                        selectedIds = emptySet()
                        showError = false
                    }
                },
                modifier = Modifier.testTag("refresh_captcha_matrix")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Matrix",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Select all Honey Pots 🍯 & Bees 🐝 (3 total) to authorize crypto claim",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // 3x3 Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val cell = cells[index]
                        val isSelected = selectedIds.contains(cell.id)

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        isSolved && cell.isTarget -> HoneyMintTertiary.copy(alpha = 0.25f)
                                        isSelected -> HoneyGoldPrimary.copy(alpha = 0.22f)
                                        else -> Color(0xFF141519)
                                    }
                                )
                                .border(
                                    width = if (isSelected || (isSolved && cell.isTarget)) 2.dp else 1.dp,
                                    color = when {
                                        isSolved && cell.isTarget -> HoneyMintTertiary
                                        isSelected -> HoneyGoldPrimary
                                        else -> Color(0xFF2E323E)
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = !isSolved) {
                                    showError = false
                                    selectedIds = if (isSelected) {
                                        selectedIds - cell.id
                                    } else {
                                        selectedIds + cell.id
                                    }
                                }
                                .testTag("captcha_cell_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = cell.emoji, fontSize = 28.sp)
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .background(HoneyGoldPrimary, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showError) {
            Text(
                text = "Selection incorrect. Please select all Bees 🐝 and Honey Pots 🍯.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val targetIds = cells.filter { it.isTarget }.map { it.id }.toSet()
                if (selectedIds == targetIds) {
                    isSolved = true
                    showError = false
                    onSolved()
                } else {
                    showError = true
                }
            },
            enabled = !isSolved && selectedIds.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSolved) HoneyMintTertiary else HoneyGoldPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("verify_pattern_button")
        ) {
            Text(
                text = if (isSolved) "Verified Successfully ✓" else "Verify Selection (${selectedIds.size}/3)",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
