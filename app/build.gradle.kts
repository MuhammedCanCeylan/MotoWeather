plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.canceylan.motoweather" // Kendi paket adın kalabilir
    compileSdk = 34 // 36 Hatası için bunu 34'e çekiyoruz (Güvenli Liman)

    defaultConfig {
        applicationId = "com.canceylan.motoweather"
        minSdk = 24
        targetSdk = 34 // Bunu da 34 yap
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        // HATA BURADAYDI: 21 yerine 17 yapıyoruz
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // ASIL KATİL BURASI: "21" Kotlin 1.9.0'da çalışmaz. "17" yapıyoruz.
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Temel Android Kütüphaneleri (Manuel Sürümler)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Test Kütüphaneleri
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // --- SENİN PROJEN İÇİN GEREKLİ OLANLAR (Bunlar eksikse ekle) ---
    // Eğer kodunda Lottie veya Volley kullanıyorsan bunları da eklemezsen hata alırsın:
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("com.android.volley:volley:1.2.1")
    // GSON (JSON işlemleri için kullanıyorsan):
    implementation("com.google.code.gson:gson:2.10.1")


    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.android.volley:volley:1.2.1")
}