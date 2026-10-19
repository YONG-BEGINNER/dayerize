package app.dayerize.core

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class MoneyTest {
    @Test fun currencyScales() {
        assertEquals("123", Money(123,"JPY").decimal().toPlainString())
        assertEquals("1.23", Money(123,"MYR").decimal().toPlainString())
        assertEquals("1.234", Money(1234,"KWD").decimal().toPlainString())
    }
    @Test fun parsesExactly() {
        assertEquals(Money(1234,"KWD"), Money.parse("1.234","KWD"))
        assertThrows(IllegalArgumentException::class.java) { Money.parse("1.234","MYR") }
        assertThrows(IllegalArgumentException::class.java) { Money.parse("NaN","USD") }
        assertThrows(IllegalArgumentException::class.java) { Money.parse("1e99","USD") }
        assertThrows(IllegalArgumentException::class.java) { Money(1,"XXX") }
    }
    @Test fun convertsWithoutFloatingPoint() {
        val rate=FxRate("USD","MYR", BigDecimal("4.5"),LocalDate.of(2026,10,19))
        assertEquals(Money(45000,"MYR"),convert(Money(10000,"USD"),rate))
        assertEquals(Money(2,"JPY"),convert(Money(150,"USD"),rate.copy(to="JPY",rate=BigDecimal.ONE)))
    }
    @Test fun rejectsInvalidRates() {
        assertThrows(IllegalArgumentException::class.java) { FxRate("USD","MYR",BigDecimal.ZERO,LocalDate.now()) }
        assertThrows(IllegalArgumentException::class.java) {
            convert(Money(100,"EUR"),FxRate("USD","MYR",BigDecimal.ONE,LocalDate.now()))
        }
    }
}
