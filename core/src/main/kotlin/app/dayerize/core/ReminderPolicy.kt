package app.dayerize.core
import java.time.LocalDate

data class DueReminder(val assetId:String,val kind:String,val date:LocalDate)
object ReminderPolicy {
    fun due(assets:List<Asset>,today:LocalDate):List<DueReminder> = assets.flatMap{a->
        val dates=when {
            a.wishlist->listOf("wishlist" to a.coolingOffUntil)
            a.sale!=null->emptyList()
            else->listOf("warranty" to a.warranty,"return" to a.returnBy,"maintenance" to a.maintenanceDate)
        }
        dates.mapNotNull{(kind,value)->value.takeIf{it.isNotBlank()}?.let{LocalDate.parse(it)}?.takeIf{it in today..today.plusDays(7)}?.let{DueReminder(a.id,kind,it)}}
    }
}
