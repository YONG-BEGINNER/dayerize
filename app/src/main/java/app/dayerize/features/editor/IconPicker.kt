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
import org.json.JSONArray
import coil.compose.AsyncImage
import app.dayerize.R
import app.dayerize.ui.*

@Composable fun IconPicker(onSelect:(String)->Unit,onDismiss:()->Unit) {
    val ctx=LocalContext.current
    val catalog=remember { val a=JSONArray(ctx.assets.open("icons.json").bufferedReader().use { it.readText() }); (0 until a.length()).map { a.getJSONObject(it).let { o->o.getString("id") to o.getString("name") } } }
    var query by rememberSaveable { mutableStateOf("") };var style by rememberSaveable { mutableStateOf("3d") }
    val visible=remember(query){catalog.filter { it.second.contains(query,true) }}
    Dialog(onDismissRequest=onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) { Column(Modifier.systemBarsPadding().padding(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick=onDismiss){Icon(Icons.Outlined.Close,stringResource(R.string.cancel))}
                Text(stringResource(R.string.icons),style=MaterialTheme.typography.titleLarge)
            }
            Row(Modifier.padding(vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                SelectPill(stringResource(R.string.style_3d),style=="3d"){style="3d"}
                SelectPill(stringResource(R.string.style_flat),style=="2d"){style="2d"}
                Text("${visible.size}",Modifier.align(Alignment.CenterVertically).padding(start=8.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(query,{query=it},placeholder={Text(stringResource(R.string.icon_search))},modifier=Modifier.fillMaxWidth().padding(bottom=16.dp),singleLine=true,shape=RoundedCornerShape(24.dp),leadingIcon={Icon(Icons.Outlined.Search,null)})
            if(visible.isEmpty())Text(stringResource(R.string.no_results))
            LazyVerticalGrid(columns=GridCells.Adaptive(56.dp),modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                items(visible,key={it.first}) { (id,name)->
                    Surface(onClick={onSelect("$id:$style")},shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surface) {
                        AsyncImage(model="file:///android_asset/icons/${id}_${style}.${if(style=="3d")"png" else "webp"}",contentDescription=name,modifier=Modifier.aspectRatio(1f).padding(8.dp))
                    }
                }
            }
        } }
    }
}
