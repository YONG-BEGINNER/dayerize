package app.dayerize.features.detail

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.dayerize.R
import app.dayerize.AppViewModel
import app.dayerize.core.*
import app.dayerize.ui.*
import app.dayerize.features.editor.Field
import java.util.UUID

@Composable fun OwnershipPanels(a:Asset,vm:AppViewModel) {
    var add by remember{mutableStateOf(false)}
    var removing by remember{mutableStateOf<Int?>(null)}
    var removingEvent by remember{mutableStateOf<String?>(null)}
    var eventLimit by remember(a.id){mutableIntStateOf(20)}
    var usageLimit by remember(a.id){mutableIntStateOf(20)}
    var saving by remember{mutableStateOf(false)}
    Panel(Modifier.fillMaxWidth()) {
        Label(R.string.ownership_total);Amount(Ownership.totalCost(a))
        Ownership.netCost(a)?.let{Label(R.string.net_ownership_cost);Amount(it)}
        Label(R.string.cost_per_use);Text(formatCost(Ownership.costPerUse(a),a.purchase.currency))
        if(a.location.isNotBlank())SettingRow(stringResource(R.string.location_label),a.location)
        if(a.collections.isNotEmpty())SettingRow(stringResource(R.string.collection_filter),a.collections.joinToString(" · "))
    }
    Panel(Modifier.fillMaxWidth()) {
        Label(R.string.ownership_timeline)
        Text("${a.purchaseDate} · ${stringResource(R.string.purchase_date)} · ${displayMoney(a.purchase)}")
        a.events.sortedBy{it.date}.takeLast(eventLimit).forEach{e->Row {
            Text("${e.date} · ${eventName(e.kind)} · ${e.note} · ${displayMoney(Money(e.costMinor,a.purchase.currency))}",Modifier.weight(1f))
            IconButton(onClick={removingEvent=e.id},enabled=!saving){Icon(Icons.Outlined.DeleteOutline,stringResource(R.string.remove_event))}
        }}
        if(a.events.size>eventLimit)TextButton(onClick={eventLimit+=20}){Text(stringResource(R.string.show_more))}
        a.retiredDate?.let{Text("$it · ${stringResource(R.string.retired)}")}
        a.sale?.let{Text("${it.date} · ${stringResource(R.string.sold)} · ${displayMoney(it.proceeds)}")}
        if(a.sale==null)OutlinedButton(onClick={add=true}){Text(stringResource(R.string.add_event))}
    }
    if(a.uses.isNotEmpty())Panel(Modifier.fillMaxWidth()) {
        Label(R.string.usage_history)
        a.uses.indices.reversed().take(usageLimit).forEach{i->val use=a.uses[i];Row {
            Text("${use.date} · ${use.count}",Modifier.weight(1f))
            IconButton(onClick={removing=i},enabled=!saving){Icon(Icons.Outlined.DeleteOutline,stringResource(R.string.remove_usage))}
        }}
    }
    if(a.uses.size>usageLimit)TextButton(onClick={usageLimit+=20}){Text(stringResource(R.string.show_more))}
    removingEvent?.let { id->AlertDialog(onDismissRequest={removingEvent=null},title={Text(stringResource(R.string.remove_event))},confirmButton={TextButton(enabled=!saving,onClick={
        saving=true;vm.mutate(a.id,{Ownership.removeEvent(it,id)},onError={saving=false}){saving=false;removingEvent=null}
    }){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={removingEvent=null}){Text(stringResource(R.string.cancel))}}) }
    removing?.let { index->AlertDialog(onDismissRequest={removing=null},title={Text(stringResource(R.string.remove_usage))},confirmButton={TextButton(enabled=!saving,onClick={
        saving=true;vm.mutate(a.id,{current->require(current.uses==a.uses);Ownership.removeUsage(current,index)},onError={saving=false}){saving=false;removing=null}
    }){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={removing=null}){Text(stringResource(R.string.cancel))}}) }
    if(add) {
        var date by remember{mutableStateOf(vm.today().toString())};var kind by remember{mutableStateOf("repair")}
        var note by remember{mutableStateOf("")};var amount by remember{mutableStateOf("0")};var error by remember{mutableStateOf(false)}
        val kinds=listOf("repair","maintenance","accessory","note").associateWith{eventName(it)}
        AlertDialog(onDismissRequest={if(!saving)add=false},title={Text(stringResource(R.string.add_event))},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            ValueChoice(kind,kinds.keys.toList(),R.string.category,{kinds.getValue(it)}){kind=it}
            Field(date,{date=it},R.string.event_date);Field(note,{note=it},R.string.notes);Field(amount,{amount=it},R.string.price,true)
            Text(stringResource(R.string.expense_note),style=MaterialTheme.typography.bodySmall)
            if(error)Text(stringResource(R.string.check_fields),color=MaterialTheme.colorScheme.error)
        }},confirmButton={TextButton(enabled=!saving,onClick={try{
            val updated=a.copy(events=a.events+OwnershipEvent(UUID.randomUUID().toString(),date,kind,note,Money.parse(amount,a.purchase.currency).minor))
            updated.validate(vm.today());saving=true;vm.mutate(a.id,{current->require(current.sale==null);current.copy(events=current.events+updated.events.last())},onError={saving=false;error=true}){saving=false;add=false}
        }catch(e:Exception){error=true}}){Text(stringResource(R.string.save_asset))}},dismissButton={TextButton(enabled=!saving,onClick={add=false}){Text(stringResource(R.string.cancel))}})
    }
}
@Composable private fun eventName(kind:String)=stringResource(when(kind){"repair"->R.string.event_repair;"maintenance"->R.string.event_maintenance;"accessory"->R.string.event_accessory;else->R.string.event_note})
