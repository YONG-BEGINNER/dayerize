package app.dayerize.core
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class BackupTest {
    private val day=LocalDate.of(2026,10,19)
    private val asset=Asset("1","w","相机",Money(1234,"KWD"),"2026-01-01",tags=listOf("旅行"),icon="camera:2d")
    @Test fun roundTripPreservesOriginalInformation() {
        val b=BackupData(workspaceId="w",assets=listOf(asset),preferences=mapOf("language" to "zh-CN"))
        assertEquals(b,BackupCodec.decode(BackupCodec.encode(b),day))
    }
    @Test fun invalidArchivesAreRejected() {
        val b=BackupData(workspaceId="w",assets=listOf(asset))
        for(bad in listOf(b.copy(version=2),b.copy(assets=listOf(asset,asset)),b.copy(assets=listOf(asset.copy(workspaceId="other"))),b.copy(assets=listOf(asset.copy(photo="../private.png"))))) {
            assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode(BackupCodec.encode(bad),day) }
        }
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode("not json",day) }
    }
}
