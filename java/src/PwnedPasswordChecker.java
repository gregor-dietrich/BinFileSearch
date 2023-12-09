import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PwnedPasswordChecker {
    private final static String path = "D:/pwned-passwords/pwned-passwords-sha1-ordered-by-hash-full.txt";

    private static long binarySearch(RandomAccessFile haystack, String needle, long start, long end) throws IOException {
        if (start > end) return 0;

        final long middle = (start + end) / 2;
        haystack.seek(middle);
        haystack.readLine();

        final String line = haystack.readLine();
        final String hash = line.substring(0, 40);

        return needle.equals(hash) ? Long.parseLong(line.substring(41)) :
                needle.compareTo(hash) < 0 ? binarySearch(haystack, needle, start, middle - 1) :
                        /* needle.compareTo(hash) > 0 ? */ binarySearch(haystack, needle, middle + 1, end);
    }

    private static long getCount(String needle) throws IOException {
        try (RandomAccessFile haystack = new RandomAccessFile(path, "r")) {
            final long size = haystack.length();
            return binarySearch(haystack, needle, 0, size);
        }
    }

    private static String getHash(String needle) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        final byte[] hash = digest.digest(needle.getBytes(StandardCharsets.UTF_8));

        final StringBuilder hexStringBuilder = new StringBuilder();
        for (final byte b : hash) hexStringBuilder.append(String.format("%02x", b));

        return hexStringBuilder.toString().toUpperCase();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        for (final String arg : args) {
            System.out.println("Password: " + arg);
            System.out.println("Hash: " + getHash(arg));
            System.out.println("Count: " + getCount(getHash(arg)) + "\n");
        }
    }
}