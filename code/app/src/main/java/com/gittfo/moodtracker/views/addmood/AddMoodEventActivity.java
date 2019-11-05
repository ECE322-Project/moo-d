package com.gittfo.moodtracker.views.addmood;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gittfo.moodtracker.database.Database;
import com.gittfo.moodtracker.mood.Mood;
import com.gittfo.moodtracker.mood.MoodEvent;
import com.gittfo.moodtracker.views.R;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Allows the user to create a new MoodEvent, or edit an existing one (by passing a MoodEvent
 * through an intent).
 *
 * @author Mark Thomas
 */
public class AddMoodEventActivity extends AppCompatActivity {

    public static final String EDIT_MOOD = "EDIT THE MOODS";
    private static final int HAPPY_INDEX = 0;
    private static final int SAD_INDEX = 1;
    private static final int SURPRISED_INDEX = 2;
    private static final int AFRAID_INDEX = 3;
    private static final int DISGUSTED_INDEX = 4;
    private static final int ANGRY_INDEX = 5;
    private static final int ZERO_SOCIAL_INDEX = 0;
    private static final int ONE_SOCIAL_INDEX = 1;
    private static final int TWOPLUS_SOCIAL_INDEX = 2;
    private static final int CROWD_SOCIAL_INDEX = 3;
    private static final int NA_SOCIAL_INDEX = 4;

    // Get the current date and time, which are used when creating a new Mood Event
    private Date date = new Date();
    // Use user input to create a new Mood Event
    private Mood.EmotionalState emotionalState = null;
    private String reason = "";
    private MoodEvent.SocialSituation socialSituation = null;
    private String photoReference = "";
    private String location = "";
    private MoodEvent moodEvent;

    // Buttons representing pre-defined moods and social situations that the user may choose from
    private List<Button> moodButtons;
    private List<Button> socialSituationButtons;

    // EditText that the user can optionally use to attach a reason to the MoodEvent
    private EditText reasonEditText;

    private boolean editing = false;

    /**
     * In the oncreate method, dynamically update the layout as needed and initialize views.
     *
     * @param savedInstanceState Reference to the Bundle object passed into the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide ActionBar so that Mood Event entry is full-screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_add_mood_event);

        // Initialize Mood Buttons
        Button happyButton = findViewById(R.id.happy_mood_button);
        Button sadButton = findViewById(R.id.sad_mood_button);
        Button surprisedButton = findViewById(R.id.surprised_mood_button);
        Button afraidButton = findViewById(R.id.afraid_mood_button);
        Button disgustedButton = findViewById(R.id.disgusted_mood_button);
        Button angryButton = findViewById(R.id.angry_mood_button);
        moodButtons = Arrays.asList(happyButton, sadButton, surprisedButton, afraidButton, disgustedButton, angryButton);

        reasonEditText = findViewById(R.id.reason_entry);

        // Initialize Social Situation Buttons
        Button zeroButton = findViewById(R.id.social_button_zero);
        Button oneButton = findViewById(R.id.social_button_one);
        Button twoPlusButton = findViewById(R.id.social_button_two_plus);
        Button crowdButton = findViewById(R.id.social_button_crowd);
        Button naButton = findViewById(R.id.social_button_na);
        socialSituationButtons = Arrays.asList(zeroButton, oneButton, twoPlusButton, crowdButton, naButton);

        String isEdit = this.getIntent().getStringExtra(EDIT_MOOD);
        if (isEdit != null) {
            editing = true;
            Database.get(this).getMoodByID(isEdit).addOnSuccessListener(moodEvent1 -> {
                this.moodEvent = moodEvent1;
                populateEdit();
                // TODO: Maybe show a loading wheel/spinner?
            });
        }
        // If the user is editing an existing MoodEvent, carry out the appropriate actions

        // Format date and time for display
        Format dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String mDate = dateFormat.format(date);
        Format timeFormat = new SimpleDateFormat("h:mm a");
        String mTime = timeFormat.format(date);

        // Display date and time. If creating a new mood event, the current date and time will be
        // shown. If editing an existing mood event, its date and time will be shown.
        TextView dateDisplay = findViewById(R.id.date_display);
        dateDisplay.setText(mDate);
        TextView timeDisplay = findViewById(R.id.time_display);
        timeDisplay.setText(mTime);
    }

    private void populateEdit() {
        if (editing) {
            // Display the Delete Mood Event button
            Button deleteButton = findViewById(R.id.delete_mood_event_button);
            deleteButton.setVisibility(View.VISIBLE);

            // Display current MoodEvent's date/time
            date = moodEvent.getDate();

            // Display current MoodEvent's emotional state
            emotionalState = moodEvent.getMood();
            switch (emotionalState) {
                case HAPPY:
                    moodButtons.get(HAPPY_INDEX).performClick();
                    break;

                case SAD:
                    moodButtons.get(SAD_INDEX).performClick();
                    break;

                case SURPRISED:
                    moodButtons.get(SURPRISED_INDEX).performClick();
                    break;

                case AFRAID:
                    moodButtons.get(AFRAID_INDEX).performClick();
                    break;

                case DISGUSTED:
                    moodButtons.get(DISGUSTED_INDEX).performClick();
                    break;

                case ANGRY:
                    moodButtons.get(ANGRY_INDEX).performClick();
                    break;
            }

            // Display current MoodEvent's reason
            reasonEditText.setText(moodEvent.getReason());

            // Display current MoodEvent's social situation
            socialSituation = moodEvent.getSocialSituation();
            switch (socialSituation) {
                case ZERO:
                    socialSituationButtons.get(ZERO_SOCIAL_INDEX).performClick();
                    break;

                case ONE:
                    socialSituationButtons.get(ONE_SOCIAL_INDEX).performClick();
                    break;

                case TWOPLUS:
                    socialSituationButtons.get(TWOPLUS_SOCIAL_INDEX).performClick();
                    break;

                case CROWD:
                    socialSituationButtons.get(CROWD_SOCIAL_INDEX).performClick();
                    break;

                case NA:
                    socialSituationButtons.get(NA_SOCIAL_INDEX).performClick();
                    break;
            }

            //TODO: display current MoodEvent's photo
            photoReference = moodEvent.getPhotoReference();
            //TODO: display current MoodEvent's location
            location = moodEvent.getLocation();
        }
    }

    /**
     * Bring up an AlertDialog that allows the user to abort the activity. Nothing will be saved,
     * and any changes will be lost.
     *
     * Called when the user clicks the exit button in the top-right corner of the screen.
     *
     * @param view The view that caused the method to be called
     */
    public void exit(View view) {
        new AlertDialog.Builder(AddMoodEventActivity.this)
                .setTitle("Exit Mood Event Creation")
                .setMessage("Are you sure you want to exit?\n\nAny changes will be lost.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    // If the user confirms that they want to exit, finish the activity
                    // (without saving changes)
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * The user can select a mood by clicking one of the mood buttons.
     *
     * The selected mood button will be highlighted, all other mood buttons will lose colour, and
     * the activity's mood attribute will be set accordingly.
     *
     * @param view The view that caused the method to be called
     */
    public void selectMoodButton(View view) {
        // Deselect all other buttons (remove their colour)
        for (Button button : moodButtons) {
            button.setBackgroundColor(Color.GRAY);
        }

        Button selectedMoodButton = (Button) view;
        String selectedMoodString = selectedMoodButton.getText().toString();

        // Get the selected emotionalState
        emotionalState = Mood.emotionalStateFromString(selectedMoodString);

        // Add the corresponding colour to the button that was just clicked
        Mood selectedMood = Mood.moodFromEmotionalState(emotionalState);
        view.setBackgroundColor(selectedMood.getColor());
    }

    /**
     * When implemented, this method will obtain the user's current GPS coordinates.
     *
     * @param view The view that caused the method to be called
     */
    public void getLocation(View view) {
        ;
    }

    /**
     * The user can select a social situation by clicking one of the social situation buttons.
     *
     * The selected social situation button will be highlighted, all other social situation buttons
     * will lose colour, and the activity's socialSituation attribute will be set accordingly.
     *
     * @param view The view that caused the method to be called
     */
    public void selectSocialSituationButton(View view) {
        // Deselect all other buttons (remove their colour)
        for (Button button : socialSituationButtons) {
            button.setBackgroundColor(Color.GRAY);
        }

        // Get the selected social situation
        String selectedSocialSituationString = ((TextView) view).getText().toString();
        socialSituation = MoodEvent.socialSituationFromString(selectedSocialSituationString);

        // Add colour to the button that was just clicked
        int selectedColor = Color.parseColor("#008577");
        view.setBackgroundColor(selectedColor);
    }

    /**
     * When implemented, this method will allow the user to optionally attach a photo to the current
     * mood event.
     *
     * @param view The view that caused the method to be called
     */
    public void addPhoto(View view) {
        ;
    }

    /**
     * If all user input is valid, save the current MoodEvent and return to the previous screen.
     *
     * @param view The view that caused the method to be called
     */
    public void saveMoodEvent(View view) {
        reason = reasonEditText.getText().toString();
        if (editing) {
            // Update all of the selected MoodEvent's attributes so that they reflect any changes
            moodEvent.setMood(emotionalState);
            moodEvent.setReason(reason);
            moodEvent.setSocialSituation(socialSituation);
            moodEvent.setPhotoReference(photoReference);
            moodEvent.setLocation(location);

            Database.get(this).updateMoodEvent(moodEvent);
        } else {
            // Ensure that the user has selected an emotionalState
            if (emotionalState == null) {
                new AlertDialog.Builder(AddMoodEventActivity.this)
                        .setTitle("Missing Information")
                        .setMessage("Please select a mood.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }

            // Ensure that the user has selected a social situation
            if (socialSituation == null) {
                new AlertDialog.Builder(AddMoodEventActivity.this)
                        .setTitle("Missing Information")
                        .setMessage("Please select a social situation.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }


            // Create the MoodEvent object
            moodEvent = new MoodEvent(location, photoReference, reason, date, socialSituation, emotionalState);

            // add the new mood event to the local mood history
            Log.d("JDB", "Adding new mood of type " + moodEvent.getMood().toString() + " to mood history.");

            Database.get(this).addMoodEvent(moodEvent);
        }
        finish();
    }

    /**
     * Delete the current MoodEvent from the database. Prompts the user for confirmation first.
     * Only available when the user is editing
     *
     * @param view The view that caused the method to be called
     */
    public void deleteMoodEvent(View view) {
        // Delete the current MoodEvent from the database, then exit the activity
        new AlertDialog.Builder(AddMoodEventActivity.this)
                .setTitle("Delete Mood Event")
                .setMessage("Are you sure you want to delete this Mood Event?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Database.get(AddMoodEventActivity.this).deleteMoodEvent(moodEvent);
                    finish();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return;
    }
}
