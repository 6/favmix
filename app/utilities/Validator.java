/*
 * File: Validator.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package utilities;

import java.util.regex.Matcher;

/**
 * A helper class for basic validations.
 *
 * @author Peter Graham
 */
public class Validator {

    /**
     * Validate an e-mail address.
     *
     * @param email String of email address to check
     * @return true if valid email address, otherwise false
     */
    public static boolean isEmail(String email) {
        Matcher match = Constants.REGEX_EMAIL.matcher(email);
        return email != null && match.find();
    }

    /**
     * Check if a string is empty or null.
     *
     * @param toCheck String to check
     * @return true if null or empty String, otherwise false
     */
    public static boolean isEmpty(String toCheck) {
        return toCheck == null || toCheck.length() < 1;
    }
}
