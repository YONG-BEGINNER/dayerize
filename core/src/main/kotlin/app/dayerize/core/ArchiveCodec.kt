package app.dayerize.core
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.util.zip.*
import java.io.ByteArrayOutputStream
data class ImportedArchive(val data:BackupData,val photos:Map<String,ByteArray>)
object ArchiveCodec {
    private const val LIMIT=128*1024*1024
    fun write(data:BackupData,photos:Map<String,ByteArray>,output:OutputStream) {
        val referenced=data.assets.flatMap { listOfNotNull(it.photo,it.originalPhoto) }.toSet()
        require(photos.keys.containsAll(referenced))
        require(photos.values.sumOf { it.size.toLong() }<=LIMIT)
        ZipOutputStream(output).use { zip->
            fun entry(name:String,bytes:ByteArray){zip.putNextEntry(ZipEntry(name));zip.write(bytes);zip.closeEntry()}
            entry("data.json",BackupCodec.encode(data).toByteArray(Charsets.UTF_8))
            referenced.forEach { name->require(name.matches(Regex("[a-zA-Z0-9_-]+\\.(jpg|png|webp)")));entry("photos/$name",photos.getValue(name)) }
        }
    }
    fun read(input:InputStream,today:LocalDate):ImportedArchive {
        try {
            val entries=mutableMapOf<String,ByteArray>();var total=0L
            ZipInputStream(input).use { zip->
                while(true){val entry=zip.nextEntry ?: break
                    require(!entry.isDirectory && entries.size<10001 && entry.name !in entries)
                    require(entry.name=="data.json" || entry.name.matches(Regex("photos/[a-zA-Z0-9_-]+\\.(jpg|png|webp)")))
                    val out=ByteArrayOutputStream();val buffer=ByteArray(8192)
                    while(true){val n=zip.read(buffer);if(n<0)break;total+=n;require(total<=LIMIT && out.size()+n<=20_000_000);out.write(buffer,0,n)}
                    entries[entry.name]=out.toByteArray();zip.closeEntry()
                }
            }
            val data=BackupCodec.decode(requireNotNull(entries.remove("data.json")).toString(Charsets.UTF_8),today)
            val photos=entries.mapKeys { it.key.removePrefix("photos/") }
            require(photos.keys==data.assets.flatMap { listOfNotNull(it.photo,it.originalPhoto) }.toSet())
            return ImportedArchive(data,photos)
        }catch(e:Exception){throw IllegalArgumentException("Invalid or unsafe archive",e)}
    }
}
