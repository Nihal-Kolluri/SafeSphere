package com.safesphere.ui.screens.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safesphere.data.model.MedicalProfile
import com.safesphere.ui.theme.EmergencyRed
import com.safesphere.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalProfileScreen(
    currentProfile: MedicalProfile,
    onSaveProfile: (MedicalProfile) -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf(currentProfile.fullName) }
    var bloodType by remember { mutableStateOf(currentProfile.bloodType) }
    var allergies by remember { mutableStateOf(currentProfile.allergies) }
    var conditions by remember { mutableStateOf(currentProfile.medicalConditions) }
    var medications by remember { mutableStateOf(currentProfile.currentMedications) }
    var organDonor by remember { mutableStateOf(currentProfile.organDonor) }
    var notes by remember { mutableStateOf(currentProfile.emergencyNotes) }
    var savedSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("ICE Medical ID", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onSaveProfile(
                            MedicalProfile(
                                fullName = fullName,
                                bloodType = bloodType,
                                allergies = allergies,
                                medicalConditions = conditions,
                                currentMedications = medications,
                                organDonor = organDonor,
                                emergencyNotes = notes
                            )
                        )
                        savedSnackbar = true
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = SafeGreen)
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
                Text(
                    text = "In-Case-of-Emergency (ICE) details are shared with verified first responders and medical staff upon arrival.",
                    color = Color(0xFFAAAAAA),
                    fontSize = 13.sp
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Legal Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bloodType,
                            onValueChange = { bloodType = it },
                            label = { Text("Blood Group (e.g. O+, A+, B-)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = allergies,
                            onValueChange = { allergies = it },
                            label = { Text("Critical Allergies (e.g. Penicillin, Peanuts)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = conditions,
                            onValueChange = { conditions = it },
                            label = { Text("Medical Conditions (e.g. Asthma, Diabetes)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = medications,
                            onValueChange = { medications = it },
                            label = { Text("Current Medications") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Organ Donor", color = Color.White, fontSize = 14.sp)
                            Switch(checked = organDonor, onCheckedChange = { organDonor = it })
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Special Emergency Notes / Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        onSaveProfile(
                            MedicalProfile(
                                fullName = fullName,
                                bloodType = bloodType,
                                allergies = allergies,
                                medicalConditions = conditions,
                                currentMedications = medications,
                                organDonor = organDonor,
                                emergencyNotes = notes
                            )
                        )
                        savedSnackbar = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Save Medical Profile", fontWeight = FontWeight.Bold)
                }
            }

            if (savedSnackbar) {
                item {
                    Text("✅ Profile updated successfully!", color = SafeGreen, fontSize = 13.sp)
                }
            }
        }
    }
}
