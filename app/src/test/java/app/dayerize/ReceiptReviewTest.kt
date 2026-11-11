package app.dayerize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import app.dayerize.features.editor.ReceiptReview
import app.dayerize.features.editor.ReceiptFields
import app.dayerize.ui.DayerizeTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="en-rUS-w390dp-h844dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptReviewTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun show(content:@Composable ()->Unit) {
        val vm=androidx.lifecycle.ViewModelProvider(compose.activity)[AppViewModel::class.java]
        compose.waitUntil(15000){vm.state.value.loaded}
        compose.runOnUiThread{compose.activity.setContent{}};compose.waitForIdle()
        compose.runOnUiThread{compose.activity.setContent(content=content)};compose.waitForIdle()
    }
    @Test fun reviewDoesNotApplyUntilExplicitlyConfirmedAndEditsAreUsed() {
        var applied:ReceiptFields?=null
        show { DayerizeTheme(dark=true) { ReceiptReview("Camera Store\nTOTAL MYR 106.00\n2026-10-23",onApply={applied=it},onDismiss={}) } }
        compose.waitForIdle();assertNull(applied)
        compose.onNodeWithText("Recognized text").assertDoesNotExist()
        compose.runOnUiThread {
            val root=android.view.inspector.WindowInspector.getGlobalWindowViews().last{it.isShown && it.width>0}
            val bitmap=android.graphics.Bitmap.createBitmap(root.width,root.height,android.graphics.Bitmap.Config.ARGB_8888)
            root.draw(android.graphics.Canvas(bitmap))
            val f=java.io.File("build/test-screenshots/18-receipt-review.png");f.parentFile!!.mkdirs()
            f.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        }
        compose.onNodeWithTag("receipt-amount").performTextReplacement("99.00")
        compose.onNodeWithTag("receipt-apply").performClick()
        compose.runOnIdle {assertEquals("99.00",applied?.amount);assertEquals("MYR",applied?.currency);assertEquals("2026-10-23",applied?.date)}
    }
    @Test fun ambiguousCurrencyCannotApplyAndDismissDoesNotWrite() {
        var writes=0;var dismissed=false
        show { DayerizeTheme(dark=true) { ReceiptReview("Shop\nTOTAL $ 10.00\n03/04/2026",onApply={writes++},onDismiss={dismissed=true}) } }
        compose.onNodeWithTag("receipt-apply").assertIsNotEnabled()
        compose.onNodeWithTag("receipt-cancel").performClick()
        compose.runOnIdle {assertEquals(0,writes);assertTrue(dismissed)}
    }
}
