/*
 * File: TopicModel.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 17
 */
package models;

import java.util.Date;
import play.i18n.Messages;
import siena.Id;
import siena.Model;
import siena.Query;
import utilities.ValidationException;
import utilities.Validator;

/**
 * Model for accessing and modifying topics.
 *
 * @author Peter Graham
 */
public class TopicModel extends Model{

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
        return all().filter("name", topicName).get();
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
     * Return the topic name.
     *
     * @return a string of the topic name
     */
    public String getName() {
        return this.name;
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