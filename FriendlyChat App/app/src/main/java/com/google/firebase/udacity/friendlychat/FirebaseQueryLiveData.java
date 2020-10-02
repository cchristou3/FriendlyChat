package com.google.firebase.udacity.friendlychat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseQueryLiveData extends LiveData<List<FriendlyMessage>> {
    private static final String LOG_TAG = "FirebaseQueryLiveData";

    private List<FriendlyMessage> messageList = new ArrayList<>();
    private final Query query;
    private MessagesValueEventListener mValueEventListener;
    private MessageRecyclerViewAdapter mMessageRecyclerViewAdapter;

    public void setMessageRecyclerViewAdapter(MessageRecyclerViewAdapter mMessageRecyclerViewAdapter) {
        this.mMessageRecyclerViewAdapter = mMessageRecyclerViewAdapter;
    }

    public FirebaseQueryLiveData(Query query) {
        this.query = query;
    }

    public void detachDatabaseListener(){
        if(mValueEventListener != null) {
            query.removeEventListener(mValueEventListener);
        }
    }



    @Nullable
    @Override
    public List<FriendlyMessage> getValue() {
        return messageList;
    }

    @Override
    protected void onActive() { // When onStart - onResume lifecycle
        super.onActive();
        Log.d(LOG_TAG, "onActive");
        /* We empty the list before we re-fetch the data from the Firebase database
         in order to avoid duplicates */
        messageList.removeAll(messageList);
        mValueEventListener = new MessagesValueEventListener();
        query.addChildEventListener(mValueEventListener);
    }

    @Override
    protected void onInactive() { // When onPause - onStop lifecycle
        super.onInactive();
        Log.d(LOG_TAG, "onInactive");
        /* We empty the list before we re-fetch the data from the Firebase database
         in order to avoid duplicates */
        messageList.removeAll(messageList);
        query.removeEventListener(mValueEventListener);
        mValueEventListener = null;
    }

    private class MessagesValueEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if (snapshot.exists()){
                // Deserialize the message from the database into a FriendlyMessage object
                messageList.add(snapshot.getValue(FriendlyMessage.class));
                setValue(messageList);
                mMessageRecyclerViewAdapter.notifyItemInserted(messageList.size()-1);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e(LOG_TAG, "Can't listen to query " + query, error.toException());
        }
    }
}
