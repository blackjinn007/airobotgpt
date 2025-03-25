package com.example.jai

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val routeList = mutableListOf<String>()
    private lateinit var routeText: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        routeText = findViewById(R.id.route_text)

        // Check and request location permission
        if (checkLocationPermission()) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                routeText.text = "Location permission denied. Enable it in settings."
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val point = "Time: $time, Latitude: ${it.latitude}, Longitude: ${it.longitude}"
                routeList.add(point)
                updateRouteDisplay()
            }
        }
    }

    private fun updateRouteDisplay() {
        val displayText = "Your Route:\n${routeList.joinToString("\n")}\n\nJAI Analysis: ${analyzeRoute()}"
        routeText.text = displayText
    }

    // Simple AI analysis (Grok-style)
    private fun analyzeRoute(): String {
        return if (routeList.isEmpty()) {
            "Your route is empty. Where are you heading?"
        } else {
            "You have ${routeList.size} points in your route. Safe travels!"
        }
    }
}

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //  (fusedLocationClient, routeText)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun updateMap(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(latLng).title("You are here"))
        mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    // startRealTimeTracking-l updateMap(location) call
}

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

private lateinit var locationCallback: LocationCallback

private fun startRealTimeTracking() {
    val locationRequest = LocationRequest.create().apply {
        interval = 10000 // 10 seconds
        fastestInterval = 5000 // 5 seconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val point = "Time: $time, Lat: ${location.latitude}, Lon: ${location.longitude}"
                routeList.add(point)
                updateRouteDisplay()
            }
        }
    }

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
}

override fun onDestroy() {
    super.onDestroy()
    fusedLocationClient.removeLocationUpdates(locationCallback) // Clean up
}

private fun sendToBackend(latitude: Double, longitude: Double) {
    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("latitude", latitude)
        put("longitude", longitude)
    }
    val body = RequestBody.create(MediaType.parse("application/json"), json.toString())
    val request = Request.Builder()
        .url("http://<നിന്റെ_Kali_IP>:5000/add_location")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread { routeText.append("\nBackend error: ${e.message}") }
        }
        override fun onResponse(call: Call, response: Response) {
            runOnUiThread { routeText.append("\nBackend: ${response.body()?.string()}") }
        }
    })
}

private fun analyzeRoute(): String {
    if (routeList.isEmpty()) {
        return "Your route is empty. Where are you heading?"
    }
    val points = routeList.size
    val firstTime = routeList.first().substringAfter("Time: ").substringBefore(",")
    val lastTime = routeList.last().substringAfter("Time: ").substringBefore(",")
    return "You have $points points. Traveled from $firstTime to $lastTime. What's next?"
}

package com.example.routetracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val routeList = mutableListOf<Pair<Location, String>>() // Store Location + Time
    private lateinit var routeText: TextView
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        routeText = findViewById(R.id.route_text)

        // Check and request location permission
        if (checkLocationPermission()) {
            startRealTimeTracking()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRealTimeTracking()
        } else {
            routeText.text = "Location permission denied. Enable it in settings to use JAI."
        }
    }

    private fun startRealTimeTracking() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update every 10 seconds
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    routeList.add(Pair(location, time))
                    updateRouteDisplay()
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun updateRouteDisplay() {
        val routeSummary = routeList.joinToString("\n") { "Time: ${it.second}, Lat: ${it.first.latitude}, Lon: ${it.first.longitude}" }
        val displayText = "Your Route:\n$routeSummary\n\nJAI Analysis:\n${analyzeRoute()}"
        routeText.text = displayText
    }

    // Improved AI Analysis (Grok-style)
    private fun analyzeRoute(): String {
        if (routeList.isEmpty()) {
            return "Hey there! Your route is empty. Where are you planning to go today?"
        }

        val points = routeList.size
        if (points == 1) {
            return "You've just started with 1 point. Looks like you're on the move! Where to next?"
        }

        // Calculate total distance traveled (in kilometers)
        var totalDistance = 0.0
        for (i in 0 until routeList.size - 1) {
            val start = routeList[i].first
            val end = routeList[i + 1].first
            totalDistance += start.distanceTo(end) / 1000 // Convert meters to kilometers
        }

        // Time difference between first and last point
        val firstTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(routeList.first().second)
        val lastTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(routeList.last().second)
        val timeDiffMinutes = (lastTime.time - firstTime.time) / (1000 * 60) // Minutes

        // Grok-style analysis
        val analysis = buildString {
            append("Nice going! You've tracked $points points.\n")
            append("Total distance: ${"%.2f".format(totalDistance)} km.\n")
            if (timeDiffMinutes > 0) {
                append("You've been traveling for $timeDiffMinutes minutes.\n")
                val speed = totalDistance / (timeDiffMinutes / 60.0) // km/h
                append("Average speed: ${"%.1f".format(speed)} km/h.\n")
            }
            append("What's your next stop? Need help planning?")
        }
        return analysis
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Clean up
    }
}

<ImageView
android:layout_width="100dp"
android:layout_height="100dp"
android:src="@drawable/jai_logo"
android:layout_gravity="center_horizontal"
android:layout_marginBottom="16dp" />

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Other existing code (fusedLocationClient, routeText, etc.)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun updateMap(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(latLng).title("JAI - You are here"))
        mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onLocationResult(locationResult: LocationResult) {
        for (location in locationResult.locations) {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            routeList.add(Pair(location, time))
            updateRouteDisplay()
            updateMap(location) // Update map with each new location
        }
    }
}
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

private fun sendToBackend(latitude: Double, longitude: Double) {
    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("latitude", latitude)
        put("longitude", longitude)
    }
    val body = RequestBody.create(MediaType.parse("application/json"), json.toString())
    val request = Request.Builder()
        .url("http://<YOUR_KALI_IP>:5000/add_location") // Replace with your Kali IP
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            runOnUiThread { routeText.append("\nError sending to backend: ${e.message}") }
        }
        override fun onResponse(call: Call, response: Response) {
            runOnUiThread { routeText.append("\nBackend response: ${response.body()?.string()}") }
        }
    })
}


override fun onLocationResult(locationResult: LocationResult) {
    for (location in locationResult.locations) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        routeList.add(Pair(location, time))
        updateRouteDisplay()
        updateMap(location)
        sendToBackend(location.latitude, location.longitude) // Send to backend
    }
}

import android.content.Context
import java.io.File

private fun saveRoute() {
    val file = File(getExternalFilesDir(null), "jai_route.txt")
    val routeData = routeList.joinToString("\n") { "Time: ${it.second}, Lat: ${it.first.latitude}, Lon: ${it.first.longitude}" }
    file.writeText(routeData)
    routeText.append("\nRoute saved to ${file.absolutePath}")
}

private fun loadRoute() {
    val file = File(getExternalFilesDir(null), "jai_route.txt")
    if (file.exists()) {
        val savedData = file.readText()
        routeText.text = "Saved Route:\n$savedData\n\nJAI Analysis:\n${analyzeRoute()}"
    } else {
        routeText.append("\nNo saved route found.")
    }
}
<Button
android:id="@+id/save_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Save Route" />

<Button
android:id="@+id/load_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Load Route" />

private fun loadRoute() {
    val file = File(getExternalFilesDir(null), "jai_route.txt")
    if (file.exists()) {
        val savedData = file.readText()
        val lines = savedData.split("\n")
        routeList.clear()
        mMap.clear()
        lines.forEach { line ->
            val parts = line.split(", ")
            if (parts.size >= 3) {
                val lat = parts[1].substringAfter("Lat: ").toDouble()
                val lon = parts[2].substringAfter("Lon: ").toDouble()
                val location = Location("").apply {
                    latitude = lat
                    longitude = lon
                }
                val time = parts[0].substringAfter("Time: ")
                routeList.add(Pair(location, time))
                val latLng = LatLng(lat, lon)
                mMap.addMarker(MarkerOptions().position(latLng).title("JAI - Saved Point"))
            }
        }
        val polylineOptions = PolylineOptions().width(5f).color(android.graphics.Color.BLUE)
        routeList.forEach { polylineOptions.add(LatLng(it.first.latitude, it.first.longitude)) }
        mMap.addPolyline(polylineOptions)
        updateRouteDisplay()
    } else {
        routeText.append("\nNo saved route found.")
    }
}

private fun analyzeRoute(): String {
    if (routeList.isEmpty()) {
        return "Hey there! Your route’s empty. How about a quick trip somewhere fun?"
    }

    val points = routeList.size
    if (points == 1) {
        return "Just one point so far? You’re starting an adventure—where to next?"
    }

    var totalDistance = 0.0
    for (i in 0 until routeList.size - 1) {
        totalDistance += routeList[i].first.distanceTo(routeList[i + 1].first) / 1000
    }

    val firstTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(routeList.first().second)
    val lastTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(routeList.last().second)
    val timeDiffMinutes = (lastTime.time - firstTime.time) / (1000 * 60)
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val analysis = buildString {
        append("Nice going! You’ve tracked $points points.\n")
        append("Distance covered: ${"%.2f".format(totalDistance)} km.\n")
        if (timeDiffMinutes > 0) {
            append("Travel time: $timeDiffMinutes minutes.\n")
            val speed = totalDistance / (timeDiffMinutes / 60.0)
            append("Speed: ${"%.1f".format(speed)} km/h.\n")
        }
        when {
            currentHour in 6..11 -> append("Morning trip? Perfect time for a breakfast stop!")
            currentHour in 12..16 -> append("Midday journey—how about a lunch break?")
            currentHour in 17..21 -> append("Evening travel? Maybe a scenic spot nearby!")
            totalDistance > 5 -> append("Long trek! Consider a rest soon.")
            else -> append("Short and sweet—keep exploring!")
        }
    }
    return analysis
}

<Button
android:id="@+id/start_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Start"
android:backgroundTint="#0000FF"  <!-- Blue -->
android:textColor="#000000" />    <!-- Black -->

import android.content.Intent

private fun shareRoute() {
    val routeSummary = "JAI Route Summary:\n${routeList.joinToString("\n") { "Time: ${it.second}, Lat: ${it.first.latitude}, Lon: ${it.first.longitude}" }}\n\n${analyzeRoute()}"
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, routeSummary)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(shareIntent, "Share your JAI route"))
}
<Button
android:id="@+id/share_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Share"
android:backgroundTint="#0000FF"
android:textColor="#000000" />


private fun startRealTimeTracking() {
    val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                routeList.add(Pair(location, time))
                updateRouteDisplay()
                updateMap(location)
                sendToBackend(location.latitude, location.longitude)
            }
        }
    }

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        routeText.append("\nTracking started. Note: GPS may drain battery—charge up!")
    }
}
<LinearLayout
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:orientation="horizontal"
android:layout_marginTop="8dp">

<Button
android:id="@+id/start_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Start"
android:backgroundTint="#0000FF"
android:textColor="#000000" />

<Button
android:id="@+id/stop_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Stop"
android:backgroundTint="#0000FF"
android:textColor="#000000" />

<Button
android:id="@+id/clear_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Clear"
android:backgroundTint="#0000FF"
android:textColor="#000000" />

<Button
android:id="@+id/save_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Save"
android:backgroundTint="#0000FF"
android:textColor="#000000" />

<Button
android:id="@+id/load_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Load"
android:backgroundTint="#0000FF"
android:textColor="#000000" />

<Button
android:id="@+id/share_button"
android:layout_width="0dp"
android:layout_weight="1"
android:layout_height="wrap_content"
android:text="Share"
android:backgroundTint="#0000FF"
android:textColor="#000000" />
</LinearLayout>


private fun analyzeRoute(): String {
    if (routeList.isEmpty()) {
        return "Hey there! Your route’s empty. How about a quick trip somewhere fun?"
    }

    val points = routeList.size
    if (points == 1) {
        return "Just one point so far? You’re starting an adventure—where to next?"
    }

    var totalDistance = 0.0
    for (i in 0 until routeList.size - 1) {
        totalDistance += routeList[i].first.distanceTo(routeList[i + 1].first) / 1000
    }

    val firstTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(routeList.first().second)
    val lastTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(routeList.last().second)
    val timeDiffMinutes = (lastTime.time - firstTime.time) / (1000 * 60)
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val quotes = listOf(
        "‘The journey is the destination.’ — Keep exploring!",
        "‘Not all who wander are lost.’ — Enjoy the ride!",
        "‘Travel far, travel wide.’ — You’re doing great!"
    )
    val randomQuote = quotes.random()

    val analysis = buildString {
        append("Nice going! You’ve tracked $points points.\n")
        append("Distance covered: ${"%.2f".format(totalDistance)} km.\n")
        if (timeDiffMinutes > 0) {
            append("Travel time: $timeDiffMinutes minutes.\n")
            val speed = totalDistance / (timeDiffMinutes / 60.0)
            append("Speed: ${"%.1f".format(speed)} km/h.\n")
        }
        when {
            currentHour in 6..11 -> append("Morning trip? Perfect time for a breakfast stop!\n")
            currentHour in 12..16 -> append("Midday journey—how about a lunch break?\n")
            currentHour in 17..21 -> append("Evening travel? Maybe a scenic spot nearby!\n")
            totalDistance > 5 -> append("Long trek! Consider a rest soon.\n")
            else -> append("Short and sweet—keep exploring!\n")
        }
        append(randomQuote)
    }
    return analysis
}
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
android:layout_width="match_parent"
android:layout_height="match_parent"
android:background="#000000"  <!-- Black background -->
android:gravity="center"
android:orientation="vertical">

<ImageView
android:layout_width="200dp"
android:layout_height="200dp"
android:src="@drawable/jai_logo" />

<TextView
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="JAI - Smart Route Tracker"
android:textSize="24sp"
android:textColor="#0000FF" />  <!-- Blue text -->
</LinearLayout>


package com.example.routetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Wait 3 seconds, then go to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
private fun startRealTimeTracking() {
    try {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isEmpty()) {
                    routeText.append("\nNo location data—check GPS settings!")
                    return
                }
                for (location in locationResult.locations) {
                    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    routeList.add(Pair(location, time))
                    updateRouteDisplay()
                    updateMap(location)
                    sendToBackend(location.latitude, location.longitude)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                .addOnFailureListener { e ->
                    routeText.append("\nTracking failed: ${e.message}")
                }
            routeText.append("\nTracking started. GPS may drain battery—charge up!")
        }
    } catch (e: Exception) {
        routeText.append("\nError starting tracking: ${e.message}")
    }
}


override fun onLocationResult(locationResult: LocationResult) {
    if (locationResult.locations.isEmpty()) {
        routeText.append("\nNo location data—check GPS settings!")
        return
    }
    for (location in locationResult.locations) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        if (routeList.size >= 100) routeList.removeAt(0) // Keep max 100 points
        routeList.add(Pair(location, time))
        updateRouteDisplay()
        updateMap(location)
        sendToBackend(location.latitude, location.longitude)
    }
}

