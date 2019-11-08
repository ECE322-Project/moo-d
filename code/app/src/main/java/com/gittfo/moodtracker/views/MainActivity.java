package com.gittfo.moodtracker.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gittfo.moodtracker.database.Database;
import com.gittfo.moodtracker.mood.MoodEvent;
import com.gittfo.moodtracker.mood.MoodHistoryAdapter;
import com.gittfo.moodtracker.views.addmood.AddMoodEventActivity;

import java.util.ArrayList;

/**
 * The main activity of the app. In this activity, the user is able to see their MoodHistory, which
 * includes all of the MoodEvents that they've created.
 */
public class MainActivity extends AppCompatActivity {

    //private MoodHistory moodHistory;
    private RecyclerView moodView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private FilterDialog filterDialog;
    private ArrayList<MoodEvent> moodHistory;

    /**
     * Each time the user returns to this activity, update the RecyclerView with moods from the
     * database.
     */
    @Override
    protected void onResume() {
        super.onResume();
        getFromDB();
    }

    /**
     * In the oncreate method, dynamically update the layout as needed and initialize views.
     *
     * @param savedInstanceState Reference to the Bundle object passed into the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the mood history
        moodHistory = new ArrayList<>();

        // setup the RecyclerView
        moodView = findViewById(R.id.mood_history_view);
        moodView.addItemDecoration(new DividerItemDecoration(moodView.getContext(), DividerItemDecoration.VERTICAL));
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        moodView.setLayoutManager(layoutManager);

        moodHistoryAdapter = new MoodHistoryAdapter(this, moodHistory);
        moodView.setAdapter(moodHistoryAdapter);
        getFromDB();

        filterDialog = new FilterDialog(this);
        // TODO: put on an actual filter button
        findViewById(R.id.filter_button).setOnClickListener(v -> filterDialog.show());
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
        // Get all moods from the database
        Log.d("JDB", "Getting Moods");
        Database.get(this).getMoods().addOnSuccessListener(moods -> {
            moodHistory.clear();
            for(MoodEvent ev : moods) {
                // add events to the mood history
                if (filterDialog.isFiltered(ev.getMood().ordinal()))
                    moodHistory.add(ev);
                Log.d("JDB", ev.toString());
            }
            moodHistory.sort((b, a) -> a.getDate().compareTo(b.getDate()));
            // Update the RecyclerView so that any new moods can be displayed
            moodHistoryAdapter.notifyDataSetChanged();
        });
        Log.d("JDB", "Got Moods");
    }

    /**
     * When the New MoodEvent Button (the '+' icon in the bottom-middle of the screen) is clicked,
     * pass the user through to AddMoodEventActivity.
     *
     * @param view The New MoodEvent Button
     */
    public void createMoodEvent(View view) {
        Intent i = new Intent(this, AddMoodEventActivity.class);
        this.startActivity(i);
    }
}
