package app.dayerize
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.dayerize.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
@RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35],qualifiers="en-rUS")
class PersistenceTest {
    @Test fun recordSurvivesDatabaseReopen()= runBlocking {
        val ctx=InstrumentationRegistry.getInstrumentation().targetContext
        ctx.deleteDatabase("roundtrip.db")
        val row=AssetRow("id","local","{\"name\":\"相机\",\"currency\":\"KWD\",\"minor\":1234,\"icon\":\"camera:3d\"}")
        val first=AppDatabase.open(ctx,"roundtrip.db");first.assets().put(row);first.close()
        val reopened=AppDatabase.open(ctx,"roundtrip.db")
        assertEquals(listOf(row),reopened.assets().all());reopened.close();ctx.deleteDatabase("roundtrip.db");Unit
    }
}
