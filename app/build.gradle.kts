plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.localjobs"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.localjobs"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Voyager for navigation
    implementation("cafe.adriel.voyager:voyager-navigator:1.0.0-rc06")
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:1.0.0-rc06")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Datastore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Lifecycle Runtime
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Mapbox

    implementation ("org.osmdroid:osmdroid-android:6.1.14")
    implementation ("com.google.android.gms:play-services-maps:17.0.0")
    implementation ("com.google.android.gms:play-services-location:17.0.0")




    // Play Services
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Compose Material
    implementation("androidx.compose.material:material-icons-extended:1.5.0")

    // Koin for dependency injection
    implementation("io.insert-koin:koin-android:3.5.0")
    implementation("io.insert-koin:koin-androidx-compose:3.5.0")
    implementation("io.insert-koin:koin-androidx-workmanager:3.5.0") // Optional

    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.play.services.location)
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.30.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



}
