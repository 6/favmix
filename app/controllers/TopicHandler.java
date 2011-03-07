/*
 * File: Topics.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package controllers;

import java.util.List;
import models.Topic;
import models.Update;
import models.UserTopic;
import play.data.validation.Required;
import play.i18n.Messages;

/**
 * Controller for handling of viewing and adding updates to topics.
 *
 * @author Peter Graham
 */
public class TopicHandler extends BaseController{

    /**
     * View a topic and its updates.
     *
     * @param topicName the unique name of the topic to view
     */
    public static void viewUpdates(String topicName) {
        // get the topic associated with this name
        Topic topic = getTopicModel().findByName(topicName);
        if(topic == null) {
            ErrorHandler.showError(404);
        }
        boolean isFollowing = false;
        if(isLoggedIn()){
            // see if user is following this topic
            isFollowing = getUserTopicModel().isUserFollowing(getUser(), topic);
        }

        List<Update> updates = getUpdateModel().findNewestByTopic(topic);
        renderArgs.put("topicName", topicName);
        renderArgs.put("updates",updates);
        renderArgs.put("following",isFollowing);
        renderTemplate("TopicHandler/index.html");
    }

    /**
     * Show the form for creating a new topic.
     */
    public static void showNewTopicForm(){
        if(!isLoggedIn()){
            // if not logged in, redirect to home
            Home.defaultFilters();
        }
        renderTemplate("TopicHandler/new.html");
    }

    /**
     * Validate and add an update to a topic.
     *
     * @param topicName the name of the topic to add an update to
     * @param content String representing the update to add
     */
    public static void addUpdate(@Required String topicName,
            @Required String content) {
        if(!isLoggedIn()){
            // if not logged in, redirect to home
            Home.defaultFilters();
        }
        if(!validation.hasErrors()) {
            if(getTopicModel().topicExists(topicName)) {
                Update update = new Update(getUser(), 
                        getTopicModel().findByName(topicName), content);
                update.insert();
                flash.success(Messages.get("topic.updateAdded"));
            }
        }
        else {
            flash.error(Messages.get("form.emptyField"));
        }
        viewUpdates(topicName);
    }

    /**
     * Validate and create a new topic.
     *
     * @param name String representing the topic name
     */
    public static void createNewTopic(@Required String name) {
        if(!isLoggedIn()){
            // if not logged in, redirect to home
            Home.defaultFilters();
        }
        if(!validation.hasErrors()) {
            // name is valid so check if available
            if(getTopicModel().topicExists(name)) {
                flash.error(Messages.get("topic.exists"));
            }
            else{
                // insert the new topic
                Topic newTopic = new Topic(name);
                newTopic.insert();

                // make user follow that topic
                UserTopic userTopic = new UserTopic(getUser().getId(),
                        newTopic.getId());
                userTopic.insert();
                
                flash.success(Messages.get("action.saved"));
                viewUpdates(name);
            }
        }
        else {
            flash.error(Messages.get("form.emptyField"));
        }
        showNewTopicForm();
    }
}