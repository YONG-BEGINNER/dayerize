package app.dayerize.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.dayerize.R
import app.dayerize.ui.*

@Composable fun BackupScreen(assetCount:Int,wishCount:Int,onExport:()->Unit,onRestore:()->Unit,onBack:()->Unit,lastExport:String?) {
    BackHandler(onBack=onBack)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        TextButton(onClick=onBack){Text(stringResource(R.string.back))}
        ScreenHeading(stringResource(R.string.backup))
        Panel(Modifier.fillMaxWidth()) {
            Label(R.string.local_workspace)
            SettingRow(stringResource(R.string.assets),assetCount.toString())
            SettingRow(stringResource(R.string.wishlist),wishCount.toString())
            Label(R.string.last_export)
            Text(lastExport ?: stringResource(R.string.never_exported))
        }
        Panel(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.backup_note))
            Button(onClick=onExport,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(stringResource(R.string.export))}
        }
        Panel(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.restore_warning))
            OutlinedButton(onClick=onRestore,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(stringResource(R.string.restore))}
        }
    }
}
