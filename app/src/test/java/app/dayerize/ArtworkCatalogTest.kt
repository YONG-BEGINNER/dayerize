package app.dayerize

import app.dayerize.artwork.*
import org.junit.Test
import org.junit.Assert.*
import androidx.test.core.app.ApplicationProvider
import android.content.Context

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
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
}
