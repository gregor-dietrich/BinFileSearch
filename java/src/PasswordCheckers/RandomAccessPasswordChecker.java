package PasswordCheckers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;

public final class RandomAccessPasswordChecker implements IPasswordChecker {
    final RandomAccessFile fileStream;
    final long fileSize;

    public RandomAccessPasswordChecker(String path) throws IOException {
        this.fileStream = new RandomAccessFile(path, "r");
        this.fileSize = fileStream.length();
    }

    private long binarySearch(RandomAccessFile haystack, String needle, long start, long end) throws IOException {
        if (start > end) {
            return 0;
        }

        final long middle = (start + end) / 2;
        haystack.seek(middle);
        haystack.readLine();

        final String line = haystack.readLine();
        final String hash = line.substring(0, needle.length());

        return needle.equals(hash) ? Long.parseLong(line.substring(needle.length() + 1)) :
                needle.compareTo(hash) < 0 ? binarySearch(haystack, needle, start, middle - 1) :
                        /* needle.compareTo(hash) > 0 ? */ binarySearch(haystack, needle, middle + 1, end);
    }

    @Override
    public long getCount(String password) throws IOException, NoSuchAlgorithmException {
        final String hash = HashUtil.getHash(password);
        return binarySearch(fileStream, hash, 0, fileSize);
    }
}
