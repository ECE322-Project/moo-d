package com.gittfo.moodtracker.views;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.gittfo.moodtracker.database.Database;

/**
 * This class implements a dialog that can be used to allow a user to follow another user, in order
 * to stay up to date with their most recent MoodEvent.
 */
public class FollowDialog {
    private AlertDialog followDialog;
    private EditText userFollowEditText;
    private Button sendRequestButton;
    private Activity c;

    /**
     * Create a new FollowDialog object
     *
     * @param c The activity context that the dialog will be created in
     */
    public FollowDialog(Activity c) {
        this.c = c;
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        LayoutInflater inflater = c.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_follow_user, null);
        builder.setView(layout);

        ImageButton exitButton = layout.findViewById(R.id.exit_button);
        userFollowEditText = layout.findViewById(R.id.user_follow_editText);
        sendRequestButton = layout.findViewById(R.id.send_request_button);

        exitButton.setOnClickListener(v -> followDialog.dismiss());

        sendRequestButton.setOnClickListener(v -> {
            String followee = userFollowEditText.getText().toString();
            sendRequest(followee);
        });

        userFollowEditText.setOnEditorActionListener((p1, p2, p3) -> {
            userFollowEditText.setError("");
            return true;
        });

        Log.d("JUI", "Making builder");
        followDialog = builder.create();
    }

    /**
     * Display the FollowDialog and set onClickListener for the "Send Request" button.
     */
    public void show() {
        followDialog.show();
    }

    /**
     * When the user clicks the "Send Request" button, send a Follow Request to the user whose
     * username has been entered into the username field.
     *
     * @param username The name of the user who will receive a Follow Request
     */
    public void sendRequest(String username) {
        Database.get(this.c).getUserIdFromUsername(username, userId -> {
            Log.d("JDBCLOUD", String.format("Retrieved {%s}'s userid: {%s}", username, userId));

            if (userId == null) {
                Log.d("JDBCLOUD", String.format("{%s} does not exist. Unable to follow.", username, userId));
                userFollowEditText.setError("User not Found");
                return;
            }

            Database.get(this.c).followUser(userId, b -> {
                Log.d("JDBCLOUD", String.format("Attempted to follow {%s} - {%s}", username, b));
                if (b) {
                    followDialog.cancel();
                } else {
                    Toast.makeText(c, "Unable to follow user. Try again later", Toast.LENGTH_LONG);
                }
            });
        });
    }
}
