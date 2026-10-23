package app.dayerize.core
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class OwnershipTest {
    private val day=LocalDate.parse("2026-10-22")
    private val item=Asset("one","workspace","Camera",Money(10000,"MYR"),"2026-10-01")
    @Test fun duplicateKeepsDescriptionButNotLifecycleOrPrivateSerial() {
        val source=item.copy(serial="private",status=Status.RETIRED,retiredDate="2026-10-20",uses=listOf(Usage("2026-10-03",2)))
        val copy=Ownership.duplicate(source,"two",day)
        assertEquals("two",copy.id);assertEquals(source.icon,copy.icon)
        assertEquals(Status.ACTIVE,copy.status);assertNull(copy.retiredDate)
        assertEquals("",copy.serial);assertTrue(copy.uses.isEmpty());assertEquals(day.toString(),copy.purchaseDate)
    }
    @Test fun costsIncludeExpensesButNotPurchaseTwiceAndUsageCanBeCorrected() {
        val a=item.copy(events=listOf(OwnershipEvent("repair","2026-10-10","repair","Repair",5000)),uses=listOf(Usage("2026-10-03",2)))
        assertEquals(Money(15000,"MYR"),Ownership.totalCost(a))
        assertEquals("75.00000000",Ownership.costPerUse(a)!!.toPlainString())
        val corrected=Ownership.removeUsage(a,0)
        assertNull(Ownership.costPerUse(corrected));assertEquals(item.purchase,corrected.purchase)
    }
    @Test fun saleDeductsRecoveryFromExpenseInclusiveCostPerUse() {
        val a=item.copy(events=listOf(OwnershipEvent("r","2026-10-10","repair","Repair",2000)),uses=listOf(Usage("2026-10-03",2)),status=Status.SOLD,sale=Sale(Money(8000,"MYR"),"2026-10-22"))
        assertEquals("20.00000000",Ownership.costPerUse(a)!!.toPlainString())
    }
    @Test fun removingWrongExpensePreservesOtherEvents() {
        val a=item.copy(events=listOf(OwnershipEvent("r","2026-10-10","repair","",2000),OwnershipEvent("m","2026-10-12","maintenance","",1000)))
        val corrected=Ownership.removeEvent(a,"r")
        assertEquals(listOf("m"),corrected.events.map{it.id});assertEquals(Money(11000,"MYR"),Ownership.totalCost(corrected))
        assertEquals(corrected,Ownership.removeEvent(corrected,"r"))
    }
    @Test fun invalidEventAndNegativeSavingsAreRejected() {
        for(a in listOf(item.copy(events=listOf(OwnershipEvent("bad","2026-11-01","repair","",10))),item.copy(savingsMinor=-1))) {
            try{a.validate(day);fail("Accepted invalid ownership data")}catch(expected:IllegalArgumentException){}
        }
    }
    @Test fun newFieldsRoundTripAndLegacyBackupStillLoads() {
        val a=item.copy(location="Studio",collections=listOf("Camera kit","Travel"),returnBy="2026-11-01",maintenanceDate="2026-12-01",events=listOf(OwnershipEvent("r","2026-10-12","repair","Service",300)))
        val restored=BackupCodec.decode(BackupCodec.encode(BackupData(workspaceId=a.workspaceId,assets=listOf(a))),day)
        assertEquals(a,restored.assets.single())
        val legacy="""{"workspaceId":"workspace","assets":[{"id":"one","workspaceId":"workspace","name":"Old","purchase":{"minor":100,"currency":"MYR"},"purchaseDate":"2026-10-01"}]}"""
        assertEquals(emptyList<OwnershipEvent>(),BackupCodec.decode(legacy,day).assets.single().events)
    }
}
