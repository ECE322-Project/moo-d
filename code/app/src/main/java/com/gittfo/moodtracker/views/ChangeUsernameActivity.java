package com.gittfo.moodtracker.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gittfo.moodtracker.database.Database;

/**
 * An activity for managing a user's profile.
 */
public class ChangeUsernameActivity extends AppCompatActivity {
    private EditText usernameView;
    private Button updateButton;
    private TextView welcomeBox;

    private String initialUsername = "";
    private static int DEFAULT_THEME_ID = R.style.AppTheme;

    /**
     * In the onCreate method, determine whether the user has chosen a username. If not, prompt them
     * to set one before allowing them to see the rest of the app.
     *
     * @param savedInstanceState Reference to the Bundle object passed into the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(DEFAULT_THEME_ID);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        // Hide ActionBar
        getSupportActionBar().hide();

        usernameView = findViewById(R.id.username);
        updateButton = findViewById(R.id.update_username_button);
        welcomeBox = findViewById(R.id.welcome_message);
        welcomeBox.setText("Set your username here");


        Database.get(this).getUserName(username -> {
            if(username == null){
                usernameView.setHint("Please set a username.");
                updateButton.setText("Update Username");
            } else {
                initialUsername = username;
                updateButton.setText("Continue");
                usernameView.setText(username);
                welcomeBox.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * called by onClick of update_user_button Button.
     * Pushes the username from the EditText to the database.
     * Potentially notifies the user if that username already exists.
     *
     * @param view
     */
    public void updateUsername(View view){
        String name = usernameView.getText().toString();

        // protect against empty input
        if (name.equals("")) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid Username")
                    .setMessage("Please enter a username.")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

        Database.get(this).isUniqueUsername(name, isUnique -> {
            Log.d("JDB", String.format("Is username: {%s} unique? = %b", name, isUnique));
            if (name.equals(initialUsername)) {
               startMain();
            } else if (isUnique) {
                Database.get(this).setUserName(name);
                startMain();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Invalid Username")
                        .setMessage("Someone already uses that username, please come up with a new one.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    /**
     * Start MainActivity
     */
    public void startMain(){
        Intent i = new Intent(this, MainActivity.class);
        this.startActivity(i);
    }

}
