package app.dayerize

import androidx.activity.compose.setContent
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
class CollectionFilterTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun categoryNamedAllIsARealFilterAndResetRestoresEveryCategory() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val assets=listOf(
            Asset("one","workspace","First camera",Money(100,"MYR"),vm.today().toString(),category="All"),
            Asset("two","workspace","Other item",Money(200,"MYR"),vm.today().toString(),category="Tech")
        )
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){CollectionScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,vm.today(),{},{})}}}
        ui.onNodeWithContentDescription("Filter").performClick()
        ui.onNodeWithText("Category").performClick()
        ui.onAllNodesWithText("All").onLast().performClick()
        ui.onNodeWithText("Apply").performScrollTo().performClick()
        ui.onNodeWithText("First camera").assertExists()
        ui.onNodeWithText("Other item").assertDoesNotExist()
        ui.onNodeWithContentDescription("Filter").performClick()
        ui.onNodeWithText("Reset").performScrollTo().performClick()
        ui.onNodeWithText("Apply").performScrollTo().performClick()
        ui.onNodeWithText("Other item").assertExists()
    }


    @Test @org.robolectric.annotation.Config(qualifiers="zh-rCN-w411dp-h891dp-xhdpi")
    fun simplifiedChineseCategoryNamedAllRemainsSelectable()=checkTranslatedCategory()
    @Test @org.robolectric.annotation.Config(qualifiers="zh-rTW-w411dp-h891dp-xhdpi")
    fun traditionalChineseCategoryNamedAllRemainsSelectable()=checkTranslatedCategory()
    @Test @org.robolectric.annotation.Config(qualifiers="ms-rMY-w411dp-h891dp-xhdpi")
    fun malayCategoryNamedAllRemainsSelectable()=checkTranslatedCategory()

    private fun checkTranslatedCategory() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val context=ui.activity
        val category=context.getString(R.string.all)
        val assets=listOf(
            Asset("one","workspace","Selected item",Money(100,"MYR"),vm.today().toString(),category=category),
            Asset("two","workspace","Excluded item",Money(200,"MYR"),vm.today().toString(),category="Other")
        )
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){CollectionScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,vm.today(),{},{})}}}
        ui.onNodeWithContentDescription(context.getString(R.string.filter)).performClick()
        ui.onNodeWithText(context.getString(R.string.category)).performClick()
        ui.onAllNodesWithText(category).onLast().performClick()
        ui.onNodeWithText(context.getString(R.string.apply)).performScrollTo().performClick()
        ui.onNodeWithText("Selected item").assertExists()
        ui.onNodeWithText("Excluded item").assertDoesNotExist()
    }
}
