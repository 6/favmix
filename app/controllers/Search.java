/*
 * File: Search.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 18
 */
package controllers;

import models.TopicModel;
import utilities.AllowGuest;
import utilities.Constants;

/**
 * Controller for searching for topics.
 *
 * @author Peter Graham
 */
@AllowGuest({"index"})
public class Search extends BaseController {

    /**
     * Redirect to a topic page if exact match is found, or display that no
     * results are found.
     */
    public static void index() { 
        String searchQuery = params.get("q");
        if(searchQuery != null) {
            TopicModel topic = getTopicModel().findByName(searchQuery);
            if(topic != null){
                Topic.showUpdates(topic.getName(), Constants.DEFAULT_ORDER, 0);
            }
        }
        renderArgs.put("searchQuery", searchQuery);
        render();
    }
}