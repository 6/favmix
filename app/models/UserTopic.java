/*
 * File: UserTopic.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package models;

import java.util.ArrayList;
import java.util.List;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;

/**
 * Maps together the many-to-many relationship between users and topics.
 *
 * @author Peter Graham
 */
public class UserTopic extends Model{

    /** auto-incremented unique ID for the UserTopic */
    @Id
    private Long id;

    /** user ID associated with this UserTopic */
    @Index("user_idx")
    private Long userId;

    /** topic ID associated with this UserTopic */
    @Index("topic_idx")
    private Long topicId;

    /**
     * Constructs the UserTopic object.
     */
    public UserTopic(){
        super();
    }

    /**
     * Constructs the UserTopic and initializes fields.
     *
     * @param theUserId user ID to associate with this UserTopic
     * @param theTopicId topic ID to associate with this UserTopic
     */
    public UserTopic(Long theUserId, Long theTopicId) {
        this();
        this.userId = theUserId;
        this.topicId = theTopicId;
    }

    /**
     * Get a list of the topics followed by a given user.
     * 
     * @param user the user to get the topics followed of
     * @return list of the topics followed
     */
    public List<Topic> getTopicsByUser(User user){
        List<UserTopic> userTopics = all()
                .filter("userId",user.getId()).fetch();
        List<Topic> topics = new ArrayList<Topic>();
        Topic topicObject = new Topic();
        if(userTopics != null) {
            for(UserTopic userTopic : userTopics) {
                Long curTopicId = userTopic.getTopicId();
                topics.add(topicObject.findById(curTopicId));
            }
        }
        return topics;
    }

    /**
     * Delete a UserTopic from the database, for when a user unfollows a topic.
     *
     * @param user the user associated with the UserTopic to delete
     * @param topic the topic associated with the UserTopic to delete
     */
    public void delete(User user, Topic topic) {
        UserTopic toDelete = all().filter("userId", user.getId())
                .filter("topicId", topic.getId()).get();
        if(toDelete != null) {
            // user is following this topic, so delete
            toDelete.delete();
        }
    }

    /**
     * Determine if the given user is following the given topic.
     *
     * @param user the user to check
     * @param topic the topic to check
     * @return boolean whether or not the user is following the topic
     */
    public boolean isUserFollowing(User user, Topic topic) {
        return all().filter("userId", user.getId())
                .filter("topicId", topic.getId()).get() != null;
    }

    /**
     * Returns unique ID of the UserTopic.
     *
     * @return the unique id of the UserTopic
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns unique ID of the user.
     *
     * @return the unique id of the user
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Returns unique ID of the topic.
     *
     * @return the unique id of the topic
     */
    public Long getTopicId() {
        return this.topicId;
    }

    /**
     * Returns a query object representing all UserTopics.
     *
     * @return a query object representing all UserTopics
     */
    private Query<UserTopic> all() {
        return Model.all(UserTopic.class);
    }
}