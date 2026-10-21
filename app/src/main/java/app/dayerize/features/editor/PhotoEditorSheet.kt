package app.dayerize.features.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import app.dayerize.R
import app.dayerize.ui.*
import app.dayerize.images.*
import coil.compose.AsyncImage
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID

data class PhotoEditResult(val originalName:String,val renderedName:String)
@Composable fun PhotoEditorSheet(originalName:String,directory:File,onApply:(PhotoEditResult)->Unit,onDismiss:()->Unit,loadPreview:suspend(File,Int)->Bitmap?=::loadPhotoPreview) {
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    var cutout by remember{mutableStateOf<File?>(null)}
    var useCutout by remember{mutableStateOf(false)}
    var width by remember{mutableFloatStateOf(0f)}
    var busy by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf(false)}
    val original=File(directory,originalName)
    val source=if(useCutout)cutout ?: original else original
    var previewKey by remember{mutableStateOf<Pair<String,Int>?>(null)}
    var preview by remember{mutableStateOf<Bitmap?>(null)}
    LaunchedEffect(source,width) {
        previewKey=null
        preview=loadPreview(source,width.toInt())
        previewKey=source.path to width.toInt()
    }
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            Column(Modifier.systemBarsPadding().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    ScreenHeading(stringResource(R.string.photo_workspace))
                    AsyncImage(preview,null,Modifier.fillMaxWidth().aspectRatio(1f),contentScale=ContentScale.Fit)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        SelectPill(stringResource(R.string.original),!useCutout){useCutout=false}
                        Button(enabled=!busy,onClick={
                            if(cutout!=null)useCutout=true else scope.launch {
                                busy=true;error=false
                                try { cutout=withContext(Dispatchers.Default){CutoutProcessor(context).process(original,directory)};useCutout=true }
                                catch(e:CancellationException){throw e}
                                catch(e:Exception){error=true;useCutout=false}
                                finally{busy=false}
                            }
                        }){Text(stringResource(R.string.cutout))}
                    }
                    Text(stringResource(R.string.photo_fit))
                    SettingRow(stringResource(R.string.photo_outline),"${width.toInt()} px")
                    Slider(width,{width=it},valueRange=0f..4f,steps=3,enabled=!busy)
                    if(previewKey!=(source.path to width.toInt())) {
                        LinearProgressIndicator(Modifier.fillMaxWidth().testTag("photo-preview-loading"))
                        Text(stringResource(R.string.loading_preview))
                    } else if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
                    if(error || (previewKey==(source.path to width.toInt()) && preview==null))Text(stringResource(R.string.processing_failed),color=MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick=onDismiss,modifier=Modifier.weight(1f)){Text(stringResource(R.string.cancel))}
                    Button(enabled=!busy && preview!=null && previewKey==(source.path to width.toInt()),onClick={scope.launch {
                        busy=true;error=false
                        try {
                            val bitmap=requireNotNull(preview)
                            val file=withContext(Dispatchers.IO) {
                                File(directory,UUID.randomUUID().toString()+".png").also{out->out.outputStream().use{require(bitmap.compress(Bitmap.CompressFormat.PNG,100,it))}}
                            }
                            onApply(PhotoEditResult(originalName,file.name))
                        }catch(e:CancellationException){throw e}catch(e:Exception){error=true}finally{busy=false}
                    }},modifier=Modifier.weight(1f)){Text(stringResource(R.string.apply))}
                }
            }
        }
    }
}

internal suspend fun loadPhotoPreview(source:File,width:Int):Bitmap?=withContext(Dispatchers.Default) {
    runCatching {
        val options=BitmapFactory.Options().apply{inJustDecodeBounds=true}
        BitmapFactory.decodeFile(source.path,options)
        require(options.outWidth in 1..4000 && options.outHeight in 1..4000)
        val image=requireNotNull(BitmapFactory.decodeFile(source.path))
        try{PhotoOutline.render(image,width)}finally{image.recycle()}
    }.getOrNull()
}
