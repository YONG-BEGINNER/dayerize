package app.dayerize.features.analytics
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import app.dayerize.*
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.ui.*
import java.time.*

@Composable fun AnalyticsScreen(state:AppState,vm:AppViewModel,today:LocalDate) {
    val owned=state.assets.filterNot { it.wishlist }
    val summary=runCatching { summarize(owned,state.config["currency"] ?: "MYR",vm.fxRates(),today) }.getOrNull()
    val flow=mutableMapOf<String,Long>()
    owned.forEach { a->
        if(YearMonth.from(LocalDate.parse(a.purchaseDate))==YearMonth.from(today)) flow[a.purchase.currency]=Math.subtractExact(flow[a.purchase.currency] ?: 0,a.purchase.minor)
        a.sale?.let { if(YearMonth.from(LocalDate.parse(it.date))==YearMonth.from(today))flow[it.proceeds.currency]=Math.addExact(flow[it.proceeds.currency] ?: 0,it.proceeds.minor) }
    }
    val groups=owned.filter { it.sale==null }.groupBy { it.category }.mapValues { (_,assets)->runCatching{summarize(assets,state.config["currency"]?:"MYR",vm.fxRates(),today)}.getOrNull() }
    val shades=listOf(Lime,Color(0xFF969B88),Color(0xFF687052),Color(0xFF45493D))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,bottom=110.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        ScreenHeading(stringResource(R.string.analytics))
        Panel(Modifier.fillMaxWidth()) {
            Label(R.string.estimated_value);Text(summary?.let { displayMoney(it.total) } ?: "—",fontSize=34.sp,fontWeight=FontWeight.Bold)
            Text((state.config["currency"]?:"MYR")+" · "+stringResource(R.string.rates),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(summary?.missing?.isNotEmpty()==true)Text(stringResource(R.string.incomplete_total)+": "+summary.missing.joinToString(),fontSize=12.sp)
        }
        Panel(Modifier.fillMaxWidth()) {
            Label(R.string.category_distribution)
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(20.dp)) {
                Canvas(Modifier.size(115.dp).padding(8.dp)) {
                    val total=groups.values.sumOf { it?.total?.minor?.toDouble() ?: 0.0 }
                    if(total==0.0)drawArc(Color.Gray,0f,360f,false,style=Stroke(22.dp.toPx()))
                    else {var start=-90f;groups.values.forEachIndexed { i,s->val sweep=((s?.total?.minor ?: 0)/total*360).toFloat();drawArc(shades[i%shades.size],start,sweep,false,style=Stroke(22.dp.toPx()));start+=sweep}}
                }
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    if(groups.isEmpty())Text("—")
                    groups.entries.forEachIndexed { i,(category,s)->Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        Surface(Modifier.size(8.dp),shape=androidx.compose.foundation.shape.CircleShape,color=shades[i%shades.size]){}
                        Column {Text(category,fontSize=12.sp);Text(s?.let{displayMoney(it.total)} ?: "—",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                    } }
                }
            }
        }
        Panel(Modifier.fillMaxWidth()) { Label(R.string.status_distribution);Status.entries.forEach { status->
            SettingRow(stringResource(statusLabel(status)),owned.count { it.status==status }.toString())
        } }
        Panel(Modifier.fillMaxWidth()) { Label(R.string.monthly_flow);if(flow.isEmpty())Text("—");flow.forEach { (code,minor)->Amount(Money(minor,code)) } }
        Panel(Modifier.fillMaxWidth()) { Label(R.string.currency_subtotals);summary?.subtotals?.forEach { (code,minor)->Amount(Money(minor,code)) };if(summary?.subtotals.isNullOrEmpty())Text("—") }
        Text(stringResource(R.string.rate_note),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.no_history),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
