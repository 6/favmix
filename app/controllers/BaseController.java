/*
 * File: BaseController.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

import java.util.List;
import models.TopicModel;
import models.UpdateModel;
import models.UserModel;
import models.UserTopicModel;
import models.VoteModel;
import play.i18n.Messages;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import utilities.AllowGuest;
import utilities.DenyUser;
import utilities.Constants;

/**
 * Base class for controllers which has helper methods for all controllers.
 *
 * Some parts based off of the secure module:
 * https://github.com/playframework/play/tree/master/modules/secure
 *
 * @author Peter Graham
 * @author Guillaume Bort
 */
public class BaseController extends Controller {

    /** the User model */
    private static UserModel userModel;

    /** the Topic model */
    private static TopicModel topicModel;

    /** the UserTopic model */
    private static UserTopicModel userTopicModel;

    /** the Update model */
    private static UpdateModel updateModel;

    /** the Vote model */
    private static VoteModel voteModel;

    /** the currently logged in user */
    private static UserModel loggedInUser;

    /**
     * Check whether or not the session corresponds to someone who is logged in.
     *
     * @return true if logged in, false if not logged in
     */
    public static boolean isLoggedIn() {
        return session.get(Constants.SESSION_KEY) != null;
    }

    /**
     * Gets the currently logged in user.
     *
     * @return the currently logged in user
     */
    public static UserModel getUser(){
        return loggedInUser;
    }

    /**
     * Gets the topic model object.
     *
     * @return the topic model object
     */
    public static TopicModel getTopicModel(){
        return topicModel;
    }

    /**
     * Gets the user model object.
     *
     * @return the user model object
     */
    public static UserModel getUserModel(){
        return userModel;
    }

    /**
     * Gets the UserTopic model object.
     *
     * @return the UserTopic model object
     */
    public static UserTopicModel getUserTopicModel(){
        return userTopicModel;
    }

    /**
     * Gets the Update model object.
     *
     * @return the Update model object
     */
    public static UpdateModel getUpdateModel(){
        return updateModel;
    }

    /**
     * Gets the Vote model object.
     *
     * @return the Vote model object
     */
    public static VoteModel getVoteModel(){
        return voteModel;
    }

    /**
     * Returns the current URL the visitor is requesting if the method is GET,
     * or otherwise returns homepage.
     * 
     * @return String of current URL the visitor is requesting or homepage URL
     */
    public static String getCurrentUrl() {
        String url = "/";
        if ("GET".equals(request.method)) {
            url = request.url;
        }
        return url;
    }

    /**
     * Returns the original URL that has been saved through requests using the
     * flash param cookie.
     * 
     * @return a String of the original URL, or homepage URL if no original URL
     */
    public static String getOriginalUrl() {
        String url = flash.get(Constants.ORIGINAL_URL);
        if(url == null) {
            url = "/";
        }
        return url;
    }

    /**
     * Dispatcher method used to initialize fields and do user authentication
     * checks before each request.
     */
    @Before
    private static void dispatcher(){
        initModels();
        initLoginArgs();
        if(isLoggedIn()) {
            checkUserAccess();
            initLoggedInUser();
            initTopicsArgs();
            initUserInformationArgs();
        }
        else {
            checkRememberMe();
            checkGuestAccess();
        }
    }

    /**
     * Check whether or not a logged in user is allowed to access a controller
     * action method. If they are not allowed, redirect to homepage.
     */
    private static void checkUserAccess() {
        // allow guests if specified
        DenyUser actions = getControllerInheritedAnnotation(DenyUser.class);
        if(actions != null) {
            for(String action : actions.value()) {
                if(action.equals(request.actionMethod)) {
                    // users are denied for this requested method
                    Home.defaultFilters();
                }
            }
        }
    }

    /**
     * Check whether or not a guest user (not logged in) is allowed to access a
     * controller action method. If they are not, redirect to login form.
     */
    private static void checkGuestAccess() {
        // allow guests if specified
        AllowGuest actions = getControllerInheritedAnnotation(AllowGuest.class);
        if(actions != null) {
            for(String action : actions.value()) {
                if(action.equals(request.actionMethod)) {
                    // guests are allowed for this requested method
                    return;
                }
            }
        }
        // visitor needs to be logged in order to access this
        flash.put(Constants.ORIGINAL_URL, getCurrentUrl());
        flash.error(Messages.get("login.loginRequired"));
        Account.login();
    }

    /**
     * Checks the validity of the "Remember me" cookie. If valid, login user and
     * refresh the page.
     */
    private static void checkRememberMe() {
        Http.Cookie remember = request.cookies.get(Constants.REMEMBER_ME);
        if(remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0,
                    remember.value.indexOf("-"));
            String email = remember.value.substring(remember.value.indexOf("-")
                    + 1);
            if(Crypto.sign(email).equals(sign)) {
                // start login session and refresh the current page
                session.put(Constants.SESSION_KEY, email);
                redirect(getCurrentUrl());
            }
        }
    }

    /**
     * Initialize model objects used in various controllers before each request.
     */
    private static void initModels() {
        userModel = new UserModel();
        topicModel = new TopicModel();
        userTopicModel = new UserTopicModel();
        updateModel = new UpdateModel();
        voteModel = new VoteModel();
    }

    /**
     * Initialize logged in user field, so we can reuse this in various methods.
     */
    private static void initLoggedInUser() {
        String email = session.get(Constants.SESSION_KEY);
        if(email != null){
            loggedInUser = userModel.findByEmail(email);
        }
    }

    /**
     * Check if visitor is logged in, so we can render view accordingly.
     */
    private static void initLoginArgs(){
        if(isLoggedIn()) {
            renderArgs.put("loggedin", true);
        }
        else{
            renderArgs.put("loggedin", false);
        }
    }

    /**
     * Initialize topic arguments so we can render view accordingly.
     */
    private static void initTopicsArgs() {
        UserModel curUser = getUser();
        // add list of topics that user follows
        List<TopicModel> topics = getUserTopicModel().getTopicsByUser(curUser);
        renderArgs.put("topics", false);
        if(topics != null) {
            renderArgs.put("topics", topics);
        }
    }

    /**
     * Initialize user information arguments so we can render view accordingly.
     */
    private static void initUserInformationArgs(){
        // add the user's unique ID and name
        renderArgs.put("userId", getUser().getId());
        renderArgs.put("userName", getUser().getName());
    }
}