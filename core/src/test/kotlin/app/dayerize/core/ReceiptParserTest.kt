package app.dayerize.core

import org.junit.Assert.*
import org.junit.Test

class ReceiptParserTest {
    @Test fun suggestsGrandTotalRatherThanSubtotalTenderOrChange() {
        val r=ReceiptParser.parse("ACME CAMERA SHOP\n2026-10-23\nSubtotal RM 100.00\nTax RM 6.00\nGRAND TOTAL RM 106.00\nCash RM 120.00\nChange RM 14.00")
        assertEquals("106.00",r.suggestedAmount)
        assertEquals(setOf("MYR"),r.currencies)
        assertEquals(listOf("2026-10-23"),r.dates)
        assertEquals("ACME CAMERA SHOP",r.retailer)
    }
    @Test fun doesNotGuessAmbiguousCurrencyDateOrMultipleTotals() {
        val r=ReceiptParser.parse("Receipt\n03/04/2026\nTOTAL $ 10.00\nTOTAL $ 20.00")
        assertNull(r.suggestedAmount);assertTrue(r.currencies.isEmpty())
        assertEquals(setOf("2026-03-04","2026-04-03"),r.dates.toSet())
        assertNull(r.retailer)
    }
    @Test fun handlesDecimalConventionsWithoutTreatingDateAsPrice() {
        val r=ReceiptParser.parse("SHOP\n23/10/2026\nTOTAL EUR 1.234,56")
        assertEquals("1234.56",r.suggestedAmount);assertEquals(setOf("EUR"),r.currencies)
        assertEquals(listOf("2026-10-23"),r.dates)
        assertEquals(listOf("1234.56"),r.amounts.map{it.value})
        assertEquals("1234.56",ReceiptParser.parse("TOTAL USD 1,234.56").suggestedAmount)
        assertNull(ReceiptParser.parse("TOTAL 1,234").suggestedAmount)
    }
    @Test fun preservesCurrencyUncertaintyAndRejectsInvalidDates() {
        assertEquals(setOf("CNY","JPY"),ReceiptParser.parse("合计 ¥ 123.00").currencies)
        assertNull(ReceiptParser.parse("TOTAL USD 10.00\nTOTAL SGD 10.00").suggestedAmount)
        assertTrue(ReceiptParser.parse("2026-02-30\n99999999999999999999999999").dates.isEmpty())
        assertEquals("1000",ReceiptParser.parse("合計 JPY 1000").suggestedAmount)
    }
    @Test fun neverTurnsNegativeOrRefundNotationIntoPositivePurchase() {
        for(value in listOf("-106.00","(106.00)","106.00-","−106.00","- MYR 106.00","(MYR 106.00)")) {
            val r=ReceiptParser.parse("TOTAL MYR $value")
            assertNull("Negative $value",r.suggestedAmount)
            assertTrue("Do not offer positive candidate for $value",r.amounts.isEmpty())
        }
        assertNull(ReceiptParser.parse("REFUND TOTAL MYR 106.00").suggestedAmount)
    }
    @Test fun currencyWordsRequireMonetaryContext() {
        val r=ReceiptParser.parse("SHOP\nTOTAL $ 10.00\nALL SALES FINAL")
        assertTrue(r.currencies.isEmpty())
        assertEquals(setOf("ALL"),ReceiptParser.parse("TOTAL ALL 1000").currencies)
        assertEquals(setOf("USD"),ReceiptParser.parse("Currency: USD\nTOTAL 10.00").currencies)
        assertEquals(setOf("EUR"),ReceiptParser.parse("TOTAL 10.00 EUR").currencies)
    }
    @Test fun itemQuantityPointsAndSavingsAreNotPurchaseTotals() {
        for(label in listOf("TOTAL ITEMS","TOTAL QTY","TOTAL POINTS","TOTAL SAVINGS","合计件数")) {
            assertNull(label,ReceiptParser.parse("$label 2\nPAYMENT RM 106.00").suggestedAmount)
        }
    }
    @Test fun boundsInputAndNeverSelectsUnlabelledItemAsTotal() {
        assertNull(ReceiptParser.parse("Camera 100.00\nCase 20.00").suggestedAmount)
        assertEquals(2,ReceiptParser.parse("Camera 100.00\nCase 20.00").amounts.size)
        try {ReceiptParser.parse("x".repeat(100001));fail("Unbounded receipt accepted")}catch(_:IllegalArgumentException){}
    }
}
