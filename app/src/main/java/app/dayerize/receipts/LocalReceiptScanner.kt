package app.dayerize.receipts

import android.content.Context
import android.net.Uri
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.coroutineContext

/** Bundled local OCR. Never retains source receipt images/text or uses a network service. */
class LocalReceiptScanner(private val context:Context) {
    companion object { private val scanLock=Mutex()
        private val hashes=mapOf(
            "eng" to "7d4322bd2a7749724879683fc3912cb542f19906c83bcc1a52132556427170b2",
            "chi_sim" to "a5fcb6f0db1e1d6d8522f39db4e848f05984669172e584e8d76b6b3141e1f730",
            "chi_tra" to "529c5b5797d64b126065cd55f2bb4c7fd7b15790798091b1ff259941a829330b")
    }
    suspend fun recognize(uri:Uri,language:String,cancelled:AtomicBoolean):String=withContext(Dispatchers.IO) {
        scanLock.withLock {
            require(language in setOf("eng","chi_sim+eng","chi_tra+eng"))
            if(cancelled.get())throw CancellationException()
            coroutineContext.ensureActive()
            val bitmap=ReceiptImage.read(context.contentResolver,uri)
            try {
                val directory=installModels(language.split('+'))
                coroutineContext.ensureActive();if(cancelled.get())throw CancellationException()
                val api=TessBaseAPI();val lifecycle=Any();var closed=false
                val timedOut=AtomicBoolean(false);val job=coroutineContext[Job]
                val deadline=System.nanoTime()+45_000_000_000L
                val timer=Timer("receipt-scan-limit",true)
                try {
                    check(api.init(directory.path,language,TessBaseAPI.OEM_LSTM_ONLY)){"Model initialization failed"}
                    api.pageSegMode=TessBaseAPI.PageSegMode.PSM_AUTO
                    api.setImage(bitmap)
                    timer.scheduleAtFixedRate(object:TimerTask(){override fun run(){
                        if(System.nanoTime()>deadline)timedOut.set(true)
                        if(cancelled.get() || job?.isActive==false || timedOut.get())synchronized(lifecycle){if(!closed)api.stop()}
                    }},100,100)
                    // hOCR recognition supports stop(); plain getUTF8Text alone does not.
                    api.getHOCRText(0)
                    coroutineContext.ensureActive();if(cancelled.get())throw CancellationException()
                    check(!timedOut.get()){ "Recognition timed out" }
                    val text=api.utF8Text ?: ""
                    require(text.isNotBlank() && text.length<=100000){"No usable text"}
                    text
                }finally{timer.cancel();synchronized(lifecycle){closed=true;api.recycle()}}
            }finally{bitmap.recycle()}
        }
    }
    private fun installModels(languages:List<String>):File {
        val root=File(context.noBackupFilesDir,"receipt-models-v1");val data=File(root,"tessdata");check(data.isDirectory || data.mkdirs())
        for(language in languages){
            val file=File(data,"$language.traineddata");val expected=hashes.getValue(language)
            if(file.isFile && digest(file)==expected)continue
            val temp=File(data,"$language.pending")
            try {context.assets.open("ocr/$language.traineddata").use{input->temp.outputStream().use{input.copyTo(it)}}
                check(digest(temp)==expected){"Damaged OCR model"};check(temp.renameTo(file)){"Cannot install model"}
            }finally{temp.delete()}
        }
        return root
    }
    private fun digest(file:File):String {
        val hash=MessageDigest.getInstance("SHA-256")
        file.inputStream().use{input->val b=ByteArray(8192);while(true){val n=input.read(b);if(n<0)break;hash.update(b,0,n)}}
        return hash.digest().joinToString(""){"%02x".format(it)}
    }
}
