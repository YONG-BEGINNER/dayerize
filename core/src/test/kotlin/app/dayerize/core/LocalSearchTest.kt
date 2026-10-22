package app.dayerize.core
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
class LocalSearchTest {
    val now=LocalDate.parse("2026-10-22")
    @Test fun knownLanguageFiltersAreVisibleAndApplied() {
        val q=LocalSearch.parse("show cameras bought last year under 500",now,"MYR",listOf("Cameras"))
        assertEquals("Cameras",q.category);assertEquals("2025-01-01",q.from.toString());assertEquals("2025-12-31",q.to.toString())
        val a=Asset("1","w","Camera",Money(49900,"MYR"),"2025-02-01",category="Cameras")
        assertTrue(q.matches(a,emptyList(),now));assertFalse(q.matches(a.copy(purchase=Money(50000,"MYR")),emptyList(),now))
        assertFalse(q.matches(a.copy(purchaseDate="2026-02-01"),emptyList(),now))
    }
    @Test fun unknownTextIsRetainedAndNoConversionIsInvented() {
        assertEquals("serialXYZ",LocalSearch.parse("serialXYZ",now,"MYR",emptyList()).text)
        val q=LocalSearch.parse("under 500",now,"MYR",emptyList())
        assertFalse(q.matches(Asset("1","w","Camera",Money(100,"USD"),"2025-01-01"),emptyList(),now))
    }
}
