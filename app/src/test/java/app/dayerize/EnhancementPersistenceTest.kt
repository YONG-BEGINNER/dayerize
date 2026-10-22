package app.dayerize
import app.dayerize.core.*
import app.dayerize.data.AssetStore
import app.dayerize.backup.FileBackup
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import org.junit.Test
import org.junit.Assert.*
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk=[35])
class EnhancementPersistenceTest {
    @Test fun encryptedArchivePreservesOwnershipProfileAndDensityWithoutChangingDataOnWrongPassword()=runBlocking {
        val ctx=org.robolectric.RuntimeEnvironment.getApplication()
        val store=AssetStore(ctx)
        try {
            store.initialize();val w=store.settings().getValue("workspace");val day=store.today().toString()
            val a=Asset("new","$w","Camera",Money(10000,"MYR"),day,location="Studio",collections=listOf("Travel","Camera kit"),favourite=true,uses=listOf(Usage(day,2)),events=listOf(OwnershipEvent("r",day,"repair","repair",500)))
            store.save(a);store.preference("density","compact");store.preference("profileName","Owner");store.preference("birthday","1990-01-01")
            ctx.getSharedPreferences("artwork_picker",0).edit().putStringSet("favourites",setOf("camera")).putString("recent","camera|laptop").commit()
            val archive=ByteArrayOutputStream();FileBackup(store).export(archive)
            val encrypted=EncryptedBackup.encrypt(archive.toByteArray(),"correct long password".toCharArray())
            try{EncryptedBackup.decrypt(encrypted,"wrong".toCharArray());fail("Wrong password accepted")}catch(_:IllegalArgumentException){}
            assertEquals(1,store.dao.all().size)
            val restored=ArchiveCodec.read(EncryptedBackup.decrypt(encrypted,"correct long password".toCharArray()).inputStream(),store.today())
            ctx.getSharedPreferences("artwork_picker",0).edit().clear().commit()
            store.dao.clear();FileBackup(store).restore(restored)
            assertEquals(a,store.json.decodeFromString<Asset>(store.dao.all().single().payload))
            assertEquals(setOf("camera"),ctx.getSharedPreferences("artwork_picker",0).getStringSet("favourites",emptySet()))
            assertEquals("camera|laptop",ctx.getSharedPreferences("artwork_picker",0).getString("recent",null))
            assertEquals("compact",store.settings()["density"]);assertEquals("Owner",store.settings()["profileName"])
            val duplicate=Ownership.duplicate(a,"copy",store.today());store.save(duplicate)
            assertEquals(2,store.dao.all().size);assertTrue(duplicate.events.isEmpty());assertFalse(duplicate.favourite)
        }finally{store.database.close()}
    }
}
