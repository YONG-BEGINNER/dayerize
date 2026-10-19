package app.dayerize
import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.runBlocking
import androidx.test.platform.app.InstrumentationRegistry
import app.dayerize.core.*
import app.dayerize.data.AssetStore
import app.dayerize.backup.FileBackup
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
class RestoreSafetyTest {
    @Test fun emptyArchiveWithInvalidZonePreservesExistingRecords()=runBlocking {
        val ctx=InstrumentationRegistry.getInstrumentation().targetContext
        val store=AssetStore(ctx);store.initialize()
        val a=Asset("safe","${store.settings().getValue("workspace")}","Camera",Money(10000,"MYR"),store.today().toString())
        store.save(a)
        val archive=ImportedArchive(BackupData(workspaceId="backup",assets=emptyList(),preferences=mapOf("timezone" to "INVALID")),emptyMap())
        val result=runCatching { FileBackup(store).restore(archive) }
        assertTrue("Invalid preference must be rejected",result.isFailure)
        assertEquals("safe",store.dao.all().single().id)
        store.database.close()
    }
}
