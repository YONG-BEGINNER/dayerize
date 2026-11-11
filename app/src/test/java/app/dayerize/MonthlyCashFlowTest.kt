package app.dayerize

import app.dayerize.core.*
import app.dayerize.features.analytics.*
import java.time.YearMonth
import org.junit.Assert.*
import org.junit.Test

class MonthlyCashFlowTest {
    private fun asset(id:String,date:String,currency:String="MYR",amount:Long=100)=
        Asset(id,"workspace",id,Money(amount,currency),date)

    @Test fun monthBoundariesSeparatePurchasesAndSales() {
        val assets=listOf(asset("camera","2025-12-31").copy(sale=Sale(Money(75,"MYR"),"2026-01-01")),asset("phone","2026-01-31"),asset("later","2026-02-01"))
        assertEquals(mapOf("MYR" to -100L),monthlyCashFlow(assets,YearMonth.of(2025,12)))
        assertEquals(mapOf("MYR" to -25L),monthlyCashFlow(assets,YearMonth.of(2026,1)))
    }
    @Test fun currenciesStaySeparateAndWishlistIsExcluded() {
        val assets=listOf(asset("camera","2026-01-01"),asset("phone","2026-01-02","USD",200),asset("wish","2026-01-01",amount=900).copy(wishlist=true))
        assertEquals(mapOf("MYR" to -100L,"USD" to -200L),monthlyCashFlow(assets,YearMonth.of(2026,1)))
    }
    @Test(expected=ArithmeticException::class) fun overflowDoesNotReturnPartialTotals() {
        monthlyCashFlow(listOf(asset("a","2026-01-01",amount=Long.MAX_VALUE),asset("b","2026-01-01",amount=100)),YearMonth.of(2026,1))
    }
    @Test fun emptyMonthHasNoInventedTotals() {
        assertTrue(monthlyCashFlow(listOf(asset("camera","2026-01-01")),YearMonth.of(2026,2)).isEmpty())
    }
    @Test fun earliestMonthIncludesSalesAndNeverStartsInFuture() {
        val current=YearMonth.of(2026,10)
        assertEquals(current,earliestCashFlowMonth(emptyList(),current))
        assertEquals(current,earliestCashFlowMonth(listOf(asset("future","2027-01-01")),current))
        assertEquals(YearMonth.of(2025,12),earliestCashFlowMonth(listOf(asset("camera","2025-12-31")),current))
    }
}
