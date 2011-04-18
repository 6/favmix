/*
 * File: Topic.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package controllers;

import java.util.Date;
import java.util.List;
import models.TopicModel;
import models.UpdateModel;
import models.UserTopicModel;
import models.VoteModel;
import play.data.validation.Required;
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
        // get the topic associated with this name
        TopicModel topic = getTopicModel().findByName(topicName);
        if(topic == null) {
            Error.index(404, Messages.get("topic.notFound"));
        }
        boolean isFollowing = false;
        if(isLoggedIn()){
            // see if user is following this topic
            isFollowing = getUserTopicModel().isUserFollowing(getUser(), topic);
        }

        boolean sortByRecent = true;
        if(sortBy != null && sortBy.equals("popular")) {
            sortByRecent = false;
        }
        List<UpdateModel> updates;
        if(sortByRecent){
            updates = getUpdateModel().findNewestByTopic(topic);
            renderArgs.put("sortBy", "recent");
        }
        else {
            // source:
            // http://stackoverflow.com/questions/4348525/get-date-as-of-4-hours-ago
            Date day = new Date(System.currentTimeMillis() - (24*60*60*1000));
            // TODO: different date ranges
            updates = getUpdateModel().findPopularByTopic(topic, day);
            renderArgs.put("sortBy", "popular");
        }
        
        renderArgs.put("topicName", topicName);
        renderArgs.put("updates",updates);
        renderArgs.put("following",isFollowing);
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
     * TODO: only "up"vote?
     *
     * @param updateId the ID of the Update to vote up
     * @param sortBy how to sort votes by upon redirection --> TODO: enum
     */
    public static void onVoteSubmit(Long updateId, String sortBy) {
        getVoteModel().createVote(updateId, getUser());
        // redirect back to the topic TODO using flash keep
        /*TopicModel parentTopic = getTopicModel()
                .findById(toVoteOn.getParentTopicId());
        index(parentTopic.getName(), sortBy);*/

        /*UpdateModel toVoteOn = getUpdateModel().findById(updateId);
        if(toVoteOn != null) {
            // check if user has already voted on this update
            if(getVoteModel().voteExists(getUser(), toVoteOn)) {                
                // delete current vote, as user is un-voting
                getVoteModel().getByUserAndUpdate(getUser(), toVoteOn).delete();
            }
            else {
                // cast the vote
                VoteModel vote = new VoteModel(getUser(), toVoteOn);
                vote.insert();
            }
            
            // redirect back to the topic
            TopicModel parentTopic = getTopicModel()
                    .findById(toVoteOn.getParentTopicId());
            index(parentTopic.getName(), sortBy);
        }*/
    }
}