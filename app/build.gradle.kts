import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "coil3.annotation.ExperimentalCoilApi",
            "androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }
}

configure<ApplicationExtension> {
    namespace = "com.projectdreams.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.projectdreams.app"
        minSdk {
            version = release(26)
        }
        targetSdk {
            version = release(36)
        }

        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation(libs.androidx.ui.tooling)

    // Google Fonts
    implementation("androidx.compose.ui:ui-text-google-fonts")

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // HTTP
    implementation(libs.squareup.okhttp)

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // GPlayApi
    implementation(libs.auroraoss.gplayapi)

    // Lib-SU (root)
    implementation(libs.github.topjohnwu.libsu)

    // Shizuku
    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)
}
