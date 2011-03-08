/*
 * File: TopicHandler.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package controllers;

import java.util.Date;
import java.util.List;
import models.Topic;
import models.Update;
import models.UserTopic;
import models.Vote;
import play.data.validation.Required;
import play.i18n.Messages;

/**
 * Controller for handling of viewing and adding updates to topics.
 *
 * @author Peter Graham
 * @author Bauke Scholtz
 */
public class TopicHandler extends BaseController{

    /**
     * View a topic and its updates.
     *
     * @param topicName the unique name of the topic to view
     * @param sortBy how to sort the topic updates (popular/recent) -->TODO enum
     */
    public static void viewUpdates(String topicName, String sortBy) {
        // get the topic associated with this name
        Topic topic = getTopicModel().findByName(topicName);
        if(topic == null) {
            ErrorHandler.showError(404);
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
        List<Update> updates;
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
        renderTemplate("TopicHandler/index.html");
    }

    /**
     * Show the form for creating a new topic.
     */
    public static void showNewTopicForm(){
        if(!isLoggedIn()) {
            flash.error(Messages.get("login.loginRequired"));
            Account.showLoginForm();
        }
        renderTemplate("TopicHandler/new.html");
    }

    /**
     * Validate and add an update to a topic.
     *
     * @param topicName the name of the topic to add an update to
     * @param content String representing the update to add
     */
    public static void addUpdate(@Required String topicName,
            @Required String content) {
        if(!isLoggedIn()) {
            flash.error(Messages.get("login.loginRequired"));
            Account.showLoginForm();
        }
        if(!validation.hasErrors()) {
            if(getTopicModel().topicExists(topicName)) {
                Update update = new Update(getUser(), 
                        getTopicModel().findByName(topicName), content);
                update.insert();
                flash.success(Messages.get("topic.updateAdded"));
            }
        }
        else {
            flash.error(Messages.get("form.emptyField"));
        }
        viewUpdates(topicName, "recent");
    }

    /**
     * Validate and create a new topic.
     *
     * @param name String representing the topic name
     */
    public static void createNewTopic(@Required String name) {
        if(!isLoggedIn()) {
            flash.error(Messages.get("login.loginRequired"));
            Account.showLoginForm();
        }
        if(!validation.hasErrors()) {
            // name is valid so check if available
            if(getTopicModel().topicExists(name)) {
                flash.error(Messages.get("topic.exists"));
            }
            else{
                // insert the new topic
                Topic newTopic = new Topic(name);
                newTopic.insert();

                // make user follow that topic
                UserTopic userTopic = new UserTopic(getUser().getId(),
                        newTopic.getId());
                userTopic.insert();
                
                flash.success(Messages.get("action.saved"));
                viewUpdates(name, "recent");
            }
        }
        else {
            flash.error(Messages.get("form.emptyField"));
        }
        showNewTopicForm();
    }

    /**
     * Vote up the Update with the given updateId.
     *
     * @param updateId the ID of the Update to vote up
     * @param voteType the type of vote to cast (up or down) --> TODO: enum
     * @param sortBy how to sort votes by upon redirection --> TODO: enum
     */
    public static void vote(Long updateId, String voteType, String sortBy) {
        if(!isLoggedIn()) {
            flash.error(Messages.get("login.loginRequired"));
            Account.showLoginForm();
        }
        boolean isUpVote = false;
        if(voteType.equals("up")) {
            isUpVote = true;
        }
        Update toVoteOn = getUpdateModel().findById(updateId);
        if(toVoteOn != null) {
            boolean insertVote = true;
            // check if user has already voted on this update
            if(getVoteModel().voteExists(getUser(), toVoteOn)) {
                Vote currentVote = getVoteModel().getByUserAndUpdate(getUser(),
                        toVoteOn);
                if(currentVote.isUpVote() == isUpVote) {
                    // disable if voting in the same direction
                    insertVote = false;
                }
                else {
                    // delete current vote, as user is changing to opposite vote
                    currentVote.delete();
                }
            }
            if(insertVote) {
                Vote vote = new Vote(getUser(), toVoteOn, isUpVote);
                vote.insert();
            }
            
            // redirect back to the topic
            Topic parentTopic = getTopicModel()
                    .findById(toVoteOn.getParentTopicId());
            viewUpdates(parentTopic.getName(), sortBy);
        }
    }
}