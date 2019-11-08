package com.gittfo.moodtracker;

import java.util.ArrayList;

/**
 * Represents a single person using our mood tracker app. Should correspond one-to-one with UUID's
 * in the database.
 */
public class User {

    // People who are following our moods
    private ArrayList<User> followers;
    // People who's moods we are following
    private ArrayList<User> following;
    // List of people who are requested to follow us
    private ArrayList<User> followRequests;

    // The actual legal name of the user
    private String name;
    // The unique username of this user
    private String userName;
    // A unique ID identifying this user
    private long UUID;

    /**
     * Create a new User, a user of the app with a mood history.
     * @param name  // Actual IRL name of the user.
     * @param userName  // An online username.
     * @param UUID  // Universally Unique Identifier.
     */
    public User(String name, String userName, long UUID) {
        this.name = name;
        this.userName = userName;
        this.UUID = UUID;
    }

    /**
     * @return The list of followers for this user.
     */
    public ArrayList<User> getFollowers() {
        return followers;
    }

    /**
     * @param followers The list of followers for this user.
     */
    public void setFollowers(ArrayList<User> followers) {
        this.followers = followers;
    }

    /**
     * @return The list of people following this user.
     */
    public ArrayList<User> getFollowing() {
        return following;
    }

    /**
     * @param following The list of people following this user.
     */
    public void setFollowing(ArrayList<User> following) {
        this.following = following;
    }

    /**
     * @return The list of people who've requested to follow this user.
     */
    public ArrayList<User> getFollowRequests() {
        return followRequests;
    }

    /**
     * @param followRequests The list of people who've requested to follow this user.
     */
    public void setFollowRequests(ArrayList<User> followRequests) {
        this.followRequests = followRequests;
    }

    /**
     * @return The legal name of this user.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The legal name of this user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The unique username of this user.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName The unique username of this user.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return This user's UUID, identifying them uniquely for the database.
     */
    public long getUUID() {
        return UUID;
    }
}
