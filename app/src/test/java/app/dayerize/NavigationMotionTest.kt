package app.dayerize

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import org.junit.*

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS-w390dp-h844dp-xhdpi")
class NavigationMotionTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()

    @Test fun collectionAndWishlistKeepSeparateSearchAcrossTabsWithMotion() {
        val vm=ViewModelProvider(ui.activity)[AppViewModel::class.java]
        ui.waitUntil(15000){vm.state.value.loaded}
        val previous=vm.state.value.config["reduceMotion"] ?: "true"
        try {
            runBlocking { vm.store.preference("reduceMotion","false") }
            ui.waitUntil(10000){vm.state.value.config["reduceMotion"]=="false"}
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.search)).performClick()
            ui.onNode(hasSetTextAction()).performTextInput("camera")
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.wishlist)).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.wishlist)).assertIsSelected()
            ui.onAllNodes(hasSetTextAction()).assertCountEquals(0)
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.search)).performClick()
            ui.onNode(hasSetTextAction()).performTextInput("headphones")
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.collection)).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            ui.onNode(hasSetTextAction()).assertTextContains("camera")
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.settings)).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.wishlist)).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.wishlist)).assertIsSelected()
            ui.onNode(hasSetTextAction()).assertTextContains("headphones")
            runBlocking { vm.store.preference("reduceMotion","true") }
            ui.waitUntil(10000){vm.state.value.config["reduceMotion"]=="true"}
            ui.onNodeWithContentDescription(ui.activity.getString(R.string.collection)).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            ui.onNode(hasSetTextAction()).assertTextContains("camera")
        } finally {
            runBlocking { vm.store.preference("reduceMotion",previous) }
        }
    }
}
