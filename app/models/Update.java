/*
 * File: Update.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
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
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;

/**
 * Model for accessing and modifying topic/user updates.
 *
 * @author Peter Graham
 */
public class Update extends Model{

    /** auto-incremented unique ID for the update */
    @Id
    private Long id;

    /** the update content */
    private String content;

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
    public Update(){
        super();
    }

    /**
     * Constructs the update and initializes fields.
     *
     * @param user the User who posted this update
     * @param topic the Topic this update was posted to
     * @param updateContent the content of the update
     */
    public Update(User user, Topic topic, String updateContent) {
        this();
        this.userId = user.getId();
        this.topicId = topic.getId();
        this.content = updateContent;
        this.created = new Date();
    }

    /**
     * Find a update associated with the given unique topic ID.
     *
     * @param updateId the unique ID of the topic
     * @return update associated with the ID, or null if no update with that ID
     *      exists
     */
    public Update findById(Long updateId) {
        return all().filter("id", updateId).get();
    }

    /**
     * Return a list of the newest updates associated with the given topic.
     *
     * @param topic the topic to find newest updates of
     * @return the recent updates associated with the given topic
     */
    public List<Update> findNewestByTopic(Topic topic) {
        return all().filter("topicId", topic.getId()).order("-created").fetch();
    }

    /**
     * Return a list of the popular updates associated with the given topic.
     *
     * @param topic the topic to find popular updates of
     * @param afterDate the date after which to include topics
     * @return the popular updates associated with the given topic
     */
    public List<Update> findPopularByTopic(Topic topic, Date afterDate) {
        List<Update> updates = all().filter("topicId", topic.getId())
                .filter("created>", afterDate).fetch();
        Map<Update,Long> updatesByVotes = new HashMap<Update,Long>();
        // count vote value for each update
        for(Update update: updates) {
            updatesByVotes.put(update, update.getVoteCount());
        }
        updatesByVotes = this.sortByValue(updatesByVotes);
        // convert Map keys to List (we don't need the values anymore)
        List orderedUpdates = new ArrayList<Update>(updatesByVotes.keySet());
        Collections.reverse(orderedUpdates);
        return orderedUpdates;
    }

    /**
     * Convenience method for getting the vote count of this update.
     *
     * @return the vote count of this update
     */
    public Long getVoteCount() {
        Vote voteModel = new Vote();
        return voteModel.getVoteCount(this);
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
    private Query<Update> all() {
        return Model.all(Update.class);
    }

    /**
     * Sort a given map by value. Based off code from:
     * http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-
     *      the-values-in-java
     * 
     * @param map the map to sort by value
     * @return the sorted map by value
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