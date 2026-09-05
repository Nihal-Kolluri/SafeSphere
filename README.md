# SafeSphere 🚨

**Smart Emergency Alert, Rescue Optimization & Volunteer Coordination System**

SafeSphere is a Native Android emergency response system engineered in Kotlin and Jetpack Compose. It bridges the gap between victims, family members, nearby community volunteers, and official emergency services (police, fire department, and paramedics).

---

## Key Features

1. **3-Step Tiered Escalation Pipeline**:
   - **Step 1: Family & Trusted Contacts (30s Timeout)**: Automated direct cellular SMS dispatch containing GPS coordinates, Google Maps navigation link, and the live rescue portal link.
   - **Step 2: Nearby Community Volunteers (60s Timeout)**: Push broadcast to opt-in community first responders within 1–2 km with live turn-by-turn routing and an **"I am Responding"** coordination button.
   - **Step 3: Official Emergency Authorities**: Police, Fire Department, and Ambulance auto-dialer prompt and official dispatch SMS.
   - **One-Tap Manual Skip**: Users can bypass timeouts and escalate directly to Step 3 at any second.

2. **In-App "Rescue Power Saver" Mode**:
   - Switches the UI into a pure AMOLED pitch-black theme (`#000000`).
   - Dims screen brightness to 1% (`0.01f`).
   - Throttles background GPS radio cycles into smart burst intervals.
   - Provides a one-tap shortcut to native Android Battery Saver settings.

3. **Battery-Aware Smart Adaptive Telemetry**:
   - **Battery > 50%**: High-precision continuous streaming (every 3–5 seconds).
   - **Battery 15%–50%**: Dynamic adaptive burst tracking (every 10–15s when moving, 60s when stationary).
   - **Battery < 15%**: Critical ultra-survival mode (burst updates every 120s) with final known coordinates caching.

4. **Dual Trigger Modes**:
   - **Standard SOS**: Pulsing Big Red SOS button + Physical Shake gesture with a 5-second countdown to cancel false alarms.
   - **Silent / Discreet SOS**: Stealth volume button sequence (3 rapid presses) that broadcasts live GPS and alerts silently without siren, vibration, or bright screens.

5. **Dual-PIN Security (Safe PIN vs. Duress PIN)**:
   - **Safe PIN (`1234`)**: Truly deactivates the emergency session and notifies contacts.
   - **Duress PIN (`9999`)**: If forced by an attacker, fakes cancellation on screen (returns to normal UI) while secretly continuing background tracking and flagging a coercion alert to responders!

6. **Web Live Tracking Portal**:
   - Interactive Leaflet.js map with open-source CartoDB / OpenStreetMap tiles.
   - Accessible via SMS link without requiring app installation.
   - Displays real-time pulsing beacon, breadcrumb trail, speed, battery gauge, and responder acknowledgment.

7. **Built-in Safe Simulation & Test Lab**:
   - Safely test the entire escalation pipeline, low-battery throttling, and mock responder dispatch without sending real cellular SMS or calling 911.

---

## Project Architecture

```
d:/safesphere/
├── app/
│   ├── src/main/java/com/safesphere/
│   │   ├── SafeSphereApp.kt             # Application Singleton & DI
│   │   ├── core/
│   │   │   ├── battery/                 # BatteryMonitor & PowerSaverController
│   │   │   ├── location/                # AdaptiveLocationTracker (Battery-calibrated GPS)
│   │   │   ├── security/                # SecurityPinManager (Safe & Duress PINs)
│   │   │   ├── sensors/                 # ShakeDetector & VolumeChordDetector
│   │   │   ├── service/                 # EmergencyForegroundService & Notifications
│   │   │   └── simulation/              # EmergencySimulator & MockVolunteerGenerator
│   │   ├── data/
│   │   │   ├── local/                   # Room Database (Contacts & Incident Logs)
│   │   │   ├── model/                   # StateFlow models & EscalationTier enums
│   │   │   └── repository/              # EmergencyRepository & SmsDispatcher
│   │   └── ui/
│   │       ├── MainActivity.kt          # Host Activity & Volume key interceptor
│   │       ├── theme/                   # Material 3 dark/high-contrast emergency theme
│   │       └── screens/
│   │           ├── home/                # Big SOS Button & 5s countdown
│   │           ├── emergency/           # Active alert & AMOLED Rescue Power Saver
│   │           ├── volunteer/           # Community Responder Dashboard
│   │           ├── contacts/            # Family Contacts & ICE Medical ID
│   │           └── simulation/          # Test Lab & Fake Battery Slider
└── web-tracking-portal/
    ├── index.html                       # Responsive Leaflet.js tracking map
    ├── styles.css                       # Emergency dark theme
    └── app.js                           # Real-time marker, breadcrumbs & telemetry
```

---

## How to Build & Run

### 1. Open in Android Studio
1. Launch **Android Studio** (Hedgehog / Iguana / Ladybug or newer).
2. Select **Open** and choose `d:\safesphere`.
3. Let Gradle sync project dependencies.

### 2. Run on Device or Emulator
1. Connect an Android device (Android 8.0 / API 26+) or launch an Android Virtual Device (AVD).
2. Tap **Run ▶** to install and launch `SafeSphere`.

### 3. Open Web Live Tracking Portal
- Open `d:\safesphere\web-tracking-portal\index.html` in any web browser (Chrome, Edge, Firefox, Safari).
- You can pass query parameters:
  `index.html?incidentId=INC-9912&lat=37.7749&lng=-122.4194&bat=14`

---

## Default Security PINs
- **Safe PIN**: `1234`
- **Duress PIN**: `9999`
