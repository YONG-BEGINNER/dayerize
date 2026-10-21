package app.dayerize
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import app.dayerize.features.detail.SaleScreen
import app.dayerize.ui.DayerizeTheme
import org.junit.*
import org.junit.Assert.*
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
class SaleScreenTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun settlementPreviewAndCancellationNeverMutateAsset() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val asset=Asset("sale-test",vm.state.value.config.getValue("workspace"),"Camera",Money(240000,"MYR"),vm.today().minusDays(399).toString())
        kotlinx.coroutines.runBlocking{vm.store.save(asset)}
        var cancelled=false
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){SaleScreen(asset,vm){cancelled=true}}}}
        ui.onNodeWithTag("sale-price").performTextInput("1800")
        ui.onNodeWithTag("sale-net").assertTextContains("1.50",substring=true)
        ui.onNodeWithText("Cancel").performClick()
        assertTrue(cancelled)
        assertNull(vm.state.value.assets.single{it.id==asset.id}.sale)
    }
}
