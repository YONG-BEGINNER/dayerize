package app.dayerize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.robolectric.annotation.GraphicsMode
import android.graphics.Bitmap
import java.io.File

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RedesignRenderTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    private fun shot(name:String) {
        ui.waitForIdle()
        lateinit var bitmap:Bitmap
        ui.runOnUiThread {
            val root=android.view.inspector.WindowInspector.getGlobalWindowViews().lastOrNull { it.isShown && it.width>0 } ?: ui.activity.window.decorView
            bitmap=Bitmap.createBitmap(root.width,root.height,Bitmap.Config.ARGB_8888)
            root.draw(android.graphics.Canvas(bitmap))
        }
        val f=File("/workspace/dayerize/docs/redesign/screenshots/$name.png");f.parentFile!!.mkdirs()
        f.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        assertTrue(bitmap.width>300 && bitmap.height>500)
    }
    @Test fun renderAndNavigateSixScreens() {
        ui.waitUntil(15000){ui.onAllNodesWithContentDescription("Collection").fetchSemanticsNodes().isNotEmpty()}
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        val today=vm.today()
        runBlocking {
            vm.store.dao.clear()
            vm.store.preference("theme","dark")
            val workspace=vm.store.settings().getValue("workspace")
            vm.store.save(Asset("sample-one",workspace,"Edifier Comfo Bass",Money(10985,"MYR"),today.minusDays(55).toString(),category="Tech",icon="original/headphones:3d"))
            vm.store.save(Asset("sample-two",workspace,"Anker Soundcore",Money(21675,"MYR"),today.minusDays(192).toString(),category="Tech",icon="original/headphones:3d"))
            vm.store.save(Asset("wish-one",workspace,"Studio headphones",Money(249900,"MYR"),today.toString(),icon="original/headphones:3d",wishlist=true))
            vm.store.save(Asset("sample-three",workspace,"Canon EOS RP",Money(442211,"MYR"),today.minusDays(226).toString(),category="Camera",icon="original/camera:cute2d"))
        }
        ui.waitUntil(10000){ui.onAllNodesWithText("Edifier Comfo Bass").fetchSemanticsNodes().isNotEmpty()}
        repeat(40) { Thread.sleep(100); ui.waitForIdle() }
        shot("01-collection")
        ui.onNodeWithText("Edifier Comfo Bass").performClick()
        shot("04-detail")
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNodeWithContentDescription("Wishlist").performClick()
        shot("02-wishlist")
        ui.onNodeWithContentDescription("Add asset").performClick()
        shot("03-wish-editor")
        ui.onNodeWithText("Asset",useUnmergedTree=true).performClick()
        shot("03-editor")
        ui.onNodeWithText("Purchase date").performScrollTo().performClick()
        shot("10-date-picker")
        ui.onNodeWithText("Confirm").performClick()
        ui.onNodeWithText("Category").performScrollTo().performClick()
        shot("11-category-picker")
        ui.onNodeWithText("Tech").performClick()
        ui.onNodeWithContentDescription("Change image").performClick()
        ui.onNodeWithText("Choose an icon").performClick()
        repeat(40) { Thread.sleep(100); ui.waitForIdle() }
        shot("07-icon-picker")
        ui.onNodeWithText("Cute 2D").performClick()
        repeat(40) { Thread.sleep(100); ui.waitForIdle() }
        shot("15-cute-icon-picker")
        ui.onNodeWithContentDescription("Cancel").performClick()
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNodeWithText("Confirm").performClick()
        ui.onNodeWithContentDescription("Analytics").performClick()
        shot("05-analytics")
        ui.onNodeWithContentDescription("Settings").performClick()
        shot("06-settings")
        ui.onNodeWithText("Language").performScrollTo()
        shot("12-settings-appearance")
        ui.onNodeWithText("Light").performScrollTo().performClick()
        ui.waitUntil(10000){vm.state.value.config["theme"]=="light"}
        ui.onNodeWithContentDescription("Collection").performClick()
        shot("13-light-collection")
        ui.onNodeWithContentDescription("Grid / list").performClick()
        shot("14-list")
    }
}
