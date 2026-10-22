package app.dayerize.core

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Versioned authenticated envelope. The password is never persisted. */
object EncryptedBackup {
    private val magic=byteArrayOf(68,65,89,69,82,1)
    const val LIMIT=8*1024*1024
    fun isEncrypted(bytes:ByteArray)=bytes.size>=magic.size && bytes.copyOfRange(0,magic.size).contentEquals(magic)
    private fun key(password:CharArray,salt:ByteArray):SecretKeySpec {
        val spec=PBEKeySpec(password,salt,210000,256)
        return try{val bytes=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            try{SecretKeySpec(bytes,"AES")}finally{bytes.fill(0)}
        }finally{spec.clearPassword()}
    }
    fun encrypt(plain:ByteArray,password:CharArray):ByteArray {
        require(password.size>=12 && plain.size<=LIMIT)
        val random=SecureRandom();val salt=ByteArray(16).also(random::nextBytes);val nonce=ByteArray(12).also(random::nextBytes)
        val header=magic+salt+nonce
        val cipher=Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE,key(password,salt),GCMParameterSpec(128,nonce));cipher.updateAAD(header)
        return header+cipher.doFinal(plain)
    }
    fun decrypt(bytes:ByteArray,password:CharArray):ByteArray {
        try {
            require(bytes.size in 50..(LIMIT+50) && isEncrypted(bytes) && password.isNotEmpty())
            val cipher=Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE,key(password,bytes.copyOfRange(6,22)),GCMParameterSpec(128,bytes.copyOfRange(22,34)))
            cipher.updateAAD(bytes.copyOfRange(0,34))
            return cipher.doFinal(bytes,34,bytes.size-34)
        }catch(e:Exception){throw IllegalArgumentException("Wrong password or damaged backup",e)}
    }
}
