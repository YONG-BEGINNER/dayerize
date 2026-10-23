package app.dayerize

import app.dayerize.artwork.*
import org.junit.Test
import org.junit.Assert.*
import androidx.test.core.app.ApplicationProvider
import android.content.Context

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class ArtworkCatalogTest {
    private val context:Context get()=ApplicationProvider.getApplicationContext()
    @Test fun resolverPreservesLegacyAndRejectsUnknownPaths() {
        val resolver=ArtworkResolver(context)
        assertEquals("icons/camera_3d.png",resolver.resolve("camera:3d").path)
        assertEquals("artwork/camera_3d.png",resolver.resolve("original/camera:3d").path)
        assertEquals(resolver.resolve("unknown"),resolver.resolve("../../private:3d"))
        assertTrue(resolver.resolve("unknown").path.startsWith("artwork/"))
    }
    @Test fun searchesLocalizedNamesAndOnlyAvailableStyles() {
        val catalog=ArtworkCatalog(context)
        assertTrue(catalog.search("相机",null,ArtworkStyle.CUTE_2D,"zh-CN").any{it.id=="camera"})
        assertTrue(catalog.search("camera","Vehicles",ArtworkStyle.REALISTIC_3D,"en").isEmpty())
        catalog.search("",null,ArtworkStyle.CUTE_2D,"en").forEach { entry ->
            context.assets.open(entry.files.getValue(ArtworkStyle.CUTE_2D)).use { assertTrue(it.read()!=-1) }
        }
    }

    @Test fun everydayCatalogHasBothStylesAndSearchableLocalizedNames() {
        val catalog=ArtworkCatalog(context)
        val expected=setOf("tablet","earbuds","smartwatch","monitor","keyboard","mouse","speaker","game_console","power_bank","camera_lens","drone","television","air_conditioner","refrigerator","washing_machine","vacuum","air_fryer","rice_cooker","desk","chair","suitcase","handbag","sunglasses","motorcycle")
        assertTrue("Everyday assets must be selectable",catalog.entries.map{it.id}.containsAll(expected))
        assertTrue(catalog.entries.size>=100)
        for(entry in catalog.entries) {
            for(style in ArtworkStyle.entries) {
                val path=entry.files.getValue(style)
                val decoded=context.assets.open(path).use{android.graphics.BitmapFactory.decodeStream(it)}
                assertNotNull("Decode $path",decoded)
                assertTrue(decoded.width==decoded.height)
                assertTrue("Alpha channel in $path",decoded.hasAlpha())
                assertEquals(path,ArtworkResolver(context).resolve(entry.icon(style)).path)
                decoded.recycle()
            }
            for(locale in listOf("en","zh-CN","zh-TW","ms")) {
                assertTrue(catalog.search(entry.name(locale),entry.category,ArtworkStyle.CUTE_2D,locale).any{it.id==entry.id})
            }
        }
    }
}
