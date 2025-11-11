import com.google.gson.annotations.SerializedName
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("com.chaquo.python")
}

val propertiesFile = rootProject.file("server.properties")
val properties = Properties()

if (propertiesFile.exists()) {
    properties.load(FileInputStream(propertiesFile))
}


android {
    namespace = "kr.co.uxn.agms_p"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.co.uxn.agms_p"
        minSdk = 29
        targetSdk = 35
        versionCode = 24
        versionName = "0.3.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "kakao_native_app_key",
            properties.getProperty("kakao_native_app_key")
        )
        resValue("string", "kakao_oauth_host", properties.getProperty("kakao_oauth_host"))
        buildConfigField(
            "String",
            "google_web_client_id",
            properties.getProperty("google_web_client_id")
        )
        buildConfigField("String", "base_url", properties.getProperty("base_url"))

        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        chaquopy {
            defaultConfig {
                version = "3.8"
                pip {
                    // A requirement specifier, with or without a version number:
                    install("scipy")
//                    install("requests==2.24.0")

                    // An sdist or wheel filename, relative to the project directory:
//                    install("MyPackage-1.2.3-py2.py3-none-any.whl")

                    // A directory containing a setup.py, relative to the project
                    // directory (must contain at least one slash):
                    install("numpy")

                    // "-r"` followed by a requirements filename, relative to the
                    // project directory:
//                    install("-r", "requirements.txt")
                    install("pandas")
                    install("configparser")
                }
            }
        }
    }

    chaquopy {
        sourceSets {
            getByName("main") {
                srcDir("src/main/python")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Coil
    implementation("io.coil-kt:coil:2.2.2")
    implementation("io.coil-kt:coil-compose:2.2.2")

    // 카카오 API
    implementation("com.kakao.sdk:v2-all:2.20.6") // 전체 모듈 설치, 2.11.0 버전부터 지원
    implementation("com.kakao.sdk:v2-user:2.20.6") // 카카오 로그인 API 모듈
    implementation("com.kakao.sdk:v2-auth:2.20.6") // 인증 관련 SDK 추가
    // 구글 API
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // navigation
    implementation("androidx.navigation:navigation-compose:2.8.8")
    // splash api
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // okhttp3
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // GSON
    implementation("com.google.code.gson:gson:2.10.1")
    // Scalar
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // Coroutine
    implementation("io.github.ParkSangGwon:tedpermission-coroutine:3.4.2")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")

    // numberpicker
    implementation("com.chargemap.compose:numberpicker:1.0.3")

    // datastore
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // timber
//    implementation ("com.jakewharton.timber:timber:5.0.1")

    // rottie
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // room
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // To use Kotlin annotation processing tool (kapt)
    // kapt("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // workManager
    val workVersion = "2.10.0"
    implementation("androidx.work:work-runtime-ktx:${workVersion}")

//    val vicoVersion = "1.11.1"
//    implementation("com.patrykandpatrick.vico:core:$vicoVersion")
//    implementation("com.patrykandpatrick.vico:compose:$vicoVersion")
//    implementation("com.patrykandpatrick.vico:compose-m2:$vicoVersion")
//    implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion")

    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.multiplatform)
    implementation(libs.vico.views)

    // 앱 업데이트 확인
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // 권한 meticha library
    implementation(libs.permissions.compose)

    // timepicker library
    implementation("com.github.commandiron:WheelPickerCompose:1.1.11")

}