package com.safesphere.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.safesphere.data.model.EmergencyState
import com.safesphere.ui.theme.EmergencyRed
import com.safesphere.ui.theme.RescueAmber
import com.safesphere.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    emergencyState: EmergencyState,
    batteryPercent: Int,
    contactCount: Int,
    isVolunteerMode: Boolean,
    isSimulationMode: Boolean,
    onTriggerSos: (isSilent: Boolean) -> Unit,
    onAbortCountdown: () -> Unit,
    onToggleVolunteerMode: (Boolean) -> Unit,
    @Suppress("UNUSED_PARAMETER") onToggleSimulationMode: (Boolean) -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToMedical: () -> Unit,
    onNavigateToVolunteer: () -> Unit,
    onNavigateToSimulation: () -> Unit,
) {
    // Pulsing animation for the SOS button
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SafeSphere",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        if (isSimulationMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = RescueAmber,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "TEST MODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                actions = {
                    IconButton(onClick = onNavigateToSimulation) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Simulation Lab",
                            tint = RescueAmber
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status bar overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStatusChip(
                    icon = Icons.Default.BatteryChargingFull,
                    label = "Battery",
                    value = "$batteryPercent%",
                    valueColor = if (batteryPercent < 20) EmergencyRed else SafeGreen,
                    modifier = Modifier.weight(1f)
                )

                QuickStatusChip(
                    icon = Icons.Default.Contacts,
                    label = "Contacts",
                    value = "$contactCount Ready",
                    valueColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToContacts
                )

                QuickStatusChip(
                    icon = Icons.Default.HealthAndSafety,
                    label = "Medical ID",
                    value = "Configured",
                    valueColor = SafeGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMedical
                )
            }

            // Central Pulsing SOS Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(EmergencyRed.copy(alpha = 0.2f))
                        .clickable { onTriggerSos(false) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(EmergencyRed)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "SOS",
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SOS",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "HOLD TO ALERT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFCDD2)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tap or shake phone to trigger 3-step escalation",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Silent SOS Shortcut
                OutlinedButton(
                    onClick = { onTriggerSos(true) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFAAAAAA)
                    ),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Silent SOS",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Silent SOS (Covert Alert)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom Navigation Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Volunteer Responder Switch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = "Volunteer",
                                tint = SafeGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Volunteer Responder Mode",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isVolunteerMode) "Ready to receive alerts within 2km" else "Opt-in to help neighbors in crisis",
                                    fontSize = 11.sp,
                                    color = Color(0xFFAAAAAA)
                                )
                            }
                        }
                        Switch(
                            checked = isVolunteerMode,
                            onCheckedChange = onToggleVolunteerMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SafeGreen
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToContacts,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))
                    ) {
                        Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Contacts", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onNavigateToVolunteer,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E))
                    ) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Responders", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // 5-Second Cancel Countdown Dialog (for non-silent trigger)
    if (emergencyState.isCountdownActive) {
        Dialog(onDismissRequest = onAbortCountdown) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚨 ALERTING IN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RescueAmber
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = emergencyState.countdownRemainingSeconds.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = EmergencyRed
                    )

                    Text(
                        text = "Dispatches to Family, Volunteers & 911",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onAbortCountdown,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF424242),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancel (False Alarm)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatusChip(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = valueColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF888888))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
