package net.frcp.app

/**
 * Where the files the app shares with the web version sit in the APK's assets (./gradlew syncWebAssets copies them),
 * and how its icon atlases are laid out.
 */
object Assets {
    const val DATA = "data.json"

    // The web version's tools/atlas_builder.py lays the DLC atlas out in rows of this length: the edition icons
    // (normal, hover and pressed for each edition) followed by the icons of the DLCs that have one, in data.json order.
    // The edition banners are stacked in data.json order.
    const val DLC_ATLAS = "images/dlc/dlc_atlas.webp"
    const val DLC_ATLAS_COLS = 6
    const val BANNER_ATLAS = "images/dlc/edition_banners_atlas.webp"

    /**
     * A civ's icon, big or small: the app's own lossless PNGs, sharper than the web version's WebP icons and atlas.
     * A new civ needs its PNG added here by hand, as the web version has none to sync.
     */
    fun civIcon(file: String) = "images/civ/$file.png"
    const val RANDOM_ICON = "images/civ/random.webp"
    const val RANDOM = "random"

    fun ui(name: String) = "images/ui/$name.webp"

    /** The app's own page art: the parchment and its three tab strips, PNGs that meet seamlessly. */
    fun page(name: String) = "images/ui/$name.png"
    fun jingle(file: String, legacy: Boolean) = if (legacy) "sounds/civ/legacy/$file.webm" else "sounds/civ/$file.webm"
    fun uiSound(name: String) = "sounds/ui/$name.webm"
}
