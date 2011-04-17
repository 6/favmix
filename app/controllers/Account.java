/*
 * File: Account.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

import models.UserModel;
import play.data.validation.Required;
import play.i18n.Messages;
import play.libs.Crypto;
import utilities.AllowGuest;
import utilities.Constants;
import utilities.DenyUser;

/**
 * Controller for handling of user login/logout/registration and user settings
 * operations.
 *
 * @author Peter Graham
 */
@AllowGuest({"login", "register", "onLoginSubmit", "onRegisterSubmit"})
@DenyUser({"login", "register", "onLoginSubmit", "onRegisterSubmit"})
public class Account extends BaseController {

    /**
     * Render the registration form.
     */
    public static void register() {
        render();
    }

    /**
     * Render the settings form.
     */
    public static void settings() {
        renderArgs.put("editEmail", getUser().getEmail());
        render();
    }
    
    /**
     * Render the login form.
     */
    public static void login() {
        // persist this original URL
        flash.keep(Constants.ORIGINAL_URL);
        render();
    }

    /**
     * Validate login information and login user if valid.
     *
     * @param email the submitted email address
     * @param password the submitted password
     * @param remember true if "Remember me" cookie is checked
     */
    public static void onLoginSubmit(@Required String email,
            @Required String password, boolean remember) {
        if(!validation.hasErrors()) {
            // check against database
            if(getUserModel().isValidLogin(email, password)) {
                // create Remember me cookie if needed
                if(remember) {
                    response.setCookie(Constants.REMEMBER_ME, 
                            Crypto.sign(email) + "-" + email, "30d");
                }
                session.put(Constants.SESSION_KEY, email);
                redirect(getOriginalUrl());
            }
        }
        flash.error(Messages.get("login.incorrect"));
        params.flash();
        flash.keep(Constants.ORIGINAL_URL);
        // redisplay the login page
        login();
    }

    /**
     * Validate registration information and create an account if valid.
     *
     * @param email the submitted email address
     * @param password the submitted password
     */
    public static void onRegisterSubmit(@Required String email,
            @Required String password) {
        if(!validation.hasErrors()) {
            if(getUserModel().isEmailAvailable(email)) {
                getUserModel().createUser(email, password);
                session.put(Constants.SESSION_KEY, email);
                Home.defaultFilters();
            }
            else {
                // email is in database
                flash.error(Messages.get("register.emailUsed", email));
            }
        }
        else {
            flash.error(Messages.get("form.emptyField"));
        }
        params.flash();
        // redisplay the register page
        register();
    }

    /**
     * Validate and modify user's settings. TODO lean
     *
     * @param email String representing the user's email
     * @param oldPassword String representing the user's old password
     * @param newPassword String representing the user's new password
     */
    public static void onSettingsSubmit(@Required String email,
            String oldPassword, String newPassword) {
        boolean hasError = validation.hasErrors();
        if(hasError) {
            flash.error(Messages.get("form.emptyField"));
        }
        // validate email
        if(!hasError && !email.equals(getUser().getEmail())
                && !getUserModel().isEmailAvailable(email)) {
            // email is invalid
            hasError = true;
            flash.error(Messages.get("form.emailUsed", email));
        }
        // valid new password if filled out
        if(!hasError && !oldPassword.equals("") &&
                !getUser().isValidPassword(oldPassword)) {
            hasError = true;
            flash.error(Messages.get("form.badPassword"));
        }
        if(!hasError) {
            UserModel user = getUser();
            user.setEmail(email);
            if(!newPassword.equals("")) {
                user.setPassword(newPassword);
            }
            user.update();
            flash.success(Messages.get("action.saved"));
        }
        settings();
    }

    /**
     * Logout and return to homepage. Delete "Remember me" cookie if applicable.
     */
    public static void logout() {
        session.clear();
        response.removeCookie(Constants.REMEMBER_ME);
        Home.defaultFilters();
    }
}