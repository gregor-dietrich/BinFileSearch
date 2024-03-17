import PasswordCheckers.HashUtil;
import PasswordCheckers.IPasswordChecker;
import PasswordCheckers.FileChannelPasswordChecker;
import PasswordCheckers.RandomAccessPasswordChecker;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PasswordCheckerApp {
    private static String parseArgs(final String[] args) {
        if (args.length < 2) {
            return "";
        }
        return args[0];
    }

    private static Map<String, Long> doBenchmark(final List<IPasswordChecker> checkers,
                                                       final String[] args)
            throws IOException, NoSuchAlgorithmException {
        final Map<String, Long> results = new HashMap<>();
        final StringBuilder output = new StringBuilder();

        for (final IPasswordChecker checker : checkers) {
            final Instant start = Instant.now();
            for (int i = 1; i < args.length; i++) {
                output.append("Password: ").append(args[i]).append("\n");
                output.append("Hash: ").append(HashUtil.getHash(args[i])).append("\n");
                output.append("Count: ").append(checker.getCount(args[i])).append("\n\n");
            }
            final long timeElapsed = Duration.between(start, Instant.now()).toMillis();
            results.put(checker.getClass().getSimpleName(), timeElapsed);
        }

        System.out.println(output);
        return results;
    }

    private static Map<String, Long[]> doBenchmark(final List<IPasswordChecker> checkers,
                                                   final String[] args, final int runs)
            throws IOException, NoSuchAlgorithmException {
        final Map<String, Long[]> results = new HashMap<>();
        for (int i = 0; i < runs; i++) {
            final Map<String, Long> temp = doBenchmark(checkers, args);
            for (Map.Entry<String, Long> entry : temp.entrySet()) {
                if (results.containsKey(entry.getKey())) {
                    results.get(entry.getKey())[0] += entry.getValue();
                    results.get(entry.getKey())[1]++;
                    continue;
                }
                results.put(entry.getKey(), new Long[]{entry.getValue(), 1L});
            }
        }
        return results;
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        final String path = parseArgs(args);

        if (path.isEmpty()) {
            System.err.println("Invalid arguments");
            System.err.println("Usage: java -jar PasswordCheckerApp.jar <path/to/txt> <password> [<password> ...]");
            return;
        }

        if (!new File(path).exists()) {
            System.err.println("File does not exist: " + path);
            return;
        }

        final List<IPasswordChecker> checkers = new ArrayList<>();
        checkers.add(new RandomAccessPasswordChecker(path));
        checkers.add(new FileChannelPasswordChecker(path));

        final List<Map<String, Long[]>> results = new ArrayList<>();
        for (int i = 1; i < 1000; i *= 10) {
            results.add(doBenchmark(checkers, args, i));
        }

        for (final Map<String, Long[]> result : results) {
            for (final Map.Entry<String, Long[]> entry : result.entrySet()) {
                System.out.println(entry.getKey() + " average time: " + entry.getValue()[0] / entry.getValue()[1]
                        + "ms for " + (args.length - 1) + " passwords (" + entry.getValue()[1] + " runs)");
            }
            System.out.println();
        }
    }
}