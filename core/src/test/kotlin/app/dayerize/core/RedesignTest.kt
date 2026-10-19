package app.dayerize.core
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
class RedesignTest {
    val today=LocalDate.parse("2026-01-20")
    val a=Asset("a","local","Camera",Money(100000,"MYR"),"2026-01-01")
    @Test fun unrepresentableSaleRateCannotBeSaved() {
        assertThrows(IllegalArgumentException::class.java) {
            AssetLifecycle.sell(a,Sale(Money(100,"USD"),today.toString(),rateToPurchase="1e100"),today)
        }
    }
    @Test fun laterSaleKeepsFrozenGrossCost() {
        val sold=AssetLifecycle.sell(AssetLifecycle.retire(a,LocalDate.parse("2026-01-10")),Sale(Money(40000,"MYR"),today.toString()),today)
        assertEquals("100.00000000",CostCalculator.calculate(sold,today).current.toString())
    }
    @Test fun useModeSaleStillUsesCalendarDaysForSettlement() {
        val sold=AssetLifecycle.sell(a.copy(useMode=true,uses=listOf(Usage("2026-01-02",2))),Sale(Money(40000,"MYR"),today.toString()),today)
        assertEquals("30.00000000",CostCalculator.calculate(sold,today).net.toString())
    }
    @Test fun wishesNeverIncreaseOwnedAssetValue() {
        val wish=Json.decodeFromString<Asset>("""{"id":"w","workspaceId":"local","name":"Wish","purchase":{"minor":99900,"currency":"MYR"},"purchaseDate":"2026-01-01","wishlist":true}""")
        assertEquals(Money(100000,"MYR"),summarize(listOf(a,wish),"MYR",emptyList(),today).total)
    }
}
