package app.dayerize.core

import java.time.LocalDate
import java.math.BigDecimal
import java.util.Locale

enum class CollectionSort { PURCHASE_DATE, NAME, DAILY_COST }
object CollectionQuery {
    fun dailyCost(asset:Asset,today:LocalDate,reportingCurrency:String,rates:List<FxRate>):BigDecimal? {
        if(asset.useMode && asset.sale==null)return null
        val cost=CostCalculator.calculate(asset,today).let{it.net ?: it.current} ?: return null
        val rate=rates.rateOn(asset.purchase.currency,reportingCurrency,today) ?: return null
        return cost*rate
    }
    fun apply(assets:List<Asset>,category:String?,status:Status?,tags:Set<String>,query:String,sort:CollectionSort,ascending:Boolean,today:LocalDate,reportingCurrency:String,rates:List<FxRate>):List<Asset> {
        val filtered=assets.filter { a -> (category==null || a.category==category) && (status==null || a.status==status) && a.tags.containsAll(tags) && listOf(a.name,a.notes,a.serial,a.tags.joinToString(" ")).any{it.contains(query.trim(),true)} }
        val costs=if(sort==CollectionSort.DAILY_COST)filtered.associate{it.id to dailyCost(it,today,reportingCurrency,rates)} else emptyMap()
        return filtered.sortedWith { a,b ->
            val order=when(sort) {
                CollectionSort.PURCHASE_DATE->a.purchaseDate.compareTo(b.purchaseDate)
                CollectionSort.NAME->a.name.lowercase(Locale.ROOT).compareTo(b.name.lowercase(Locale.ROOT))
                CollectionSort.DAILY_COST->{
                    val ac=costs[a.id];val bc=costs[b.id]
                    if(ac==null || bc==null)return@sortedWith when {ac!=null->-1;bc!=null->1;else->a.id.compareTo(b.id)}
                    ac.compareTo(bc)
                }
            }
            (if(ascending)order else -order).takeIf{it!=0} ?: a.id.compareTo(b.id)
        }
    }
}
