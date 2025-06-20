package PasswordCheckers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface IPasswordChecker {
    long getCount(String needle) throws IOException, NoSuchAlgorithmException;
}