package app.dayerize.features.editor

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import app.dayerize.R
import app.dayerize.ui.*
import app.dayerize.artwork.*

@Composable fun IconPicker(onSelect:(String)->Unit,onDismiss:()->Unit,defaultStyle:String="3d") {
    val ctx=LocalContext.current
    val catalog=remember(ctx){ArtworkCatalog(ctx)}
    val locale=androidx.compose.ui.platform.LocalConfiguration.current.locales[0].toLanguageTag()
    var query by rememberSaveable{mutableStateOf("")}
    var styleKey by rememberSaveable{mutableStateOf(defaultStyle)}
    val style=ArtworkStyle.entries.firstOrNull{it.key==styleKey} ?: ArtworkStyle.REALISTIC_3D
    var category by rememberSaveable{mutableStateOf<String?>(null)}
    var selected by rememberSaveable{mutableStateOf<String?>(null)}
    val prefs=remember(ctx){ctx.getSharedPreferences("artwork_picker",android.content.Context.MODE_PRIVATE)}
    var favourites by remember{mutableStateOf(prefs.getStringSet("favourites",emptySet())!!.toSet())}
    var recent by remember{mutableStateOf(prefs.getString("recent","")!!.split('|').filter{it.isNotBlank()})}
    var shelf by rememberSaveable{mutableStateOf("all")}
    val matches=catalog.search(query,category,style,locale)
    val visible=when(shelf){
        "favourites"->matches.filter{it.id in favourites}
        "recent"->recent.mapNotNull{id->matches.find{it.id==id}}
        else->matches
    }
    val preview=catalog.entries.find{it.id==selected && style in it.files}
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            Column(Modifier.systemBarsPadding().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    IconButton(onClick=onDismiss){Icon(Icons.Outlined.Close,stringResource(R.string.cancel))}
                    Text(stringResource(R.string.icons),Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
                    Text("${visible.size}",color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    SelectPill(stringResource(R.string.style_3d),style==ArtworkStyle.REALISTIC_3D){styleKey="3d"}
                    SelectPill(stringResource(R.string.cute_2d),style==ArtworkStyle.CUTE_2D){styleKey="cute2d"}
                }
                OutlinedTextField(query,{query=it},placeholder={Text(stringResource(R.string.icon_search))},modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(12.dp),leadingIcon={Icon(Icons.Outlined.Search,null)})
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    (listOf<String?>(null)+catalog.entries.filter{it.id!="fallback"}.map{it.category}.distinct()).forEach { c->SelectPill(c?.let{artworkCategoryLabel(it)} ?: stringResource(R.string.all),c==category){category=c} }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    listOf("all" to R.string.all,"favourites" to R.string.icon_favourites,"recent" to R.string.icon_recent).forEach { (key,label)->
                        SelectPill(stringResource(label),shelf==key){shelf=key}
                    }
                }
                if(visible.isEmpty())Text(stringResource(R.string.no_results))
                LazyVerticalGrid(columns=GridCells.Adaptive(80.dp),modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    items(visible,key={it.id}) { entry ->
                        Surface(onClick={selected=entry.id},shape=AlloyPanelShape,border=BorderStroke(1.dp,if(selected==entry.id)MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),color=MaterialTheme.colorScheme.surface) {
                            Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(6.dp)) {
                                AsyncImage("file:///android_asset/${entry.files.getValue(style)}",entry.name(locale),modifier=Modifier.fillMaxWidth().aspectRatio(1f).graphicsLayer{scaleX=entry.opticalScale;scaleY=entry.opticalScale},contentScale=ContentScale.Fit)
                                Text(entry.name(locale),style=MaterialTheme.typography.labelSmall,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
                if(preview!=null)Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    AsyncImage("file:///android_asset/${preview.files.getValue(style)}",preview.name(locale),Modifier.size(64.dp).graphicsLayer{scaleX=preview.opticalScale;scaleY=preview.opticalScale},contentScale=ContentScale.Fit)
                    Text(preview.name(locale),Modifier.weight(1f))
                    IconButton(onClick={
                        favourites=if(preview.id in favourites)favourites-preview.id else favourites+preview.id
                        prefs.edit().putStringSet("favourites",favourites).apply()
                    }){Icon(if(preview.id in favourites)Icons.Outlined.Star else Icons.Outlined.StarBorder,stringResource(if(preview.id in favourites)R.string.icon_unfavourite else R.string.icon_favourite))}
                    Button(onClick={
                        recent=(listOf(preview.id)+recent.filter{it!=preview.id}).take(24)
                        prefs.edit().putString("recent",recent.joinToString("|")).apply()
                        onSelect(preview.icon(style))
                    }){Text(stringResource(R.string.apply))}
                }
            }
        }
    }
}
@Composable private fun artworkCategoryLabel(category:String):String=stringResource(when(category){"Tech"->R.string.art_tech;"Vehicles"->R.string.art_vehicles;"Wearables"->R.string.art_wearables;"Home"->R.string.art_home;else->R.string.art_hobbies})
