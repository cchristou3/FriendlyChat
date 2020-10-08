package com.google.firebase.udacity.friendlychat.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.udacity.friendlychat.Adapter.MessageRecyclerViewAdapter;
import com.google.firebase.udacity.friendlychat.LiveData.FirebaseQueryLiveData;
import com.google.firebase.udacity.friendlychat.Observer.MessageRVAdapterDataObserver;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.Repository.MessagesRepository;
import com.google.firebase.udacity.friendlychat.ViewModel.MessagesViewModel;
import com.google.firebase.udacity.friendlychat.dto.FriendlyMessage;

import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivityCompanion";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;

    private RecyclerView mMessageRecyclerView;
    private MessageRecyclerViewAdapter mMessageAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;
    private FirebaseQueryLiveData liveData;

    private MessagesRepository mMessagesRepository;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;

        // Initialize references to views
        mProgressBar = findViewById(R.id.progressBar);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);

        // Set up the RecyclerView with a Layout Manager
        mLayoutManager = new LinearLayoutManager(this);
        mMessageRecyclerView.setLayoutManager(mLayoutManager);
        mMessageRecyclerView.setHasFixedSize(true);

        // Initialize Firebase Components
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null){
                // User is signed in
                onSignedInInitialize(user.getDisplayName());
            } else {
                // User is signed out
                onSignOutCleanUp();
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false) // automatically save the user's credentials = disabled
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .build(),
                        RC_SIGN_IN);
            }
        };

        // Initialize the messages repository
        mMessagesRepository = new MessagesRepository();

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message - TODO
        mPhotoPickerButton.setOnClickListener(view -> { });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(getTextWatcher());
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(view -> {
            FriendlyMessage friendlyMessage = new FriendlyMessage(
                    mMessageEditText.getText().toString(), mUsername, null, new Timestamp(new Date())
            );
            // Add message to the firebase database
            mMessagesRepository.SendMessage(friendlyMessage);

            // Clear input box
            mMessageEditText.setText("");
        });
    }

    private void onSignOutCleanUp() {
        mUsername = ANONYMOUS;
        if(liveData != null) {
            liveData.removeObservers(this);
//            liveData.detachDatabaseListener();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Signed in cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void onSignedInInitialize(String displayName) {
        mUsername = displayName;
        // Obtain a new or prior instance of MessagesViewModel from the
        // ViewModelProvider.AndroidViewModelFactory utility class.
        ViewModelProvider.AndroidViewModelFactory modelFactory = new ViewModelProvider.AndroidViewModelFactory(getApplication());
        MessagesViewModel viewModel = new ViewModelProvider(this, modelFactory).get(MessagesViewModel.class);
        liveData = viewModel.getMessagesLiveData();

        // Initialize message ListView and its adapter
        // Create a new MessageRecyclerViewAdapter instance and pass it the list of messages
        mMessageAdapter = new MessageRecyclerViewAdapter(liveData.getValue());
        // Pass adapter and activity instance object to LiveData
        liveData.setMessageRecyclerViewAdapter(mMessageAdapter);
        liveData.setMainActivity(this);
        // Register an observer for the adapter's data
        mMessageAdapter.registerAdapterDataObserver(new MessageRVAdapterDataObserver(mMessageRecyclerView));
        // Set adapter to RecyclerView reference
        mMessageRecyclerView.setAdapter(mMessageAdapter);

        // When you update the value stored in the LiveData object, it triggers all registered observers
        liveData.observe(this, friendlyMessages -> {

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    TextWatcher getTextWatcher(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        };
    }
}