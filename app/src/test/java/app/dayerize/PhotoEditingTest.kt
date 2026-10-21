package app.dayerize
import android.graphics.Bitmap
import android.graphics.Color
import app.dayerize.images.PhotoOutline
import org.junit.Test
import org.junit.Assert.*
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class PhotoEditingTest {
    @Test fun outlineExpandsBoundsWithoutStretchingSource() {
        val source=Bitmap.createBitmap(20,10,Bitmap.Config.ARGB_8888);source.eraseColor(Color.RED)
        val result=PhotoOutline.render(source,4)
        assertEquals(28,result.width);assertEquals(18,result.height)
        assertEquals(Color.RED,result.getPixel(4,4));assertEquals(Color.WHITE,result.getPixel(1,8))
        assertEquals(20,source.width)
    }
    @Test fun zeroWidthPreservesPixelsAndInvalidWidthIsRejected() {
        val source=Bitmap.createBitmap(20,10,Bitmap.Config.ARGB_8888);source.setPixel(4,4,Color.BLUE)
        val result=PhotoOutline.render(source,0)
        assertEquals(20,result.width);assertEquals(10,result.height);assertEquals(Color.BLUE,result.getPixel(4,4))
        assertTrue(runCatching{PhotoOutline.render(source,5)}.isFailure)
    }
}
