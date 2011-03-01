/*
 * File: Informational.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 16
 */
package controllers;

/**
 * Controller for the About, Contact, and Help pages.
 *
 * @author Peter Graham
 */
public class Informational extends BaseController{

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
     * Render the Help page.
     */
    public static void help() {
        placeholder();
    }

    /**
     * Temporary placeholder for the About, Contact, and Help pages.
     */
    private static void placeholder(){
        renderTemplate("Informational/placeholder.html");
    }
}