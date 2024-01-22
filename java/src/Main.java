import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        final List<AbstractPasswordChecker> checkers = new ArrayList<>();
        checkers.add(new RandomAccessPasswordChecker());
        checkers.add(new FileChannelPasswordChecker());

        for (final AbstractPasswordChecker checker : checkers) {
            for (final String arg : args) {
                System.out.println("Password: " + arg);
                System.out.println("Hash: " + checker.getHash(arg));
                System.out.println("Count: " + checker.getCount(checker.getHash(arg)) + "\n");
            }
        }
    }
}