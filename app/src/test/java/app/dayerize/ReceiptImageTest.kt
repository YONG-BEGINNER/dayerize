package app.dayerize
import android.graphics.Bitmap
import android.net.Uri
import android.media.ExifInterface
import app.dayerize.receipts.ReceiptImage
import org.junit.Test
import org.junit.Assert.*
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.junit.runner.RunWith
import java.io.File
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptImageTest {
 @Test fun boundsLargeImagesAndHonoursCameraRotation() {
  val ctx=RuntimeEnvironment.getApplication();val f=File(ctx.cacheDir,"receipt.jpg")
  val b=Bitmap.createBitmap(6000,1000,Bitmap.Config.ARGB_8888);f.outputStream().use{b.compress(Bitmap.CompressFormat.JPEG,90,it)};b.recycle()
  val exif=ExifInterface(f.path);exif.setAttribute(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_ROTATE_90.toString());exif.saveAttributes()
  val decoded=ReceiptImage.read(ctx.contentResolver,Uri.fromFile(f))
  try {assertTrue(decoded.height>decoded.width);assertTrue(maxOf(decoded.width,decoded.height)<=2400)}finally{decoded.recycle();f.delete()}
 }
 @Test fun rejectsInvalidOrOversizedInput() {
  val ctx=RuntimeEnvironment.getApplication();val f=File(ctx.cacheDir,"bad-receipt")
  f.writeText("not an image")
  try {ReceiptImage.read(ctx.contentResolver,Uri.fromFile(f));fail("Invalid image accepted")}catch(_:IllegalArgumentException){}
  f.outputStream().use{out->repeat(21){out.write(ByteArray(1024*1024))}}
  try {ReceiptImage.read(ctx.contentResolver,Uri.fromFile(f));fail("Oversized image accepted")}catch(_:IllegalArgumentException){}
  f.delete()
 }
}
