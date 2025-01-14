plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    `maven-publish`
}

android {
    namespace = "com.elytelabs.dialoghub"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)

    implementation(libs.toolbox)
}

// Maven publishing configuration
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.elytelabs.dialoghub"
            artifactId = "dialoghub"
            version = "1.1.4"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("DialogHub")
                description.set("Dialogs Library")
                url.set("https://github.com/elytelabs/dialoghub")

                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}