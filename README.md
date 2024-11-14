# Mutual Fund Scheme NAV Data

This Android application retrieves mutual fund scheme NAV (Net Asset Value) data from a public API, allowing users to access both current and historical NAV information. The app uses **Retrofit** to fetch data in JSON format and **Jetpack Compose** to display the data in a table. This data is fetched directly from the API and is not stored locally, so an internet connection is required.

## Features

- **Fetch and display NAV data**: Retrieve real-time as well as historical NAV data for mutual fund schemes.
- **View in a table format**: Data is presented in an easy-to-read table format using **Jetpack Compose**.
- **Dependency Injection with Hilt**: Leverages Hilt for dependency management, making the code modular and testable.
- **Simple to Extend**: Easily store data in a local database for offline access (optional).

## Libraries and Dependencies

The following dependencies are used in this project:

- **build.gradle.kts(app)**
  ```kotlin
    plugins {
        alias(libs.plugins.androidApplication)
        alias(libs.plugins.jetbrainsKotlinAndroid)
        // Apply the Hilt plugin
        id("dagger.hilt.android.plugin")
        // Apply the Kotlin Kapt plugin for annotation processing
        id("kotlin-kapt")
    }

- **Hilt for Dependency Injection**
  ```kotlin
  implementation("com.google.dagger:hilt-android:2.48")
  kapt("com.google.dagger:hilt-compiler:2.48")
  implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

- **Lifecycle Components**
  ```kotlin
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

- **Retrofit for Networking**
  ```kotlin
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")

- **Jetpack Compose and Material 3 for UI**
  ```kotlin
  implementation(platform("androidx.compose:compose-bom:2024.02.00"))
  implementation("androidx.compose.animation:animation:1.6.1")
  implementation("androidx.compose.material3:material3:1.2.0")

- **Requirements**
Internet Connection: This app requires an internet connection to fetch data from the API.
Permissions: Make sure to add the following permission in your AndroidManifest.xml:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />

- **Future Enhancements**
Database Storage: Optionally, you can extend this project to store fetched data in a local database (e.g., Room) and retrieve it for offline access.

## Setup and Installation

1. **Clone the Repository:**
    ```bash
    git clone https://github.com/jayptel/MFAPIJSONData.git
    ```

2. **Open the Project in Android Studio.**

3. **Configure API Access:**
   - Update the `baseUrl` in `RetrofitInstance` with the actual endpoint.
   - Add any required API keys or tokens, if applicable.

4. **Add Internet Permission:**
   Ensure the following permission is in your `AndroidManifest.xml`:
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    ```

5. **Build and Run the App.**


## Contributing

Contributions are welcome! Please follow these steps to contribute:

1. **Fork the Repository.**

2. **Create a New Branch:**
    ```bash
    git checkout -b feature/YourFeatureName
    ```

3. **Commit Your Changes:**
    ```bash
    git commit -m "Add some feature"
    ```

4. **Push to the Branch:**
    ```bash
    git push origin feature/YourFeatureName
    ```

5. **Open a Pull Request.**
