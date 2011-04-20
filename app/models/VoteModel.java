/*
 * File: VoteModel.java
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
public class VoteModel extends BaseModel{

    /** auto-incremented unique ID for the vote */
    @Id
    private Long id;

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
    public VoteModel(){
        super();
    }

    /**
     * Constructs the Vote and initializes fields.
     *
     * @param name the name of the topic
     */
    public VoteModel(UserModel user, UpdateModel update) {
        this();
        this.userId = user.getId();
        this.updateId = update.getId();
        this.created = new Date();
    }

    /**
     * Create a new vote for the given update.
     *
     * @param update the update to vote on
     * @param creator the user who creates this
     */
    public void createVote(UpdateModel update, UserModel creator) {
        if(update != null) {
            // check if user has already voted on this update
            if(this.voteExists(creator, update)) {
                // delete current vote, as user is un-voting
                this.getByUserAndUpdate(creator, update).delete();
            }
            else {
                // cast the vote
                VoteModel vote = new VoteModel(creator, update);
                vote.insert();
            }
        }
    }

    /**
     * Find a update associated with the given unique topic ID.
     *
     * @param updateId the unique ID of the topic
     * @return update associated with the ID, or null if no update with that ID
     *      exists
     */
    public VoteModel findById(Long updateId) {
        return all().filter("id", updateId).get();
    }

    /**
     * Return a list of the votes associated with the given user.
     *
     * @param user the user to find votes of
     * @return the votes associated with the given user
     */
    public List<VoteModel> findByUser(UserModel user) {
        return all().filter("userId", user.getId()).fetch();
    }

    /**
     * Return a list of the votes associated with the given update.
     *
     * @param user the user to find votes of
     * @return the votes associated with the given user
     */
    public List<VoteModel> findByUpdate(UpdateModel update) {
        return all().filter("updateId", update.getId()).fetch();
    }

    /**
     * Get the Vote of a specific user and update.
     * 
     * @param user the user to get the vote of
     * @param update the update to get the vote of
     * @return Vote of a specific user and update
     */
    public VoteModel getByUserAndUpdate(UserModel user, UpdateModel update) {
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
    public boolean voteExists(UserModel user, UpdateModel update) {
        return getByUserAndUpdate(user, update) != null;
    }

    /**
     * Get the vote count of a given update.
     *
     * @param update the Update to get the vote count of
     * @return the vote count of a given update
     */
    public int getVoteCount(UpdateModel update) {
        List<VoteModel> votes = this.findByUpdate(update);
        int count = 0;
        if(votes != null) {
            count = votes.size();
        }
        return count;
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
    private Query<VoteModel> all() {
        return Model.all(VoteModel.class);
    }
}