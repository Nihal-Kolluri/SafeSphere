package com.safesphere.ui.screens.emergency

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safesphere.data.model.EmergencyState
import com.safesphere.ui.theme.PureBlack

@Composable
fun RescuePowerSaverScreen(
    emergencyState: EmergencyState,
    onExitPowerSaver: () -> Unit,
    onRequestPinCancel: () -> Unit
) {
    val context = LocalContext.current
    val batteryPercent = emergencyState.latestTelemetry?.batteryPercentage ?: 100

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header: Minimal OLED Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RESCUE POWER SAVER [ACTIVE]",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF666666)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = if (batteryPercent < 20) Color(0xFFB71C1C) else Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$batteryPercent%",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF888888)
                    )
                }
            }

            // Center Telemetry Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SOS BROADCASTING",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFCCCCCC)
                )

                Text(
                    text = "Screen dimmed to 1% • GPS in battery-burst mode",
                    fontSize = 12.sp,
                    color = Color(0xFF555555)
                )

                Spacer(modifier = Modifier.height(16.dp))

                emergencyState.latestTelemetry?.let { telem ->
                    Text(
                        text = "LAT: ${"%.5f".format(telem.latitude)} | LNG: ${"%.5f".format(telem.longitude)}",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF777777)
                    )
                }

                val respondingCount = emergencyState.responders.count {
                    it.status == com.safesphere.data.model.VolunteerStatus.RESPONDING
                }
                if (respondingCount > 0) {
                    Text(
                        text = "HELP EN ROUTE: $respondingCount volunteer(s) responding",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom Actions (High utility, lowest OLED burn)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF888888)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System Power Saver",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Android Battery Saver Settings",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onExitPowerSaver,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFAAAAAA)
                        )
                    ) {
                        Text(
                            text = "Exit Power Saver",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = onRequestPinCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF331111),
                            contentColor = Color(0xFFFF6666)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Cancel PIN",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cancel SOS",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
