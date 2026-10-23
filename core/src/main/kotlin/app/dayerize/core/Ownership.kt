package app.dayerize.core

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode

@Serializable data class OwnershipEvent(val id:String,val date:String,val kind:String,val note:String="",val costMinor:Long=0)
object Ownership {
    fun totalCost(asset:Asset):Money=Money(asset.events.fold(asset.purchase.minor){sum,e->Math.addExact(sum,e.costMinor)},asset.purchase.currency)
    fun netCost(asset:Asset):Money? = asset.sale?.let{sale->
        val recovered=if(sale.proceeds.currency==asset.purchase.currency)sale.proceeds else convert(sale.proceeds,FxRate(sale.proceeds.currency,asset.purchase.currency,BigDecimal(sale.rateToPurchase),LocalDate.parse(sale.date)))
        Money(Math.subtractExact(totalCost(asset).minor,recovered.minor),asset.purchase.currency)
    }
    fun costPerUse(asset:Asset):BigDecimal? {
        val count=asset.uses.sumOf{it.count.toLong()}
        return if(count==0L)null else (netCost(asset) ?: totalCost(asset)).decimal().divide(BigDecimal(count),8,RoundingMode.HALF_UP)
    }
    fun removeEvent(asset:Asset,id:String):Asset=asset.copy(events=asset.events.filterNot{it.id==id})
    fun removeUsage(asset:Asset,index:Int):Asset {
        require(index in asset.uses.indices)
        return asset.copy(uses=asset.uses.filterIndexed{i,_->i!=index})
    }
    fun duplicate(asset:Asset,newId:String,date:LocalDate):Asset=asset.copy(
        id=newId,purchaseDate=date.toString(),status=Status.ACTIVE,retiredDate=null,sale=null,
        favourite=false,serial="",warranty="",uses=emptyList(),events=emptyList(),returnBy="",maintenanceDate="",savingsMinor=0,coolingOffUntil=""
    )
}
