/*
 * File: Vote.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package models;

import java.util.Date;
import java.util.List;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;

/**
 * Model for accessing and modifying update votes.
 *
 * @author Peter Graham
 */
public class Vote extends Model{

    /** auto-incremented unique ID for the vote */
    @Id
    private Long id;

    /** whether or not the vote is up (positive) or down (negative) */
    private boolean isUpVote;

    /** Date vote is added to database */
    private Date created;

    /** user ID associated with this Vote */
    @Index("user_idx")
    private Long userId;

    /** update ID associated with this Vote */
    @Index("update_idx")
    private Long updateId;

    /**
     * Constructs a Vote object.
     */
    public Vote(){
        super();
    }

    /**
     * Constructs the Vote and initializes fields.
     *
     * @param name the name of the topic
     */
    public Vote(User user, Update update, boolean isUp) {
        this();
        this.userId = user.getId();
        this.updateId = update.getId();
        this.isUpVote = isUp;
        this.created = new Date();
    }

    /**
     * Find a update associated with the given unique topic ID.
     *
     * @param updateId the unique ID of the topic
     * @return update associated with the ID, or null if no update with that ID
     *      exists
     */
    public Vote findById(Long updateId) {
        return all().filter("id", updateId).get();
    }

    /**
     * Return a list of the votes associated with the given user.
     *
     * @param user the user to find votes of
     * @return the votes associated with the given user
     */
    public List<Vote> findByUser(User user) {
        return all().filter("userId", user.getId()).fetch();
    }

    /**
     * Return a list of the votes associated with the given update.
     *
     * @param user the user to find votes of
     * @return the votes associated with the given user
     */
    public List<Vote> findByUpdate(Update update) {
        return all().filter("updateId", update.getId()).fetch();
    }

    /**
     * Get the Vote of a specific user and update.
     * 
     * @param user the user to get the vote of
     * @param update the update to get the vote of
     * @return Vote of a specific user and update
     */
    public Vote getByUserAndUpdate(User user, Update update) {
        return all().filter("userId", user.getId())
                .filter("updateId", update.getId()).get();
    }

    /**
     * Returns whether or not given user has voted on a given update.
     *
     * @param user the user to check
     * @param update the update to check
     * @return boolean representing whether or not given user has voted on
     *      given update
     */
    public boolean voteExists(User user, Update update) {
        return getByUserAndUpdate(user, update) != null;
    }

    /**
     * Get the vote count of a given update.
     *
     * @param update the Update to get the vote count of
     * @return the vote count of a given update
     */
    public Long getVoteCount(Update update) {
        List<Vote> votes = this.findByUpdate(update);
        Long count = Long.valueOf(0);
        for(Vote vote : votes) {
            if(vote.isUpVote()) {
                count += 1;
            }
            else {
                count += -1;
            }
        }
        return count;
    }

    /**
     * Return whether or not this vote is an up-vote (positive).
     *
     * @return boolean representing whether or not this vote is an up-vote
     */
    public boolean isUpVote() {
        return this.isUpVote;
    }

    /**
     * Returns unique ID of the vote.
     *
     * @return the unique id of the vote
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the date that this vote was added to the database.
     *
     * @return the date that this vote was added to the database
     */
    public Date getDateCreated() {
        return this.created;
    }

    /**
     * Returns a query object representing all votes.
     *
     * @return a query object representing all votes
     */
    private Query<Vote> all() {
        return Model.all(Vote.class);
    }
}