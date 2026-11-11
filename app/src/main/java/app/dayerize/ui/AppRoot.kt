package app.dayerize.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dayerize.*
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.backup.FileBackup
import app.dayerize.features.collection.CollectionScreen
import app.dayerize.features.editor.EditorScreen
import app.dayerize.features.detail.DetailScreen
import app.dayerize.features.analytics.AnalyticsScreen
import app.dayerize.features.settings.SettingsScreen
import app.dayerize.features.settings.BackupScreen
import kotlinx.coroutines.*
import java.time.LocalDate

@Composable fun AppRoot(vm:AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle();val message by vm.message.collectAsStateWithLifecycle()
    val ctx=LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) };var selected by rememberSaveable { mutableStateOf<String?>(null) };var editing by rememberSaveable { mutableStateOf((ctx as? android.app.Activity)?.intent?.getBooleanExtra("quick_add",false)==true) }
    var backupPage by rememberSaveable{mutableStateOf(false)}
    var duplicating by rememberSaveable { mutableStateOf(false) }
    var acquiring by rememberSaveable { mutableStateOf(false) }
    var restore by remember { mutableStateOf<ImportedArchive?>(null) }
    val tabState = rememberSaveableStateHolder()
    val snackbar=remember { SnackbarHostState() };val scope=rememberCoroutineScope()
    val today by produceState(vm.today(),state.config["timezone"]) { while(true){value=vm.today();delay(60_000)} }
    LaunchedEffect(state.assets,state.config){if(state.loaded)withContext(Dispatchers.IO){app.dayerize.widgets.OwnershipWidget.update(ctx,state.assets,state.config)}}
    LaunchedEffect(state.config["reminders"]){if(state.loaded)app.dayerize.reminders.ReminderScheduler.update(ctx,state.config["reminders"]=="true")}
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(ctx.getString(it));vm.message.value=null } }
    LaunchedEffect(state.config["language"]) {
        val tag=state.config["language"] ?: "system"
        val requested=if(tag=="system")LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
        if(AppCompatDelegate.getApplicationLocales()!=requested)AppCompatDelegate.setApplicationLocales(requested)
    }
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")){uri->if(uri!=null)vm.launch {
        FileBackup(vm.store).exportAndRecord(requireNotNull(ctx.contentResolver.openOutputStream(uri)));vm.message.value=R.string.saved
    } }
    val import=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch {
        try { restore=withContext(Dispatchers.IO){ctx.contentResolver.openInputStream(uri)!!.use { ArchiveCodec.read(it,vm.today()) }} }
        catch(e:Exception){vm.message.value=R.string.failed}
    } }
    val dark=when(state.config["theme"]){"dark"->true;"light"->false;"system"->isSystemInDarkTheme();else->true}
    DayerizeTheme(dark) {
        val asset=state.assets.find { it.id==selected }
        BackHandler(enabled=selected!=null && !editing){selected=null}
        CompositionLocalProvider(LocalMotionEnabled provides (state.config["reduceMotion"] == "false"),LocalLayoutDensity provides layoutDensity(state.config["density"]),LocalDisplayOptions provides DisplayOptions(state.config["decimals"]?.toIntOrNull(),state.config["grouping"]!="false")) {
        Scaffold(bottomBar={
            if(!editing && !backupPage && selected==null && state.loaded) Surface(Modifier.fillMaxWidth(),shape=AlloyNavigationShape,color=MaterialTheme.colorScheme.surface,border=BorderStroke(0.7.dp,MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().padding(vertical=2.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
                    listOf(R.string.collection to Icons.Outlined.GridView,R.string.wishlist to Icons.Outlined.FavoriteBorder,R.string.analytics to Icons.Outlined.BarChart,R.string.settings to Icons.Outlined.Settings).forEachIndexed { i,(label,icon)->
                        Column(Modifier.weight(1f).selectable(selected=tab==i,onClick={tab=i},role=androidx.compose.ui.semantics.Role.Tab).heightIn(min=56.dp).padding(vertical=6.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            Icon(icon,stringResource(label),Modifier.size(20.dp),tint=if(tab==i)MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(label),fontSize=12.sp,color=if(tab==i)MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        },snackbarHost={SnackbarHost(snackbar)},containerColor=MaterialTheme.colorScheme.background) { padding ->Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !state.loaded->CircularProgressIndicator(Modifier.align(Alignment.Center))
                backupPage->BackupScreen(state.assets.count{!it.wishlist},state.assets.count{it.wishlist},onExport={export.launch("dayerize-${vm.today()}.zip")},onRestore={import.launch(arrayOf("application/zip","application/octet-stream"))},onBack={backupPage=false},lastExport=state.config["lastExport"],extra={app.dayerize.features.settings.EncryptedBackupActions(vm){restore=it}})
                editing->EditorScreen(vm,if(duplicating && asset!=null)remember(asset.id){Ownership.duplicate(asset,java.util.UUID.randomUUID().toString(),today)} else asset,onDone={selected=it;editing=false;acquiring=false;duplicating=false},onBack={editing=false;acquiring=false;duplicating=false},wishDefault=tab==1,acquire=acquiring)
                asset!=null->DetailScreen(asset,vm,today,onBack={selected=null},onEdit={editing=true},onPurchase={acquiring=true;editing=true},onDuplicate={duplicating=true;editing=true})
                else->PrimaryTabFade(tab) {
                    tabState.SaveableStateProvider("primary-tab-$tab") {
                        when (tab) {
                            0,1->CollectionScreen(state,vm,today,onAdd={selected=null;editing=true},onOpen={selected=it.id},wishes=tab==1,onPurchase={selected=it.id;acquiring=true;editing=true},onDuplicate={selected=it.id;duplicating=true;editing=true})
                            2->AnalyticsScreen(remember(state){state.copy(assets=state.assets.filterNot { it.wishlist })},vm,today,onOpen={selected=it.id})
                            else->SettingsScreen(state,vm,onExport={backupPage=true},onRestore={backupPage=true})
                        }
                    }
                }
            }

        } }
        }
        restore?.let { archive->AlertDialog(onDismissRequest={restore=null},title={Text(stringResource(R.string.restore_preview))},text={Text(stringResource(R.string.restore_warning)+"\n"+stringResource(R.string.current_records)+": ${state.assets.size}\n"+stringResource(R.string.incoming_records)+": ${archive.data.assets.size}")},confirmButton={TextButton(onClick={vm.launch { FileBackup(vm.store).restore(archive);vm.message.value=R.string.saved };restore=null;selected=null;editing=false}){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={restore=null}){Text(stringResource(R.string.cancel))}}) }
    }
}
