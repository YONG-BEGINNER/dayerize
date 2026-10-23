package app.dayerize.features.detail

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.dayerize.*
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.ui.*
import app.dayerize.features.editor.Field
import java.time.LocalDate

@Composable fun DetailScreen(a:Asset,vm:AppViewModel,today:LocalDate,onBack:()->Unit,onEdit:()->Unit,onPurchase:()->Unit={},onDuplicate:()->Unit={}) {
    var action by remember { mutableStateOf("") }
    if(action=="sell"){SaleScreen(a,vm){action=""};return}
    val cost=remember(a,today){CostCalculator.calculate(a,today)}
    var logging by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,stringResource(R.string.back))}
            Row {
                IconToggleButton(checked=a.favourite,onCheckedChange={vm.mutate(a.id,{current->current.copy(favourite=it)})}){Icon(if(a.favourite)Icons.Outlined.Star else Icons.Outlined.StarBorder,stringResource(R.string.widget_favourite))}
                IconButton(onClick=onEdit){Icon(Icons.Outlined.Edit,stringResource(R.string.edit))}
            }
        }
        AssetArt(a,Modifier.fillMaxWidth().height((LocalLayoutDensity.current.artworkHeight+36).dp))
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(a.name,Modifier.weight(1f),fontSize=22.sp,fontWeight=FontWeight.Bold,letterSpacing=(-0.4).sp)
            if(!a.wishlist)StatusBadge(a.status)
        }
        if(a.wishlist) {
            Panel(Modifier.fillMaxWidth()){Label(R.string.budget);Amount(a.purchase,true)}
            Panel(Modifier.fillMaxWidth()){
                Label(R.string.savings_amount);Amount(Money(a.savingsMinor,a.purchase.currency))
                LinearProgressIndicator(progress={if(a.purchase.minor==0L)0f else (a.savingsMinor.toFloat()/a.purchase.minor).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
                if(a.coolingOffUntil.isNotBlank())Text(stringResource(R.string.cooling_off)+": "+a.coolingOffUntil)
            }
            if(a.notes.isNotBlank())Panel(Modifier.fillMaxWidth()){Label(R.string.notes);Text(a.notes)}
            Button(onClick=onPurchase,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Text(stringResource(R.string.purchase_wish))}
        } else {
            Panel(Modifier.fillMaxWidth()) {
                Label(R.string.purchase_only_cost)
                Text(formatCost(if(a.sale!=null)cost.net else cost.current,a.purchase.currency)+stringResource(if(a.sale==null && a.useMode)R.string.per_use else R.string.per_day),fontSize=26.sp,fontWeight=FontWeight.Bold,letterSpacing=(-0.8).sp)
                if(a.sale!=null){Label(R.string.daily_cost);Text(formatCost(cost.current,a.purchase.currency)+stringResource(if(a.useMode)R.string.per_use else R.string.per_day))}
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    Column{Text("${cost.days}",fontSize=20.sp,fontWeight=FontWeight.Bold);Label(R.string.held_days)}
                    if(a.targetMode!="none")Column{Text("${(a.targetDays-cost.days).coerceAtLeast(0)}",fontSize=20.sp,fontWeight=FontWeight.Bold);Label(R.string.remaining)}
                }
                if(a.targetMode!="none") {
                    LinearProgressIndicator(progress={cost.progress},modifier=Modifier.fillMaxWidth(),color=Lime,trackColor=MaterialTheme.colorScheme.surfaceVariant)
                    Text("${(cost.progress*100).toInt()}% · "+stringResource(R.string.target)+" "+formatCost(cost.target,a.purchase.currency),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if(a.useMode && cost.current==null)Text(stringResource(R.string.no_uses))
                if(a.retiredDate!=null)Text(stringResource(R.string.retire_note),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Panel(Modifier.fillMaxWidth()) {
                SettingRow(stringResource(R.string.price),displayMoney(a.purchase),Icons.Outlined.Paid)
                SettingRow(stringResource(R.string.purchase_date),a.purchaseDate,Icons.Outlined.CalendarMonth)
                SettingRow(stringResource(R.string.category),a.category,Icons.Outlined.Category)
                if(a.tags.isNotEmpty())SettingRow(stringResource(R.string.tag_title),a.tags.joinToString(" · "),Icons.Outlined.Sell)
                if(a.notes.isNotBlank()){Label(R.string.notes);Text(a.notes)}
                if(a.serial.isNotBlank())SettingRow(stringResource(R.string.serial),a.serial)
                cost.gain?.let { Label(R.string.purchase_only_gain);Amount(it) }
            }
            OwnershipPanels(a,vm)
            if(a.status==Status.ACTIVE)Button(onClick={logging=true;vm.mutate(a.id,{current->require(current.status==Status.ACTIVE);current.copy(uses=current.uses+Usage(today.toString(),1))},onError={logging=false}){logging=false}},enabled=!logging,modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.log_use))}
        }
        OutlinedButton(onClick=onDuplicate,modifier=Modifier.fillMaxWidth()){Text(stringResource(R.string.duplicate_item))}
        Button(onClick=onEdit,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)){Text(stringResource(R.string.edit))}
        if(!a.wishlist)Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            if(a.status==Status.ACTIVE)OutlinedButton(onClick={action="retire"},modifier=Modifier.weight(1f).heightIn(min=48.dp)){Text(stringResource(R.string.retire))}
            if(a.status==Status.RETIRED)OutlinedButton(onClick={action="reactivate"},modifier=Modifier.weight(1f)){Text(stringResource(R.string.reactivate))}
            if(a.sale==null)OutlinedButton(onClick={action="sell"},modifier=Modifier.weight(1f).heightIn(min=48.dp)){Text(stringResource(R.string.resell))}
        }
        TextButton(onClick={action="delete"},modifier=Modifier.align(Alignment.CenterHorizontally)){Text(stringResource(R.string.delete),color=MaterialTheme.colorScheme.error)}
    }
    if(action in listOf("retire","delete","reactivate"))AlertDialog(onDismissRequest={action=""},title={Text(stringResource(when(action){"delete"->R.string.delete_note;"reactivate"->R.string.reactivate_note;else->R.string.retire_note}))},confirmButton={TextButton(onClick={
        when(action){"delete"->vm.delete(a.id,onBack);"retire"->vm.mutate(a.id,{AssetLifecycle.retire(it,today)});"reactivate"->vm.mutate(a.id,{current->require(current.status==Status.RETIRED);current.copy(status=Status.ACTIVE,retiredDate=null)})};action=""
    }){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={action=""}){Text(stringResource(R.string.cancel))}})
}
