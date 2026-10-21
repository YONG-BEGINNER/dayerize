package app.dayerize.core
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.math.BigDecimal
class CollectionQueryTest {
    private val today=LocalDate.of(2026,10,21)
    private fun asset(id:String,currency:String="MYR",minor:Long=10000)=Asset(id,"w","相机 $id",Money(minor,currency),"2026-10-12",tags=listOf("travel"))
    @Test fun combinesUnicodeSearchTagsAndCategoryWithStableTies() {
        val a=asset("b");val b=asset("a")
        assertEquals(listOf("a","b"),CollectionQuery.apply(listOf(a,b),"Electronics",Status.ACTIVE,setOf("travel"),"相机",CollectionSort.PURCHASE_DATE,true,today,"MYR",emptyList()).map{it.id})
        assertTrue(CollectionQuery.apply(listOf(a),null,null,setOf("other"),"",CollectionSort.NAME,true,today,"MYR",emptyList()).isEmpty())
    }
    @Test fun dailySortConvertsAndKeepsUnavailableLastInBothDirections() {
        val myr=asset("myr");val usd=asset("usd","USD",5000);val unknown=asset("missing","EUR");val perUse=asset("uses").copy(useMode=true)
        val rates=listOf(FxRate("USD","MYR",BigDecimal("4"),today))
        val source=listOf(unknown,usd,perUse,myr)
        fun sorted(up:Boolean)=CollectionQuery.apply(source,null,null,emptySet(),"",CollectionSort.DAILY_COST,up,today,"MYR",rates).map{it.id}
        assertEquals(listOf("myr","usd","missing","uses"),sorted(true))
        assertEquals(listOf("usd","myr","missing","uses"),sorted(false))
    }
    @Test fun retiredCostsFreezeAndSoldUsesNetCalendarCost() {
        val retired=asset("retired").copy(status=Status.RETIRED,retiredDate="2026-10-13")
        val sold=asset("sold").copy(useMode=true,status=Status.SOLD,sale=Sale(Money(9000,"MYR"),today.toString()))
        assertEquals(listOf("sold","retired"),CollectionQuery.apply(listOf(retired,sold),null,null,emptySet(),"",CollectionSort.DAILY_COST,true,today,"MYR",emptyList()).map{it.id})
    }
}
