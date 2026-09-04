plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // ফায়ারবেস গুগল সার্ভিস প্লাগিন
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sabbirsamol.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sabbirsamol.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    
    // নতুন এবং আপডেট করা পিডিএফ ভিউয়ার লাইব্রেরি (যা বিল্ড এরর সমাধান করবে)
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
    
    // Coroutines (ব্যাকগ্রাউন্ড কাজের জন্য)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // ================= ফায়ারবেস ও গুগল সাইন-ইন ডিপেন্ডেন্সি =================
    // Firebase BoM (ভার্সন কন্ট্রোল)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firebase Authentication & Firestore (জিমেইল ও ক্লাউড ডাটা ব্যাকআপের জন্য)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    
    // Google Play Services Auth (আসল জিমেইল সাইন-ইন স্ক্রিনের জন্য)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("org.json:json:20210307")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
