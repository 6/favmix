/*
 * File: Topic.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

import models.TopicModel;
import models.UpdateModel;
import play.i18n.Messages;
import utilities.AllowGuest;
import utilities.ValidationException;

/**
 * Controller for handling of viewing and adding updates to topics.
 *
 * TODO clean up all methods --> model
 * @author Peter Graham
 * @author Bauke Scholtz
 */
@AllowGuest({"index"})
public class Topic extends BaseController {

    /**
     * View a topic and its updates.
     *
     * @param topicName the unique name of the topic to view
     * @param sortBy how to sort the topic updates (popular/recent) -->TODO enum
     */
    public static void index(String topicName, String sortBy) {
        TopicModel topic = getTopicModel().findByName(topicName);
        if(topic == null) {
            Error.index(404, Messages.get("topic.notFound"));
        }
        renderArgs.put("sortBy", sortBy);
        renderArgs.put("topicName", topicName);
        renderArgs.put("updates", getUpdateModel().findByTopic(topic, sortBy));
        renderArgs.put("following", 
                getUserTopicModel().isFollowing(getUser(), topic));
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
            index(name, "recent");
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
     */
    public static void onUpdateSubmit(String topicName, String content) {
        try {
            getUpdateModel().createUpdate(content, topicName, getUser());
            flash.success(Messages.get("topic.updateAdded"));
        }
        catch(ValidationException e) {
            flash.error(e.getMessage());
        }
        index(topicName, "recent");
    }

    /**
     * Vote up the Update with the given updateId. This acts as the fallback
     * for when Javascript/AJAX doesn't work or is disabled.
     *
     * @param updateId the ID of the Update to vote up
     * @param sortBy how to sort votes by upon redirection --> TODO: enum
     */
    public static void onVoteSubmit(Long updateId, String sortBy) {
        UpdateModel update = getUpdateModel().findById(updateId);
        getVoteModel().createVote(update, getUser());
        // redirect back to the topic
        TopicModel parentTopic = getTopicModel().findById(
                update.getParentTopicId());
        index(parentTopic.getName(), sortBy);
    }
}