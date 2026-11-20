package net.frcp.app.device

object Assets {
    const val DATA = "data.json"

    const val DLC_ATLAS = "images/dlc/dlc_atlas.webp"
    const val DLC_ATLAS_COLS = 6
    const val BANNER_ATLAS = "images/dlc/edition_banners_atlas.webp"

    fun civIcon(file: String) = "images/civ/$file.png"
    const val RANDOM_ICON = "images/civ/random.webp"
    const val RANDOM = "random"

    fun ui(name: String) = "images/ui/$name.webp"

    fun page(name: String) = "images/ui/$name.png"
    fun jingle(file: String, legacy: Boolean) = if (legacy) "sounds/civ/legacy/$file.webm" else "sounds/civ/$file.webm"
    fun uiSound(name: String) = "sounds/ui/$name.webm"
}
