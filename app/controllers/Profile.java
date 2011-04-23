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
@AllowGuest({"index"})
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
        renderArgs.put("profile_user", user);
        renderArgs.put("topics", getUserTopicModel().getTopicsByUser(user));
        render();
    }

    /**
     * Show the profile edit form.
     */
    public static void edit() {
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
}