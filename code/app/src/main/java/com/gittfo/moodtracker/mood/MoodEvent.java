package com.gittfo.moodtracker.mood;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.function.Supplier;


/**
 * A Mood Event contains information about a User's emotional state, the date and time of entry,
 * an optional reason for that emotional state, an optional description of the Social Situation that
 * the User was in at time of entry, an optional picture, and an optional location.
 */
public class MoodEvent implements Serializable {

    public static final int HAPPY_INDEX = 0;
    public static final int SAD_INDEX = 1;
    public static final int SURPRISED_INDEX = 2;
    public static final int AFRAID_INDEX = 3;
    public static final int DISGUSTED_INDEX = 4;
    public static final int ANGRY_INDEX = 5;
    public static final int ZERO_SOCIAL_INDEX = 0;
    public static final int ONE_SOCIAL_INDEX = 1;
    public static final int TWOPLUS_SOCIAL_INDEX = 2;
    public static final int CROWD_SOCIAL_INDEX = 3;
    public static final int NA_SOCIAL_INDEX = 4;

    // Valid social situations for a MoodEvent
    public enum SocialSituation {
        ZERO,
        ONE,
        TWOPLUS,
        CROWD,
        NA
    }

    private String photoReference; // The name of a photograph corresponding to this event

    private String reason; // Brief textual comment describing the reason for this event

    private Date dateTime; // The date/time the mood event was create

    private SocialSituation socialSituation; // The number of people around during this event

    private Mood.EmotionalState mood; // Last but not least, the actual Mood associated to this event

    private String username; // optional; tracks usernames of MoodEvents created by other users

    // The location the event was created
    private double latitude;
    private double longitude;

    // ID used for Firebase
    @Exclude
    private String id;

    /**
     * Create a new Mood Event
     * @param photoReference  Filename for a photo of the event.
     * @param reason          Reason for this mood event, e.g. "breakup".
     * @param dateTime        When the mood event was created
     * @param socialSituation How many people were around.
     * @param mood            The mood of this event.
     * @param lat             Where the event happened.
     * @param lon             Where the event happened.
     */
    public MoodEvent(String photoReference, String reason, Date dateTime, SocialSituation socialSituation, Mood.EmotionalState mood, double lat, double lon) {
        this.photoReference = photoReference;
        this.reason = reason;
        this.dateTime = dateTime;
        this.socialSituation = socialSituation;
        this.mood = mood;
        this.latitude = lat;
        this.longitude = lon;
    }

    /**
     * Used when querying the database for a mood,
     * when only the id is used
     * @param moodEventID the id of the moodEvent
     * @return a new mood id
     */
    public static MoodEvent fromId(String moodEventID) {
        MoodEvent me = new MoodEvent(
                null,
                null,
                null,
                null,
                null,
                Double.NaN,
                Double.NaN
        );
        me.setId(moodEventID);
        return me;
    }

    /**
     * Given a string representation (as described on Firebase) return a valid SocialSituation
     * @param socialSituation A string representation of a valid SocialSituation
     * @return                A valid SocialSituation
     */
    public static SocialSituation socialSituationFromFirebaseString(String socialSituation) {
        switch (socialSituation) {
            case "ZERO": return SocialSituation.ZERO;
            case "ONE": return SocialSituation.ONE;
            case "TWOPLUS": return SocialSituation.TWOPLUS;
            case "CROWD": return SocialSituation.CROWD;
            default:
                return SocialSituation.NA;
        }
    }

    /**
     * Given a string representation (as displayed in our app's UI) return a valid SocialSituation
     * @param socialSituation A string representation of a valid SocialSituation
     * @return                A valid SocialSituation
     */
    public static SocialSituation socialSituationFromString(String socialSituation) {
        switch (socialSituation) {
            case "0": return SocialSituation.ZERO;
            case "1": return SocialSituation.ONE;
            case "2+": return SocialSituation.TWOPLUS;
            case "A Crowd": return SocialSituation.CROWD;
            default:
                return SocialSituation.NA;
        }
    }

    /**
     * Get the ID for this MoodEvent
     *
     * @return The ID for this MoodEvent
     */
    public String getId() {
        return id;
    }

    /**
     * Set the ID for this MoodEvent
     *
     * @param id The new ID for this MoodEvent
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Create a MoodEvent object based on data stored in Firebase.
     *
     * @param document A Firebase document containing data describing a MoodEvent
     * @return         A MoodEvent object based on data stored in Firebase
     */
    public static MoodEvent getMoodEventFromFirebase(DocumentSnapshot document) {
        String photoReference = safeGet(() -> document.getString("photoReference"));
        String reason = safeGet(() -> document.getString("reason"));
        Date date = safeGet(() -> document.getDate("date"));
        SocialSituation situation = safeGet(() -> socialSituationFromFirebaseString(document.getString("socialSituation")));
        Mood.EmotionalState mood = safeGet(() -> Mood.emotionalStateFromString(document.getString("mood")));
        Double lat = safeGet(() -> document.getDouble("latitude"));
        Double lon = safeGet(() -> document.getDouble("longitude"));
        MoodEvent moodEvent = new MoodEvent(
                photoReference,
                reason,
                date,
                situation,
                mood,
                lat != null ? lat : Double.NaN,
                lon != null ? lon : Double.NaN
        );
        moodEvent.setId(document.getId());
        return moodEvent;
    }

    /**
     * Create a MoodEvent object from a JsonObject
     *
     * @param e A JsonObject containing data describing a MoodEvent
     * @return  A MoodEvent object based on data stored in a  JsonObject
     */
    public static MoodEvent getMoodEventFromJson(JsonObject e) {
        String photoReference = safeGet(() -> e.get("photoReference").getAsString());
        String reason = safeGet(() -> e.get("reason").getAsString());
        Date date = safeGet(() -> new Date(1000 * e.get("date").getAsJsonObject().get("_seconds").getAsLong()));
        SocialSituation situation = safeGet(() -> socialSituationFromFirebaseString(e.get("socialSituation").getAsString()));
        Mood.EmotionalState mood = safeGet(() -> Mood.emotionalStateFromString(e.get("mood").getAsString()));
        Double lat = safeGet(() -> e.get("latitude").getAsDouble());
        Double lon = safeGet(() -> e.get("longitude").getAsDouble());
        String id = safeGet(() -> e.get("id").getAsString());

        JsonObject ownerInfo = safeGet(() -> e.get("owner").getAsJsonObject());
        String ownerUsername = safeGet(() -> ownerInfo.get("username").getAsString());
        String ownerId = safeGet(() -> ownerInfo.get("id").getAsString());

        MoodEvent moodEvent = new MoodEvent(
                photoReference,
                reason,
                date,
                situation,
                mood,
                lat != null ? lat : Double.NaN,
                lon != null ? lon : Double.NaN
        );
        moodEvent.setId(id);

        moodEvent.setUsername(ownerUsername); // store username for later display on the map
        return moodEvent;
    }
    
    /**
     * Safely call a get function; if a NullPointerException is caught, a StackTrace will be printed
     *
     * @param fs the getter function
     * @param <T> the type
     * @return the value of the getter function or null if a null pointer exception occured
     */
    private static<T> T safeGet(Supplier<T> fs){
        try {
            return fs.get();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the path of a photo describing this MoodEvent
     *
     * @return The filename of a photo for this event.
     */
    public String getPhotoReference() {
        return photoReference;
    }

    /**
     * Set the path of a photo describing this MoodEvent
     *
     * @param photoReference The filename of a photo for this event.
     */
    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    /**
     * Get the reason for this MoodEvent
     *
     * @return A description of the reason for this event.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set the reason for this MoodEvent
     *
     * @param reason A description of the reason for this event.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Get the date this MoodEvent was created
     *
     * @return When the mood event was created
     */
    public Date getDate() {
        return dateTime;
    }

    /**
     * Set the date this MoodEvent was created
     *
     * @param date When the mood event was created
     */
    public void setDate(Date date) {
        this.dateTime = date;
    }

    /**
     * Get the social situation this MoodEvent was created in
     *
     * @return The number of people around during this event.
     */
    public SocialSituation getSocialSituation() {
        return socialSituation;
    }

    /**
     * Set the social situation this MoodEvent was created in
     *
     * @param socialSituation The number of people around during this event.
     */
    public void setSocialSituation(SocialSituation socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Get the EmotionalState of this MoodEvent.
     *
     * @return The mood of this event.
     */
    public Mood.EmotionalState getMood() {
        return mood;
    }

    /**
     * Set the EmotionalState of this MoodEvent.
     *
     * @param mood The mood of this event.
     */
    public void setMood(Mood.EmotionalState mood) {
        this.mood = mood;
    }

    /**
     * Return a string representation of this mood event that describes some of its attributes
     * (emotional state, date, and reason).
     *
     * @return A string representation of this mood event
     */
    public String toString() {
        return String.format("%s: (%s, %s, %s)", this.id, this.mood.toString(), this.dateTime.toString(), this.reason);
    }

    /**
     * Return a string representation of this mood event that describes all
     * Useful for debugging
     *
     * @return A string representation of this mood event
     */
    public String toStringFull() {
        return String.format(Locale.CANADA,
                "%s: (%s, %s, %s)\nLoc: %f, %f\nID: %s\nPhoto: %s\nSocial: %s",
                this.id, this.mood.toString(), this.dateTime.toString(), this.reason,
                this.longitude, this.latitude, this.id, this.photoReference, this.socialSituation);
    }

    /**
     * Get the longitude this MoodEvent was created at.
     *
     * @return The longitude this MoodEvent was created at
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude this MoodEvent was created at.
     *
     * @param longitude The longitude this MoodEvent was created at
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get the latitude this MoodEvent was created at.
     *
     * @return The latitude this MoodEvent was created at
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude this MoodEvent was created at.
     *
     * @param latitude The latitude this MoodEvent was created at
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    /**
     * Set the name of the user who created this mood.
     * @param username the User's name.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the username of the user who created this mood.
     */
    public String getUsername() {
        return username;
    }
}
