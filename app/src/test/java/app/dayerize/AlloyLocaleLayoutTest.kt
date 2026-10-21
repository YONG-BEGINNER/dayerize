package app.dayerize
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import app.dayerize.core.*
import app.dayerize.ui.*
import app.dayerize.features.collection.CollectionScreen
import org.junit.*
import java.util.Locale
import android.content.res.Configuration
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w411dp-h891dp-xhdpi")
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class AlloyLocaleLayoutTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun collectionKeepsAddAndAmountsAccessibleAcrossLocalesWidthsAndFontScales() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        for(locale in listOf("en","zh-CN","zh-TW","ms"))for(width in listOf(360,390,411))for(font in listOf(1f,1.3f,2f)) {
            val config=Configuration(ui.activity.resources.configuration).apply{setLocale(Locale.forLanguageTag(locale))}
            val context=ui.activity.createConfigurationContext(config)
            var added=false
            val state=AppState(assets=emptyList(),config=mapOf("currency" to "MYR"),loaded=true)
            ui.runOnUiThread{ui.activity.setContent {
                val density=LocalDensity.current
                CompositionLocalProvider(LocalContext provides context,LocalConfiguration provides config,LocalDensity provides Density(density.density,font)) {
                    DayerizeTheme(true){Surface(androidx.compose.ui.Modifier.width(width.dp).fillMaxHeight()){CollectionScreen(state,vm,vm.today(),{added=true},{})}}
                }
            }}
            ui.onNodeWithContentDescription(context.getString(R.string.add_asset)).assertIsDisplayed().performClick()
            org.junit.Assert.assertTrue("$locale $width $font",added)
            ui.onAllNodesWithText("MYR0.00").onFirst().assertIsDisplayed()
        }
    }
    @Test fun largePurchaseAmountNeverEllipsizesInCompactGallery() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val asset=Asset("large","workspace","Large value asset",Money(123456789012,"MYR"),vm.today().toString())
        ui.runOnUiThread{ui.activity.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)) {
                DayerizeTheme(true){Surface(androidx.compose.ui.Modifier.width(360.dp).fillMaxHeight()) {
                    CollectionScreen(AppState(listOf(asset),mapOf("currency" to "MYR"),true),vm,vm.today(),{},{})
                }}
            }
        }}
        val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        ui.onNodeWithText("MYR1,234,567,890.12 · 1 days",useUnmergedTree=true).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult){it(layouts)}
        org.junit.Assert.assertTrue(layouts.isNotEmpty())
        org.junit.Assert.assertFalse("Purchase amount must retain every digit",layouts.any{it.hasVisualOverflow || (0 until it.lineCount).any{line->it.isLineEllipsized(line)}})
    }

}
