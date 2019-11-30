package com.gittfo.moodtracker.views;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.gittfo.moodtracker.mood.MoodEvent;

import java.util.Arrays;

/**
 * This class implements a filter dialog that can be used to filter MoodHistory by emotional state.
 */
public class FilterDialog {
    private AlertDialog filterDialog;
    private boolean[] filterState;
    private Activity c;
    private int selectedCount = 0;

    /**
     * Create a new FilterDialog object
     *
     * @param c The activity context that the dialog will be created in
     */
    public FilterDialog(Activity c) {
        this.c = c;
        filterState = new boolean[]{true, true, true, true, true, true};
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        LayoutInflater inflater = c.getLayoutInflater();


        builder.setView(inflater.inflate(R.layout.dialog_filter, null));
        Log.d("JUI", "Making builder");
        filterDialog = builder.create();
    }

    /**
     * Display the FilterDialog and set OnClickListeners for each of the mood buttons.
     */
    public void show() {
        filterDialog.show();
        filterDialog.findViewById(R.id.happy_mood_button).setOnClickListener(v -> this.selectMoodButton(v));
        filterDialog.findViewById(R.id.sad_mood_button).setOnClickListener(v -> this.selectMoodButton(v));
        filterDialog.findViewById(R.id.surprised_mood_button).setOnClickListener(v -> this.selectMoodButton(v));
        filterDialog.findViewById(R.id.angry_mood_button).setOnClickListener(v -> this.selectMoodButton(v));
        filterDialog.findViewById(R.id.disgusted_mood_button).setOnClickListener(v -> this.selectMoodButton(v));
        filterDialog.findViewById(R.id.afraid_mood_button).setOnClickListener(v -> this.selectMoodButton(v));
        this.selectedCount = 0;
        for (int i = 0; i < 6; i++){
            if (filterState[i]) {
                this.selectedCount++;
            }
        }
        if (selectedCount == 6) {
            selectedCount = 0;
        }

        updateFilterUI();
    }

    /**
     * Handle mood button selection
     *
     * @param v The mood button that the user clicked
     */
    public void selectMoodButton(View v) {
        Log.d("JUI", "Selected Button");
        int index;
        if (v.getId() == R.id.happy_mood_button) {
            index = MoodEvent.HAPPY_INDEX;
        } else if (v.getId() == R.id.sad_mood_button) {
            index = MoodEvent.SAD_INDEX;
        } else if (v.getId() == R.id.angry_mood_button) {
            index = MoodEvent.ANGRY_INDEX;
        } else if (v.getId() == R.id.afraid_mood_button) {
            index = MoodEvent.AFRAID_INDEX;
        } else if (v.getId() == R.id.disgusted_mood_button) {
            index = MoodEvent.DISGUSTED_INDEX;
        } else if (v.getId() == R.id.surprised_mood_button) {
            index = MoodEvent.SURPRISED_INDEX;
        } else {
            index = 0;
            Log.d("JUI", "Error, invalid view, assuming happy");
        }
        Log.d("JUI", "Picked a view: " + index);
        if (isAllSet() && selectedCount == 0) {
            selectedCount++;
            for (int i = 0; i < 6; i++) {
                filterState[i] = i == index;
            }
        } else {
            selectedCount += filterState[index] ? -1 : 1;
            filterState[index] ^= true;
        }
        if (isNoneSet()) {
            for (int i = 0; i < 6; i++) {
                filterState[i] = true;
            }
        }
        Log.d("JUI", isNoneSet()+":"+Arrays.toString(filterState)+":"+isAllSet()+"sc="+selectedCount);
        updateFilterUI();
    }

    /**
     * Update the filter dialog according to the buttons that have been selected.
     */
    private void updateFilterUI() {
        // The order is important here. This isn't ideal, it needs
        // to reflect the MOOD_INDEX values in MoodEvent
        View[] buttons = new View[]{
                filterDialog.findViewById(R.id.happy_mood_button),
                filterDialog.findViewById(R.id.sad_mood_button),
                filterDialog.findViewById(R.id.surprised_mood_button),
                filterDialog.findViewById(R.id.afraid_mood_button),
                filterDialog.findViewById(R.id.disgusted_mood_button),
                filterDialog.findViewById(R.id.angry_mood_button),
        };

        ColorStateList[] tints = new ColorStateList[]{
                ContextCompat.getColorStateList(c, R.color.colorHappy),
                ContextCompat.getColorStateList(c, R.color.colorSad),
                ContextCompat.getColorStateList(c, R.color.colorSurprised),
                ContextCompat.getColorStateList(c, R.color.colorAfraid),
                ContextCompat.getColorStateList(c, R.color.colorDisgusted),
                ContextCompat.getColorStateList(c, R.color.colorAngry)
        };
        ColorStateList notSelectedTint = ContextCompat.getColorStateList(c, R.color.design_default_color_background);

        boolean allSet = isAllSet();
        for (int i = 0; i < 6; i++) {
            buttons[i].setBackgroundTintList(filterState[i] && (selectedCount == 6 || !allSet) ? tints[i] : notSelectedTint);
        }
    }

    /**
     * Determine whether all mood buttons have been selected
     *
     * @return True if all mood buttons have been selected; otherwise False
     */
    public boolean isAllSet() {
        boolean allSet = true;
        for (int i = 0; i < 6; i++){
            allSet &= filterState[i];
        }
        return allSet;
    }

    /**
     * Determine whether no mood buttons have been selected
     *
     * @return True if no mood buttons have been selected; otherwise False
     */
    public boolean isNoneSet() {
        for (int i = 0; i < 6; i++){
            if (filterState[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Close the FilterDialog.
     */
    public void cancel() {
        filterDialog.cancel();
    }

    /**
     * Returns true if the mood is currently selected.
     *
     * @param ordinal Int representing each of the mood buttons
     * @return Whether or not the mood is currently selected
     */
    public boolean isFiltered(int ordinal) {
        return this.filterState[ordinal];
    }

    /**
     * Set all of the filters to true (i.e. display all moods)
     */
    public void setAllSet() {
        for (int i = 0; i < 6; i++){
            filterState[i] = true;
        }
    }
}
