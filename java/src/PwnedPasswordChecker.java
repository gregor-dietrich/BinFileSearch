import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PwnedPasswordChecker {
    private final static String path = "D:/pwned-passwords/pwned-passwords-sha1-ordered-by-hash-full.txt";

    private static long binarySearch(final RandomAccessFile haystack, final String needle, long start, long end) throws IOException {
        if (start > end) return 0;

        long middle = (start + end) / 2;
        haystack.seek(middle);
        haystack.readLine();

        String line = haystack.readLine();
        if (line == null) return 0;

        String hash = line.substring(0, 40);

        return needle.equals(hash) ? Long.parseLong(line.substring(41)) :
        needle.compareTo(hash) < 0 ? binarySearch(haystack, needle, start, middle - 1) :
        /* needle.compareTo(hash) > 0 ? */ binarySearch(haystack, needle, middle + 1, end);
    }

    private static long getCount(final String needle) throws IOException {
        try (RandomAccessFile haystack = new RandomAccessFile(path, "r")) {
            long size = haystack.length();
            return binarySearch(haystack, needle, 0, size);
        }
    }

    private static String getHash(final String needle) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(needle.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : hash) hexStringBuilder.append(String.format("%02x", b));

        return hexStringBuilder.toString().toUpperCase();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        for (String arg : args) {
            System.out.println("Password: " + arg);
            System.out.println("Hash: " + getHash(arg));
            System.out.println("Count: " + getCount(getHash(arg)) + "\n");
        }
    }
}