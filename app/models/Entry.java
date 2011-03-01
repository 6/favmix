/*
 * File: Entry.java
 * Name: Peter Graham
 * Class: CS 461
 * Project 1
 * Date: February 14
 */
package models;

import java.util.Date;
import java.util.List;
import play.data.validation.Required;
import play.data.validation.URL;
import siena.Id;
import siena.Index;
import siena.Model;
import siena.Query;

/**
 * Model for accessing and modifying feed entries.
 *
 * @author Peter Graham
 */
public class Entry extends Model{

    /** auto-incremented unique ID for the entry */
    @Id
    private Long id;

    /** the entry URL */
    @URL
    @Required
    private String entryUrl;

    /** the entry title */
    @Required
    private String entryTitle;

    /** Date entry is added to database */
    private Date created;

    /** number of votes on the entry */
    private int votes;

    /** map feeds with entries */
    @Index("feed_idx")
    private Topic entryFeed;

    /**
     * Constructs a Entry object.
     */
    public Entry(){
        super();
    }

    /**
     * Constructs the feed and initializes fields.
     *
     * @param url the URL of the feed
     * @param name the name of the feed
     * @param feed the feed that this entry is part of
     */
    public Entry(String url, String title, Topic feed) {
        this();
        this.entryUrl = url;
        this.entryTitle = title;
        this.entryFeed = feed;
        this.created = new Date();
        this.votes = 0;
    }

    /**
     * Find an entry associated with the given URL and feed.
     * 
     * @param url the URL of the entry to check
     * @param feed the feed to check URLs of
     * @return the feed associated with the feed, or null if no feed is
     *      associated with that URL
     */
    public Entry findByUrl(String url, Topic feed) {
        return all().filter("entryUrl", url).filter("entryFeed", feed).get();
    }

    /**
     * Check if the given entry URL is already in the database.
     *
     * @param url the URL of the entry to check
     * @param feed the feed to check URLs of
     * @return true if URL is in the database, or false it isn't in the database
     */
    public boolean entryInDatabase(String url, Topic feed) {
        return findByUrl(url, feed) != null;
    }

    /**
     * Returns string representation of the entry (the URL).
     * 
     * @return a string of the entry URL
     */
    public String toString() {
        return getEntryUrl();
    }

    /**
     * Return the entry URL.
     *
     * @return a string of the entry URL
     */
    public String getEntryUrl() {
        return this.entryUrl;
    }

    /**
     * Returns unique ID of the entry.
     * 
     * @return the unique id of the entry
     */
    public Long getEntryId() {
        return this.id;
    }

    /**
     * Returns entry title.
     *
     * @return the entry title
     */
    public String getEntryTitle() {
        return this.entryTitle;
    }

    /**
     * Returns the date that this entry was added to the database.
     *
     * @return the date that this entry was added to the database
     */
    public Date getDateCreated() {
        return this.created;
    }

    /**
     * Returns a list of entries of a given feed, and order them by recency.
     *
     * @param feed the Feed to find the entries of
     * @return a list of entries of the given feed
     */
    public List<Entry> findRecentByFeed(Topic feed) {
        return all().filter("entryFeed",feed).order("-created").fetch();
    }

    /**
     * Returns a list of entries of a given feed, and order them by popularity.
     *
     * @param feed the Feed to find the entries of
     * @return a list of entries of the given feed
     */
    public List<Entry> findPopularByFeed(Topic feed) {
        return all().filter("entryFeed",feed).order("-votes").fetch();
    }

    /**
     * Returns a query object representing all entries.
     *
     * @return a query object representing all entries
     */
    private Query<Entry> all() {
        return Model.all(Entry.class);
    }
}