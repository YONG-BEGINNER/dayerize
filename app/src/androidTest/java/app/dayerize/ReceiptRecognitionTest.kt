package app.dayerize

import android.graphics.*
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dayerize.receipts.LocalReceiptScanner
import app.dayerize.core.ReceiptParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class ReceiptRecognitionTest {
 @Test fun recognisesBundledModelWithoutNetworkAndParsesSyntheticReceipt()=runBlocking {
  val ctx=InstrumentationRegistry.getInstrumentation().targetContext
  val bitmap=Bitmap.createBitmap(1000,800,Bitmap.Config.ARGB_8888);val canvas=Canvas(bitmap);canvas.drawColor(Color.WHITE)
  val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.BLACK;textSize=42f;typeface=Typeface.MONOSPACE}
  listOf("CAMERA STORE","2026-10-23","Camera case RM 100.00","Tax RM 6.00","TOTAL RM 106.00").forEachIndexed{i,s->canvas.drawText(s,60f,100f+i*95f,paint)}
  val file=File(ctx.cacheDir,"synthetic-receipt.png");file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
  try {
   val text=LocalReceiptScanner(ctx).recognize(Uri.fromFile(file),"eng",AtomicBoolean(false))
   val result=ReceiptParser.parse(text)
   assertEquals("106.00",result.suggestedAmount);assertEquals(setOf("MYR"),result.currencies);assertTrue(result.dates.contains("2026-10-23"))
  }finally{file.delete()}
 }
}
