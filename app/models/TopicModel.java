/*
 * File: TopicModel.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package models;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import play.i18n.Messages;
import siena.Id;
import siena.Model;
import siena.Query;
import utilities.Constants;
import utilities.ValidationException;
import utilities.Validator;

/**
 * Model for accessing and modifying topics.
 *
 * @author Peter Graham
 */
public class TopicModel extends BaseModel{

    /** auto-incremented unique ID for the topic */
    @Id
    private Long id;

    /** the topic name */
    private String name;

    /** Date topic is added to database */
    private Date created;

    /**
     * Constructs a topic object.
     */
    public TopicModel(){
        super();
    }

    /**
     * Constructs the topic and initializes fields.
     *
     * @param name the name of the topic
     */
    public TopicModel(String topicName) {
        this();
        this.name = topicName;
        this.created = new Date();
    }

    /**
     * Find a topic associated with the given unique topic ID.
     *
     * @param topicId the unique ID of the topic
     * @return topic associated with the ID, or null if no topic with that ID
     *      exists
     */
    public TopicModel findById(Long topicId) {
        return all().filter("id", topicId).get();
    }

    /**
     * Find a topic associated with the given topic name.
     * 
     * @param topicName the name of the topic
     * @return topic associated with the name, or null if no topic with that 
     *      name exists
     */
    public TopicModel findByName(String topicName) {
        // make sure to decode URL
        URLDecoder decoder = new URLDecoder();
        try {
            String decodedName = decoder.decode(topicName, "UTF-8");
            return all().filter("name", decodedName).get();
        }
        catch(UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Check if the given topic name is already in use.
     *
     * @param topicName the name of the topic
     * @return true if name is in database, or false it isn't in database
     */
    public boolean topicExists(String topicName) {
        return findByName(topicName) != null;
    }

    /**
     * Creates a new topic with the given name if that name is valid.
     * 
     * @param topicName the name if the topic to create
     * @param creator the user who creates this topic
     * @throws ValidationException if topic name is empty or already in use
     */
    public void createTopic(String topicName, UserModel creator)
            throws ValidationException {
        if(Validator.isEmpty(topicName)) {
            throw new ValidationException(Messages.get("form.emptyField"));
        }
        if(Constants.RESERVED_TOPIC_NAMES.contains(topicName)) {
            throw new ValidationException(Messages.get("topic.exists"));
        }
        if(this.topicExists(topicName)) {
            throw new ValidationException(Messages.get("topic.exists"));
        }
        // insert the new topic
        TopicModel newTopic = new TopicModel(topicName);
        newTopic.insert();

        // make user follow that topic
        UserTopicModel userTopic = new UserTopicModel(creator.getId(),
                newTopic.getId());
        userTopic.insert();
    }

    /**
     * Gets the number of followers for a given topic.
     *
     * @return the number of followers
     */
    public int getFollowerCount() {
        UserTopicModel userTopicModel = new UserTopicModel();
        List<UserModel> followers = userTopicModel.getUsersByTopic(this);
        int count = 0;
        if(followers != null) {
            count = followers.size();
        }
        return count;
    }

    /**
     * Get popular topics based on the number of followers.
     *
     * @param howMany how many topics to return
     * @param offset int used for pagination
     * @return List of the popular topics with the given offset
     */
    public List<TopicModel> getPopular(int howMany, int offset) {
        Map<TopicModel,Integer> topicsByFollowers =
                new HashMap<TopicModel,Integer>();
        // count vote value for each update
        for(TopicModel topic : all().fetch()) {
            topicsByFollowers.put(topic, topic.getFollowerCount());
        }
        topicsByFollowers = this.sortByValue(topicsByFollowers);
        // convert Map keys to List (we don't need the values anymore)
        List orderedUpdates =
                new ArrayList<TopicModel>(topicsByFollowers.keySet());
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
     * Fetches the most recently created topics overall.
     *
     * @param howMany how many topics to return
     * @param offset the offset used for pagination
     * @return List if the most recent topics
     */
    public List<TopicModel> getNewest(int howMany, int offset) {
        return all().order("-created").fetch(howMany, offset);
    }

    /**
     * Return the topic name.
     *
     * @return a string of the topic name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the URL encoded version of the topic name.
     *
     * @return String of the URL encoded version of the topic name
     */
    public String getNameEncoded() {
        try {
            return URLEncoder.encode(this.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Returns unique ID of the topic.
     *
     * @return the unique id of the topic
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the date that this topic was added to the database.
     *
     * @return the date that this topic was added to the database
     */
    public Date getDateCreated() {
        return this.created;
    }

    /**
     * Convenience method for seeing how long ago a topic was created.
     *
     * @return how long ago this topic was created
     */
    public String getHowLongAgo() {
        return this.getHowLongAgo(this.getDateCreated());
    }

    /**
     * Returns string representation of the topic.
     *
     * @return a string representation of the topic
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns a query object representing all topics.
     *
     * @return a query object representing all topics
     */
    private Query<TopicModel> all() {
        return Model.all(TopicModel.class);
    }
}