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
import siena.NotNull;
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
    @NotNull
    @Index("user_idx")
    private Long userId;

    /** topic ID associated with this UserTopic */
    @NotNull
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
     * @param theUser the user to get the topics followed of
     * @return list of the topics followed
     */
    public List<Topic> getTopicsByUser(User theUser){
        List<UserTopic> userTopics = all()
                .filter("userId",theUser.getId()).fetch();
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
