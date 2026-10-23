package app.dayerize

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import app.dayerize.features.editor.IconPicker
import app.dayerize.ui.DayerizeTheme
import org.junit.*
import org.junit.Assert.*

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w390dp-h844dp-xhdpi")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class PickerPersonalizationTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun favouriteAndRecentSurvivePickerRecreationWithoutApplyingOnPreview() {
        val vm=androidx.lifecycle.ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        ui.activity.getSharedPreferences("artwork_picker",0).edit().clear().commit()
        var applied:String?=null
        fun open(){ui.runOnUiThread{ui.activity.setContent{}};ui.waitForIdle();ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){IconPicker({applied=it},{})}}};ui.waitForIdle()}
        open()
        ui.onNode(hasSetTextAction()).performTextInput("Camera")
        ui.onNodeWithContentDescription("Camera").performClick()
        assertNull(applied)
        ui.onNodeWithContentDescription("Add to favourites").performClick()
        ui.onNodeWithText("Apply").performClick()
        assertEquals("original/camera:3d",applied)
        open()
        ui.onNodeWithText("Favourites").performClick()
        ui.onNodeWithContentDescription("Camera").assertExists()
        ui.onNodeWithContentDescription("Laptop").assertDoesNotExist()
        ui.onNodeWithText("Recent").performClick()
        ui.onNodeWithContentDescription("Camera").assertExists()
    }
}
