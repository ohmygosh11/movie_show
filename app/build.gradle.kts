plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

    buildFeatures {
        dataBinding = true
    }
    buildToolsVersion = "30.0.3"

}



dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//  Material Design
    implementation(libs.material.v121)
//  Retrofit & GSON
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
//  Picasso
    implementation(libs.picasso)
//  Lifecycle Extensions
    implementation(libs.lifecycle.extensions)
//  Room & RxJava Support
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.rxjava2)
//  RxJava
    implementation (libs.rxandroid)
//  Scalable size units
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
//  Rounded Image View
    implementation(libs.roundedimageview)
//    ExoPlayer
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
//    OkHttp
    implementation(libs.okhttp)
}