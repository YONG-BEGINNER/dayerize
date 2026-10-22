package app.dayerize.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.widget.RemoteViews
import app.dayerize.MainActivity
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.data.AssetStore
import app.dayerize.ui.formatMoney
import kotlinx.coroutines.runBlocking
import java.time.*
import java.math.BigDecimal

class OwnershipWidget:AppWidgetProvider() {
    override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray) {
        val pending=goAsync()
        Thread{val store=AssetStore(context)
            try{runBlocking{update(context,store.dao.all().map{store.json.decodeFromString<Asset>(it.payload)},store.settings())}}
            finally{store.database.close();pending.finish()}
        }.start()
    }
    companion object {
        fun update(context:Context,assets:List<Asset>,config:Map<String,String>) {
            val manager=AppWidgetManager.getInstance(context);val ids=manager.getAppWidgetIds(ComponentName(context,OwnershipWidget::class.java));if(ids.isEmpty())return
            val today=LocalDate.now(ZoneId.of(config["timezone"] ?: ZoneId.systemDefault().id))
            val rates=runCatching{config["rates"]?.let{kotlinx.serialization.json.Json.decodeFromString<List<SavedRate>>(it)} ?: emptyList()}.getOrDefault(emptyList()).map{FxRate(it.from,it.to,BigDecimal(it.value),LocalDate.parse(it.date))}
            val total=runCatching{summarize(assets,config["currency"] ?: "MYR",rates,today)}.getOrNull()
            val view=RemoteViews(context.packageName,R.layout.ownership_widget)
            view.setTextViewText(R.id.widget_summary,if(total==null || total.missing.isNotEmpty())context.getString(R.string.incomplete_total) else formatMoney(total.total))
            view.setTextViewText(R.id.widget_favourites,assets.filter{it.favourite}.take(3).joinToString("\n"){it.name})
            fun open(add:Boolean)=PendingIntent.getActivity(context,if(add)72 else 71,Intent(context,MainActivity::class.java).putExtra("quick_add",add).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            view.setOnClickPendingIntent(R.id.widget_add,open(true));view.setOnClickPendingIntent(R.id.widget_title,open(false));view.setOnClickPendingIntent(R.id.widget_summary,open(false))
            manager.updateAppWidget(ids,view)
        }
    }
}
