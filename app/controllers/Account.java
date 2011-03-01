/*
 * File: Account.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package controllers;

import models.User;
import play.data.validation.Required;
import play.i18n.Messages;
import utilities.Constants;

/**
 * Controller for handling of user login/logout/registration and user settings
 * operations.
 *
 * @author Peter Graham
 */
public class Account extends BaseController{

    /**
     * Render the settings form.
     */
    public static void showSettingsForm() {
        if(!isLoggedIn()){
            Home.defaultFilters();
        }
        renderArgs.put("editEmail", getUser().getEmail());
        renderTemplate("Account/settings.html");
    }

    /**
     * Render the login form.
     */
    public static void showLoginForm() {
        if(isLoggedIn()){
            Home.defaultFilters();
        }
        renderTemplate("Account/login.html");
    }

    /**
     * Render the registration form.
     */
    public static void showRegisterForm() {
        if(isLoggedIn()){
            Home.defaultFilters();
        }
        renderTemplate("Account/register.html");
    }

    /**
     * Validate and modify user's settings.
     *
     * @param email String representing the user's email
     * @param oldPassword String representing the user's old password
     * @param newPassword String representing the user's new password
     */
    public static void modifySettings(@Required String email,
            String oldPassword, String newPassword) {
        if(!isLoggedIn()){
            // if not logged in, redirect to home
            Home.defaultFilters();
        }
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
            User user = getUser();
            user.setEmail(email);
            if(!newPassword.equals("")) {
                user.setPassword(newPassword);
            }
            user.update();
            flash.success(Messages.get("action.saved"));
        }
        showSettingsForm();
    }

    /**
     * Validate login information and login user if valid.
     *
     * @param email the submitted email address
     * @param password the submitted password
     */
    public static void login(@Required String email, @Required String password){
        if(isLoggedIn()){
            Home.defaultFilters();
        }
        // check if email/password is empty
        boolean hasErrors = validation.hasErrors();
        if(!hasErrors) {
            // check email/password against database for validity
            User user = getUserModel().findByEmail(email);
            if(user != null) {
                if(user.isValidPassword(password)) {
                    // email/password combination is valid
                    logUserIn(user.getId());
                }
            }
        }
        flash.error(Messages.get("login.incorrect"));
        // add HTTP parameters to the flash scope
        params.flash();
        // keep the errors for the next request
        validation.keep();
        // redisplay the login page
        showLoginForm();
    }

    /**
     * Validate registration information and create an account if valid.
     *
     * @param email the submitted email address
     * @param password the submitted password
     */
    public static void register(@Required String email,
            @Required String password){
        // check if email/password is empty
        if(!validation.hasErrors()) {
            // check if email is not in database
            if(getUserModel().isEmailAvailable(email)) {
                // email/password are valid, so insert into database
                User user = new User(email, password);
                user.insert();
                logUserIn(user.getId());
            }
            else {
                // email is in database, redirect to login form
                flash.error(Messages.get("register.emailUsed", email));
                params.flash();
                showLoginForm();
            }
        }
        else {
            flash.error(Messages.get("register.emptyField"));
        }
        // add HTTP parameters to the flash scope
        params.flash();
        // keep the errors for the next request
        validation.keep();
        // redisplay the register page
        showRegisterForm();
    }

    /**
     * Logout and return to home page.
     */
    public static void logout() {
        // clear session cookie
        session.clear();
        Home.defaultFilters();
    }

    /**
     * Log the user in by adding them to the session.
     *
     * @param id the unique ID of the user to login
     */
    private static void logUserIn(Long id) {
        session.put(Constants.sessionKey, id);
        // redirect to home page
        Home.defaultFilters();
    }
}