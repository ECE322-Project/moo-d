package com.gittfo.moodtracker.views.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.gittfo.moodtracker.mood.Mood;
import com.gittfo.moodtracker.mood.MoodEvent;
import com.gittfo.moodtracker.views.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
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
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    // tag for passing in a mood history wrapper through intent extras
    public static final String MOOD_HISTORY_WRAPPER = "MOOD_HISTORY_WRAPPER";
    // list of mood events to show on the map. source-agnostic, so we can use this for a personal
    // history, or a friend's history, or whatever
    private ArrayList<MoodEvent> moodEvents;

    private GoogleMap googleMap;

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
    }

    /**
     * When "getMapAsync" returns a GoogleMap, populate it with our MoodHistory locations.
     *
     * @param googleMap the Map to display mood events on.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        showMoodEvents(moodEvents);
    }


    private BitmapDescriptor fromDrawable(int id, int color) {
        Drawable drawable = ContextCompat.getDrawable(this, id);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setColorFilter(color, PorterDuff.Mode.DST);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    public void showMoodEvents(List<MoodEvent> moodEventList) {
        // for aesthetics, move the camera to the most recent mood event
        LatLng last = null;
        for (MoodEvent me : moodEventList) {
            LatLng moodLocation = new LatLng(me.getLatitude(), me.getLongitude());
            MarkerOptions mo = new MarkerOptions();
            mo.title(me.getReason());
            mo.position(new LatLng(me.getLatitude(), me.getLongitude()));
            Mood mood = Mood.moodFromEmotionalState(me.getMood());

            mo.icon(fromDrawable(mood.getEmoticon(), mood.getColor()));

            googleMap.addMarker(mo);
            last = moodLocation;
        }
        if (last != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(last));
        }
    }
}
