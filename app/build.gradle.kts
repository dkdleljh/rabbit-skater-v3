plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.devbyzenith.rabbitskater"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.devbyzenith.rabbitskater"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "3.0.2"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main") {
            // 실제 게임 에셋은 앱 모듈 밖의 저장소 루트에 있습니다.
            assets.srcDirs(rootProject.file("assets"))
        }
    }

    buildFeatures {
        viewBinding = false
    }

    testOptions {
        unitTests.all {
            // 실제 배포 에셋으로 파서 회귀를 검증합니다.
            it.systemProperty("rabbitSkater.assetsDir", rootProject.file("assets").absolutePath)
        }
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Activity & Lifecycle
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
