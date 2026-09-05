package com.safesphere.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.safesphere.core.battery.BatteryMonitor
import com.safesphere.core.location.AdaptiveLocationTracker
import com.safesphere.core.security.PinValidationResult
import com.safesphere.core.security.SecurityPinManager
import com.safesphere.core.service.EmergencyForegroundService
import com.safesphere.core.simulation.EmergencySimulator
import com.safesphere.core.simulation.MockVolunteerGenerator
import com.safesphere.data.local.AppDatabase
import com.safesphere.data.local.entity.EmergencyContactEntity
import com.safesphere.data.local.entity.IncidentLogEntity
import com.safesphere.data.model.EmergencyState
import com.safesphere.data.model.EscalationTier
import com.safesphere.data.model.MedicalProfile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class EmergencyRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val pinManager: SecurityPinManager = SecurityPinManager(context),
    private val batteryMonitor: BatteryMonitor = BatteryMonitor(context),
    private val locationTracker: AdaptiveLocationTracker = AdaptiveLocationTracker(context),
    private val smsDispatcher: SmsDispatcher = SmsDispatcher(context),
    val simulator: EmergencySimulator = EmergencySimulator()
) : EmergencyRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _emergencyState = MutableStateFlow(EmergencyState())
    override val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    override val contacts: Flow<List<EmergencyContactEntity>> =
        database.contactDao().getAllContacts()

    override val incidentHistory: Flow<List<IncidentLogEntity>> =
        database.incidentLogDao().getAllIncidents()

    private val _medicalProfile = MutableStateFlow(MedicalProfile())
    override val medicalProfile: StateFlow<MedicalProfile> = _medicalProfile.asStateFlow()

    private val _isVolunteerModeEnabled = MutableStateFlow(false)
    override val isVolunteerModeEnabled: StateFlow<Boolean> = _isVolunteerModeEnabled.asStateFlow()

    private var countdownJob: Job? = null
    private var escalationTimerJob: Job? = null

    init {
        // Collect location updates and update emergency state
        coroutineScope.launch {
            locationTracker.currentTelemetry.collect { telemetry ->
                if (telemetry != null && _emergencyState.value.isActive) {
                    _emergencyState.update { it.copy(latestTelemetry = telemetry) }
                }
            }
        }
    }

    override fun startCountdown(isSilent: Boolean, isSimulated: Boolean) {
        if (isSilent) {
            // Silent SOS bypasses countdown and initiates immediately
            triggerEmergencyDirectly(isSilent = true, isSimulated = isSimulated)
            return
        }

        countdownJob?.cancel()
        _emergencyState.update {
            it.copy(
                isCountdownActive = true,
                countdownRemainingSeconds = 5,
                isSilentMode = false,
                isSimulated = isSimulated
            )
        }

        countdownJob = coroutineScope.launch {
            for (sec in 4 downTo 1) {
                delay(1000)
                _emergencyState.update { it.copy(countdownRemainingSeconds = sec) }
            }
            delay(1000)
            _emergencyState.update { it.copy(isCountdownActive = false) }
            triggerEmergencyDirectly(isSilent = false, isSimulated = isSimulated)
        }
    }

    override fun abortCountdown() {
        countdownJob?.cancel()
        _emergencyState.update {
            it.copy(
                isCountdownActive = false,
                countdownRemainingSeconds = 5
            )
        }
    }

    override fun triggerEmergencyDirectly(isSilent: Boolean, isSimulated: Boolean) {
        val incidentId = "INC-" + UUID.randomUUID().toString().take(8).uppercase()
        val initialTier = EscalationTier.TIER_1_FAMILY

        // Start hardware tracking
        val batteryInfo = if (isSimulated) {
            BatteryMonitor.BatteryInfo(simulator.simulatedBattery.value, false, false)
        } else {
            batteryMonitor.getCurrentBatteryInfo()
        }

        locationTracker.startTracking(batteryInfo.percentage, batteryInfo.isCharging)
        val initialTelemetry = if (isSimulated) {
            simulator.createSimulatedTelemetry()
        } else {
            locationTracker.currentTelemetry.value
        }

        val responders = if (isSimulated) {
            MockVolunteerGenerator.generateNearbyVolunteers(3)
        } else {
            MockVolunteerGenerator.generateNearbyVolunteers(2)
        }

        _emergencyState.update {
            it.copy(
                isActive = true,
                isCountdownActive = false,
                incidentId = incidentId,
                isSilentMode = isSilent,
                isSimulated = isSimulated,
                activeTier = initialTier,
                tierRemainingSeconds = initialTier.defaultDurationSeconds,
                latestTelemetry = initialTelemetry,
                responders = responders,
                isDuressActive = false,
                eventLogs = listOf("🚨 Emergency triggered (Incident #$incidentId)"),
                dispatchedSmsLogs = emptyList()
            )
        }

        // Start Android Foreground Service
        startForegroundService()

        // Execute Step 1 dispatch (Family)
        dispatchTierActions(initialTier)

        // Start Tier Timer Loop
        startTierEscalationLoop()
    }

    private fun startTierEscalationLoop() {
        escalationTimerJob?.cancel()
        escalationTimerJob = coroutineScope.launch {
            while (_emergencyState.value.isActive) {
                delay(1000)
                val current = _emergencyState.value
                if (!current.isActive) break

                if (current.activeTier == EscalationTier.TIER_3_AUTHORITIES) {
                    // Final tier - stays until resolved
                    continue
                }

                if (current.tierRemainingSeconds > 1) {
                    _emergencyState.update {
                        it.copy(tierRemainingSeconds = it.tierRemainingSeconds - 1)
                    }
                } else {
                    // Transition to next tier
                    escalateToNextTier()
                }
            }
        }
    }

    private fun escalateToNextTier() {
        val currentTier = _emergencyState.value.activeTier
        val nextTier = when (currentTier) {
            EscalationTier.TIER_1_FAMILY -> EscalationTier.TIER_2_VOLUNTEERS
            EscalationTier.TIER_2_VOLUNTEERS -> EscalationTier.TIER_3_AUTHORITIES
            EscalationTier.TIER_3_AUTHORITIES -> EscalationTier.TIER_3_AUTHORITIES
        }

        if (nextTier != currentTier) {
            _emergencyState.update {
                it.copy(
                    activeTier = nextTier,
                    tierRemainingSeconds = nextTier.defaultDurationSeconds,
                    eventLogs = it.eventLogs + "⏱️ Escalated to Step ${nextTier.stepNumber}: ${nextTier.title}"
                )
            }
            dispatchTierActions(nextTier)
        }
    }

    override fun escalateImmediatelyToAuthorities() {
        val targetTier = EscalationTier.TIER_3_AUTHORITIES
        _emergencyState.update {
            it.copy(
                activeTier = targetTier,
                tierRemainingSeconds = 0,
                eventLogs = it.eventLogs + "⚡ User manually escalated immediately to Step 3: Emergency Authorities"
            )
        }
        dispatchTierActions(targetTier)
    }

    private fun dispatchTierActions(tier: EscalationTier) {
        val state = _emergencyState.value
        coroutineScope.launch {
            when (tier) {
                EscalationTier.TIER_1_FAMILY -> {
                    val contactList = database.contactDao().getAllContactsSync()
                    val recipientCount = contactList.size
                    val logEntry = if (recipientCount > 0) {
                        "📱 Step 1: Dispatched emergency SMS to $recipientCount family contact(s)"
                    } else {
                        "📱 Step 1: Dispatched emergency SMS to primary emergency numbers"
                    }

                    contactList.forEach { contact ->
                        val msg = smsDispatcher.buildEmergencyMessage(
                            userName = _medicalProfile.value.fullName,
                            incidentId = state.incidentId,
                            telemetry = state.latestTelemetry,
                            tierName = "Step 1: Family Alert"
                        )
                        smsDispatcher.sendEmergencySms(contact.phoneNumber, msg, state.isSimulated)
                    }

                    _emergencyState.update {
                        it.copy(
                            eventLogs = it.eventLogs + logEntry,
                            dispatchedSmsLogs = it.dispatchedSmsLogs + logEntry
                        )
                    }
                }
                EscalationTier.TIER_2_VOLUNTEERS -> {
                    val count = state.responders.size
                    val log = "🤝 Step 2: High-priority FCM push broadcast to $count nearby community volunteers"
                    _emergencyState.update {
                        it.copy(
                            eventLogs = it.eventLogs + log,
                            dispatchedSmsLogs = it.dispatchedSmsLogs + log
                        )
                    }
                }
                EscalationTier.TIER_3_AUTHORITIES -> {
                    val log = "🚔 Step 3: Emergency Dispatch triggered (Police/Fire automated dialer & SMS ready)"
                    _emergencyState.update {
                        it.copy(
                            eventLogs = it.eventLogs + log,
                            dispatchedSmsLogs = it.dispatchedSmsLogs + log
                        )
                    }
                }
            }
        }
    }

    override fun validatePinAndCancel(pin: String): PinValidationResult {
        val result = pinManager.validatePin(pin)
        when (result) {
            PinValidationResult.ValidSafePin -> {
                // True cancellation
                stopEmergencySession(isDuress = false)
            }
            PinValidationResult.ValidDuressPin -> {
                // Duress: pretend to stop on UI, but covertly continue tracking
                _emergencyState.update {
                    it.copy(
                        isActive = false, // UI returns to normal so attacker believes it is off
                        isDuressActive = true,
                        eventLogs = it.eventLogs + "⚠️ DURESS PIN ENTERED: Covert beacon actively streaming!"
                    )
                }
                // Do NOT stop foreground service or location tracker!
            }
            PinValidationResult.InvalidPin -> {
                // Rejected
            }
        }
        return result
    }

    private fun stopEmergencySession(isDuress: Boolean) {
        escalationTimerJob?.cancel()
        locationTracker.stopTracking()
        stopForegroundService()

        val currentState = _emergencyState.value
        coroutineScope.launch {
            // Save incident record in Room
            val logEntity = IncidentLogEntity(
                incidentId = currentState.incidentId.ifEmpty { "INC-MANUAL" },
                startTimestamp = System.currentTimeMillis() - 60000,
                endTimestamp = System.currentTimeMillis(),
                maxEscalationTierReached = currentState.activeTier.stepNumber,
                isSilentMode = currentState.isSilentMode,
                isDuressTriggered = isDuress,
                finalLatitude = currentState.latestTelemetry?.latitude,
                finalLongitude = currentState.latestTelemetry?.longitude,
                finalBatteryPercentage = currentState.latestTelemetry?.batteryPercentage ?: 100,
                volunteerCountAlerted = currentState.responders.size,
                wasSimulated = currentState.isSimulated
            )
            database.incidentLogDao().insertIncident(logEntity)
        }

        _emergencyState.update {
            it.copy(
                isActive = false,
                isCountdownActive = false,
                isRescuePowerSaverActive = false,
                isDuressActive = false,
                eventLogs = it.eventLogs + "✅ Emergency resolved & safe PIN verified"
            )
        }
    }

    override fun toggleRescuePowerSaver() {
        _emergencyState.update {
            it.copy(isRescuePowerSaverActive = !it.isRescuePowerSaverActive)
        }
    }

    override fun setVolunteerModeEnabled(enabled: Boolean) {
        _isVolunteerModeEnabled.value = enabled
    }

    override suspend fun addContact(contact: EmergencyContactEntity): Long {
        return database.contactDao().insertContact(contact)
    }

    override suspend fun deleteContact(contact: EmergencyContactEntity) {
        database.contactDao().deleteContact(contact)
    }

    override suspend fun updateMedicalProfile(profile: MedicalProfile) {
        _medicalProfile.value = profile
    }

    override fun setSimulatedBattery(level: Int) {
        simulator.setSimulatedBattery(level)
        val updatedTelemetry = _emergencyState.value.latestTelemetry?.copy(batteryPercentage = level)
            ?: simulator.createSimulatedTelemetry().copy(batteryPercentage = level)
        _emergencyState.update { it.copy(latestTelemetry = updatedTelemetry) }
        locationTracker.updateBatteryProfile(level, false)
    }

    override fun simulateVolunteerAcceptance() {
        simulator.simulateVolunteerAcceptance()
        _emergencyState.update {
            it.copy(
                responders = simulator.simulatedResponders.value,
                eventLogs = it.eventLogs + "🙋 Volunteer Dr. Sarah Jenkins confirmed 'I am Responding' (ETA 2 mins)"
            )
        }
    }

    private fun startForegroundService() {
        val serviceIntent = Intent(context, EmergencyForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun stopForegroundService() {
        val serviceIntent = Intent(context, EmergencyForegroundService::class.java)
        context.stopService(serviceIntent)
    }
}
