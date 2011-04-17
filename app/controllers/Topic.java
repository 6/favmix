/*
 * File: Topic.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 16
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
     * Validate and create a new topic.
     *
     * @param name String representing the topic name
     */
    public static void onCreateSubmit(@Required String name) {
        if(!validation.hasErrors()) {
            // name is valid so check if available
            if(getTopicModel().topicExists(name)) {
                flash.error(Messages.get("topic.exists"));
            }
            else{
                // insert the new topic
                TopicModel newTopic = new TopicModel(name);
                newTopic.insert();

                // make user follow that topic
                UserTopicModel userTopic = new UserTopicModel(getUser().getId(),
                        newTopic.getId());
                userTopic.insert();

                flash.success(Messages.get("action.saved"));
                index(name, "recent");
            }
        }
        else {
            flash.error(Messages.get("form.emptyField"));
        }
        create();
    }

    /**
     * Validate and add an update to a topic.
     *
     * @param topicName the name of the topic to add an update to
     * @param content String representing the update to add
     */
    public static void addUpdate(@Required String topicName,
            @Required String content) {
        if(!validation.hasErrors()) {
            if(getTopicModel().topicExists(topicName)) {
                UpdateModel update = new UpdateModel(getUser(),
                        getTopicModel().findByName(topicName), content);
                update.insert();
                flash.success(Messages.get("topic.updateAdded"));
            }
        }
        else {
            flash.error(Messages.get("form.emptyField"));
        }
        index(topicName, "recent");
    }

    /**
     * Vote up the Update with the given updateId.
     * TODO: only "up"vote?
     *
     * @param updateId the ID of the Update to vote up
     * @param sortBy how to sort votes by upon redirection --> TODO: enum
     */
    public static void vote(Long updateId, String sortBy) {
        UpdateModel toVoteOn = getUpdateModel().findById(updateId);
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
        }
    }
}