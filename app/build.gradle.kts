plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "net.frcp.app"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "net.frcp.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        noCompress += "webm"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

val webPath = providers.gradleProperty("frcpWebDir").getOrElse("../frcp_html")
val webDir = rootProject.layout.projectDirectory.dir(webPath)
val uiImages = listOf(
    "button", "button_pressed", "button_9slice", "button_pressed_9slice", "checkbox", "log_9slice", "panel_9slice",
    "off", "scrollbar_3slice", "scrollbar_knob", "share_icon", "slider", "slider_knob", "textfield_9slice",
    "dropdown_button", "external",
)

tasks.register<Copy>("syncWebAssets") {
    group = "frcp"
    description = "Copies data.json and the images and sounds the app uses from the web version."
    from(webDir) {
        include("data.json", "images/civ/random.webp", "images/dlc/*_atlas.webp")
        include("sounds/civ/**/*.webm", "sounds/ui/*.webm")
        include(uiImages.map { "images/ui/$it.webp" })
        exclude("sounds/ui/hover_sound.webm")
    }
    into(layout.projectDirectory.dir("src/main/assets"))
}

tasks.register<Copy>("syncWebTestFixtures") {
    group = "frcp"
    description = "Copies the web version's test fixtures: frozen civ IDs, share links and preset files."
    from(webDir.dir("tests/fixtures"))
    into(layout.projectDirectory.dir("src/test/resources/fixtures"))
}

val testsChosen = gradle.startParameter.taskRequests.any { "--tests" in it.args }

tasks.withType<Test>().configureEach {
    if (!testsChosen) filter.includeTestsMatching("net.frcp.app.AllTests")
}
