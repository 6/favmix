/*
 * File: UpdateModel.java
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
import play.i18n.Messages;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;
import utilities.ValidationException;
import utilities.Validator;

/**
 * Model for accessing and modifying topic/user updates.
 *
 * @author Peter Graham
 */
public class UpdateModel extends Model{

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
    public UpdateModel(){
        super();
    }

    /**
     * Constructs the update and initializes fields.
     *
     * @param user the User who posted this update
     * @param topic the Topic this update was posted to
     * @param updateContent the content of the update
     */
    public UpdateModel(UserModel user, TopicModel topic, String updateContent) {
        this();
        this.userId = user.getId();
        this.topicId = topic.getId();
        this.content = updateContent;
        this.created = new Date();
    }

    /**
     * Create a new update for the given topic.
     *
     * @param content String of the content of update
     * @param topicName String of the name of the topic to post to
     * @param  creator the user who creates this
     * @throws ValidationException if content is empty
     */
    public void createUpdate(String content, String topicName, 
            UserModel creator) throws ValidationException {
        if(Validator.isEmpty(content)) {
            throw new ValidationException(Messages.get("form.emptyField"));
        }
        TopicModel topicModel = new TopicModel();
        if(topicModel.topicExists(topicName)) {
            UpdateModel update = new UpdateModel(creator,
                    topicModel.findByName(topicName), content);
            update.insert();
        }
    }

    /**
     * Find updates associated with the given topic, and sort them.
     *
     * Note: Date code adapted from:
     * http://stackoverflow.com/questions/4348525/get-date-as-of-4-hours-ago
     * 
     * @param topic the topic to find updates of
     * @param sortBy how to sort topics (popular, recent)
     * @return List of updates
     */
    public List<UpdateModel> findByTopic(TopicModel topic, String sortBy) {
        boolean sortByRecent = true;
        if(sortBy != null && sortBy.equals("popular")) {
            sortByRecent = false;
        }
        List<UpdateModel> updates;
        if(sortByRecent){
            updates = this.findNewestByTopic(topic);

        }
        else {
            Date day = new Date(System.currentTimeMillis() - (24*60*60*1000));
            // TODO: different date ranges
            updates = this.findPopularByTopic(topic, day);
        }
        return updates;
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
     * @param topic the topic to find newest updates of
     * @return the recent updates associated with the given topic
     */
    public List<UpdateModel> findNewestByTopic(TopicModel topic) {
        return all().filter("topicId", topic.getId()).order("-created").fetch();
    }

    /**
     * Return a list of the popular updates associated with the given topic.
     *
     * @param topic the topic to find popular updates of
     * @param afterDate the date after which to include topics
     * @return the popular updates associated with the given topic
     */
    public List<UpdateModel> findPopularByTopic(TopicModel topic, Date afterDate) {
        List<UpdateModel> updates = all().filter("topicId", topic.getId())
                .filter("created>", afterDate).fetch();
        Map<UpdateModel,Long> updatesByVotes = new HashMap<UpdateModel,Long>();
        // count vote value for each update
        for(UpdateModel update: updates) {
            updatesByVotes.put(update, update.getVoteCount());
        }
        updatesByVotes = this.sortByValue(updatesByVotes);
        // convert Map keys to List (we don't need the values anymore)
        List orderedUpdates = new ArrayList<UpdateModel>(updatesByVotes.keySet());
        Collections.reverse(orderedUpdates);
        return orderedUpdates;
    }

    /**
     * Convenience method for getting the vote count of this update.
     *
     * @return the vote count of this update
     */
    public Long getVoteCount() {
        VoteModel voteModel = new VoteModel();
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