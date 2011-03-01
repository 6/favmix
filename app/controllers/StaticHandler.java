/*
 * File: StaticHandler.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package controllers;

import java.io.File;
import play.mvc.Controller;

/**
 * Handles routing of static files that are accessed from the base directory,
 * such as favicon.ico and robots.txt.
 *
 * Note: This should be replaced by staticFile routing in Play version 1.2.
 *
 * Code for this from:
 * http://groups.google.com/group/play-framework
 *  /browse_thread/thread/5b3882b641670c4b
 *
 * @author Peter Graham
 * @author Guillaume Bort
 */
public class StaticHandler extends Controller {

    /**
     * Render the robots.txt file.
     */
    public static void robots(){
        File file = play.Play.getFile("public/robots.txt");
        response.cacheFor("24h");
        renderBinary(file);
    }

    /**
     * Render the favicon file.
     */
    public static void favicon(){
        File file = play.Play.getFile("public/favicon.ico");
        response.cacheFor("300h");
        renderBinary(file);
    }
}
