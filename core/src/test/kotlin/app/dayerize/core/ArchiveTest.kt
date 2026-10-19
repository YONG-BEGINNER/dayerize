package app.dayerize.core
import org.junit.Assert.*
import org.junit.Test
import java.io.*
import java.time.LocalDate
import java.util.zip.*
class ArchiveTest {
    val day=LocalDate.of(2026,10,19)
    @Test fun archivePreservesPhotoBytesAndMetadata() {
        val asset=Asset("a","w","Camera",Money(1200,"USD"),"2026-01-01",photo="photo.png")
        val data=BackupData(workspaceId="w",assets=listOf(asset))
        val out=ByteArrayOutputStream();ArchiveCodec.write(data,mapOf("photo.png" to byteArrayOf(1,2,3)),out)
        val restored=ArchiveCodec.read(out.toByteArray().inputStream(),day)
        assertEquals(data,restored.data);assertArrayEquals(byteArrayOf(1,2,3),restored.photos["photo.png"])
    }
    @Test fun rejectsTraversalMissingPhotosAndGarbage() {
        val out=ByteArrayOutputStream()
        ZipOutputStream(out).use { it.putNextEntry(ZipEntry("../secret"));it.write(byteArrayOf(1));it.closeEntry() }
        assertThrows(IllegalArgumentException::class.java) { ArchiveCodec.read(out.toByteArray().inputStream(),day) }
        assertThrows(IllegalArgumentException::class.java) { ArchiveCodec.read(byteArrayOf(1,2).inputStream(),day) }
        val a=Asset("a","w","A",Money(0,"JPY"),"2026-01-01",photo="missing.png")
        assertThrows(IllegalArgumentException::class.java) { ArchiveCodec.write(BackupData(workspaceId="w",assets=listOf(a)),emptyMap(),ByteArrayOutputStream()) }
    }
}
