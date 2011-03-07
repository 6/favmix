/*
 * File: Profile.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package controllers;

import models.User;
import play.data.validation.Required;
import play.i18n.Messages;

/**
 * Controller for handling of viewing and modifying user profiles.
 *
 * @author Peter Graham
 */
public class Profile extends BaseController{

    /**
     * View a user's profile.
     *
     * @param id the unique user ID of the user whose profile to view
     */
    public static void index(Long id) {
        // check if this profile is the logged in user's profile
        String name = null;
        boolean isOwnProfile = false;
        User curUser;
        if(isLoggedIn() && id.equals(getUser().getId())) {
            isOwnProfile = true;
            curUser = getUser();
        }
        else {
            // viewing someone else's profile
            curUser = getUserModel().findById(id);
        }
        if(curUser != null) {
            renderArgs.put("bio", curUser.getBio());
            renderArgs.put("topics",getUserTopicModel()
                    .getTopicsByUser(curUser));
        }
        renderArgs.put("name", getUserName(curUser));
        renderArgs.put("isOwnProfile",isOwnProfile);
        render();
    }

    /**
     * Show the profile edit form.
     */
    public static void showEditForm(){
        if(!isLoggedIn()){
            // if not logged in, redirect to home
            Home.defaultFilters();
        }
        renderArgs.put("editName", getUserName(getUser()));
        renderArgs.put("editBio", getUser().getBio());
        renderTemplate("Profile/edit.html");
    }

    /**
     * Validate and modify user's profile.
     *
     * @param name String representing the user's name
     * @param bio String representing the user's short biography
     */
    public static void modifyProfile(@Required String name, String bio) {
        if(!isLoggedIn()){
            // if not logged in, redirect to home
            Home.defaultFilters();
        }
        if(!validation.hasErrors()) {
            // name is valid, so update
            User user = getUser();
            user.setName(name);
            user.setBio(bio);
            user.update();
            flash.success(Messages.get("action.saved"));
        }
        else{
            flash.error(Messages.get("form.emptyField"));
        }
        showEditForm();
    }

    /**
     * View a user's followed topics.
     *
     * @param id the unique user ID of the user whose followed topics to view
     */
    public static void showTopics(Long id){
        renderTemplate("Profile/topics.html");
    }

    /**
     * Adds a topic from the user's followed topics.
     *
     * @param topicName the name of the topic to follow
     */
    public static void followTopic(String topicName) {
        if(!isLoggedIn()){
            // if not logged in, can't edit so redirect to login page
            Account.showLoginForm();
        }
        if(getTopicModel().topicExists(topicName)) {
            // topic is valid
            getUser().followTopic(getTopicModel().findByName(topicName));
        }
        index(getUser().getId());
    }

    /**
     * Removes a topic from the user's followed topics.
     *
     * @param topicName the name of the topic to remove
     */
    public static void unFollowTopic(String topicName) {
        if(!isLoggedIn()){
            // if not logged in, can't edit so redirect to home
            Home.defaultFilters();
        }
        if(getTopicModel().topicExists(topicName)) {
            // topic is valid
            getUser().unFollowTopic(getTopicModel().findByName(topicName));
        }
        index(getUser().getId());
    }
}