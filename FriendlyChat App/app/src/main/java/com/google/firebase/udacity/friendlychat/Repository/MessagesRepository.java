package com.google.firebase.udacity.friendlychat.Repository;

import android.util.Log;

import com.google.firebase.udacity.friendlychat.ViewModel.MessagesViewModel;
import com.google.firebase.udacity.friendlychat.dto.FriendlyMessage;

import java.util.HashMap;

public class MessagesRepository {
    public static final String TAG = "MESSAGES_REPOSITORY";
    public static final String NAME_KEY = "name";
    public static final String TEXT_KEY = "text";
    public static final String PHOTO_URL_KEY = "photoUrl";
    public static final String TIMESTAMP_KEY = "timestamp";

    public void SendMessage(FriendlyMessage friendlyMessage) {
        // Create a new HashMap object and initialize it with the given argument
        HashMap<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(NAME_KEY, friendlyMessage.getName());
        dataToSave.put(TEXT_KEY, friendlyMessage.getText());
        dataToSave.put(PHOTO_URL_KEY, friendlyMessage.getPhotoUrl());
        dataToSave.put(TIMESTAMP_KEY, friendlyMessage.getTimestamp());

        /* IMPORTANT NOTE: Add to collections - replace documents */

        // Add the HashMap object to the Database
        MessagesViewModel.GetDatabaseCollectionRef().add(dataToSave).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Document has been saved!");
            } else {
                Log.d(TAG, "Document failed to get saved!");
            }
        });
    }
}
