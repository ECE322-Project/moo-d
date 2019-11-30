package com.gittfo.moodtracker.views.addmood;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gittfo.moodtracker.database.Database;
import com.gittfo.moodtracker.mood.Mood;
import com.gittfo.moodtracker.mood.MoodEvent;
import com.gittfo.moodtracker.views.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.gittfo.moodtracker.mood.MoodEvent.AFRAID_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.ANGRY_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.CROWD_SOCIAL_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.DISGUSTED_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.HAPPY_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.NA_SOCIAL_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.ONE_SOCIAL_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.SAD_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.SURPRISED_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.TWOPLUS_SOCIAL_INDEX;
import static com.gittfo.moodtracker.mood.MoodEvent.ZERO_SOCIAL_INDEX;

/**
 * Allows the user to create a new MoodEvent, or edit an existing one (by passing a MoodEvent
 * through an intent).
 *
 * If the user chooses to save the MoodEvent, the data will be saved to the database.
 */
public class AddMoodEventActivity extends AppCompatActivity  {

    public static final String EDIT_MOOD = "EDIT THE MOODS";
    private static final int RESULT_LOAD_IMG = 1;
    private static final int IMAGE_HEIGHT = 150;

    private static int DEFAULT_THEME_ID = R.style.AppTheme;

    // For getting the location
    private FusedLocationProviderClient fusedLocationClient;

    private Mood.EmotionalState emotionalState = null;

    // The Mood Event that's currently being changed
    private MoodEvent moodEvent;

    // Buttons representing pre-defined moods and social situations that the user may choose from
    private List<Button> moodButtons;
    private List<Button> socialSituationButtons;

    // EditText that the user can optionally use to attach a reason to the MoodEvent
    private EditText reasonEditText;

    // the following enables image adding functionality
    private ImageView photoView;
    private LinearLayoutCompat photoInfo;
    private Button addPhotoButton;

    private boolean editing = false;
    private boolean addLocation = false;

    /**
     * In the oncreate method, dynamically update the layout as needed and initialize views.
     * Determine if the user is creating a new MoodEvent, or editing an existing one.
     *
     * If editing, obtain the MoodEvent from the database, and prepopulate the form with its data.
     *
     * @param savedInstanceState Reference to the Bundle object passed into the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(DEFAULT_THEME_ID);
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

        // Initialize photo ImageView
        photoView = findViewById(R.id.user_photo_preview);
        photoInfo = findViewById(R.id.photo_info);
        addPhotoButton = findViewById(R.id.add_photo_button);

        // Initialize Social Situation Buttons
        Button zeroButton = findViewById(R.id.social_button_zero);
        Button oneButton = findViewById(R.id.social_button_one);
        Button twoPlusButton = findViewById(R.id.social_button_two_plus);
        Button crowdButton = findViewById(R.id.social_button_crowd);
        Button naButton = findViewById(R.id.social_button_na);
        socialSituationButtons = Arrays.asList(zeroButton, oneButton, twoPlusButton, crowdButton, naButton);

        naButton.setBackgroundColor(Color.parseColor("#008577"));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // make an empty mood event
        this.moodEvent = new MoodEvent(
                null,
                null,
                new Date(),
                MoodEvent.SocialSituation.NA,
                null,
                Double.NaN,
                Double.NaN
        );

        // If editing, obtain the MoodEvent from the database
        String isEdit = this.getIntent().getStringExtra(EDIT_MOOD);
        if (isEdit != null) {
            editing = true;
            Database.get(this).getMoodByID(isEdit).addOnSuccessListener(moodEvent1 -> {
                this.moodEvent = moodEvent1;
                populateEdit();
                // TODO: Maybe show a loading wheel/spinner?
            });
        } else {
            getDeviceLocation(); // Gets the location
        }

        // Format date and time for display
        Format dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String mDate = dateFormat.format(moodEvent.getDate());
        Format timeFormat = new SimpleDateFormat("h:mm a");
        String mTime = timeFormat.format(moodEvent.getDate());

        // Display date and time. If creating a new mood event, the current date and time will be
        // shown. If editing an existing mood event, its date and time will be shown.
        TextView dateDisplay = findViewById(R.id.date_display);
        dateDisplay.setText(mDate);
        TextView timeDisplay = findViewById(R.id.time_display);
        timeDisplay.setText(mTime);
    }

    /**
     * If the user is editing a pre-existing MoodEvent, then populate the form with that MoodEvent's data.
     */
    private void populateEdit() {
        if (editing) {
            // Display the Delete Mood Event button
            Button deleteButton = findViewById(R.id.delete_mood_event_button);
            deleteButton.setVisibility(View.VISIBLE);

            // Reuse this code here to catch any delay when fetching a MoodEvent from the database
            // Format date and time for display
            Format dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            String mDate = dateFormat.format(moodEvent.getDate());
            Format timeFormat = new SimpleDateFormat("h:mm a");
            String mTime = timeFormat.format(moodEvent.getDate());

            // Display date and time. If creating a new mood event, the current date and time will be
            // shown. If editing an existing mood event, its date and time will be shown.
            TextView dateDisplay = findViewById(R.id.date_display);
            dateDisplay.setText(mDate);
            TextView timeDisplay = findViewById(R.id.time_display);
            timeDisplay.setText(mTime);

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
            switch (moodEvent.getSocialSituation()) {
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

            // display current MoodEvent's photo

            Database.get(this).downloadImage(this.moodEvent.getPhotoReference(), bitmap -> {
                final int scaledHeight = IMAGE_HEIGHT;
                int scaledWidth = (int) (((double)scaledHeight) / ((double)bitmap.getHeight()) * ((double)bitmap.getWidth()));
                photoView.setImageBitmap(
                        Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
                );
                photoInfo.setVisibility(View.VISIBLE);
                addPhotoButton.setText("Choose a Different Photo");
            });
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
        this.addLocation = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            findViewById(R.id.save_mood_event_button).setEnabled(false);


        }
        getDeviceLocation();

        Log.d("JLOC", "Using Location");
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
        this.moodEvent.setSocialSituation(
                MoodEvent.socialSituationFromString(selectedSocialSituationString)
        );

        // Add colour to the button that was just clicked
        int selectedColor = Color.parseColor("#008577");
        view.setBackgroundColor(selectedColor);
    }

    /**
     * Applies a series of checks to the currently entered mood Event.
     * If a check fail, an error dialog is raised, and returns false
     * @return boolean if moodEvent isn't finished
     */
    private boolean failedMoodEventChecks(){
        // Validate reason
        if (!validReason(this.moodEvent.getReason())){
            new AlertDialog.Builder(AddMoodEventActivity.this)
                    .setTitle("Invalid Reason")
                    .setMessage("Please ensure that your provided Reason is less than 20 characters and less than 3 words.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        }

        // Ensure that the user has selected an emotionalState
        if (emotionalState == null) {
            new AlertDialog.Builder(AddMoodEventActivity.this)
                    .setTitle("Missing Information")
                    .setMessage("Please select a mood.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        }

        // Ensure that the user has selected a social situation
        if (this.moodEvent.getSocialSituation() == null) {
            new AlertDialog.Builder(AddMoodEventActivity.this)
                    .setTitle("Missing Information")
                    .setMessage("Please select a social situation.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        }

        // All checks passed
        return false;
    }

    /**
     * Update this class' moodEvent with all values from the GUI if applicable
     */
    private void syncMoodEventWithGUI(){
        moodEvent.setReason(reasonEditText.getText().toString());
        moodEvent.setMood(emotionalState);
        if(addLocation){
            getDeviceLocation();
        }
        moodEvent.setLatitude(addLocation ? this.moodEvent.getLatitude() : Double.NaN);
        moodEvent.setLongitude(addLocation ? this.moodEvent.getLongitude() : Double.NaN);
    }

    /**
     * Add moodEvent to the database or update a moodEvent in the database
     * depending if this activity is updating or creating a mood Event
     */
    private void pushMoodEventToDatabase(){
        if (editing) {
            // Save any changes to the MoodEvent to the database
            Database.get(this).updateMoodEvent(moodEvent);
        } else {
            // Create the MoodEvent object
            Log.d("JDB", "Adding new mood of type " + moodEvent.getMood().toString() + " to mood history.");
            Database.get(this).addMoodEvent(moodEvent);
        }
    }

    /**
     * If all user input is valid, save the current MoodEvent and return to the previous screen.
     *
     * @param view The view that caused the method to be called
     */
    public void saveMoodEvent(View view) {
        syncMoodEventWithGUI();
        if(failedMoodEventChecks()){
            return;
        }
        pushMoodEventToDatabase();
        finish();
    }

    /**
     * Validates that a user-provided reason is <= 20 chars and has <= 3 words
     * @param reason The reason provided by the user
     * @return Whether the user-provided reason is of valid length
     */
    private boolean validReason(String reason){
        return (reason.split("\\s+").length <= 3) && (reason.length() <= 20);
    }

    /**
     * Get the location of the user's device; this location will be added to the created MoodEvent
     */
    private void getDeviceLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    Log.d("JLOC", "Got a location Object");
                    if (location != null) {
                        // Store this location
                        this.moodEvent.setLongitude(location.getLongitude());
                        this.moodEvent.setLatitude(location.getLatitude());
                        Log.d("JLOC", this.moodEvent.getLongitude() + " : " + this.moodEvent.getLatitude());
                    }
                    findViewById(R.id.save_mood_event_button).setEnabled(true);
                });
        Log.d("JLOC", "Requesting Location");
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
    }

    /**
     * This method allows the user to (optionally) attach a photo to the current mood event.
     *
     * @param v The view that caused the method to be called
     */
    public void addPhoto(View v) {
        // use Android's photoPicker UI
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    /**
     * This method allows the user to remove the photo attached to the current mood event.
     *
     * @param v The view that caused the method to be called
     */
    public void removePhoto(View v) {
        this.moodEvent.setPhotoReference("");
        photoView.setImageResource(android.R.color.transparent);
        photoInfo.setVisibility(View.GONE);
        addPhotoButton.setText("Add a Photo (optional)");
    }

    /**
     * This method is used to ensure that user photos have been properly uploaded to the database.
     *
     * @param reqCode    The request code that was passed to startActivityForResult()
     * @param resultCode The result code specified by the second activity
     * @param data       The result data
     */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    Log.v("JUI", "Bitmap: " + selectedImage.toString() + ". Isnull?=" + (bitmap == null));
                    final int scaledHeight = IMAGE_HEIGHT;
                    int scaledWidth = (int) (((double)scaledHeight) / ((double)bitmap.getHeight()) * ((double)bitmap.getWidth()));
                    photoView.setImageBitmap(
                            Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
                    );
                    addPhotoButton.setText("Choose a Different Photo");
                    photoInfo.setVisibility(View.VISIBLE);
                    String s = Database.get(this).uploadImage(bitmap, didWork -> {
                        if (didWork) {
                            Log.i("JUI", "Upload Completed");
                        } else {
                            quickSnack("Failed to upload Image, please try again");
                        }
                    });
                    this.moodEvent.setPhotoReference(s);
                } catch (IOException e) {
                    Log.i("JUI", "Some exception " + e);
                }
            } catch (Exception e) {
                quickSnack("Image not uploaded");
                e.printStackTrace();
            }
        }
    }

    /**
     * Display a message using one of Android's snackbars
     * @param msg The message that should be displayed in the Snackbar
     */
    private void quickSnack(String msg) {
        Snackbar.make(findViewById(R.id.add_mood_root), msg, Snackbar.LENGTH_SHORT).show();
    }
}
