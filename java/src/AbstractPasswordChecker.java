import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractPasswordChecker {
    protected final static String path = "D:/pwned-passwords/pwned-passwords-sha1-ordered-by-hash-full.txt";

    public final String getHash(String needle) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        final byte[] hash = digest.digest(needle.getBytes(StandardCharsets.UTF_8));

        final StringBuilder hexStringBuilder = new StringBuilder();
        for (final byte b : hash) hexStringBuilder.append(String.format("%02x", b));

        return hexStringBuilder.toString().toUpperCase();
    }

    public abstract long getCount(String needle) throws IOException;
}