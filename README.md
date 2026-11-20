[![GitHub last commit](https://img.shields.io/github/last-commit/cnordenb/frcp-mobile)](#)
[![language](https://img.shields.io/badge/language-Kotlin-FFFF00)](#)
[![license](https://img.shields.io/badge/license-MIT-007FFF)](LICENSE)
[![testing](https://img.shields.io/badge/testing-yes-C3FF00)](documentation/TESTS.md)
[![maintainable](https://img.shields.io/badge/maintainable-yes-00FF72)](documentation/MAINTAINER.md)

# Fresh Random Civ Picker for Android

## Description

The app version of [frcp.net](https://frcp.net/): it draws a fresh random civilisation for Age of Empires II, and every enabled civ comes up once before any civ repeats. Written in Kotlin with Jetpack Compose, it follows frcp.net's phone layout and its parchment look.

## Usage

- **Draw tab:** swipe left to draw, right to undraw, up to redraw and down to reset. Tap the civ icon to hear its jingle.
- **Log tab:** the drawn civs, newest first and round by round, and the civs left this round.
- **Pool tab:** the edition, the DLC toggles and the civ pool.
- **Menu:** open a share link, save and load presets, the tech tree, options and about.

The app reads and writes the same as the web version and the desktop app:

- **Share links** (`https://frcp.net/#p=…`). The share buttons hand them to Android's share sheet. Links open in the app from the menu's Open Link, or straight from another app once Android sends frcp.net links here.
- **Presets:** the desktop app's `.txt` files.

Android sends `https://frcp.net/` links to the app by default only once frcp.net vouches for the app. Until then, turn it on under the app's settings, in Open by default.

To build the app yourself, see [MAINTAINER.md](documentation/MAINTAINER.md#building-the-app).

## Contributing

Anyone is welcome to open an [issue](https://github.com/cnordenb/frcp-mobile/issues) to submit a bug report or suggest a new feature. Alternatively, you may fork or copy the repository and take it in any direction you wish.

If you wish to contribute work into this repository, please consult [CONTRIBUTING.md](.github/CONTRIBUTING.md) before any commits or pull requests.

## Maintaining

To keep Fresh Random Civ Picker for Android working and up to date, see [MAINTAINER.md](documentation/MAINTAINER.md).

## Acknowledgements

Development of Fresh Random Civ Picker for Android was accelerated with Claude Fable 5.2.

## Microsoft assets

Age of Empires II © Microsoft Corporation. Fresh Random Civ Picker was created under Microsoft's "[Game Content Usage Rules](https://www.xbox.com/en-US/developers/rules)" using assets from Age of Empires II: The Age of Kings, Age of Empires II: HD Edition, and Age of Empires II: Definitive Edition, and it is not endorsed by or affiliated with Microsoft.
