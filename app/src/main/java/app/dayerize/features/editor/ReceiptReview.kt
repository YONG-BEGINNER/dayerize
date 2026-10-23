package app.dayerize.features.editor

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.ui.*
import java.time.LocalDate

data class ReceiptFields(val amount:String,val currency:String,val date:String,val retailer:String)

@Composable fun ReceiptReview(text:String,onApply:(ReceiptFields)->Unit,onDismiss:()->Unit,today:LocalDate=LocalDate.now()) {
    val candidates=remember(text){ReceiptParser.parse(text)}
    var amount by rememberSaveable(text){mutableStateOf(candidates.suggestedAmount ?: "")}
    var currency by rememberSaveable(text){mutableStateOf(candidates.currencies.singleOrNull() ?: "")}
    var date by rememberSaveable(text){mutableStateOf(candidates.dates.singleOrNull() ?: "")}
    var retailer by rememberSaveable(text){mutableStateOf(candidates.retailer ?: "")}
    var details by rememberSaveable{mutableStateOf(false)}
    val valid=runCatching {
        require(Money.parse(amount,currency).minor>=0)
        if(date.isNotBlank())require(!LocalDate.parse(date).isAfter(today))
        require(retailer.length<=120)
    }.isSuccess
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            Column(Modifier.systemBarsPadding().imePadding().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    ScreenHeading(stringResource(R.string.receipt_review))
                    Text(stringResource(R.string.receipt_unverified))
                    Panel {
                        OutlinedTextField(amount,{amount=it.take(32)},label={Text(stringResource(R.string.price))},modifier=Modifier.fillMaxWidth().testTag("receipt-amount"),singleLine=true)
                        Text(stringResource(R.string.receipt_total_warning),style=MaterialTheme.typography.bodySmall)
                        candidates.amounts.filter{it.total}.takeIf{it.size>1}.orEmpty().take(5).forEach { a->TextButton(onClick={amount=a.value}){Text(a.line)} }
                        Choice(currency, currencies,R.string.currency){currency=it}
                        if(candidates.currencies.size!=1)Text(stringResource(R.string.receipt_choose_currency))
                        OutlinedTextField(date,{date=it.take(10)},label={Text(stringResource(R.string.purchase_date))},placeholder={Text("YYYY-MM-DD")},modifier=Modifier.fillMaxWidth().testTag("receipt-date"),singleLine=true)
                        if(candidates.dates.size>1) {
                            Text(stringResource(R.string.receipt_choose_date))
                            candidates.dates.take(6).forEach { d->TextButton(onClick={date=d}){Text(d)} }
                        }
                        OutlinedTextField(retailer,{retailer=it.take(120)},label={Text(stringResource(R.string.receipt_retailer))},modifier=Modifier.fillMaxWidth(),singleLine=true)
                    }
                    TextButton(onClick={details=!details}){Text(stringResource(if(details)R.string.receipt_hide_details else R.string.receipt_show_details))}
                    if(details)Panel { Text(stringResource(R.string.receipt_raw));Text(text,style=MaterialTheme.typography.bodySmall) }
                }
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick=onDismiss,modifier=Modifier.weight(1f).testTag("receipt-cancel")){Text(stringResource(R.string.cancel))}
                    Button(onClick={onApply(ReceiptFields(amount,currency,date,retailer))},enabled=valid,modifier=Modifier.weight(1f).testTag("receipt-apply")){Text(stringResource(R.string.apply))}
                }
            }
        }
    }
}
