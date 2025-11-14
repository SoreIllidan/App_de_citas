plugins {
    id("com.android.application")
    // No apliques aquí directamente google-services para evitar el fallo cuando falte el json
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pruebat2moviles"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pruebat2moviles"
        minSdk = 26
        targetSdk = 36
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
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    // activity 1.11.0 exige compileSdk 36: si ya lo tienes, puedes subir a 1.11.0
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.android.volley:volley:1.2.1")

    // Firebase (FCM). Esto puede compilar sin el plugin, pero no habrá config hasta poner el json.
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-messaging")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// Aplica google-services SOLO si existe el archivo (evita el fallo del task processDebugGoogleServices)
val hasGsJson = file("google-services.json").exists()
        || file("src/debug/google-services.json").exists()
        || file("src/release/google-services.json").exists()

if (hasGsJson) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn("google-services.json no encontrado; se omite el plugin com.google.gms.google-services. FCM quedará deshabilitado en este build.")
}