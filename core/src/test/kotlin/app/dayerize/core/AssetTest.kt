package app.dayerize.core

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AssetTest {
    private val today=LocalDate.of(2026,1,20)
    private fun asset()=Asset("a","local","Laptop",Money(100000,"MYR"),"2026-01-01",100)
    @Test fun retirementFreezesAndSaleSettlesCalendarDays() {
        val retired=AssetLifecycle.retire(asset(),LocalDate.of(2026,1,10))
        assertEquals("100.00000000",CostCalculator.calculate(retired,today).current.toString())
        val sold=AssetLifecycle.sell(retired,Sale(Money(40000,"MYR"),"2026-01-20"),today)
        val cost=CostCalculator.calculate(sold,today)
        assertEquals("30.00000000",cost.net.toString())
        assertEquals(Money(-60000,"MYR"),cost.gain)
    }
    @Test fun sameDayAndLeapDay() {
        assertEquals(1,CostCalculator.calculate(asset(),LocalDate.of(2026,1,1)).days.toInt())
        val leap=asset().copy(purchaseDate="2024-02-28")
        assertEquals(3,CostCalculator.calculate(leap,LocalDate.of(2024,3,1)).days.toInt())
    }
    @Test fun zeroUsesHasNoInventedDailyCost() {
        assertNull(CostCalculator.calculate(asset().copy(useMode=true),today).current)
    }
    @Test fun invalidDatesAndAmountsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { asset().copy(purchaseDate="2027-01-01").validate(today) }
        assertThrows(IllegalArgumentException::class.java) { asset().copy(targetDays=0).validate(today) }
        assertThrows(IllegalArgumentException::class.java) { asset().copy(purchase=Money(-1,"MYR")).validate(today) }
        assertThrows(IllegalArgumentException::class.java) { asset().copy(uses=listOf(Usage("2026-01-01",0))).validate(today) }
    }
    @Test fun foreignSaleRequiresSnapshotAndCanBeProfitable() {
        assertThrows(IllegalArgumentException::class.java) { AssetLifecycle.sell(asset(),Sale(Money(30000,"USD"),today.toString()),today) }
        val sold=AssetLifecycle.sell(asset(),Sale(Money(30000,"USD"),today.toString(),rateToPurchase="4.5"),today)
        assertEquals(Money(35000,"MYR"),CostCalculator.calculate(sold,today).gain)
    }
    @Test fun depreciationRespectsResidualAndDisposal() {
        val a=asset().copy(valuation="linear",residual=10000)
        assertEquals(Money(55000,"MYR"),CostCalculator.estimate(a,LocalDate.of(2026,2,20)))
        assertEquals(Money(10000,"MYR"),CostCalculator.estimate(a,LocalDate.of(2026,12,20)))
        assertEquals(Money(0,"MYR"),CostCalculator.estimate(a.copy(status=Status.SOLD,sale=Sale(Money(0,"MYR"),"2026-01-20")),today))
    }
}
