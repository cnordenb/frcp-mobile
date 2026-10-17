# Fresh Random Civ Picker for Android

The app version of [frcp.net](https://frcp.net/): it draws a fresh random civilisation for Age of Empires II, and every enabled civ comes up once before any civ repeats. Written in Kotlin with Jetpack Compose, it follows frcp.net's phone layout and its parchment look.

- **Draw tab:** swipe left to draw, right to undraw, up to redraw and down to reset. Tap the civ icon to hear its jingle.
- **Log tab:** the drawn civs, newest first and round by round, and the civs left this round.
- **Pool tab:** the edition, the DLC toggles and the civ pool.
- **Menu:** open a share link, save and load presets, the tech tree, options and about.

## Shared with the web version

The rules in `app/src/main/java/net/frcp/app/logic/` are a port of the web version's `logic.js`, rule for rule, so the app and frcp.net read and write the same:

- **Share links** (`https://frcp.net/#p=…`). The share buttons hand them to Android's share sheet. Links open in the app from the menu's Open Link, or straight from another app once Android sends frcp.net links here (see below).
- **Presets:** the desktop app's `.txt` files.
- **Saved state and settings:** the same JSON as the browser keeps.

The app's `data.json`, images and sounds are copies of the web version's, except the civ icons: the app has its own lossless PNGs in `app/src/main/assets/images/civ/`, so a new civ needs its PNG added there by hand. After the shared files change in [frcp_html](https://github.com/cnordenb/frcp_html), copy them over from a checkout beside this one:

```
./gradlew syncWebAssets syncWebTestFixtures
```

Pass `-PfrcpWebDir=<path>` for a checkout somewhere else. Run these on their own rather than in the same Gradle call as a build.

## Building

Open the folder in Android Studio, or build from the command line with JDK 17 or newer:

```
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # the rules, the data and the assets
```

The build needs Android SDK platform 37.2, which Android Studio installs. `gradle.properties` sets `android.overridePathCheck=true`, as the Android Gradle plugin otherwise refuses a project path with non-ASCII characters, such as one under `C:\Users\Hjörleif`.

## Tests

The unit tests in `app/src/test/` port the web version's `tests/` one for one, against the same fixtures: civ data and its icons, atlases and jingles, saved state, drawing and continuous freshness, share links and preset files.

## Opening frcp.net links in the app

The manifest asks Android to send `https://frcp.net/` links to the app. Android only does so by default once frcp.net vouches for the app, with a `/.well-known/assetlinks.json` that names the app's ID (`net.frcp.app`) and the SHA-256 fingerprint of its signing certificate. Until then, a user can turn it on under the app's settings, in Open by default.

## Microsoft assets

Age of Empires II © Microsoft Corporation. Fresh Random Civ Picker was created under Microsoft's "[Game Content Usage Rules](https://www.xbox.com/en-US/developers/rules)" using assets from Age of Empires II: The Age of Kings, Age of Empires II: HD Edition, and Age of Empires II: Definitive Edition, and it is not endorsed by or affiliated with Microsoft.
