plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    id("com.google.relay") version "0.3.12"
}

android {
    namespace = "com.example.lazy_susan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lazy_susan"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation("androidx.navigation:navigation-compose:$2.8.9")
    implementation(libs.androidx.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.room.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    //user authentication
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    implementation("androidx.appcompat:appcompat:1.4.0")
    // Networking (OkHttp + Retrofit)
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // For JSON parsing

    // JSON Parsing (Gson)
    implementation("com.google.code.gson:gson:2.8.9")

    // Jetpack Compose for Lists (LazyColumn)
    implementation("androidx.compose.foundation:foundation:1.5.0")

    // RecyclerView (if not using Compose)
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation ("com.google.code.gson:gson:2.8.8")   // To parse JSON

    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.4.4")
    implementation("com.google.firebase:firebase-auth-ktx")


}