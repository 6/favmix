/*
 * File: Topic.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package models;

import java.util.Date;
import java.util.List;
import siena.Id;
import siena.Model;
import siena.NotNull;
import siena.Query;

/**
 * Model for accessing and modifying topics.
 *
 * @author Peter Graham
 */
public class Topic extends Model{

    /** auto-incremented unique ID for the topic */
    @Id
    private Long id;

    /** the topic name */
    @NotNull
    private String name;

    /** Date topic is added to database */
    private Date created;

    /**
     * Constructs a topic object.
     */
    public Topic(){
        super();
    }

    /**
     * Constructs the topic and initializes fields.
     *
     * @param name the name of the topic
     */
    public Topic(String topicName) {
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
    public Topic findById(Long topicId) {
        return all().filter("id", topicId).get();
    }

    /**
     * Find a topic associated with the given topic name.
     * 
     * @param topicName the name of the topic
     * @return topic associated with the name, or null if no topic with that 
     *      name exists
     */
    public Topic findByName(String topicName) {
        return all().filter("name", topicName).get();
    }
    /*public Topic findByUrl(String url, User subscriber) {
        return all().filter("feedUrl", url).filter("user", subscriber).get();
    }*/

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
     * Returns a list of feeds that the given user is subscribed to in
     * alphabetical order.
     *
     * @param subscriber the user to find the feeds of
     * @return a list of feeds that the given user is subscribed to
     */
    /*public List<Topic> findByUser(User subscriber) {
        return all().filter("user",subscriber).order("feedName").fetch();
    }*/

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
     * Returns a query object representing all feeds.
     *
     * @return a query object representing all feeds
     */
    private Query<Topic> all() {
        return Model.all(Topic.class);
    }
}