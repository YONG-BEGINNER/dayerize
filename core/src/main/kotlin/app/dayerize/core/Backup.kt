package app.dayerize.core
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.math.BigDecimal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable data class SavedRate(val from: String,val to: String,val value: String,val date: String)
@Serializable data class BackupData(val version: Int=1,val workspaceId: String,val assets: List<Asset>,val rates: List<SavedRate> = emptyList(),val preferences: Map<String,String> = emptyMap())
object BackupCodec {
    private val json=Json { encodeDefaults=true }
    fun encode(data: BackupData): String = json.encodeToString(data)
    fun decode(text: String,today: LocalDate): BackupData {
        require(text.length<=20_000_000)
        try {
            val data=json.decodeFromString<BackupData>(text)
            require(data.version==1 && data.workspaceId.isNotBlank() && data.assets.size<=10000)
            require(data.assets.map { it.id }.toSet().size==data.assets.size)
            data.assets.forEach { require(it.workspaceId==data.workspaceId);it.validate(today) }
            data.rates.forEach { FxRate(it.from,it.to,BigDecimal(it.value),LocalDate.parse(it.date));require(LocalDate.parse(it.date)<=today) }
            return data
        } catch(e:Exception) { throw IllegalArgumentException("Invalid or unsupported backup",e) }
    }
}
