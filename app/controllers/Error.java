/*
 * File: Error.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

/**
 * Handles HTTP errors.
 *
 * @author Peter Graham
 */
public class Error extends BaseController {

    /**
     * Generic method for handling all error messages.
     *
     * @param errorCode the HTTP error code
     * @param message the message to display
     */
    public static void index(int errorCode, String message) {
        renderArgs.put("errorMessage", message);
        response.status = errorCode;
        render();
    }
}
