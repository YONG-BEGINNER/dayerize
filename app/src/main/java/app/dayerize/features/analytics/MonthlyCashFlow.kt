package app.dayerize.features.analytics

import app.dayerize.core.Asset
import java.time.LocalDate
import java.time.YearMonth

internal fun monthlyCashFlow(assets:List<Asset>,month:YearMonth):Map<String,Long> {
    val flow=sortedMapOf<String,Long>()
    assets.filterNot { it.wishlist }.forEach { asset ->
        if(YearMonth.from(LocalDate.parse(asset.purchaseDate))==month)
            flow[asset.purchase.currency]=Math.subtractExact(flow[asset.purchase.currency] ?: 0,asset.purchase.minor)
        asset.sale?.let { sale ->
            if(YearMonth.from(LocalDate.parse(sale.date))==month)
                flow[sale.proceeds.currency]=Math.addExact(flow[sale.proceeds.currency] ?: 0,sale.proceeds.minor)
        }
    }
    return flow
}

internal fun earliestCashFlowMonth(assets:List<Asset>,current:YearMonth):YearMonth =
    assets.filterNot { it.wishlist }.flatMap { listOfNotNull(it.purchaseDate,it.sale?.date) }
        .map { YearMonth.from(LocalDate.parse(it)) }.plus(current).minOrNull()!!
