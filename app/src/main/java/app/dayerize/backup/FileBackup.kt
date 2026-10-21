package app.dayerize.backup

import app.dayerize.core.*
import app.dayerize.data.*
import androidx.room.withTransaction
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.*
import java.util.UUID
import java.time.ZoneId

class FileBackup(private val store:AssetStore) {
    suspend fun exportAndRecord(output:OutputStream?) {
        if(output==null)return
        output.use{export(it)}
        store.preference("lastExport",java.time.Instant.now().toString())
    }
    suspend fun export(output:OutputStream)=store.mutex.withLock {
        val config=store.settings()
        val assets=store.dao.all().map { store.json.decodeFromString<Asset>(it.payload) }
        val rates=config["rates"]?.let { store.json.decodeFromString<List<SavedRate>>(it) } ?: emptyList()
        val data=BackupData(workspaceId=config.getValue("workspace"),assets=assets,rates=rates,preferences=config.filterKeys { it in safeSettings })
        val photos=assets.flatMap { listOfNotNull(it.photo,it.originalPhoto) }.distinct().associateWith { File(store.photos,it).readBytes() }
        ArchiveCodec.write(data,photos,output)
    }
    suspend fun restore(archive:ImportedArchive)=store.mutex.withLock {
        // Validate before any mutation. Newly staged images never overwrite existing files.
        val checked=BackupCodec.decode(BackupCodec.encode(archive.data),store.today())
        val prefs=checked.preferences.filterKeys { it in safeSettings }
        prefs["currency"]?.let { Money(0,it) }
        prefs["timezone"]?.let { zone->val zoneId=ZoneId.of(zone);checked.assets.forEach { it.validate(java.time.LocalDate.now(zoneId)) } }
        require(prefs["language"] in listOf(null,"system","en","zh-CN","zh-TW","ms"))
        require(prefs["theme"] in listOf(null,"system","light","dark"))
        require(prefs["decimals"] in listOf(null,"0","1","2","3"))
        require(prefs["artworkStyle"] in listOf(null,"3d","cute2d"))
        require(prefs["grouping"] in listOf(null,"true","false"))
        val staged=mutableListOf<File>()
        try {
            val mapped=archive.photos.mapValues { (old,bytes)->
                val bounds=android.graphics.BitmapFactory.Options().apply {inJustDecodeBounds=true}
                android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
                require(bounds.outWidth in 1..20000 && bounds.outHeight in 1..20000)
                val file=File(store.photos,UUID.randomUUID().toString()+"."+old.substringAfterLast('.'));staged+=file
                FileOutputStream(file).use { it.write(bytes);it.fd.sync() };file.name
            }
            val workspace=store.settings().getValue("workspace")
            store.database.withTransaction {
                store.dao.clear()
                checked.assets.forEach { original->
                    val a=original.copy(workspaceId=workspace,photo=original.photo?.let { mapped.getValue(it) },originalPhoto=original.originalPhoto?.let { mapped.getValue(it) })
                    store.dao.put(AssetRow(a.id,workspace,store.json.encodeToString(a)))
                }
                prefs.forEach { (key,value)->store.dao.setConfig(ConfigRow(key,value)) }
                store.dao.setConfig(ConfigRow("rates",store.json.encodeToString(checked.rates)))
            }
            // Old photos are retained: a crash before the transaction leaves old data intact;
            // a crash after it leaves all new photos present. Orphans can be cleaned separately.
        }catch(e:Exception){staged.forEach { it.delete() };throw e}
    }
    companion object { val safeSettings=setOf("currency","timezone","language","theme","reduceMotion","decimals","grouping","artworkStyle") }
}
