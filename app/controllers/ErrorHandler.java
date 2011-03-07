/*
 * File: ErrorHandler.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package controllers;

/**
 * Handles HTTP errors.
 *
 * @author Peter Graham
 */
public class ErrorHandler extends BaseController {

    /**
     * Generic method for handling all error messages.
     *
     * @param errorCode the HTTP error code
     */
    public static void showError(int errorCode) {
        renderTemplate("errors/"+String.valueOf(errorCode)+".html");
    }
}
