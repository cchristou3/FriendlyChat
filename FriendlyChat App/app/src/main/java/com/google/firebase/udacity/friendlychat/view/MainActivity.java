package com.google.firebase.udacity.friendlychat.view;

import android.content.Intent;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.udacity.friendlychat.FirebaseQueryLiveData;
import com.google.firebase.udacity.friendlychat.MessageRVAdapterDataObserver;
import com.google.firebase.udacity.friendlychat.MessageRecyclerViewAdapter;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.data.dto.FriendlyMessage;
import com.google.firebase.udacity.friendlychat.data.repository.MessagesRepository;

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
    private StorageReference mChatPhotosStorageReference;

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
        mChatPhotosStorageReference = FirebaseStorage.getInstance().getReference().child("chat_photos");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                onSignedInInitialize(user.getDisplayName());
            } else {
                // User is signed out
                onSignOutCleanUp();
                AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                        .Builder(R.layout.activity_customised_login)
                        .setGoogleButtonId(R.id.activity_customised_login_sign_in_button)
                        .setEmailButtonId(R.id.activity_customised_login_email_button)
                        .build();

                startActivityForResult(
                        AuthUI.getInstance(FirebaseApp.initializeApp(this))
                                .createSignInIntentBuilder()
                                .setAuthMethodPickerLayout(customLayout)
                                .setIsSmartLockEnabled(false) // automatically save the user's credentials = disabled
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        // set email builder but hide its button from the layout => Customization
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
        mPhotoPickerButton.setOnClickListener(view -> {
            // Create an intent for accessing the device's content (gallery)
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
        });

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
        if (liveData != null) {
            liveData.removeObservers(this);
//            liveData.detachDatabaseListener();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Signed in cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at chat_photos/<FILENAME>
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase storage
            UploadTask uploadPhotoTask = photoRef.putFile(selectedImageUri);
            Task<Uri> uriTask = uploadPhotoTask.continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();

                // Continue with the task to get the URL
                return photoRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Access the download URL
                    Uri photoDownloadUri = task.getResult();

                    // Store to database
                    FriendlyMessage friendlyMessage =
                            new FriendlyMessage(null,
                                    mUsername,
                                    photoDownloadUri
                                            .toString(),
                                    new Timestamp(new Date()));
                    mMessagesRepository.SendMessage(friendlyMessage);
                }
            });
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //AuthUI.getInstance().silentSignIn();
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @org.jetbrains.annotations.Contract(value = " -> new", pure = true)
    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { /* ignore */ }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) { /* ignore */ }
        };
    }
}