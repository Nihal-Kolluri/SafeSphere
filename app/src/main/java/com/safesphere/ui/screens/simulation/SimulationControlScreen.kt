package com.safesphere.ui.screens.simulation

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safesphere.data.model.EmergencyState
import com.safesphere.ui.theme.EmergencyRed
import com.safesphere.ui.theme.RescueAmber
import com.safesphere.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationControlScreen(
    state: EmergencyState,
    onSetBattery: (Int) -> Unit,
    onSimulateVolunteerAcceptance: () -> Unit,
    onTriggerTestSos: () -> Unit,
    onBack: () -> Unit
) {
    var batteryLevel by remember {
        mutableFloatStateOf((state.latestTelemetry?.batteryPercentage ?: 85).toFloat())
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("Simulation & Test Lab", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = RescueAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Safe Testing Environment",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "Test the complete 3-tier escalation pipeline, battery throttling, and mock responder dispatch without sending real cellular SMS or alerting emergency authorities.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Fake Battery Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "🔋 Simulate Battery Level: ${batteryLevel.toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Slider(
                            value = batteryLevel,
                            onValueChange = {
                                batteryLevel = it
                                onSetBattery(it.toInt())
                            },
                            valueRange = 5f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (batteryLevel < 20) EmergencyRed else SafeGreen,
                                activeTrackColor = if (batteryLevel < 20) EmergencyRed else SafeGreen
                            )
                        )

                        val adaptiveStatus = when {
                            batteryLevel > 50 -> "Mode: High Precision Continuous (3-5s updates)"
                            batteryLevel in 15f..50f -> "Mode: Dynamic Adaptive Burst (15s moving, 60s idle)"
                            else -> "Mode: Critical Ultra-Survival (120s updates)"
                        }

                        Text(
                            text = "Current Throttle: $adaptiveStatus",
                            fontSize = 12.sp,
                            color = RescueAmber
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!state.isActive) {
                        Button(
                            onClick = onTriggerTestSos,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trigger Test SOS (Simulated)")
                        }
                    }

                    if (state.isActive) {
                        Button(
                            onClick = onSimulateVolunteerAcceptance,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Inject Volunteer 'I am Responding'")
                        }
                    }
                }
            }

            // Simulated SMS Outbox Logs
            item {
                Text(
                    text = "📨 Simulated SMS Outbox (${state.dispatchedSmsLogs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (state.dispatchedSmsLogs.isEmpty()) {
                item {
                    Text(
                        text = "No SMS dispatched yet. Trigger an emergency to inspect messages.",
                        color = Color(0xFF666666),
                        fontSize = 13.sp
                    )
                }
            } else {
                items(state.dispatchedSmsLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF252528))
                    ) {
                        Text(
                            text = log,
                            color = Color(0xFFE0E0E0),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
