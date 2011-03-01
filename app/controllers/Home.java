/*
 * File: Home.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 16
 */
package controllers;

import java.util.Arrays;
import java.util.List;
import play.i18n.Messages;

/**
 * Controller for the home page, allowing visitors to view and filter news.
 * News can be filtered by scope (the user's own news - you, or everyone's news)
 * and news can also be filtered by order (ordered by most popular or recent).
 *
 * @author Peter Graham
 */
public class Home extends BaseController {

    /** List of valid scopes for filtering */
    private static List<String> scopes = Arrays.asList("you", "everyone");

    /** List of valid order for filtering */
    private static List<String> orders = Arrays.asList("popular", "recent");

    /**
     * Render the home page with default filters. Default order is popular, and
     * default scope is everyone if not logged in or you if logged in.
     */
    public static void defaultFilters() {
        String scope = "everyone";
        if(isLoggedIn()) {
            scope = "you";
        }
        renderIndex(scope,"popular");
    }

    /**
     * Render home page with specified filters.
     *
     * @param scope the scope of the news (you, everyone)
     * @param order the order of the news (popular, recent)
     */
    public static void filter(String scope, String order) {
        // if scope or order is invalid, redirect to index
        if(!scopes.contains(scope) || !orders.contains(order)){
            defaultFilters();
        }
        // otherwise, render index with appropriate filters applied
        else{
            renderIndex(scope, order);
        }
    }

    /**
     * Renders the home page with specified filters applied.
     * 
     * @param scope the scope of the news (you, everyone)
     * @param order the order of the news (popular, recent)
     */
    private static void renderIndex(String scope, String order) {
        // if scope is "your news" and not logged in, redirect to login page
        if(scope.equals("you") && !isLoggedIn()) {
            flash.error(Messages.get("login.loginRequired"));
            Account.showLoginForm();
        }
        // make these variables accessible in the view
        renderArgs.put("scope",scope);
        renderArgs.put("order",order);
        renderTemplate("Home/index.html");
    }
}
