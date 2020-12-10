package com.google.firebase.udacity.friendlychat.view;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.udacity.friendlychat.FirebaseQueryLiveData;

public class MessagesViewModel extends ViewModel {
    private static final CollectionReference MESSAGES_COLLECTION_REF =
            FirebaseFirestore.getInstance()
                    .collection("rooms/room1/messages");

    private FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(MESSAGES_COLLECTION_REF);

    public static CollectionReference GetDatabaseCollectionRef() {
        return MESSAGES_COLLECTION_REF;
    }

    @NonNull
    public FirebaseQueryLiveData getMessagesLiveData() {
        return liveData;
    }
}
