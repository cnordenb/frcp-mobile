# Development log, part 1

Part 1 of the development log. The decisions made in the log are listed in [DECISIONS.md](../DECISIONS.md).

- [2026-11-20](#2026-11-20)

## 2026-11-20

### 04:17:16: the project set up from the template and brought under its rules

#### What changed

- **Why.** The project is the app version of frcp.net: it draws a fresh random civ for Age of Empires II, each civ of
  the civ pool once a round. It was to follow the repository template at github.com/cnordenb/repo_template: its
  documents, its procedures for AI agents, its checks, and its rules for code and tests.
- **Before this log.** The repository had four commits from 2026-10-17 and 2026-10-18 and no log. By then the app had
  a port of the web version's `logic.js` and of its tests, a view model following the web version's `script.js`, the
  three tabs, the menu and dialogs in the web version's phone layout, share links and presets shared with the web
  version and the desktop app, the sync tasks, civ icons and page art of its own, and 146 tests.
- **Template files.** `AGENTS.md`, `CLAUDE.md`, `LICENSE`, `.claude/`, `.github/` and `documentation/` were copied
  from the template. The project's name, description, owner, chief developer, test command, stack and the model named
  in the README were filled in. `.github/FUNDING.yml` names one Ko-fi account. `.gitignore` and `.gitattributes` keep
  the project's lines and gain the template's.
- **README.md.** It has the template's chapters. Building, the sync tasks and the overview of the tests moved to
  `MAINTAINER.md`; the chapter on Microsoft's assets stays, last.
- **Folders.** The Kotlin files were moved so that no folder holds more than 7: `picker/` (the view model),
  `device/` (storage, sounds, files, asset paths), and under `ui/` the folders `tabs/`, `menu/` and `widgets/`.
- **The view model in two files.** `PickerViewModel.kt` had 485 lines of code. Its state, saving, sound, draws, undo
  and civ pool are now `PickerCore.kt`, which `PickerViewModel` extends; the factory is `PickerFactory.kt`. What the
  view model offers the screen is unchanged.
- **The screen's long functions.** `DrawTab`, `PoolTab`, the logs' scrollbar and the dropdown were split into smaller
  functions; the swipe's code is in `SwipeGesture.kt` and the scrollbar's in `LogScrollBar.kt`. The longest function
  is now 50 lines.
- **Comments.** All were removed from the Kotlin files and the Gradle scripts, 463 lines in the Kotlin files. What a
  maintainer needs of them is in `MAINTAINER.md`.
- **Lines.** Every line of code over 120 characters was wrapped.
- **Tests.** 352, up from 146. Each test now makes one check, so most of the old tests became several; ten checks
  that another test already made, or that followed from one, were dropped. Tests 1 to 343 are functional, 344 to 350
  check the contrast of text, and 351 and 352 are new: no comments in the code, and no function over 60 lines. Each
  test's name starts with its number, and `AllTests.kt` runs the test classes in the order of `TESTS.md`.
- **Checked.** All 352 tests passed, and the debug build, the release build and lint ran without error. Eleven tests
  were shown to fail when the code they protect was broken on purpose: the cap of the log, closing a round twice, a
  share link without draws, the cap of undo, the sound of a reset, the preview of a share link, the longest glide, a
  page of the scrollbar, the least number of tiles, a planted comment and a planted function of 62 lines. Each
  breakage was undone. `bash .github/scripts/check-rules.sh` passed.
- **Documents.** `MAINTAINER.md`: written, with "Adding a new civ" first. `TESTS.md`: all 352 tests, each with an
  Origin field naming the test of the web version or of the app's earlier tests that it follows. `TERMINOLOGY.md`: 28
  terms of the app, from "web version" to "sync task". `DECISIONS.md`. `AGENTS.md`: the stack.
- **Rules.** Rule 14: the main folder may hold 11 files, and `app/src/main/assets/` and `app/src/main/res/` are
  exempt. Rule 15: `build.gradle.kts` and the legacy jingles are exempt. Rule 26: `*.xml`, `*.toml`, `*.pro`,
  `*.properties`, `.gitignore`, `.gitattributes`, `gradlew` and `gradlew.bat` are not code. `check-rules.sh` follows
  all three, and checks `*.kt` and `*.kts` for the lengths of lines and files.
- **The workflow.** `.github/workflows/checks.yml` selects JDK 17 and runs `bash gradlew testDebugUnitTest`.

#### How the rules were read

- **Rule 11** was read as counting every line from a function's `fun` to its closing brace, blank lines too.
- **Rules 5 and 25** were read as one assertion in each test. A test that goes through many civs or files gathers
  what is wrong and asserts once that nothing is.
- **Rule 10** was applied to `.github/FUNDING.yml` as well, a YAML file, by leaving only its one line in use.

#### Not done

- `REQUIREMENTS.md` is still the template's, to be written by the chief developer. Until then no test's purpose
  names a requirement.
- The placeholder for the security contact in `.github/SECURITY.md` is left, so the `project-setup` skill stays.
- The app was not tried on a phone or an emulator after the screen's functions were split; only the tests, the
  builds and lint ran.
- The workflow was not run: it runs on GitHub at the first push. Whether the runner has what the build needs is not
  known.
- `.github/ACCESSIBILITY.md` is the template's checklist with nothing marked.
- `gradlew` is not marked executable in Git, which is why the workflow starts it with `bash`.
- Renumbering 352 tests by hand after a change is long work; no tool for it is in the project.

#### Questions

1. **Data in `data.json`.** The earlier practice that what belongs to the game lives in `data.json`, not in the code,
   is not among the rules. Should it become a rule? Proposal: yes, as a new rule after Rule 26.
2. **A tool for renumbering.** Should the project hold a script that renumbers the tests and rewrites the numbers in
   `TESTS.md`? Proposal: yes, in `.github/scripts/`, since Rule 3 asks for as few steps as possible.
3. **One test of the rules among the view model's.** Test 125 checks `freshLimit`, a function of `Logic.kt`, from the
   view model's tests. Should it move to the tests of the rules? Proposal: yes, at the next change of the tests.
4. **The accessibility checklist.** Should the 55 items of `.github/ACCESSIBILITY.md` be gone through for the app
   now? Proposal: yes, by a person with the app on a phone and TalkBack.

### 05:12:50: game data ruled into data.json, and a script that renumbers the tests

#### What changed

- **Answers.** Of the questions of 04:17:16, in
  [the project set up from the template and brought under its rules](#041716-the-project-set-up-from-the-template-and-brought-under-its-rules):
  game data in `data.json` becomes a rule (1); the project holds a script that renumbers the tests (2); the test of
  `freshLimit` moves to the tests of the picker's rules (3); the accessibility checklist is to be gone through by a
  person with the app on a phone (4).
- **Rules.** Rule 27, new: what belongs to the game is kept in `data.json`, not in the code.
- **Renumbering.** `.github/scripts/renumber-tests.py` goes through the test classes in the order of `AllTests.kt`,
  numbers every test in its name, and rewrites the numbers, files and lines of `TESTS.md`. An entry follows its test
  by the old number; a new test gets an entry to fill in, and a removed test loses its entry.
- **The test of `freshLimit`** moved from `VmSettingsActionsTest.kt` to the end of `DrawTest.kt`, where it uses that
  file's own value for "the most" instead of the view model's setting.
- **Tests.** 352, as before. Test 125 became Test 71, and Tests 71 to 124 became 72 to 125; 55 were renumbered, by
  the script.
- **Checked.** All 352 tests passed after the renumbering, and `bash .github/scripts/check-rules.sh` passed. The
  script's handling of a new test and of a removed test was not tried.
- **Documents.** `MAINTAINER.md`: how to renumber, the script in the table of files, and Rule 27 among the rules for
  changing the code. `TESTS.md`: the numbers, and Test 71's file and lines. `check-rules.sh`: `*.py` is checked as
  code. `DECISIONS.md`.

#### Not done

- The accessibility checklist is still unmarked: it needs a person, a phone and TalkBack.
- `REQUIREMENTS.md` is still to be written; a draft of requirements was given outside the project's files.
- The code was not checked against Rule 27 by reading; no test checks it.
