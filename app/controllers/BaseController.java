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
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.Crypto;
import play.mvc.After;
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
@AllowGuest({"toggleMobileEnabled","changeLanguage"})
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
     * Checks if mobile site is enabled.
     *
     * @return true if mobile site is enabled, otherwise false
     */
    public static boolean isMobileEnabled() {
        return "enabled".equals(session.get(Constants.MOBILE_KEY));
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
    public static String getPreviousUrl() {
        return getUrlFromSession(Constants.PREVIOUS_URL);
    }

    /**
     * Returns the original URL that has been saved through one or more requests
     * using the flash param cookie.
     * 
     * @return a String of the original URL, or homepage URL if no original URL
     */
    public static String getOriginalUrl() {
        return getUrlFromSession(Constants.ORIGINAL_URL);
    }

    /**
     * Toggle whether or not mobile is enabled, and redirect to homepage.
     */
    public static void toggleMobileEnabled() {
        if(isMobileEnabled()) {
            session.put(Constants.MOBILE_KEY,"disabled");
        }
        else {
            session.put(Constants.MOBILE_KEY,"enabled");
        }
        Topic.defaultFilters();
    }

    /**
     * Changes interface language to the given language.
     *
     * @param languageCode the ISO language code
     */
    public static void changeLanguage(String languageCode) {
        if(Constants.VALID_LANGUAGE_CODES.contains(languageCode)) {
            Lang.change(languageCode);
        }
        redirect(getPreviousUrl());
    }

    /**
     * Dispatcher method used to initialize fields and do user authentication
     * checks before each request.
     */
    @Before
    private static void dispatcher() {
        initModels();
        initLoginArgs();
        initLoggedInUser();
        initMobile();
        initJavaScriptErrorArgs();
        if(isLoggedIn()) {
            checkUserAccess();
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
                    Topic.defaultFilters();
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
                UserModel user = getUserModel().findByEmail(email);
                session.put(Constants.SESSION_KEY, user.getId());
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
     * Initializes the mobile session cookie for tracking whether or not the
     * mobile site is enabled.
     */
    private static void initMobile() {
        if(session.get(Constants.MOBILE_KEY) == null) {
            // check user agent for mobile match
            String userAgent = request.headers.get("user-agent").value();
            for(String mobileAgent : Constants.MOBILE_USER_AGENTS) {
                if(userAgent.contains(mobileAgent)){
                    session.put(Constants.MOBILE_KEY,"enabled");
                    break;
                }
            }
            if(!isMobileEnabled()) {
                session.put(Constants.MOBILE_KEY,"disabled");
            }
        }
        renderArgs.put("isMobile", isMobileEnabled());
    }

    /**
     * Initialize logged in user field, so we can reuse this in various methods.
     */
    private static void initLoggedInUser() {
        if(isLoggedIn()) {
            Long userId = Long.valueOf(session.get(Constants.SESSION_KEY));
            loggedInUser = userModel.findById(userId);
        }
        else {
            loggedInUser = null;
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
        // add list of topics that user follows
        List<TopicModel> topics = getUserTopicModel().getTopicsByUser(
                getUser());
        renderArgs.put("topics", false);
        if(topics != null) {
            renderArgs.put("topics", topics);
        }
    }

    /**
     * Initialize user information arguments so we can render view accordingly.
     */
    private static void initUserInformationArgs(){
        renderArgs.put("user", getUser());
    }

    /**
     * Initialize internationalized error messages for JavaScript.
     */
    private static void initJavaScriptErrorArgs() {
        renderArgs.put("emptyError", Messages.get("form.emptyField"));
        renderArgs.put("loginRequired", Messages.get("login.loginRequired"));
    }

    /**
     * Returns the URL that has been saved through requests using the flash
     * param cookie.
     *
     * @param sessionKey the session key for the flash param
     * @return a String of the original URL, or homepage URL if no original URL
     */
    private static String getUrlFromSession(String sessionKey) {
        String url = flash.get(sessionKey);
        if(url == null) {
            url = "/";
        }
        return url;
    }

    /**
     * Store the current URL in case we need it for the next request.
     */
    @After
    private static void storeCurrentUrl() {
        flash.put(Constants.PREVIOUS_URL, getCurrentUrl());
    }
}