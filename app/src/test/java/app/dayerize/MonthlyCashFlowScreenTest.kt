package app.dayerize

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import app.dayerize.features.analytics.AnalyticsScreen
import app.dayerize.ui.DayerizeTheme
import java.time.LocalDate
import org.junit.*

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w411dp-h891dp-xhdpi")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class MonthlyCashFlowScreenTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun monthControlsRespectBoundsAndReset()=checkMonthControls(1f)
    @Test @org.robolectric.annotation.Config(qualifiers="en-rUS-w360dp-h800dp-xhdpi")
    fun monthControlsRemainUsableAtLargeText()=checkMonthControls(2f)
    @Test fun oversizedTotalsShowUnavailableInsteadOfCrashing() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val today=LocalDate.of(2026,3,15)
        val assets=listOf("a","b").map{Asset(it,"workspace",it,Money(Long.MAX_VALUE,"MYR"),"2026-03-01")}
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){AnalyticsScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,today)}}}
        ui.onNodeWithText("Cash flow total is too large to display.").performScrollTo().assertIsDisplayed()
    }
    private fun checkMonthControls(fontScale:Float) {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val today=LocalDate.of(2026,3,15)
        val assets=listOf(Asset("camera","workspace","Camera",Money(100,"MYR"),"2026-01-31"))
        ui.runOnUiThread{ui.activity.setContent{
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale)) {
                DayerizeTheme(true){AnalyticsScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,today)}
            }
        }}
        ui.onNodeWithContentDescription("Next month").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithContentDescription("Previous month").performClick()
        ui.onNodeWithText("February 2026").assertExists()
        ui.onNodeWithText("No purchases or sales this month.").assertExists()
        ui.onNodeWithContentDescription("Previous month").performClick().assertIsNotEnabled()
        ui.onNodeWithText("January 2026").assertExists()
        ui.onNodeWithText("This month").performScrollTo().performClick()
        val month=ui.onNodeWithText("March 2026").performScrollTo()
        val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        month.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult){it(layouts)}
        Assert.assertTrue("Month must remain readable",layouts.isNotEmpty() && layouts.none{it.hasVisualOverflow})
        ui.onNodeWithContentDescription("Next month").assertIsNotEnabled()
    }
}
