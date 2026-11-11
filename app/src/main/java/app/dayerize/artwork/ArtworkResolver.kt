package app.dayerize.artwork

import android.content.Context
import org.json.JSONArray

data class ArtworkSource(val path:String,val opticalScale:Float=1f)
class ArtworkResolver(context:Context) {
    private val catalog=ArtworkCatalog(context).entries.associateBy{it.id}
    private val legacy=JSONArray(context.assets.open("icons.json").bufferedReader().use{it.readText()}).let { a -> (0 until a.length()).map{a.getJSONObject(it).getString("id")}.toSet() }
    fun resolve(icon:String):ArtworkSource {
        val id=icon.substringBefore(':');val style=icon.substringAfter(':',"3d")
        if(id.startsWith("original/")) {
            val entry=catalog[id.removePrefix("original/")]
            val key=ArtworkStyle.entries.find{it.key==style}
            entry?.files?.get(key)?.let{return ArtworkSource(it,entry.opticalScale)}
        } else if(id in legacy && style in setOf("3d","2d")) return ArtworkSource("icons/${id}_${style}.webp")
        return ArtworkSource("artwork/fallback_3d.png")
    }
    companion object {
        @Volatile private var instance:ArtworkResolver?=null
        /** One resolver per process: parsing the catalog for every tile made scrolling expensive. */
        fun shared(context:Context):ArtworkResolver=instance ?: synchronized(this){instance ?: ArtworkResolver(context.applicationContext).also{instance=it}}
    }
}
