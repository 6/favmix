/*
 * File: Profile.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

import models.UserModel;
import play.i18n.Messages;
import utilities.AllowGuest;

/**
 * Controller for handling of viewing and modifying user profiles.
 *
 * @author Peter Graham
 */
@AllowGuest({"index", "topics"})
public class Profile extends BaseController {

    /**
     * View a user's profile.
     *
     * @param id the unique user ID of the user whose profile to view
     */
    public static void index(Long id) {
        // check if this profile is the logged in user's profile
        UserModel user;
        if(isLoggedIn() && getUser().getId().equals(id)) {
            user = getUser();
            renderArgs.put("isOwnProfile", true);
        }
        else {
            // viewing someone else's profile
            user = getUserModel().findById(id);
            renderArgs.put("isOwnProfile", false);
        }
        if(user == null) {
            Error.index(404, Messages.get("profile.notFound"));
        }
        renderArgs.put("bio", user.getBio());
        renderArgs.put("topics", getUserTopicModel().getTopicsByUser(user));
        renderArgs.put("name", user.getName());
        render();
    }

    /**
     * Show the profile edit form.
     */
    public static void edit(){
        renderArgs.put("editName", getUser().getName());
        renderArgs.put("editBio", getUser().getBio());
        render();
    }

    /**
     * Modify user's profile.
     *
     * @param name String representing the user's name
     * @param bio String representing the user's short biography
     */
    public static void onEditSubmit(String name, String bio) {
        getUser().modifyProfile(name, bio);
        flash.success(Messages.get("action.saved"));
        index(getUser().getId());
    }

    /**
     * View a user's followed topics.
     *
     * @param id the unique user ID of the user whose followed topics to view
     */
    public static void topics(Long id){
        render();
    }

    /**
     * Adds a topic from the user's followed topics.
     *
     * @param topicName the name of the topic to follow
     * @param sortBy how the user had sorted the updates
     */
    public static void followTopic(String topicName, String sortBy) {
        getUser().followTopic(getTopicModel().findByName(topicName));
        // go to that topic page
        Topic.index(topicName, sortBy);
    }

    /**
     * Removes a topic from the user's followed topics.
     *
     * @param topicName the name of the topic to remove
     * @param sortBy how the user had sorted the updates
     */
    public static void unFollowTopic(String topicName, String sortBy) {
        getUser().unFollowTopic(getTopicModel().findByName(topicName));
        // go back to user profile
        index(getUser().getId());
    }
}