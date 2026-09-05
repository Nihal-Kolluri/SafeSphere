package com.safesphere.ui.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safesphere.data.model.EmergencyState
import com.safesphere.data.model.VolunteerStatus
import com.safesphere.ui.theme.EmergencyRed
import com.safesphere.ui.theme.RescueAmber
import com.safesphere.ui.theme.SafeGreen

@Composable
fun ActiveEmergencyScreen(
    state: EmergencyState,
    onEscalateImmediately: () -> Unit,
    onTogglePowerSaver: () -> Unit,
    onRequestCancel: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF121212),
        bottomBar = {
            Surface(
                color = Color(0xFF1E1E1E),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onTogglePowerSaver,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF2C2C2E),
                            contentColor = RescueAmber
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatterySaver,
                            contentDescription = "Power Saver",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Power Saver", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onRequestCancel,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmergencyRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Stop",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Cancel SOS (PIN)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Active Alert Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF330A0A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Distress",
                            tint = EmergencyRed,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (state.isSilentMode) "SILENT SOS BROADCASTING" else "EMERGENCY RESCUE ACTIVE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Incident: ${state.incidentId.ifEmpty { "ACTIVE" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCCCCCC)
                            )
                        }
                    }
                }
            }

            // Escalation Pipeline Card
            item {
                EscalationProgressCard(
                    activeTier = state.activeTier,
                    remainingSeconds = state.tierRemainingSeconds,
                    responderCount = state.responders.size,
                    onEscalateImmediately = onEscalateImmediately
                )
            }

            // Live Telemetry & Battery Optimization Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📍 Live Rescue Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        val telemetry = state.latestTelemetry
                        val battery = telemetry?.batteryPercentage ?: 100
                        val profileText = when {
                            battery > 50 -> "High Precision Mode (Updates every 3-5s)"
                            battery in 15..50 -> "Adaptive Burst Mode (15s moving, 60s idle)"
                            else -> "Critical Survival Mode (Updates every 120s)"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Phone Battery: $battery%",
                                fontWeight = FontWeight.SemiBold,
                                color = if (battery < 20) EmergencyRed else SafeGreen
                            )
                            Text(
                                text = if (state.isSimulated) "SIMULATION ACTIVE" else "GPS LOCKED",
                                fontSize = 12.sp,
                                color = Color(0xFFAAAAAA)
                            )
                        }

                        Text(
                            text = "🔋 Profile: $profileText",
                            fontSize = 12.sp,
                            color = RescueAmber
                        )

                        if (telemetry != null) {
                            Text(
                                text = "Coordinates: ${"%.5f".format(telemetry.latitude)}, ${"%.5f".format(telemetry.longitude)} (±${"%.1f".format(telemetry.accuracyMeters)}m)",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Speed: ${"%.1f".format(telemetry.speedMps * 3.6f)} km/h • Altitude: ${"%.0f".format(telemetry.altitudeMeters)}m",
                                fontSize = 12.sp,
                                color = Color(0xFFAAAAAA)
                            )
                        } else {
                            Text(
                                text = "Acquiring GPS fix...",
                                fontSize = 13.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                }
            }

            // Volunteer Community Responders Card
            if (state.responders.isNotEmpty()) {
                item {
                    Text(
                        text = "🤝 Community First Responders (Within 2km)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                items(state.responders) { responder ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF252528))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = responder.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${responder.distanceMeters}m away • ETA: ${responder.estimatedArrivalMinutes} mins",
                                    fontSize = 12.sp,
                                    color = Color(0xFFAAAAAA)
                                )
                            }

                            val isResponding = responder.status == VolunteerStatus.RESPONDING
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isResponding) Color(0xFF1B5E20) else Color(0xFF37474F),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isResponding) "Responding" else "Alerted",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isResponding) Color(0xFF81C784) else Color(0xFFB0BEC5)
                                )
                            }
                        }
                    }
                }
            }

            // Event Logs
            if (state.eventLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "📋 Incident Activity Log",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA)
                    )
                }

                items(state.eventLogs.reversed().take(5)) { log ->
                    Text(
                        text = "• $log",
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
