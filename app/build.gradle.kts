
plugins {
    alias(libs.plugins.android.application)

    // Firebase Google Services
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.tshwaneconnect"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.tshwaneconnect"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // EXISTING ANDROID DEPENDENCIES
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // FIREBASE BILL OF MATERIALS
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // FIREBASE AUTHENTICATION
    implementation("com.google.firebase:firebase-auth")

    // CLOUD FIRESTORE DATABASE
    implementation("com.google.firebase:firebase-firestore")

    // TEST DEPENDENCIES
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
