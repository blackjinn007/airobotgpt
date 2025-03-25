JAI - Smart Route Tracker

JAI is a powerful Android app that tracks your route using GPS, visualizes it on a map, and provides smart AI-driven insights like distance, speed, and travel tips. Built from scratch on Kali Linux, JAI combines real-time tracking with a sleek black-and-blue UI inspired by Mars exploration. Whether you’re traveling, exploring, or just curious, JAI is your smart companion.
Features

    Real-Time GPS Tracking: Updates your location every 10 seconds (customizable).
    Interactive Map: Displays your route with markers and a blue line using Google Maps.
    Smart AI Analysis: Calculates distance, speed, and offers contextual tips (e.g., "Morning trip? Grab breakfast!") with random travel quotes.
    Route Management: Save, load, and share your routes via text, WhatsApp, or email.
    Custom Settings: Adjust tracking interval to suit your needs.
    Themed UI: Black-and-blue design with a Mars-inspired JAI logo.
    Backend Sync: (Optional) Sends data to a Flask server on Kali Linux for storage.

Screenshots

(Add these after testing!)

    Splash screen with JAI logo
    Map showing a tracked route
    AI analysis with distance and tips

Requirements

    Development Environment: Kali Linux with Android Studio installed.
    Android Device: Android 5.0+ for testing/running the app.
    Google Maps API Key: For map functionality.
    Optional: Flask server on Kali Linux for backend integration.

Setup Instructions
1. Install Tools on Kali Linux

    Java JDK:
    bash

sudo apt update
sudo apt install openjdk-11-jdk
Android Studio:
bash
wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.1.1.28/android-studio-2023.1.1.28-linux.tar.gz
tar -xvzf android-studio-2023.1.1.28-linux.tar.gz
cd android-studio/bin
./studio.sh
Git (for version control):
bash

    sudo apt install git

2. Clone or Create the Project

    If you’re starting fresh:
        Open Android Studio → New Project → Empty Activity → Language: Kotlin → Package: com.example.routetracker.
    If using existing code:
        git clone <your-repo> (if you’ve set up a repo).

3. Add Dependencies

Edit app/build.gradle:
gradle
dependencies {
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
}
4. Configure Google Maps

    Get an API key from Google Cloud Console.
    Add to AndroidManifest.xml:
    xml

    <meta-data
        android:name="com.google.android.gms.maps.API_KEY"
        android:value="YOUR_API_KEY" />

5. Add Permissions

In AndroidManifest.xml:
xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
6. Build and Run

    Connect an Android device via USB or set up an emulator in Android Studio.
    Click "Run" in Android Studio to build and install the app.

7. (Optional) Backend Setup

    On Kali Linux, run a Flask server:
    python

    from flask import Flask, request, jsonify
    import sqlite3

    app = Flask(__name__)

    def init_db():
        conn = sqlite3.connect('route_data.db')
        c = conn.cursor()
        c.execute('''CREATE TABLE IF NOT EXISTS routes (id INTEGER PRIMARY KEY, timestamp REAL, latitude REAL, longitude REAL)''')
        conn.commit()
        conn.close()

    @app.route('/add_location', methods=['POST'])
    def add_location():
        data = request.json
        latitude = data.get('latitude')
        longitude = data.get('longitude')
        timestamp = data.get('timestamp', time.time())
        conn = sqlite3.connect('route_data.db')
        c = conn.cursor()
        c.execute("INSERT INTO routes (timestamp, latitude, longitude) VALUES (?, ?, ?)", (timestamp, latitude, longitude))
        conn.commit()
        conn.close()
        return jsonify({"message": "Location added"})

    if __name__ == '__main__':
        init_db()
        app.run(host='0.0.0.0', port=5000)
    Install Flask: pip3 install flask.
    Update MainActivity.kt with your Kali IP.

How to Use

    Launch: Open the app—see the JAI splash screen, then the main interface.
    Track: Tap "Start" to begin GPS tracking. Move around to log points.
    View: Watch the map update with markers and a route line; read AI insights below.
    Control: Use "Stop" to pause, "Clear" to reset, "Save" to store, "Load" to reload, and "Share" to send your route.
    Customize: Tap "Settings" to adjust the tracking interval (e.g., 5, 10, 20 seconds).

Development Notes

    Built On: Kali Linux, March 2025.
    Language: Kotlin.
    Purpose: Created by [Your Name] for smart route tracking and a $200,000 Kickstarter goal.

Future Plans

    Add route suggestions (e.g., "Turn left in 500m").
    Integrate weather data for travel tips.
    Support multiple users via cloud sync.

Contributing

    Feedback? Test the app and let me know!
    Want to help? Join me
