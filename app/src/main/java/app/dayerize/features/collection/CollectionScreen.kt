package app.dayerize.features.collection

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import app.dayerize.*
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.ui.*
import java.time.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalFoundationApi::class,ExperimentalMaterial3Api::class)
@Composable fun CollectionScreen(state:AppState,vm:AppViewModel,today:LocalDate,onAdd:()->Unit,onOpen:(Asset)->Unit,wishes:Boolean=false,onPurchase:(Asset)->Unit=onOpen) {
    var search by rememberSaveable(wishes) { mutableStateOf("") };var showSearch by rememberSaveable(wishes) { mutableStateOf(false) };var list by rememberSaveable { mutableStateOf(false) }
    var status by remember { mutableStateOf<Status?>(null) };var category by rememberSaveable(wishes) { mutableStateOf<String?>(null) }
    var tag by rememberSaveable(wishes){mutableStateOf<String?>(null)}
    var sort by remember{mutableStateOf(CollectionSort.PURCHASE_DATE)}
    var ascending by remember{mutableStateOf(false)}
    val largeText=LocalDensity.current.fontScale>1.3f
    var filters by remember { mutableStateOf(false) };var menu by remember { mutableStateOf<Asset?>(null) }
    val source=state.assets.filter { it.wishlist==wishes }
    val scoped=source.filter { category==null || it.category==category }
    val code=state.config["currency"] ?: "MYR"
    val items=CollectionQuery.apply(source,category,if(wishes)null else status,setOfNotNull(tag),search,sort,ascending,today,code,vm.fxRates())
    val summary=remember(scoped,code,state.config,today) { runCatching { summarize(scoped.map { if(wishes)it.copy(wishlist=false)else it },code,vm.fxRates(),today) }.getOrNull() }
    val daily=remember(scoped,code,state.config,today) { runCatching {
        var total=BigDecimal.ZERO
        for(a in scoped.filter { it.status==Status.ACTIVE && !it.useMode }) {
            val cost=CostCalculator.calculate(a,today).current ?: continue
            val rate=if(a.purchase.currency==code)BigDecimal.ONE else vm.fxRates().filter { it.from==a.purchase.currency && it.to==code && it.date<=today }.maxByOrNull { it.date }?.rate ?: return@runCatching null
            total+=cost*rate
        };total
    }.getOrNull() }
    LazyVerticalGrid(columns=GridCells.Fixed(if(list || wishes || largeText)1 else 2),contentPadding=PaddingValues(start=16.dp,end=16.dp,top=4.dp,bottom=150.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item(span={GridItemSpan(maxLineSpan)}) { ScreenHeading(if(wishes)stringResource(R.string.wishlist) else "Dayerize") { IconButton(onClick={showSearch=!showSearch}){Icon(Icons.Outlined.Search,stringResource(R.string.search))}; FilledIconButton(onClick=onAdd,shape=RoundedCornerShape(12.dp)){Icon(Icons.Outlined.Add,stringResource(R.string.add_asset))} } }
        if(showSearch)item(span={GridItemSpan(maxLineSpan)}) { OutlinedTextField(search,{search=it},placeholder={Text(stringResource(R.string.search))},modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(24.dp),leadingIcon={Icon(Icons.Outlined.Search,null)}) }
        item(span={GridItemSpan(maxLineSpan)}) { Panel(Modifier.fillMaxWidth()) {
            if(largeText) {
                Label(if(wishes)R.string.budget else R.string.estimated_value)
                Text(summary?.let{displayMoney(it.total)} ?: "—",fontSize=24.sp,fontWeight=FontWeight.Bold)
                if(!wishes){Label(R.string.daily_total);Text(formatCost(daily,code),fontSize=24.sp,fontWeight=FontWeight.Bold)}
            } else Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) { Label(if(wishes)R.string.budget else R.string.estimated_value);Text(summary?.let { displayMoney(it.total) } ?: "—",fontSize=22.sp,fontWeight=FontWeight.Bold) }
                if(!wishes)Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) { Label(R.string.daily_total);Text(formatCost(daily,code),fontSize=22.sp,fontWeight=FontWeight.Bold) }
            }
            if(summary?.missing?.isNotEmpty()==true)Text(stringResource(R.string.incomplete_total)+" · "+summary.missing.joinToString(),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(!wishes) {
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { listOf(Status.ACTIVE,Status.RETIRED,Status.SOLD).forEach { s->
                    val n=scoped.count { it.status==s }
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(statusLabel(s))+" $n",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)
                    }
                } }
            }
        } }
        if(!wishes) {
            item(span={GridItemSpan(maxLineSpan)}) { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(22.dp)) {
                (listOf<String?>(null)+source.map { it.category }.distinct()).forEach { c->Column(Modifier.clickable { category=c }.heightIn(min=48.dp).padding(vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(c ?: stringResource(R.string.all),fontWeight=FontWeight.Bold,color=if(category==c)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,fontSize=16.sp)
                    Box(Modifier.width(28.dp).height(4.dp).clip(RoundedCornerShape(4.dp)).background(if(category==c)Lime else MaterialTheme.colorScheme.background))
                } }
            } }
            item(span={GridItemSpan(maxLineSpan)}) { Row(verticalAlignment=Alignment.CenterVertically) {
                Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    (listOf<Status?>(null)+Status.entries).forEach { s->SelectPill(stringResource(s?.let(::statusLabel) ?: R.string.all),status==s){status=s} }
                }
                IconButton(onClick={filters=true}){Icon(Icons.Outlined.FilterAlt,stringResource(R.string.filter))}
                IconButton(onClick={list=!list}){Icon(if(list)Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,stringResource(R.string.layout))}
            } }
        }
        if(sort==CollectionSort.DAILY_COST && items.any{CollectionQuery.dailyCost(it,today,code,vm.fxRates())==null})item(span={GridItemSpan(maxLineSpan)}){Text(stringResource(R.string.sort_unavailable),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        if(items.isEmpty()) item(span={GridItemSpan(maxLineSpan)}) { Column(Modifier.fillMaxWidth().padding(vertical=48.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            AssetArt(Asset("box","preview","",Money(0,code),today.toString(),icon="package:3d"),Modifier.size(84.dp))
            Text(stringResource(if(source.isEmpty())if(wishes)R.string.empty_wish else R.string.empty else R.string.no_results),Modifier.padding(top=16.dp),fontWeight=FontWeight.Bold)
            TextButton(onClick=onAdd){Text(stringResource(R.string.empty_hint),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        } }
        items(items,key={it.id}) { a ->
            val cost=runCatching { CostCalculator.calculate(a,today) }.getOrNull()
            if(wishes)Panel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    AssetArt(a,Modifier.size(96.dp).clickable{onOpen(a)})
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Text(a.name,Modifier.clickable{onOpen(a)},fontWeight=FontWeight.Bold)
                        Text(displayMoney(a.purchase),fontWeight=FontWeight.Bold)
                        OutlinedButton(onClick={onPurchase(a)}){Text(stringResource(R.string.purchase_wish),fontSize=12.sp)}
                    }
                }
            } else if(list || largeText)Surface(shape=AlloyPanelShape,color=MaterialTheme.colorScheme.surface,border=BorderStroke(0.7.dp,MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().combinedClickable(onClick={onOpen(a)},onLongClick={menu=a}).padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                    AssetArt(a,Modifier.size(64.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Text(a.name,fontWeight=FontWeight.SemiBold,fontSize=15.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                        Text(displayMoney(a.purchase)+" · ${cost?.days ?: 0} "+stringResource(R.string.days),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatCost(cost?.net ?: cost?.current,a.purchase.currency)+stringResource(if(a.sale==null && a.useMode)R.string.per_use else R.string.per_day),fontSize=16.sp,fontWeight=FontWeight.Bold)
                    }
                    Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else
            Surface(shape=AlloyPanelShape,color=MaterialTheme.colorScheme.surface,border=BorderStroke(0.7.dp,MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.combinedClickable(onClick={onOpen(a)},onLongClick={menu=a}).padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().height(144.dp)) {
                        AssetArt(a,Modifier.fillMaxSize().padding(top=22.dp).align(Alignment.BottomCenter))
                        if(!wishes)Box(Modifier.align(Alignment.TopEnd)){StatusBadge(a.status)}
                    }
                    Text(a.name,fontWeight=FontWeight.SemiBold,fontSize=15.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(displayMoney(a.purchase)+(if(!wishes)" · ${cost?.days ?: 0} "+stringResource(R.string.days) else ""),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if(wishes)stringResource(R.string.view) else formatCost(cost?.net ?: cost?.current,a.purchase.currency)+stringResource(if(a.sale==null && a.useMode)R.string.per_use else R.string.per_day),Modifier.padding(top=10.dp),fontWeight=FontWeight.Bold,fontSize=17.sp)
                }
            }
        }
    }
    if(filters) {
        val allCategoriesLabel=stringResource(R.string.all_categories)
        var draftTag by remember{mutableStateOf(tag)}
        var draftCategory by remember{mutableStateOf(category)}
        var draftStatus by remember{mutableStateOf(status)}
        var draftSort by remember{mutableStateOf(sort)}
        var draftAscending by remember{mutableStateOf(ascending)}
        ModalBottomSheet(onDismissRequest={filters=false},containerColor=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp).navigationBarsPadding().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.filter),style=MaterialTheme.typography.titleLarge)
                ValueChoice(draftCategory,listOf<String?>(null)+source.map{it.category}.distinct(),R.string.category,{it ?: allCategoriesLabel}){draftCategory=it}
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    (listOf<Status?>(null)+Status.entries).forEach { value->SelectPill(stringResource(value?.let(::statusLabel) ?: R.string.all),draftStatus==value){draftStatus=value} }
                }
                Label(R.string.tag_title)
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    SelectPill(stringResource(R.string.all),draftTag==null){draftTag=null}
                    source.flatMap{it.tags}.distinct().forEach{value->SelectPill(value,draftTag==value){draftTag=value}}
                }
                Label(R.string.sort_by)
                CollectionSort.entries.forEach { value->SelectPill(stringResource(when(value){CollectionSort.PURCHASE_DATE->R.string.purchase_date;CollectionSort.NAME->R.string.name;CollectionSort.DAILY_COST->R.string.daily_cost}),draftSort==value){draftSort=value} }
                SettingRow(stringResource(R.string.ascending),trailing={Switch(draftAscending,{draftAscending=it})})
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                    OutlinedButton(onClick={draftTag=null;draftCategory=null;draftStatus=null;draftSort=CollectionSort.PURCHASE_DATE;draftAscending=false},modifier=Modifier.weight(1f)){Text(stringResource(R.string.reset))}
                    Button(onClick={tag=draftTag;category=draftCategory;status=draftStatus;sort=draftSort;ascending=draftAscending;filters=false},modifier=Modifier.weight(1f)){Text(stringResource(R.string.apply))}
                }
            }
        }
    }
    menu?.let { a->AlertDialog(onDismissRequest={menu=null},title={Text(a.name)},confirmButton={TextButton(onClick={menu=null;onOpen(a)}){Text(stringResource(R.string.view))}},dismissButton={TextButton(onClick={menu=null}){Text(stringResource(R.string.cancel))}}) }
}
