package net.frcp.app

/**
 * Where the files the app shares with the web version sit in the APK's assets (./gradlew syncWebAssets copies them),
 * and how its icon atlases are laid out.
 */
object Assets {
    const val DATA = "data.json"

    // The web version's tools/atlas_builder.py lays the icon atlases out in rows of these lengths: the civ icons in
    // civ ID order with "random" last, and the edition icons (normal, hover and pressed for each edition) followed by
    // the icons of the DLCs that have one, in data.json order. The edition banners are stacked in data.json order.
    const val CIV_ATLAS = "images/civ/civicon_atlas.webp"
    const val CIV_ATLAS_COLS = 8
    const val DLC_ATLAS = "images/dlc/dlc_atlas.webp"
    const val DLC_ATLAS_COLS = 6
    const val BANNER_ATLAS = "images/dlc/edition_banners_atlas.webp"

    /** The big civ icon: one civ's own icon file, or random's. */
    fun civIcon(file: String) = "images/civ/$file.webp"
    const val RANDOM = "random"

    fun ui(name: String) = "images/ui/$name.webp"
    fun jingle(file: String, legacy: Boolean) = if (legacy) "sounds/civ/legacy/$file.webm" else "sounds/civ/$file.webm"
    fun uiSound(name: String) = "sounds/ui/$name.webm"
}
