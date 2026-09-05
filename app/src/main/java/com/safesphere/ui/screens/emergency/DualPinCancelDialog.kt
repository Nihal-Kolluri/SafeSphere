package com.safesphere.ui.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.safesphere.core.security.PinValidationResult
import com.safesphere.ui.theme.EmergencyRed

@Composable
fun DualPinCancelDialog(
    onValidatePin: (String) -> PinValidationResult,
    onDismiss: () -> Unit,
    onDeactivatedSuccessfully: () -> Unit,
    onDuressCovertDeactivation: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security PIN",
                        tint = EmergencyRed,
                        modifier = Modifier.size(28.dp)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Text(
                    text = "Enter Deactivation PIN",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Enter your 4-digit Safe PIN to cancel the emergency broadcast.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAAAAAA),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    for (i in 0 until 4) {
                        val filled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) EmergencyRed else Color(0xFF3A3A3C)
                                )
                        )
                    }
                }

                if (isError) {
                    Text(
                        text = errorMessage,
                        color = EmergencyRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Number Pad (1-9, 0, Backspace)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "DEL")
                    )

                    for (row in rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (key in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key.isNotEmpty()) {
                                        FilledTonalButton(
                                            onClick = {
                                                if (key == "DEL") {
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                        isError = false
                                                    }
                                                } else {
                                                    if (enteredPin.length < 4) {
                                                        enteredPin += key
                                                        if (enteredPin.length == 4) {
                                                            // Validate PIN immediately upon 4th digit
                                                            val result = onValidatePin(enteredPin)
                                                            when (result) {
                                                                PinValidationResult.ValidSafePin -> {
                                                                    onDeactivatedSuccessfully()
                                                                }
                                                                PinValidationResult.ValidDuressPin -> {
                                                                    onDuressCovertDeactivation()
                                                                }
                                                                PinValidationResult.InvalidPin -> {
                                                                    isError = true
                                                                    errorMessage = "Invalid PIN. Try again."
                                                                    enteredPin = ""
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color(0xFF2C2C2E),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            if (key == "DEL") {
                                                Icon(
                                                    imageVector = Icons.Default.Backspace,
                                                    contentDescription = "Delete",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = key,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
