plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 30
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

               buildFeatures {
               viewBinding = false
           }
           
           packaging {
               resources {
                   excludes += "/META-INF/{AL2.0,LGPL2.1}"
                   pickFirsts += "META-INF/INDEX.LIST"
                   pickFirsts += "META-INF/io.netty.versions.properties"
               }
           }
}

       dependencies {
           testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
           testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

           implementation(libs.appcompat)
           implementation(libs.material)
           implementation(libs.constraintlayout)
           implementation(libs.navigation.fragment)
           implementation(libs.navigation.ui)
           
           // Netty 통신을 위한 의존성 (필요한 모듈만)
           implementation("io.netty:netty-buffer:4.1.122.Final")
           implementation("io.netty:netty-transport:4.1.122.Final")
           implementation("io.netty:netty-handler:4.1.122.Final")
           implementation("io.netty:netty-codec:4.1.122.Final")
       }

tasks.withType<Test> {
    useJUnitPlatform()
}