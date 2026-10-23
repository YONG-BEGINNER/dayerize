package app.dayerize.features.detail

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.dayerize.*
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.ui.*
import app.dayerize.features.editor.Field

@Composable fun SaleScreen(asset:Asset,vm:AppViewModel,onBack:()->Unit) {
    var price by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf(asset.purchase.currency) }
    var date by rememberSaveable { mutableStateOf(vm.today().toString()) }
    var channel by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    BackHandler(onBack=onBack)
    fun candidate()=AssetLifecycle.sell(asset,Sale(Money.parse(price,currency),date,channel,if(currency!=asset.purchase.currency)rate else null),vm.today())
    val preview=runCatching{CostCalculator.calculate(candidate(),vm.today())}.getOrNull()
    Column(Modifier.fillMaxSize().imePadding().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            ScreenHeading(stringResource(R.string.resell))
            AssetArt(asset,Modifier.fillMaxWidth().height(120.dp))
            Text(asset.name,style=MaterialTheme.typography.titleMedium)
            Panel(Modifier.fillMaxWidth()) {
                Label(R.string.price);Amount(asset.purchase)
                OutlinedTextField(price,{price=it},label={Text(stringResource(R.string.sale_price))},modifier=Modifier.fillMaxWidth().testTag("sale-price"),keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Decimal))
                Choice(currency,currencies,R.string.currency){currency=it}
                Field(date,{date=it},R.string.sale_date)
                Field(channel,{channel=it},R.string.sale_channel)
                if(currency!=asset.purchase.currency)Field(rate,{rate=it},R.string.sale_rate,true)
            }
            preview?.let { Panel(Modifier.fillMaxWidth()) {
                Label(R.string.purchase_only_gain);it.gain?.let{gain->Amount(gain)}
                Label(R.string.purchase_only_cost)
                Text(formatCost(it.net,asset.purchase.currency)+" "+stringResource(R.string.per_day),Modifier.testTag("sale-net"),style=MaterialTheme.typography.titleLarge)
                Ownership.netCost(candidate())?.let{net->Label(R.string.net_ownership_cost);Amount(net)}
            } }
        }
        if(error)Text(stringResource(R.string.check_fields),color=MaterialTheme.colorScheme.error)
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick=onBack,enabled=!busy,modifier=Modifier.weight(1f)){Text(stringResource(R.string.cancel))}
            Button(onClick={try { val result=candidate();busy=true;vm.mutate(asset.id,{current->AssetLifecycle.sell(current,requireNotNull(result.sale),vm.today())},onError={busy=false;error=true}){busy=false;onBack()} }catch(e:Exception){error=true}},enabled=!busy,modifier=Modifier.weight(1f)){Text(stringResource(R.string.confirm))}
        }
    }
}
