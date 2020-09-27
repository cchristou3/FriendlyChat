package com.google.firebase.udacity.friendlychat;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessagesViewModel extends ViewModel {
    private static final DatabaseReference MESSAGES_REF =
            FirebaseDatabase.getInstance().getReference().child("messages");

    private FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(MESSAGES_REF);

    public static DatabaseReference GetDatabaseRef(){ return MESSAGES_REF;}

    @NonNull
    public LiveData<List<FriendlyMessage>> getMessagesLiveData(){
        return liveData;
    }
}
