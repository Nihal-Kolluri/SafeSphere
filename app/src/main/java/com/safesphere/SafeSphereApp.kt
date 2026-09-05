package com.safesphere

import android.app.Application
import com.safesphere.core.battery.BatteryMonitor
import com.safesphere.core.battery.PowerSaverController
import com.safesphere.core.security.SecurityPinManager
import com.safesphere.core.service.ServiceNotificationHelper
import com.safesphere.data.local.AppDatabase
import com.safesphere.data.local.entity.EmergencyContactEntity
import com.safesphere.data.repository.EmergencyRepository
import com.safesphere.data.repository.EmergencyRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SafeSphereApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: EmergencyRepository
        private set

    lateinit var powerSaverController: PowerSaverController
        private set

    lateinit var notificationHelper: ServiceNotificationHelper
        private set

    lateinit var pinManager: SecurityPinManager
        private set

    lateinit var batteryMonitor: BatteryMonitor
        private set

    companion object {
        lateinit var instance: SafeSphereApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        pinManager = SecurityPinManager(this)
        powerSaverController = PowerSaverController(this)
        notificationHelper = ServiceNotificationHelper(this)
        batteryMonitor = BatteryMonitor(this)

        repository = EmergencyRepositoryImpl(
            context = this,
            database = database,
            pinManager = pinManager,
            batteryMonitor = batteryMonitor
        )

        // Seed initial mock contacts if database is fresh
        CoroutineScope(Dispatchers.IO).launch {
            if (database.contactDao().getContactCount() == 0) {
                database.contactDao().insertContact(
                    EmergencyContactEntity(
                        name = "Sarah Miller",
                        phoneNumber = "+1 (555) 234-5678",
                        relationship = "Mother / Primary",
                        isPrimary = true
                    )
                )
                database.contactDao().insertContact(
                    EmergencyContactEntity(
                        name = "David Miller",
                        phoneNumber = "+1 (555) 876-5432",
                        relationship = "Spouse",
                        isPrimary = false
                    )
                )
                database.contactDao().insertContact(
                    EmergencyContactEntity(
                        name = "Local Emergency Response",
                        phoneNumber = "911",
                        relationship = "Public Safety Dispatch",
                        isPrimary = false
                    )
                )
            }
        }
    }
}
