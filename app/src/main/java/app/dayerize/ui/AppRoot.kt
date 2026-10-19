package app.dayerize.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.*
import java.time.LocalDate

@Composable fun AppRoot(vm:AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle();val message by vm.message.collectAsStateWithLifecycle()
    val ctx=LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) };var selected by rememberSaveable { mutableStateOf<String?>(null) };var editing by rememberSaveable { mutableStateOf(false) }
    var acquiring by rememberSaveable { mutableStateOf(false) }
    var restore by remember { mutableStateOf<ImportedArchive?>(null) }
    val snackbar=remember { SnackbarHostState() };val scope=rememberCoroutineScope()
    val today by produceState(vm.today(),state.config["timezone"]) { while(true){value=vm.today();delay(60_000)} }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(ctx.getString(it));vm.message.value=null } }
    LaunchedEffect(state.config["language"]) {
        val tag=state.config["language"] ?: "system"
        val requested=if(tag=="system")LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
        if(AppCompatDelegate.getApplicationLocales()!=requested)AppCompatDelegate.setApplicationLocales(requested)
    }
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")){uri->if(uri!=null)vm.launch {
        ctx.contentResolver.openOutputStream(uri)!!.use { FileBackup(vm.store).export(it) };vm.message.value=R.string.saved
    } }
    val import=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch {
        try { restore=withContext(Dispatchers.IO){ctx.contentResolver.openInputStream(uri)!!.use { ArchiveCodec.read(it,vm.today()) }} }
        catch(e:Exception){vm.message.value=R.string.failed}
    } }
    val dark=when(state.config["theme"]){"dark"->true;"light"->false;"system"->isSystemInDarkTheme();else->true}
    DayerizeTheme(dark) {
        val asset=state.assets.find { it.id==selected }
        BackHandler(enabled=selected!=null && !editing){selected=null}
        CompositionLocalProvider(LocalDisplayOptions provides DisplayOptions(state.config["decimals"]?.toIntOrNull(),state.config["grouping"]!="false")) {
        Scaffold(snackbarHost={SnackbarHost(snackbar)},containerColor=MaterialTheme.colorScheme.background) { padding ->Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !state.loaded->CircularProgressIndicator(Modifier.align(Alignment.Center))
                editing->EditorScreen(vm,asset,onDone={selected=it;editing=false;acquiring=false},onBack={editing=false;acquiring=false},wishDefault=tab==1,acquire=acquiring)
                asset!=null->DetailScreen(asset,vm,today,onBack={selected=null},onEdit={editing=true},onPurchase={acquiring=true;editing=true})
                tab==0 || tab==1->CollectionScreen(state,vm,today,onAdd={selected=null;editing=true},onOpen={selected=it.id},wishes=tab==1,onPurchase={selected=it.id;acquiring=true;editing=true})
                tab==2->AnalyticsScreen(state.copy(assets=state.assets.filterNot { it.wishlist }),vm,today)
                else->SettingsScreen(state,vm,onExport={export.launch("dayerize-${LocalDate.now()}.zip")},onRestore={import.launch(arrayOf("application/zip","application/octet-stream"))})
            }
            if(!editing && selected==null && state.loaded) Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                Surface(Modifier.weight(1f),shape=RoundedCornerShape(40.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),shadowElevation=8.dp) {
                    Row(Modifier.padding(horizontal=8.dp,vertical=8.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
                        listOf(R.string.collection to Icons.Outlined.Home,R.string.wishlist to Icons.Outlined.FavoriteBorder,R.string.analytics to Icons.Outlined.PieChart,R.string.settings to Icons.Outlined.Settings).forEachIndexed { i,(label,icon)->
                            IconButton(onClick={tab=i},modifier=Modifier.clip(RoundedCornerShape(18.dp)).background(if(tab==i)MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent)) { Icon(icon,stringResource(label),tint=if(tab==i)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=.55f)) }
                        }
                    }
                }
                Surface(onClick={selected=null;acquiring=false;editing=true},shape=RoundedCornerShape(50),color=MaterialTheme.colorScheme.primaryContainer,contentColor=MaterialTheme.colorScheme.onPrimaryContainer,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
                    Box(Modifier.size(64.dp),contentAlignment=Alignment.Center) { Icon(Icons.Outlined.Add,stringResource(R.string.add_asset),Modifier.size(30.dp),tint=androidx.compose.ui.graphics.Color.Black) }
                }
            }
        } }
        }
        restore?.let { archive->AlertDialog(onDismissRequest={restore=null},title={Text(stringResource(R.string.restore))},text={Text(stringResource(R.string.restore_warning)+"\n${archive.data.assets.size} ${stringResource(R.string.assets)}")},confirmButton={TextButton(onClick={vm.launch { FileBackup(vm.store).restore(archive);vm.message.value=R.string.saved };restore=null;selected=null;editing=false}){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={restore=null}){Text(stringResource(R.string.cancel))}}) }
    }
}
