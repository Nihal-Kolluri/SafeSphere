package com.safesphere.ui.screens.emergency

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safesphere.data.model.EscalationTier
import com.safesphere.ui.theme.EmergencyRed
import com.safesphere.ui.theme.RescueAmber
import com.safesphere.ui.theme.SafeGreen

@Composable
fun EscalationProgressCard(
    activeTier: EscalationTier,
    remainingSeconds: Int,
    responderCount: Int,
    onEscalateImmediately: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚨 Tiered Escalation Pipeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (activeTier != EscalationTier.TIER_3_AUTHORITIES) {
                    FilledTonalButton(
                        onClick = onEscalateImmediately,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = EmergencyRed,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Escalate Now",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Escalate Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Family
            TierStepRow(
                stepNumber = 1,
                title = "Family & Trusted Contacts",
                subtitle = "Direct SMS dispatched with live GPS",
                icon = Icons.Default.People,
                isActive = activeTier == EscalationTier.TIER_1_FAMILY,
                isCompleted = activeTier.stepNumber > 1,
                remainingSeconds = if (activeTier == EscalationTier.TIER_1_FAMILY) remainingSeconds else null
            )

            Divider(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .height(16.dp)
                    .width(2.dp),
                color = if (activeTier.stepNumber > 1) SafeGreen else Color(0xFF424242)
            )

            // Step 2: Volunteers
            TierStepRow(
                stepNumber = 2,
                title = "Nearby Community Volunteers",
                subtitle = if (responderCount > 0) "$responderCount volunteers active nearby" else "Alerting responders within 1-2 km",
                icon = Icons.Default.Groups,
                isActive = activeTier == EscalationTier.TIER_2_VOLUNTEERS,
                isCompleted = activeTier.stepNumber > 2,
                remainingSeconds = if (activeTier == EscalationTier.TIER_2_VOLUNTEERS) remainingSeconds else null
            )

            Divider(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .height(16.dp)
                    .width(2.dp),
                color = if (activeTier.stepNumber > 2) SafeGreen else Color(0xFF424242)
            )

            // Step 3: Emergency Authorities
            TierStepRow(
                stepNumber = 3,
                title = "Emergency Authorities (Police / Fire)",
                subtitle = "911/112 auto-dialer & official dispatch SMS",
                icon = Icons.Default.LocalPolice,
                isActive = activeTier == EscalationTier.TIER_3_AUTHORITIES,
                isCompleted = false,
                remainingSeconds = null
            )
        }
    }
}

@Composable
private fun TierStepRow(
    stepNumber: Int,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    isCompleted: Boolean,
    remainingSeconds: Int?
) {
    val stepColor by animateColorAsState(
        targetValue = when {
            isCompleted -> SafeGreen
            isActive -> RescueAmber
            else -> Color(0xFF616161)
        },
        label = "stepColor"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(stepColor),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Step $stepNumber: $title",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) Color.White else Color(0xFFB0B0B0)
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888)
            )
        }

        if (isActive && remainingSeconds != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF332500))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${remainingSeconds}s",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RescueAmber
                )
            }
        }
    }
}
