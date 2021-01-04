package com.payu.sampleapp;

import java.security.MessageDigest;

public final class HashGenerationUtils {

    public static String generateHashFromSDK(String paymentParams, String salt) {
        return calculateHash("SHA-512", paymentParams + salt);
    }

    private static String calculateHash(String type, String hashString) {
        try {
            StringBuilder hash = new StringBuilder();
            MessageDigest messageDigest = MessageDigest.getInstance(type);
            messageDigest.update(hashString.getBytes());
            byte[] mdbytes = messageDigest.digest();
            for (byte hashByte : mdbytes) {
                hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }

            return hash.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
