package app.dayerize.core

import java.math.BigDecimal
import java.time.LocalDate
import java.math.RoundingMode
import java.util.Currency
import kotlinx.serialization.Serializable

@Serializable
data class Money(val minor: Long, val currency: String) {
    init { require(Currency.getInstance(currency).defaultFractionDigits in 0..3) { "Unsupported currency" } }
    fun decimal(): BigDecimal = BigDecimal.valueOf(minor, Currency.getInstance(currency).defaultFractionDigits)
    companion object {
        fun parse(value: String, currency: String): Money {
            try {
                require(value.matches(Regex("-?[0-9]+(\\.[0-9]+)?"))) { "Use a decimal amount" }
                val digits=Currency.getInstance(currency).defaultFractionDigits
                require(digits in 0..3)
                return Money(BigDecimal(value).movePointRight(digits).longValueExact(),currency)
            } catch (e: ArithmeticException) { throw IllegalArgumentException("Amount exceeds currency precision or range",e) }
        }
    }
}
data class FxRate(val from: String, val to: String, val rate: BigDecimal, val date: LocalDate) {
    init { Money(0,from); Money(0,to); require(rate > BigDecimal.ZERO) }
}
fun convert(money: Money, rate: FxRate): Money {
    require(money.currency==rate.from) { "Wrong exchange-rate currency pair" }
    val digits=Currency.getInstance(rate.to).defaultFractionDigits
    return Money(money.decimal().multiply(rate.rate).movePointRight(digits).setScale(0,RoundingMode.HALF_UP).longValueExact(),rate.to)
}
/** Latest saved rate on or before [date]; 1 for the same currency; null when no rate applies. */
fun List<FxRate>.rateOn(from: String, to: String, date: LocalDate): BigDecimal? =
    if(from==to) BigDecimal.ONE else filter { it.from==from && it.to==to && it.date<=date }.maxByOrNull { it.date }?.rate
