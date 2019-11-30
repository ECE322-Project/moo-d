package com.gittfo.moodtracker.database;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Log;

import androidx.core.util.Consumer;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

/**
 * A singleton class for abstracting out firestore
 */
public class Database {
    private static final String TAG = "DB_INTERNAL";
    public static final String PREFS = "databasesharedprefereneces";
    private static final String cloudRoot = "https://us-central1-moo-d-95679.cloudfunctions.net";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static RequestQueue queue;
    private static String username;
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
     * Gets all the mood events for the signed in user
     *
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
                        Log.d(TAG, "Adding " + v.toString());
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
     *
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
     *
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
     *
     * @param callback the callback function
     */
    public void getFolloweeMoods(Consumer<List<MoodEvent>> callback) {
        String url = buildCloudURL(String.format("getFolloweeMoods?uid=%s", userId));
        callCloudFunctionForString(url,
                res -> {
                    Log.v("JDBCLOUD", "Got data:\n" + res);
                    JsonParser j = new JsonParser();
                    JsonArray data = j.parse(res).getAsJsonArray();
                    List<MoodEvent> moods = new ArrayList<>(data.size());
                    for (JsonElement e : data) {
                        if (e.isJsonObject()) {
                            MoodEvent me = MoodEvent.getMoodEventFromJson(e.getAsJsonObject());
                            moods.add(me);
                        }
                    }
                    callback.accept(moods);
                });
        Log.v("JDBCLOUD", url);
    }

    /**
     * Follow a user
     *
     * Usage:
     * <pre><code>
     *   Database.get(this).followUser("", b -> {
     *      // here, b is a boolean. It is true if the follow was successful,
     *      // otherwise it is false. This value should be checked, and shown to the user
     *      // Perhaps with a snackbar, or toast
     *      Log.d("JDBCLOUD", "Attempted to follow" + b));
     *   }
     * </code></pre>
     *
     * @param otherId the user to follow
     * @param c the callback function
     */
    public void followUser(String otherId, Consumer<Boolean> c) {
        callCloudFunctionSimple(buildCloudURL(String.format("followUser?uid=%s&oid=%s", userId, otherId)), c);
    }

    /**
     * unfollow a user
     *
     * Usage:
     * <pre><code>
     *   Database.get(this).unfollowUser("", b -> {
     *      // here, b is a boolean. It is true if the unfollow was successful,
     *      // otherwise it is false. This value should be checked, and shown to the user
     *      // Perhaps with a snackbar, or toast
     *      Log.d("JDBCLOUD", "Attempted to follow" + b));
     *   }
     * </code></pre>
     *
     * @param otherId the user to follow
     * @param c the callback function
     */
    public void unFollowUser(String otherId, Consumer<Boolean> c) {
        callCloudFunctionSimple(buildCloudURL(String.format("unfollowUser?uid=%s&oid=%s", userId, otherId)), c);
    }

    /**
     * Creates the url for the cloud function
     *
     * @param end url stub to attach to prfix
     * @return the completed ukl
     */
    private String buildCloudURL(String end) {
        return String.format("%s/%s", cloudRoot, end);
    }

    /**
     * Makes a http request that either succeeds or fails, does not check the result
     *
     * @param url the url to make a get request on
     * @param c the callback with the result of success or failure
     */
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

    /**
     * Makes an http get request that returns a string
     *
     * @param url the url to get on
     * @param c the callback on success
     */
    private void callCloudFunctionForString(String url, Consumer<String> c) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (c != null) c.accept(response);
                }, error -> {
                    Log.d("JDBCLOUD", "Failed request: " + url);
        });
        queue.add(stringRequest);
    }

    /**
     * Changes the current user's username in firebase
     *
     * @param username the username to set to
     */
    public void setUserName(String username)  {
        HashMap<String, Object> data = new HashMap<>();
        data.put("username", username);
        db.collection("users")
                .document(currentUser())
                .set(data);
        if (getUserName() != null) {
            db.collection("usernames")
                    .document(getUserName())
                    .delete();
        }
        Database.username = username;
        HashMap<String, Object> initialData = new HashMap<>();
        initialData.put("id", userId);
        db.collection("usernames")
                .document(username)
                .set(initialData);
    }

    /**
     * Gets the username synchronously, returning null if the information is not yet available
     *
     * @return The username if available, or null
     */
    public String getUserName() {
       return getUserName(null);
    }

    /**
     * Returns the username synchronously
     * Or null if the username is not yet queried
     *
     * @param callback an optional callback to be called when the data is available
     * @return The username, or null if the username is not yet available
     */
    public String getUserName(Consumer<String> callback) {
        db.collection("users")
                .document(currentUser())
                .get().addOnSuccessListener(documentSnapshot -> {
                    String s = documentSnapshot.getString("username");
                    if (s != null) {
                        Database.username = s;
                    }
                    if (callback != null) {
                        callback.accept(s == null ? null : s.trim());
                    }
                });

        return username;
    }

    /**
     * Check if the given username is already in the database
     *
     * @param username
     * @param cb the callback with the result
     */
    public void isUniqueUsername(String username, Consumer<Boolean> cb){
        db.collection("usernames")
                .document(username)
                .get()
                .addOnSuccessListener(doc -> {
                    Log.d("JDB", doc.toString());
                    cb.accept(!doc.exists());
                })
                .addOnFailureListener(doc -> cb.accept(true));

    }

    /**
     * Uploads an image to firebase, callback for success result
     *
     * @param bitmap the bitmap to upload
     * @param callback the success status
     * @return the string at which the image can be found
     */
    public String uploadImage(Bitmap bitmap, Consumer<Boolean> callback) {
        String name = UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child(name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            if (callback != null)
                callback.accept(false);
        }).addOnSuccessListener(taskSnapshot -> {
            if (callback != null)
                callback.accept(true);
        });

        return name;
    }

    /**
     * Downloads an image from firebase
     *
     * @param loc The storage URL to get from
     * @param callback a function that has the image in Bitmap form, or null if it could not get the image
     */
    public void downloadImage(String loc, Consumer<Bitmap> callback) {
        if (loc == null || loc.isEmpty()) {
            // TODO: maybe do an error?
            return;
        }

        StorageReference imageRef = storage.getReference().child(loc);

        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(10 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            if (callback != null)
                callback.accept(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }).addOnFailureListener(exception -> {
            exception.printStackTrace();
            if (callback != null)
                callback.accept(null);
        });
    }

    /**
     * Gets the userid for a given username
     *
     * @param username the username to get the id for
     * @param callback a callback that will pass through the userid when it finds it, otherwise it will pass through null
     */
    public void getUserIdFromUsername(String username, Consumer<String> callback) {
        db.collection("usernames")
                .document(username)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.accept(doc.getString("id"));
                    } else {
                        callback.accept(null);
                    }
                });
    }

    /**
     * Gets the username for a given users id
     *
     * @param userId the userId to get the username for
     * @param callback a callback that will pass through the userid when it finds it, otherwise it will pass through null
     */
    public void getUsernameFromUserId(String userId, Consumer<String> callback) {
        db.collection("users")
                .document(userId)
                .get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                callback.accept(doc.getString("username"));
            } else {
                callback.accept(null);
            }
        });
    }

    /**
     * Retrieves any outstanding follow requests from the database
     *
     * @param callback a callback that will accept a list of follow requests (represented as strings)
     */
    public void getFollowRequests(Consumer<List<String>> callback) {
        DocumentReference reqs = db.collection("requests")
                .document(currentUser());

        reqs.get().addOnSuccessListener(docs -> {
            Log.d(TAG, "Found the requests");

            List<String> requestInfo = new ArrayList<>();
            Map<String, Object> items = docs.getData();
            if (items != null) {
                for (String obj : (ArrayList<String>) items.get("following")) {
                    requestInfo.add(obj);
                }
            }

            callback.accept(requestInfo);
        });
    }

    /**
     * Respond to a follow request (i.e. allow the other user to follow the current user, or not)
     *
     * @param usrid The userid of the user who sent the follow request
     * @param b True if follow permissions are granted, False if they're denied
     */
    public void completeFollowRequest(String usrid, boolean b) {
        if (b)
            callCloudFunctionSimple(buildCloudURL(String.format("confirmFollowUser?uid=%s&oid=%s", userId, usrid)), null);
        else
            callCloudFunctionSimple(buildCloudURL(String.format("denyFollowUser?uid=%s&oid=%s", userId, usrid)), null);
    }

    /**
     * Get a list of followees for the current user
     *
     * @param callback a callback that will accept a list of followees (represented as strings)
     */
    public void getFollowees(Consumer<List<String>> callback) {
        DocumentReference reqs = db.collection("users")
                .document(currentUser());

        reqs.get().addOnSuccessListener(docs -> {
            Log.d(TAG, "Found the requests");

            List<String> requestInfo = new ArrayList<>();
            Map<String, Object> items = docs.getData();
            if (items != null) {
                ArrayList<String> itmFol = (ArrayList<String>) items.get("following");
                if (itmFol != null) {
                    for (String obj : itmFol) {
                        requestInfo.add(obj);
                    }
                }
            }

            callback.accept(requestInfo);
        });
    }

    /**
     * Initializes data in the database (ensures the current user has a username)
    */
    public void init() {
        getUserName();
    }

}

