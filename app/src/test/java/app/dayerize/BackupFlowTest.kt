package app.dayerize
import app.dayerize.backup.FileBackup
import app.dayerize.data.AssetStore
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import java.io.*
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
class BackupFlowTest {
    @Test fun cancelledOrFailedExportNeverRecordsSuccess()=runBlocking {
        val store=AssetStore(InstrumentationRegistry.getInstrumentation().targetContext);store.initialize()
        store.preference("lastExport","previous")
        val backup=FileBackup(store)
        backup.exportAndRecord(null)
        assertEquals("previous",store.settings()["lastExport"])
        assertTrue(runCatching{backup.exportAndRecord(object:OutputStream(){override fun write(b:Int){throw IOException("full")}})}.isFailure)
        assertEquals("previous",store.settings()["lastExport"])
        assertTrue(runCatching{backup.exportAndRecord(object:ByteArrayOutputStream(){override fun close(){throw IOException("close failed")}})}.isFailure)
        assertEquals("previous",store.settings()["lastExport"])
        backup.exportAndRecord(ByteArrayOutputStream())
        assertNotEquals("previous",store.settings()["lastExport"])
        store.database.close()
    }
}
