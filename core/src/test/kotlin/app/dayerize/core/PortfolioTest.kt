package app.dayerize.core
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
class PortfolioTest {
    private val day=LocalDate.of(2026,10,19)
    private val assets=listOf(Asset("1","w","A",Money(10000,"MYR"),"2026-01-01"),Asset("2","w","B",Money(1000,"USD"),"2026-01-01"))
    @Test fun missingRateDoesNotPretendParity() {
        val s=summarize(assets,"MYR",emptyList(),day)
        assertEquals(Money(10000,"MYR"),s.total);assertEquals(setOf("USD"),s.missing)
        assertEquals(1000L,s.subtotals["USD"])
    }
    @Test fun explicitRatesConvertWithoutMutatingAssets() {
        val s=summarize(assets,"MYR",listOf(FxRate("USD","MYR",BigDecimal("4.5"),day)),day)
        assertEquals(Money(14500,"MYR"),s.total);assertTrue(s.missing.isEmpty())
        assertEquals("USD",assets[1].purchase.currency)
    }
    @Test fun futureRatesAreNotUsedAndSoldItemsExcluded() {
        val s=summarize(assets,"MYR",listOf(FxRate("USD","MYR",BigDecimal("4.5"),day.plusDays(1))),day)
        assertEquals(setOf("USD"),s.missing)
        val sold=AssetLifecycle.sell(assets[1],Sale(Money(500,"USD"),day.toString()),day)
        assertEquals(Money(10000,"MYR"),summarize(listOf(assets[0],sold),"MYR",emptyList(),day).total)
    }
}
