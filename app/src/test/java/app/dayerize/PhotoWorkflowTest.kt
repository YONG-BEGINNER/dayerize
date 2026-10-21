package app.dayerize
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import app.dayerize.features.editor.*
import app.dayerize.ui.DayerizeTheme
import android.graphics.Bitmap
import org.junit.*
import org.junit.Assert.*
import java.io.File
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class PhotoWorkflowTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun cancellingKeepsOriginalAndApplyWritesSeparateOutput() {
        val directory=File(ui.activity.filesDir,"photo-test").apply{mkdirs()}
        val original=File(directory,"original.png")
        Bitmap.createBitmap(20,10,Bitmap.Config.ARGB_8888).also{it.eraseColor(android.graphics.Color.BLUE)}.let{bitmap->original.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
        val originalBytes=original.readBytes()
        var applied:PhotoEditResult?=null;var cancelled=false
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){PhotoEditorSheet(original.name,directory,{applied=it},{cancelled=true})}}}
        ui.onNodeWithText("Cancel").performClick()
        assertTrue(cancelled);assertNull(applied);assertArrayEquals(originalBytes,original.readBytes())
        ui.waitUntil(10000){ui.onAllNodes(hasText("Apply") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Apply").performClick()
        ui.waitUntil(10000){applied!=null}
        assertEquals(original.name,applied!!.originalName)
        assertNotEquals(original.name,applied!!.renderedName)
        assertTrue(File(directory,applied!!.renderedName).exists())
        assertArrayEquals(originalBytes,original.readBytes())
    }
    @Test fun invalidInputCannotBeApplied() {
        val directory=File(ui.activity.filesDir,"missing-test").apply{mkdirs()}
        var applied=false
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){PhotoEditorSheet("missing.png",directory,{applied=true},{})}}}
        ui.onNodeWithText("Apply").assertIsNotEnabled()
        assertFalse(applied)
    }
}
