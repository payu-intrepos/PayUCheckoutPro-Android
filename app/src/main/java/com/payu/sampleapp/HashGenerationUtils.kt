package com.payu.sampleapp

import java.security.MessageDigest

object HashGenerationUtils {

    /*
Do not use this, you may use this only for testing.
This should be done from server side..
Do not keep salt anywhere in app.
*/
    fun generateHashFromSDK(
        paymentParams: String,
        salt: String?
    ): String? {

        return calculateHash("SHA-512","$paymentParams$salt")
    }

    /**
     * Function to calculate the SHA-512 hash
     * @param hashString hash string for hash calculation
     * @return Post Data containig the
     * */
    private fun calculateHash(type: String, hashString: String): String? {
        val messageDigest =
            MessageDigest.getInstance(type)
        messageDigest.update(hashString.toByteArray())
        val mdbytes = messageDigest.digest()
        return getHexString(mdbytes)
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
}