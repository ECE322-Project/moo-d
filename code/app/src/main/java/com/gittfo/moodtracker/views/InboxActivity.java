package com.gittfo.moodtracker.views;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gittfo.moodtracker.database.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is an activity for users to manage their inbox, containing things like follow requests.
 */
public class InboxActivity extends AppCompatActivity {

    private RecyclerView inboxViews;
    private List<Pair<String, String>> inboxItems;
    private BottomAppBar bottomAppBar;

    /**
     * In the oncreate method, dynamically update the layout as needed, pulling in any outstanding
     * follow requests (if they exist).
     *
     * @param savedInstanceState Reference to the Bundle object passed into the activity
     */
    protected void onCreate(Bundle savedInstanceState){
        bottomAppBar = new BottomAppBar(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        bottomAppBar.setListeners();

        // Hide the ActionBar
        getSupportActionBar().hide();

        inboxItems = new ArrayList<>();
        inboxViews = findViewById(R.id.inbox_items_view);
        inboxViews.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        inboxViews.setLayoutManager(new LinearLayoutManager(this));
        inboxViews.setAdapter(new InboxRCAdapter(inboxItems, this));

        Database.get(this).getFollowRequests(followRequests -> {
            Log.d("JDB", "Got all requests");
            for (String fromUserId : followRequests) {
                Log.d("JDB", "Found a request: " + fromUserId);
                Database.get(this).getUsernameFromUserId(fromUserId, username -> {
                    Log.d("JDB", "This person wants to follow you: " + username);
                    inboxItems.add(new Pair<>(username, fromUserId));
                    inboxViews.getAdapter().notifyDataSetChanged();
                });
            }
        });
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


/**
 * This class manages responding to follow requests
 */
class ResponseDialog {
    private Activity c;
    private String usrname;

    public ResponseDialog(Activity c, String usrname) {
        this.c = c;
        this.usrname = usrname;
    }

    /**
     * Display the ResponseDialog to the user
     *
     * @param callback Callback to handle the User's decision
     */
    public void open(Consumer<Boolean> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        LayoutInflater inflater = c.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_allow_follow_request, null);
        builder.setView(layout);

        ((TextView) layout.findViewById(R.id.requesting_username)).setText(this.usrname);

        AlertDialog ad = builder.create();

        layout.findViewById(R.id.cancel_permission_button).setOnClickListener(v -> ad.cancel());

        layout.findViewById(R.id.allow_button).setOnClickListener(v -> {
            ad.cancel();
            callback.accept(true);
        });
        layout.findViewById(R.id.deny_button).setOnClickListener(v -> {
            ad.cancel();
            callback.accept(false);
        });

        ad.show();
    }
}

/**
 * This class holds Follow Requests (which are displayed in the InboxActivity), and which will be
 * displayed in the InboxActivity.
 */
class InboxRCViewHolder extends RecyclerView.ViewHolder {

    private View layout;

    /**
     * Create an InboxRCViewHolder
     *
     * @param itemView The view for items in the holder
     */
    public InboxRCViewHolder(@NonNull View itemView) {
        super(itemView);
        this.layout = itemView;
    }

    /**
     * Get the layout used for items in the holder
     *
     * @return The layout used for items in the holder
     */
    public View getLayout() {
        return layout;
    }
}


/**
 * This class functions as an adapter, in order to display FollowRequests in the InboxActivity's
 * RecyclerView.
 */
class InboxRCAdapter extends RecyclerView.Adapter<InboxRCViewHolder> {

    private final List<Pair<String, String>> items;
    private Activity c;

    /**
     * Create an InboxRCAdapter
     *
     * @param items A list of <String, String> pairs that will be used in the Adapter
     * @param c     The activity context that the dialog will be created in
     */
    InboxRCAdapter(List<Pair<String, String>> items, Activity c) {
        this.items = items;
        this.c = c;

    }

    /**
     * Inflate the layout for this ViewHolder
     *
     * @param parent   The parent view for this ViewHolder
     * @param viewType The type of view to be inflated
     * @return         The View Holder, with the appropriate inflated view
     */
    @NonNull
    @Override
    public InboxRCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.follow_request_notification, parent, false);
        return new InboxRCViewHolder(v);
    }

    /**
     * Handle interaction with this ViewHolder's items. Namely, respond to Follow Requests.
     *
     * @param holder   The ViewHolder containing items
     * @param position The position of the item in the RecyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull InboxRCViewHolder holder, int position) {
        Log.d("JUI", "Setting inbox for loc: " + position);
        TextView usr = holder.getLayout().findViewById(R.id.follow_request_username);
        usr.setText(items.get(position).first);
        ResponseDialog rd = new ResponseDialog(c, items.get(position).first);
        holder.getLayout().setOnClickListener(v ->
            rd.open(didFollow -> {
                Database.get(c).completeFollowRequest(items.get(position).second, didFollow);
                items.remove(position);
                this.notifyDataSetChanged();
                Log.d("JUI", "Removing old follow");
            }));
    }

    /**
     * Get the number of items in the ViewHolder
     *
     * @return The number of items in the ViewHolder
     */
    @Override
    public int getItemCount() {
        return items.size();
    }
}
