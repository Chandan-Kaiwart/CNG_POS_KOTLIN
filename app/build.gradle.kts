plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.rsgl.cngpos"
    compileSdk = 36
    packagingOptions {
        resources {
            excludes += "META-INF/versions/**"
        }
    }

    defaultConfig {
        applicationId = "com.rsgl.cngpos"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField ("String", "PHONEPE_MERCHANT_ID", "\"M23VAIS6MPRP8\"")
        buildConfigField ("String", "PHONEPE_APP_ID", "\"SU2511111301182712747921\"")
        buildConfigField ("String", "PHONEPE_SALT_KEY", "\"cbc109b2-2036-4e2b-92d0-358f686d08da\"")
        buildConfigField ("String", "API_BASE_URL", "\"https://www.cng-suvidha.in/CNGPortal/\"")
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
    buildFeatures{
        compose = true
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (platform("androidx.compose:compose-bom:2025.12.01"))
    implementation (libs.androidx.compose.ui)
    implementation (libs.androidx.compose.material3)
    implementation (libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
// ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
// Networking
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
// Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
// WebView
    implementation("com.google.accompanist:accompanist-webview:0.32.0")
// Gson
    implementation("com.google.code.gson:gson:2.13.2")

    implementation("phonepe.intentsdk.android.release:IntentSDK:5.3.0")
}