/*
 * File: ValidationException.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package utilities;

/**
 * Custom exception for when user-submitted information is invalid.
 *
 * @author Peter Graham
 */
public class ValidationException extends Exception {

    /**
     * Creates a new ValidationException with an error message.
     *
     * @param message String of the error message
     */
    public ValidationException(String message) {
        super(message);
    }
}
