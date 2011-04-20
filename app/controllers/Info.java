/*
 * File: Info.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 16
 */
package controllers;

import utilities.AllowGuest;

/**
 * Controller for the About, Contact, and Help pages.
 *
 * @author Peter Graham
 */
@AllowGuest({"about","contact"})
public class Info extends BaseController{

    /**
     * Render the About page.
     */
    public static void about() {
        placeholder();
    }

    /**
     * Render the Contact page.
     */
    public static void contact() {
        placeholder();
    }

    /**
     * Temporary placeholder for the About, Contact, and Help pages.
     */
    private static void placeholder(){
        renderTemplate("Info/placeholder.html");
    }
}