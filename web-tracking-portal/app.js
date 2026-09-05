// SafeSphere Live Rescue Portal Client
(function () {
  const urlParams = new URLSearchParams(window.location.search);
  const incidentId = urlParams.get('incidentId') || urlParams.get('token') || 'INC-7842A';
  let lat = parseFloat(urlParams.get('lat')) || 37.7749;
  let lng = parseFloat(urlParams.get('lng')) || -122.4194;
  let battery = parseInt(urlParams.get('bat')) || 14;

  // DOM Elements
  const incidentIdText = document.getElementById('incidentIdText');
  const batteryValue = document.getElementById('batteryValue');
  const powerModeSub = document.getElementById('powerModeSub');
  const speedValue = document.getElementById('speedValue');
  const latValue = document.getElementById('latValue');
  const lngValue = document.getElementById('lngValue');
  const mapsDirectionsBtn = document.getElementById('mapsDirectionsBtn');
  const respondBtn = document.getElementById('respondBtn');

  incidentIdText.textContent = incidentId;
  updateTelemetryUI(lat, lng, battery, 0);

  // Initialize Leaflet Map
  const map = L.map('map', {
    zoomControl: true
  }).setView([lat, lng], 16);

  // Dark-themed tiles from CartoDB
  L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://carto.com/">CARTO</a> &copy; OpenStreetMap'
  }).addTo(map);

  // Pulsing custom rescue marker icon
  const rescueIcon = L.divIcon({
    className: 'custom-rescue-marker',
    html: `
      <div style="position: relative; width: 32px; height: 32px;">
        <div style="position: absolute; width: 32px; height: 32px; background: rgba(255, 51, 68, 0.4); border-radius: 50%; animation: pulse-ring 1.2s infinite;"></div>
        <div style="position: absolute; top: 6px; left: 6px; width: 20px; height: 20px; background: #ff3344; border: 3px solid #ffffff; border-radius: 50%; box-shadow: 0 2px 8px rgba(0,0,0,0.5);"></div>
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  });

  const marker = L.marker([lat, lng], { icon: rescueIcon }).addTo(map);
  marker.bindPopup(`<b>🚨 Active Distress Signal</b><br>Incident: ${incidentId}<br>Battery: ${battery}%`).openPopup();

  // Accuracy circle
  const accuracyCircle = L.circle([lat, lng], {
    color: '#ff3344',
    fillColor: '#ff3344',
    fillOpacity: 0.15,
    radius: 25
  }).addTo(map);

  // Breadcrumbs polyline
  const breadcrumbs = L.polyline([[lat, lng]], {
    color: '#ffaa00',
    weight: 4,
    dashArray: '5, 10'
  }).addTo(map);

  function updateTelemetryUI(newLat, newLng, newBat, newSpeed) {
    latValue.textContent = newLat.toFixed(5);
    lngValue.textContent = newLng.toFixed(5);
    batteryValue.textContent = newBat + '%';
    speedValue.textContent = newSpeed.toFixed(1) + ' km/h';

    if (newBat <= 15) {
      powerModeSub.textContent = 'Ultra-Survival Throttle (120s)';
      batteryValue.className = 'metric-value danger';
    } else if (newBat <= 50) {
      powerModeSub.textContent = 'Adaptive Burst Throttle';
      batteryValue.className = 'metric-value';
    } else {
      powerModeSub.textContent = 'High Precision Stream';
      batteryValue.className = 'metric-value';
    }

    mapsDirectionsBtn.href = `https://www.google.com/maps/dir/?api=1&destination=${newLat},${newLng}`;
  }

  // Simulated live telemetry movement for preview
  setInterval(() => {
    // Add tiny simulated jitter to show live heartbeat
    const jitterLat = (Math.random() - 0.5) * 0.0001;
    const jitterLng = (Math.random() - 0.5) * 0.0001;
    lat += jitterLat;
    lng += jitterLng;

    marker.setLatLng([lat, lng]);
    accuracyCircle.setLatLng([lat, lng]);
    breadcrumbs.addLatLng([lat, lng]);
    updateTelemetryUI(lat, lng, battery, Math.random() * 2.5);
  }, 4000);

  // First Responder Acknowledgment Action
  respondBtn.addEventListener('click', function () {
    respondBtn.textContent = '✅ Responding — Status Broadcasted!';
    respondBtn.style.backgroundColor = '#00e676';
    respondBtn.style.color = '#000000';
    alert(`Thank you for responding! The victim and family have been notified that you are en route to ${lat.toFixed(4)}, ${lng.toFixed(4)}.`);
  });
})();
