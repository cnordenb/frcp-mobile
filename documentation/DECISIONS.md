# Decisions

- [Rules, and how they are read](#rules-and-how-they-are-read)
- [Requirements, and how they are read](#requirements-and-how-they-are-read)
- [Code and files](#code-and-files)
- [Tests](#tests)
- [Documents](#documents)

The decisions recorded in the development log in [DEVLOG/](DEVLOG/DEVLOG1.md), by subject (Rule 23). Each ends with the
day it was made, and the time where the log has one, as a link to its entry in the log. Where a later decision replaced
an earlier one, the earlier one is named after it, with its own link.

## Rules, and how they are read

- **Game data is kept in `data.json`** (Rule 27), not in the code: the editions, the DLCs and the civs.
  [2026-11-20 05:12:50](DEVLOG/DEVLOG1.md#051250-game-data-ruled-into-datajson-and-a-script-that-renumbers-the-tests)

- **The main folder may hold 11 files** (Rule 14), which Gradle, Git and the template's own files need.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **Assets and resources are exempt from Rule 14.** `app/src/main/assets/` and `app/src/main/res/` keep the web
  version's layout, whatever the number of files.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **Two exemptions from Rule 15.** `build.gradle.kts`, which Gradle needs twice, and the legacy jingles, which keep
  the names of the jingles they stand in for.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **Code is Kotlin and the Gradle scripts** (Rule 26), with the template's shell and YAML files. XML, `.toml`, `.pro`,
  `.properties`, `.gitignore`, `.gitattributes` and the Gradle wrapper's scripts are not code.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **A function's length counts every line** (Rule 11), from `fun` to its closing brace, blank lines too.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)

## Requirements, and how they are read

- **Tests name no requirement yet.** `REQUIREMENTS.md` is not written, so each test's purpose is in words only.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)

## Code and files

- **Python counts as code** in `.github/scripts/`, with the shell and YAML files.
  [2026-11-20 05:12:50](DEVLOG/DEVLOG1.md#051250-game-data-ruled-into-datajson-and-a-script-that-renumbers-the-tests)

- **The code's folders.** `logic/`, `picker/`, `device/` and `ui/` with `tabs/`, `menu/` and `widgets/`, none over 7
  files.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **The view model is two files.** `PickerViewModel` extends `PickerCore`; what it offers the screen is unchanged.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **What comments said is in `MAINTAINER.md`.** Only what a maintainer needs was kept; the rest went.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **GitHub runs the tests** with JDK 17, by `bash gradlew testDebugUnitTest`.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)

## Tests

- **Tests are renumbered by a script**, `.github/scripts/renumber-tests.py`, in the order of `AllTests.kt`.
  [2026-11-20 05:12:50](DEVLOG/DEVLOG1.md#051250-game-data-ruled-into-datajson-and-a-script-that-renumbers-the-tests)
- **A test sits with the code it checks.** The test of `freshLimit` is among the tests of the picker's rules.
  [2026-11-20 05:12:50](DEVLOG/DEVLOG1.md#051250-game-data-ruled-into-datajson-and-a-script-that-renumbers-the-tests)

- **One assertion a test** (Rules 5 and 25). A test over many civs or files gathers what is wrong and asserts once.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **Each test keeps its origin.** `TESTS.md` has a fourth field, Origin, naming the test of the web version or of the
  app's earlier tests that it follows.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **A test's number starts its name**, such as `t017_`, and `AllTests.kt` runs the classes in the order of
  `TESTS.md`: functional, accessibility, maintainability.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **Rules 10 and 11 are tested in Kotlin**, by `CodeRulesTest`, as `check-rules.sh` cannot check them.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)

## Documents

- **The accessibility checklist is gone through by a person**, with the app on a phone and TalkBack.
  [2026-11-20 05:12:50](DEVLOG/DEVLOG1.md#051250-game-data-ruled-into-datajson-and-a-script-that-renumbers-the-tests)

- **README.md has the template's chapters**, and Microsoft's assets as a last chapter; building is in `MAINTAINER.md`.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **"Adding a new civ" is the most common maintenance task**, and so the first chapter of `MAINTAINER.md`.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **The licence is MIT**, as the template's.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **The log starts on 2026-11-20**, with a short account of what was built before it.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
- **The security contact is left open** until the first release.
  [2026-11-20 04:17:16](DEVLOG/DEVLOG1.md#041716-the-project-set-up-from-the-template-and-brought-under-its-rules)
