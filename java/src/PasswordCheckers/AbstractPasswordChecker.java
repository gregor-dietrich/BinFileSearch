package PasswordCheckers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractPasswordChecker {
    protected final String path;

    public AbstractPasswordChecker(final String path) {
        this.path = path;
    }

    public final String getHash(String needle) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        final byte[] hash = digest.digest(needle.getBytes(StandardCharsets.UTF_8));

        final StringBuilder hexStringBuilder = new StringBuilder();
        for (final byte b : hash) {
            hexStringBuilder.append(String.format("%02x", b));
        }

        return hexStringBuilder.toString().toUpperCase();
    }

    public abstract long getCount(String needle) throws IOException;
}