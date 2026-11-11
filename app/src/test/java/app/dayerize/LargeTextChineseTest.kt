package app.dayerize
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import app.dayerize.ui.AppRoot
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.GraphicsMode
import android.graphics.Bitmap
import java.io.File
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="zh-rCN-w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LargeTextChineseTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    fun shot(name:String) {
        ui.waitForIdle()
        ui.runOnUiThread {
            val root=ui.activity.window.decorView
            val bitmap=Bitmap.createBitmap(root.width,root.height,Bitmap.Config.ARGB_8888)
            root.draw(android.graphics.Canvas(bitmap))
            val f=File("build/test-screenshots/$name.png");f.parentFile!!.mkdirs()
            f.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        }
    }
    @Test fun chineseEditorSaveRemainsReachableAtDoubleTextSize() {
        ui.waitUntil(15000){ui.onAllNodesWithContentDescription("陈列馆").fetchSemanticsNodes().isNotEmpty()}
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.runOnUiThread { ui.activity.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,2f)){AppRoot(vm)}
        } }
        ui.waitForIdle();shot("08-zh-large-collection")
        ui.onNodeWithContentDescription("录入资产").performClick()
        shot("09-zh-large-editor")
        ui.onNodeWithTag("asset-name").performTextInput("测试相机")
        ui.onNodeWithTag("asset-price").performScrollTo().performTextInput("123.45")
        ui.onNodeWithText("保存资产").assertIsDisplayed().performClick()
        ui.waitUntil(10000){vm.state.value.assets.any{it.name=="测试相机"}}
        org.junit.Assert.assertEquals(12345L,vm.state.value.assets.single{it.name=="测试相机"}.purchase.minor)
    }
}
