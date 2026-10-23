package app.dayerize.features.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.net.Uri
import app.dayerize.R
import app.dayerize.receipts.LocalReceiptScanner
import app.dayerize.ui.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

@Composable fun ReceiptEntry(onApply:(ReceiptFields)->Unit,onDismiss:()->Unit) {
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    var uri by remember{mutableStateOf<Uri?>(null)}
    var language by rememberSaveable{mutableStateOf("eng")}
    var text by remember{mutableStateOf<String?>(null)}
    var busy by remember{mutableStateOf(false)};var failed by remember{mutableStateOf(false)}
    val cancelled=remember{AtomicBoolean(false)}
    DisposableEffect(Unit){onDispose{cancelled.set(true)}}
    val dismiss={cancelled.set(true);onDismiss()}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){if(it!=null){uri=it;failed=false}}
    if(text!=null) {ReceiptReview(requireNotNull(text),onApply,dismiss);return}
    Dialog(onDismissRequest=dismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            Column(Modifier.systemBarsPadding().padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    ScreenHeading(stringResource(R.string.receipt_scan))
                    Text(stringResource(R.string.receipt_local))
                    Panel {
                        if(!busy)ValueChoice(language,listOf("eng","chi_sim+eng","chi_tra+eng"),R.string.language,{key->context.getString(when(key){"chi_sim+eng"->R.string.receipt_simplified;"chi_tra+eng"->R.string.receipt_traditional;else->R.string.receipt_latin})}){language=it}
                        OutlinedButton(onClick={runCatching{picker.launch("image/*")}.onFailure{failed=true}},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.receipt_select))}
                        if(uri!=null)Text(stringResource(R.string.receipt_selected),style=MaterialTheme.typography.bodySmall)
                    }
                    if(busy){LinearProgressIndicator(Modifier.fillMaxWidth());Text(stringResource(R.string.receipt_processing))}
                    if(failed)Text(stringResource(R.string.receipt_failed),color=MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick=dismiss,modifier=Modifier.weight(1f)){Text(stringResource(R.string.cancel))}
                    Button(enabled=uri!=null && !busy,onClick={scope.launch {
                        busy=true;failed=false;cancelled.set(false)
                        try {text=LocalReceiptScanner(context.applicationContext).recognize(requireNotNull(uri),language,cancelled)}
                        catch(e:CancellationException){throw e}
                        catch(e:Exception){failed=true}
                        catch(e:LinkageError){failed=true}
                        finally{busy=false}
                    }},modifier=Modifier.weight(1f)){Text(stringResource(R.string.receipt_scan))}
                }
            }
        }
    }
}
