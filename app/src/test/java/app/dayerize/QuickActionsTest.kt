package app.dayerize

import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import app.dayerize.features.collection.CollectionScreen
import app.dayerize.ui.DayerizeTheme
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import org.junit.*

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w411dp-h891dp-xhdpi")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class QuickActionsTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    private fun viewModel():AppViewModel {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        return vm
    }

    @Test fun dueItemsAreListedSoonestFirstAndOpenTheItem() {
        val vm=viewModel();val today=vm.today()
        val assets=listOf(
            Asset("later","workspace","Laptop",Money(100,"MYR"),today.toString(),maintenanceDate=today.plusDays(5).toString()),
            Asset("soon","workspace","Camera",Money(100,"MYR"),today.toString(),warranty=today.plusDays(2).toString()),
            Asset("far","workspace","Phone",Money(100,"MYR"),today.toString(),warranty=today.plusDays(40).toString()))
        var opened:String?=null
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){CollectionScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,today,{},{opened=it.id})}}}
        ui.onNodeWithText("Due soon").assertExists()
        ui.onNodeWithText("Maintenance · "+today.plusDays(5)).assertExists()
        ui.onNodeWithText("Warranty ends · "+today.plusDays(40)).assertDoesNotExist()
        ui.onNodeWithText("Warranty ends · "+today.plusDays(2)).performClick()
        ui.runOnIdle{Assert.assertEquals("soon",opened)}
    }

    @Test fun nothingDueShowsNoPanel() {
        val vm=viewModel();val today=vm.today()
        val assets=listOf(Asset("one","workspace","Camera",Money(100,"MYR"),today.toString()))
        ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){CollectionScreen(AppState(assets,mapOf("currency" to "MYR"),true),vm,today,{},{})}}}
        ui.onNodeWithText("Camera").assertExists()
        ui.onNodeWithText("Due soon").assertDoesNotExist()
    }

    @Test fun longPressTogglesWidgetChoiceAndDeletesAfterConfirmation() {
        val vm=viewModel();val today=vm.today()
        val id="quick-"+java.util.UUID.randomUUID()
        val asset=Asset(id,vm.state.value.config.getValue("workspace"),"Quick camera",Money(100,"MYR"),today.toString())
        try {
            runBlocking{vm.store.save(asset)}
            ui.waitUntil(15000){vm.state.value.assets.any{it.id==id}}
            ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){val live=vm.state.collectAsState().value;CollectionScreen(live.copy(assets=live.assets.filter{it.id==id}),vm,today,{},{})}}}
            ui.onNodeWithText("Quick camera").performTouchInput{longClick()}
            ui.onNodeWithText("Duplicate item").assertDoesNotExist()
            ui.onNodeWithText("Show in widget").performClick()
            ui.waitUntil(15000){vm.state.value.assets.first{it.id==id}.favourite}
            ui.onNodeWithText("Quick camera").performTouchInput{longClick()}
            ui.onNodeWithText("Remove from widget").assertExists()
            ui.onNodeWithText("Delete").performClick()
            ui.waitUntil(15000){vm.state.value.assets.any{it.id==id}}
            ui.onNodeWithText("Confirm").performClick()
            ui.waitUntil(15000){vm.state.value.assets.none{it.id==id}}
        } finally {
            runBlocking{vm.store.delete(id)}
        }
    }

    @Test fun cardCostUsesReportingCurrencyOnlyWhenARateIsSaved() {
        val vm=viewModel();val today=vm.today()
        val previous=vm.state.value.config["rates"]
        val assets=listOf(Asset("usd","workspace","Lens",Money(10000,"USD"),today.toString()),Asset("eur","workspace","Tripod",Money(10000,"EUR"),today.toString()))
        try {
            vm.addRate(SavedRate("USD","MYR","4",today.toString()))
            ui.waitUntil(15000){vm.fxRates().any{it.from=="USD" && it.rate.compareTo(BigDecimal("4"))==0}}
            ui.runOnUiThread{ui.activity.setContent{DayerizeTheme(true){CollectionScreen(vm.state.collectAsState().value.copy(assets=assets),vm,today,{},{})}}}
            // USD 100 held one day at 4 MYR is 400 a day; the EUR item has no rate and stays at 100.
            ui.onNode(hasText("Lens") and hasText("400.00",substring=true)).assertExists()
            ui.onNode(hasText("Tripod") and hasText("400.00",substring=true)).assertDoesNotExist()
            ui.onNodeWithText("Tripod").assertExists()
        } finally {
            runBlocking{vm.store.preference("rates",previous ?: "[]")}
        }
    }
}
