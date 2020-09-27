package com.google.firebase.udacity.friendlychat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
    private final MessagesValueEventListener mValueEventListener = new MessagesValueEventListener();

    public FirebaseQueryLiveData(Query query) {
        this.query = query;
    }

    @Override
    protected void onActive() { // When onStart - onResume lifecycle
        super.onActive();
        Log.d(LOG_TAG, "onActive");
        messageList = new ArrayList<>();
        query.addChildEventListener(mValueEventListener);
    }

    @Override
    protected void onInactive() { // When onPause - onStop lifecycle
        super.onInactive();
        Log.d(LOG_TAG, "onInactive");
        messageList = new ArrayList<>();
        query.removeEventListener(mValueEventListener);
    }

    private class MessagesValueEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if (snapshot.exists()){
                // Deserialize the message from the database into a FriendlyMessage object
                messageList.add(snapshot.getValue(FriendlyMessage.class));
                setValue(messageList);
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
