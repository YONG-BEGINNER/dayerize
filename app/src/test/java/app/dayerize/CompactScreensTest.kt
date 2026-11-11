package app.dayerize

import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import app.dayerize.ui.AppRoot
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import app.dayerize.features.collection.CollectionScreen
import app.dayerize.features.settings.SettingsScreen
import app.dayerize.ui.DayerizeTheme
import org.junit.*

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w390dp-h844dp-xhdpi")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class CompactScreensTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun scrappedCountAndFilterAreAccessible() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val date=vm.today().toString()
        val assets=listOf(
            Asset("active","workspace","Working camera",Money(100,"MYR"),date),
            Asset("scrapped","workspace","Broken camera",Money(100,"MYR"),date,status=Status.SCRAPPED,sale=Sale(Money(0,"MYR"),date)))
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){CollectionScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,vm.today(),{},{})}}}
        ui.onNodeWithText(ui.activity.getString(R.string.scrapped)+" 1").assertIsDisplayed()
        ui.onAllNodesWithText(ui.activity.getString(R.string.scrapped),useUnmergedTree=true).onFirst().performScrollTo().performClick()
        ui.onNodeWithText("Broken camera").assertExists()
        ui.onNodeWithText("Working camera").assertDoesNotExist()
        shot("collection-"+ui.activity.resources.configuration.locales[0].toLanguageTag())
    }
    @Test @org.robolectric.annotation.Config(qualifiers="zh-rCN-w390dp-h844dp-xhdpi")
    fun chineseScrappedRemainsReachable()=scrappedCountAndFilterAreAccessible()

    @Test @org.robolectric.annotation.Config(qualifiers="en-rUS-w360dp-h800dp-xhdpi")
    fun categoryLabelDoesNotCollapseAtLargeText() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        ui.runOnUiThread{ui.activity.setContent{
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,2f)) {
                DayerizeTheme(true){CollectionScreen(AppState(emptyList(),emptyMap(),true),vm,vm.today(),{},{})}
            }
        }}
        val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        ui.onNodeWithText("Category",useUnmergedTree=true).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult){it(layouts)}
        Assert.assertTrue("Category label must remain readable",layouts.isNotEmpty() && layouts.all{it.lineCount<=2 && !it.hasVisualOverflow})
    }
    @Test @org.robolectric.annotation.Config(qualifiers="en-rUS-w360dp-h800dp-xhdpi")
    fun lastSettingsTextClearsNavigationAtLargeText() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        ui.runOnUiThread{ui.activity.setContent{
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,2f)){AppRoot(vm)}
        }}
        ui.onNodeWithContentDescription("Settings").performClick()
        val last=ui.onNodeWithText(ui.activity.getString(R.string.local_only)).performScrollTo()
        val nav=ui.onNodeWithContentDescription("Settings")
        Assert.assertTrue("Last settings text must clear bottom navigation",last.fetchSemanticsNode().boundsInRoot.bottom<=nav.fetchSemanticsNode().boundsInRoot.top)
        shot("settings-large")
    }

    private fun shot(name:String) {
        ui.waitForIdle()
        ui.runOnUiThread {
            val view=ui.activity.window.decorView
            val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val file=java.io.File("build/test-screenshots/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
        }
    }
    @Test fun decimalChoicesAreInlineAndPersist() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){SettingsScreen(vm.state.collectAsState().value,vm,{},{})}}}
        val previous=vm.state.value.config["decimals"] ?: "2"
        try {
            ui.onNodeWithText("3").assertIsDisplayed().performClick()
            ui.waitUntil(10000){vm.state.value.config["decimals"]=="3"}
            ui.onNodeWithText("3").assertIsSelected()
            Assert.assertEquals("3",kotlinx.coroutines.runBlocking{vm.store.settings()["decimals"]})
            ui.onAllNodes(hasSetTextAction()).assertCountEquals(0)
            shot("settings")
        } finally {
            kotlinx.coroutines.runBlocking{vm.store.preference("decimals",previous)}
        }
    }
}
