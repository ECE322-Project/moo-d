package com.gittfo.moodtracker.mood;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.gittfo.moodtracker.database.Database;
import com.gittfo.moodtracker.views.R;


/**
 * Provide a reference to the views for each data item. Complex data items may need more than one
 * view per item, and you provide access to all the views for a data item in a view holder.
 *
 * Each MoodEvent gets it's own MoodViewHolder, which is what gets displayed in the RecyclerView.
 */
public class MoodViewHolder extends RecyclerView.ViewHolder {

    // layout to hold attributes of a mood event
    // TODO: make private
    public LinearLayout container;
    // TODO: make private
    public String moodEventID;

    /**
     * Makes a new MoodViewHolder, sets it's container.
     *
     * @param layout the view which will contain a mood event.
     */
    public MoodViewHolder(LinearLayout layout) {
        super(layout);
        container = layout;
    }

    /**
     * Populate the container with the attributes of a mood event.
     *
     * @param moodEvent the moodEvent whose attributes will be shown.
     */
    public void populateMoodEventContainer(MoodEvent moodEvent){
        this.moodEventID = moodEvent.getId();
        Mood mood = Mood.moodFromEmotionalState(moodEvent.getMood());
        int moodColor = mood.getColor();

        // Add the MoodEvent's emotional state (with the appropriate colour)
        TextView eventMood = container.findViewById(R.id.user_mood_textView);
        eventMood.setText(moodEvent.getMood().toString());
        eventMood.setTextColor(moodColor);

        // Add the MoodEvent's reason
        TextView eventReason = container.findViewById(R.id.user_reason_textView);
        eventReason.setText(moodEvent.getReason());

        // Add the MoodEvent's location
        TextView eventLocation = container.findViewById(R.id.location_textView);
        eventLocation.setText(String.format("%f, %f", moodEvent.getLatitude(), moodEvent.getLongitude()));

        // Add the MoodEvent's date/time
        TextView eventDate = container.findViewById(R.id.date_time_textView);
        eventDate.setText(moodEvent.getDate() != null ? moodEvent.getDate().toString() : "NULL");

        // Add the MoodEvent's social situation
        TextView eventSocialSituation = container.findViewById(R.id.num_people_textView);
        eventSocialSituation.setText(moodEvent.getSocialSituation().toString().toLowerCase());

        // Add the MoodEvent's mood emoticon (with the appropriate colour)
        ImageView eventEmoticon = container.findViewById(R.id.user_emotion_imageView);
        eventEmoticon.setImageResource(mood.getEmoticon());
        eventEmoticon.setColorFilter(mood.getColor());

        // Set the MoodEvent's photo
        if (moodEvent.getPhotoReference() != null && moodEvent.getPhotoReference() != "") {
            Database.get(container.getContext()).downloadImage(moodEvent.getPhotoReference(), image -> {
                Log.d("JUI", "Got an Image for mood: "+ moodEvent.toString());
                if (image != null) {
                    final int scaledHeight = 150;
                    int scaledWidth = (int) (((double)scaledHeight) / ((double)image.getHeight()) * ((double)image.getWidth()));
                    ((ImageView) container.findViewById(R.id.user_image)).setImageBitmap(
                            Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false)
                    );
                }
            });
        }
    }


}
