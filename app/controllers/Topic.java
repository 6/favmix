/*
 * File: Topic.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import models.TopicModel;
import play.libs.WS;
import models.UpdateModel;
import play.i18n.Messages;
import utilities.AllowGuest;
import utilities.Constants;
import utilities.ValidationException;

/**
 * Controller for handling viewing and adding updates to topics.
 *
 * @author Peter Graham
 */
@AllowGuest({"defaultFilters", "browse", "showUpdates"})
public class Topic extends BaseController {

    /**
     * Render the home page with default filters. Default order is popular, and
     * default scope is everyone if not logged in or you if logged in.
     */
    public static void defaultFilters() {
        if(isLoggedIn()) {
            showUpdates("you", Constants.DEFAULT_ORDER, 0);
        }
        else {
            showUpdates("everyone", Constants.DEFAULT_ORDER, 0);
        }
    }

    /**
     * Display updates for the given scope, order, and offset.
     * 
     * @param scope either "you" for all your topics, "everyone" for everyone's
     *      topics, or otherwise a String representing the name of a topic.
     * @param order how to sort the updates (recent, popular)
     * @param offset int used to determine how much to offset updates by
     */
    public static void showUpdates(String scope, String order, int offset) {
        if(!Constants.VALID_ORDERS.contains(order)) {
            order = Constants.DEFAULT_ORDER;
        }
        // get the list of updates to show
        List<UpdateModel> updates;
        if("you".equals(scope)) {
            if(!isLoggedIn()) {
                // can't access "you" if not logged in
                flash.put(Constants.ORIGINAL_URL, getCurrentUrl());
                flash.error(Messages.get("login.loginRequired"));
                Account.login();
            }
            updates = getUpdateModel().getUpdates(getUser(),null,order,offset);
        }
        else if("everyone".equals(scope)) {
            updates = getUpdateModel().getUpdates(null, null, order, offset);
        }
        else {
            // the scope is the name of a topic
            TopicModel topic = getTopicModel().findByName(scope);
            if(topic == null) {
                Error.index(404, Messages.get("topic.notFound"));
            }
            updates = getUpdateModel().getUpdates(null, topic, order, offset);
            renderArgs.put("isFollowing",
                getUserTopicModel().isFollowing(getUser(), topic));
        }
        renderArgs.put("updates", updates);
        renderArgs.put("scope",scope);
        String decodedScope = null;
        try {
            decodedScope = URLDecoder.decode(scope, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            decodedScope = scope;
        }
        renderArgs.put("scopeDecoded", decodedScope);
        renderArgs.put("order",order);
        int lowerBound = offset+1;
        int upperBound = offset+Constants.UPDATES_PER_PAGE;
        renderArgs.put("lower", lowerBound);
        renderArgs.put("upper", upperBound);
        renderArgs.put("prevOffset", lowerBound -1- Constants.UPDATES_PER_PAGE);
        renderArgs.put("numUpdates",updates.size());
        renderArgs.put("defaultNumUpdates",Constants.UPDATES_PER_PAGE);
        render();
    }

    /**
     * Show the form for creating a new topic.
     */
    public static void create(){
        render();
    }

    /**
     * Create a new topic.
     *
     * @param topicName String representing the topic name
     */
    public static void onCreateSubmit(String topicName) {
        try {
            getTopicModel().createTopic(topicName, getUser());
            flash.success(Messages.get("topic.created"));
            // redirect to the newly created topic
            WS webService = new WS();
            showUpdates(webService.encode(topicName), "recent", 0);
        }
        catch(ValidationException e) {
            flash.error(e.getMessage());
            params.flash();
            create();
        }
    }

    /**
     * Validate and add an update to a topic.
     *
     * @param topicName the name of the topic to add an update to
     * @param content String representing the update to add
     * @param url String of the URL associated with this update (optional)
     * @param sortBy how to sort updates
     */
    public static void onUpdateSubmit(String topicName, String content,
            String url, String sortBy) {
        try {
            getUpdateModel().createUpdate(content, url, topicName, getUser());
            flash.success(Messages.get("topic.updateAdded"));
            // redirect to recent updates, so your update appears on the top
            showUpdates(topicName, "recent", 0);
        }
        catch(ValidationException e) {
            flash.error(e.getMessage());
            params.flash();
            redirect(getPreviousUrl());
        }
    }

    /**
     * Remove an update with the given update ID.
     *
     * @param updateId the ID of the update to remove.
     */
    public static void removeUpdate(Long updateId) {
        getUpdateModel().removeUpdate(updateId, getUser());
        redirect(getPreviousUrl());
    }

    /**
     * Vote up the Update with the given updateId.
     * 
     * @param updateId the ID of the Update to vote up
     */
    public static void onVoteSubmit(Long updateId) {
        UpdateModel update = getUpdateModel().findById(updateId);
        getVoteModel().createVote(update, getUser());
        // redirect back to the original page if not AJAX
        if(!params._contains("ajax")) {
            redirect(getPreviousUrl());
        }
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
        Topic.showUpdates(topicName, sortBy, 0);
    }

    /**
     * Removes a topic from the user's followed topics.
     *
     * @param topicName the name of the topic to remove
     */
    public static void unFollowTopic(String topicName) {
        getUser().unFollowTopic(getTopicModel().findByName(topicName));
        // go back to user profile
        Profile.index(getUser().getId());
    }

    /**
     * Browse topics by popularity or recency.
     *
     * @param order how to order topics (popular or recent)
     * @param offset int used to determine how much to offset topics by
     */
    public static void browse(String order, int offset) {
        List<TopicModel> topics;
        if("recent".equals(order)) {
            topics = getTopicModel().getNewest(Constants.TOPICS_PER_PAGE,
                    offset);
        }
        else {
            topics = getTopicModel().getPopular(Constants.TOPICS_PER_PAGE,
                    offset);
            order = "popular";
        }
        renderArgs.put("topics",topics);
        renderArgs.put("order", order);
        int lowerBound = offset+1;
        int upperBound = offset+Constants.TOPICS_PER_PAGE;
        renderArgs.put("lower", lowerBound);
        renderArgs.put("upper", upperBound);
        renderArgs.put("prevOffset", lowerBound -1- Constants.TOPICS_PER_PAGE);
        renderArgs.put("numUpdates",topics.size());
        renderArgs.put("defaultNumUpdates",Constants.TOPICS_PER_PAGE);
        render();
    }
}