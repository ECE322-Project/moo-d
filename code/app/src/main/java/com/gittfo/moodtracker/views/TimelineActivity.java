package com.gittfo.moodtracker.views;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gittfo.moodtracker.database.Database;
import com.gittfo.moodtracker.mood.MoodEvent;
import com.gittfo.moodtracker.mood.MoodHistoryAdapter;

import java.util.ArrayList;

/**
 * Contains the most recent MoodEvents of a user's followed users
 */
public class TimelineActivity extends AppCompatActivity {

    private RecyclerView moodView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private FilterDialog filterDialog;
    private ArrayList<MoodEvent> followeesMoods;
    private BottomAppBar bottomAppBar;


    /**
     * In the onCreate method, setup the UI, and pull in any followee's methods from the database
     * (if they exist).
     *
     * @param savedInstanceState Reference to the Bundle object passed into the activity
     */
    protected void onCreate(Bundle savedInstanceState){
        bottomAppBar = new BottomAppBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        bottomAppBar.setListeners();

        // Hide the ActionBar
        getSupportActionBar().hide();

        // Handle Follow Button
        findViewById(R.id.follow_button).setOnClickListener(v -> sendFollowRequest(v));

        // initialize the mood history
        followeesMoods = new ArrayList<>();

        // setup the RecyclerView
        moodView = findViewById(R.id.followed_moodevents_view);
        moodView.addItemDecoration(new DividerItemDecoration(moodView.getContext(), DividerItemDecoration.VERTICAL));
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        moodView.setLayoutManager(layoutManager);

        moodHistoryAdapter = new MoodHistoryAdapter(this, followeesMoods, R.layout.mood_event_timeline);
        moodView.setAdapter(moodHistoryAdapter);
        getFromDB();

        filterDialog = new FilterDialog(this);
        findViewById(R.id.toolbar_filter_button).setOnClickListener(v -> filterDialog.show());
    }

    /**
     * Apply any MoodHistory filters that the user has set
     *
     * @param v The view that the method has been called from
     */
    public void applyFilters(View v) {
        getFromDB();
        filterDialog.cancel();
    }

    /**
     * Show all of the user's MoodEvents (i.e. clear any MoodHistory filters that the user has set)
     *
     * @param v The view that the method has been called from
     */
    public void showAllMoods(View v) {
        filterDialog.setAllSet();
        getFromDB();
    }

    /**
     * Get all moods from the database, then update the RecyclerView in order to display them.
     */
    public void getFromDB() {
        // Get all followee's most recent moods from the database
        Log.d("JDB", "Getting Followee's Most Recent Moods");

        Database.get(this).getFolloweeMoods(moods -> {
            Log.d("JDB", "Success");
            followeesMoods.clear();
            for(MoodEvent ev : moods) {
                // add events to the mood history
                if (filterDialog.isFiltered(ev.getMood().ordinal())) {
                    followeesMoods.add(ev);
                }
                Log.d("JDB", ev.toString());
            }
            followeesMoods.sort((b, a) -> a.getDate().compareTo(b.getDate()));
            // Update the RecyclerView so that any new moods can be displayed
            moodHistoryAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Each time the user returns to this activity, update the RecyclerView with moods from the
     * database.
     */
    @Override
    protected void onResume() {
        super.onResume();
        getFromDB();
    }

    public void sendFollowRequest(View view) {
        FollowDialog followDialog = new FollowDialog(this);
        followDialog.show();
    }

    /**
     * For smoother transitions between activities, disable animations when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }


}
