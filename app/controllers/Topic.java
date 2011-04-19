/*
 * File: Topic.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

import java.util.List;
import models.TopicModel;
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
@AllowGuest({"defaultFilters", "showUpdates"})
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
        List<UpdateModel> updates;
        if("you".equals(scope)) {
            if(!isLoggedIn()) {
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
            updates = getUpdateModel().getUpdates(null, topic, order, 0);
            renderArgs.put("isFollowing",
                getUserTopicModel().isFollowing(getUser(), topic));
        }
        renderArgs.put("updates", updates);
        renderArgs.put("scope",scope);
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
     * @param name String representing the topic name
     */
    public static void onCreateSubmit(String name) {
        try {
            getTopicModel().createTopic(name, getUser());
            flash.success(Messages.get("action.saved")); // TODO better message
            showUpdates(name, "recent", 0);
        }
        catch(ValidationException e) {
            flash.error(e.getMessage());
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
        }
        catch(ValidationException e) {
            flash.error(e.getMessage());
            params.flash();
        }
        showUpdates(topicName, "recent", 0);
    }

    /**
     * Vote up the Update with the given updateId. This acts as the fallback
     * for when Javascript/AJAX doesn't work or is disabled.
     *
     * @param updateId the ID of the Update to vote up
     * @param scope the scope to redirect back to
     * @param order how to sort votes by upon redirection
     * @param offset what offset to go to upon redirection
     */
    public static void onVoteSubmit(Long updateId, String scope, String order,
            int offset) {
        UpdateModel update = getUpdateModel().findById(updateId);
        getVoteModel().createVote(update, getUser());
        // redirect back to the original page
        showUpdates(scope, order, offset);
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
}