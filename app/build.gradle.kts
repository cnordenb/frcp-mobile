plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "net.frcp.app"
    // Compose 1.12 compiles against API 37.
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
            // Until the app has an upload key of its own, release builds are signed with the debug key, so they
            // install on a phone for testing and update a debug build in place. Google Play refuses them.
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
    // Sounds.kt plays the jingles straight from the APK, which needs them stored uncompressed.
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

// The web version (frcp.net, the frcp_html repository) is the source of data.json, the images, the sounds and the
// test fixtures that the app shares with it. These tasks copy them from frcpWebDir, ../frcp_html by default, after
// the web version changes them. Run them on their own: ./gradlew syncWebAssets syncWebTestFixtures
val webDir = rootProject.layout.projectDirectory.dir(providers.gradleProperty("frcpWebDir").getOrElse("../frcp_html"))
// The page and its tab strips (images/ui/*.png) are the app's own, so they aren't among these.
val uiImages = listOf(
    "button", "button_pressed", "button_9slice", "button_pressed_9slice", "checkbox", "log_9slice", "panel_9slice",
    "off", "scrollbar_3slice", "scrollbar_knob", "share_icon", "slider", "slider_knob", "textfield_9slice",
    "dropdown_button", "external",
)

tasks.register<Copy>("syncWebAssets") {
    group = "frcp"
    description = "Copies data.json and the images and sounds the app uses from the web version."
    from(webDir) {
        // The app has civ icons of its own, PNGs that are sharper than the web version's, so only random's comes over.
        include("data.json", "images/civ/random.webp", "images/dlc/*_atlas.webp", "sounds/civ/**/*.webm", "sounds/ui/*.webm")
        include(uiImages.map { "images/ui/$it.webp" })
        // A touch has no hover.
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
