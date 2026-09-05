package com.safesphere.ui.screens.volunteer

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
import com.safesphere.data.model.VolunteerResponder
import com.safesphere.data.model.VolunteerStatus
import com.safesphere.ui.theme.EmergencyRed
import com.safesphere.ui.theme.RescueAmber
import com.safesphere.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerDashboardScreen(
    isVolunteerMode: Boolean,
    onToggleVolunteerMode: (Boolean) -> Unit,
    activeIncidents: List<VolunteerResponder>,
    onRespondToIncident: (VolunteerResponder) -> Unit,
    onBack: () -> Unit
) {
    var radiusKm by remember { mutableStateOf(2.0f) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("Community Volunteer Hub", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Volunteer Mode Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Responder Availability",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isVolunteerMode) "ACTIVE: You will be notified of nearby crises" else "INACTIVE: You will not receive distress alerts",
                                    fontSize = 12.sp,
                                    color = if (isVolunteerMode) SafeGreen else Color(0xFF888888)
                                )
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

                        if (isVolunteerMode) {
                            Divider(color = Color(0xFF2C2C2E))
                            Text(
                                text = "Alert Radius: ${"%.1f".format(radiusKm)} km",
                                color = Color(0xFFAAAAAA),
                                fontSize = 13.sp
                            )
                            Slider(
                                value = radiusKm,
                                onValueChange = { radiusKm = it },
                                valueRange = 0.5f..5.0f,
                                steps = 9,
                                colors = SliderDefaults.colors(thumbColor = SafeGreen, activeTrackColor = SafeGreen)
                            )
                        }
                    }
                }
            }

            // Active Distress Alerts in Radius
            item {
                Text(
                    text = "🚨 Nearby Active Emergencies (${activeIncidents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (!isVolunteerMode) {
                item {
                    Text(
                        text = "Turn on Responder Availability above to see and accept nearby emergency calls.",
                        color = Color(0xFF777777),
                        fontSize = 13.sp
                    )
                }
            } else if (activeIncidents.isEmpty()) {
                item {
                    Text(
                        text = "No active emergency alerts in your radius. The community is safe!",
                        color = SafeGreen,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(activeIncidents) { incident ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF252528))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alert",
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = incident.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }

                                Surface(
                                    color = RescueAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${incident.distanceMeters}m away",
                                        color = RescueAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Victim needs immediate assistance. Estimated walking time: ${incident.estimatedArrivalMinutes} mins.",
                                color = Color(0xFFB0B0B0),
                                fontSize = 13.sp
                            )

                            val isResponding = incident.status == VolunteerStatus.RESPONDING
                            Button(
                                onClick = { onRespondToIncident(incident) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isResponding) SafeGreen else EmergencyRed
                                )
                            ) {
                                Icon(
                                    imageVector = if (isResponding) Icons.Default.DirectionsRun else Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isResponding) "En Route (Navigation Active)" else "I am Responding!",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
