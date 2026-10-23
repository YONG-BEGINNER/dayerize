package app.dayerize.receipts

import android.content.ContentResolver
import android.net.Uri
import android.graphics.*
import android.media.ExifInterface
import java.io.ByteArrayOutputStream

object ReceiptImage {
    fun read(resolver:ContentResolver,uri:Uri):Bitmap {
        val bytes=resolver.openInputStream(uri)?.use{input->
            val out=ByteArrayOutputStream();val buffer=ByteArray(8192)
            while(true){val n=input.read(buffer);if(n<0)break;require(out.size()+n<=20*1024*1024){"Image too large"};out.write(buffer,0,n)}
            out.toByteArray()
        } ?: throw IllegalArgumentException("Cannot open image")
        val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
        require(bounds.outWidth in 1..20000 && bounds.outHeight in 1..20000){"Invalid image"}
        var sample=1;while(maxOf(bounds.outWidth,bounds.outHeight)/sample>2400)sample*=2
        val decoded=requireNotNull(BitmapFactory.decodeByteArray(bytes,0,bytes.size,BitmapFactory.Options().apply{inSampleSize=sample;inPreferredConfig=Bitmap.Config.ARGB_8888}))
        val orientation=runCatching{ExifInterface(bytes.inputStream()).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)}.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val matrix=Matrix().apply {when(orientation){
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL->setScale(-1f,1f)
            ExifInterface.ORIENTATION_ROTATE_180->setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL->setScale(1f,-1f)
            ExifInterface.ORIENTATION_TRANSPOSE->{setRotate(90f);postScale(-1f,1f)}
            ExifInterface.ORIENTATION_ROTATE_90->setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE->{setRotate(-90f);postScale(-1f,1f)}
            ExifInterface.ORIENTATION_ROTATE_270->setRotate(-90f)
        }}
        val rotated=try{Bitmap.createBitmap(decoded,0,0,decoded.width,decoded.height,matrix,true)}catch(e:Exception){decoded.recycle();throw e}
        if(rotated!==decoded)decoded.recycle()
        try {
            val opaque=Bitmap.createBitmap(rotated.width,rotated.height,Bitmap.Config.ARGB_8888)
            Canvas(opaque).apply{drawColor(Color.WHITE);drawBitmap(rotated,0f,0f,null)}
            return opaque
        }finally{rotated.recycle()}
    }
}
