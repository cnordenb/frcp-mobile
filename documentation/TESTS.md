# Tests

This project strives to have sensible testing. The goal is not to have 100% test coverage but to have tests which _sufficiently_ ensure the functional completeness, functional correctness, maintainability, and accessibility of Fresh Random Civ Picker for Android. For more guidelines on software quality and accessibility, see [ISO/IEC 25010:2011](https://cdn.standards.iteh.ai/samples/35733/2ca18b477b7845a5b8cae39d6de0c098/ISO-IEC-25010-2011.pdf), [ISO/IEC 25010:2023](https://cdn.standards.iteh.ai/samples/78176/13ff8ea97048443f99318920757df124/ISO-IEC-25010-2023.pdf) and [WCAG 2.2](https://www.w3.org/TR/WCAG22/).

This document lists each test, its purpose, and in which test file it resides. This document is an effort to mitigate redundant tests and ensure test quality. It is intended as an aid in the trouble-shooting of potential false positives.

Test descriptions must remain brief and parameters (eg. Purpose) may not exceed 120 characters. Tests are ordered in order of their execution.

The test report names each test by its number here, such as "Test 15 failed": a test's number is its place in the order
the tests run. So when a test is added, removed or moved, renumber this document to match.

<!--
Each test has an entry of this form, after the table of contents, in the order the tests run:

## Test N

### File residence:
`path/to/test_file`, lines 24-26

### Purpose:
Verify that ... (Req. 3, 4).

### Assertion expectation:
What must hold for the test to pass.

### Origin:
The test of the web version or the desktop app that this test follows, or "The app only."
-->

Each test makes one check (Rule 25), so one test of the web version is often several tests here. A test's Origin names
the test it follows: in the web version's `tests/` folder (repository `frcp_html`), or in this app's tests as they were
before they were split. The number is also the start of the test's name in the code, such as `t017_`.

## Table of contents per category

Functional completeness & correctness tests:

- [Test 1](#test-1): frozen civs are still there
- [Test 2](#test-2): frozen civs keep their ids
- [Test 3](#test-3): every civ has its id frozen
- [Test 4](#test-4): earlier links still open
- [Test 5](#test-5): earlier links open with their edition
- [Test 6](#test-6): earlier links open with their civs off
- [Test 7](#test-7): earlier links open with their draws
- [Test 8](#test-8): a link can name every civ
- [Test 9](#test-9): civ names are single words
- [Test 10](#test-10): edition ids are single words
- [Test 11](#test-11): civ ids run from zero without gaps or repeats
- [Test 12](#test-12): civ names are unique
- [Test 13](#test-13): every civs dlc exists
- [Test 14](#test-14): every dlcs edition exists
- [Test 15](#test-15): edition ids are unique
- [Test 16](#test-16): edition years are unique
- [Test 17](#test-17): editions name only known dlcs
- [Test 18](#test-18): editions name no dlc of anewer edition
- [Test 19](#test-19): every civ has an icon
- [Test 20](#test-20): every civ has ajingle
- [Test 21](#test-21): every legacy civ has alegacy jingle
- [Test 22](#test-22): random has its icon and jingles
- [Test 23](#test-23): civ icons are of randoms size
- [Test 24](#test-24): dlc atlas has acell for every edition look and dlc icon
- [Test 25](#test-25): banner atlas stacks every editions banner
- [Test 26](#test-26): new state has the default edition
- [Test 27](#test-27): new state has every civ on
- [Test 28](#test-28): new state has one empty round
- [Test 29](#test-29): new state has no content
- [Test 30](#test-30): saved state reads back as it was
- [Test 31](#test-31): state with draws has content
- [Test 32](#test-32): what is not asaved state reads as nothing
- [Test 33](#test-33): saved state keeps only known civs off
- [Test 34](#test-34): saved state drops broken draws empty past rounds and repeats
- [Test 35](#test-35): saved state without alog gets an empty round
- [Test 36](#test-36): saved state of empty rounds gets one empty round
- [Test 37](#test-37): saved state of an older edition has the newer civs off
- [Test 38](#test-38): web versions state reads back its current round
- [Test 39](#test-39): web versions state saves the same
- [Test 40](#test-40): draws are counted out of the pool and afull round starts anew one
- [Test 41](#test-41): closing around twice adds one empty round
- [Test 42](#test-42): trimmed log keeps256 draws
- [Test 43](#test-43): trimmed log keeps the current round
- [Test 44](#test-44): trimmed log drops whole old rounds
- [Test 45](#test-45): trimmed log drops the oldest draws first
- [Test 46](#test-46): current round longer than the cap stays whole
- [Test 47](#test-47): mersenne twister gives the reference output for aseed
- [Test 48](#test-48): mersenne twister gives the reference outputs for aseed array
- [Test 49](#test-49): mersenne twisters integers stay in range
- [Test 50](#test-50): mersenne twisters range of one gives that number
- [Test 51](#test-51): mersenne twister refuses arange turned around
- [Test 52](#test-52): every civ in the pool comes up once around
- [Test 53](#test-53): freshness keeps the newest civs out of the next rounds first draws
- [Test 54](#test-54): without freshness around can start with the last civ
- [Test 55](#test-55): with freshness apool of two civs takes turns
- [Test 56](#test-56): pool of one civ draws it every time
- [Test 57](#test-57): empty pool has nothing to draw
- [Test 58](#test-58): nothing to pick from gives no pick
- [Test 59](#test-59): without arandom source there is no pick
- [Test 60](#test-60): next draw is one of the civs it can be
- [Test 61](#test-61): next draw is an even pick
- [Test 62](#test-62): next draw is one of the civs when civs join
- [Test 63](#test-63): next draw stays an even pick when civs join
- [Test 64](#test-64): next draw is one of the civs left when civs drop out
- [Test 65](#test-65): next draw stays an even pick when civs drop out
- [Test 66](#test-66): next draw is one of the new civs when civs join and others drop out
- [Test 67](#test-67): next draw stays an even pick when civs join and others drop out
- [Test 68](#test-68): next draw stays put when the civs come in another order
- [Test 69](#test-69): next draw stays put when other civs drop out
- [Test 70](#test-70): after adraw the next is picked from the civs left
- [Test 71](#test-71): the most freshness is half the pool in every edition
- [Test 72](#test-72): desktop preset loads its edition
- [Test 73](#test-73): desktop preset leaves the civs it does not list on
- [Test 74](#test-74): desktop preset loads its draws as one round
- [Test 75](#test-75): older desktop preset still has its old civ names
- [Test 76](#test-76): older desktop preset loads its pool passing over old names
- [Test 77](#test-77): older desktop preset without draws loads an empty round
- [Test 78](#test-78): round start preset loads its civs off
- [Test 79](#test-79): round start presets earlier history makes around without counts
- [Test 80](#test-80): desktop preset saves back byte for byte
- [Test 81](#test-81): saved preset ends every line in cr lf
- [Test 82](#test-82): saved preset lists every civ in id order with its state
- [Test 83](#test-83): saved preset lists this rounds draws of civs that are on
- [Test 84](#test-84): saved preset names its edition
- [Test 85](#test-85): saved preset has no history with freshness off
- [Test 86](#test-86): saved presets history holds the newest civs that could sit out
- [Test 87](#test-87): presets edition is read from any section order
- [Test 88](#test-88): preset is read to the first empty line without civs of newer editions
- [Test 89](#test-89): presets civ states are read after other sections
- [Test 90](#test-90): preset with an unknown edition keeps the default one
- [Test 91](#test-91): presets repeated and unknown draws are passed over
- [Test 92](#test-92): text with none of the sections is not apreset
- [Test 93](#test-93): loaded presets earlier civs keep sitting out
- [Test 94](#test-94): link starts with the address the format version and the edition
- [Test 95](#test-95): full pool is all set bits
- [Test 96](#test-96): draws follow adot
- [Test 97](#test-97): link opens as what it was written from for every edition
- [Test 98](#test-98): link opens with or without its address
- [Test 99](#test-99): civs past ashort bitfield are on
- [Test 100](#test-100): link without draws opens with none
- [Test 101](#test-101): civs of newer editions are left out
- [Test 102](#test-102): text that is not alink opens as nothing
- [Test 103](#test-103): link leaves out the draws of civs that are off
- [Test 104](#test-104): link leaves out the draws when asked
- [Test 105](#test-105): opening alink replaces the edition
- [Test 106](#test-106): opening alink starts around of its draws keeping past rounds
- [Test 107](#test-107): opening alink without draws starts an empty round
- [Test 108](#test-108): drawn log lists rounds and draws newest first with counts
- [Test 109](#test-109): drawn log row has its civs icon
- [Test 110](#test-110): empty round leaves no gap and preset civs show without counts
- [Test 111](#test-111): remaining log lists civs left alphabetically without civs that are off
- [Test 112](#test-112): counter counts this rounds draws of civs still on
- [Test 113](#test-113): counter is out of the pool without civs that are off
- [Test 114](#test-114): edition total counts civs that are off too
- [Test 115](#test-115): new settings save as the web versions defaults
- [Test 116](#test-116): settings read back as they were saved
- [Test 117](#test-117): web settings read back passing over web only ones
- [Test 118](#test-118): setting of the wrong type keeps its default
- [Test 119](#test-119): freshness reads within one and the most
- [Test 120](#test-120): what is not saved settings reads as the defaults
- [Test 121](#test-121): checkbox setting changes
- [Test 122](#test-122): checkbox setting is saved
- [Test 123](#test-123): checkbox setting sounds the button
- [Test 124](#test-124): freshness slider saves its value
- [Test 125](#test-125): freshness sliders right end means the most
- [Test 126](#test-126): turning jingles on plays the current civs
- [Test 127](#test-127): turning jingles off plays the mute sound
- [Test 128](#test-128): turning jingles off is saved
- [Test 129](#test-129): turning ui sounds off plays the mute sound
- [Test 130](#test-130): with ui sounds off the buttons are silent
- [Test 131](#test-131): switching legacy jingles plays the jingle in the new style
- [Test 132](#test-132): switching legacy jingles sounds the view for aciv without alegacy one
- [Test 133](#test-133): switching legacy jingles is saved and leaves jingles on
- [Test 134](#test-134): every ui sound loads ahead
- [Test 135](#test-135): sounds stay while the view model lives
- [Test 136](#test-136): sounds are let go with the view model
- [Test 137](#test-137): draw counts one civ drawn
- [Test 138](#test-138): draw takes its civ out of the civs remaining
- [Test 139](#test-139): draw is saved with its count out of the pool
- [Test 140](#test-140): whole pool draws every civ once in every edition
- [Test 141](#test-141): draw after awhole pool starts asecond round in every edition
- [Test 142](#test-142): swipe left shows the civ it then draws
- [Test 143](#test-143): empty pool shows no civ to draw
- [Test 144](#test-144): draw from an empty pool says the pool is empty
- [Test 145](#test-145): draw from an empty pool sounds the error
- [Test 146](#test-146): draw from an empty pool draws nothing
- [Test 147](#test-147): empty pool message goes when the pool changes
- [Test 148](#test-148): draw without arandom source says why
- [Test 149](#test-149): draw without arandom source draws nothing
- [Test 150](#test-150): randoms jingle loads ahead
- [Test 151](#test-151): next draws jingle loads ahead
- [Test 152](#test-152): draw plays the jingle of the civ shown ahead
- [Test 153](#test-153): with jingles off adraw sounds the button
- [Test 154](#test-154): with jingles off no jingle loads
- [Test 155](#test-155): civ plays its legacy jingle while legacy jingles are on
- [Test 156](#test-156): civ plays its new jingle while legacy jingles are off
- [Test 157](#test-157): undo takes back areset
- [Test 158](#test-158): undo takes back adraw
- [Test 159](#test-159): undo plays the jingle of the civ it goes back to
- [Test 160](#test-160): redo brings back adraw and areset
- [Test 161](#test-161): redone steps are saved
- [Test 162](#test-162): redo after anew draw sounds the error
- [Test 163](#test-163): redo after anew draw changes nothing
- [Test 164](#test-164): undo and redo with nothing to step sound the error
- [Test 165](#test-165): undo and redo with nothing to step draw nothing
- [Test 166](#test-166): undo goes back two hundred steps
- [Test 167](#test-167): undo past two hundred steps sounds the error
- [Test 168](#test-168): undo past two hundred steps changes nothing
- [Test 169](#test-169): swipe right shows the civ undo goes back to
- [Test 170](#test-170): swipe right undoes
- [Test 171](#test-171): swipe right shows random before the first draw
- [Test 172](#test-172): swipe right shows nothing with nothing to undo
- [Test 173](#test-173): swipe up shows the civ it swaps in
- [Test 174](#test-174): reroll swaps in another civ
- [Test 175](#test-175): reroll keeps the earlier draws
- [Test 176](#test-176): reroll counts no other draw
- [Test 177](#test-177): rerolled civ can still come up this round
- [Test 178](#test-178): reroll plays the new civs jingle
- [Test 179](#test-179): undo passes over areroll
- [Test 180](#test-180): redo after areroll sounds the error
- [Test 181](#test-181): redo after areroll changes nothing
- [Test 182](#test-182): swipe up shows nothing before adraw
- [Test 183](#test-183): reroll before adraw sounds the error
- [Test 184](#test-184): swipe up shows nothing with no other civ to swap in
- [Test 185](#test-185): reroll with no other civ sounds the error
- [Test 186](#test-186): reroll with no other civ keeps the civ
- [Test 187](#test-187): swipe down shows nothing before adraw
- [Test 188](#test-188): reset before adraw is silent
- [Test 189](#test-189): reset before adraw adds no round
- [Test 190](#test-190): swipe down shows random after adraw
- [Test 191](#test-191): swipe down leaves no draws this round
- [Test 192](#test-192): swipe down starts anew round
- [Test 193](#test-193): swipe down plays randoms jingle
- [Test 194](#test-194): clearing the log keeps one round
- [Test 195](#test-195): clearing the log keeps this rounds draws
- [Test 196](#test-196): clearing the log clears undo
- [Test 197](#test-197): cleared log is saved
- [Test 198](#test-198): link opens the app at its edition
- [Test 199](#test-199): link opens the app at its draws
- [Test 200](#test-200): opening alink says it loaded
- [Test 201](#test-201): opened link is saved
- [Test 202](#test-202): link over apicker with draws is previewed first
- [Test 203](#test-203): previewed link changes nothing before it is confirmed
- [Test 204](#test-204): confirming the preview closes it
- [Test 205](#test-205): confirming the preview loads the link after the past rounds
- [Test 206](#test-206): confirming the preview says the link loaded
- [Test 207](#test-207): link that is not one says so
- [Test 208](#test-208): link that is not one sounds the error
- [Test 209](#test-209): link that is not one changes nothing
- [Test 210](#test-210): address without alink opens no dialog
- [Test 211](#test-211): address without alink shows no message
- [Test 212](#test-212): address without alink changes nothing
- [Test 213](#test-213): opening the app twice starts one new round
- [Test 214](#test-214): opening the app twice draws once on load
- [Test 215](#test-215): draw on load is silent
- [Test 216](#test-216): visit is saved
- [Test 217](#test-217): share link starts with the sites address
- [Test 218](#test-218): share link opens at the same edition in another picker
- [Test 219](#test-219): share link opens at the same pool in another picker
- [Test 220](#test-220): share link opens at the same round in another picker
- [Test 221](#test-221): pool link leaves out the rounds draws
- [Test 222](#test-222): undone draw is not shared
- [Test 223](#test-223): open link dialog opens
- [Test 224](#test-224): open link dialog turns down text that is not alink
- [Test 225](#test-225): text that is not alink sounds the error
- [Test 226](#test-226): text that is not alink leaves the dialog open
- [Test 227](#test-227): open link dialog takes alink without its address
- [Test 228](#test-228): link taken by the dialog closes it
- [Test 229](#test-229): link taken by the dialog loads
- [Test 230](#test-230): loading apreset says so
- [Test 231](#test-231): preset loads its pool in place of the sessions
- [Test 232](#test-232): preset loads its draws in place of the sessions
- [Test 233](#test-233): loaded preset is saved
- [Test 234](#test-234): loading apreset clears undo
- [Test 235](#test-235): same message again is anew message
- [Test 236](#test-236): file that is not apreset says so by name
- [Test 237](#test-237): file that is not apreset sounds the error
- [Test 238](#test-238): file that is not apreset changes nothing
- [Test 239](#test-239): saving apreset asks for afile named with the version and the time
- [Test 240](#test-240): loading apreset asks the screen for afile
- [Test 241](#test-241): editions banner opens its store page
- [Test 242](#test-242): edition without astore page says why
- [Test 243](#test-243): tech tree opens at its start before adraw
- [Test 244](#test-244): tech tree opens at the current civ
- [Test 245](#test-245): opening an outside link closes the menu
- [Test 246](#test-246): outside link is handed to the screen
- [Test 247](#test-247): link no app can open says so
- [Test 248](#test-248): link no app can open sounds the error
- [Test 249](#test-249): switching edition sets the edition
- [Test 250](#test-250): switching edition leaves its civs on and the newer ones off
- [Test 251](#test-251): switching edition shows the editions total
- [Test 252](#test-252): switched edition is saved
- [Test 253](#test-253): switching to the edition already on keeps undo
- [Test 254](#test-254): civ toggles off
- [Test 255](#test-255): civ toggled off is saved
- [Test 256](#test-256): civ toggles on again
- [Test 257](#test-257): toggling aciv sounds the click
- [Test 258](#test-258): disable all turns every civ off
- [Test 259](#test-259): enable all turns on the editions civs
- [Test 260](#test-260): every dlc toggle has civs in its edition
- [Test 261](#test-261): dlc toggle turns only its own civs off
- [Test 262](#test-262): dlc toggle turns its civs on once they are all off
- [Test 263](#test-263): dlc toggle turns its civs off while any is on
- [Test 264](#test-264): changing the pool clears undo and redo
- [Test 265](#test-265): changing the pool keeps the rounds draws
- [Test 266](#test-266): change that leaves the pool as it was keeps undo
- [Test 267](#test-267): without auto reset achange of the pool keeps the round
- [Test 268](#test-268): with auto reset achange of the pool starts anew round
- [Test 269](#test-269): turning auto toggle on is saved
- [Test 270](#test-270): turning auto toggle on turns on the editions older civs
- [Test 271](#test-271): turning auto toggle off is saved
- [Test 272](#test-272): turning auto toggle off turns the older civs off
- [Test 273](#test-273): with auto toggle switching edition turns on its older civs
- [Test 274](#test-274): auto reset is saved
- [Test 275](#test-275): round fits awhole number of tiles along an edge
- [Test 276](#test-276): round keeps at least one tile on ashort edge
- [Test 277](#test-277): repeat keeps the tiles at their own size
- [Test 278](#test-278): page is the size page measures it in
- [Test 279](#test-279): strips are the size page measures them in
- [Test 280](#test-280): strips reach from their top to the pages bottom
- [Test 281](#test-281): each strips top rows are the pages own so it meets the page without aseam
- [Test 282](#test-282): each tabs touch box covers its drawn tab
- [Test 283](#test-283): tabs touch boxes run left to right without overlap
- [Test 284](#test-284): gaps between the tabs are see through
- [Test 285](#test-285): page is opaque all along the edges of the tabs content
- [Test 286](#test-286): crop takes in all of the art that shows
- [Test 287](#test-287): art shows at the crops left edge
- [Test 288](#test-288): art shows at the crops right edge
- [Test 289](#test-289): art shows at the crops top edge
- [Test 290](#test-290): art shows at the crops bottom edge
- [Test 291](#test-291): page scales its left margin to the size it is drawn at
- [Test 292](#test-292): page scales its right margin to the size it is drawn at
- [Test 293](#test-293): page scales its top margin to the size it is drawn at
- [Test 294](#test-294): page scales its bottom margin to the size it is drawn at
- [Test 295](#test-295): page scales the tabs top to the size it is drawn at
- [Test 296](#test-296): knob at the minimum sits at the grooves start
- [Test 297](#test-297): knob at the maximum sits at the grooves end
- [Test 298](#test-298): knob between the ends sits in proportion along the groove
- [Test 299](#test-299): slider with asingle value keeps its knob at the start
- [Test 300](#test-300): slider with asingle value reads that value
- [Test 301](#test-301): finger on the knob reads its value for every value and width
- [Test 302](#test-302): finger at the images left edge reads the minimum
- [Test 303](#test-303): finger left of the slider reads the minimum
- [Test 304](#test-304): finger at the images right edge reads the maximum
- [Test 305](#test-305): finger right of the slider reads the maximum
- [Test 306](#test-306): swipe left draws
- [Test 307](#test-307): swipe right undoes
- [Test 308](#test-308): swipe up rerolls
- [Test 309](#test-309): swipe down resets
- [Test 310](#test-310): axis is unsettled while the finger is within the slop
- [Test 311](#test-311): axis settles sideways when the finger went further sideways
- [Test 312](#test-312): axis settles up and down when the finger went further that way
- [Test 313](#test-313): diagonal swipe settles up and down
- [Test 314](#test-314): draw swipe takes the current civ towards the edge fading it out
- [Test 315](#test-315): draw swipe shows no ghost before it acts
- [Test 316](#test-316): other swipes fade the current civ in place
- [Test 317](#test-317): other swipes bring the next civ in from the edge with the finger
- [Test 318](#test-318): acted draw swipe shows the new civ in place
- [Test 319](#test-319): acted draw swipe leaves the old civ as aghost where it was
- [Test 320](#test-320): acted undo swipe shows the new civ where the ghost was
- [Test 321](#test-321): acted undo swipe leaves the old civ as aghost in place
- [Test 322](#test-322): at the end of its travel the new civ is in place
- [Test 323](#test-323): at the end of its travel the old civ is gone
- [Test 324](#test-324): lifted swipe glides on from the fingers speed
- [Test 325](#test-325): slow finger glides for at most350 milliseconds
- [Test 326](#test-326): stopped finger glides for350 milliseconds
- [Test 327](#test-327): fast finger glides for at least80 milliseconds
- [Test 328](#test-328): newest editions grid holds every civ in alphabetical order
- [Test 329](#test-329): grid has three civs to arow
- [Test 330](#test-330): grid has the same number of rows in every edition
- [Test 331](#test-331): every civ keeps its place in every edition and newer civs leave theirs empty
- [Test 332](#test-332): hidden dlc toggles are toggles the data has
- [Test 333](#test-333): each editions shown dlc toggles fit in three rows of two
- [Test 334](#test-334): knob starts at the top of its track
- [Test 335](#test-335): knob sits halfway when the log is scrolled halfway
- [Test 336](#test-336): knob ends at the bottom of its track
- [Test 337](#test-337): knob reaches the bottom once the log cannot scroll further
- [Test 338](#test-338): knob stays at the top of alog that does not scroll
- [Test 339](#test-339): dragging the knob down from the top scrolls in proportion
- [Test 340](#test-340): dragging the knob down from the middle scrolls to the end
- [Test 341](#test-341): dragging the knob up from the middle scrolls to the start
- [Test 342](#test-342): press on the track pages by aline short of apage
- [Test 343](#test-343): press on the track pages by at least aline

Accessibility tests:

- [Test 344](#test-344): page text has minimum contrast on the parchment
- [Test 345](#test-345): swipe hints have minimum contrast on the parchment
- [Test 346](#test-346): menu items have minimum contrast
- [Test 347](#test-347): pressed menu item has minimum contrast
- [Test 348](#test-348): log lines have minimum contrast
- [Test 349](#test-349): text field text has minimum contrast
- [Test 350](#test-350): status message has minimum contrast

Maintainability tests:

- [Test 351](#test-351): code has no comments
- [Test 352](#test-352): no function is longer than sixty lines

## Test 1

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 27-31

### Purpose:
Prevent a civ that links and saved data refer to from being removed.

### Assertion expectation:
No ID in `civ-ids.json` is at or past the number of civs; the failure names the civs gone.

### Origin:
frcp.net `tests/compat.test.js`, "civ IDs never change, and every civ has its ID frozen"

## Test 2

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 33-37

### Purpose:
Prevent a civ's ID from changing, as links and saved data refer to civs by ID.

### Assertion expectation:
The civs' names in ID order equal the names in `civ-ids.json`.

### Origin:
frcp.net `tests/compat.test.js`, "civ IDs never change, and every civ has its ID frozen"

## Test 3

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 39-43

### Purpose:
Enforce that every civ is listed in the frozen civ IDs.

### Assertion expectation:
No civ has an ID past the end of `civ-ids.json`; the failure names the civs missing.

### Origin:
frcp.net `tests/compat.test.js`, "civ IDs never change, and every civ has its ID frozen"

## Test 4

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 45-49

### Purpose:
Verify that share links written by earlier versions still open.

### Assertion expectation:
`parseShare` returns a link for every entry of `share-links.json`; the failure names the others.

### Origin:
frcp.net `tests/compat.test.js`, "share links written by earlier versions open as they did"

## Test 5

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 51-54

### Purpose:
Verify that earlier share links open with the edition they had.

### Assertion expectation:
Each opened link's edition equals the entry's `edition`; the failure names the entries.

### Origin:
frcp.net `tests/compat.test.js`, "share links written by earlier versions open as they did"

## Test 6

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 56-64

### Purpose:
Verify that earlier share links open with the same civs off.

### Assertion expectation:
The edition's civs each link has off equal the entry's `off`; the failure names the entries.

### Origin:
frcp.net `tests/compat.test.js`, "share links written by earlier versions open as they did"

## Test 7

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 66-69

### Purpose:
Verify that earlier share links open with the same draws.

### Assertion expectation:
Each opened link's draws equal the IDs of the entry's `draws`; the failure names the entries.

### Origin:
frcp.net `tests/compat.test.js`, "share links written by earlier versions open as they did"

## Test 8

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 71-74

### Purpose:
Prevent more civs than link format 1 can name with one character a draw.

### Assertion expectation:
The number of civs is at most 64, the length of `B64`.

### Origin:
frcp.net `tests/compat.test.js`, "a share link can name every civ"

## Test 9

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 76-80

### Purpose:
Enforce single-word civ names, as preset files split their lines on spaces.

### Assertion expectation:
No civ's name has white space or ends in a colon; the failure names the civs.

### Origin:
frcp.net `tests/compat.test.js`, "civ names and edition IDs are single words"

## Test 10

### File residence:
`app/src/test/java/net/frcp/app/logic/CompatTest.kt`, lines 82-85

### Purpose:
Enforce single-word edition IDs, as preset files split their lines on spaces.

### Assertion expectation:
No edition ID has white space; the failure names the editions.

### Origin:
frcp.net `tests/compat.test.js`, "civ names and edition IDs are single words"

## Test 11

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 19-22

### Purpose:
Verify that `data.json` numbers its civs from 0 without gaps or repeats.

### Assertion expectation:
The civ IDs, sorted, equal 0 to the number of civs less one.

### Origin:
frcp.net `tests/data.test.js`, "civ IDs run from 0 with no gaps or repeats"

## Test 12

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 24-27

### Purpose:
Verify that no two civs in `data.json` share a name.

### Assertion expectation:
The number of distinct civ names equals the number of civs.

### Origin:
frcp.net `tests/data.test.js`, "civ names are unique"

## Test 13

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 29-32

### Purpose:
Verify that every civ's DLC is listed in `data.json`.

### Assertion expectation:
No civ names a DLC that is not among the DLCs; the failure names civ and DLC.

### Origin:
frcp.net `tests/data.test.js`, "every civ's DLC and every DLC's edition exist"

## Test 14

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 34-37

### Purpose:
Verify that every DLC's edition is listed in `data.json`.

### Assertion expectation:
No DLC names an edition that is not among the editions; the failure names DLC and edition.

### Origin:
frcp.net `tests/data.test.js`, "every civ's DLC and every DLC's edition exist"

## Test 15

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 39-42

### Purpose:
Verify that no two editions share an ID.

### Assertion expectation:
The number of distinct edition IDs equals the number of editions.

### Origin:
frcp.net `tests/data.test.js`, "editions have unique IDs and years"

## Test 16

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 44-47

### Purpose:
Verify that no two editions share a year, as an edition's civs go by year.

### Assertion expectation:
The number of distinct edition years equals the number of editions.

### Origin:
frcp.net `tests/data.test.js`, "editions have unique IDs and years"

## Test 17

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 49-53

### Purpose:
Verify that each edition's DLC toggles and older DLCs are DLCs that exist.

### Assertion expectation:
No edition names an unknown DLC; the failure names edition and DLC.

### Origin:
frcp.net `tests/data.test.js`, "each edition's DLC toggles and older DLCs are DLCs it has"

## Test 18

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 55-62

### Purpose:
Verify that no edition names a DLC of a newer edition.

### Assertion expectation:
No DLC an edition names belongs to an edition of a later year; the failure names both.

### Origin:
frcp.net `tests/data.test.js`, "each edition's DLC toggles and older DLCs are DLCs it has"

## Test 19

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 64-67

### Purpose:
Verify that every civ has its icon among the assets.

### Assertion expectation:
`images/civ/<civ>.png` is a file for every civ; the failure names the civs without.

### Origin:
frcp.net `tests/data.test.js`, "every civ has an icon and a jingle, and a legacy jingle"

## Test 20

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 69-72

### Purpose:
Verify that every civ has its jingle among the assets.

### Assertion expectation:
`sounds/civ/<civ>.webm` is a file for every civ; the failure names the civs without.

### Origin:
frcp.net `tests/data.test.js`, "every civ has an icon and a jingle, and a legacy jingle"

## Test 21

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 74-77

### Purpose:
Verify that every civ of an edition with legacy jingles has one.

### Assertion expectation:
`sounds/civ/legacy/<civ>.webm` is a file for every such civ; the failure names the civs without.

### Origin:
frcp.net `tests/data.test.js`, "every civ has an icon and a jingle, and a legacy jingle"

## Test 22

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 79-83

### Purpose:
Verify that random has its icon, its jingle and its legacy jingle.

### Assertion expectation:
`images/civ/random.webp` and both `random.webm` files exist; the failure names those missing.

### Origin:
frcp.net `tests/data.test.js`, "every civ has an icon and a jingle, and a legacy jingle"

## Test 23

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 85-89

### Purpose:
Verify that the civ icons are all of one size, the size of random's icon.

### Assertion expectation:
Every civ's PNG has the width and height of `random.webp`; the failure names the others.

### Origin:
The app only; in place of frcp.net `tests/data.test.js`, "the civ icon atlas has a cell for every civ".

## Test 24

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 91-96

### Purpose:
Verify that the DLC icon atlas has a cell for every icon in it.

### Assertion expectation:
The atlas is 6 cells of 128 wide, with rows for 3 looks an edition and every DLC icon.

### Origin:
frcp.net `tests/data.test.js`, "the DLC icon atlas has cells for every edition's three looks"

## Test 25

### File residence:
`app/src/test/java/net/frcp/app/logic/DataTest.kt`, lines 98-101

### Purpose:
Verify that the banner atlas holds one banner for every edition.

### Assertion expectation:
The atlas is 128 wide and 93 high for each edition.

### Origin:
frcp.net `tests/data.test.js`, "the banner atlas stacks every edition's banner"

## Test 26

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 35-38

### Purpose:
Verify that a new state has the default edition.

### Assertion expectation:
`emptyState()` has the first edition of `data.json`.

### Origin:
frcp.net `tests/state.test.js`, "a new state is the default edition with every civ on"

## Test 27

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 40-43

### Purpose:
Verify that a new state has every civ on.

### Assertion expectation:
`emptyState()` has no civ off.

### Origin:
frcp.net `tests/state.test.js`, "a new state is the default edition with every civ on"

## Test 28

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 45-48

### Purpose:
Verify that a new state's log is one empty round.

### Assertion expectation:
`emptyState()` has a log of one round with no draws.

### Origin:
frcp.net `tests/state.test.js`, "a new state is the default edition with every civ on"

## Test 29

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 50-53

### Purpose:
Verify that a new state counts as having nothing in it.

### Assertion expectation:
`hasContent` is false for `emptyState()`.

### Origin:
frcp.net `tests/state.test.js`, "a new state is the default edition with every civ on"

## Test 30

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 55-59

### Purpose:
Verify that a saved state reads back as it was.

### Assertion expectation:
`parseState(serialiseState(s))` equals `s`, with two civs off and two rounds.

### Origin:
frcp.net `tests/state.test.js`, "a saved state reads back as it was"

## Test 31

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 61-64

### Purpose:
Verify that a state with civs off and draws counts as having content.

### Assertion expectation:
`hasContent` is true for a state with two civs off and three draws.

### Origin:
frcp.net `tests/state.test.js`, "a saved state reads back as it was"

## Test 32

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 66-72

### Purpose:
Verify that text that is not a saved state reads as nothing.

### Assertion expectation:
`parseState` is null for null, broken JSON, other JSON and unknown editions; failure lists others.

### Origin:
frcp.net `tests/state.test.js`, "anything that isn't a saved state reads as nothing"

## Test 33

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 74-77

### Purpose:
Verify that a saved state keeps only the IDs of known civs among those off.

### Assertion expectation:
Of `[1,-1,999,2.5,"3",null]` only civ 1 is off.

### Origin:
frcp.net `tests/state.test.js`, "a saved state keeps only the parts that make sense"

## Test 34

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 79-82

### Purpose:
Verify that a saved state's log loses broken draws, empty past rounds and repeats.

### Assertion expectation:
The log read is one round of civ 10 and a current round of civs 13 and 14.

### Origin:
frcp.net `tests/state.test.js`, "a saved state keeps only the parts that make sense"

## Test 35

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 84-87

### Purpose:
Verify that a saved state with no log gets an empty round.

### Assertion expectation:
`{"edition":"DE"}` reads with a log of one empty round.

### Origin:
frcp.net `tests/state.test.js`, "a saved state with no log gets an empty round"

## Test 36

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 89-92

### Purpose:
Verify that a saved state of only empty rounds keeps one of them.

### Assertion expectation:
A log of `[[],[]]` reads as one empty round.

### Origin:
frcp.net `tests/state.test.js`, "a saved state with no log gets an empty round"

## Test 37

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 94-98

### Purpose:
Verify that a saved state of an older edition has the newer civs off.

### Assertion expectation:
A state of AOK with nothing disabled reads with exactly the civs not in AOK off.

### Origin:
frcp.net `tests/state.test.js`, "a saved state of an older edition has the newer civs off"

## Test 38

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 100-103

### Purpose:
Verify that a state the web version saved reads with its current round.

### Assertion expectation:
The current round read is civ 10 (1/54) then civ 5 (2/54).

### Origin:
The app only.

## Test 39

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 105-108

### Purpose:
Verify that a state the web version saved is saved again as the same text.

### Assertion expectation:
`serialiseState(parseState(text))` equals the text, character for character.

### Origin:
The app only.

## Test 40

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 110-118

### Purpose:
Verify that draws are counted out of the pool, and a draw after a full round starts a new one.

### Assertion expectation:
In a pool of two: Celts 1/2 and Britons 2/2, then a new round of Britons 1/2.

### Origin:
frcp.net `tests/state.test.js`, "a draw is counted out of the pool"

## Test 41

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 120-126

### Purpose:
Verify that closing a round adds one empty round, and never two.

### Assertion expectation:
After two `closeRound` calls the log is the round of Celts and one empty round.

### Origin:
frcp.net `tests/state.test.js`, "closing a round adds one empty round, and never two"

## Test 42

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 128-131

### Purpose:
Verify that the log keeps no more than its newest 256 draws.

### Assertion expectation:
Six full rounds hold 256 draws after `trimLog`.

### Origin:
frcp.net `tests/state.test.js`, "the log keeps its newest 256 draws"

## Test 43

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 133-136

### Purpose:
Verify that trimming the log leaves the current round whole.

### Assertion expectation:
After `trimLog` of six full rounds, the last round still has every civ.

### Origin:
frcp.net `tests/state.test.js`, "the log keeps its newest 256 draws"

## Test 44

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 138-141

### Purpose:
Verify that trimming the log removes the rounds it empties.

### Assertion expectation:
Six full rounds leave 6 less the whole rounds among the draws dropped.

### Origin:
frcp.net `tests/state.test.js`, "the log keeps its newest 256 draws"

## Test 45

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 143-146

### Purpose:
Verify that trimming the log drops the oldest draws first.

### Assertion expectation:
The oldest draw left is of the civ whose ID is the dropped draws modulo the number of civs.

### Origin:
frcp.net `tests/state.test.js`, "the log keeps its newest 256 draws"

## Test 46

### File residence:
`app/src/test/java/net/frcp/app/logic/StateTest.kt`, lines 148-154

### Purpose:
Verify that a current round longer than the cap is never trimmed.

### Assertion expectation:
A log of one round of 300 draws still has 300 after `trimLog`.

### Origin:
frcp.net `tests/state.test.js`, "the log keeps its newest 256 draws"

## Test 47

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 81-84

### Purpose:
Verify that the Mersenne Twister matches mt19937ar.c for a seed.

### Assertion expectation:
Seeded with 5489, the first output is 3499211612.

### Origin:
frcp.net `tests/draw.test.js`, "the Mersenne Twister gives the reference outputs"

## Test 48

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 86-91

### Purpose:
Verify that the Mersenne Twister matches mt19937ar.c for a seed array.

### Assertion expectation:
Seeded with 0x123, 0x234, 0x345, 0x456, the first five outputs are the reference's.

### Origin:
frcp.net `tests/draw.test.js`, "the Mersenne Twister gives the reference outputs"

## Test 49

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 93-97

### Purpose:
Verify that the Mersenne Twister's integers stay in the range asked for.

### Assertion expectation:
None of 1000 calls of `int(3, 7)` is outside 3 to 7; the failure lists those that are.

### Origin:
frcp.net `tests/draw.test.js`, "the Mersenne Twister's integers stay in range"

## Test 50

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 99-102

### Purpose:
Verify that a range of one number gives that number.

### Assertion expectation:
`int(4, 4)` is 4.

### Origin:
frcp.net `tests/draw.test.js`, "the Mersenne Twister's integers stay in range"

## Test 51

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 104-107

### Purpose:
Verify that a range with its ends turned around is refused.

### Assertion expectation:
`int(5, 4)` throws an `IllegalArgumentException`.

### Origin:
frcp.net `tests/draw.test.js`, "the Mersenne Twister's integers stay in range"

## Test 52

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 109-116

### Purpose:
Verify that every civ in the pool comes up once a round, before any repeats.

### Assertion expectation:
For 3 pools and 3 freshness settings, each of 12 rounds is the pool; failure names the rounds.

### Origin:
frcp.net `tests/draw.test.js`, "every civ in the pool comes up once a round"

## Test 53

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 118-121

### Purpose:
Verify that continuous freshness keeps the newest civs out of the next round's first draws.

### Assertion expectation:
For freshness 1, 5 and the most, no early draw of 29 rounds repeats a civ still sitting out.

### Origin:
frcp.net `tests/draw.test.js`, "continuous freshness keeps the newest civs out"

## Test 54

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 123-127

### Purpose:
Verify that without continuous freshness a round can start with the civ the last one ended on.

### Assertion expectation:
In 400 draws from two civs, some civ comes up twice in a row.

### Origin:
frcp.net `tests/draw.test.js`, "without continuous freshness, a round can start with the civ"

## Test 55

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 129-133

### Purpose:
Verify that with continuous freshness a pool of two civs takes turns.

### Assertion expectation:
In 400 draws from two civs, no civ comes up twice in a row.

### Origin:
frcp.net `tests/draw.test.js`, "with continuous freshness, a pool of two civs takes turns"

## Test 56

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 135-139

### Purpose:
Verify that a pool of one civ draws that civ every time.

### Assertion expectation:
Five draws from a pool of only Celts are all Celts.

### Origin:
frcp.net `tests/draw.test.js`, "a pool of one civ draws it every time"

## Test 57

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 141-145

### Purpose:
Verify that an empty pool has nothing to draw.

### Assertion expectation:
`drawCandidates` is empty when every civ is off.

### Origin:
frcp.net `tests/draw.test.js`, "a pool of one civ draws it every time"

## Test 58

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 147-150

### Purpose:
Verify that the picker picks nothing when there is nothing to pick from.

### Assertion expectation:
`Picker.next(emptyList())` is null.

### Origin:
frcp.net `tests/draw.test.js`, "a pool of one civ draws it every time"

## Test 59

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 152-155

### Purpose:
Verify that there is no pick without a random source.

### Assertion expectation:
`Picker(null).next` is null for three civs.

### Origin:
frcp.net `tests/draw.test.js`, "without a random source there's no pick"

## Test 60

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 157-160

### Purpose:
Verify that the next draw is always one of the civs it can be.

### Assertion expectation:
None of 24000 picks from civs 0 to 11 is null or another civ; the failure lists those.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw is an even pick from the civs it can be"

## Test 61

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 162-165

### Purpose:
Verify that the next draw is an even pick from the civs it can be.

### Assertion expectation:
The chi-square of 24000 picks from 12 civs is under its 0.999 limit.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw is an even pick from the civs it can be"

## Test 62

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 167-170

### Purpose:
Verify that the next draw is one of the civs after civs join.

### Assertion expectation:
None of 30000 picks, from civs 0 to 9 and then 0 to 14, is outside 0 to 14.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays an even pick when civs join"

## Test 63

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 172-175

### Purpose:
Verify that the next draw stays an even pick when civs join.

### Assertion expectation:
The chi-square of 30000 picks over civs 0 to 14 is under its 0.999 limit.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays an even pick when civs join"

## Test 64

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 177-180

### Purpose:
Verify that the next draw is one of the civs left after civs drop out.

### Assertion expectation:
None of 20000 picks, from civs 0 to 14 and then 0 to 9, is outside 0 to 9.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays an even pick when civs drop out"

## Test 65

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 182-185

### Purpose:
Verify that the next draw stays an even pick when civs drop out.

### Assertion expectation:
The chi-square of 20000 picks over civs 0 to 9 is under its 0.999 limit.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays an even pick when civs drop out"

## Test 66

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 187-190

### Purpose:
Verify that the next draw is one of the civs it can now be when civs join and others drop out.

### Assertion expectation:
None of 20000 picks, from civs 0 to 9 and then 5 to 14, is outside 5 to 14.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays an even pick when civs join and others drop out"

## Test 67

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 192-195

### Purpose:
Verify that the next draw stays an even pick when civs join and others drop out.

### Assertion expectation:
The chi-square of 20000 picks over civs 5 to 14 is under its 0.999 limit.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays an even pick when civs join and others drop out"

## Test 68

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 197-200

### Purpose:
Prevent the next draw from changing, and its jingle loading again, when only the order changes.

### Assertion expectation:
In 200 runs, the pick from 20 civs is the same when asked with them reversed.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays put while it can"

## Test 69

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 202-205

### Purpose:
Prevent the next draw from changing, and its jingle loading again, when other civs drop out.

### Assertion expectation:
In 200 runs, the pick from 20 civs is the same after half of the other civs are taken out.

### Origin:
frcp.net `tests/draw.test.js`, "the next draw stays put while it can"

## Test 70

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 207-217

### Purpose:
Verify that after a draw the next one is picked from the civs left.

### Assertion expectation:
In 200 runs, the pick after the drawn civ is taken out is one of the 19 left.

### Origin:
frcp.net `tests/draw.test.js`, "after a draw, the next one is picked again from the civs left"

## Test 71

### File residence:
`app/src/test/java/net/frcp/app/logic/DrawTest.kt`, lines 219-223

### Purpose:
Verify that the most freshness follows the pool's size.

### Assertion expectation:
For every edition's full pool, `freshLimit` at `FRESH_MOST` is half the pool, rounded down.

### Origin:
The app's former `SettingsTest`, "the freshness slider's right end means the most, which follows the pool's…"

## Test 72

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 33-36

### Purpose:
Verify that a preset the desktop app wrote loads its edition.

### Assertion expectation:
`desktop-log.txt` loads with edition DE.

### Origin:
frcp.net `tests/preset.test.js`, "a preset the desktop app wrote mid-round loads its pool"

## Test 73

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 38-41

### Purpose:
Verify that civs a desktop preset does not list stay on.

### Assertion expectation:
`desktop-log.txt` loads with no civ off.

### Origin:
frcp.net `tests/preset.test.js`, "a preset the desktop app wrote mid-round loads its pool"

## Test 74

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 43-48

### Purpose:
Verify that a desktop preset written mid-round loads its draws as one round.

### Assertion expectation:
The log is one round of the file's DrawnCivs, counted 1, 2, 3 out of every civ.

### Origin:
frcp.net `tests/preset.test.js`, "a preset the desktop app wrote mid-round loads its pool"

## Test 75

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 50-54

### Purpose:
Enforce that the older preset fixture keeps the civ names since renamed.

### Assertion expectation:
`desktop-v1.3.2.txt` contains "Incas 1" and "Mayans 1".

### Origin:
frcp.net `tests/preset.test.js`, "a preset from an older desktop app loads"

## Test 76

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 56-60

### Purpose:
Verify that a preset from an older desktop app loads, passing over names no longer used.

### Assertion expectation:
`desktop-v1.3.2.txt` loads with Bulgarians, Goths, Persians and Sicilians off.

### Origin:
frcp.net `tests/preset.test.js`, "a preset from an older desktop app loads"

## Test 77

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 62-65

### Purpose:
Verify that a preset without draws loads an empty round.

### Assertion expectation:
`desktop-v1.3.2.txt` loads with a log of one empty round.

### Origin:
frcp.net `tests/preset.test.js`, "a preset from an older desktop app loads"

## Test 78

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 67-70

### Purpose:
Verify that a preset loads the civs it has off.

### Assertion expectation:
`round-start.txt` loads with Aztecs and Wu off.

### Origin:
frcp.net `tests/preset.test.js`, "a desktop preset loads and saves back byte for byte"

## Test 79

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 72-79

### Purpose:
Verify that a preset's freshness history from before the round loads as an earlier round.

### Assertion expectation:
Huns, Franks and Teutons make a round without counts before Celts 1 and Mongols 2.

### Origin:
frcp.net `tests/preset.test.js`, "a desktop preset loads and saves back byte for byte"

## Test 80

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 81-85

### Purpose:
Verify that a loaded desktop preset is saved again byte for byte.

### Assertion expectation:
`presetText` of the state loaded from `round-start.txt` equals the file's text.

### Origin:
frcp.net `tests/preset.test.js`, "a desktop preset loads and saves back byte for byte"

## Test 81

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 87-91

### Purpose:
Verify that a saved preset ends every line in CRLF, as the desktop app's do.

### Assertion expectation:
The text ends in CRLF and has no line feed without a carriage return before it.

### Origin:
frcp.net `tests/preset.test.js`, "a saved preset lists every civ in ID order"

## Test 82

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 93-98

### Purpose:
Verify that a saved preset lists every civ in ID order, on or off.

### Assertion expectation:
CivStates is every civ's name with 1, and Celts, which is off, with 0.

### Origin:
frcp.net `tests/preset.test.js`, "a saved preset lists every civ in ID order"

## Test 83

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 100-103

### Purpose:
Verify that a saved preset lists this round's draws of civs that are on.

### Assertion expectation:
DrawnCivs is Huns and Franks; Celts, drawn but off, is left out.

### Origin:
frcp.net `tests/preset.test.js`, "a saved preset lists every civ in ID order"

## Test 84

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 105-108

### Purpose:
Verify that a saved preset names its edition.

### Assertion expectation:
EditionState is HD.

### Origin:
frcp.net `tests/preset.test.js`, "a saved preset lists every civ in ID order"

## Test 85

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 110-113

### Purpose:
Verify that a preset saved with continuous freshness off has no history.

### Assertion expectation:
The text has no ContfreshCivArray section.

### Origin:
frcp.net `tests/preset.test.js`, "a saved preset lists every civ in ID order"

## Test 86

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 115-122

### Purpose:
Verify that a saved preset's history holds as many of the newest civs as could sit out, oldest first.

### Assertion expectation:
ContfreshCivArray is half the pool: the newest of the last round, then Celts and Franks.

### Origin:
frcp.net `tests/preset.test.js`, "a saved preset's freshness history holds as many of the newest civs"

## Test 87

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 124-127

### Purpose:
Verify that a preset's sections are read in any order.

### Assertion expectation:
A text starting with EditionState HD loads with edition HD.

### Origin:
frcp.net `tests/preset.test.js`, "a preset is read from the default edition with every civ on"

## Test 88

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 129-132

### Purpose:
Verify that a preset is read to its first empty line, without draws of civs its edition lacks.

### Assertion expectation:
The log is Britons 1/30: Wu is not in HD, and Franks is past the empty line.

### Origin:
frcp.net `tests/preset.test.js`, "a preset is read from the default edition with every civ on"

## Test 89

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 134-137

### Purpose:
Verify that a preset's civ states are read when they come last.

### Assertion expectation:
Celts is off after a text whose CivStates follow its other sections.

### Origin:
frcp.net `tests/preset.test.js`, "a preset is read from the default edition with every civ on"

## Test 90

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 139-142

### Purpose:
Verify that a preset with an unknown edition keeps the default one.

### Assertion expectation:
A text with EditionState XX loads with the first edition.

### Origin:
frcp.net `tests/preset.test.js`, "a preset with an unknown edition keeps the default one"

## Test 91

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 144-148

### Purpose:
Verify that repeated and unknown draws in a preset are passed over.

### Assertion expectation:
Celts, Nonsense, Celts, Franks loads as Celts 1 and Franks 2.

### Origin:
frcp.net `tests/preset.test.js`, "a preset with an unknown edition keeps the default one"

## Test 92

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 150-154

### Purpose:
Verify that text with none of the sections is not a preset.

### Assertion expectation:
`parsePreset` is null for five such texts; the failure lists those it read.

### Origin:
frcp.net `tests/preset.test.js`, "text with none of the sections isn't a preset"

## Test 93

### File residence:
`app/src/test/java/net/frcp/app/logic/PresetTest.kt`, lines 156-162

### Purpose:
Verify that after loading a preset its earlier civs keep sitting out.

### Assertion expectation:
None of Huns, Franks, Teutons, Celts and Mongols is a candidate for the next draw.

### Origin:
frcp.net `tests/preset.test.js`, "after loading a preset, its earlier civs keep sitting out"

## Test 94

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 60-63

### Purpose:
Verify that a link starts with the address, the format version and the edition.

### Assertion expectation:
The link of a pool of the first edition starts with `https://frcp.net/#p=1A`.

### Origin:
frcp.net `tests/share.test.js`, "a link starts with the address, the format version and the edition"

## Test 95

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 65-69

### Purpose:
Verify that a full pool is written as all set bits.

### Assertion expectation:
For each edition with every civ, the link is the address, 1, the edition and only `_`.

### Origin:
frcp.net `tests/share.test.js`, "a full pool is all set bits, and draws follow a dot"

## Test 96

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 71-76

### Purpose:
Verify that a link's draws follow a dot, one character a civ.

### Assertion expectation:
With the first and last civ drawn, the full pool's link ends in a dot and their two characters.

### Origin:
frcp.net `tests/share.test.js`, "a full pool is all set bits, and draws follow a dot"

## Test 97

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 78-82

### Purpose:
Verify that a link opens as what it was written from.

### Assertion expectation:
For every edition, a pool with a third of its civs off and three draws opens the same.

### Origin:
frcp.net `tests/share.test.js`, "a link opens as what it was written from, for every edition"

## Test 98

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 84-93

### Purpose:
Verify that a link opens with or without its address.

### Assertion expectation:
Six forms of one link, one with a %31 escape, open the same; the failure lists the others.

### Origin:
frcp.net `tests/share.test.js`, "a link opens with or without its address"

## Test 99

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 95-99

### Purpose:
Verify that the civs past the end of a short bitfield are on.

### Assertion expectation:
`1AA` opens with civs 0 to 5 off and every later civ of the edition on.

### Origin:
frcp.net `tests/share.test.js`, "the civs past a short bitfield are on"

## Test 100

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 101-104

### Purpose:
Verify that a link without draws opens with none.

### Assertion expectation:
`1AA` opens with an empty list of draws.

### Origin:
frcp.net `tests/share.test.js`, "the civs past a short bitfield are on"

## Test 101

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 106-110

### Purpose:
Verify that a link leaves out the civs of editions newer than its own.

### Assertion expectation:
For every edition, an all-bits link opens with exactly that edition's civs on.

### Origin:
frcp.net `tests/share.test.js`, "the civs of newer editions are left out"

## Test 102

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 112-115

### Purpose:
Verify that text that is not a link opens as nothing.

### Assertion expectation:
`parseShare` is null for 21 broken links; the failure lists those it opened.

### Origin:
frcp.net `tests/share.test.js`, "text that isn't a link opens as nothing"

## Test 103

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 117-120

### Purpose:
Verify that a link leaves out the draws of civs that are off.

### Assertion expectation:
With Wu off, a round of Celts, Wu, Franks is shared as Celts and Franks.

### Origin:
frcp.net `tests/share.test.js`, "a link leaves out the draws of civs that are off"

## Test 104

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 122-125

### Purpose:
Verify that a link has no draws when shared without them.

### Assertion expectation:
`shareCode(s, false)` opens with an empty list of draws.

### Origin:
frcp.net `tests/share.test.js`, "a link leaves out the draws of civs that are off"

## Test 105

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 127-130

### Purpose:
Verify that opening a link replaces the edition.

### Assertion expectation:
After `1B__________.KF` is applied, the edition is HD.

### Origin:
frcp.net `tests/share.test.js`, "opening a link replaces the pool and starts a round of its draws"

## Test 106

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 132-140

### Purpose:
Verify that opening a link starts a round of its draws and keeps past rounds.

### Assertion expectation:
The two earlier rounds stay, followed by Celts 1/31 and Britons 2/31.

### Origin:
frcp.net `tests/share.test.js`, "opening a link replaces the pool and starts a round of its draws"

## Test 107

### File residence:
`app/src/test/java/net/frcp/app/logic/ShareTest.kt`, lines 142-147

### Purpose:
Verify that opening a link without draws starts an empty round.

### Assertion expectation:
After `1A__________` is applied, the log is the round of Celts and one empty round.

### Origin:
frcp.net `tests/share.test.js`, "opening a link without draws starts an empty round"

## Test 108

### File residence:
`app/src/test/java/net/frcp/app/picker/PickerViewTest.kt`, lines 27-31

### Purpose:
Verify that the drawn log lists rounds newest first, each round's draws newest first with counts.

### Assertion expectation:
Two rounds read `Franks (1/56)`, then `Celts (2/56)` and `Huns (1/56)`.

### Origin:
The app's former `PickerViewTest`, "the drawn log lists the rounds newest first, and each round's draws newe…"

## Test 109

### File residence:
`app/src/test/java/net/frcp/app/picker/PickerViewTest.kt`, lines 33-37

### Purpose:
Verify that a row of the drawn log carries its civ's icon.

### Assertion expectation:
The newest row's icon is the path of Franks's PNG in `images/civ/`.

### Origin:
The app's former `PickerViewTest`, "the drawn log lists the rounds newest first, and each round's draws newe…"

## Test 110

### File residence:
`app/src/test/java/net/frcp/app/picker/PickerViewTest.kt`, lines 39-43

### Purpose:
Verify that a round without draws leaves no gap and a preset's earlier civs show without counts.

### Assertion expectation:
A log of two countless draws and an empty round reads as one round, `Celts` and `Huns`.

### Origin:
The app's former `PickerViewTest`, "a round with no draws yet leaves no gap, and a preset's earlier civs sho…"

## Test 111

### File residence:
`app/src/test/java/net/frcp/app/picker/PickerViewTest.kt`, lines 45-51

### Purpose:
Verify that the remaining log lists the civs left this round alphabetically, without civs that are off.

### Assertion expectation:
In AOK with Celts off and Britons drawn, the rows are the other AOK civs' names, sorted.

### Origin:
The app's former `PickerViewTest`, "the remaining log lists the civs left this round in alphabetical order,…"

## Test 112

### File residence:
`app/src/test/java/net/frcp/app/picker/PickerViewTest.kt`, lines 53-56

### Purpose:
Verify that the counter counts only this round's draws of civs that are still on.

### Assertion expectation:
With Huns and Celts drawn and Celts off, `drawn` is 1.

### Origin:
The app's former `PickerViewTest`, "the counter counts this round's draws of civs still on, out of the pool"

## Test 113

### File residence:
`app/src/test/java/net/frcp/app/picker/PickerViewTest.kt`, lines 58-61

### Purpose:
Verify that the counter is out of the pool, which leaves out civs that are off.

### Assertion expectation:
With Celts off, `pool` holds one civ fewer than there are civs.

### Origin:
The app's former `PickerViewTest`, "the counter counts this round's draws of civs still on, out of the pool"

## Test 114

### File residence:
`app/src/test/java/net/frcp/app/picker/PickerViewTest.kt`, lines 63-66

### Purpose:
Verify that the edition's total counts every civ of the edition, on or off.

### Assertion expectation:
With Celts off, `editionTotal` equals the number of civs.

### Origin:
The app's former `PickerViewTest`, "the counter counts this round's draws of civs still on, out of the pool"

## Test 115

### File residence:
`app/src/test/java/net/frcp/app/picker/SettingsTest.kt`, lines 15-18

### Purpose:
Verify that new settings are the web version's defaults and save in its format.

### Assertion expectation:
`Settings().toJson()` equals the web version's default settings as JSON, less its web-only ones.

### Origin:
The app's former `SettingsTest`, "new settings are the web version's defaults, and save as it does"

## Test 116

### File residence:
`app/src/test/java/net/frcp/app/picker/SettingsTest.kt`, lines 20-28

### Purpose:
Verify that settings read back as they were saved.

### Assertion expectation:
Settings with every value changed from its default equal `parse` of their own JSON.

### Origin:
The app's former `SettingsTest`, "settings read back as they were saved"

## Test 117

### File residence:
`app/src/test/java/net/frcp/app/picker/SettingsTest.kt`, lines 30-35

### Purpose:
Verify that settings saved by the web version read back, passing over the ones a phone has no use for.

### Assertion expectation:
JSON with tooltips, hotkeys and charKeys reads as `showName` off and `freshN` 4, the rest default.

### Origin:
The app's former `SettingsTest`, "settings the web version saved read back, passing over the ones a phone ha…"

## Test 118

### File residence:
`app/src/test/java/net/frcp/app/picker/SettingsTest.kt`, lines 37-41

### Purpose:
Verify that a setting that is missing or of the wrong type keeps its default.

### Assertion expectation:
JSON with strings, 0, null, a list and an object for settings reads as the default settings.

### Origin:
The app's former `SettingsTest`, "a setting that's missing or of the wrong type keeps its default"

## Test 119

### File residence:
`app/src/test/java/net/frcp/app/picker/SettingsTest.kt`, lines 43-48

### Purpose:
Verify that a saved freshness below 1 or not whole reads as 1, and above the most as the most.

### Assertion expectation:
7, 1, 0, -3, 2.5, 1000 and 5000 read as 7, 1, 1, 1, 1, 1000 and 1000.

### Origin:
The app's former `SettingsTest`, "freshness below 1 or not whole reads as 1, and above the most as the most"

## Test 120

### File residence:
`app/src/test/java/net/frcp/app/picker/SettingsTest.kt`, lines 50-54

### Purpose:
Verify that anything that is not saved settings reads as the defaults.

### Assertion expectation:
null, empty text, text that is not JSON, a list, a number, null and a string all read as defaults.

### Origin:
The app's former `SettingsTest`, "anything that isn't saved settings reads as the defaults"

## Test 121

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 34-37

### Purpose:
Verify that toggling a checkbox setting changes it.

### Assertion expectation:
After `toggleSetting` on `showName`, the view model's `showName` is false.

### Origin:
The app's former `SettingsTest`, "a checkbox setting changes and is saved"

## Test 122

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 39-42

### Purpose:
Verify that a toggled checkbox setting is saved.

### Assertion expectation:
After `toggleSetting` on `showName`, the stored settings read `showName` false.

### Origin:
The app's former `SettingsTest`, "a checkbox setting changes and is saved"

## Test 123

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 44-47

### Purpose:
Verify that toggling a checkbox setting sounds the button.

### Assertion expectation:
The last sound played is `button_sound`.

### Origin:
The app's former `SettingsTest`, "a checkbox setting changes and is saved"

## Test 124

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 49-54

### Purpose:
Verify that the freshness slider saves its value.

### Assertion expectation:
After `setFreshN(5, 28)`, the stored `freshN` is 5.

### Origin:
The app's former `SettingsTest`, "the freshness slider's right end means the most, which follows the pool's…"

## Test 125

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 56-62

### Purpose:
Verify that the freshness slider's right end means the most.

### Assertion expectation:
After `setFreshN(28, 28)`, the stored `freshN` is `FRESH_MOST`.

### Origin:
The app's former `SettingsTest`, "the freshness slider's right end means the most, which follows the pool's…"

## Test 126

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 64-70

### Purpose:
Verify that turning jingles on plays the current civ's jingle.

### Assertion expectation:
After a draw with jingles off, `setJingles(true)` plays the drawn civ's jingle.

### Origin:
The app's former `SettingsTest`, "turning jingles on plays the current civ's, and turning them off plays the…"

## Test 127

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 72-75

### Purpose:
Verify that turning jingles off plays the mute sound.

### Assertion expectation:
After `setJingles(false)`, the last sound played is `mute`.

### Origin:
The app's former `SettingsTest`, "turning jingles on plays the current civ's, and turning them off plays the…"

## Test 128

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 77-80

### Purpose:
Verify that turning jingles off is saved.

### Assertion expectation:
After `setJingles(false)`, the stored settings read `jingles` false.

### Origin:
The app's former `SettingsTest`, "turning jingles on plays the current civ's, and turning them off plays the…"

## Test 129

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 82-87

### Purpose:
Verify that turning UI sounds off plays the mute sound.

### Assertion expectation:
After `setUiSounds(false)`, the last sound played is `mute`.

### Origin:
The app's former `SettingsTest`, "with UI sounds off, the buttons are silent"

## Test 130

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 89-98

### Purpose:
Verify that with UI sounds off, the buttons are silent.

### Assertion expectation:
A civ toggle, a tab change and an undo with nothing to undo play no further sound.

### Origin:
The app's former `SettingsTest`, "with UI sounds off, the buttons are silent"

## Test 131

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 100-103

### Purpose:
Verify that switching legacy jingles off plays the current civ's jingle in the new style.

### Assertion expectation:
With Britons drawn, `setLegacyJingles(false)` plays `sounds/civ/britons.webm`.

### Origin:
The app's former `SettingsTest`, "switching legacy jingles plays the civ's jingle in the new style, or the v…"

## Test 132

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 105-108

### Purpose:
Verify that switching legacy jingles sounds the view for a civ without a legacy jingle.

### Assertion expectation:
With Wu drawn, `setLegacyJingles(false)` plays `view_sound`.

### Origin:
The app's former `SettingsTest`, "switching legacy jingles plays the civ's jingle in the new style, or the v…"

## Test 133

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 110-114

### Purpose:
Verify that switching legacy jingles off is saved and leaves jingles on.

### Assertion expectation:
The stored settings read `jingles` true and `legacyJingles` false.

### Origin:
The app's former `SettingsTest`, "switching legacy jingles plays the civ's jingle in the new style, or the v…"

## Test 134

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 116-120

### Purpose:
Verify that every UI sound loads ahead when the view model is made.

### Assertion expectation:
No `UiSound` is missing from the sounds loaded.

### Origin:
The app's former `SettingsTest`, "every UI sound loads ahead, and the sounds are let go with the view model"

## Test 135

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 122-127

### Purpose:
Prevent the sounds from being let go while the view model is still in use.

### Assertion expectation:
With the view model in a `ViewModelStore` that is not cleared, `released` is false.

### Origin:
The app's former `SettingsTest`, "every UI sound loads ahead, and the sounds are let go with the view model"

## Test 136

### File residence:
`app/src/test/java/net/frcp/app/picker/VmSettingsActionsTest.kt`, lines 129-134

### Purpose:
Verify that the sounds are let go with the view model.

### Assertion expectation:
After the `ViewModelStore` holding the view model is cleared, `released` is true.

### Origin:
The app's former `SettingsTest`, "every UI sound loads ahead, and the sounds are let go with the view model"

## Test 137

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 33-36

### Purpose:
Verify that a draw counts one civ drawn.

### Assertion expectation:
After one draw, the view's `drawn` is 1.

### Origin:
The app's former `DrawingTest`, "a draw takes a civ from the pool, counts it out of the pool and saves it"

## Test 138

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 38-42

### Purpose:
Verify that a draw takes its civ out of the civs remaining this round.

### Assertion expectation:
After one draw, the current civ is not in the view's `remaining`.

### Origin:
The app's former `DrawingTest`, "a draw takes a civ from the pool, counts it out of the pool and saves it"

## Test 139

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 44-48

### Purpose:
Verify that a draw is saved with its count out of the pool.

### Assertion expectation:
The saved log is one round of one draw: the civ, 1, and the number of civs.

### Origin:
The app's former `DrawingTest`, "a draw takes a civ from the pool, counts it out of the pool and saves it"

## Test 140

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 50-54

### Purpose:
Verify that drawing a whole pool draws every civ of it once, in every edition.

### Assertion expectation:
For no edition does the round, sorted, differ from the pool after as many draws as civs.

### Origin:
The app's former `DrawingTest`, "drawing a whole pool draws every civ once, in every edition, and the next d…"

## Test 141

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 56-59

### Purpose:
Verify that the draw after a whole pool starts a new round, in every edition.

### Assertion expectation:
After one draw more than the pool holds, every edition's log has 2 rounds.

### Origin:
The app's former `DrawingTest`, "drawing a whole pool draws every civ once, in every edition, and the next d…"

## Test 142

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 61-72

### Purpose:
Verify that a swipe left shows the civ it then draws.

### Assertion expectation:
Over two pools of swipes, `swipeCell(Draw)` before each equals the current civ after it.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following the fi…"

## Test 143

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 74-77

### Purpose:
Verify that an empty pool shows no civ for a swipe left.

### Assertion expectation:
With every civ off, `swipeCell(Draw)` is null.

### Origin:
The app's former `DrawingTest`, "an empty pool draws nothing, says so and sounds the error, until the pool c…"

## Test 144

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 79-82

### Purpose:
Verify that a draw from an empty pool says so.

### Assertion expectation:
The view's `flash` is `Empty pool!`.

### Origin:
The app's former `DrawingTest`, "an empty pool draws nothing, says so and sounds the error, until the pool c…"

## Test 145

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 84-87

### Purpose:
Verify that a draw from an empty pool sounds the error.

### Assertion expectation:
The last sound played is `error_sound`.

### Origin:
The app's former `DrawingTest`, "an empty pool draws nothing, says so and sounds the error, until the pool c…"

## Test 146

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 89-92

### Purpose:
Prevent a draw from an empty pool from drawing anything.

### Assertion expectation:
The view's `drawn` is 0.

### Origin:
The app's former `DrawingTest`, "an empty pool draws nothing, says so and sounds the error, until the pool c…"

## Test 147

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 94-99

### Purpose:
Verify that the empty pool message goes when the pool changes.

### Assertion expectation:
After Celts is toggled on, the view's `flash` is null.

### Origin:
The app's former `DrawingTest`, "an empty pool draws nothing, says so and sounds the error, until the pool c…"

## Test 148

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 101-104

### Purpose:
Verify that a draw without a random source says why nothing is drawn.

### Assertion expectation:
With no random source, the view's `flash` is `No random source`.

### Origin:
The app's former `DrawingTest`, "without a random source, nothing is drawn and the screen says why"

## Test 149

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/DrawingTest.kt`, lines 106-109

### Purpose:
Prevent a draw without a random source from drawing anything.

### Assertion expectation:
With no random source, there is no current civ after `draw`.

### Origin:
The app's former `DrawingTest`, "without a random source, nothing is drawn and the screen says why"

## Test 150

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmDrawSoundTest.kt`, lines 23-26

### Purpose:
Verify that random's jingle loads ahead.

### Assertion expectation:
A new view model has loaded `sounds/civ/legacy/random.webm`.

### Origin:
The app's former `DrawingTest`, "the next draw's jingle and random's load ahead, and a draw plays the one lo…"

## Test 151

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmDrawSoundTest.kt`, lines 28-38

### Purpose:
Verify that the next draw's jingle loads ahead.

### Assertion expectation:
Before each of 10 draws, the jingle of the civ `swipeCell(Draw)` shows is among the loaded.

### Origin:
The app's former `DrawingTest`, "the next draw's jingle and random's load ahead, and a draw plays the one lo…"

## Test 152

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmDrawSoundTest.kt`, lines 40-51

### Purpose:
Verify that a draw plays the jingle that was loaded ahead for it.

### Assertion expectation:
Over 10 draws, the sound each plays is the jingle of the civ shown before it.

### Origin:
The app's former `DrawingTest`, "the next draw's jingle and random's load ahead, and a draw plays the one lo…"

## Test 153

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmDrawSoundTest.kt`, lines 53-56

### Purpose:
Verify that with jingles off, a draw sounds the button.

### Assertion expectation:
The last sound played is `button_sound`.

### Origin:
The app's former `DrawingTest`, "with jingles off, a draw sounds the button and no jingle loads"

## Test 154

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmDrawSoundTest.kt`, lines 58-62

### Purpose:
Prevent jingles from loading while they are off.

### Assertion expectation:
After a draw with jingles off, no loaded sound is under `sounds/civ/`.

### Origin:
The app's former `DrawingTest`, "with jingles off, a draw sounds the button and no jingle loads"

## Test 155

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmDrawSoundTest.kt`, lines 64-67

### Purpose:
Verify that a civ with a legacy jingle plays it while legacy jingles are on.

### Assertion expectation:
Drawing Britons plays `sounds/civ/legacy/britons.webm`.

### Origin:
The app's former `DrawingTest`, "a civ plays its legacy jingle while legacy jingles are on, and its new one…"

## Test 156

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmDrawSoundTest.kt`, lines 69-72

### Purpose:
Verify that a civ plays its new jingle while legacy jingles are off.

### Assertion expectation:
Drawing Britons plays `sounds/civ/britons.webm`.

### Origin:
The app's former `DrawingTest`, "a civ plays its legacy jingle while legacy jingles are on, and its new one…"

## Test 157

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 37-44

### Purpose:
Verify that undo takes back a reset.

### Assertion expectation:
After 3 draws, a reset and an undo, the round is the 3 draws again.

### Origin:
The app's former `DrawingTest`, "undo takes back draws and resets one at a time, and redo brings them back"

## Test 158

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 46-52

### Purpose:
Verify that undo takes back one draw.

### Assertion expectation:
After 3 draws and an undo, the round is the first 2 draws.

### Origin:
The app's former `DrawingTest`, "undo takes back draws and resets one at a time, and redo brings them back"

## Test 159

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 54-60

### Purpose:
Verify that undo plays the jingle of the civ it goes back to.

### Assertion expectation:
After 3 draws and an undo, the last sound is the second civ's jingle.

### Origin:
The app's former `DrawingTest`, "undo takes back draws and resets one at a time, and redo brings them back"

## Test 160

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 62-65

### Purpose:
Verify that redo brings back a draw and a reset that were undone.

### Assertion expectation:
After 3 draws, a reset, 2 undos and 2 redos, the round is empty.

### Origin:
The app's former `DrawingTest`, "undo takes back draws and resets one at a time, and redo brings them back"

## Test 161

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 67-71

### Purpose:
Verify that the state redo brings back is saved.

### Assertion expectation:
The saved log is the round of 3 draws followed by an empty round.

### Origin:
The app's former `DrawingTest`, "undo takes back draws and resets one at a time, and redo brings them back"

## Test 162

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 73-78

### Purpose:
Verify that a new draw clears what redo would bring back.

### Assertion expectation:
After an undo and a new draw, redo plays `error_sound`.

### Origin:
The app's former `DrawingTest`, "a new draw clears what redo would bring back"

## Test 163

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 80-86

### Purpose:
Prevent redo from changing the round after a new draw cleared it.

### Assertion expectation:
After an undo, a new draw and a redo, the round is as the new draw left it.

### Origin:
The app's former `DrawingTest`, "a new draw clears what redo would bring back"

## Test 164

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 88-91

### Purpose:
Verify that undo and redo with nothing to step to sound the error.

### Assertion expectation:
On a new picker, an undo and a redo play `error_sound` twice and nothing else.

### Origin:
The app's former `DrawingTest`, "undo with nothing to undo changes nothing and sounds the error"

## Test 165

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 93-96

### Purpose:
Prevent undo and redo with nothing to step to from changing the round.

### Assertion expectation:
On a new picker, there is no current civ after an undo and a redo.

### Origin:
The app's former `DrawingTest`, "undo with nothing to undo changes nothing and sounds the error"

## Test 166

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 98-101

### Purpose:
Verify that undo goes back 200 steps.

### Assertion expectation:
After 210 draws and 200 undos, the log holds 10 draws.

### Origin:
The app's former `DrawingTest`, "undo goes back up to 200 steps"

## Test 167

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 103-106

### Purpose:
Enforce that undo keeps no more than 200 steps.

### Assertion expectation:
After 210 draws, the 201st undo plays `error_sound`.

### Origin:
The app's former `DrawingTest`, "undo goes back up to 200 steps"

## Test 168

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 108-111

### Purpose:
Prevent an undo past the 200 kept steps from changing the log.

### Assertion expectation:
After 210 draws and 201 undos, the log still holds 10 draws.

### Origin:
The app's former `DrawingTest`, "undo goes back up to 200 steps"

## Test 169

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 113-117

### Purpose:
Verify that a swipe right shows the civ undo goes back to.

### Assertion expectation:
After 2 draws, `swipeCell(Undo)` is the first civ drawn.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following the fi…"

## Test 170

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 119-125

### Purpose:
Verify that a swipe right undoes.

### Assertion expectation:
After 2 draws and `swipe(Undo)`, the current civ is the first civ drawn.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following the fi…"

## Test 171

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 127-131

### Purpose:
Verify that a swipe right shows random when undo goes back to before the first draw.

### Assertion expectation:
After 1 draw, `swipeCell(Undo)` is `randomCell`.

### Origin:
The app's former `DrawingTest`, "a swipe right shows the civ undo goes back to, and random before the first…"

## Test 172

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmUndoTest.kt`, lines 133-136

### Purpose:
Verify that a swipe right shows nothing when nothing is left to undo.

### Assertion expectation:
On a new picker, `swipeCell(Undo)` is null.

### Origin:
The app's former `DrawingTest`, "a swipe right shows the civ undo goes back to, and random before the first…"

## Test 173

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 43-49

### Purpose:
Verify that a swipe up shows the civ the re-roll swaps in.

### Assertion expectation:
After 3 draws, `swipeCell(Reroll)` equals the current civ after `swipe(Reroll)`.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following the fi…"

## Test 174

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 51-55

### Purpose:
Verify that a re-roll swaps the current civ for another.

### Assertion expectation:
After a re-roll, the current civ differs from the civ drawn last before it.

### Origin:
The app's former `DrawingTest`, "a swipe up swaps the civ it shows for the current one, without counting ano…"

## Test 175

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 57-61

### Purpose:
Prevent a re-roll from changing the round's earlier draws.

### Assertion expectation:
The round without its last draw is the same before and after the re-roll.

### Origin:
The app's former `DrawingTest`, "a swipe up swaps the civ it shows for the current one, without counting ano…"

## Test 176

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 63-67

### Purpose:
Prevent a re-roll from counting another draw.

### Assertion expectation:
The last draw's count `k` is the same before and after the re-roll.

### Origin:
The app's former `DrawingTest`, "a swipe up swaps the civ it shows for the current one, without counting ano…"

## Test 177

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 69-73

### Purpose:
Verify that the civ a re-roll swaps out can still come up this round.

### Assertion expectation:
The swapped-out civ is in the view's `remaining`.

### Origin:
The app's former `DrawingTest`, "a swipe up swaps the civ it shows for the current one, without counting ano…"

## Test 178

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 75-79

### Purpose:
Verify that a re-roll plays the jingle of the civ it swaps in.

### Assertion expectation:
The last sound played is the current civ's jingle.

### Origin:
The app's former `DrawingTest`, "a swipe up swaps the civ it shows for the current one, without counting ano…"

## Test 179

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 81-88

### Purpose:
Verify that undo passes over a re-roll, back to before the draw it swapped.

### Assertion expectation:
After 3 draws, a re-roll and an undo, the round is the first 2 draws.

### Origin:
The app's former `DrawingTest`, "undo passes over a re-roll, back to before the draw it swapped"

## Test 180

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 90-95

### Purpose:
Verify that a re-roll clears redo, which would otherwise undo it.

### Assertion expectation:
After an undo and a re-roll, redo plays `error_sound`.

### Origin:
The app's former `DrawingTest`, "a re-roll clears redo, which would otherwise undo it"

## Test 181

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 97-103

### Purpose:
Prevent redo from changing the round after a re-roll cleared it.

### Assertion expectation:
After an undo, a re-roll and a redo, the round is as the re-roll left it.

### Origin:
The app's former `DrawingTest`, "a re-roll clears redo, which would otherwise undo it"

## Test 182

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 105-108

### Purpose:
Verify that a swipe up shows nothing before the round's first draw.

### Assertion expectation:
On a new picker, `swipeCell(Reroll)` is null.

### Origin:
The app's former `DrawingTest`, "a re-roll needs a draw to swap and another civ to swap in"

## Test 183

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 110-113

### Purpose:
Verify that a re-roll with no draw to swap sounds the error.

### Assertion expectation:
On a new picker, `reroll` plays `error_sound`.

### Origin:
The app's former `DrawingTest`, "a re-roll needs a draw to swap and another civ to swap in"

## Test 184

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 115-118

### Purpose:
Verify that a swipe up shows nothing when no other civ can be swapped in.

### Assertion expectation:
With a pool of Celts alone, drawn, `swipeCell(Reroll)` is null.

### Origin:
The app's former `DrawingTest`, "a re-roll needs a draw to swap and another civ to swap in"

## Test 185

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 120-123

### Purpose:
Verify that a re-roll with no other civ to swap in sounds the error.

### Assertion expectation:
With a pool of Celts alone, drawn, `reroll` plays `error_sound`.

### Origin:
The app's former `DrawingTest`, "a re-roll needs a draw to swap and another civ to swap in"

## Test 186

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmRerollTest.kt`, lines 125-128

### Purpose:
Prevent a re-roll with no other civ to swap in from changing the round.

### Assertion expectation:
With a pool of Celts alone, the round is still Celts after `reroll`.

### Origin:
The app's former `DrawingTest`, "a re-roll needs a draw to swap and another civ to swap in"

## Test 187

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 23-26

### Purpose:
Verify that a swipe down shows nothing on a round with no draws.

### Assertion expectation:
On a new picker, `swipeCell(Reset)` is null.

### Origin:
The app's former `DrawingTest`, "a swipe down closes the round with random's jingle, and does nothing on a r…"

## Test 188

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 28-31

### Purpose:
Prevent a reset of a round with no draws from making a sound.

### Assertion expectation:
On a new picker, no sound has played after `reset`.

### Origin:
The app's former `DrawingTest`, "a swipe down closes the round with random's jingle, and does nothing on a r…"

## Test 189

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 33-36

### Purpose:
Prevent a reset of a round with no draws from adding a round.

### Assertion expectation:
On a new picker, the log has 1 round after `reset`.

### Origin:
The app's former `DrawingTest`, "a swipe down closes the round with random's jingle, and does nothing on a r…"

## Test 190

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 38-42

### Purpose:
Verify that a swipe down shows random once the round has a draw.

### Assertion expectation:
After 1 draw, `swipeCell(Reset)` is `randomCell`.

### Origin:
The app's former `DrawingTest`, "a swipe down closes the round with random's jingle, and does nothing on a r…"

## Test 191

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 44-47

### Purpose:
Verify that a swipe down leaves the round without draws.

### Assertion expectation:
After 1 draw and `swipe(Reset)`, the view's `drawn` is 0.

### Origin:
The app's former `DrawingTest`, "a swipe down closes the round with random's jingle, and does nothing on a r…"

## Test 192

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 49-52

### Purpose:
Verify that a swipe down closes the round and starts a new one.

### Assertion expectation:
After 1 draw and `swipe(Reset)`, the log has 2 rounds.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following the fi…"

## Test 193

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 54-57

### Purpose:
Verify that a swipe down plays random's jingle.

### Assertion expectation:
After 1 draw and `swipe(Reset)`, the last sound is random's jingle.

### Origin:
The app's former `DrawingTest`, "a swipe down closes the round with random's jingle, and does nothing on a r…"

## Test 194

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 59-62

### Purpose:
Verify that clearing the log keeps this round only.

### Assertion expectation:
After 2 draws more than the pool holds and `clearLog`, the log has 1 round.

### Origin:
The app's former `DrawingTest`, "clearing the log keeps this round only, and clears undo as the web page does"

## Test 195

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 64-67

### Purpose:
Prevent clearing the log from dropping this round's draws.

### Assertion expectation:
After 2 draws more than the pool holds and `clearLog`, the round has 2 draws.

### Origin:
The app's former `DrawingTest`, "clearing the log keeps this round only, and clears undo as the web page does"

## Test 196

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 69-74

### Purpose:
Verify that clearing the log clears undo, as the web page does.

### Assertion expectation:
After `clearLog`, undo plays `error_sound`.

### Origin:
The app's former `DrawingTest`, "clearing the log keeps this round only, and clears undo as the web page does"

## Test 197

### File residence:
`app/src/test/java/net/frcp/app/picker/draws/VmResetTest.kt`, lines 76-79

### Purpose:
Verify that the cleared log is saved.

### Assertion expectation:
After `clearLog`, the saved log has 1 round.

### Origin:
The app's former `DrawingTest`, "clearing the log keeps this round only, and clears undo as the web page does"

## Test 198

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 36-39

### Purpose:
Verify that a share link opens the app at the shared edition.

### Assertion expectation:
Launching with the HD link sets the edition to `HD`.

### Origin:
The app's former `LinkTest`, "a share link opens the app at the shared pool and its draws"

## Test 199

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 41-44

### Purpose:
Verify that a share link opens the app at the shared draws.

### Assertion expectation:
Launching with the HD link gives one round: Celts 1/31, then Britons 2/31.

### Origin:
The app's former `LinkTest`, "a share link opens the app at the shared pool and its draws"

## Test 200

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 46-49

### Purpose:
Verify that opening a share link says it loaded.

### Assertion expectation:
The toast reads `Loaded the shared link`.

### Origin:
The app's former `LinkTest`, "a share link opens the app at the shared pool and its draws"

## Test 201

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 51-55

### Purpose:
Verify that the state a share link opened is saved.

### Assertion expectation:
The saved state equals the view's state.

### Origin:
The app's former `LinkTest`, "a share link opens the app at the shared pool and its draws"

## Test 202

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 57-60

### Purpose:
Verify that a link over a picker with draws is previewed before it loads.

### Assertion expectation:
The dialog is a `Preview` whose link has edition `HD`.

### Origin:
The app's former `LinkTest`, "a link over a picker with draws shows what it loads first, and loads once that…"

## Test 203

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 62-65

### Purpose:
Prevent a previewed link from changing anything before it is confirmed.

### Assertion expectation:
The log is still the one round with Huns 1/56.

### Origin:
The app's former `LinkTest`, "a link over a picker with draws shows what it loads first, and loads once that…"

## Test 204

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 67-70

### Purpose:
Verify that confirming the preview closes it.

### Assertion expectation:
After `loadPreviewedLink`, the dialog is null.

### Origin:
The app's former `LinkTest`, "a link over a picker with draws shows what it loads first, and loads once that…"

## Test 205

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 72-75

### Purpose:
Verify that confirming the preview loads the link's round after the past rounds.

### Assertion expectation:
The log is the round with Huns, then the link's Celts and Britons.

### Origin:
The app's former `LinkTest`, "a link over a picker with draws shows what it loads first, and loads once that…"

## Test 206

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 77-80

### Purpose:
Verify that confirming the preview says the link loaded.

### Assertion expectation:
The toast reads `Loaded the shared link`.

### Origin:
The app's former `LinkTest`, "a link over a picker with draws shows what it loads first, and loads once that…"

## Test 207

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 82-86

### Purpose:
Verify that a link that is not a valid one says so.

### Assertion expectation:
The dialog is the message `Link not recognised` with its explanation.

### Origin:
The app's former `LinkTest`, "a link that isn't one says so and changes nothing"

## Test 208

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 88-91

### Purpose:
Verify that a link that is not a valid one sounds the error.

### Assertion expectation:
Launching with `#p=nonsense` plays `error_sound`.

### Origin:
The app's former `LinkTest`, "a link that isn't one says so and changes nothing"

## Test 209

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 93-96

### Purpose:
Prevent a link that is not a valid one from changing the state.

### Assertion expectation:
Launching with `#p=nonsense` leaves the state equal to a new one.

### Origin:
The app's former `LinkTest`, "a link that isn't one says so and changes nothing"

## Test 210

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 98-101

### Purpose:
Verify that an address without a link opens no dialog.

### Assertion expectation:
No address, the site's address and `#about` all leave the dialog null.

### Origin:
The app's former `LinkTest`, "an address without a link opens the app as the launcher does"

## Test 211

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 103-106

### Purpose:
Verify that an address without a link shows no message.

### Assertion expectation:
No address, the site's address and `#about` all leave the toast null.

### Origin:
The app's former `LinkTest`, "an address without a link opens the app as the launcher does"

## Test 212

### File residence:
`app/src/test/java/net/frcp/app/picker/links/LinkTest.kt`, lines 108-111

### Purpose:
Prevent an address without a link from changing the state.

### Assertion expectation:
No address, the site's address and `#about` all leave the state equal to a new one.

### Origin:
The app's former `LinkTest`, "an address without a link opens the app as the launcher does"

## Test 213

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 46-49

### Purpose:
Verify that opening the app is one visit, which starts one new round with that setting.

### Assertion expectation:
With new round on visit, two calls of `onLaunch` leave 2 rounds in the log.

### Origin:
The app's former `LinkTest`, "opening the app is one visit, which starts a new round with that setting and d…"

## Test 214

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 51-54

### Purpose:
Verify that draw on load draws once a visit.

### Assertion expectation:
With draw on load, two calls of `onLaunch` leave 1 draw in the round.

### Origin:
The app's former `LinkTest`, "opening the app is one visit, which starts a new round with that setting and d…"

## Test 215

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 56-59

### Purpose:
Verify that the draw on load is silent.

### Assertion expectation:
No sound has played after `onLaunch`.

### Origin:
The app's former `LinkTest`, "opening the app is one visit, which starts a new round with that setting and d…"

## Test 216

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 61-65

### Purpose:
Verify that the state after opening the app is saved.

### Assertion expectation:
The saved state equals the view's state.

### Origin:
The app's former `LinkTest`, "opening the app is one visit, which starts a new round with that setting and d…"

## Test 217

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 67-71

### Purpose:
Verify that a share link starts with the site's address.

### Assertion expectation:
The shared text starts with `https://frcp.net/#p=`.

### Origin:
The app's former `LinkTest`, "a picker's share link opens as the same pool and round in another"

## Test 218

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 73-76

### Purpose:
Verify that a picker's share link opens at the same edition in another picker.

### Assertion expectation:
A link shared from HD opens a new picker at edition `HD`.

### Origin:
The app's former `LinkTest`, "a picker's share link opens as the same pool and round in another"

## Test 219

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 78-82

### Purpose:
Verify that a picker's share link opens at the same pool in another picker.

### Assertion expectation:
The new picker's pool equals the sharing picker's, with every fourth HD civ off.

### Origin:
The app's former `LinkTest`, "a picker's share link opens as the same pool and round in another"

## Test 220

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 84-88

### Purpose:
Verify that a picker's share link opens at the same round in another picker.

### Assertion expectation:
The new picker's round equals the sharing picker's 3 draws.

### Origin:
The app's former `LinkTest`, "a picker's share link opens as the same pool and round in another"

## Test 221

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 90-93

### Purpose:
Verify that a pool link leaves out the round's draws.

### Assertion expectation:
A link shared without draws after 3 draws opens with an empty round.

### Origin:
The app's former `LinkTest`, "a pool link leaves out the round's draws"

## Test 222

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 95-100

### Purpose:
Prevent an undone draw from being shared.

### Assertion expectation:
After 4 draws and an undo, the shared link's draws equal the round's 3.

### Origin:
The app's former `LinkTest`, "an undone draw isn't shared"

## Test 223

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 102-105

### Purpose:
Verify that the Open Link dialog opens.

### Assertion expectation:
After `openLinkDialog`, the dialog is `OpenLink`.

### Origin:
The app's former `LinkTest`, "the Open Link dialog takes a link, and turns down other text"

## Test 224

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 107-110

### Purpose:
Verify that the Open Link dialog turns down text that is not a link.

### Assertion expectation:
`openLinkText("hello")` returns false.

### Origin:
The app's former `LinkTest`, "the Open Link dialog takes a link, and turns down other text"

## Test 225

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 112-115

### Purpose:
Verify that text that is not a link sounds the error in the Open Link dialog.

### Assertion expectation:
After `openLinkText("hello")`, the last sound is `error_sound`.

### Origin:
The app's former `LinkTest`, "the Open Link dialog takes a link, and turns down other text"

## Test 226

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 117-120

### Purpose:
Verify that text that is not a link leaves the Open Link dialog open.

### Assertion expectation:
After `openLinkText("hello")`, the dialog is still `OpenLink`.

### Origin:
The app's former `LinkTest`, "the Open Link dialog takes a link, and turns down other text"

## Test 227

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 122-125

### Purpose:
Verify that the Open Link dialog takes a link without its address.

### Assertion expectation:
`openLinkText("1B__________.KF")` returns true.

### Origin:
The app's former `LinkTest`, "the Open Link dialog takes a link, and turns down other text"

## Test 228

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 127-130

### Purpose:
Verify that a link taken by the Open Link dialog closes it.

### Assertion expectation:
After the link is taken, the dialog is null.

### Origin:
The app's former `LinkTest`, "the Open Link dialog takes a link, and turns down other text"

## Test 229

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmLaunchTest.kt`, lines 132-135

### Purpose:
Verify that a link taken by the Open Link dialog loads.

### Assertion expectation:
The log is one round: Celts 1/31, then Britons 2/31.

### Origin:
The app's former `LinkTest`, "the Open Link dialog takes a link, and turns down other text"

## Test 230

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 31-34

### Purpose:
Verify that loading a preset says so.

### Assertion expectation:
After `round-start.txt` loads, the toast reads `Preset loaded`.

### Origin:
The app's former `LinkTest`, "a preset file loads its pool and draws in place of the session, and clears undo"

## Test 231

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 36-39

### Purpose:
Verify that a preset's pool replaces the session's.

### Assertion expectation:
After `round-start.txt` loads, the civs that are off are Aztecs and Wu.

### Origin:
The app's former `LinkTest`, "a preset file loads its pool and draws in place of the session, and clears undo"

## Test 232

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 41-44

### Purpose:
Verify that a preset's draws replace the session's.

### Assertion expectation:
After `round-start.txt` loads over a round with a draw, the round is Celts, Mongols.

### Origin:
The app's former `LinkTest`, "a preset file loads its pool and draws in place of the session, and clears undo"

## Test 233

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 46-50

### Purpose:
Verify that the state a preset loaded is saved.

### Assertion expectation:
The saved state equals the view's state.

### Origin:
The app's former `LinkTest`, "a preset file loads its pool and draws in place of the session, and clears undo"

## Test 234

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 52-57

### Purpose:
Verify that loading a preset clears undo.

### Assertion expectation:
After a draw and a loaded preset, undo plays `error_sound`.

### Origin:
The app's former `LinkTest`, "a preset file loads its pool and draws in place of the session, and clears undo"

## Test 235

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 59-65

### Purpose:
Verify that the same message shown again is a new message, so it shows again.

### Assertion expectation:
The toast after a second load of the preset differs from the first.

### Origin:
The app's former `LinkTest`, "a preset file loads its pool and draws in place of the session, and clears undo"

## Test 236

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 67-72

### Purpose:
Verify that a file that is not a preset, or could not be read, says so by its name.

### Assertion expectation:
For the text `hello` and for none, the dialog is `Preset not recognised`, naming `notes.txt`.

### Origin:
The app's former `LinkTest`, "a file that isn't a preset, or couldn't be read, says so by name and changes n…"

## Test 237

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 74-77

### Purpose:
Verify that a file that is not a preset, or could not be read, sounds the error.

### Assertion expectation:
For the text `hello` and for none, the last sound is `error_sound`.

### Origin:
The app's former `LinkTest`, "a file that isn't a preset, or couldn't be read, says so by name and changes n…"

## Test 238

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 79-82

### Purpose:
Prevent a file that is not a preset, or could not be read, from changing the state.

### Assertion expectation:
For the text `hello` and for none, the state equals a new one.

### Origin:
The app's former `LinkTest`, "a file that isn't a preset, or couldn't be read, says so by name and changes n…"

## Test 239

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 84-92

### Purpose:
Verify that saving a preset asks for a file named with the app's version and the time.

### Assertion expectation:
The file name matches `FRCP Preset v<version>a @yyyy.mm.dd hhmmss.txt`.

### Origin:
The app's former `LinkTest`, "saving a preset asks for a file named with the app's version and the time"

## Test 240

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmPresetTest.kt`, lines 94-99

### Purpose:
Verify that loading a preset asks the screen for a file.

### Assertion expectation:
After `loadPreset`, the effect is `LoadPreset`.

### Origin:
The app's former `LinkTest`, "saving a preset asks for a file named with the app's version and the time"

## Test 241

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 25-30

### Purpose:
Verify that the edition's banner opens its store page.

### Assertion expectation:
In DE, `openEditionPage` asks the screen to open DE's `url` from `data.json`.

### Origin:
The app's former `LinkTest`, "the edition's banner opens its store page, or says why it has none"

## Test 242

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 32-39

### Purpose:
Verify that an edition without a store page says why.

### Assertion expectation:
In AOK, `openEditionPage` shows a message with AOK's short name and store note.

### Origin:
The app's former `LinkTest`, "the edition's banner opens its store page, or says why it has none"

## Test 243

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 41-46

### Purpose:
Verify that the tech tree opens at its start before a draw.

### Assertion expectation:
The effect is to open `https://aoe2techtree.net/`.

### Origin:
The app's former `LinkTest`, "the tech tree opens at the current civ, or at its start before a draw"

## Test 244

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 48-54

### Purpose:
Verify that the tech tree opens at the current civ.

### Assertion expectation:
After a draw, the effect is to open the address with `#` and the civ's tech tree name.

### Origin:
The app's former `LinkTest`, "the tech tree opens at the current civ, or at its start before a draw"

## Test 245

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 56-59

### Purpose:
Verify that opening a link out of the app closes the menu.

### Assertion expectation:
With the menu open, `openExternal` leaves `menuOpen` false.

### Origin:
The app's former `LinkTest`, "a link no app can open says so"

## Test 246

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 61-64

### Purpose:
Verify that a link out of the app is handed to the screen to open.

### Assertion expectation:
`openExternal` with the GitHub address gives the effect `OpenUrl` of that address.

### Origin:
The app's former `LinkTest`, "a link no app can open says so"

## Test 247

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 66-69

### Purpose:
Verify that a link no app can open says so.

### Assertion expectation:
After `linkFailed`, the toast reads `No app here can open that`.

### Origin:
The app's former `LinkTest`, "a link no app can open says so"

## Test 248

### File residence:
`app/src/test/java/net/frcp/app/picker/links/VmOutLinkTest.kt`, lines 71-74

### Purpose:
Verify that a link no app can open sounds the error.

### Assertion expectation:
After `linkFailed`, the last sound is `error_sound`.

### Origin:
The app's former `LinkTest`, "a link no app can open says so"

## Test 249

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 30-33

### Purpose:
Verify that switching edition sets the edition.

### Assertion expectation:
For every edition, the state's edition after `setEdition` is that edition.

### Origin:
The app's former `PoolTest`, "switching edition leaves its civs on and the newer ones off"

## Test 250

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 35-38

### Purpose:
Verify that switching edition leaves its civs on and the newer ones off.

### Assertion expectation:
For every edition, the pool is exactly the civs of that edition and of older ones.

### Origin:
The app's former `PoolTest`, "switching edition leaves its civs on and the newer ones off"

## Test 251

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 40-43

### Purpose:
Verify that switching edition shows the edition's total of civs.

### Assertion expectation:
For every edition, the view's `editionTotal` equals `logic.editionTotal`.

### Origin:
The app's former `PoolTest`, "switching edition leaves its civs on and the newer ones off"

## Test 252

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 45-49

### Purpose:
Verify that a switch of edition is saved.

### Assertion expectation:
For no edition does the saved state differ from the view's state.

### Origin:
The app's former `PoolTest`, "switching edition leaves its civs on and the newer ones off"

## Test 253

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 51-58

### Purpose:
Prevent a switch to the edition already on from clearing undo.

### Assertion expectation:
After a draw and `setEdition` of the same edition, undo empties the round.

### Origin:
The app's former `PoolTest`, "switching to the edition already on changes nothing"

## Test 254

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 60-63

### Purpose:
Verify that a civ toggles off.

### Assertion expectation:
After one `toggleCiv`, Celts is not in the pool.

### Origin:
The app's former `PoolTest`, "a civ toggles off and on again"

## Test 255

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 65-68

### Purpose:
Verify that a civ toggled off is saved.

### Assertion expectation:
The saved state's civs that are off are Celts alone.

### Origin:
The app's former `PoolTest`, "a civ toggles off and on again"

## Test 256

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 70-73

### Purpose:
Verify that a civ toggles on again.

### Assertion expectation:
After two `toggleCiv`, Celts is in the pool.

### Origin:
The app's former `PoolTest`, "a civ toggles off and on again"

## Test 257

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 75-78

### Purpose:
Verify that toggling a civ sounds the click.

### Assertion expectation:
The last sound played is `click_sound`.

### Origin:
The app's former `PoolTest`, "a civ toggles off and on again"

## Test 258

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 80-85

### Purpose:
Verify that Disable All turns every civ off.

### Assertion expectation:
In HD, the pool is empty after `disableAll`.

### Origin:
The app's former `PoolTest`, "Enable All turns on the edition's civs, and Disable All turns them all off"

## Test 259

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/PoolTest.kt`, lines 87-93

### Purpose:
Verify that Enable All turns on the edition's civs and no newer ones.

### Assertion expectation:
In HD, after `disableAll` and `enableAll`, the pool is HD's civs.

### Origin:
The app's former `PoolTest`, "Enable All turns on the edition's civs, and Disable All turns them all off"

## Test 260

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmDlcToggleTest.kt`, lines 33-36

### Purpose:
Verify that every DLC toggle of every edition has civs to toggle.

### Assertion expectation:
No DLC toggle in `data.json` is without civs in its edition.

### Origin:
The app's former `PoolTest`, "a DLC toggle turns its civs off while any is on, and on once they're all off"

## Test 261

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmDlcToggleTest.kt`, lines 38-42

### Purpose:
Verify that a DLC toggle turns off its own civs and no others.

### Assertion expectation:
For every toggle, the pool after one `toggleDlc` is the edition's civs less the DLC's.

### Origin:
The app's former `PoolTest`, "a DLC toggle turns its civs off while any is on, and on once they're all off"

## Test 262

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmDlcToggleTest.kt`, lines 44-47

### Purpose:
Verify that a DLC toggle turns its civs on once they are all off.

### Assertion expectation:
For every toggle, the pool after two `toggleDlc` is the edition's civs again.

### Origin:
The app's former `PoolTest`, "a DLC toggle turns its civs off while any is on, and on once they're all off"

## Test 263

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmDlcToggleTest.kt`, lines 49-52

### Purpose:
Verify that a DLC toggle turns its civs off while any of them is on.

### Assertion expectation:
For every toggle of 2 civs or more, with one off, `toggleDlc` leaves none of them on.

### Origin:
The app's former `PoolTest`, "a DLC toggle turns its civs off while any is on, and on once they're all off"

## Test 264

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 39-42

### Purpose:
Verify that changing the pool clears undo and redo.

### Assertion expectation:
After a change of the pool, an undo and a redo play `error_sound` twice.

### Origin:
The app's former `PoolTest`, "changing the pool clears undo and redo"

## Test 265

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 44-47

### Purpose:
Prevent undo and redo from changing the round after the pool changed.

### Assertion expectation:
The round still has its 1 draw.

### Origin:
The app's former `PoolTest`, "changing the pool clears undo and redo"

## Test 266

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 49-56

### Purpose:
Prevent a change that leaves the pool as it was from clearing undo.

### Assertion expectation:
After a draw and `enableAll` on a full pool, undo empties the round.

### Origin:
The app's former `PoolTest`, "a change that leaves the pool as it was keeps undo"

## Test 267

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 58-61

### Purpose:
Prevent a change of the pool from starting a new round while auto-reset is off.

### Assertion expectation:
After Wu is toggled off, the log has 1 round.

### Origin:
The app's former `PoolTest`, "with auto-reset, a change of the pool starts a new round"

## Test 268

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 63-67

### Purpose:
Verify that with auto-reset, a change of the pool starts a new round.

### Assertion expectation:
After Wu is toggled off, the log is the round of 2 draws and an empty round.

### Origin:
The app's former `PoolTest`, "with auto-reset, a change of the pool starts a new round"

## Test 269

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 69-72

### Purpose:
Verify that turning auto-toggle on is saved.

### Assertion expectation:
After one `toggleAutoToggle`, the stored `autoToggle` is true.

### Origin:
The app's former `PoolTest`, "turning auto-toggle on turns on the edition's older civs, and turning it off t…"

## Test 270

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 74-77

### Purpose:
Verify that turning auto-toggle on turns on the edition's older civs.

### Assertion expectation:
From an empty pool in DE, the pool becomes the civs of DE's `olderDlcs`.

### Origin:
The app's former `PoolTest`, "turning auto-toggle on turns on the edition's older civs, and turning it off t…"

## Test 271

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 79-82

### Purpose:
Verify that turning auto-toggle off is saved.

### Assertion expectation:
After two `toggleAutoToggle`, the stored `autoToggle` is false.

### Origin:
The app's former `PoolTest`, "turning auto-toggle on turns on the edition's older civs, and turning it off t…"

## Test 272

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 84-87

### Purpose:
Verify that turning auto-toggle off turns the edition's older civs off.

### Assertion expectation:
After two `toggleAutoToggle` from an empty pool, the pool is empty again.

### Origin:
The app's former `PoolTest`, "turning auto-toggle on turns on the edition's older civs, and turning it off t…"

## Test 273

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 89-94

### Purpose:
Verify that with auto-toggle, switching edition turns on its older civs.

### Assertion expectation:
From an empty pool, `setEdition("HD")` makes the pool the civs of HD's `olderDlcs`.

### Origin:
The app's former `PoolTest`, "with auto-toggle, switching edition turns on its older civs"

## Test 274

### File residence:
`app/src/test/java/net/frcp/app/picker/pool/VmPoolRoundTest.kt`, lines 96-101

### Purpose:
Verify that auto-reset is saved.

### Assertion expectation:
After `toggleAutoReset`, the stored `autoReset` is true.

### Origin:
The app's former `PoolTest`, "auto-reset is saved"

## Test 275

### File residence:
`app/src/test/java/net/frcp/app/ui/UiTileTest.kt`, lines 10-13

### Purpose:
Verify that a rounded nine-slice edge fits a whole number of tiles.

### Assertion expectation:
A 100 px edge of 38 px tiles gets tiles 100/3 px long.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "round fits a whole number of tiles"

## Test 276

### File residence:
`app/src/test/java/net/frcp/app/ui/UiTileTest.kt`, lines 15-18

### Purpose:
Verify that a rounded edge shorter than a tile still gets one tile.

### Assertion expectation:
A 10 px edge of 38 px tiles gets one tile 10 px long.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "round fits a whole number of tiles"

## Test 277

### File residence:
`app/src/test/java/net/frcp/app/ui/UiTileTest.kt`, lines 20-23

### Purpose:
Verify that a repeated nine-slice edge keeps its tiles at their own size.

### Assertion expectation:
A 100 px edge of 38 px tiles keeps tiles 38 px long.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "repeat keeps the tiles at their own size"

## Test 278

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 59-62

### Purpose:
Verify that background.png has the size that Page's places are measured in.

### Assertion expectation:
The PNG is Page.ART_WIDTH by Page.ART_HEIGHT pixels.

### Origin:
The app's former `PageArtTest`, "the page and its strips are the sizes Page measures them in"

## Test 279

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 64-68

### Purpose:
Verify that the three tab strips have the size that Page measures them in.

### Assertion expectation:
Each strip PNG is Page.ART_WIDTH by Page.STRIP_HEIGHT pixels.

### Origin:
The app's former `PageArtTest`, "the page and its strips are the sizes Page measures them in"

## Test 280

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 70-73

### Purpose:
Verify that a strip placed at its top ends at the bottom of the page art.

### Assertion expectation:
Page.STRIP_TOP + Page.STRIP_HEIGHT equals Page.ART_HEIGHT.

### Origin:
The app's former `PageArtTest`, "the page and its strips are the sizes Page measures them in"

## Test 281

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 75-80

### Purpose:
Verify that each tab strip meets the page without a visible seam.

### Assertion expectation:
In every strip's top 40 rows, no channel differs from the page's by more than 2.

### Origin:
The app's former `PageArtTest`, "each strip's top rows are the page's own, so it meets the page..."

## Test 282

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 82-91

### Purpose:
Verify that each tab's touch box lies over its drawn tab.

### Assertion expectation:
On every strip, at least 95 % of each Page.TABS box is opaque on the labels' row.

### Origin:
The app's former `PageArtTest`, "each tab's touch box covers its drawn tab, and leaves out the gaps..."

## Test 283

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 93-97

### Purpose:
Verify that the tabs' touch boxes do not overlap.

### Assertion expectation:
Each box in Page.TABS ends left of where the next one starts.

### Origin:
The app's former `PageArtTest`, "each tab's touch box covers its drawn tab, and leaves out the gaps..."

## Test 284

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 99-107

### Purpose:
Verify that the tabs' touch boxes leave out the gaps between the drawn tabs.

### Assertion expectation:
On every strip, the pixel midway between two tab boxes is not opaque.

### Origin:
The app's former `PageArtTest`, "each tab's touch box covers its drawn tab, and leaves out the gaps..."

## Test 285

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 109-123

### Purpose:
Verify that the tabs' content lies wholly on the parchment.

### Assertion expectation:
With every strip, the art is at least 250/255 opaque all along the content's edges.

### Origin:
The app's former `PageArtTest`, "the tabs' content lies on the page, which is opaque all along..."

## Test 286

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 125-131

### Purpose:
Verify that Page's crop cuts off none of the art that shows.

### Assertion expectation:
With every strip, no pixel outside the crop is half opaque or more.

### Origin:
The app's former `PageArtTest`, "the crop takes in all of the art that shows"

## Test 287

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 133-136

### Purpose:
Verify that Page's crop leaves no empty margin on the left.

### Assertion expectation:
Some pixel in the crop's leftmost column is half opaque or more.

### Origin:
The app's former `PageArtTest`, "the crop leaves no margin of its own, as the art shows at each..."

## Test 288

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 138-141

### Purpose:
Verify that Page's crop leaves no empty margin on the right.

### Assertion expectation:
Some pixel in the crop's rightmost column is half opaque or more.

### Origin:
The app's former `PageArtTest`, "the crop leaves no margin of its own, as the art shows at each..."

## Test 289

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 143-146

### Purpose:
Verify that Page's crop leaves no empty margin at the top.

### Assertion expectation:
Some pixel in the crop's top row is half opaque or more.

### Origin:
The app's former `PageArtTest`, "the crop leaves no margin of its own, as the art shows at each..."

## Test 290

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 148-151

### Purpose:
Verify that Page's crop leaves no empty margin at the bottom.

### Assertion expectation:
Some pixel in the crop's bottom row is half opaque or more.

### Origin:
The app's former `PageArtTest`, "the crop leaves no margin of its own, as the art shows at each..."

## Test 291

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 153-156

### Purpose:
Verify that Page scales the content's left margin with the drawn size.

### Assertion expectation:
At half size, left is (INNER_LEFT - CROP_LEFT) / 2 dp.

### Origin:
The app's former `PageArtTest`, "Page scales the crop to the size it's drawn at"

## Test 292

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 158-161

### Purpose:
Verify that Page scales the content's right margin with the drawn size.

### Assertion expectation:
At half size, right is (CROP_RIGHT - INNER_RIGHT) / 2 dp.

### Origin:
The app's former `PageArtTest`, "Page scales the crop to the size it's drawn at"

## Test 293

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 163-166

### Purpose:
Verify that Page scales the content's top margin with the drawn size.

### Assertion expectation:
At half size, top is (INNER_TOP - CROP_TOP) / 2 dp.

### Origin:
The app's former `PageArtTest`, "Page scales the crop to the size it's drawn at"

## Test 294

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 168-171

### Purpose:
Verify that Page scales the content's bottom margin with the drawn size.

### Assertion expectation:
At half size, bottom is (CROP_BOTTOM - PAGE_BOTTOM) / 2 dp.

### Origin:
The app's former `PageArtTest`, "Page scales the crop to the size it's drawn at"

## Test 295

### File residence:
`app/src/test/java/net/frcp/app/ui/PageArtTest.kt`, lines 173-176

### Purpose:
Verify that Page scales the top of the tabs with the drawn size.

### Assertion expectation:
At half size, tabTop is (TAB_TOP - CROP_TOP) / 2 dp.

### Origin:
The app's former `PageArtTest`, "Page scales the crop to the size it's drawn at"

## Test 296

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 14-17

### Purpose:
Verify that the slider's knob sits at the groove's start for the minimum.

### Assertion expectation:
sliderKnobAt(1, 1, 28) is 32/340.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the slider's knob runs the groove"

## Test 297

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 19-22

### Purpose:
Verify that the slider's knob sits at the groove's end for the maximum.

### Assertion expectation:
sliderKnobAt(28, 1, 28) is 308/340.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the slider's knob runs the groove"

## Test 298

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 24-27

### Purpose:
Verify that the slider's knob moves along the groove in proportion to its value.

### Assertion expectation:
sliderKnobAt(10, 1, 28) is (32 + 276/3)/340.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the slider's knob runs the groove"

## Test 299

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 29-32

### Purpose:
Prevent a division by zero placing the knob of a slider with one value.

### Assertion expectation:
sliderKnobAt(5, 5, 5) is 32/340.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a slider with a single value keeps its knob..."

## Test 300

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 34-37

### Purpose:
Verify that a slider with one value reads that value wherever it is touched.

### Assertion expectation:
sliderValueAt(200, 340, 5, 5) is 5.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a slider with a single value keeps its knob..."

## Test 301

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 39-46

### Purpose:
Verify that a finger on the knob reads the knob's value at any slider width.

### Assertion expectation:
For widths 240, 340 and 1000 px, each value 1 to 28 reads back from its knob's place.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a finger on the knob reads its value"

## Test 302

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 48-51

### Purpose:
Verify that a finger left of the groove, on the arrowhead, reads the minimum.

### Assertion expectation:
sliderValueAt(0, 340, 1, 28) is 1.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a finger past the groove's ends reads as th..."

## Test 303

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 53-56

### Purpose:
Verify that a finger dragged off the slider's left end reads the minimum.

### Assertion expectation:
sliderValueAt(-20, 340, 1, 28) is 1.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a finger past the groove's ends reads as th..."

## Test 304

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 58-61

### Purpose:
Verify that a finger right of the groove, on the arrowhead, reads the maximum.

### Assertion expectation:
sliderValueAt(340, 340, 1, 28) is 28.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a finger past the groove's ends reads as th..."

## Test 305

### File residence:
`app/src/test/java/net/frcp/app/ui/widgets/UiSliderTest.kt`, lines 63-66

### Purpose:
Verify that a finger dragged off the slider's right end reads the maximum.

### Assertion expectation:
sliderValueAt(1000, 340, 1, 28) is 28.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a finger past the groove's ends reads as th..."

## Test 306

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 15-18

### Purpose:
Verify that a swipe left on the Draw tab draws.

### Assertion expectation:
swipeKind(horizontal, -50) is Draw.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 307

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 20-23

### Purpose:
Verify that a swipe right on the Draw tab undoes.

### Assertion expectation:
swipeKind(horizontal, 50) is Undo.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 308

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 25-28

### Purpose:
Verify that a swipe up on the Draw tab re-rolls.

### Assertion expectation:
swipeKind(vertical, -50) is Reroll.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 309

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 30-33

### Purpose:
Verify that a swipe down on the Draw tab resets.

### Assertion expectation:
swipeKind(vertical, 50) is Reset.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 310

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 35-38

### Purpose:
Verify that a touch that has barely moved is not yet a swipe.

### Assertion expectation:
swipeAxis(6, -8, slop 10) is null.

### Origin:
The app's former `SwipeTest`, "the axis settles once the finger is past the slop"

## Test 311

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 40-43

### Purpose:
Verify that a swipe is sideways when the finger went further sideways.

### Assertion expectation:
swipeAxis(-12, 5, slop 10) is true.

### Origin:
The app's former `SwipeTest`, "the axis settles once the finger is past the slop"

## Test 312

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 45-48

### Purpose:
Verify that a swipe is up-and-down when the finger went further that way.

### Assertion expectation:
swipeAxis(4, 15, slop 10) is false.

### Origin:
The app's former `SwipeTest`, "the axis settles once the finger is past the slop"

## Test 313

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 50-53

### Purpose:
Verify that an exactly diagonal swipe counts as up-and-down.

### Assertion expectation:
swipeAxis(11, -11, slop 10) is false.

### Origin:
The app's former `SwipeTest`, "the axis settles once the finger is past the slop"

## Test 314

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 55-60

### Purpose:
Verify that a swipe left moves the current civ's icon with the finger, fading it.

### Assertion expectation:
At 0, 30 and 90 px of 120, the icon is at that distance with alpha 1 - distance/120.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 315

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 62-66

### Purpose:
Verify that a swipe left shows no incoming civ, as the draw is not revealed early.

### Assertion expectation:
At 0, 30 and 90 px, swipePlaces(Draw, not acted) has no ghost.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 316

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 68-72

### Purpose:
Verify that undo, re-roll and reset swipes fade the current civ where it is.

### Assertion expectation:
At 30 px of 120, the icon is at distance 0 with alpha 0.75 for all three.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 317

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 74-78

### Purpose:
Verify that undo, re-roll and reset swipes bring the next civ in from the edge.

### Assertion expectation:
At 30 px of 120, the ghost is at distance 90 with alpha 0.25 for all three.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 318

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 80-83

### Purpose:
Verify that once a draw swipe acts, the civ icon shows the new civ in place.

### Assertion expectation:
swipePlaces(Draw, 60 of 120, acted) puts the icon at distance 0, alpha 1.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 319

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 85-88

### Purpose:
Verify that once a draw swipe acts, the old civ stays where the finger left it.

### Assertion expectation:
swipePlaces(Draw, 60 of 120, acted) puts the ghost at distance 60, alpha 0.5.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 320

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 90-93

### Purpose:
Verify that once an undo swipe acts, the civ icon takes the incoming civ's place.

### Assertion expectation:
swipePlaces(Undo, 60 of 120, acted) puts the icon at distance 60, alpha 0.5.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 321

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 95-98

### Purpose:
Verify that once an undo swipe acts, the old civ keeps fading in place.

### Assertion expectation:
swipePlaces(Undo, 60 of 120, acted) puts the ghost at distance 0, alpha 0.5.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 322

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 100-104

### Purpose:
Verify that every swipe ends with the new civ in place and fully shown.

### Assertion expectation:
For all four kinds at full travel, the icon is at distance 0 with alpha 1.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 323

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 106-110

### Purpose:
Verify that every swipe ends with the old civ faded out.

### Assertion expectation:
For all four kinds at full travel, the ghost's alpha is 0.

### Origin:
frcp.net `tests/swipe.test.js`, "phone swipes draw, undo, re-roll and reset, with the icons following..."

## Test 324

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 112-115

### Purpose:
Verify that a lifted swipe glides on at the finger's speed.

### Assertion expectation:
glideMillis(100 px, 1 px/ms) is 200 ms.

### Origin:
The app's former `SwipeTest`, "a lifted swipe glides on from the finger's speed, taking 80 to 350 ms"

## Test 325

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 117-120

### Purpose:
Verify that a slow finger's glide is capped at 350 ms.

### Assertion expectation:
glideMillis(100 px, 0.05 px/ms) is 350 ms.

### Origin:
The app's former `SwipeTest`, "a lifted swipe glides on from the finger's speed, taking 80 to 350 ms"

## Test 326

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 122-125

### Purpose:
Prevent a division by zero when the finger stopped before lifting.

### Assertion expectation:
glideMillis(100 px, 0 px/ms) is 350 ms.

### Origin:
The app's former `SwipeTest`, "a lifted swipe glides on from the finger's speed, taking 80 to 350 ms"

## Test 327

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/SwipeTest.kt`, lines 127-130

### Purpose:
Verify that a fast finger's glide still takes 80 ms, so it can be seen.

### Assertion expectation:
glideMillis(100 px, 10 px/ms) is 80 ms.

### Origin:
The app's former `SwipeTest`, "a lifted swipe glides on from the finger's speed, taking 80 to 350 ms"

## Test 328

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/PoolTabTest.kt`, lines 14-17

### Purpose:
Verify that the Pool tab lists every civ of the newest edition alphabetically.

### Assertion expectation:
civGrid for the newest edition, flattened, equals logic.displayOrder.

### Origin:
The app's former `PoolTabTest`, "the newest edition's grid holds every civ in alphabetical order..."

## Test 329

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/PoolTabTest.kt`, lines 19-23

### Purpose:
Verify that the Pool tab's civ grid has three civs to a row.

### Assertion expectation:
Every row but the last of the newest edition's grid holds 3 places.

### Origin:
The app's former `PoolTabTest`, "the newest edition's grid holds every civ in alphabetical order..."

## Test 330

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/PoolTabTest.kt`, lines 25-29

### Purpose:
Verify that the civ grid keeps its number of rows in older editions.

### Assertion expectation:
civGrid has as many rows for each edition as for the newest.

### Origin:
The app's former `PoolTabTest`, "every civ keeps its place in every edition, and newer civs leave..."

## Test 331

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/PoolTabTest.kt`, lines 31-39

### Purpose:
Verify that civs never move in the grid, and newer civs leave their places empty.

### Assertion expectation:
In each edition, place i holds displayOrder[i] if it is in the edition, else null.

### Origin:
The app's former `PoolTabTest`, "every civ keeps its place in every edition, and newer civs leave..."

## Test 332

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/PoolTabTest.kt`, lines 41-45

### Purpose:
Prevent a hidden DLC toggle that data.json no longer has from going unnoticed.

### Assertion expectation:
Every key in HIDDEN_DLC_TOGGLES is among some edition's dlcToggles.

### Origin:
The app's former `PoolTabTest`, "the DLC toggles left out are toggles data json has..."

## Test 333

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/PoolTabTest.kt`, lines 47-51

### Purpose:
Verify that the shown DLC toggles leave the civ grid its room.

### Assertion expectation:
For each edition, shownDlcToggles needs at most 3 rows of 2.

### Origin:
The app's former `PoolTabTest`, "each edition's DLC toggles fit in three rows of two..."

## Test 334

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 13-16

### Purpose:
Verify that the logs' scrollbar knob is at the top for an unscrolled log.

### Assertion expectation:
scrollKnobTop(0 of 600, free 354) is 0.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the scrollbar's knob starts at the top of i..."

## Test 335

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 18-21

### Purpose:
Verify that the scrollbar knob moves in proportion to the scroll.

### Assertion expectation:
scrollKnobTop(300 of 600, free 354) is 177.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the scrollbar's knob starts at the top of i..."

## Test 336

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 23-26

### Purpose:
Verify that the scrollbar knob is at the bottom for a fully scrolled log.

### Assertion expectation:
scrollKnobTop(600 of 600, at end, free 354) is 354.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the scrollbar's knob starts at the top of i..."

## Test 337

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 28-31

### Purpose:
Verify that the knob reaches the bottom when the list says it is at its end.

### Assertion expectation:
scrollKnobTop(590 of 600, at end, free 354) is 354.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the knob reaches the bottom once the log ca..."

## Test 338

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 33-36

### Purpose:
Prevent a division by zero placing the knob of a log that does not scroll.

### Assertion expectation:
scrollKnobTop(0 of 0, free 354) is 0.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "the knob reaches the bottom once the log ca..."

## Test 339

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 38-41

### Purpose:
Verify that dragging the knob scrolls the log in proportion.

### Assertion expectation:
scrollForKnobDrag(from 0, 177 px, range 600, free 354) is 300.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "dragging the knob scrolls the log in propor..."

## Test 340

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 43-46

### Purpose:
Verify that a knob drag adds to where the log was when the drag began.

### Assertion expectation:
scrollForKnobDrag(from 300, 177 px, range 600, free 354) is 600.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "dragging the knob scrolls the log in propor..."

## Test 341

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 48-51

### Purpose:
Verify that dragging the knob up scrolls the log back.

### Assertion expectation:
scrollForKnobDrag(from 300, -177 px, range 600, free 354) is 0.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "dragging the knob scrolls the log in propor..."

## Test 342

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 53-56

### Purpose:
Verify that a press on the scrollbar's track pages by a page less one line.

### Assertion expectation:
scrollPage(viewport 400, line 20) is 380.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a press on the track pages by a line short..."

## Test 343

### File residence:
`app/src/test/java/net/frcp/app/ui/tabs/UiScrollBarTest.kt`, lines 58-61

### Purpose:
Verify that a press on the track of a very short log still moves a line.

### Assertion expectation:
scrollPage(viewport 30, line 20) is 20.

### Origin:
Desktop app's `ParchmentGeometryTest`; former `ControlsTest`, "a press on the track pages by a line short..."

## Test 344

### File residence:
`app/src/test/java/net/frcp/app/ui/UiContrastTest.kt`, lines 42-45

### Purpose:
Verify that the page's text can be read against its background (WCAG 1.4.3).

### Assertion expectation:
Ink.Text on the page art's average colour has a contrast of at least 4.5 to 1.

### Origin:
frcp.net `tests/wcag.test.js`, "1.4.3, 1.4.6 Contrast (Enhanced): all text is at least 7:1, ..."

## Test 345

### File residence:
`app/src/test/java/net/frcp/app/ui/UiContrastTest.kt`, lines 47-50

### Purpose:
Verify that the swipe hints can be read against its background (WCAG 1.4.3).

### Assertion expectation:
Ink.Brown on the page art's average colour has a contrast of at least 4.5 to 1.

### Origin:
frcp.net `tests/wcag.test.js`, "1.4.3, 1.4.6 Contrast (Enhanced): all text is at least 7:1, ..."

## Test 346

### File residence:
`app/src/test/java/net/frcp/app/ui/UiContrastTest.kt`, lines 52-55

### Purpose:
Verify that a menu's items can be read against its background (WCAG 1.4.3).

### Assertion expectation:
Ink.Cream on Ink.Menu has a contrast of at least 4.5 to 1.

### Origin:
frcp.net `tests/wcag.test.js`, "1.4.3, 1.4.6 Contrast (Enhanced): all text is at least 7:1, ..."

## Test 347

### File residence:
`app/src/test/java/net/frcp/app/ui/UiContrastTest.kt`, lines 57-60

### Purpose:
Verify that a pressed menu item can be read against its background (WCAG 1.4.3).

### Assertion expectation:
Ink.Text on Ink.Gold has a contrast of at least 4.5 to 1.

### Origin:
frcp.net `tests/wcag.test.js`, "1.4.3, 1.4.6 Contrast (Enhanced): all text is at least 7:1, ..."

## Test 348

### File residence:
`app/src/test/java/net/frcp/app/ui/UiContrastTest.kt`, lines 62-65

### Purpose:
Verify that the logs' lines can be read against its background (WCAG 1.4.3).

### Assertion expectation:
Ink.LogText on Ink.LogBack has a contrast of at least 4.5 to 1.

### Origin:
frcp.net `tests/wcag.test.js`, "1.4.3, 1.4.6 Contrast (Enhanced): all text is at least 7:1, ..."

## Test 349

### File residence:
`app/src/test/java/net/frcp/app/ui/UiContrastTest.kt`, lines 67-70

### Purpose:
Verify that a text field's text can be read against its background (WCAG 1.4.3).

### Assertion expectation:
White on Ink.Field has a contrast of at least 4.5 to 1.

### Origin:
frcp.net `tests/wcag.test.js`, "1.4.3, 1.4.6 Contrast (Enhanced): all text is at least 7:1, ..."

## Test 350

### File residence:
`app/src/test/java/net/frcp/app/ui/UiContrastTest.kt`, lines 72-75

### Purpose:
Verify that a status message can be read against its background (WCAG 1.4.3).

### Assertion expectation:
White on Ink.Toast has a contrast of at least 4.5 to 1.

### Origin:
frcp.net `tests/wcag.test.js`, "1.4.3, 1.4.6 Contrast (Enhanced): all text is at least 7:1, ..."

## Test 351

### File residence:
`app/src/test/java/net/frcp/app/rules/CodeRulesTest.kt`, lines 24-32

### Purpose:
Enforce that no Kotlin file or Gradle script has a comment (Rule 10).

### Assertion expectation:
No `*.kt` or `*.kts` file has `//` or `/*` outside a string; the failure lists each file and line.

### Origin:
The app only.

## Test 352

### File residence:
`app/src/test/java/net/frcp/app/rules/CodeRulesTest.kt`, lines 34-42

### Purpose:
Enforce that no function is longer than 60 lines (Rule 11).

### Assertion expectation:
Every `fun` in `*.kt` and `*.kts` spans at most 60 lines; the failure lists file, line, name and length.

### Origin:
The app only.
