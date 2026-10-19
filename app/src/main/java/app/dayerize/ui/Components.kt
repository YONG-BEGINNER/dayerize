package app.dayerize.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import coil.compose.SubcomposeAsyncImage
import app.dayerize.R
import app.dayerize.core.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import java.io.File

val Lime=Color(0xFFBBFA26)
data class DisplayOptions(val decimals:Int?=null,val grouping:Boolean=true)
val LocalDisplayOptions=staticCompositionLocalOf { DisplayOptions() }
@Composable fun DayerizeTheme(dark:Boolean,content:@Composable ()->Unit) {
    val colors=if(dark) darkColorScheme(primary=Color.White,onPrimary=Color(0xFF111111),primaryContainer=Lime,onPrimaryContainer=Color.Black,secondary=Lime,onSecondary=Color.Black,background=Color(0xFF111111),onBackground=Color(0xFFF5F5F5),surface=Color(0xFF202020),onSurface=Color(0xFFF5F5F5),surfaceVariant=Color(0xFF2A2A2A),onSurfaceVariant=Color(0xFFA6A6A6),outlineVariant=Color(0xFF303030),outline=Color(0xFF666666))
    else lightColorScheme(primary=Color(0xFF151515),onPrimary=Color.White,primaryContainer=Lime,onPrimaryContainer=Color.Black,secondary=Color(0xFF527300),background=Color(0xFFF4F4F2),surface=Color.White,surfaceVariant=Color(0xFFECECE8),onSurface=Color(0xFF151515),onBackground=Color(0xFF151515),onSurfaceVariant=Color(0xFF64645F),outlineVariant=Color(0xFFE2E2DD))
    MaterialTheme(colorScheme=colors,typography=Typography(),content=content)
}
@Composable fun Panel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    Surface(modifier,shape=RoundedCornerShape(24.dp),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),color=MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
    }
}
@Composable fun Label(id:Int) { Text(stringResource(id),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant) }
@Composable fun Amount(money:Money,large:Boolean=false) { Text(displayMoney(money),fontWeight=FontWeight.Bold,fontSize=if(large)28.sp else 16.sp) }
fun formatMoney(m:Money):String=NumberFormat.getCurrencyInstance(Locale.getDefault()).apply { val c=Currency.getInstance(m.currency);currency=c;maximumFractionDigits=c.defaultFractionDigits;minimumFractionDigits=c.defaultFractionDigits }.format(m.decimal())
@Composable fun formatCost(value:BigDecimal?,currency:String):String {
    if(value==null)return "—"
    val o=LocalDisplayOptions.current
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).apply { val c=Currency.getInstance(currency);this.currency=c;maximumFractionDigits=o.decimals ?: maxOf(2,c.defaultFractionDigits);minimumFractionDigits=maximumFractionDigits;isGroupingUsed=o.grouping }.format(value)
}
@Composable fun displayMoney(m:Money):String {
    val o=LocalDisplayOptions.current
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).apply { val c=Currency.getInstance(m.currency);currency=c;minimumFractionDigits=o.decimals?:c.defaultFractionDigits;maximumFractionDigits=minimumFractionDigits;isGroupingUsed=o.grouping }.format(m.decimal())
}
fun statusLabel(status:Status)=when(status){Status.ACTIVE->R.string.active;Status.RETIRED->R.string.retired;Status.SOLD->R.string.sold;Status.SCRAPPED->R.string.scrapped}
@Composable fun AssetArt(asset:Asset,modifier:Modifier=Modifier) {
    val ctx=LocalContext.current
    val parts=asset.icon.split(':');val id=parts[0];val style=parts.getOrElse(1){"3d"}
    val image=asset.photo?.let { File(ctx.filesDir,"photos/$it") } ?: "file:///android_asset/icons/${id}_${style}.${if(style=="3d")"png" else "webp"}"
    SubcomposeAsyncImage(model=image,contentDescription=asset.name,modifier=modifier,contentScale=ContentScale.Fit,error={Icon(Icons.Outlined.Inventory2,asset.name,Modifier.padding(16.dp))})
}
@Composable fun StatusBadge(status:Status) {
    Row(Modifier.clip(RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal=8.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).clip(RoundedCornerShape(6.dp)).background(if(status==Status.ACTIVE)Lime else MaterialTheme.colorScheme.onSurfaceVariant))
        Text(stringResource(statusLabel(status)),fontSize=10.sp,maxLines=1)
    }
}
@Composable fun ScreenHeading(title:String,action:(@Composable ()->Unit)?=null) {
    Row(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(title,Modifier.weight(1f),fontSize=26.sp,fontWeight=FontWeight.Bold)
        action?.invoke()
    }
}
@Composable fun SettingRow(label:String,value:String="",icon:ImageVector?=null,onClick:(()->Unit)?=null,trailing:(@Composable ()->Unit)?=null) {
    Row(Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(onClick=onClick) else Modifier).heightIn(min=48.dp).padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        if(icon!=null)Icon(icon,null,Modifier.size(20.dp))
        Text(label,Modifier.weight(1f),fontSize=14.sp)
        if(value.isNotBlank())Text(value,Modifier.widthIn(max=150.dp),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
        if(trailing!=null)trailing() else if(onClick!=null)Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun SelectPill(text:String,selected:Boolean,onClick:()->Unit) {
    Surface(onClick=onClick,shape=RoundedCornerShape(50),color=if(selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,contentColor=if(selected)MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) {
        Box(Modifier.heightIn(min=40.dp).padding(horizontal=14.dp,vertical=10.dp),contentAlignment=Alignment.Center) { Text(text,fontSize=13.sp,fontWeight=if(selected)FontWeight.Bold else FontWeight.Normal) }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun Choice(value:String,choices:List<String>,label:Int,onSelect:(String)->Unit) {
    var expanded by remember { mutableStateOf(false) };var query by remember { mutableStateOf("") }
    SettingRow(stringResource(label),value,onClick={expanded=true})
    if(expanded)ModalBottomSheet(onDismissRequest={expanded=false},containerColor=MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding()) {
            Text(stringResource(label),fontSize=22.sp,fontWeight=FontWeight.Bold)
            OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth().padding(vertical=12.dp),singleLine=true,leadingIcon={Icon(Icons.Outlined.Search,null)})
            androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max=420.dp)) {
                items(choices.filter { it.contains(query,true) }.size) { i->val c=choices.filter { it.contains(query,true) }[i]
                    SettingRow(c,onClick={onSelect(c);expanded=false;query=""},trailing={if(c==value)Icon(Icons.Outlined.Check,null)})
                }
            }
        }
    }
}
val currencies:List<String> = Currency.getAvailableCurrencies().filter { it.defaultFractionDigits in 0..3 }.map { it.currencyCode }.sorted()
