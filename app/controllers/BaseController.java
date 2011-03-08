/*
 * File: BaseController.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package controllers;

import java.util.List;
import models.Topic;
import models.Update;
import models.User;
import models.UserTopic;
import models.Vote;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import utilities.Constants;

/**
 * Base class for controllers which sets view render arguments for all requests.
 *
 * @author Peter Graham
 */
public class BaseController extends Controller {

    /** the User model */
    private static User userModel;

    /** the Topic model */
    private static Topic topicModel;

    /** the UserTopic model */
    private static UserTopic userTopicModel;

    /** the Update model */
    private static Update updateModel;

    /** the Vote model */
    private static Vote voteModel;

    /** the currently logged in user */
    private static User loggedInUser;

    /**
     * Check whether or not the session corresponds to someone who is logged in.
     *
     * @return true if logged in, false if not logged in
     */
    public static boolean isLoggedIn() {
        String id = session.get(Constants.sessionKey);
        return id != null;
    }

    /**
     * Gets the currently logged in user.
     *
     * @return the currently logged in user
     */
    public static User getUser(){
        return loggedInUser;
    }

    /**
     * Gets the topic model object.
     *
     * @return the topic model object
     */
    public static Topic getTopicModel(){
        return topicModel;
    }

    /**
     * Gets the user model object.
     *
     * @return the user model object
     */
    public static User getUserModel(){
        return userModel;
    }

    /**
     * Gets the UserTopic model object.
     *
     * @return the UserTopic model object
     */
    public static UserTopic getUserTopicModel(){
        return userTopicModel;
    }

    /**
     * Gets the Update model object.
     *
     * @return the Update model object
     */
    public static Update getUpdateModel(){
        return updateModel;
    }

    /**
     * Gets the Vote model object.
     *
     * @return the Vote model object
     */
    public static Vote getVoteModel(){
        return voteModel;
    }

    /**
     * Get the name of the given user.
     * 
     * @param user User to get the name of
     * @return the user's name, or the default name if no name is specified
     */
    public static String getUserName(User user) {
        String name = null;
        if(user != null) {
            name = user.getName();
        }
        if(name == null) {
            // give them the default name
            name = Messages.get("msg.defaultUserName");
        }
        return name;
    }

    /**
     * Initialize various fields before each request.
     */
    @Before
    private static void init(){
        initModels();
        initLoggedInUser();

        // initialize render arguments
        initLoginArgs();
        initTopicsArgs();
        initUserInformationArgs();
    }

    /**
     * Initialize model objects used in various controllers before each request.
     */
    private static void initModels() {
        userModel = new User();
        topicModel = new Topic();
        userTopicModel = new UserTopic();
        updateModel = new Update();
        voteModel = new Vote();
    }

    /**
     * Initialize logged in user field, so we can reuse this in various methods.
     */
    private static void initLoggedInUser() {
        if(isLoggedIn()) {
            Long userId = Long.valueOf(session.get(Constants.sessionKey));
            if(userId != null){
                loggedInUser = userModel.findById(userId);
            }
        }
    }

    /**
     * Check if visitor is logged in, so we can render view accordingly.
     */
    private static void initLoginArgs(){
        if(isLoggedIn()) {
            renderArgs.put("loggedin",true);
        }
        else{
            renderArgs.put("loggedin",false);
        }
    }

    /**
     * Initialize topic arguments so we can render view accordingly.
     */
    private static void initTopicsArgs() {
        if(isLoggedIn()) {
            User curUser = getUser();
            // add list of topics that user follows
            List<Topic> topics = getUserTopicModel().getTopicsByUser(curUser);
            renderArgs.put("topics", false);
            if(topics != null) {
                renderArgs.put("topics", topics);
            }
        }
    }

    /**
     * Initialize user information arguments so we can render view accordingly.
     */
    private static void initUserInformationArgs(){
        if(isLoggedIn()) {
            // add the user's unique ID and name
            renderArgs.put("userId", getUser().getId());
            String userName = getUser().getName();
            if(userName != null){
                renderArgs.put("userName", userName);
            }
            else {
                // get default user name if none specified
                renderArgs.put("userName", Messages.get("msg.defaultUserName"));
            }
        }
    }
}