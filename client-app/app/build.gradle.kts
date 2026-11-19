plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.doch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.doch"
        minSdk = 26 // Исправлено с 36 на 26 для поддержки адаптивных иконок
        targetSdk = 35 // Синхронизировано с compileSdk
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    // ViewModel и LiveData
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.8.3")

    // ZXing для QR-кодов
    implementation ("com.google.zxing:core:3.5.3")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0") // Для сканирования QR-кодов

    // Для работы с RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    // Для уведомлений
    implementation ("androidx.work:work-runtime:2.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}