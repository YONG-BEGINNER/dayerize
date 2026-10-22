package app.dayerize
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import org.junit.*
import org.junit.Assert.*
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
class ExportRotationTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun pendingExportPasswordSurvivesActivityRecreationAndIsConsumedOnce() {
        val first=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        first.stageExportPassword("long secret password".toCharArray())
        ui.activityRule.scenario.recreate()
        val second=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        val password=second.takeExportPassword()
        assertArrayEquals("long secret password".toCharArray(),password)
        assertNull(second.takeExportPassword());password!!.fill('\u0000')
    }
}
