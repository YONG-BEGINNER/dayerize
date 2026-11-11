package app.dayerize

import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import app.dayerize.features.collection.CollectionScreen
import app.dayerize.ui.DayerizeTheme
import org.junit.*

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w411dp-h891dp-xhdpi")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class CollectionEnhancementsTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    private lateinit var vm:AppViewModel
    private val visible=mutableStateOf(true)
    private fun show() {
        vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val assets=listOf(Asset("one","workspace","Camera",Money(100,"MYR"),vm.today().toString(),category="Tech"))
        ui.runOnUiThread { ui.activity.setContent { DayerizeTheme(true) {
            val holder=rememberSaveableStateHolder()
            if(visible.value)holder.SaveableStateProvider("collection") {
                CollectionScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,vm.today(),{},{})
            }
        } } }
    }
    @Test fun unmatchedSearchOffersResetAndRestoresItems() {
        show()
        ui.onNodeWithContentDescription(ui.activity.getString(R.string.search)).performClick()
        ui.onNode(hasSetTextAction()).performTextInput("unmatched")
        ui.onNodeWithText("Camera").assertDoesNotExist()
        ui.onNodeWithText("Reset search and filters").performScrollTo().performClick()
        ui.onNodeWithText("Camera").assertExists()
        ui.onNodeWithText("Clear search and filters").assertDoesNotExist()
    }
    @Test fun hiddenSearchRemainsVisibleAsRemovableChip() {
        show()
        ui.onNodeWithContentDescription(ui.activity.getString(R.string.search)).performClick()
        ui.onNode(hasSetTextAction()).performTextInput("unmatched")
        ui.onNodeWithContentDescription(ui.activity.getString(R.string.search)).performClick()
        ui.onNodeWithText("Search: unmatched").performClick()
        ui.onNodeWithText("Camera").assertExists()
    }
    @Test fun navigationRestoresFilters() {
        show()
        ui.onNodeWithContentDescription(ui.activity.getString(R.string.search)).performClick()
        ui.onNode(hasSetTextAction()).performTextInput("unmatched")
        ui.runOnIdle{visible.value=false}
        ui.waitForIdle()
        ui.runOnIdle{visible.value=true}
        ui.onNodeWithText("Search: unmatched").assertExists()
        ui.onNodeWithText("Camera").assertDoesNotExist()
    }
    @Test fun layoutChoiceIsStored() {
        show()
        ui.onNodeWithContentDescription(ui.activity.getString(R.string.layout)).performClick()
        ui.waitUntil(15000){vm.state.value.config["collectionList"]=="true"}
        ui.onNodeWithContentDescription(ui.activity.getString(R.string.layout)).performClick()
        ui.waitUntil(15000){vm.state.value.config["collectionList"]=="false"}
    }
}
