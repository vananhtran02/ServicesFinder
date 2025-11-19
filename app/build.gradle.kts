plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.4" apply false
}

android {
    namespace = "edu.sjsu.android.servicesfinder"
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.sjsu.android.servicesfinder"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
}

// Apply plugin
apply(plugin = "com.google.gms.google-services")

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.firestore)
    implementation(libs.firebase.ui.firestore)
    // Firebase Storage
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room components
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)


    implementation (libs.glide)
    annotationProcessor (libs.glide.compiler)




}