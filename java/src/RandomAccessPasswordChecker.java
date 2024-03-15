import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessPasswordChecker extends AbstractPasswordChecker {
    public RandomAccessPasswordChecker(String path) {
        super(path);
    }

    protected long binarySearch(RandomAccessFile haystack, String needle, long start, long end) throws IOException {
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
    public long getCount(String needle) throws IOException {
        try (RandomAccessFile haystack = new RandomAccessFile(path, "r")) {
            final long size = haystack.length();
            return binarySearch(haystack, needle, 0, size);
        }
    }
}
