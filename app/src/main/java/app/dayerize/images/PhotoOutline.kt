package app.dayerize.images

import android.graphics.*

object PhotoOutline {
    fun render(source:Bitmap,widthPx:Int):Bitmap {
        require(widthPx in 0..4 && !source.isRecycled)
        val result=Bitmap.createBitmap(source.width+2*widthPx,source.height+2*widthPx,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(result)
        if(widthPx>0) {
            val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{colorFilter=PorterDuffColorFilter(Color.WHITE,PorterDuff.Mode.SRC_IN)}
            for(y in -widthPx..widthPx)for(x in -widthPx..widthPx)if(x*x+y*y<=widthPx*widthPx)canvas.drawBitmap(source,(x+widthPx).toFloat(),(y+widthPx).toFloat(),paint)
        }
        canvas.drawBitmap(source,widthPx.toFloat(),widthPx.toFloat(),null)
        return result
    }
}
