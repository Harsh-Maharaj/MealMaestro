import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.mealmaestro"
    compileSdk = 34

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

    testOptions {
        unitTests.all {
            systemProperty("robolectric.offline", "true")
            systemProperty("robolectric.dependency-dir", "${rootDir}/robolectric-dependencies")
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
        implementation(libs.firebase.firestore.ktx)
        implementation(libs.firebase.messaging)
        implementation(libs.firebase.functions)
        implementation(libs.firebase.analytics)
        implementation(libs.firebase.config.ktx)
        implementation("com.github.bumptech.glide:glide:4.12.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

        // Test dependencies
        testImplementation(libs.junit)
        testImplementation(libs.robolectric)
        testImplementation(libs.mockito.core)
        testImplementation(libs.mockito.inline)
        testImplementation(libs.androidx.core)
        testImplementation(libs.androidx.junit.v113)

        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }
}
