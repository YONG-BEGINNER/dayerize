package app.dayerize.artwork

import android.content.Context
import org.json.JSONArray
import java.util.Locale

enum class ArtworkStyle(val key:String) { REALISTIC_3D("3d"), CUTE_2D("cute2d") }
data class ArtworkEntry(val id:String,val category:String,val names:Map<String,String>,val aliases:List<String>,val files:Map<ArtworkStyle,String>,val opticalScale:Float) {
    fun name(localeTag:String)=names[localeTag] ?: names[localeTag.substringBefore('-')] ?: names.getValue("en")
    fun icon(style:ArtworkStyle)="original/$id:${style.key}"
}
class ArtworkCatalog(context:Context) {
    val entries:List<ArtworkEntry>
    init {
        val array=JSONArray(context.assets.open("artwork/catalog.json").bufferedReader().use{it.readText()})
        entries=(0 until array.length()).map { index ->
            val o=array.getJSONObject(index)
            val names=o.getJSONObject("names");val files=o.getJSONObject("files");val aliases=o.getJSONArray("aliases")
            val id=o.getString("id");require(id.matches(Regex("[a-z0-9_-]+")))
            ArtworkEntry(id,o.getString("category"),names.keys().asSequence().associateWith{names.getString(it)},(0 until aliases.length()).map{aliases.getString(it)},ArtworkStyle.entries.filter{files.has(it.key)}.associateWith { style ->
                files.getString(style.key).also { require(it.matches(Regex("artwork/[a-z0-9_-]+\\.png"))) }
            },o.optDouble("opticalScale",1.0).toFloat().also{require(it in 0.5f..1f)})
        }
        require(entries.map{it.id}.distinct().size==entries.size)
    }
    fun search(query:String,category:String?,style:ArtworkStyle,localeTag:String):List<ArtworkEntry> = entries.filter {
        it.id!="fallback" && style in it.files && (category==null || it.category==category) &&
            (it.names.values+it.aliases+it.id).any { name -> name.lowercase(Locale.ROOT).contains(query.trim().lowercase(Locale.ROOT)) }
    }.sortedBy{it.name(localeTag)}
}
