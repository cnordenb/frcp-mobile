package net.frcp.app

import net.frcp.app.logic.CompatTest
import net.frcp.app.logic.DataTest
import net.frcp.app.logic.DrawTest
import net.frcp.app.logic.PresetTest
import net.frcp.app.logic.ShareTest
import net.frcp.app.logic.StateTest
import net.frcp.app.picker.PickerViewTest
import net.frcp.app.picker.SettingsTest
import net.frcp.app.picker.VmSettingsActionsTest
import net.frcp.app.picker.draws.DrawingTest
import net.frcp.app.picker.draws.VmDrawSoundTest
import net.frcp.app.picker.draws.VmRerollTest
import net.frcp.app.picker.draws.VmResetTest
import net.frcp.app.picker.draws.VmUndoTest
import net.frcp.app.picker.links.LinkTest
import net.frcp.app.picker.links.VmLaunchTest
import net.frcp.app.picker.links.VmOutLinkTest
import net.frcp.app.picker.links.VmPresetTest
import net.frcp.app.picker.pool.PoolTest
import net.frcp.app.picker.pool.VmDlcToggleTest
import net.frcp.app.picker.pool.VmPoolRoundTest
import net.frcp.app.rules.CodeRulesTest
import net.frcp.app.ui.PageArtTest
import net.frcp.app.ui.UiContrastTest
import net.frcp.app.ui.UiTileTest
import net.frcp.app.ui.tabs.PoolTabTest
import net.frcp.app.ui.tabs.SwipeTest
import net.frcp.app.ui.tabs.UiScrollBarTest
import net.frcp.app.ui.widgets.UiSliderTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    CompatTest::class,
    DataTest::class,
    StateTest::class,
    DrawTest::class,
    PresetTest::class,
    ShareTest::class,
    PickerViewTest::class,
    SettingsTest::class,
    VmSettingsActionsTest::class,
    DrawingTest::class,
    VmDrawSoundTest::class,
    VmUndoTest::class,
    VmRerollTest::class,
    VmResetTest::class,
    LinkTest::class,
    VmLaunchTest::class,
    VmPresetTest::class,
    VmOutLinkTest::class,
    PoolTest::class,
    VmDlcToggleTest::class,
    VmPoolRoundTest::class,
    UiTileTest::class,
    PageArtTest::class,
    UiSliderTest::class,
    SwipeTest::class,
    PoolTabTest::class,
    UiScrollBarTest::class,
    UiContrastTest::class,
    CodeRulesTest::class,
)
class AllTests
