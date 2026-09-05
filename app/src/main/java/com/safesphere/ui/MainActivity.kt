package com.safesphere.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safesphere.SafeSphereApp
import com.safesphere.core.sensors.ShakeDetector
import com.safesphere.core.sensors.VolumeChordDetector
import com.safesphere.data.local.entity.EmergencyContactEntity
import com.safesphere.ui.screens.contacts.EmergencyContactsScreen
import com.safesphere.ui.screens.contacts.MedicalProfileScreen
import com.safesphere.ui.screens.emergency.ActiveEmergencyScreen
import com.safesphere.ui.screens.emergency.DualPinCancelDialog
import com.safesphere.ui.screens.emergency.RescuePowerSaverScreen
import com.safesphere.ui.screens.home.HomeScreen
import com.safesphere.ui.screens.simulation.SimulationControlScreen
import com.safesphere.ui.screens.volunteer.VolunteerDashboardScreen
import com.safesphere.ui.theme.SafeSphereTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var volumeChordDetector: VolumeChordDetector

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val smsGranted = permissions[Manifest.permission.SEND_SMS] == true
        if (!locationGranted || !smsGranted) {
            Toast.makeText(this, "Location and SMS permissions are required for rescue tracking", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRequiredPermissions()

        val app = SafeSphereApp.instance
        val repository = app.repository

        // Initialize Shake Detection
        shakeDetector = ShakeDetector(this) {
            runOnUiThread {
                if (!repository.emergencyState.value.isActive) {
                    Toast.makeText(this, "⚡ Shake detected! Triggering SOS...", Toast.LENGTH_SHORT).show()
                    repository.startCountdown(isSilent = false, isSimulated = false)
                }
            }
        }

        // Initialize Silent SOS Volume Chord Detection (3 rapid volume button presses)
        volumeChordDetector = VolumeChordDetector {
            runOnUiThread {
                if (!repository.emergencyState.value.isActive) {
                    Toast.makeText(this, "🤫 Silent SOS Activated", Toast.LENGTH_SHORT).show()
                    repository.triggerEmergencyDirectly(isSilent = true, isSimulated = false)
                }
            }
        }

        setContent {
            SafeSphereTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()

                    val emergencyState by repository.emergencyState.collectAsState()
                    val contacts by repository.contacts.collectAsState(initial = emptyList())
                    val medicalProfile by repository.medicalProfile.collectAsState()
                    val isVolunteerMode by repository.isVolunteerModeEnabled.collectAsState()
                    val batteryState by app.batteryMonitor.batteryState.collectAsState()

                    var isSimulationMode by remember { mutableStateOf(value = false) }
                    var showPinCancelDialog by remember { mutableStateOf(value = false) }

                    // Apply Rescue Brightness (dims to 1% when Power Saver is toggled)
                    LaunchedEffect(emergencyState.isRescuePowerSaverActive) {
                        app.powerSaverController.applyRescueBrightness(
                            this@MainActivity,
                            emergencyState.isRescuePowerSaverActive
                        )
                    }

                    // Top-level Navigation & Active State routing
                    if (emergencyState.isActive) {
                        if (emergencyState.isRescuePowerSaverActive) {
                            RescuePowerSaverScreen(
                                emergencyState = emergencyState,
                                onExitPowerSaver = { repository.toggleRescuePowerSaver() },
                                onRequestPinCancel = { showPinCancelDialog = true },
                            )
                        } else {
                            ActiveEmergencyScreen(
                                state = emergencyState,
                                onEscalateImmediately = { repository.escalateImmediatelyToAuthorities() },
                                onTogglePowerSaver = { repository.toggleRescuePowerSaver() },
                                onRequestCancel = { showPinCancelDialog = true }
                            )
                        }
                    } else {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    emergencyState = emergencyState,
                                    batteryPercent = batteryState.percentage,
                                    contactCount = contacts.size,
                                    isVolunteerMode = isVolunteerMode,
                                    isSimulationMode = isSimulationMode,
                                    onTriggerSos = { isSilent ->
                                        repository.startCountdown(isSilent = isSilent, isSimulated = isSimulationMode)
                                    },
                                    onAbortCountdown = { repository.abortCountdown() },
                                    onToggleVolunteerMode = { repository.setVolunteerModeEnabled(it) },
                                    onToggleSimulationMode = { isSimulationMode = it },
                                    onNavigateToContacts = { navController.navigate("contacts") },
                                    onNavigateToMedical = { navController.navigate("medical") },
                                    onNavigateToVolunteer = { navController.navigate("volunteer") },
                                    onNavigateToSimulation = { navController.navigate("simulation") }
                                )
                            }

                            composable("contacts") {
                                EmergencyContactsScreen(
                                    contacts = contacts,
                                    onAddContact = { name, phone, relation, isPrimary ->
                                        scope.launch {
                                            repository.addContact(
                                                EmergencyContactEntity(
                                                    name = name,
                                                    phoneNumber = phone,
                                                    relationship = relation,
                                                    isPrimary = isPrimary,
                                                )
                                            )
                                        }
                                    },
                                    onDeleteContact = { contact ->
                                        scope.launch { repository.deleteContact(contact) }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("medical") {
                                MedicalProfileScreen(
                                    currentProfile = medicalProfile,
                                    onSaveProfile = { newProfile ->
                                        scope.launch { repository.updateMedicalProfile(newProfile) }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("volunteer") {
                                VolunteerDashboardScreen(
                                    isVolunteerMode = isVolunteerMode,
                                    onToggleVolunteerMode = { repository.setVolunteerModeEnabled(it) },
                                    activeIncidents = emergencyState.responders,
                                    onRespondToIncident = { repository.simulateVolunteerAcceptance() },
                                    onBack = { navController.popBackStack() },
                                )
                            }

                            composable("simulation") {
                                SimulationControlScreen(
                                    state = emergencyState,
                                    onSetBattery = { repository.setSimulatedBattery(it) },
                                    onSimulateVolunteerAcceptance = { repository.simulateVolunteerAcceptance() },
                                    onTriggerTestSos = {
                                        isSimulationMode = true
                                        repository.triggerEmergencyDirectly(isSilent = false, isSimulated = true)
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    // Dual-PIN Cancellation Dialog
                    if (showPinCancelDialog) {
                        DualPinCancelDialog(
                            onValidatePin = { pin ->
                                repository.validatePinAndCancel(pin)
                            },
                            onDismiss = { showPinCancelDialog = false },
                            onDeactivatedSuccessfully = {
                                showPinCancelDialog = false
                                Toast.makeText(this@MainActivity, "✅ Emergency cancelled safely", Toast.LENGTH_SHORT).show()
                            },
                            onDuressCovertDeactivation = {
                                showPinCancelDialog = false
                                Toast.makeText(this@MainActivity, "Emergency cancelled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val handled = volumeChordDetector.onKeyDown(event.keyCode, event)
            if (handled) return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()
        SafeSphereApp.instance.batteryMonitor.startListening()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
        SafeSphereApp.instance.batteryMonitor.stopListening()
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.VIBRATE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}
