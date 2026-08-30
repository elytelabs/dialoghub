plugins {
    alias(libs.plugins.android.library)

    `maven-publish`
}

android {
    namespace = "com.elytelabs.dialoghub"
    compileSdk = 37

    defaultConfig {
        minSdk = 25

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
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
afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.elytelabs.dialoghub"
                artifactId = "dialoghub"
                version = "1.5.3"
                from(components["release"])

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
}