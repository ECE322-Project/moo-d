package com.gittfo.moodtracker.database;

import android.content.Context;
import android.util.Log;


import androidx.core.util.Consumer;
import androidx.core.util.Supplier;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gittfo.moodtracker.mood.MoodEvent;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_PRIVATE;

/**
 * A singleton class for abstracting out firestore
 */
public class Database {
    private static final String TAG = "DB_INTERNAL";
    public static final String PREFS = "databasesharedprefereneces";
    private static final String cloudRoot = "https://us-central1-moo-d-95679.cloudfunctions.net";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static RequestQueue queue;

    private String userId;

    /**
     * Create a new database object and store the userID of the current user
     *
     * @param c Context in which the database is created
     */
    private Database(Context c) {
        userId = c.getSharedPreferences(Database.PREFS, MODE_PRIVATE)
                .getString("user", "");
        if (queue == null) {
            queue = Volley.newRequestQueue(c);
        }
    }

    /**
     * Get an instance of the database
     *
     * @param c Context in which the database is obtained
     * @return An instance of the database object
     */
    public static Database get(Context c) {
        return new Database(c);
    }

    /**
     * Return an instance of the Database object with testymctestface301's
     * user ID.
     *
     * @param c context
     * @return instance of Database
     */
    public static Database getMock(Context c) {
        Database mocked = new Database(c);
        mocked.userId = "105648403813593449833";
        return mocked;
    }

    /**
     * Gets all the mood events for the signed in user
     * <p>
     * Usage:
     *
     * <pre>
     * <code>
     *   // Print out all mood events of current user
     *   Database.get(this).getMoods()
     *       .addOnSuccessListener(moods -> {
     *          for(MoodEvent ev : moods) {
     *              System.out.println(ev.toString());
     *          }
     *      });
     * </code>
     * </pre>
     *
     * @return A task containing a list of mood events for the signed in user
     */
    public Task<List<MoodEvent>> getMoods() {
        CollectionReference moods = db.collection("users")
                .document(currentUser())
                .collection("moods");
        return moods.get().continueWithTask(
                task -> {
                    Log.d(TAG, "Found a mood");
                    List<MoodEvent> me = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        MoodEvent v = MoodEvent.getMoodEventFromFirebase(document);
                        if (v != null) {
                            me.add(v);
                        }
                    }
                    return Tasks.call(() -> me);
                }
        );
    }

    /**
     * Adds a new Mood Event to firebase for the current user
     * <p>
     * Usage:
     * <pre><code>
     *  Database.get(this).addMoodEvent(new MoodEvent(
     *      new Mood("sad", "dsfjajsdj"),
     *      new Date()
     *  ));
     *  </code></pre>
     *
     * @param me the MoodEvent
     */
    public void addMoodEvent(MoodEvent me) {
        db.collection("users")
                .document(currentUser())
                .collection("moods")
                .document()
                .set(me);
    }

    /**
     * Returns the userID of the current user (each user has a unique userID).
     *
     * @return The userID of the current user
     */
    public String currentUser() {
        return userId;
    }

    /**
     * Updates a user's MoodEvent from the database. This is necessary when the user chooses to edit
     * a MoodEvent, making changes to its data. The database knows the proper MoodEvent to update
     * based on the MoodEvent's ID.
     *
     * @param me The MoodEvent to be updated
     */
    public void updateMoodEvent(MoodEvent me) {
        Log.d(TAG, "UPDATING TO: " + me.toString());
        db.collection("users")
                .document(currentUser())
                .collection("moods")
                .document(me.getId())
                .set(me);
    }

    /**
     * Retrieves a MoodEvent from the database based on its ID.
     *
     * @param isEdit The ID of the MoodEvent to be retrieved
     * @return A Task containing the specified MoodEvent
     */
    public Task<MoodEvent> getMoodByID(String isEdit) {
        DocumentReference moods = db.collection("users")
                .document(currentUser())
                .collection("moods")
                .document(isEdit);
        Task<DocumentSnapshot> t = moods.get();
        return t.continueWithTask(task -> {
            DocumentSnapshot ds = task.getResult();
            return Tasks.call(() -> MoodEvent.getMoodEventFromFirebase(ds));
        });
    }

    /**
     * Permanently deletes a user's MoodEvent from the database. The database knows the proper
     * MoodEvent to delete based on the MoodEvent's ID.
     *
     * @param moode The MoodEvent to be deleted
     */
    public void deleteMoodEvent(MoodEvent moode) {
        db.collection("users")
                .document(currentUser())
                .collection("moods")
                .document(moode.getId())
                .delete();
    }

    /**
     * Gets all the moods of all the users the current user is following
     * Usage:
     * <pre><code>
     *   Database.get(this).getFolloweeMoods("", moods -> {
     *      // here, moods is a List<MoodEvent> containing all the moods
     *      // These can be stuck in listview or something
     *      for (MoodEvent me : moods) {
     *          // Here me is each individual mood
     *          Log.d("JDBCLOUD", me.toString());
     *      }
     *   }
     * </code></pre>
     * @param callback the callback function
     */
    public void getFolloweeMoods(Consumer<List<MoodEvent>> callback) {
        String url = buildCloudURL(String.format("getFolloweeMoods?uid=%s", userId));
        callCloudFunctionForString(url,
                res -> {
                    // TODO: parse json data to mood events
                    // TODO: actually finish function
                    Log.v("JDBCLOUD", "Got data:\n" + res);
                });
        Log.v("JDBCLOUD", url);
    }


    /**
     * Follow a user
     * Usage:
     * <pre><code>
     *   Database.get(this).followUser("", b -> {
     *      // here, b is a boolean. It is true if the follow was successful,
     *      // otherwise it is false. This value should be checked, and shown to the user
     *      // Perhaps with a snackbar, or toast
     *      Log.d("JDBCLOUD", "Attempted to follow" + b));
     *   }
     * </code></pre>
     * @param otherId the user to follow
     * @param c the callback function
     */
    public void followUser(String otherId, Consumer<Boolean> c) {
        callCloudFunctionSimple(buildCloudURL(String.format("followUser?uid=%s&oid=%s", userId, otherId)), c);
    }


    /**
     * unfollow a user
     * Usage:
     * <pre><code>
     *   Database.get(this).unfollowUser("", b -> {
     *      // here, b is a boolean. It is true if the unfollow was successful,
     *      // otherwise it is false. This value should be checked, and shown to the user
     *      // Perhaps with a snackbar, or toast
     *      Log.d("JDBCLOUD", "Attempted to follow" + b));
     *   }
     * </code></pre>
     * @param otherId the user to follow
     * @param c the callback function
     */
    public void unFollowUser(String otherId, Consumer<Boolean> c) {
        callCloudFunctionSimple(buildCloudURL(String.format("followUser?uid=%s&oid=%s", userId, otherId)), c);
    }

    private String buildCloudURL(String end) {
        return String.format("%s/%s", cloudRoot, end);
    }

    private void callCloudFunctionSimple(String url, Consumer<Boolean> c) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (c != null) c.accept(true);
                }, error -> {
                    if (c != null) c.accept(false);
                    Log.d("JDBCLOUD", "Failed request: " + url);
                });
        queue.add(stringRequest);
    }

    private void callCloudFunctionForString(String url, Consumer<String> c) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (c != null) c.accept(response);
                }, error -> {
            Log.d("JDBCLOUD", "Failed request: " + url);
        });
        queue.add(stringRequest);
    }

}

