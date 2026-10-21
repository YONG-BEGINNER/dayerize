package app.dayerize.features.settings
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.dayerize.*
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.ui.*
import app.dayerize.features.editor.Field
import java.time.*
import java.math.BigDecimal

@Composable fun SettingsScreen(state:AppState,vm:AppViewModel,onExport:()->Unit,onRestore:()->Unit) {
    var rates by remember { mutableStateOf(false) };var categories by remember { mutableStateOf(false) };var licenses by remember { mutableStateOf(false) };var tagDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=16.dp,end=16.dp,bottom=150.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        ScreenHeading(stringResource(R.string.settings))
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.AccountCircle,null,Modifier.size(40.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
                Column{Text(stringResource(R.string.local_workspace),fontWeight=FontWeight.Bold);Text(stringResource(R.string.local_device),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            }
        }
        Label(R.string.numbers_units)
        Panel(Modifier.fillMaxWidth()) {
            Choice(state.config["currency"] ?: "MYR",currencies,R.string.reporting_currency){vm.setting("currency",it)}
            Choice(state.config["decimals"] ?: "2",listOf("0","1","2","3"),R.string.decimals){vm.setting("decimals",it)}
            SettingRow(stringResource(R.string.grouping),icon=Icons.Outlined.Numbers,trailing={Switch(state.config["grouping"]!="false",{vm.setting("grouping",it.toString())})})
            SettingRow(stringResource(R.string.rates),icon=Icons.Outlined.CurrencyExchange,onClick={rates=true})
        }
        Label(R.string.data_manage)
        Panel(Modifier.fillMaxWidth()) {
            SettingRow(stringResource(R.string.category_manage),icon=Icons.Outlined.Category,onClick={categories=true})
            SettingRow(stringResource(R.string.tag_manage),icon=Icons.Outlined.Sell,onClick={tagDialog=true})
            SettingRow(stringResource(R.string.backup),icon=Icons.Outlined.Backup,onClick=onExport)
        }
        Text(stringResource(R.string.backup_note),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Label(R.string.appearance)
        Panel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("system" to R.string.system,"light" to R.string.light,"dark" to R.string.dark).forEach { (k,label)->
                SelectPill(stringResource(label),(state.config["theme"] ?: "dark")==k){vm.setting("theme",k)}
            } }
            val styles=mapOf("3d" to stringResource(R.string.style_3d),"cute2d" to stringResource(R.string.cute_2d))
            Choice(styles[state.config["artworkStyle"] ?: "3d"]!!,styles.values.toList(),R.string.default_artwork){name->vm.setting("artworkStyle",styles.entries.first{it.value==name}.key)}
            SettingRow(stringResource(R.string.reduce_motion),trailing={Switch(state.config["reduceMotion"]!="false",{vm.setting("reduceMotion",it.toString())})})
        }
        Label(R.string.general)
        Panel(Modifier.fillMaxWidth()) {
            val languageNames=mapOf("system" to stringResource(R.string.system),"en" to "English","zh-CN" to "简体中文","zh-TW" to "繁體中文","ms" to "Bahasa Melayu")
            Choice(languageNames[state.config["language"] ?: "system"] ?: "English",languageNames.values.toList(),R.string.language){name->vm.setting("language",languageNames.entries.first{it.value==name}.key)}
            Choice(state.config["timezone"] ?: ZoneId.systemDefault().id,ZoneId.getAvailableZoneIds().sorted(),R.string.timezone){zone->
                if(runCatching { state.assets.forEach { it.validate(LocalDate.now(ZoneId.of(zone))) } }.isSuccess)vm.setting("timezone",zone) else vm.message.value=R.string.check_fields
            }
            SettingRow(stringResource(R.string.licenses),icon=Icons.Outlined.Info,onClick={licenses=true})
        }
        Text(stringResource(R.string.local_only),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if(rates) {
        var from by remember { mutableStateOf("USD") };var to by remember { mutableStateOf(state.config["currency"] ?: "MYR") };var value by remember { mutableStateOf("") };var date by remember { mutableStateOf(vm.today().toString()) };var error by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest={rates=false},title={Text(stringResource(R.string.rates))},text={Column(Modifier.verticalScroll(rememberScrollState())) {
            vm.rates().forEach { Text("${it.from} → ${it.to}: ${it.value} (${it.date})",style=MaterialTheme.typography.bodySmall) }
            Choice(from,currencies,R.string.from_currency){from=it};Choice(to,currencies,R.string.to_currency){to=it};Field(value,{value=it},R.string.rate,true);Field(date,{date=it},R.string.rate_date)
            if(error)Text(stringResource(R.string.check_fields))
        }},confirmButton={TextButton(onClick={try{require(from!=to);FxRate(from,to,BigDecimal(value),LocalDate.parse(date));require(LocalDate.parse(date)<=vm.today());vm.addRate(SavedRate(from,to,value,date));rates=false}catch(e:Exception){error=true}}){Text(stringResource(R.string.add_rate))}},dismissButton={TextButton(onClick={rates=false}){Text(stringResource(R.string.cancel))}})
    }
    if(categories) {
        var from by remember { mutableStateOf(state.assets.firstOrNull()?.category ?: "") };var to by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest={categories=false},title={Text(stringResource(R.string.rename))},text={Column {Choice(from,state.assets.map { it.category }.distinct(),R.string.category){from=it};Field(to,{to=it},R.string.new_name)}},confirmButton={TextButton(onClick={if(to.isNotBlank()){vm.launch{vm.store.renameCategory(from,to.trim())};categories=false}}){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={categories=false}){Text(stringResource(R.string.cancel))}})
    }
    if(tagDialog) {
        var from by remember { mutableStateOf(state.assets.flatMap{it.tags}.firstOrNull() ?: "") };var to by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest={tagDialog=false},title={Text(stringResource(R.string.rename_tag))},text={Column{Choice(from,state.assets.flatMap{it.tags}.distinct(),R.string.tag_title){from=it};Field(to,{to=it},R.string.new_name)}},confirmButton={TextButton(onClick={if(from.isNotBlank()&&to.isNotBlank()){vm.launch{vm.store.renameTag(from,to.trim())};tagDialog=false}}){Text(stringResource(R.string.confirm))}},dismissButton={TextButton(onClick={tagDialog=false}){Text(stringResource(R.string.cancel))}})
    }
    if(licenses)AlertDialog(onDismissRequest={licenses=false},title={Text("Dayerize artwork & licenses")},text={Text("Original Dayerize artwork: generated for this project.\nLegacy artwork retained for saved items:\n1,284 3D + 1,284 Flat icons\nMIT License · © Microsoft Corporation\nU²-Net · Apache-2.0\nFull licenses are included in the application assets.")},confirmButton={TextButton(onClick={licenses=false}){Text(stringResource(R.string.confirm))}})
}
