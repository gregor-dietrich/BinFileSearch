package PasswordCheckers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {
    public static String getHash(String needle) throws NoSuchAlgorithmException {
        return getHash(needle, "SHA-1");
    }

    public static String getHash(String needle, String algorithm) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance(algorithm);
        final byte[] hash = digest.digest(needle.getBytes(StandardCharsets.UTF_8));

        final StringBuilder hexStringBuilder = new StringBuilder();
        for (final byte b : hash) {
            hexStringBuilder.append(String.format("%02x", b));
        }

        return hexStringBuilder.toString().toUpperCase();
    }
}