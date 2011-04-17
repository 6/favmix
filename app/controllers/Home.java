/*
 * File: Home.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 16
 */
package controllers;

import utilities.AllowGuest;
import utilities.Constants;

/**
 * Controller for the home page, allowing visitors to view and filter news.
 * News can be filtered by scope (the user's own news - you, or everyone's news)
 * and news can also be filtered by order (ordered by most popular or recent).
 *
 * @author Peter Graham
 */
@AllowGuest({"defaultFilters", "everyoneFilter"})
public class Home extends BaseController {

    /**
     * Render the home page with default filters. Default order is popular, and
     * default scope is everyone if not logged in or you if logged in.
     */
    public static void defaultFilters() {
        if(isLoggedIn()) {
            youFilter(Constants.DEFAULT_ORDER);
        }
        everyoneFilter(Constants.DEFAULT_ORDER);
    }
    
    /**
     * Render the homepage using the updates from topics you follow.
     * 
     * @param order the order of the updates (popular, recent)
     */
    public static void youFilter(String order) {
        if(!Constants.VALID_ORDERS.contains(order)){
            order = Constants.DEFAULT_ORDER;
        }
        renderIndex("you", order);
    }

    /**
     * Render the homepage using the updates from all topics.
     *
     * @param order the order of the updates (popular, recent)
     */
    public static void everyoneFilter(String order) {
        if(!Constants.VALID_ORDERS.contains(order)){
            order = Constants.DEFAULT_ORDER;
        }
        renderIndex("everyone", order);
    }

    /**
     * Renders the home page with specified filters applied.
     * 
     * @param scope the scope of the news (you, everyone)
     * @param order the order of the news (popular, recent)
     */
    private static void renderIndex(String scope, String order) {
        // make these variables accessible in the view
        renderArgs.put("scope",scope);
        renderArgs.put("order",order);
        renderTemplate("Home/index.html");
    }
}
