package com.safesphere.ui.screens.contacts

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
import androidx.compose.ui.window.Dialog
import com.safesphere.data.local.entity.EmergencyContactEntity
import com.safesphere.ui.theme.EmergencyRed
import com.safesphere.ui.theme.SafeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
    contacts: List<EmergencyContactEntity>,
    onAddContact: (String, String, String, Boolean) -> Unit,
    onDeleteContact: (EmergencyContactEntity) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmergencyRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Auto-Detected Official Authorities Section
            item {
                Text(
                    text = "🚨 Local Emergency Services (Auto-Detected)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFAAAAAA),
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuthorityRow(name = "Police Dispatch", number = "911 / 112", icon = Icons.Default.LocalPolice)
                        Divider(color = Color(0xFF2C2C2E))
                        AuthorityRow(name = "Fire Department", number = "911 / 112", icon = Icons.Default.LocalFireDepartment)
                        Divider(color = Color(0xFF2C2C2E))
                        AuthorityRow(name = "Ambulance / Paramedics", number = "911 / 112", icon = Icons.Default.LocalHospital)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "👨‍👩‍👧 Family & Trusted Contacts (Step 1 Alert)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFAAAAAA),
                    fontWeight = FontWeight.Bold
                )
            }

            if (contacts.isEmpty()) {
                item {
                    Text(
                        text = "No custom contacts added yet. Tap + to add emergency family members.",
                        color = Color(0xFF666666),
                        fontSize = 13.sp
                    )
                }
            } else {
                items(contacts) { contact ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = contact.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    if (contact.isPrimary) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = SafeGreen.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "PRIMARY",
                                                color = SafeGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${contact.relationship} • ${contact.phoneNumber}",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(onClick = { onDeleteContact(contact) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("Family") }
        var isPrimary by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add Emergency Contact", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("Relationship (e.g. Mother, Spouse)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isPrimary, onCheckedChange = { isPrimary = it })
                        Text("Set as Primary Contact", color = Color.White, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    onAddContact(name, phone, relation, isPrimary)
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                        ) {
                            Text("Save Contact")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthorityRow(name: String, number: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = name, tint = EmergencyRed, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = name, color = Color.White, fontSize = 14.sp)
        }
        Text(text = number, color = SafeGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
