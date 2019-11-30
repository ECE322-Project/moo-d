package com.gittfo.moodtracker.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.gittfo.moodtracker.views.addmood.AddMoodEventActivity;
import com.gittfo.moodtracker.views.map.MapActivity;
import com.gittfo.moodtracker.views.map.MoodHistoryWrapper;

/**
 * This class implements a BottomAppBar (BAB) that functions as a navbar. It appears on most screens of
 * the app, and allows users to easily move between the most important activities.
 */
public class BottomAppBar {

    private final Activity act;
    public static int DEFAULT_THEME_ID = R.style.AppTheme;
    public static int[] themes = new int[] {
                R.style.AppTheme,
                R.style.NeonTheme,
                R.style.MonochromeTheme,
                R.style.PastelTheme,
                R.style.DarkTheme1,
                R.style.DarkTheme2,
    };

    private ImageButton dropDownButton;
    private ColorSchemeDialog colorDialog;

    /**
     * Create the BottomAppBar
     *
     * @param act The activity in which the BAB should be displayed
     */
    public BottomAppBar(Activity act) {
        DEFAULT_THEME_ID = act.getSharedPreferences("colors", Context.MODE_PRIVATE).getInt("theme", R.style.AppTheme);
        act.setTheme(DEFAULT_THEME_ID);
        this.act = act;
        colorDialog = new ColorSchemeDialog(act);
        colorDialog.setOnClicker(c -> applyColorScheme());
    }

    /**
     * Set up the nav buttons so that they point to the appropriate activities
     */
    public void setListeners() {
        setListener(R.id.timeline_menu_item, v -> startTimelineActivity());
        setListener(R.id.inbox_menu_item, v -> startInboxActivity());
        setListener(R.id.maps_menu_item, v -> startMapActivity());
        setListener(R.id.profile_menu_item, v -> startProfileActivity());

        setListener(R.id.fab, v -> createMoodEvent());
        setListener(R.id.settings_button, v -> dropdownPressed());
    }

    /**
     * Set an OnClickListener for a given item
     *
     * @param item An integer representing the item to set the listener for
     * @param fn   The method that should be carried out on click
     */
    private void setListener(int item, View.OnClickListener fn) {
        try {
            act.findViewById(item).setOnClickListener(fn);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * When the New MoodEvent Button (the '+' icon in the bottom-middle of the screen) is clicked,
     * pass the user through to AddMoodEventActivity.
     */
    private void createMoodEvent() {
        Intent i = new Intent(act, AddMoodEventActivity.class);
        act.startActivity(i);
    }

    /**
     * When the "timeline" button is pressed, go to the inbox-managing activity.
     */
    private void startTimelineActivity() {
        // don't animate transition between activities
        if (!(act instanceof TimelineActivity)) {
            Intent i = new Intent(act, TimelineActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            act.startActivity(i);
        }
    }

    /**
     * When the "inbox" button is pressed, go to the inbox activity.
     */
    private void startInboxActivity() {
        if (!(act instanceof InboxActivity)) {
            Intent i = new Intent(act, InboxActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            act.startActivity(i);
        }
    }

    /**
     * When the "profile" button is pressed, go to the profile activity (MainActivity.java).
     */
    private void startProfileActivity(){
        if (!(act instanceof MainActivity)) {
            Intent i = new Intent(act, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            act.startActivity(i);
        }
    }

    /**
     * When the "map" button is pressed, go the map-viewing activity.
     */
    private void startMapActivity() {
        if (!(act instanceof MapActivity)) {
            Intent i = new Intent(act, MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            if (this.act instanceof MainActivity) {
                MoodHistoryWrapper wrapper = new MoodHistoryWrapper(((MainActivity) act).getMoodEvents());
                i.putExtra(MapActivity.MOOD_HISTORY_WRAPPER, wrapper);
            }
            act.startActivity(i);
        }
    }

    /**
     * Start the Signin Activity
     */
    private void startSigninActivity(){
        Intent i = new Intent(act, SigninActivity.class);
        i.putExtra("sign out?", true);
        act.startActivity(i);

    }

    /**
     * Start the Username Activity
     */
    private void startUsernameActivity() {
        Intent i = new Intent(act, ChangeUsernameActivity.class);
        act.startActivity(i);
    }

    /**
     * Handle the dropdown button (The menu icon in the top-right corner of the screen (in most
     * activities).
     */
    private void dropdownPressed(){
        dropDownButton = act.findViewById(R.id.settings_button);
        // create a PopupMenu
        PopupMenu popup = new PopupMenu(act, dropDownButton);
        // inflate the popup via xml file
        popup.getMenuInflater()
                .inflate(R.menu.dropdown_menu, popup.getMenu());

        // tie popup to OnMenuItemClickListener
        popup.setOnMenuItemClickListener(menuItem -> {
            switch(menuItem.getItemId()) {
                case (R.id.dropdown_one):
                    // change username
                    startUsernameActivity();
                    break;
                case (R.id.dropdown_two):
                    // change color scheme
                    onChangeColorSchemePressed();
                    break;
                case (R.id.dropdown_three):
                    //TODO: fix log out functionality
                    startSigninActivity();
                    break;
            }
            return true;
        });
        popup.show(); // show popup menu
    }

    /**
     * Handle the ColorScheme button by displaying the ColorSchemeDialog to the user
     */
    private void onChangeColorSchemePressed(){
        colorDialog.show();
    }

    /**
     * Apply the user's selected colour scheme to the app
     */
    private void applyColorScheme() {
        DEFAULT_THEME_ID = themes[colorDialog.getSelectedNum()];
        act.getSharedPreferences("colors", Context.MODE_PRIVATE).edit().putInt("theme", DEFAULT_THEME_ID).commit();
        colorDialog.cancel();

        Intent i = new Intent(act, act.getClass()).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        act.startActivity(i);
    }


}
