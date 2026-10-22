package app.dayerize.reminders

import android.app.*
import android.content.*
import android.os.Build
import app.dayerize.MainActivity
import app.dayerize.R
import app.dayerize.core.*
import app.dayerize.data.AssetStore
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit

object ReminderScheduler {
    fun update(context:Context,enabled:Boolean) {
        val alarm=context.getSystemService(AlarmManager::class.java)
        val request=Intent(context,ReminderReceiver::class.java)
        val existing=PendingIntent.getBroadcast(context,41,request,PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if(!enabled){existing?.let{alarm.cancel(it);it.cancel()};return}
        if(existing!=null)return
        val intent=PendingIntent.getBroadcast(context,41,request,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+60_000,AlarmManager.INTERVAL_DAY,intent)
    }
}
class ReminderReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        val pending=goAsync()
        Thread {
            val store=AssetStore(context)
            try{runBlocking{
                val settings=store.settings();if(settings["reminders"]!="true")return@runBlocking
                if(intent.action==Intent.ACTION_BOOT_COMPLETED){ReminderScheduler.update(context,true);return@runBlocking}
                val manager=context.getSystemService(NotificationManager::class.java)
                if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)return@runBlocking
                manager.createNotificationChannel(NotificationChannel("ownership",context.getString(R.string.reminders),NotificationManager.IMPORTANCE_DEFAULT))
                val assets=store.dao.all().map{store.json.decodeFromString<Asset>(it.payload)}
                val count=ReminderPolicy.due(assets,store.today()).size
                val backupDue=settings["lastExport"]?.let{runCatching{Instant.parse(it).isBefore(Instant.now().minus(30,ChronoUnit.DAYS))}.getOrDefault(true)} ?: true
                if((count>0 || backupDue) && settings["lastReminderDay"]!=store.today().toString()) {
                    val open=PendingIntent.getActivity(context,42,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    manager.notify(41,Notification.Builder(context,"ownership").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(context.getString(R.string.reminder_due)).setContentText(if(count>0)"$count · ${context.getString(R.string.assets)}" else context.getString(R.string.backup_due)).setContentIntent(open).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build())
                    store.preference("lastReminderDay",store.today().toString())
                }
            }}catch(_:Exception){/* A subsequent daily attempt may recover; never change assets. */}finally{store.database.close();pending.finish()}
        }.start()
    }
}
