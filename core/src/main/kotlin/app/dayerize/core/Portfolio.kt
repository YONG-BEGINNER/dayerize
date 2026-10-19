package app.dayerize.core
import java.time.LocalDate

data class PortfolioSummary(val total: Money,val missing: Set<String>,val subtotals: Map<String,Long>)
fun summarize(assets: List<Asset>, reportingCurrency: String, rates: List<FxRate>, date: LocalDate): PortfolioSummary {
    var total=0L
    val missing=mutableSetOf<String>()
    val subtotals=mutableMapOf<String,Long>()
    assets.filter { it.sale==null && !it.wishlist }.forEach { asset ->
        val value=CostCalculator.estimate(asset,date)
        subtotals[value.currency]=Math.addExact(subtotals[value.currency] ?: 0,value.minor)
        if(value.currency==reportingCurrency) total=Math.addExact(total,value.minor)
        else {
            val rate=rates.filter { it.from==value.currency && it.to==reportingCurrency && it.date<=date }.maxByOrNull { it.date }
            if(rate==null) missing+=value.currency else total=Math.addExact(total,convert(value,rate).minor)
        }
    }
    return PortfolioSummary(Money(total,reportingCurrency),missing,subtotals)
}
