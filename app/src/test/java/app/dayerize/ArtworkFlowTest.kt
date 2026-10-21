package app.dayerize

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import app.dayerize.features.editor.IconPicker
import app.dayerize.ui.DayerizeTheme
import app.dayerize.backup.FileBackup
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.ByteArrayOutputStream

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w390dp-h844dp-xhdpi")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class ArtworkFlowTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun originalChoicesApplyAndSurviveArchiveWithLegacyArtwork() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        var selected:String?=null
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){IconPicker({selected=it},{})}}}
        ui.onNodeWithContentDescription("Camera").performClick()
        assertNull(selected)
        ui.onNodeWithText("Apply").performClick()
        assertEquals("original/camera:3d",selected)
        ui.onNodeWithText("Cute 2D").performClick()
        ui.onNodeWithText("Apply").performClick()
        assertEquals("original/camera:cute2d",selected)
        ui.onNode(hasSetTextAction()).performTextInput("rice cooker")
        ui.onNodeWithContentDescription("Rice cooker").performClick()
        assertEquals("original/camera:cute2d",selected)
        ui.onNodeWithText("Apply").performClick()
        assertEquals("original/rice_cooker:cute2d",selected)
        ui.onNodeWithText("3D icons").performClick()
        ui.onNodeWithText("Apply").performClick()
        assertEquals("original/rice_cooker:3d",selected)
        runBlocking {
            vm.store.dao.clear()
            val workspace=vm.store.settings().getValue("workspace")
            listOf("original/camera:3d","original/camera:cute2d","camera:3d","original/rice_cooker:3d","original/rice_cooker:cute2d").forEachIndexed{i,icon->vm.store.save(Asset("art-$i",workspace,"Camera $i",Money(10000,"MYR"),vm.today().toString(),icon=icon))}
            vm.store.preference("artworkStyle","cute2d")
            val bytes=ByteArrayOutputStream();FileBackup(vm.store).export(bytes)
            val archive=ArchiveCodec.read(bytes.toByteArray().inputStream(),vm.today())
            FileBackup(vm.store).restore(archive)
            val restored=vm.store.dao.all().map{vm.store.json.decodeFromString<Asset>(it.payload)}
            assertEquals(setOf("original/camera:3d","original/camera:cute2d","camera:3d","original/rice_cooker:3d","original/rice_cooker:cute2d"),restored.map{it.icon}.toSet())
            assertEquals("cute2d",vm.store.settings()["artworkStyle"])
        }
    }
}
