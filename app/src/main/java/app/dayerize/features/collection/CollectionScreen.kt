package app.dayerize.features.collection

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
@Composable fun CollectionScreen(state:AppState,vm:AppViewModel,today:LocalDate,onAdd:()->Unit,onOpen:(Asset)->Unit,wishes:Boolean=false,onPurchase:(Asset)->Unit=onOpen,onDuplicate:((Asset)->Unit)?=null) {
    var comparing by remember{mutableStateOf(false)}
    var search by rememberSaveable(wishes) { mutableStateOf("") };var showSearch by rememberSaveable(wishes) { mutableStateOf(false) };var list by rememberSaveable(wishes) { mutableStateOf(state.config[if(wishes)"wishlistList" else "collectionList"]=="true") }
    var status by rememberSaveable(wishes) { mutableStateOf<Status?>(null) };var category by rememberSaveable(wishes) { mutableStateOf<String?>(null) }
    var location by rememberSaveable(wishes){mutableStateOf<String?>(null)}
    var collection by rememberSaveable(wishes){mutableStateOf<String?>(null)}
    var tag by rememberSaveable(wishes){mutableStateOf<String?>(null)}
    val preferencePrefix=if(wishes)"wishlist" else "collection"
    var sort by rememberSaveable(wishes){mutableStateOf(runCatching{CollectionSort.valueOf(state.config[preferencePrefix+"Sort"] ?: "PURCHASE_DATE")}.getOrDefault(CollectionSort.PURCHASE_DATE))}
    var ascending by rememberSaveable(wishes){mutableStateOf(state.config[preferencePrefix+"Ascending"]=="true")}
    val clearFilters:()->Unit={search="";status=null;category=null;location=null;collection=null;tag=null}
    val hasFilters=search.isNotBlank() || status!=null || category!=null || location!=null || collection!=null || tag!=null
    val motionEnabled=LocalMotionEnabled.current
    val largeText=LocalDensity.current.fontScale>1.3f
    var filters by remember { mutableStateOf(false) };var menu by remember { mutableStateOf<Asset?>(null) }
    var confirm by remember { mutableStateOf<Pair<String,Asset>?>(null) }
    // Each stage is remembered on its real inputs so typing, tab fades and unrelated settings do not redo the work.
    val fx=vm.fxRates()
    val allSource=remember(state.assets,wishes){state.assets.filter { it.wishlist==wishes }}
    val source=remember(allSource,location,collection){allSource.filter{(location==null || it.location==location) && (collection==null || collection in it.collections)}}
    val scoped=remember(source,category){source.filter { category==null || it.category==category }}
    val code=state.config["currency"] ?: "MYR"
    val categoryChoices=remember(source){listOf<String?>(null)+source.map{it.category}.distinct()}
    val interpreted=remember(search,today,code,allSource){LocalSearch.parse(search,today,code,allSource.map{it.category}.distinct())}
    val items=remember(source,interpreted,category,status,tag,sort,ascending,today,code,fx,wishes){CollectionQuery.apply(source.filter{interpreted.matches(it,fx,today)},category,if(wishes)null else status,setOfNotNull(tag),interpreted.text,sort,ascending,today,code,fx)}
    val sortUnavailable=remember(items,sort,today,code,fx){sort==CollectionSort.DAILY_COST && items.any{CollectionQuery.dailyCost(it,today,code,fx)==null}}
    val due=remember(allSource,today){dueSoon(allSource,today)}
    val summary=remember(scoped,code,fx,today,wishes) { runCatching { summarize(scoped.map { if(wishes)it.copy(wishlist=false)else it },code,fx,today) }.getOrNull() }
    val daily=remember(scoped,code,fx,today) { runCatching {
        var total=BigDecimal.ZERO
        for(a in scoped.filter { it.status==Status.ACTIVE && !it.useMode }) {
            val cost=CostCalculator.calculate(a,today).current ?: continue
            val rate=fx.rateOn(a.purchase.currency,code,today) ?: return@runCatching null
            total+=cost*rate
        };total
    }.getOrNull() }
    LazyVerticalGrid(columns=GridCells.Fixed(if(list || wishes || largeText)1 else 2),contentPadding=PaddingValues(start=16.dp,end=16.dp,top=4.dp,bottom=24.dp),horizontalArrangement=Arrangement.spacedBy(LocalLayoutDensity.current.gap.dp),verticalArrangement=Arrangement.spacedBy(LocalLayoutDensity.current.gap.dp)) {
        item(span={GridItemSpan(maxLineSpan)}) { Column {
            ScreenHeading(if(wishes)stringResource(R.string.wishlist) else "Dayerize") { IconButton(onClick={showSearch=!showSearch}){Icon(Icons.Outlined.Search,stringResource(R.string.search))}; FilledIconButton(onClick=onAdd,shape=AlloyControlShape){Icon(Icons.Outlined.Add,stringResource(R.string.add_asset))} }
            AnimatedVisibility(showSearch,enter=if(motionEnabled)fadeIn(tween(MotionDurationMillis))+expandVertically(tween(MotionDurationMillis)) else EnterTransition.None,exit=if(motionEnabled)fadeOut(tween(MotionDurationMillis))+shrinkVertically(tween(MotionDurationMillis)) else ExitTransition.None) {
                OutlinedTextField(search,{search=it},placeholder={Text(stringResource(R.string.search))},modifier=Modifier.fillMaxWidth().padding(top=8.dp),singleLine=true,shape=RoundedCornerShape(24.dp),leadingIcon={Icon(Icons.Outlined.Search,null)})
            }
        } }
        if(wishes && source.size>=2)item(span={GridItemSpan(maxLineSpan)}){OutlinedButton(onClick={comparing=true}){Text(stringResource(R.string.compare_wishes))}}
        if(showSearch && (interpreted.category!=null || interpreted.from!=null || interpreted.below!=null || interpreted.status!=null))item(span={GridItemSpan(maxLineSpan)}) {
            Text(listOfNotNull(interpreted.category,interpreted.from?.let{"$it – ${interpreted.to}"},interpreted.below?.let{"< $code $it"},interpreted.status?.let{stringResource(statusLabel(it))}).joinToString(" · "),style=MaterialTheme.typography.bodySmall)
        }
        item(span={GridItemSpan(maxLineSpan)}) { Panel(Modifier.fillMaxWidth()) {
            if(largeText) {
                Label(if(wishes)R.string.budget else R.string.estimated_value)
                Text(summary?.let{displayMoney(it.total)} ?: "—",fontSize=24.sp,fontWeight=FontWeight.Bold)
                if(!wishes){Label(R.string.daily_total);Text(formatCost(daily,code),fontSize=24.sp,fontWeight=FontWeight.Bold)}
            } else Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) { Label(if(wishes)R.string.budget else R.string.estimated_value);Text(summary?.let { displayMoney(it.total) } ?: "—",fontSize=20.sp,fontWeight=FontWeight.Bold) }
                if(!wishes)Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) { Label(R.string.daily_total);Text(formatCost(daily,code),fontSize=20.sp,fontWeight=FontWeight.Bold) }
            }
            if(summary?.missing?.isNotEmpty()==true)Text(stringResource(R.string.incomplete_total)+" · "+summary.missing.joinToString(),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(!wishes) {
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
                StatusCounts(scoped)
            }
        } }
        if(due.isNotEmpty())item(span={GridItemSpan(maxLineSpan)}) { DueSoonPanel(due,allSource,onOpen) }
        if(!wishes) {
            item(span={GridItemSpan(maxLineSpan)}) {
                Column {
                    val allCategoriesLabel=stringResource(R.string.all_categories)
                    if(largeText) ValueChoice(category,categoryChoices,R.string.category,{it ?: allCategoriesLabel}){category=it}
                    Row(verticalAlignment=Alignment.CenterVertically) {
                    if(largeText) Spacer(Modifier.weight(1f)) else Box(Modifier.weight(1f)) {
                        ValueChoice(category,categoryChoices,R.string.category,{it ?: allCategoriesLabel}){category=it}
                    }
                    IconButton(onClick={filters=true}){Icon(Icons.Outlined.FilterAlt,stringResource(R.string.filter))}
                    IconButton(onClick={list=!list;vm.setting(preferencePrefix+"List",list.toString())}){Icon(if(list)Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,stringResource(R.string.layout))}
                    }
                }
            }
            item(span={GridItemSpan(maxLineSpan)}) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                    (listOf<Status?>(null)+Status.entries).forEach { s->SelectPill(stringResource(s?.let(::statusLabel) ?: R.string.all),status==s){status=s} }
                }
            }
        }
        if(hasFilters)item(span={GridItemSpan(maxLineSpan)}) {
            Column {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    @Composable fun ActiveFilter(label:String,onRemove:()->Unit) {
                        InputChip(selected=true,onClick=onRemove,label={Text(label)},trailingIcon={Icon(Icons.Outlined.Close,stringResource(R.string.remove_active_filter,label))},shape=AlloyControlShape)
                    }
                    if(search.isNotBlank())ActiveFilter(stringResource(R.string.filter_search_label)+": "+search){search=""}
                    category?.let{ActiveFilter(stringResource(R.string.category)+": "+it){category=null}}
                    if(!wishes)status?.let{ActiveFilter(stringResource(statusLabel(it))){status=null}}
                    location?.let{ActiveFilter(stringResource(R.string.location_label)+": "+it){location=null}}
                    collection?.let{ActiveFilter(stringResource(R.string.collection_filter)+": "+it){collection=null}}
                    tag?.let{ActiveFilter(stringResource(R.string.tag_title)+": "+it){tag=null}}
                }
                TextButton(onClick=clearFilters){Text(stringResource(R.string.clear_search_filters))}
            }
        }
        if(sortUnavailable)item(span={GridItemSpan(maxLineSpan)}){Text(stringResource(R.string.sort_unavailable),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        if(items.isEmpty()) item(span={GridItemSpan(maxLineSpan)}) { Column(Modifier.fillMaxWidth().padding(vertical=24.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            AssetArt(Asset("box","preview","",Money(0,code),today.toString(),icon="package:3d"),Modifier.size(60.dp))
            Text(stringResource(if(allSource.isEmpty())if(wishes)R.string.empty_wish else R.string.empty else R.string.no_results),Modifier.padding(top=8.dp),fontWeight=FontWeight.Bold)
            if(allSource.isNotEmpty() && hasFilters) TextButton(onClick=clearFilters){Text(stringResource(R.string.reset_search_filters))}
            else TextButton(onClick=onAdd){Text(stringResource(R.string.empty_hint),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        } }
        items(items,key={it.id}) { a ->
            val itemMotion=Modifier.animateItem(fadeInSpec=if(motionEnabled)tween(MotionDurationMillis) else null,placementSpec=if(motionEnabled)tween(MotionDurationMillis) else null,fadeOutSpec=if(motionEnabled)tween(MotionDurationMillis) else null)
            val cost=remember(a,today){runCatching { CostCalculator.calculate(a,today) }.getOrNull()}
            // Show the running cost in the reporting currency when a saved rate allows it; otherwise keep the item's own currency.
            val rate=remember(a.purchase.currency,code,fx,today){fx.rateOn(a.purchase.currency,code,today)}
            val unitCost=(cost?.net ?: cost?.current).let{value->if(rate!=null)value?.multiply(rate) else value}
            val unitCurrency=if(rate!=null)code else a.purchase.currency
            if(wishes)Panel(itemMotion.fillMaxWidth()) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    AssetArt(a,Modifier.size(96.dp).clickable{onOpen(a)})
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Text(a.name,Modifier.clickable{onOpen(a)},fontWeight=FontWeight.Bold)
                        Text(displayMoney(a.purchase),fontWeight=FontWeight.Bold)
                        OutlinedButton(onClick={onPurchase(a)}){Text(stringResource(R.string.purchase_wish),fontSize=12.sp)}
                    }
                }
            } else if(list || largeText)Surface(modifier=itemMotion,shape=AlloyPanelShape,color=MaterialTheme.colorScheme.surface,border=BorderStroke(0.7.dp,MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.fillMaxWidth().combinedClickable(onClick={onOpen(a)},onLongClick={menu=a}).padding(LocalLayoutDensity.current.panelPadding.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                    AssetArt(a,Modifier.size(64.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Text(a.name,fontWeight=FontWeight.SemiBold,fontSize=15.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                        Text(displayMoney(a.purchase)+" · ${cost?.days ?: 0} "+stringResource(R.string.days),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatCost(unitCost,unitCurrency)+stringResource(if(a.sale==null && a.useMode)R.string.per_use else R.string.per_day),fontSize=16.sp,fontWeight=FontWeight.Bold)
                    }
                    Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else
            Surface(modifier=itemMotion,shape=AlloyPanelShape,color=MaterialTheme.colorScheme.surface,border=BorderStroke(0.7.dp,MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.combinedClickable(onClick={onOpen(a)},onLongClick={menu=a}).padding(LocalLayoutDensity.current.panelPadding.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth().height(LocalLayoutDensity.current.artworkHeight.dp)) {
                        AssetArt(a,Modifier.fillMaxSize().padding(top=22.dp).align(Alignment.BottomCenter))
                        if(!wishes)Box(Modifier.align(Alignment.TopEnd)){StatusBadge(a.status)}
                    }
                    Text(a.name,fontWeight=FontWeight.SemiBold,fontSize=15.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(displayMoney(a.purchase)+(if(!wishes)" · ${cost?.days ?: 0} "+stringResource(R.string.days) else ""),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if(wishes)stringResource(R.string.view) else formatCost(unitCost,unitCurrency)+stringResource(if(a.sale==null && a.useMode)R.string.per_use else R.string.per_day),Modifier.padding(top=10.dp),fontWeight=FontWeight.Bold,fontSize=17.sp)
                }
            }
        }
    }
    if(filters) {
        val allCategoriesLabel=stringResource(R.string.all_categories)
        val allLocationsLabel=stringResource(R.string.all_locations)
        val allCollectionsLabel=stringResource(R.string.all_collections)
        var draftLocation by remember{mutableStateOf(location)}
        var draftCollection by remember{mutableStateOf(collection)}
        var draftTag by remember{mutableStateOf(tag)}
        var draftCategory by remember{mutableStateOf(category)}
        var draftStatus by remember{mutableStateOf(status)}
        var draftSort by remember{mutableStateOf(sort)}
        var draftAscending by remember{mutableStateOf(ascending)}
        ModalBottomSheet(onDismissRequest={filters=false},containerColor=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp).navigationBarsPadding().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.filter),style=MaterialTheme.typography.titleLarge)
                ValueChoice(draftCategory,categoryChoices,R.string.category,{it ?: allCategoriesLabel}){draftCategory=it}
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    (listOf<Status?>(null)+Status.entries).forEach { value->SelectPill(stringResource(value?.let(::statusLabel) ?: R.string.all),draftStatus==value){draftStatus=value} }
                }
                ValueChoice(draftLocation,listOf<String?>(null)+allSource.map{it.location}.filter{it.isNotBlank()}.distinct(),R.string.location_label,{it ?: allLocationsLabel}){draftLocation=it}
                ValueChoice(draftCollection,listOf<String?>(null)+allSource.flatMap{it.collections}.distinct(),R.string.collection_filter,{it ?: allCollectionsLabel}){draftCollection=it}
                Label(R.string.tag_title)
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    SelectPill(stringResource(R.string.all),draftTag==null){draftTag=null}
                    source.flatMap{it.tags}.distinct().forEach{value->SelectPill(value,draftTag==value){draftTag=value}}
                }
                Label(R.string.sort_by)
                CollectionSort.entries.forEach { value->SelectPill(stringResource(when(value){CollectionSort.PURCHASE_DATE->R.string.purchase_date;CollectionSort.NAME->R.string.name;CollectionSort.DAILY_COST->R.string.daily_cost}),draftSort==value){draftSort=value} }
                SettingRow(stringResource(R.string.ascending),trailing={Switch(draftAscending,{draftAscending=it})})
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                    OutlinedButton(onClick={draftLocation=null;draftCollection=null;draftTag=null;draftCategory=null;draftStatus=null;draftSort=CollectionSort.PURCHASE_DATE;draftAscending=false},modifier=Modifier.weight(1f)){Text(stringResource(R.string.reset))}
                    Button(onClick={location=draftLocation;collection=draftCollection;tag=draftTag;category=draftCategory;status=draftStatus;sort=draftSort;ascending=draftAscending;vm.setting(preferencePrefix+"Sort",sort.name);vm.setting(preferencePrefix+"Ascending",ascending.toString());filters=false},modifier=Modifier.weight(1f)){Text(stringResource(R.string.apply))}
                }
            }
        }
    }
    if(comparing && allSource.size>=2) {
        var first by remember{mutableStateOf(allSource[0].id)};var second by remember{mutableStateOf(allSource[1].id)}
        val names=allSource.associate{it.id to it.name}
        AlertDialog(onDismissRequest={comparing=false},title={Text(stringResource(R.string.compare_wishes))},text={Column(Modifier.verticalScroll(rememberScrollState())) {
            ValueChoice(first,names.keys.toList(),R.string.first_item,{names[it] ?: "—"}){first=it}
            ValueChoice(second,names.keys.toList(),R.string.second_item,{names[it] ?: "—"}){second=it}
            listOf(first,second).distinct().mapNotNull{id->allSource.find{it.id==id}}.forEach{a->
                Panel(Modifier.fillMaxWidth()){Text(a.name);Amount(a.purchase);Text(a.notes);Label(R.string.savings_amount);Amount(Money(a.savingsMinor,a.purchase.currency))}
            }
        }},confirmButton={TextButton(onClick={comparing=false}){Text(stringResource(R.string.back))}})
    }
    menu?.let { a->
        ModalBottomSheet(onDismissRequest={menu=null},containerColor=MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(horizontal=16.dp).padding(bottom=16.dp).navigationBarsPadding().verticalScroll(rememberScrollState())) {
                Text(a.name,style=MaterialTheme.typography.titleLarge,maxLines=1,overflow=TextOverflow.Ellipsis)
                SettingRow(stringResource(R.string.view),icon=Icons.Outlined.Visibility,onClick={menu=null;onOpen(a)})
                if(a.status==Status.ACTIVE)SettingRow(stringResource(R.string.log_use),icon=Icons.Outlined.AddTask,onClick={menu=null;vm.mutate(a.id,{current->require(current.status==Status.ACTIVE);current.copy(uses=current.uses+Usage(today.toString(),1))},done={vm.message.value=R.string.saved})})
                SettingRow(stringResource(if(a.favourite)R.string.widget_unfavourite else R.string.widget_favourite),icon=if(a.favourite)Icons.Outlined.Star else Icons.Outlined.StarBorder,onClick={menu=null;vm.mutate(a.id,{current->current.copy(favourite=!a.favourite)})})
                if(onDuplicate!=null)SettingRow(stringResource(R.string.duplicate_item),icon=Icons.Outlined.ContentCopy,onClick={menu=null;onDuplicate(a)})
                if(a.status==Status.ACTIVE)SettingRow(stringResource(R.string.retire),icon=Icons.Outlined.Archive,onClick={menu=null;confirm="retire" to a})
                SettingRow(stringResource(R.string.delete),icon=Icons.Outlined.Delete,onClick={menu=null;confirm="delete" to a})
            }
        }
    }
    confirm?.let { (action,a)->AlertDialog(onDismissRequest={confirm=null},title={Text(a.name)},text={Text(stringResource(if(action=="delete")R.string.delete_note else R.string.retire_note))},confirmButton={TextButton(onClick={
        if(action=="delete")vm.delete(a.id){} else vm.mutate(a.id,{AssetLifecycle.retire(it,today)})
        confirm=null
    }){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={confirm=null}){Text(stringResource(R.string.cancel))}}) }
}
