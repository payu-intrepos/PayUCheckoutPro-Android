package com.payu.sampleapp

import android.util.Base64
import android.util.Base64.NO_WRAP
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HashGenerationUtils {

    /*
Do not use this, you may use this only for testing.
This should be done from server side..
Do not keep salt anywhere in app.
*/
    fun generateHashFromSDK(
        hashData: String,
        salt: String?,
        merchantSecretKey: String? = null
    ): String? {

        return if (merchantSecretKey.isNullOrEmpty()) calculateHash("$hashData$salt")
        else calculateHmacSha1(hashData, merchantSecretKey)
    }
    fun generateV2HashFromSDK(hashString: String,salt: String?):String?{
        return  calculateHmacSha256(hashString,salt)
    }

    /**
     * Function to calculate the SHA-512 hash
     * @param hashString hash string for hash calculation
     * @return Post Data containig the
     * */
    private fun calculateHash(hashString: String): String {
        val messageDigest =
            MessageDigest.getInstance("SHA-512")
        messageDigest.update(hashString.toByteArray())
        val mdbytes = messageDigest.digest()
        return getHexString(mdbytes)
    }

    private fun calculateHmacSha1(hashString: String, key: String): String? {
        try {
            val type = "HmacSHA1"
            val secret = SecretKeySpec(key.toByteArray(), type)
            val mac: Mac = Mac.getInstance(type)
            mac.init(secret)
            val bytes: ByteArray = mac.doFinal(hashString.toByteArray())
            return getHexString(bytes)
        } catch (e: Exception){
            return null
        }
    }

    private fun getHexString(data: ByteArray): String {
        // Create Hex String
        val hexString: StringBuilder = StringBuilder()
        for (aMessageDigest: Byte in data) {
            var h: String = Integer.toHexString(0xFF and aMessageDigest.toInt())
            while (h.length < 2)
                h = "0$h"
            hexString.append(h)
        }
        return hexString.toString()
    }
    private fun calculateHmacSha256(hashString: String, salt: String?): String? {
        return try {
            val type = "HmacSHA256"
            val secret = SecretKeySpec(salt?.toByteArray(), type)
            val mac: Mac = Mac.getInstance(type)
            mac.init(secret)
            val bytes: ByteArray = mac.doFinal(hashString.toByteArray())
            return Base64.encodeToString(bytes, NO_WRAP)
        } catch (e: Exception){
            null
        }
    }
}