import PasswordCheckers.AbstractPasswordChecker;
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

public class PasswordCheckerApp {
    private static String path;

    private static boolean parseArgs(final String[] args) {
        if (args.length < 2) {
            return false;
        }
        path = args[0];
        return true;
    }

    private static Map<String, Long> doBenchmark(final List<AbstractPasswordChecker> checkers,
                                                       final String[] args)
            throws NoSuchAlgorithmException, IOException {
        Map<String, Long> results = new HashMap<>();
        StringBuilder output = new StringBuilder();

        for (final AbstractPasswordChecker checker : checkers) {
            Instant start = Instant.now();
            for (int i = 1; i < args.length; i++) {
                output.append("Password: ").append(args[i]).append("\n");
                output.append("Hash: ").append(checker.getHash(args[i])).append("\n");
                output.append("Count: ").append(checker.getCount(checker.getHash(args[i]))).append("\n\n");
            }
            long timeElapsed = Duration.between(start, Instant.now()).toMillis();
            results.put(checker.getClass().getSimpleName(), timeElapsed);
        }

        System.out.println(output);
        return results;
    }

    private static Map<String, Long[]> doBenchmark(final List<AbstractPasswordChecker> checkers,
                                                   final String[] args, final int runs)
            throws NoSuchAlgorithmException, IOException {
        Map<String, Long[]> results = new HashMap<>();
        for (int i = 0; i < runs; i++) {
            Map<String, Long> temp = doBenchmark(checkers, args);
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

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        if (!parseArgs(args)) {
            System.err.println("Invalid arguments");
            System.err.println("Usage: java -jar PasswordCheckerApp.jar <path/to/txt> <password> [<password> ...]");
            return;
        }

        if (!new File(path).exists()) {
            System.err.println("File does not exist: " + path);
            return;
        }

        final List<AbstractPasswordChecker> checkers = new ArrayList<>();
        checkers.add(new RandomAccessPasswordChecker(path));
        checkers.add(new FileChannelPasswordChecker(path));

        final Map<String, Long> coldResults = doBenchmark(checkers, args);
        final Map<String, Long[]> warmResults = doBenchmark(checkers, args, 100);

        for (final Map.Entry<String, Long> entry : coldResults.entrySet()) {
            System.out.println(entry.getKey() + " cold run time: " + entry.getValue()
                    + "ms for " + (args.length - 1) + " passwords (1 run)");
        }

        for (final Map.Entry<String, Long[]> entry : warmResults.entrySet()) {
            System.out.println(entry.getKey() + " average time: " + entry.getValue()[0] / entry.getValue()[1]
                    + "ms for " + (args.length - 1) + " passwords (" + entry.getValue()[1] + " runs)");
        }
    }
}