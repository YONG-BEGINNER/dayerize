package app.dayerize.features.analytics
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.ui.platform.LocalConfiguration
import java.time.format.DateTimeFormatter
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
    val currentMonth=YearMonth.from(today)
    val earliestMonth=earliestCashFlowMonth(owned,currentMonth)
    var selectedMonthText by rememberSaveable { mutableStateOf(currentMonth.toString()) }
    val selectedMonth=YearMonth.parse(selectedMonthText).coerceIn(earliestMonth,currentMonth)
    val flow=try { monthlyCashFlow(owned,selectedMonth) } catch (_:ArithmeticException) { null }
    val locale=LocalConfiguration.current.locales[0]
    val groups=owned.filter { it.sale==null }.groupBy { it.category }.mapValues { (_,assets)->runCatching{summarize(assets,state.config["currency"]?:"MYR",vm.fxRates(),today)}.getOrNull() }
    val shades=listOf(Lime,Color(0xFFB7C0C5),Color(0xFF788B98),Color(0xFF596066))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        ScreenHeading(stringResource(R.string.analytics))
        Text(stringResource(R.string.analytics_current_scope),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Panel(Modifier.fillMaxWidth()) {
            Label(R.string.estimated_value);Text(summary?.let { displayMoney(it.total) } ?: "—",fontSize=24.sp,fontWeight=FontWeight.Bold)
            Text((state.config["currency"]?:"MYR")+" · "+stringResource(R.string.rates),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(summary?.missing?.isNotEmpty()==true)Text(stringResource(R.string.incomplete_total)+": "+summary.missing.joinToString(),fontSize=12.sp)
        }
        Panel(Modifier.fillMaxWidth()) {
            Label(R.string.category_distribution)
            if(owned.isEmpty()) Text(stringResource(R.string.empty),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            else Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Canvas(Modifier.size(88.dp).padding(8.dp)) {
                    val total=groups.values.sumOf { it?.total?.minor?.toDouble() ?: 0.0 }
                    if(total==0.0)drawArc(Color.Gray,0f,360f,false,style=Stroke(14.dp.toPx()))
                    else {var start=-90f;groups.values.forEachIndexed { i,s->val sweep=((s?.total?.minor ?: 0)/total*360).toFloat();drawArc(shades[i%shades.size],start,sweep,false,style=Stroke(14.dp.toPx()));start+=sweep}}
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
        Panel(Modifier.fillMaxWidth()) { Label(R.string.status_distribution);StatusCounts(owned) }
        Panel(Modifier.fillMaxWidth()) {
            Label(R.string.usage_insights)
            SettingRow(stringResource(R.string.no_usage_recorded),owned.count{it.uses.isEmpty() && it.status==Status.ACTIVE}.toString())
            SettingRow(stringResource(R.string.goals_reached),owned.count{it.targetMode!="none" && CostCalculator.calculate(it,today).progress>=1f}.toString())
            Label(R.string.most_used)
            owned.filter{it.uses.isNotEmpty()}.sortedByDescending{it.uses.sumOf{u->u.count.toLong()}}.take(3).forEach{SettingRow(it.name,it.uses.sumOf{u->u.count.toLong()}.toString())}
        }
        Panel(Modifier.fillMaxWidth()) {
            Label(R.string.cash_flow_selected_month)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick={selectedMonthText=selectedMonth.minusMonths(1).toString()},enabled=selectedMonth>earliestMonth) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft,stringResource(R.string.cash_flow_previous_month))
                }
                Text(selectedMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy",locale)),Modifier.weight(1f),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                IconButton(onClick={selectedMonthText=selectedMonth.plusMonths(1).toString()},enabled=selectedMonth<currentMonth) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight,stringResource(R.string.cash_flow_next_month))
                }
            }
            TextButton(onClick={selectedMonthText=currentMonth.toString()},enabled=selectedMonth!=currentMonth) {Text(stringResource(R.string.cash_flow_this_month))}
            if(flow==null)Text(stringResource(R.string.cash_flow_unavailable))
            else {
                if(flow.isEmpty())Text(stringResource(R.string.cash_flow_empty))
                flow.forEach { (code,minor)->Amount(Money(minor,code)) }
            }
        }
        Panel(Modifier.fillMaxWidth()) { Label(R.string.currency_subtotals);summary?.subtotals?.forEach { (code,minor)->Amount(Money(minor,code)) };if(summary?.subtotals.isNullOrEmpty())Text("—") }
        Text(stringResource(R.string.rate_note),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.cash_flow_history_scope),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
