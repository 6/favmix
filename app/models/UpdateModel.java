/*
 * File: UpdateModel.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 18
 */
package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.i18n.Messages;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;
import utilities.Constants;
import utilities.ValidationException;
import utilities.Validator;
import utilities.SimpleHTMLParser;

/**
 * Model for accessing and modifying topic/user updates.
 *
 * @author Peter Graham
 * @author Bauke Scholtz
 */
public class UpdateModel extends BaseModel{

    /** auto-incremented unique ID for the update */
    @Id
    private Long id;

    /** the update content */
    private String content;

    /** the URL associated with this update (optional) */
    private String url;

    /** user ID associated with this Update */
    @Index("user_idx")
    private Long userId;

    /** topic ID associated with this Update */
    @Index("topic_idx")
    private Long topicId;

    /** Date update is added to database */
    private Date created;

    /**
     * Constructs an update object.
     */
    public UpdateModel() {
        super();
    }

    /**
     * Constructs the update and initializes fields.
     *
     * @param user the User who posted this update
     * @param topic the Topic this update was posted to
     * @param updateContent the content of the update
     * @param updateUrl the URL associated with the update
     */
    public UpdateModel(UserModel user, TopicModel topic, String updateContent,
            String updateUrl) {
        this();
        this.userId = user.getId();
        this.topicId = topic.getId();
        this.content = updateContent;
        this.url = updateUrl;
        this.created = new Date();
    }

    /**
     * Create a new update for the given topic.
     *
     * @param content String of the content of update
     * @param url String of the URL associated with the update (optional)
     * @param topicName String of the name of the topic to post to
     * @param  creator the user who creates this
     * @throws ValidationException if content is empty
     */
    public void createUpdate(String content, String url, String topicName,
            UserModel creator) throws ValidationException {
        if(Validator.isEmpty(content)) {
            throw new ValidationException(Messages.get("form.emptyField"));
        }
        if(!Validator.isEmpty(url) && !Validator.isUrl(url)) {
            throw new ValidationException(Messages.get("form.badUrl"));
        }
        else if(Validator.isEmpty(url)) {
            // "" gets converted to null
            url = null;
        }
        String entityString = SimpleHTMLParser.convertToEntities(content);
        String finalContent = SimpleHTMLParser.closeAllTags(entityString);
        TopicModel topicModel = new TopicModel();
        if(topicModel.topicExists(topicName)) {
            UpdateModel update = new UpdateModel(creator,
                    topicModel.findByName(topicName), finalContent, url);
            update.insert();
            // insert a vote automatically for user--seems like a good default
            VoteModel voteModel = new VoteModel();
            voteModel.createVote(update, creator);
        }
    }

    /**
     * Remove an update with the given ID and creator if such an update exists.
     * Also remove all votes associated with the given update.
     *
     * @param updateId the ID of the update to remove
     * @param creator the user who created the update
     */
    public void removeUpdate(Long updateId, UserModel creator) {
        UpdateModel update = this.findById(updateId);
        if(update != null && update.getUserId() == creator.getId()) {
            VoteModel voteModel = new VoteModel();
            voteModel.deleteByUpdate(update);
            update.delete();
        }
    }

    /**
     * Find a update associated with the given unique topic ID.
     *
     * @param updateId the unique ID of the topic
     * @return update associated with the ID, or null if no update with that ID
     *      exists
     */
    public UpdateModel findById(Long updateId) {
        return all().filter("id", updateId).get();
    }

    /**
     * Return a list of the newest updates associated with the given topic.
     *
     * @param topicIds List of topic IDs to find newest updates of
     * @param howMany how many updates to return
     * @param offset the offset used for pagination
     * @return the recent updates associated with the given topic IDs
     */
    public List<UpdateModel> findNewestByTopics(List<Long> topicIds,
            int howMany, int offset) {
        if(topicIds.isEmpty()) {
            return new ArrayList<UpdateModel>();
        }
        return all().filter("topicId IN", topicIds).order("-created")
                .fetch(howMany, offset);
    }

    /**
     * Fetches the most recent updates overall.
     *
     * @param howMany how many updates to return
     * @param offset the offset used for pagination
     * @return List if the most recent updates
     */
    public List<UpdateModel> findNewest(int howMany, int offset) {
        return all().order("-created").fetch(howMany, offset);
    }

    /**
     * Fetches the most recent updates among a given user's topics.
     *
     * @param user the user who's topics to include
     * @param howMany how many updates to return
     * @param offset the offset used for pagination
     * @return List of the most recent updates in this user's topics
     */
    public List<UpdateModel> findNewestByUser(UserModel user, int howMany,
            int offset) {
        UserTopicModel userTopicModel = new UserTopicModel();
        List<Long> userTopicIds = userTopicModel.getTopicIdsByUser(user);
        return this.findNewestByTopics(userTopicIds, howMany, offset);
    }

    /**
     * Fetches all the updates posted after a given date.
     *
     * @param afterDate the date after which to include updates from
     * @return List of the updates posted after a given date
     */
    public List<UpdateModel> findAfter(Date afterDate) {
        return all().filter("created>", afterDate).fetch();
    }

    /**
     * Fetches all the updates posted after a given date and in the given topic
     * IDs.
     *
     * @param afterDate the date after which to include updates from
     * @param topicIds the topic IDs to return updates from
     * @return List of the updates posted after date and in topic IDs.
     */
    public List<UpdateModel> findAfterByTopics(Date afterDate,
            List<Long> topicIds){
        if(topicIds.isEmpty()){
            return new ArrayList<UpdateModel>();
        }
        return all().filter("topicId IN", topicIds)
                .filter("created>", afterDate).fetch();
    }

    /**
     * Fetches the most popular updates among a given user's topics.
     *
     * @param user the user who's topics to include
     * @param afterDate the date after which to include updates from
     * @param howMany how many updates to return
     * @param offset the offset used for pagination
     * @return List of the most recent updates in this user's topics
     */
    public List<UpdateModel> findPopularByUser(UserModel user, Date afterDate,
            int howMany, int offset) {
        UserTopicModel userTopicModel = new UserTopicModel();
        List<Long> userTopicIds = userTopicModel.getTopicIdsByUser(user);
        return this.findPopular(userTopicIds, afterDate, howMany, offset);
    }

    /**
     * Get updates for the given user, order and offset.
     *
     * Note: Date code adapted from code by Bauke Scholtz:
     * http://stackoverflow.com/questions/4348525/get-date-as-of-4-hours-ago
     *
     * @param topic the topic to get updates of. If null, ignore.
     * @param user the user to get updates of. If null, ignore.
     * @param order how to order updates
     * @param offset the offset used for pagination
     * @return List of updates with the given order and offset
     */
    public List<UpdateModel> getUpdates(UserModel user, TopicModel topic,
            String order, int offset) {
        List<Long> topicIds = new ArrayList<Long>();
        if(topic != null) {
            topicIds.add(topic.getId());
        }
        if("recent".equals(order)) {
            if(topic != null) {
                return this.findNewestByTopics(topicIds,
                        Constants.UPDATES_PER_PAGE, offset);
            }
            else if(user != null) {
                return this.findNewestByUser(user, Constants.UPDATES_PER_PAGE,
                        offset);
            }
            else {
                return this.findNewest(Constants.UPDATES_PER_PAGE, offset);
            }
        }
        // order by popularity
        Date date;
        if("popular24h".equals(order)){
            date = new Date(System.currentTimeMillis() - (24*60*60*1000));
        }
        else {
            date = new Date(System.currentTimeMillis() - (7*24*60*60*1000));
        }
        if(topic != null) {
            return this.findPopular(topicIds, date, Constants.UPDATES_PER_PAGE,
                    offset);
        }
        else if(user != null) {
            return this.findPopularByUser(user, date,Constants.UPDATES_PER_PAGE,
                    offset);
        }
        else {
            return this.findPopular(null, date, Constants.UPDATES_PER_PAGE,
                    offset);
        }
    }

    /**
     * Return a list of the popular updates with given filters applied.
     *
     * @param topicIds List of the IDs of the topic to find popular updates of.
     *      If this list is null, find popular updates among all topics.
     * @param afterDate the date after which to include topics.
     * @param howMany how many updates to return
     * @param offset the offset used for pagination
     * @return the popular updates associated with the given topic IDs
     */
    public List<UpdateModel> findPopular(List<Long> topicIds, Date afterDate,
            int howMany, int offset) {
        List<UpdateModel> updates;
        if(topicIds != null) {
            updates = this.findAfterByTopics(afterDate, topicIds);
        }
        else {
            updates = this.findAfter(afterDate);
        }
        Map<UpdateModel,Integer> updatesByVotes =
                new HashMap<UpdateModel,Integer>();
        // count vote value for each update
        for(UpdateModel update: updates) {
            updatesByVotes.put(update, update.getVoteCount());
        }
        updatesByVotes = this.sortByValue(updatesByVotes);
        // convert Map keys to List (we don't need the values anymore)
        List orderedUpdates =
                new ArrayList<UpdateModel>(updatesByVotes.keySet());
        Collections.reverse(orderedUpdates);
        
        // return the specified number of updates with offset unless it exceeds
        // the total number of possible updates to return
        int finalIndex = offset + howMany;
        if(finalIndex > orderedUpdates.size()) {
            finalIndex = orderedUpdates.size();
        }
        return orderedUpdates.subList(offset, finalIndex);
    }

    /**
     * Convenience method for getting the vote count of this update.
     *
     * @return the vote count of this update
     */
    public int getVoteCount() {
        VoteModel voteModel = new VoteModel();
        return voteModel.getVoteCount(this);
    }

    /**
     * Returns the parent topic name.
     *
     * @return a string of the parent topic
     */
    public String getParentTopicName() {
        TopicModel topicModel = new TopicModel();
        TopicModel parentTopic = topicModel.findById(this.getParentTopicId());
        return parentTopic.getName();
    }

    /**
     * Checks whether or not this update has a URL associated with it.
     *
     * @return true if it has a URL, otherwise false
     */
    public boolean hasUrl() {
        return this.getUrl() != null;
    }

    /**
     * Convenience method for seeing if a given user has voted on this update.
     *
     * @param user the user to check if they voted on this update
     * @return true if user voted on this update, otherwise false
     */
    public boolean votedOnBy(UserModel user) {
        if(user == null) {
            return false;
        }
        VoteModel voteModel = new VoteModel();
        return voteModel.voteExists(user, this);
    }

    /**
     * Convenience method for seeing how long ago an update was posted.
     *
     * @return how long ago this update was posted
     */
    public String getHowLongAgo() {
        return this.getHowLongAgo(this.getDateCreated());
    }

    /**
     * Return the update content.
     *
     * @return a string of the update content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Returns the URL associated with update.
     * 
     * @return the URL associated with update. Null if no URL specified.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Returns unique ID of the update.
     *
     * @return the unique id of the update
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the parent topic ID.
     * 
     * @return the parent topic ID
     */
    public Long getParentTopicId() {
        return this.topicId;
    }

    /**
     * Returns the date that this update was added to the database.
     *
     * @return the date that this update was added to the database
     */
    public Date getDateCreated() {
        return this.created;
    }

    /**
     * Returns the ID of the user that posted this update.
     *
     * @return the ID of the user that posted this update. 
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Returns the name of the user that posted this update.
     * 
     * @return the name of the user that posted this update.
     */
    public String getPosterName() {
        UserModel userModel = new UserModel();
        UserModel poster = userModel.findById(this.getUserId());
        return poster.getName();
    }


    /**
     * Returns string representation of the update.
     *
     * @return a string representation of the update
     */
    @Override
    public String toString() {
        return getContent();
    }

    /**
     * Returns a query object representing all updates.
     *
     * @return a query object representing all updates
     */
    private Query<UpdateModel> all() {
        return Model.all(UpdateModel.class);
    }
}