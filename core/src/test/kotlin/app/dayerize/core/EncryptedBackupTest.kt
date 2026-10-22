package app.dayerize.core
import org.junit.Test
import org.junit.Assert.*
class EncryptedBackupTest {
    @Test fun largeArchiveIsRejectedBeforeEncryption() {
        try{EncryptedBackup.encrypt(ByteArray(8*1024*1024+1),"long test password".toCharArray());fail("Unbounded mobile backup")}catch(_:IllegalArgumentException){}
    }
    @Test fun randomSaltRoundTripAndAuthentication() {
        val plain="private camera serial".toByteArray();val password="long test password".toCharArray()
        val a=EncryptedBackup.encrypt(plain,password);val b=EncryptedBackup.encrypt(plain,password)
        assertFalse(a.contentEquals(b));assertArrayEquals(plain,EncryptedBackup.decrypt(a,password))
        for(bytes in listOf(a.copyOf().also{it[it.lastIndex]=(it.last().toInt() xor 1).toByte()},a.copyOf(10))) {
            try{EncryptedBackup.decrypt(bytes,password);fail("Accepted damaged archive")}catch(expected:IllegalArgumentException){}
        }
        try{EncryptedBackup.decrypt(a,"wrong password".toCharArray());fail("Accepted wrong password")}catch(expected:IllegalArgumentException){}
    }
}
