package com.gittfo.moodtracker.views.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.gittfo.moodtracker.database.Database;
import com.gittfo.moodtracker.mood.Mood;
import com.gittfo.moodtracker.mood.MoodEvent;
import com.gittfo.moodtracker.views.InboxActivity;
import com.gittfo.moodtracker.views.MainActivity;
import com.gittfo.moodtracker.views.R;
import com.gittfo.moodtracker.views.TimelineActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;
import java.util.List;

/**
 * An activity where users can view the locations of a list of MoodEvents on a map. Can be used for
 * an individual's history, complete or filtered, as well as their friends'.
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // tag for passing in a mood history wrapper through intent extras
    public static final String MOOD_HISTORY_WRAPPER = "MOOD_HISTORY_WRAPPER";
    // list of mood events to show on the map. source-agnostic, so we can use this for a personal
    // history, or a friend's history, or whatever
    private ArrayList<MoodEvent> moodEvents;
    // level to zoom to once marker is clicked
    private float ONCLICK_ZOOMLVL = 17; // about usual map height
    // our instamce of the googlemap
    private GoogleMap googleMap;
    // keep track of placed markers, since google maps doesn't, and we need to be able to clear them
    private ArrayList<Marker> markers;

    /**
     * Create a new map activity, set the view to the map view and call for a new map to populate
     * it. If we were passed a MoodHistoryWrapper, unwrap it and save the history for displaying.
     *
     * @param savedInstance Reference to the Bundle object passed into the activity
     */
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.maps_screen);

        // we use fragments for now, not a full on map view
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.mood_map);
        mapFragment.getMapAsync(this);

        // get the mood history
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            MoodHistoryWrapper wrapper = bundle.getParcelable(MOOD_HISTORY_WRAPPER);
            moodEvents = wrapper.getMoodEventList();
        }

        markers = new ArrayList<>();
    }

    /**
     * When "getMapAsync" returns a GoogleMap, populate it with our MoodHistory locations.
     *
     * @param googleMap the Map to display mood events on.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        onMyLocations(null);
    }

    /**
     * When the "My Locations" button is pressed, get all of this user's moods, and show them.
     * @param view The "My Locations" button.
     */
    public void onMyLocations(View view) {
        // call the database to get our moods
        Database.get(this).getMoods().addOnSuccessListener(moods -> {
            // clear out current moods
            moodEvents.clear();
            for(MoodEvent ev : moods) {
                // add events to the mood history
                moodEvents.add(ev);
            }
            moodEvents.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            // show moods on the map
            showMoodEvents(moodEvents);
        });
    }

    /**
     * When the "Followed User Locations" button is pressed, get all of our followee's moods, and
     * show them.
     * @param view The "Followed User Locations" button.
     */
    public void onFolloweeLocations(View view) {
        Database.get(this).getFolloweeMoods(
                moods -> {
                    moodEvents.clear();
                    moodEvents.addAll(moods);
                    showMoodEvents(moodEvents);
                }
        );
    }

    /**
     * Turn a drawable into a bitmap, so that it can be displayed on a google maps marker.
     * This is used to draw mood icons onto the map.
     * @param id id of the drawable.
     * @param color color to tint the drawable.
     * @return a BitmapDescriptor for the new bitmap.
     */
    private BitmapDescriptor fromDrawable(int id, int color) {
        // use the id to get the drawable
        Drawable drawable = ContextCompat.getDrawable(this, id);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        // make a new bitmap
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        // draw the drawable onto the bitmap
        Canvas canvas = new Canvas(bitmap);
        drawable.setTint(color);
        //drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Display a list of mood events as markers on the google map.
     * @param moodEventList a List of MoodEvents to display.
     */
    public void showMoodEvents(List<MoodEvent> moodEventList) {
        // clear out the current markers
        for (Marker m : markers) {
            m.remove();
        }
        markers.clear();

        // if there's no mood events with a valid location, don't show anything
        if (!moodEventList
                .stream()
                .anyMatch(m -> !(Double.isNaN(m.getLatitude()) || Double.isNaN(m.getLongitude()))))
        {
            clearInfoBox();
            return;
        }

        // for aesthetics, move the camera to the most recent mood event
        LatLng lastPos = null;
        for (MoodEvent moodEvent : moodEventList) {

            if (Double.isNaN(moodEvent.getLatitude()) || Double.isNaN(moodEvent.getLongitude())) {
                // this mood doesn't have a valid location
                continue;
            }

            LatLng moodLocation = new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.title("title");

            markerOptions.position(new LatLng(moodEvent.getLatitude(), moodEvent.getLongitude()));
            Mood mood = Mood.moodFromEmotionalState(moodEvent.getMood());

            markerOptions.icon(fromDrawable(mood.getEmoticon(), mood.getColor()));

            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(moodEvent);

            markers.add(marker);

            updateInfoBox(moodEvent, moodLocation);

            lastPos = moodLocation;
        }
        if (lastPos != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastPos));
        }
    }

    /**
     * Turn a LatLng into a short readable string.
     * @param pos the LatLng to convert.
     * @return a short string representation of the LatLng.
     */
    public String posToString(LatLng pos) {
        double lat = pos.latitude;
        double lon = pos.longitude;

        return String.format("%.2f, %.2f", lat, lon);
    }

    /**
     * When a marker is clicked, update the info box and zoom in.
     * @param marker the Marker that was clicked.
     * @return
     */
    public boolean onMarkerClick(final Marker marker) {

        // show the mood info
        MoodEvent moodEvent = (MoodEvent) marker.getTag();
        updateInfoBox(moodEvent, marker.getPosition());

        // zoom in
        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(marker.getPosition(), ONCLICK_ZOOMLVL));

        return true;
    }

    /**
     * Update the little mood info display with information.
     * @param moodEvent the MoodEvent whose information will be presented.
     * @param pos the LatLng of this MoodEvent
     */
    private void updateInfoBox(MoodEvent moodEvent, LatLng pos) {
        TextView usernameView = findViewById(R.id.map_user_name);

        String username = moodEvent.getUsername();
        if (username == null) {
            // mood event had null username, so it wasn't pulled from the followee list, so it's
            // the local user.
            username = Database.get(this).getUserName();
        }
        usernameView.setText(username);

        TextView location = findViewById(R.id.map_location_text);
        location.setText(posToString(pos));

        ImageView emoticonView = findViewById(R.id.map_emoticon);

        Mood mood = Mood.moodFromEmotionalState(moodEvent.getMood());
        emoticonView.setImageResource(mood.getEmoticon());
        emoticonView.setColorFilter(mood.getColor());

        TextView moodText = findViewById(R.id.map_mood_text);
        moodText.setTextColor(mood.getColor());
        moodText.setText(moodEvent.getMood().toString().toLowerCase());
    }

    /**
     * Clear out the mood information box.
     * Used, for instance, when the user attempts to display an empty mood list.
     */
    public void clearInfoBox() {
        TextView usernameView = findViewById(R.id.map_user_name);
        usernameView.setText("Nothing to display.");

        TextView location = findViewById(R.id.map_location_text);
        location.setText("");

        ImageView emoticonView = findViewById(R.id.map_emoticon);

        emoticonView.setImageResource(android.R.color.transparent);

        TextView moodText = findViewById(R.id.map_mood_text);
        moodText.setText("");
    }

    /**
     * When the "timeline" button is pressed, go to the inbox-managing activity.
     * @param view the Inbox button.
     */
    public void startTimelineActivity(View view) {
        // don't animate transition between activities
        Intent i = new Intent(this, TimelineActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(i);
    }

    /**
     * When the "inbox" button is pressed, go to the inbox activity.
     * @param view the Inbox button.
     */
    public void startInboxActivity(View view) {
        // don't animate transition between activities
        Intent i = new Intent(this, InboxActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(i);
    }

    /**
     * When the "profile" button is pressed, go to the inbox activity.
     * @param view the Inbox button.
     */
    public void startProfileActivity(View view){
        // don't animate transition between activities
        Intent i = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        this.startActivity(i);
    }

    /**
     * When the "map" button is pressed,  don't do anything (since we're already in MapActivity)
     * @param view the Map button.
     */
    public void startMapActivity(View view) {
    }
}
