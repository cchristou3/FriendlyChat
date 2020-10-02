package com.google.firebase.udacity.friendlychat;

public class MessagesRepository {
    public static final String TAG = "MESSAGES_REPOSITORY";

    void SendMessage(FriendlyMessage friendlyMessage){
        // push() creates a new node and appends the given object.
        MessagesViewModel.GetDatabaseRef().push().setValue(friendlyMessage);
    }

    void RemoveAllMessages(){
        // Delete all messages within the real-time database
        MessagesViewModel.GetDatabaseRef().setValue(null);
    }
}
