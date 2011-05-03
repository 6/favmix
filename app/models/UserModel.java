/*
 * File: UserModel.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: April 16
 */
package models;

import java.util.Date;
import play.i18n.Messages;
import play.libs.Codec;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Query;
import utilities.Constants;
import utilities.ValidationException;
import utilities.Validator;

/**
 * Model for accessing and modifying user data.
 *
 * @author Peter Graham
 */
public class UserModel extends BaseModel{

    /** auto-incremented unique ID for the user */
    @Id
    private Long id;

    /** the user's email address */
    @Max(75)
    private String userEmail;

    /** the user's password (salted and hashed for security) */
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
    public UserModel(){
        super();
    }

    /**
     * Constructs the user account and initializes fields.
     *
     * @param emailAddress the email address associated with the user account
     * @param password the plaintext password associated with the user account
     */
    public UserModel(String emailAddress, String password) {
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
    public boolean isCorrectPassword(String password) {
        return passwordHash.equals(Codec.hexSHA1(
                Constants.PASSWORD_SALT+password));
    }

    /**
     * Find a user associated with the given email address.
     * 
     * @param email the email address of the user
     * @return the user associated with the email address, or null if no user is
     *      associated with that email address
     */
    public UserModel findByEmail(String email) {
        return all().filter("userEmail", email).get();
    }

    /**
     * Checks whether or not the given email and password combination are
     * associated with a user.
     *
     * @param email String of the email to check
     * @param password String of the password to check
     * @throws ValidationException if an email/password validation error occurs
     * @return true if valid login info, or false otherwise
     */
    public void validateLogin(String email,
            String password) throws ValidationException {
        if(Validator.isEmpty(email) || Validator.isEmpty(password)) {
            throw new ValidationException(Messages.get("form.emptyField"));
        }
        UserModel user = this.findByEmail(email);
        if(user == null || !user.isCorrectPassword(password)) {
            // email/password combination is invalid
            throw new ValidationException(Messages.get("login.incorrect"));
        }
    }

    /**
     * Create a new user with the given email and password.
     *
     * @param email String of the email of the user
     * @param password String of the password of the user
     * @throws ValidationException if email or password is invalid
     */
    public void createUser(String email, String password) 
            throws ValidationException {
        if(Validator.isEmpty(password) || Validator.isEmpty(email)) {
            throw new ValidationException(Messages.get("form.emptyField"));
        }
        if(!Validator.isEmail(email)) {
            throw new ValidationException(Messages.get("form.badEmail",email));
        }
        if(!this.isEmailAvailable(email)) {
            throw new ValidationException(Messages.get("form.emailUsed",email));
        }
        UserModel user = new UserModel(email, password);
        user.insert();
    }

    /**
     * Modify the user's profile. Set the name to null if it's empty, as this
     * will be shown in the view as the default name.
     * 
     * @param newName string of the user's new name
     * @param newBio string if the user's new biography
     */
    public void modifyProfile(String newName, String newBio) {
        if(Validator.isEmpty(newName)) {
            newName = null;
        }
        this.setName(newName);
        this.setBio(newBio);
        this.update();
    }

    /**
     * Modify the email and/or password of this user.
     * 
     * @param email the new email of the user
     * @param oldPass the old password of the user
     * @param newPass the new password of the user
     * @throws ValidationException if email or passwords are invalid
     */
    public void modifySettings(String email, String oldPass, String newPass)
            throws ValidationException {
        if(!Validator.isEmail(email)) {
            throw new ValidationException(Messages.get("form.badEmail",email));
        }
        if(!email.equals(this.getEmail())) {
            if(!this.isEmailAvailable(email)) {
                throw new ValidationException(Messages.get("form.emailUsed",
                        email));
            }
            this.setEmail(email);
            this.update();
        }
        if(!Validator.isEmpty(newPass) && !Validator.isEmpty(oldPass)) {
            if(!this.isCorrectPassword(oldPass)) {
                // invalid old password
                throw new ValidationException(Messages.get("form.badPassword"));
            }
            this.setPassword(newPass);
            this.update();
        }
    }

    /**
     * Find a user associated with the given unique ID.
     *
     * @param id the unique ID of the user
     * @return the user associated with the ID, or null if no user is associated
     *      with that ID
     */
    public UserModel findById(Long id) {
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
    public void followTopic(TopicModel topic) {
        UserTopicModel userTopic = new UserTopicModel(this.getId(),
                topic.getId());
        userTopic.insert();
    }

    /**
     * Unfollow a given topic.
     * 
     * @param topic Topic to unfollow
     */
    public void unFollowTopic(TopicModel topic) {
        UserTopicModel userTopic = new UserTopicModel();
        userTopic.delete(this, topic);
    }
    
    /**
     * Returns string representation of the user (their email address).
     * 
     * @return a string of the user's email address
     */
    @Override
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
     * Returns a string of the user's name, or the default name if no name is
     * specified.
     * 
     * @return a string of the user's name
     */
    public String getName() {
        if(this.name != null) {
            return this.name;
        }
        return Messages.get("msg.defaultUserName");
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
     * Sets a new password for the user, and salts and hashes it.
     *
     * @param password the password to set user's password to
     */
    public void setPassword(String password) {
        this.passwordHash = Codec.hexSHA1(Constants.PASSWORD_SALT+password);
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
    private Query<UserModel> all() {
        return Model.all(UserModel.class);
    }
}