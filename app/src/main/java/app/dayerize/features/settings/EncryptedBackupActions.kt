package app.dayerize.features.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import app.dayerize.AppViewModel
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.backup.FileBackup
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

@Composable fun EncryptedBackupActions(vm:AppViewModel,onImported:(ImportedArchive)->Unit) {
    val ctx=LocalContext.current;val scope=rememberCoroutineScope()
    var mode by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var confirmation by remember{mutableStateOf("")}
    var pending by remember{mutableStateOf<ByteArray?>(null)};var error by remember{mutableStateOf(false)};var busy by remember{mutableStateOf(false)}
    DisposableEffect(Unit){onDispose{pending?.fill(0)}}
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")){uri->
        val secret=vm.takeExportPassword()
        if(uri==null || secret==null){secret?.fill('\u0000');if(uri!=null)vm.message.value=R.string.failed}else scope.launch{
            busy=true
            try{withContext(Dispatchers.IO){
                val plain=ByteArrayOutputStream().also{FileBackup(vm.store).export(it,EncryptedBackup.LIMIT.toLong())}.toByteArray()
                val encrypted=try{EncryptedBackup.encrypt(plain,secret)}finally{plain.fill(0);secret.fill('\u0000')}
                requireNotNull(ctx.contentResolver.openOutputStream(uri)).use{it.write(encrypted)}
                vm.store.preference("lastExport",java.time.Instant.now().toString())
            };vm.message.value=R.string.saved}catch(e:Exception){vm.message.value=R.string.failed}finally{busy=false;secret.fill('\u0000')}
        }
    }
    val restore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch{
        busy=true
        try{pending=withContext(Dispatchers.IO){requireNotNull(ctx.contentResolver.openInputStream(uri)).use{input->
            val out=ByteArrayOutputStream();val buffer=ByteArray(8192)
            while(true){val n=input.read(buffer);if(n<0)break;require(out.size().toLong()+n<=EncryptedBackup.LIMIT.toLong()+50);out.write(buffer,0,n)}
            out.toByteArray().also{require(EncryptedBackup.isEncrypted(it))}
        }};mode="restore";error=false}catch(e:Exception){vm.message.value=R.string.backup_password_error}finally{busy=false}
    }}
    Text(stringResource(R.string.encrypted_limit),style=MaterialTheme.typography.bodySmall)
    OutlinedButton(enabled=!busy,onClick={mode="export";error=false},modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.encrypted_export))}
    OutlinedButton(enabled=!busy,onClick={restore.launch(arrayOf("application/octet-stream","*/*"))},modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.encrypted_restore))}
    if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
    fun clear(){mode="";password="";confirmation="";pending?.fill(0);pending=null;error=false}
    if(mode.isNotEmpty())AlertDialog(onDismissRequest={if(!busy)clear()},title={Text(stringResource(if(mode=="export")R.string.encrypted_export else R.string.encrypted_restore))},text={Column{
        OutlinedTextField(password,{password=it},label={Text(stringResource(R.string.backup_password))},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),singleLine=true)
        if(mode=="export")OutlinedTextField(confirmation,{confirmation=it},label={Text(stringResource(R.string.password_confirm))},visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),singleLine=true)
        Text(stringResource(R.string.password_note))
        if(error)Text(stringResource(R.string.backup_password_error),color=MaterialTheme.colorScheme.error)
    }},confirmButton={TextButton(enabled=!busy,onClick={
        if(mode=="export") {
            if(password.length<12 || password!=confirmation)error=true else {
                vm.stageExportPassword(password.toCharArray());password="";confirmation="";mode="";export.launch("dayerize-${vm.today()}.day")
            }
        }else scope.launch {
            busy=true;val secret=password.toCharArray()
            try{val archive=withContext(Dispatchers.IO){val plain=EncryptedBackup.decrypt(requireNotNull(pending),secret);try{ArchiveCodec.read(plain.inputStream(),vm.today(),EncryptedBackup.LIMIT)}finally{plain.fill(0)}};clear();onImported(archive)}catch(e:Exception){error=true}finally{secret.fill('\u0000');busy=false}
        }
    }){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(enabled=!busy,onClick={clear()}){Text(stringResource(R.string.cancel))}})
}
