package PasswordCheckers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

public final class FileChannelPasswordChecker implements IPasswordChecker {
    private final PasswordCheckerHelper helper;

    public FileChannelPasswordChecker(PasswordCheckerHelper helper) {
        this.helper = helper;
    }

    private long binarySearch(FileChannel haystack, String needle, long start, long end) throws IOException {
        if (start > end) {
            return 0;
        }

        final long middle = (start + end) / 2;
        haystack.position(middle);

        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        haystack.read(buffer);
        buffer.flip();

        while (true) {
            if (!buffer.hasRemaining() || buffer.get() == '\n') {
                break;
            }
        }

        final StringBuilder lineBuilder = new StringBuilder();
        while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            if (c == '\n') {
                break;
            }
            lineBuilder.append(c);
        }

        final String line = lineBuilder.toString();
        final String hash = line.substring(0, needle.length());

        try {
            final String countString = line.substring(needle.length() + 1).trim(); // Trim any extra whitespace
            return needle.equals(hash) ? Long.parseLong(countString) :
                    needle.compareTo(hash) < 0 ? binarySearch(haystack, needle, start, middle - 1) :
                            /* needle.compareTo(hash) > 0 ? */ binarySearch(haystack, needle, middle + 1, end);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse number from line: " + line);
            return -1;
        }
    }

    @Override
    public long getCount(String needle) throws IOException {
        try (FileChannel haystack = FileChannel.open(Paths.get(helper.getPath()), StandardOpenOption.READ)) {
            final long size = haystack.size();
            return binarySearch(haystack, needle, 0, size);
        }
    }

    @Override
    public String getHash(String needle) throws NoSuchAlgorithmException {
        return helper.getHash(needle);
    }
}
