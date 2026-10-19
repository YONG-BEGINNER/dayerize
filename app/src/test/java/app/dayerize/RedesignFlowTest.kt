package app.dayerize
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w411dp-h891dp-xhdpi")
class RedesignFlowTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun editingLegacyUseAssetPreservesUsageMode() {
        ui.waitUntil(15000) { ui.onAllNodesWithContentDescription("Collection").fetchSemanticsNodes().isNotEmpty() }
        val vm=androidx.lifecycle.ViewModelProvider(ui.activity)[AppViewModel::class.java]
        kotlinx.coroutines.runBlocking {
            val a=app.dayerize.core.Asset("legacy",vm.store.settings().getValue("workspace"),"Legacy camera",app.dayerize.core.Money(10000,"MYR"),vm.today().toString(),useMode=true)
            vm.store.save(a)
        }
        ui.waitUntil(10000){ui.onAllNodesWithText("Legacy camera").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Legacy camera").performClick()
        ui.onNodeWithContentDescription("Edit").performClick()
        ui.onNodeWithTag("asset-name").performTextReplacement("Legacy camera edited")
        ui.onNodeWithText("Save asset").performScrollTo().performClick()
        ui.waitUntil(10000){vm.state.value.assets.any{it.name=="Legacy camera edited"}}
        org.junit.Assert.assertTrue(vm.state.value.assets.single{it.id=="legacy"}.useMode)
    }
    @Test fun unrelatedDailyTargetEditPreservesDuration() {
        ui.waitUntil(15000) { ui.onAllNodesWithContentDescription("Collection").fetchSemanticsNodes().isNotEmpty() }
        val vm=androidx.lifecycle.ViewModelProvider(ui.activity)[AppViewModel::class.java]
        kotlinx.coroutines.runBlocking {
            vm.store.save(app.dayerize.core.Asset("daily",vm.store.settings().getValue("workspace"),"Daily camera",app.dayerize.core.Money(100000,"MYR"),vm.today().toString(),targetDays=334,targetMode="daily"))
        }
        ui.waitUntil(10000){ui.onAllNodesWithText("Daily camera").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Daily camera").performClick()
        ui.onNodeWithContentDescription("Edit").performClick()
        ui.onNodeWithTag("asset-name").performTextReplacement("Daily camera edited")
        ui.onNodeWithText("Save asset").performScrollTo().performClick()
        ui.waitUntil(10000){vm.state.value.assets.any{it.name=="Daily camera edited"}}
        org.junit.Assert.assertEquals(334,vm.state.value.assets.single{it.id=="daily"}.targetDays)
    }
    @Test fun wishlistHasSeparateEntryAndPurchaseFlow() {
        ui.waitUntil(15000) { ui.onAllNodesWithContentDescription("Wishlist").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithContentDescription("Wishlist").performClick()
        ui.onNodeWithContentDescription("Add asset").performClick()
        ui.onNodeWithTag("asset-name").performTextInput("Wish camera")
        ui.onNodeWithTag("asset-price").performTextInput("123.45")
        ui.onNodeWithText("Save asset").performScrollTo().performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("Mark as purchased").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Mark as purchased").performClick()
        ui.onNodeWithText("Save asset").performScrollTo().performClick()
        val vm=androidx.lifecycle.ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(10000) { vm.state.value.assets.any { it.name=="Wish camera" && !it.wishlist } }
        org.junit.Assert.assertEquals(12345L,vm.state.value.assets.single{it.name=="Wish camera"}.purchase.minor)
    }
}
