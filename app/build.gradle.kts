import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.mealmaestro"
    compileSdk = 34

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
        }
    }


    defaultConfig {
        applicationId = "com.example.mealmaestro"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.cronet.embedded)

    // Additional dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.json:json:20210307")
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.auth)
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.json)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config.ktx)
    implementation (libs.google.google.auth.library.oauth2.http) // notification
    implementation (libs.json.v20201115) // notification
    implementation (libs.org.jetbrains.kotlinx.kotlinx.coroutines.core) // to run in the background
    implementation (libs.kotlinx.coroutines.android) // to run in the background





    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

