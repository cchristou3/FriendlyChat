package com.google.firebase.udacity.friendlychat.LiveData;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.udacity.friendlychat.Adapter.MessageRecyclerViewAdapter;
import com.google.firebase.udacity.friendlychat.View.MainActivity;
import com.google.firebase.udacity.friendlychat.dto.FriendlyMessage;

import java.util.ArrayList;
import java.util.List;

public class FirebaseQueryLiveData extends LiveData<List<FriendlyMessage>> {

    private List<FriendlyMessage> messageList = new ArrayList<>();
    private final CollectionReference collectionReference;
    private MessageRecyclerViewAdapter mMessageRecyclerViewAdapter;
    private Activity mainActivity;

    public void setMessageRecyclerViewAdapter(MessageRecyclerViewAdapter mMessageRecyclerViewAdapter) {
        this.mMessageRecyclerViewAdapter = mMessageRecyclerViewAdapter;
    }

    public FirebaseQueryLiveData(CollectionReference collectionReference) {
        this.collectionReference = collectionReference;
    }

    public void setMainActivity(Activity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Nullable
    @Override
    public List<FriendlyMessage> getValue() {
        return messageList;
    }

    @Override
    protected void onActive() { // When onStart - onResume lifecycle
        super.onActive();
        Log.d(MainActivity.TAG, "LiveData: onActive");

        /* By passing an activity as the first argument of the addSnapshotListener
         *  the event listener will get detached when the activity stops */
        collectionReference.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener(mainActivity, (value, error) -> {
            try {
                if (!value.isEmpty()) {
                /* We empty the list before we re-fetch the data from the Firebase database
                in order to avoid duplicates */
                    messageList.removeAll(messageList);
                    for (DocumentSnapshot document : value.getDocuments()) {
                        // https://firebase.google.com/docs/firestore/query-data/get-data#java_10
                        Log.d(MainActivity.TAG, document.getId() + " => " + document.getData());
                        FriendlyMessage messagesToBeShown = document.toObject(FriendlyMessage.class);
                        messageList.add(messagesToBeShown);
                        mMessageRecyclerViewAdapter.notifyDataSetChanged();
                        mMessageRecyclerViewAdapter.notifyItemInserted(messageList.size() - 1);
                    }
                } else if (error != null) {
                    Log.d(MainActivity.TAG, "Task was unsuccessful! Error: " + error.getMessage());
                }
            } catch (NullPointerException e) {
                Log.d(MainActivity.TAG, "A NullPointerException was thrown: " + "\" -- " + error.getMessage());
            }

        });

    }

    @Override
    protected void onInactive() { // When onPause - onStop lifecycle
        super.onInactive();
        Log.d(MainActivity.TAG, "LiveData: onInactive");

        // Empty the list to avoid inconsistencies in case an error occurs.
        messageList.removeAll(messageList);
    }
}
