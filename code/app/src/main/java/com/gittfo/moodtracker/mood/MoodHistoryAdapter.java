package com.gittfo.moodtracker.mood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.view.LayoutInflater;


import com.gittfo.moodtracker.database.Database;
import com.gittfo.moodtracker.views.R;
import com.gittfo.moodtracker.views.addmood.AddMoodEventActivity;
import com.gittfo.moodtracker.views.moodhistory.MoodViewHolder;

import java.util.ArrayList;

/**
 * This class serves as an adapter, allowing MoodEvents to be properly displayed in a RecyclerView.
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodViewHolder> {

    private ArrayList<MoodEvent> moodHistory;
    private Context context;

    /**
     * Create a new MoodHistoryAdapter.
     *
     * @param moodHistory The MoodHistory that this adapter will hold
     */
    public MoodHistoryAdapter(Context context, ArrayList<MoodEvent> moodHistory){
        this.moodHistory = moodHistory;
        this.context = context;
    }

    /**
     * Create a new MoodViewHolder.
     *
     * @param parent The parent view
     * @param viewType The type of view
     * @return A MoodViewHolder that will be used to hold MoodEvent views
     */
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mood_event, parent, false);

        return new MoodViewHolder(v);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager).
     *
     * Also adds a listener for when a MoodEvent in the RecyclerView is clicked.
     *
     * @param holder The MoodViewHolder that needs to be populated
     * @param position The position of the element from MoodHistory
     */
    @Override
    public void onBindViewHolder(final MoodViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.container.setText(mDataset[position]);
        holder.populateMoodEventContainer(moodHistory.get(position));
        holder.container.findViewById(R.id.edit_button).setOnClickListener(v -> {
            //Context c = moodHistory.getContext();
            Intent i = new Intent(context, AddMoodEventActivity.class);
            i.putExtra(AddMoodEventActivity.EDIT_MOOD, holder.moodEventID);
            context.startActivity(i);
        });
        holder.container.findViewById(R.id.delete_button).setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Mood")
                    .setMessage("Do you really want to delete this Mood Event?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                Database.get(context).deleteMoodEvent(MoodEvent.fromId(holder.moodEventID));
                                moodHistory.remove(position); 
                                notifyDataSetChanged();
                            }
                    )
                    .setNegativeButton(android.R.string.no, null).show();
        });
    }

    /**
     * Get the item count of this Adapter's MoodHistory
     *
     * @return Item count of the MoodHistory
     */
    @Override
    public int getItemCount() {
        return moodHistory.size();
    }
}
