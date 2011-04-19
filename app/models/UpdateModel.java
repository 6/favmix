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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

/**
 * Model for accessing and modifying topic/user updates.
 *
 * @author Peter Graham
 * @author Bauke Scholtz
 */
public class UpdateModel extends Model{

    /** auto-incremented unique ID for the update */
    @Id
    private Long id;

    /** the update content */
    private String content;

    /** the URL associated with this update (optional) */
    private String url;

    /** Date update is added to database */
    private Date created;

    /** user ID associated with this Update */
    @Index("user_idx")
    private Long userId;

    /** topic ID associated with this Update */
    @Index("topic_idx")
    private Long topicId;

    /**
     * Constructs an update object.
     */
    public UpdateModel(){
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
        TopicModel topicModel = new TopicModel();
        if(topicModel.topicExists(topicName)) {
            UpdateModel update = new UpdateModel(creator,
                    topicModel.findByName(topicName), content, url);
            update.insert();
        }
    }

    /**
     * Find updates associated with the given topic, and sort them.
     *
     * Note: Date code adapted from code by Bauke Scholtz:
     * http://stackoverflow.com/questions/4348525/get-date-as-of-4-hours-ago
     * 
     * @param topic the topic to find updates of
     * @param sortBy how to sort topics
     * // TODO pagination
     * @return List of updates
     */
    /*public List<UpdateModel> findByTopic(TopicModel topic, String sortBy) {
        boolean sortByRecent = false;
        if(Validator.isEmpty(sortBy) || "recent".equals(sortBy)) {
            sortByRecent = true;
        }
        List<UpdateModel> updates;
        if(sortByRecent){
            updates = this.findNewestByTopic(topic);
        }
        else {
            Date date;
            if(sortBy.equals("popular24h")){
                date = new Date(System.currentTimeMillis() - (24*60*60*1000));
            }
            else {
                date = new Date(System.currentTimeMillis() - (7*24*60*60*1000));
            }
            List<Long> topicIds = new ArrayList<Long>();
            topicIds.add(topic.getId());
            updates = this.findPopular(topicIds, date);
        }
        return updates;
    }*/

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
     * Returns how long ago this update was posted in minutes, hours, or days.
     *
     * Note: many Java programmers recommend the Joda-Time library for this, 
     * since this library handles many date-related things automatically, such
     * as daylight savings time.
     *
     * @return a String of how long ago this update was posted
     */
    public String getHowLongAgo() {
        Date curDate = new Date(System.currentTimeMillis());
        long diffms = curDate.getTime() - getDateCreated().getTime();
        long diffmin = diffms / (60*1000);
        if(diffmin < 1) {
            return Messages.get("time.now");
        }
        if(diffmin < 2) {
            return Messages.get("time", diffmin, Messages.get("time.min"));
        }
        if(diffmin < 60) {
            return Messages.get("time", diffmin, Messages.get("time.mins"));
        }
        long diffhour = diffmin / 60;
        if(diffhour < 2) {
            return Messages.get("time", diffhour, Messages.get("time.hour"));
        }
        if(diffhour < 24) {
            return Messages.get("time", diffhour, Messages.get("time.hours"));
        }
        long diffday = diffhour / 24;
        if(diffday < 2) {
            return Messages.get("time", diffday, Messages.get("time.day"));
        }
        return Messages.get("time", diffday, Messages.get("time.days"));
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

    /**
     * Sort a given map by value in ascending order. Based off code from:
     * http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-
     *      the-values-in-java
     * 
     * @param map the map to sort by value
     * @return the sorted map by value in ascending order
     */
    private Map sortByValue(Map map) {
         List list = new LinkedList(map.entrySet());
         Collections.sort(list, new Comparator() {
              public int compare(Object o1, Object o2) {
                   return ((Comparable) ((Map.Entry) (o1)).getValue())
                  .compareTo(((Map.Entry) (o2)).getValue());
              }
         });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}