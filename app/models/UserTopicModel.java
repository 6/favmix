/*
 * File: UserTopicModel.java
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
public class UserTopicModel extends Model{

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
    public UserTopicModel(){
        super();
    }

    /**
     * Constructs the UserTopic and initializes fields.
     *
     * @param theUserId user ID to associate with this UserTopic
     * @param theTopicId topic ID to associate with this UserTopic
     */
    public UserTopicModel(Long theUserId, Long theTopicId) {
        this();
        this.userId = theUserId;
        this.topicId = theTopicId;
    }

    /**
     * Get a list of the topic IDs of the topics followed by a given user.
     *
     * @param user the user to get the IDs of the topics followed of
     * @return list of the IDs of the topics followed
     */
    public List<Long> getTopicIdsByUser(UserModel user) {
        List<UserTopicModel> userTopics = all()
                .filter("userId",user.getId()).fetch();
        List<Long> topicIds = new ArrayList<Long>();
        if(userTopics != null) {
            for(UserTopicModel userTopic : userTopics) {
                topicIds.add(userTopic.getTopicId());
            }
        }
        return topicIds;
    }

    /**
     * Get a list of the topics followed by a given user.
     * 
     * @param user the user to get the topics followed of
     * @return list of the topics followed
     */
    public List<TopicModel> getTopicsByUser(UserModel user){
        List<TopicModel> topics = new ArrayList<TopicModel>();
        TopicModel topicObject = new TopicModel();
        for(Long curTopicId : this.getTopicIdsByUser(user)) {
            topics.add(topicObject.findById(curTopicId));
        }
        return topics;
    }

    /**
     * Get a list of the users that follow a given topic.
     *
     * @param topic the topic to get the followers of
     * @return list of the users following the topic
     */
    public List<UserModel> getUsersByTopic(TopicModel topic){
        List<UserTopicModel> userTopics = all()
                .filter("topicId",topic.getId()).fetch();
        List<UserModel> users = new ArrayList<UserModel>();
        UserModel userObject = new UserModel();
        if(userTopics != null) {
            for(UserTopicModel userTopic : userTopics) {
                Long curUserId = userTopic.getUserId();
                users.add(userObject.findById(curUserId));
            }
        }
        return users;
    }

    /**
     * Delete a UserTopic from the database, for when a user unfollows a topic.
     *
     * @param user the user associated with the UserTopic to delete
     * @param topic the topic associated with the UserTopic to delete
     */
    public void delete(UserModel user, TopicModel topic) {
        UserTopicModel toDelete = all().filter("userId", user.getId())
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
    public boolean isFollowing(UserModel user, TopicModel topic) {
        return user != null && all().filter("userId", user.getId())
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
    private Query<UserTopicModel> all() {
        return Model.all(UserTopicModel.class);
    }
}