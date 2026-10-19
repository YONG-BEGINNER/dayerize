package app.dayerize.data

import android.content.Context
import app.dayerize.core.*
import androidx.room.withTransaction
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class AssetStore(val context:Context) {
    val database=AppDatabase.open(context)
    val dao=database.assets()
    val mutex=Mutex()
    val json=Json { encodeDefaults=true }
    val photos=java.io.File(context.filesDir,"photos").apply { mkdirs() }
    suspend fun initialize()=mutex.withLock {
        database.withTransaction {
            val config=dao.config().associate { it.key to it.value }
            if("workspace" !in config) dao.setConfig(ConfigRow("workspace",UUID.randomUUID().toString()))
            if("currency" !in config) dao.setConfig(ConfigRow("currency","MYR"))
            if("timezone" !in config) dao.setConfig(ConfigRow("timezone",ZoneId.systemDefault().id))
        }
    }
    suspend fun settings()=dao.config().associate { it.key to it.value }
    suspend fun today()=LocalDate.now(ZoneId.of(settings()["timezone"] ?: ZoneId.systemDefault().id))
    suspend fun save(asset:Asset)=mutex.withLock {
        require(asset.workspaceId==settings()["workspace"])
        asset.validate(today())
        dao.put(AssetRow(asset.id,asset.workspaceId,json.encodeToString(asset)))
    }
    suspend fun delete(id:String)=mutex.withLock { dao.delete(id) }
    suspend fun preference(key:String,value:String)=mutex.withLock { dao.setConfig(ConfigRow(key,value)) }
    suspend fun renameTag(from:String,to:String)=mutex.withLock {
        require(from.isNotBlank() && to.isNotBlank())
        database.withTransaction { dao.all().forEach { row->
            val a=json.decodeFromString<Asset>(row.payload)
            if(from in a.tags)dao.put(row.copy(payload=json.encodeToString(a.copy(tags=a.tags.map{if(it==from)to else it}.distinct()))))
        } }
    }
    suspend fun renameCategory(from:String,to:String)=mutex.withLock {
        require(to.isNotBlank())
        database.withTransaction { dao.all().forEach { row ->
            val a=json.decodeFromString<Asset>(row.payload)
            if(a.category==from) dao.put(row.copy(payload=json.encodeToString(a.copy(category=to))))
        } }
    }
}
