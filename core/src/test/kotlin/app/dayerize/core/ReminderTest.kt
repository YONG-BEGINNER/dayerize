package app.dayerize.core
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
class ReminderTest {
    @Test fun onlyRelevantUpcomingDatesAreIncluded() {
        val today=LocalDate.parse("2026-10-22")
        val item=Asset("1","w","Camera",Money(100,"MYR"),"2026-10-01",warranty="2026-10-25",returnBy="2026-10-01",maintenanceDate="2027-01-01")
        assertEquals(listOf("warranty"),ReminderPolicy.due(listOf(item),today).map{it.kind})
        assertTrue(ReminderPolicy.due(listOf(item.copy(status=Status.SOLD,sale=Sale(Money(50,"MYR"),today.toString()))),today).isEmpty())
        assertEquals(listOf("wishlist"),ReminderPolicy.due(listOf(item.copy(wishlist=true,coolingOffUntil=today.toString())),today).map{it.kind})
    }
}
