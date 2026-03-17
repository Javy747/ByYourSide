plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.byyourside"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.byyourside"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
        viewBinding = true
    }
}

dependencies {

    // Dependencias base de AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:34.10.0")) // Versión actualizada y estable

    // Productos de Firebase (sin especificar versión para que BoM las gestione)
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.auth.ktx)
    implementation("com.google.firebase:firebase-firestore")

    implementation("androidx.activity:activity-ktx:1.13.0")

    // Gestor de Librerías
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")

    implementation("com.google.android.gms:play-services-auth:21.3.0")

    //storage para futuras imagenes
    //implementation("com.google.firebase:firebase-storage")

    // Coroutines de Kotlin con la versión estable más reciente
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Gson para serialización
    implementation("com.google.code.gson:gson:2.13.2")

    // Google Play Services para Mapas (sin especificar versión para que BoM las gestione)
   // implementation(libs.play.services.maps)


    // API SplashScreen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.4.0")


    // Dependencias de Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}