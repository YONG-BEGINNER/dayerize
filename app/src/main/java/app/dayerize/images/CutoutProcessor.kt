package app.dayerize.images
import android.content.Context
import android.graphics.*
import app.dayerize.imaging.SegmentationModel
import java.io.File
import java.util.UUID

class CutoutProcessor(private val context:Context) {
    fun process(original:File,directory:File):File {
        val image=requireNotNull(BitmapFactory.decodeFile(original.path))
        require(image.width<=4000 && image.height<=4000)
        val small=Bitmap.createScaledBitmap(image,320,320,true)
        val pixels=IntArray(320*320);small.getPixels(pixels,0,320,0,0,320,320)
        val channels=FloatArray(3*320*320)
        val mean=floatArrayOf(.485f,.456f,.406f);val std=floatArrayOf(.229f,.224f,.225f)
        for(i in pixels.indices){val pixel=pixels[i];val alpha=Color.alpha(pixel)/255f
            for(c in 0..2){val value=((pixel ushr (16-8*c) and 255)/255f)*alpha+(1-alpha);channels[c*pixels.size+i]=(value-mean[c])/std[c]}
        }
        val model=context.assets.open("models/u2netp.onnx").use { it.readBytes() }
        val mask=SegmentationModel(model).use { it.predict(channels) }
        val fraction=mask.count { it>.5f }.toFloat()/mask.size
        require(fraction in .005f.. .98f) { "No clear subject found" }
        val maskPixels=IntArray(mask.size) { Color.argb((mask[it]*255).toInt(),255,255,255) }
        val maskSmall=Bitmap.createBitmap(maskPixels,320,320,Bitmap.Config.ARGB_8888)
        val alphaBitmap=Bitmap.createScaledBitmap(maskSmall,image.width,image.height,true)
        val result=image.copy(Bitmap.Config.ARGB_8888,true)
        Canvas(result).drawBitmap(alphaBitmap,0f,0f,Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN) })
        val out=File(directory,UUID.randomUUID().toString()+".png")
        out.outputStream().use { require(result.compress(Bitmap.CompressFormat.PNG,100,it)) }
        result.recycle();if(small!==image)small.recycle();image.recycle();if(alphaBitmap!==maskSmall)alphaBitmap.recycle();maskSmall.recycle()
        return out
    }
}
