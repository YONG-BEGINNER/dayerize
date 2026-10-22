package app.dayerize
import app.dayerize.core.*
import app.dayerize.data.AssetStore
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
class AtomicOwnershipTest {
    @Test fun overlappingIndependentUpdatesPreserveBothChanges()=runBlocking {
        val store=AssetStore(org.robolectric.RuntimeEnvironment.getApplication())
        try{
            store.initialize();val day=store.today().toString()
            store.save(Asset("a",store.settings().getValue("workspace"),"Camera",Money(100,"MYR"),day))
            coroutineScope{
                launch(Dispatchers.IO){repeat(10){store.mutate("a"){it.copy(uses=it.uses+Usage(day,1))}}}
                launch(Dispatchers.IO){store.mutate("a"){it.copy(favourite=true)}}
                launch(Dispatchers.IO){store.mutate("a"){it.copy(events=it.events+OwnershipEvent("r",day,"repair","",10))}}
            }
            val a=store.json.decodeFromString<Asset>(store.dao.all().single().payload)
            assertTrue(a.favourite);assertEquals(10,a.uses.size);assertEquals(1,a.events.size)
        }finally{store.database.close()}
    }
}
