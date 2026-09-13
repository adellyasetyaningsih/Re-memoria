import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.finalproject"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.finalproject"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔥 Load API keys and secrets from local.properties (preferred) or gradle.properties
        val props = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            props.load(localPropsFile.inputStream())
        }
        val gradlePropsFile = rootProject.file("gradle.properties")
        if (gradlePropsFile.exists()) {
            val gProps = Properties()
            gProps.load(gradlePropsFile.inputStream())
            for (key in listOf("GROQ_API_KEY", "SUPABASE_URL", "SUPABASE_KEY")) {
                if (props.getProperty(key).isNullOrEmpty() && gProps.containsKey(key)) {
                    props[key] = gProps[key]
                }
            }
        }
        val groqKey = props.getProperty("GROQ_API_KEY") ?: ""
        val supabaseUrl = props.getProperty("SUPABASE_URL") ?: ""
        val supabaseKey = props.getProperty("SUPABASE_KEY") ?: ""

        buildConfigField("String", "GROQ_API_KEY", "\"$groqKey\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
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
        buildConfig = true
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

    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
