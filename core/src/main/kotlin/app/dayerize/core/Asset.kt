package app.dayerize.core

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.temporal.ChronoUnit

@Serializable enum class Status { ACTIVE, RETIRED, SOLD, SCRAPPED }
@Serializable data class Usage(val date: String, val count: Int)
@Serializable data class Sale(val proceeds: Money, val date: String, val channel: String="", val rateToPurchase: String?=null)
@Serializable data class Asset(
    val id: String, val workspaceId: String, val name: String, val purchase: Money, val purchaseDate: String,
    val targetDays: Int=1000, val category: String="Electronics", val tags: List<String> = emptyList(),
    val icon: String="laptop:3d", val photo: String?=null, val notes: String="", val serial: String="",
    val channel: String="", val warranty: String="", val status: Status=Status.ACTIVE,
    val retiredDate: String?=null, val sale: Sale?=null, val useMode: Boolean=false,
    val uses: List<Usage> = emptyList(), val valuation: String="purchase", val manualValue: Long?=null,
    val residual: Long=0, val originalPhoto: String?=null, val wishlist: Boolean=false, val targetMode: String="duration",
    val events: List<OwnershipEvent> = emptyList(), val collections: List<String> = emptyList(),
    val location: String="", val returnBy: String="", val maintenanceDate: String="",
    val favourite: Boolean=false, val savingsMinor: Long=0, val coolingOffUntil: String=""
) {
    fun validate(today: LocalDate) {
        require(id.isNotBlank() && workspaceId.isNotBlank() && name.isNotBlank())
        require(!wishlist || (status==Status.ACTIVE && retiredDate==null && sale==null && uses.isEmpty()))
        require(purchase.minor>=0 && targetDays>0 && targetDays<=365000)
        require(category.isNotBlank() && name.length<=200 && notes.length<=10000)
        val bought=LocalDate.parse(purchaseDate)
        require(bought<=today)
        val retired=retiredDate?.let(LocalDate::parse)
        require(retired==null || (retired>=bought && retired<=today))
        require(status!=Status.RETIRED || retired!=null)
        require(status!=Status.ACTIVE || (retired==null && sale==null))
        require((status==Status.SOLD || status==Status.SCRAPPED)==(sale!=null))
        sale?.let {
            val sold=LocalDate.parse(it.date)
            require(sold>=bought && sold<=today && (retired==null || sold>=retired))
            require(it.proceeds.minor>=0 && (status!=Status.SCRAPPED || it.proceeds.minor==0L))
            if(it.proceeds.currency!=purchase.currency) {
                require(it.rateToPurchase!=null && BigDecimal(it.rateToPurchase)>BigDecimal.ZERO)
                try { convert(it.proceeds,FxRate(it.proceeds.currency,purchase.currency,BigDecimal(it.rateToPurchase),sold)) }
                catch(e:ArithmeticException){throw IllegalArgumentException("Sale conversion exceeds supported amount",e)}
            }
        }
        val end=sale?.date?.let(LocalDate::parse) ?: today
        require(uses.all { it.count>0 && LocalDate.parse(it.date) in bought..end })
        require(savingsMinor>=0 && location.length<=200 && collections.size<=100 && collections.all{it.isNotBlank() && it.length<=100})
        try{Ownership.totalCost(this)}catch(e:ArithmeticException){throw IllegalArgumentException("Ownership cost exceeds supported amount",e)}
        require(events.size<=10000 && events.map{it.id}.distinct().size==events.size)
        events.forEach { event->
            require(event.id.isNotBlank() && event.kind in listOf("repair","maintenance","accessory","note"))
            require(event.costMinor>=0 && event.note.length<=2000 && LocalDate.parse(event.date) in bought..end)
        }
        listOf(returnBy,maintenanceDate,coolingOffUntil).filter{it.isNotBlank()}.forEach{LocalDate.parse(it)}
        require(targetMode in listOf("none","daily","duration","custom"))
        require(valuation in listOf("purchase","manual","linear"))
        require(residual in 0..purchase.minor && (manualValue==null || manualValue>=0))
        require(valuation!="manual" || manualValue!=null)
        if(warranty.isNotBlank()) LocalDate.parse(warranty)
        require(photo==null || photo.matches(Regex("[a-zA-Z0-9_-]+\\.(jpg|png|webp)")))
        require(originalPhoto==null || originalPhoto.matches(Regex("[a-zA-Z0-9_-]+\\.(jpg|png|webp)")))
    }
}
data class CostSummary(val days: Long, val current: BigDecimal?, val target: BigDecimal, val net: BigDecimal?, val gain: Money?, val progress: Float)
object CostCalculator {
    fun calculate(asset: Asset, asOf: LocalDate): CostSummary {
        asset.validate(asOf)
        val end=LocalDate.parse(asset.retiredDate ?: asset.sale?.date ?: asOf.toString())
        val days=ChronoUnit.DAYS.between(LocalDate.parse(asset.purchaseDate),end)+1
        val denominator=if(asset.useMode) asset.uses.sumOf { it.count.toLong() } else days
        fun divide(value: BigDecimal)=if(denominator==0L) null else value.divide(BigDecimal(denominator),8,RoundingMode.HALF_UP)
        val proceeds=asset.sale?.let {
            if(it.proceeds.currency==asset.purchase.currency) it.proceeds
            else convert(it.proceeds,FxRate(it.proceeds.currency,asset.purchase.currency,BigDecimal(it.rateToPurchase),LocalDate.parse(it.date)))
        }
        return CostSummary(days,divide(asset.purchase.decimal()),
            asset.purchase.decimal().divide(BigDecimal(asset.targetDays),8,RoundingMode.HALF_UP),
            proceeds?.let { (asset.purchase.decimal()-it.decimal()).divide(BigDecimal(ChronoUnit.DAYS.between(LocalDate.parse(asset.purchaseDate),LocalDate.parse(asset.sale!!.date))+1),8,RoundingMode.HALF_UP) },
            proceeds?.let { Money(Math.subtractExact(it.minor,asset.purchase.minor),asset.purchase.currency) },
            (denominator.toFloat()/asset.targetDays).coerceIn(0f,1f))
    }
    fun estimate(asset: Asset, asOf: LocalDate): Money {
        asset.validate(asOf)
        if(asset.sale!=null || asset.wishlist) return Money(0,asset.purchase.currency)
        val minor=when(asset.valuation) {
            "manual" -> requireNotNull(asset.manualValue)
            "linear" -> {
                val days=ChronoUnit.DAYS.between(LocalDate.parse(asset.purchaseDate),LocalDate.parse(asset.retiredDate ?: asOf.toString())).coerceAtMost(asset.targetDays.toLong())
                val loss=BigDecimal(asset.purchase.minor-asset.residual).multiply(BigDecimal(days)).divide(BigDecimal(asset.targetDays),0,RoundingMode.HALF_UP).longValueExact()
                asset.purchase.minor-loss
            }
            else -> asset.purchase.minor
        }
        return Money(minor,asset.purchase.currency)
    }
}
object AssetLifecycle {
    fun retire(asset: Asset,date: LocalDate): Asset {
        require(asset.status==Status.ACTIVE)
        return asset.copy(status=Status.RETIRED,retiredDate=date.toString()).also { it.validate(date) }
    }
    fun sell(asset: Asset,sale: Sale,today: LocalDate): Asset {
        require(asset.status in listOf(Status.ACTIVE,Status.RETIRED))
        return asset.copy(status=Status.SOLD,sale=sale).also { it.validate(today) }
    }
}
