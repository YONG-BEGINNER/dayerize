package app.dayerize.features.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import app.dayerize.*
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.ui.*
import java.time.*
import java.time.temporal.ChronoUnit
import java.math.RoundingMode
import java.util.UUID
import java.io.File
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlinx.coroutines.*
import app.dayerize.images.CutoutProcessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun EditorScreen(vm:AppViewModel,existing:Asset?,onDone:(String)->Unit,onBack:()->Unit,wishDefault:Boolean=false,acquire:Boolean=false) {
    val ctx=LocalContext.current;val scope=rememberCoroutineScope()
    val editorState by vm.state.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") };var price by rememberSaveable { mutableStateOf(existing?.purchase?.decimal()?.toPlainString() ?: "") }
    var currency by rememberSaveable { mutableStateOf(existing?.purchase?.currency ?: vm.state.value.config["currency"] ?: "MYR") }
    var date by rememberSaveable { mutableStateOf(if(acquire)vm.today().toString() else existing?.purchaseDate ?: vm.today().toString()) }
    var days by rememberSaveable { mutableStateOf((existing?.targetDays ?: 1000).toString()) }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: ctx.getString(R.string.uncategorized)) };var tags by rememberSaveable { mutableStateOf(existing?.tags?.joinToString(", ") ?: "") }
    var notes by rememberSaveable { mutableStateOf(existing?.notes ?: "") };var serial by rememberSaveable { mutableStateOf(existing?.serial ?: "") }
    var channel by rememberSaveable { mutableStateOf(existing?.channel ?: "") };var warranty by rememberSaveable { mutableStateOf(existing?.warranty ?: "") }
    var icon by rememberSaveable { mutableStateOf(existing?.icon ?: "original/fallback:3d") };var photo by rememberSaveable { mutableStateOf(existing?.photo) }
    var originalPhoto by rememberSaveable { mutableStateOf(existing?.originalPhoto ?: existing?.photo) }
    var processedPhoto by rememberSaveable { mutableStateOf(if(existing?.originalPhoto!=null)existing.photo else null) }
    var wish by rememberSaveable { mutableStateOf(if(acquire)false else existing?.wishlist ?: wishDefault) }
    var useMode by rememberSaveable { mutableStateOf(existing?.useMode ?: false) };var endMode by rememberSaveable { mutableStateOf(false) };var endDate by rememberSaveable { mutableStateOf("") }
    var targetMode by rememberSaveable { mutableStateOf(existing?.let { if(it.useMode)"custom" else it.targetMode } ?: "none") };var dailyGoal by rememberSaveable { mutableStateOf(existing?.let { it.purchase.decimal().divide(java.math.BigDecimal(it.targetDays),8,RoundingMode.HALF_UP).toPlainString() } ?: "") }
    var valuation by rememberSaveable { mutableStateOf(existing?.valuation ?: "purchase") };var value by rememberSaveable { mutableStateOf(existing?.manualValue?.let { Money(it,currency).decimal().toPlainString() } ?: "") }
    var residual by rememberSaveable { mutableStateOf(existing?.let { Money(it.residual,currency).decimal().toPlainString() } ?: "0") }
    var location by rememberSaveable{mutableStateOf(existing?.location ?: "")}
    var collections by rememberSaveable{mutableStateOf(existing?.collections?.joinToString(", ") ?: "")}
    var returnBy by rememberSaveable{mutableStateOf(existing?.returnBy ?: "")}
    var maintenance by rememberSaveable{mutableStateOf(existing?.maintenanceDate ?: "")}
    var savings by rememberSaveable{mutableStateOf(existing?.let{Money(it.savingsMinor,currency).decimal().toPlainString()} ?: "0")}
    var cooling by rememberSaveable{mutableStateOf(existing?.coolingOffUntil ?: "")}
    var more by rememberSaveable { mutableStateOf(false) };var picker by remember { mutableStateOf(false) };var error by remember { mutableStateOf(false) };var busy by remember { mutableStateOf(false) };var discard by remember { mutableStateOf(false) }
    var photoEditor by remember{mutableStateOf(false)}
    var sheet by remember { mutableStateOf("") };var datePicker by remember { mutableStateOf(false) };var newCategory by rememberSaveable { mutableStateOf("") }
    val imagePicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri->if(uri!=null)scope.launch {
        busy=true
        try { photo=withContext(Dispatchers.IO) {
            val bytes=ctx.contentResolver.openInputStream(uri)!!.use { input ->
                val out=ByteArrayOutputStream();val buffer=ByteArray(8192);var n=input.read(buffer)
                while(n!=-1){require(out.size()+n<=20_000_000);out.write(buffer,0,n);n=input.read(buffer)};out.toByteArray()
            }
            val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true };BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
            require(bounds.outWidth in 1..20000 && bounds.outHeight in 1..20000)
            val opt=BitmapFactory.Options().apply { inSampleSize=maxOf(1,maxOf(bounds.outWidth,bounds.outHeight)/1600) }
            val bitmap=requireNotNull(BitmapFactory.decodeByteArray(bytes,0,bytes.size,opt))
            val file=File(vm.store.photos,UUID.randomUUID().toString()+".png")
            try { file.outputStream().use { require(bitmap.compress(Bitmap.CompressFormat.PNG,100,it)) };file.name } finally {bitmap.recycle()}
        };originalPhoto=photo;processedPhoto=null }catch(e:Exception){vm.message.value=R.string.processing_failed}finally{busy=false}
    } }
    val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap->if(bitmap!=null)scope.launch {
        busy=true;try { photo=withContext(Dispatchers.IO){val f=File(vm.store.photos,UUID.randomUUID().toString()+".png");f.outputStream().use { require(bitmap.compress(Bitmap.CompressFormat.PNG,100,it)) };f.name};originalPhoto=photo;processedPhoto=null }catch(e:Exception){vm.message.value=R.string.processing_failed}finally{busy=false}
    } }
    BackHandler { discard=true }
    Column(Modifier.fillMaxSize().imePadding()) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().padding(top=8.dp)) {
            IconButton(onClick={discard=true},modifier=Modifier.align(Alignment.CenterStart)){Icon(Icons.Outlined.Close,stringResource(R.string.back))}
            Row(Modifier.align(Alignment.Center),horizontalArrangement=Arrangement.spacedBy(24.dp)) {
                listOf(false to R.string.asset_tab,true to R.string.wish).forEach { (w,label)->
                    Column(Modifier.clickable(enabled=existing==null || existing.wishlist){wish=w},horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(stringResource(label),fontSize=18.sp,fontWeight=FontWeight.Bold,color=if(wish==w)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(Modifier.width(32.dp).height(4.dp),shape=RoundedCornerShape(8.dp),color=if(wish==w)Lime else MaterialTheme.colorScheme.background){}
                    }
                }
            }
        }
        val preview=Asset("preview","preview",name.ifBlank { "Asset" },Money(0,currency),date,icon=icon,photo=photo)
        Box(Modifier.fillMaxWidth().padding(top=8.dp),contentAlignment=Alignment.Center) {
            Box(Modifier.size(112.dp)) {
                AssetArt(preview,Modifier.fillMaxSize().padding(6.dp))
                FilledIconButton(onClick={sheet="image"},modifier=Modifier.align(Alignment.BottomEnd).size(48.dp)){Icon(Icons.Outlined.Edit,stringResource(R.string.choose_image),Modifier.size(18.dp))}
            }
        }
        TextField(name,{name=it},placeholder={Text(stringResource(R.string.item_name),Modifier.fillMaxWidth(),textAlign=TextAlign.Center)},textStyle=LocalTextStyle.current.copy(textAlign=TextAlign.Center,fontWeight=FontWeight.Bold,fontSize=20.sp),modifier=Modifier.fillMaxWidth().testTag("asset-name"),singleLine=true,colors=TextFieldDefaults.colors(focusedContainerColor=MaterialTheme.colorScheme.background,unfocusedContainerColor=MaterialTheme.colorScheme.background,focusedIndicatorColor=MaterialTheme.colorScheme.background,unfocusedIndicatorColor=MaterialTheme.colorScheme.background))
        if(existing==null) {
            val templates=app.dayerize.artwork.ArtworkCatalog(ctx).entries.filter{it.id in listOf("camera","laptop","sedan","refrigerator","shoe")}
            Text(stringResource(R.string.quick_template),style=MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                templates.forEach{t->val title=t.name(androidx.compose.ui.platform.LocalConfiguration.current.locales[0].toLanguageTag())
                    SelectPill(title,icon.startsWith("original/${t.id}:")){
                        if(name.isBlank())name=title
                        icon=t.icon(if(editorState.config["artworkStyle"]=="cute2d")app.dayerize.artwork.ArtworkStyle.CUTE_2D else app.dayerize.artwork.ArtworkStyle.REALISTIC_3D)
                        category=title;photo=null;originalPhoto=null;days=when(t.id){"sedan"->"3650";"refrigerator"->"3000";"shoe"->"365";else->"1000"};targetMode="duration"
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Panel {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.Paid,null,Modifier.size(19.dp));Text(stringResource(if(wish)R.string.budget else R.string.price),Modifier.weight(1f).padding(start=8.dp),fontSize=14.sp)
                TextField(price,{price=it},placeholder={Text("0.00")},singleLine=true,modifier=Modifier.width(140.dp).testTag("asset-price"),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),textStyle=LocalTextStyle.current.copy(textAlign=TextAlign.End),colors=TextFieldDefaults.colors(focusedContainerColor=MaterialTheme.colorScheme.surface,unfocusedContainerColor=MaterialTheme.colorScheme.surface,focusedIndicatorColor=MaterialTheme.colorScheme.surface,unfocusedIndicatorColor=MaterialTheme.colorScheme.surface))
            }
            if(existing==null || acquire || existing.wishlist)Choice(currency,currencies,R.string.currency){currency=it} else SettingRow(stringResource(R.string.currency),currency)
        }
        if(!wish)Panel {
            SettingRow(stringResource(R.string.purchase_date),date,Icons.Outlined.CalendarMonth,onClick={datePicker=true})
            SettingRow(stringResource(R.string.category),category,Icons.Outlined.Category,onClick={sheet="category"})
            SettingRow(stringResource(R.string.tag_title),tags,Icons.Outlined.Sell,onClick={sheet="tags"})
        }
        if(!wish)Panel {
            SettingRow(stringResource(R.string.target),icon=Icons.Outlined.TrackChanges)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                listOf("none" to R.string.not_set,"daily" to R.string.by_price,"duration" to R.string.by_period,"custom" to R.string.custom).forEach { (key,label)->SelectPill(stringResource(label),targetMode==key){targetMode=key} }
            }
            when(targetMode) {
                "daily"->Field(dailyGoal,{dailyGoal=it},R.string.goal_cost,true)
                "duration"->Field(days,{days=it},R.string.target_days,true)
                "custom"->{SettingRow(stringResource(R.string.use_mode),trailing={Switch(useMode,{useMode=it})});if(!useMode)SettingRow(stringResource(R.string.days_mode),trailing={Switch(!endMode,{endMode=!it})});if(endMode&&!useMode)Field(endDate,{endDate=it},R.string.end_date) else Field(days,{days=it},R.string.target_days,true)}
            }
        }
        Panel {
            SettingRow(stringResource(R.string.notes),icon=Icons.Outlined.EditNote)
            TextField(notes,{notes=it},placeholder={Text(stringResource(R.string.notes_hint))},modifier=Modifier.fillMaxWidth().heightIn(min=100.dp),colors=TextFieldDefaults.colors(focusedContainerColor=MaterialTheme.colorScheme.surface,unfocusedContainerColor=MaterialTheme.colorScheme.surface,focusedIndicatorColor=MaterialTheme.colorScheme.surface,unfocusedIndicatorColor=MaterialTheme.colorScheme.surface))
        }
        Panel {
            Field(location,{location=it},R.string.location_label)
            Field(collections,{collections=it},R.string.collections_label)
            if(wish){Field(savings,{savings=it},R.string.savings_amount,true);Field(cooling,{cooling=it},R.string.cooling_off)}
        }
        if(!wish)Panel {
            SettingRow(stringResource(R.string.more),icon=Icons.Outlined.Tune,onClick={more=!more})
            if(more) {
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)) { listOf("purchase" to R.string.purchase_value,"manual" to R.string.manual_value,"linear" to R.string.linear).forEach { (key,label)->SelectPill(stringResource(label),valuation==key){valuation=key} } }
                if(valuation=="manual")Field(value,{value=it},R.string.manual_value,true)
                if(valuation=="linear")Field(residual,{residual=it},R.string.residual,true)
                Field(returnBy,{returnBy=it},R.string.return_by);Field(maintenance,{maintenance=it},R.string.maintenance_date)
                Field(channel,{channel=it},R.string.channel);Field(warranty,{warranty=it},R.string.warranty);Field(serial,{serial=it},R.string.serial)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
    Surface(shadowElevation=8.dp,color=MaterialTheme.colorScheme.background) {
        Column {
        if(error)Text(stringResource(R.string.check_fields),Modifier.padding(horizontal=24.dp,vertical=4.dp),color=MaterialTheme.colorScheme.error)
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
        Button(onClick={
            try {
                val purchase=Money.parse(price,currency)
                val target=when {wish || targetMode=="none"->1000;targetMode=="daily"->{if(existing!=null && existing.targetMode=="daily" && purchase==existing.purchase && dailyGoal==existing.purchase.decimal().divide(java.math.BigDecimal(existing.targetDays),8,RoundingMode.HALF_UP).toPlainString())existing.targetDays else {val goal=dailyGoal.toBigDecimal();require(goal.signum()>0);purchase.decimal().divide(goal,0,RoundingMode.CEILING).intValueExact().coerceAtLeast(1)}};targetMode=="custom" && endMode && !useMode->(ChronoUnit.DAYS.between(LocalDate.parse(date),LocalDate.parse(endDate))+1).toInt();else->days.toInt()}
                val a=(existing ?: Asset(UUID.randomUUID().toString(),vm.state.value.config.getValue("workspace"),name,purchase,date)).copy(name=name.trim(),purchase=purchase,purchaseDate=if(wish)vm.today().toString()else date,targetDays=target,category=category.trim(),tags=tags.split(',').map(String::trim).filter(String::isNotEmpty),notes=notes,serial=serial,channel=channel,warranty=warranty,icon=icon,photo=photo,useMode=!wish&&targetMode=="custom"&&useMode,valuation=valuation,manualValue=if(valuation=="manual")Money.parse(value,currency).minor else null,residual=if(valuation=="linear")Money.parse(residual,currency).minor else 0,originalPhoto=originalPhoto,wishlist=wish,targetMode=targetMode,location=location.trim(),collections=collections.split(',').map(String::trim).filter(String::isNotEmpty).distinct(),returnBy=returnBy,maintenanceDate=maintenance,savingsMinor=if(wish)Money.parse(savings,currency).minor else 0,coolingOffUntil=if(wish)cooling else "")
                a.validate(vm.today());busy=true;vm.save(a,onError={busy=false;error=true}){busy=false;onDone(a.id)}
            }catch(e:Exception){error=true;busy=false}
        },enabled=!busy,modifier=Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp).heightIn(min=54.dp)){Text(stringResource(R.string.save_asset),fontWeight=FontWeight.Bold)}
    }
    }
    }
    if(photoEditor && originalPhoto!=null)PhotoEditorSheet(requireNotNull(originalPhoto),vm.store.photos,onApply={originalPhoto=it.originalName;photo=it.renderedName;processedPhoto=it.renderedName;photoEditor=false},onDismiss={photoEditor=false})
    if(picker)IconPicker(defaultStyle=vm.state.value.config["artworkStyle"] ?: "3d",onSelect={icon=it;photo=null;originalPhoto=null;processedPhoto=null;picker=false},onDismiss={picker=false})
    if(sheet.isNotBlank())ModalBottomSheet(onDismissRequest={sheet=""},containerColor=MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(20.dp).navigationBarsPadding().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            when(sheet) {
                "image"->{
                    Text(stringResource(R.string.choose_image),fontSize=22.sp,fontWeight=FontWeight.Bold)
                    SettingRow(stringResource(R.string.icons),icon=Icons.Outlined.Widgets,onClick={sheet="";picker=true})
                    SettingRow(stringResource(R.string.photo),icon=Icons.Outlined.Image,onClick={sheet="";imagePicker.launch("image/*")})
                    SettingRow(stringResource(R.string.camera),icon=Icons.Outlined.CameraAlt,onClick={sheet="";runCatching{camera.launch(null)}.onFailure{vm.message.value=R.string.processing_failed}})
                    if(photo!=null)SettingRow(stringResource(R.string.photo_workspace),icon=Icons.Outlined.AutoAwesome,onClick={sheet="";photoEditor=true})
                }
                "category"->{
                    Text(stringResource(R.string.category_pick),fontSize=22.sp,fontWeight=FontWeight.Bold)
                    (listOf(ctx.getString(R.string.uncategorized))+editorState.assets.map{it.category}).distinct().forEach { c->
                        Surface(onClick={category=c;sheet=""},shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surfaceVariant) { Row(Modifier.fillMaxWidth().padding(18.dp)){Text(c,Modifier.weight(1f));if(c==category)Icon(Icons.Outlined.CheckCircle,null)} }
                    }
                    Field(newCategory,{newCategory=it},R.string.new_category)
                    Button(onClick={if(newCategory.isNotBlank()){category=newCategory.trim();sheet=""}},modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.confirm))}
                }
                "tags"->{Field(tags,{tags=it},R.string.tags);Button(onClick={sheet=""},modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.confirm))}}
            }
        }
    }
    if(datePicker) {
        val initial=runCatching { LocalDate.parse(date) }.getOrElse { vm.today() }
        val now=vm.today()
        var year by rememberSaveable { mutableIntStateOf(initial.year.coerceAtMost(now.year)) }
        var month by rememberSaveable { mutableIntStateOf(initial.monthValue) }
        var day by rememberSaveable { mutableIntStateOf(initial.dayOfMonth) }
        val maxMonth=if(year==now.year)now.monthValue else 12
        val chosenMonth=month.coerceAtMost(maxMonth)
        val maxDay=if(year==now.year && chosenMonth==now.monthValue)now.dayOfMonth else YearMonth.of(year,chosenMonth).lengthOfMonth()
        val chosenDay=day.coerceAtMost(maxDay)
        ModalBottomSheet(onDismissRequest={datePicker=false},containerColor=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(20.dp).navigationBarsPadding(),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                Text(stringResource(R.string.purchase_date),fontSize=22.sp,fontWeight=FontWeight.Bold)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    DateWheel(year,minOf(1900,initial.year),now.year,Modifier.weight(1.4f)){year=it}
                    DateWheel(chosenMonth,1,maxMonth,Modifier.weight(1f)){month=it}
                    DateWheel(chosenDay,1,maxDay,Modifier.weight(1f)){day=it}
                }
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick={datePicker=false},modifier=Modifier.weight(1f).heightIn(min=52.dp)){Text(stringResource(R.string.cancel))}
                    Button(onClick={date=LocalDate.of(year,chosenMonth,chosenDay).toString();datePicker=false},modifier=Modifier.weight(1f).heightIn(min=52.dp)){Text(stringResource(R.string.confirm))}
                }
            }
        }
    }
    if(discard)AlertDialog(onDismissRequest={discard=false},title={Text(stringResource(R.string.discard))},confirmButton={TextButton(onClick=onBack){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={discard=false}){Text(stringResource(R.string.cancel))}})
}
@Composable fun Field(value:String,onChange:(String)->Unit,label:Int,numeric:Boolean=false) {
    OutlinedTextField(value,onChange,label={Text(stringResource(label))},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),keyboardOptions=KeyboardOptions(keyboardType=if(numeric)KeyboardType.Decimal else KeyboardType.Text))
}

@Composable private fun DateWheel(value:Int,min:Int,max:Int,modifier:Modifier,onValue:(Int)->Unit) {
    val dark=MaterialTheme.colorScheme.background==AlloyTokens.Background
    androidx.compose.ui.viewinterop.AndroidView(
        factory={context->android.widget.NumberPicker(android.view.ContextThemeWrapper(context,if(dark)android.R.style.Theme_Material else android.R.style.Theme_Material_Light)).apply {
            minValue=min;maxValue=max;this.value=value;wrapSelectorWheel=false
            descendantFocusability=android.widget.NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener{_,_,new->onValue(new)}
        }},
        update={it.minValue=min;it.maxValue=max;it.value=value},
        modifier=modifier.height(190.dp)
    )
}
