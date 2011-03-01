/*
 * File: User.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package models;

import java.util.Date;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Codec;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Query;
import utilities.Constants;

/**
 * Model for accessing and modifying user data.
 *
 * @author Peter Graham
 */
public class User extends Model{

    /** auto-incremented unique ID for the user */
    @Id
    private Long id;

    /** the user's email address */
    @Email
    @Max(75)
    @Required
    private String userEmail;

    /** the user's password (salted and hashed for security) */
    @Required
    private String passwordHash;

    /** the user's name (not required) */
    @Max(75)
    private String name;

    /** the user's short bio */
    @Max(200)
    private String bio;

    /** Date user created their account */
    private Date created;

    /**
     * Constructs the user object.
     */
    public User(){
        super();
    }

    /**
     * Constructs the user account and initializes fields.
     *
     * @param emailAddress the email address associated with the user account
     * @param password the plaintext password associated with the user account
     */
    public User(String emailAddress, String password) {
        this();
        this.setEmail(emailAddress);
        this.setPassword(password);
        this.created = new Date();
    }

    /**
     * Check if the salted and hashed version of the given password matches with
     * this user's password.
     * 
     * @param password the plaintext password to check.
     * @return true if the passwords match, or false if they don't match
     */
    public boolean isValidPassword(String password) {
        return passwordHash.equals(Codec.hexSHA1(Constants.salt+password));
    }

    /**
     * Find a user associated with the given email address.
     * 
     * @param email the email address of the user
     * @return the user associated with the email address, or null if no user is
     *      associated with that email address
     */
    public User findByEmail(String email) {
        return all().filter("userEmail", email).get();
    }

    /**
     * Find a user associated with the given unique ID.
     *
     * @param id the unique ID of the user
     * @return the user associated with the ID, or null if no user is associated
     *      with that ID
     */
    public User findById(Long id) {
        return all().filter("id", id).get();
    }

    /**
     * Check if the given email address is already associated with an account.
     *
     * @param email the email address to check
     * @return true if email address is not associated with an account, or false
     *      if it is associated with an account already
     */
    public boolean isEmailAvailable(String email) {
        return findByEmail(email) == null;
    }

    /**
     * Add a new topic to the user's followed topics.
     *
     * @param topic the Topic to follow
     */
    public void followTopic(Topic topic) {
        UserTopic userTopic = new UserTopic(this.getId(), topic.getId());
        userTopic.insert();
    }


    /**
     * Unfollow a given topic.
     * 
     * @param topic Topic to unfollow
     */
    public void unfollowTopic(Topic topic) {
        UserTopic toDelete = new UserTopic(this.getId(), topic.getId());
        if(toDelete != null) {
            // user is following this topic, so delete
            toDelete.delete();
        }
    }
    
    /**
     * Returns string representation of the user (their email address).
     * 
     * @return a string of the user's email address
     */
    public String toString() {
        return getEmail();
    }

    /**
     * Return the user's email address.
     *
     * @return a string of the user's email address
     */
    public String getEmail() {
        return this.userEmail;
    }

    /**
     * Returns unique ID of the user.
     * 
     * @return the unique id of the user
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the date that this user was added to the database.
     *
     * @return the date that this user was added to the database
     */
    public Date getDateCreated() {
        return this.created;
    }

    /**
     * Returns a string of the user's name.
     * 
     * @return a string of the user's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a string of the user's bio.
     *
     * @return a string of the user's bio
     */
    public String getBio() {
        return this.bio;
    }

    /**
     * Sets a new email for the user.
     *
     * @param email the email to set user's email to
     */
    public void setEmail(String email) {
        this.userEmail = email;
    }

    /**
     * Sets a new password for the user.
     *
     * @param password the password to set user's password to
     */
    public void setPassword(String password) {
        this.passwordHash = Codec.hexSHA1(Constants.salt+password);
    }

    /**
     * Sets a new name for the user.
     *
     * @param newName the name to set user's name to
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Sets a new bio for the user.
     *
     * @param newBio the bio to set user's bio to
     */
    public void setBio(String newBio) {
        this.bio = newBio;
    }

    /**
     * Returns a query object representing all users.
     *
     * @return a query object representing all users
     */
    private Query<User> all() {
        return Model.all(User.class);
    }
}