package app.dayerize
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
class AssetFlowTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun collectionNavigationAndCreation() {
        ui.waitUntil(15000) { ui.onAllNodesWithContentDescription("Collection").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithContentDescription("Collection").assertExists()
        ui.onNodeWithContentDescription("Add asset").performClick()
        ui.onNodeWithTag("asset-name").performTextInput("Test camera")
        ui.onNodeWithTag("asset-price").performTextInput("123.45")
        ui.onNodeWithText("Save asset").performScrollTo().performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("Test camera").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Test camera").assertExists()
    }
}
