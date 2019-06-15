package br.com.ucl.cryptocoin

import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.InvalidParameterSpecException
import javax.crypto.*

object Cryptography {

    @Throws(
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class
    )
    fun generatetKey(): SecretKey {
        val kgen = KeyGenerator.getInstance("AES")
        return kgen.generateKey()
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidParameterSpecException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        UnsupportedEncodingException::class
    )
    fun encrypt(data: String, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    fun decrypt(ciphetText: ByteArray, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(ciphetText), StandardCharsets.UTF_8)
    }

}