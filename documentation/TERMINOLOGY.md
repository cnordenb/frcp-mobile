requirement - a numbered statement in REQUIREMENTS.md of what the project must do, e.g. "Requirement 4"
rule - a numbered statement in RULES.md of how the project must be made and kept, e.g. "Rule 12"
chief developer - the person who alone writes the requirements, named in Rule 2
test - a check of the project, run on every push and listed by its number in TESTS.md, e.g. "Test 15"
log - the development log in documentation/DEVLOG/: what changed in the project, why, and what is left
part - one file of the log, DEVLOG1.md, DEVLOG2.md and so on, each of at most 1000 lines
day - the entry of the log for one date, titled by it, e.g. "2026-11-02", never split between two parts
section - one change recorded under its day, titled by its time if it was the first written in that minute
decision - a choice made in a section and listed by subject in DECISIONS.md, with a link to that section
question - a numbered point for the chief developer at the end of a section, open until a later section answers it
success criterion - a numbered WCAG 2.2 statement at level A or AA that the interface must meet, e.g. "2.1.1 Keyboard"
human verification - a person's check that a success criterion holds, needed where no test can check it in full
web version - frcp.net, the web page this app follows; its source is the repository frcp_html
desktop app - the Windows program Fresh Random Civ Picker, whose preset files and model tests the app follows
civ - a civilisation of Age of Empires II, with a permanent ID in data.json, e.g. "Britons"
edition - a release of the game with its own set of civs: Definitive Edition, HD Edition or The Age of Kings
DLC - an expansion that adds civs to an edition, e.g. "The Conquerors"
civ pool - the civs that are switched on, from which the app draws
draw - the app's pick of one random civ from the civ pool; also the civ picked
round - the draws made until every civ of the civ pool has come up once, or until a reset
reset - ending the round early, so that the next draw starts a new round
re-roll - swapping the last draw for another civ it could have been
continuous freshness - the setting that keeps the most recently drawn civs out of the next draws, across rounds
jingle - the short tune of a civ, played when it is drawn
legacy jingle - the older tune that a civ of the edition The Age of Kings also has
UI sound - a sound of the interface, such as a button's click
share link - an address https://frcp.net/#p=... that holds an edition, a civ pool and optionally a round's draws
preset - a .txt file in the desktop app's format that holds an edition, a civ pool and a round's draws
tab - one of the app's three pages, chosen at the bottom of the screen: Draw, Log or Pool
swipe - a finger's stroke on the Draw tab: left draws, right undoes, up re-rolls, down resets
drawn log - the Log tab's list of the draws, newest first, round by round
remaining log - the Log tab's list of the civs of the civ pool not yet drawn this round
DLC toggle - a control on the Pool tab that switches all civs of one DLC on or off
page art - the app's own images of the parchment and its three tab strips, in app/src/main/assets/images/ui/
atlas - one image that holds many icons side by side in cells, e.g. dlc_atlas.webp
nine-slice - an image cut 3 by 3 so that it can frame a box of any size, as CSS border-image does
view model - the class PickerViewModel, which holds the picker's state and carries out the user's actions
stand-in - a class the tests use in place of the phone's storage, sounds or files, e.g. MemoryStore
fixture - a file of test data copied from the web version, in app/src/test/resources/fixtures/
sync task - one of the Gradle tasks syncWebAssets and syncWebTestFixtures, which copy files from the web version
