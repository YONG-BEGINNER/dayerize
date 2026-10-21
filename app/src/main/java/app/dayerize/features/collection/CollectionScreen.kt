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
    var filters by remember { mutableStateOf(false) };var menu by remember { mutableStateOf<Asset?>(null) }
    val source=state.assets.filter { it.wishlist==wishes }
    val scoped=source.filter { category==null || it.category==category }
    val items=scoped.filter { a -> (wishes || status==null || a.status==status) && (tag==null || tag in a.tags) && listOf(a.name,a.notes,a.serial,a.tags.joinToString(" ")).any { it.contains(search,true) } }
    val code=state.config["currency"] ?: "MYR"
    val summary=remember(scoped,code,state.config,today) { runCatching { summarize(scoped.map { if(wishes)it.copy(wishlist=false)else it },code,vm.fxRates(),today) }.getOrNull() }
    val daily=remember(scoped,code,state.config,today) { runCatching {
        var total=BigDecimal.ZERO
        for(a in scoped.filter { it.status==Status.ACTIVE && !it.useMode }) {
            val cost=CostCalculator.calculate(a,today).current ?: continue
            val rate=if(a.purchase.currency==code)BigDecimal.ONE else vm.fxRates().filter { it.from==a.purchase.currency && it.to==code && it.date<=today }.maxByOrNull { it.date }?.rate ?: return@runCatching null
            total+=cost*rate
        };total
    }.getOrNull() }
    LazyVerticalGrid(columns=GridCells.Fixed(if(list || wishes)1 else 2),contentPadding=PaddingValues(start=16.dp,end=16.dp,top=4.dp,bottom=110.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item(span={GridItemSpan(maxLineSpan)}) { ScreenHeading(if(wishes)stringResource(R.string.wishlist) else "Dayerize") { IconButton(onClick={showSearch=!showSearch}){Icon(Icons.Outlined.Search,stringResource(R.string.search))}; FilledIconButton(onClick=onAdd,shape=RoundedCornerShape(12.dp)){Icon(Icons.Outlined.Add,stringResource(R.string.add_asset))} } }
        if(showSearch)item(span={GridItemSpan(maxLineSpan)}) { OutlinedTextField(search,{search=it},placeholder={Text(stringResource(R.string.search))},modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(24.dp),leadingIcon={Icon(Icons.Outlined.Search,null)}) }
        item(span={GridItemSpan(maxLineSpan)}) { Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Text(stringResource(if(wishes)R.string.wish_value else R.string.overview),fontWeight=FontWeight.Bold,fontSize=16.sp)
                Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surfaceVariant) { Text(if(wishes)"${source.size}" else "${scoped.size}/${source.size}",Modifier.padding(horizontal=8.dp,vertical=3.dp),fontSize=12.sp) }
            }
            Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) { Label(if(wishes)R.string.budget else R.string.estimated_value);Text(summary?.let { displayMoney(it.total) } ?: "—",fontSize=24.sp,fontWeight=FontWeight.Bold) }
                if(!wishes)Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) { Label(R.string.daily_total);Text(formatCost(daily,code),fontSize=24.sp,fontWeight=FontWeight.Bold) }
                else AssetArt(Asset("jar","preview","",Money(0,code),today.toString(),icon="jar:3d"),Modifier.size(64.dp))
            }
            if(summary?.missing?.isNotEmpty()==true)Text(stringResource(R.string.incomplete_total)+" · "+summary.missing.joinToString(),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(!wishes) {
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { listOf(Status.ACTIVE,Status.RETIRED,Status.SOLD).forEach { s->
                    val n=scoped.count { it.status==s }
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(statusLabel(s))+" $n",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)
                        LinearProgressIndicator(progress={if(scoped.isEmpty())0f else n.toFloat()/scoped.size},modifier=Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(8.dp)),color=Lime,trackColor=MaterialTheme.colorScheme.surfaceVariant,drawStopIndicator={})
                    }
                } }
            }
        } }
        if(!wishes) {
            item(span={GridItemSpan(maxLineSpan)}) { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(22.dp)) {
                (listOf<String?>(null)+source.map { it.category }.distinct()).forEach { c->Column(Modifier.clickable { category=c }.padding(vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally) {
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
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(a.name,Modifier.clickable{onOpen(a)},fontWeight=FontWeight.Bold)
                        Text(displayMoney(a.purchase),fontWeight=FontWeight.Bold)
                        OutlinedButton(onClick={onPurchase(a)}){Text(stringResource(R.string.purchase_wish),fontSize=12.sp)}
                    }
                }
            } else if(list)Surface(shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(0.7.dp,MaterialTheme.colorScheme.outlineVariant)) {
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
            Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(0.7.dp,MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.combinedClickable(onClick={onOpen(a)},onLongClick={menu=a}).padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().height(if(list)64.dp else 124.dp)) {
                        AssetArt(a,Modifier.size(if(list)60.dp else 90.dp).align(Alignment.BottomCenter))
                        if(!wishes)Box(Modifier.align(Alignment.TopEnd)){StatusBadge(a.status)}
                    }
                    Text(a.name,fontWeight=FontWeight.SemiBold,fontSize=15.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(displayMoney(a.purchase)+(if(!wishes)" · ${cost?.days ?: 0} "+stringResource(R.string.days) else ""),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(if(wishes)stringResource(R.string.view) else formatCost(cost?.net ?: cost?.current,a.purchase.currency)+stringResource(if(a.sale==null && a.useMode)R.string.per_use else R.string.per_day),Modifier.padding(top=10.dp),fontWeight=FontWeight.Bold,fontSize=17.sp)
                }
            }
        }
    }
    if(filters)ModalBottomSheet(onDismissRequest={filters=false},containerColor=MaterialTheme.colorScheme.surface) { Column(Modifier.padding(20.dp).navigationBarsPadding(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.tag_title),fontSize=22.sp,fontWeight=FontWeight.Bold)
        SelectPill(stringResource(R.string.all),tag==null){tag=null}
        source.flatMap { it.tags }.distinct().forEach { t->SelectPill(t,tag==t){tag=t} }
        Button(onClick={filters=false},modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.confirm))}
    } }
    menu?.let { a->AlertDialog(onDismissRequest={menu=null},title={Text(a.name)},confirmButton={TextButton(onClick={menu=null;onOpen(a)}){Text(stringResource(R.string.view))}},dismissButton={TextButton(onClick={menu=null}){Text(stringResource(R.string.cancel))}}) }
}
