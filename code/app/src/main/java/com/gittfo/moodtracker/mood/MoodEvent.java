package com.gittfo.moodtracker.mood;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.function.Supplier;


/**
 * A Mood Event contains information about a User's emotional state, the date and time of entry,
 * an optional reason for that emotional state, an optional description of the Social Situation that
 * the User was in at time of entry, an optional picture, and an optional location.
 */
public class MoodEvent {

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

    /**
     * Used when querying the database for a mood,
     * when only the id is used
     * @param moodEventID the id of the moodEvent
     * @return a new mood id
     */
    public static MoodEvent fromId(String moodEventID) {
        MoodEvent me = new MoodEvent();
        me.setId(moodEventID);
        return me;
    }

    // Valid social situations for a MoodEvent
    public enum SocialSituation {
        ZERO,
        ONE,
        TWOPLUS,
        CROWD,
        NA
    };

    // The name of a photograph corresponding to this event
    private String photoReference;

    // Brief textual comment describing the reason for this event
    private String reason;

    // The date/time the mood event was create
    private Date dateTime;

    // The number of people around during this event
    private SocialSituation socialSituation;

    // Last but not least, the actual Mood associated to this event
    private Mood.EmotionalState mood;

    // The location the event was created
    private double latitude;
    private double longtitude;

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
        this.longtitude = lon;
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
     * For internal use only
     */
    private MoodEvent() { }

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
        Double lon = safeGet(() -> document.getDouble("longtitude"));
        MoodEvent moodEvent = new MoodEvent(
                photoReference,
                reason,
                date,
                situation,
                mood,
                lat != null ? lat : Double.NaN,
                lon != null ? lat : Double.NaN
        );
        moodEvent.setId(document.getId());
        return moodEvent;
    }


    /**
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
        }
        return null;
    }

    /**
     * @return The filename of a photo for this event.
     */
    public String getPhotoReference() {
        return photoReference;
    }

    /**
     * @param photoReference The filename of a photo for this event.
     */
    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    /**
     * @return A description of the reason for this event.
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason A description of the reason for this event.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return When the mood event was created
     */
    public Date getDate() {
        return dateTime;
    }

    /**
     * @param date When the mood event was created
     */
    public void setDate(Date date) {
        this.dateTime = date;
    }

    /**
     * @return The number of people around during this event.
     */
    public SocialSituation getSocialSituation() {
        return socialSituation;
    }

    /**
     * @param socialSituation The number of people around during this event.
     */
    public void setSocialSituation(SocialSituation socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * @return The mood of this event.
     */
    public Mood.EmotionalState getMood() {
        return mood;
    }

    /**
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

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
