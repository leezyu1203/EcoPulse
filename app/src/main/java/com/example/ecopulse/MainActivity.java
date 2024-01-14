package com.example.ecopulse;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.example.ecopulse.AuthenticationAndProfile.Profile_Collaborator;
import com.example.ecopulse.AuthenticationAndProfile.Profile_RC;
import com.example.ecopulse.AuthenticationAndProfile.Profile_user;
import com.example.ecopulse.Community.CommunityFragment;
import com.example.ecopulse.Guidance.guidanceMainFragment;
import com.example.ecopulse.Information.CollaboratorLocationFragment;
import com.example.ecopulse.Information.LocationFragment;
import com.example.ecopulse.Reminder.reminderMainFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton locationNav;
    private AppCompatButton guidanceNav;
    private AppCompatButton communityNav;
    private AppCompatButton profileNav;
    private ImageButton backButton;

    private boolean isInitial = true;
    private ImageButton IBtnReminder;
    private static ListenerRegistration listener;
    private FirebaseFirestore db;

    private FirebaseUser user;


    public static ListenerRegistration getListener() {
        return listener;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                }
            });

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission();
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            String redirect = getIntent().getExtras().get("redirect").toString();

            if (redirect.equals("reminder")) {
                replaceFragment(new reminderMainFragment());
            } else if(redirect.equals("location")) {
                Bundle bundle = new Bundle();
                bundle.putString("redirect", "request");
                CollaboratorLocationFragment colabLocationFrag = new CollaboratorLocationFragment();
                colabLocationFrag.setArguments(bundle);
                replaceFragment(colabLocationFrag);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        backButton = findViewById(R.id.backButton);
        IBtnReminder = findViewById(R.id.IBtnReminder);
        locationNav = findViewById(R.id.location_nav);
        guidanceNav = findViewById(R.id.guidance_nav);
        communityNav = findViewById(R.id.community_nav);
        profileNav = findViewById(R.id.profile_nav);
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
                if (currentFragment instanceof guidanceMainFragment == false && currentFragment instanceof LocationFragment == false && currentFragment instanceof CollaboratorLocationFragment == false && currentFragment instanceof CommunityFragment == false && currentFragment instanceof Profile_Collaborator == false && currentFragment instanceof Profile_RC == false && currentFragment instanceof Profile_user == false && currentFragment instanceof reminderMainFragment == false) {
                    MainActivity.this.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });


        user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = "";
        if (user != null) {
            userEmail = user.getEmail();
        }
        db = FirebaseFirestore.getInstance();

        listenForNotification();
        db.collection("user").whereEqualTo("email", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    QueryDocumentSnapshot document1 = task.getResult().iterator().next();
                    String role = document1.getData().get("role") + "";
                    locationNav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int color = ContextCompat.getColor(MainActivity.this, R.color.light_green);
                            int transparent = Color.argb(0, 0, 0, 0);
                            locationNav.setBackgroundColor(color);
                            guidanceNav.setBackgroundColor(transparent);
                            communityNav.setBackgroundColor(transparent);
                            profileNav.setBackgroundColor(transparent);
                            IBtnReminder.setBackgroundColor(transparent);
                            if (role.equals("Recycling Center Collaborator")) {
                                replaceFragment(new CollaboratorLocationFragment());
                            } else {
                                replaceFragment(new LocationFragment());
                            }
                        }
                    });

                    guidanceNav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int color = ContextCompat.getColor(MainActivity.this, R.color.light_green);
                            int transparent = Color.argb(0, 0, 0, 0);
                            guidanceNav.setBackgroundColor(color);
                            locationNav.setBackgroundColor(transparent);
                            communityNav.setBackgroundColor(transparent);
                            profileNav.setBackgroundColor(transparent);
                            replaceFragment(new guidanceMainFragment());
                            IBtnReminder.setBackgroundColor(transparent);
                        }
                    });

                    communityNav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int color = ContextCompat.getColor(MainActivity.this, R.color.light_green);
                            int transparent = Color.argb(0, 0, 0, 0);
                            communityNav.setBackgroundColor(color);
                            locationNav.setBackgroundColor(transparent);
                            guidanceNav.setBackgroundColor(transparent);
                            profileNav.setBackgroundColor(transparent);
                            IBtnReminder.setBackgroundColor(transparent);
                            replaceFragment(new CommunityFragment());
                        }
                    });

                    profileNav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int color = ContextCompat.getColor(MainActivity.this, R.color.light_green);
                            int transparent = Color.argb(0, 0, 0, 0);
                            profileNav.setBackgroundColor(color);
                            locationNav.setBackgroundColor(transparent);
                            communityNav.setBackgroundColor(transparent);
                            guidanceNav.setBackgroundColor(transparent);
                            IBtnReminder.setBackgroundColor(transparent);

                            if (user != null) {
                                String uid = user.getUid();
                                DocumentReference userDocRef = db.collection("user").document(uid);
                                userDocRef.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            String userRole = document.getString("role");

                                            if ("User".equals(userRole)) {
                                                replaceFragment(new Profile_user());
                                            } else if ("Event Collaborator".equals(userRole)) {
                                                replaceFragment(new Profile_Collaborator());
                                            } else if("Recycling Center Collaborator".equals(userRole)){
                                                replaceFragment(new Profile_RC());
                                            }else{}
                                        } else {
                                            // Handle the case where the document does not exist
                                        }
                                    } else {
                                        // Handle exceptions during Firestore document retrieval
                                    }
                                });
                            }
                        }
                    });

                    IBtnReminder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int transparent = Color.argb(0, 0, 0, 0);
                            communityNav.setBackgroundColor(transparent);
                            locationNav.setBackgroundColor(transparent);
                            guidanceNav.setBackgroundColor(transparent);
                            profileNav.setBackgroundColor(transparent);
                            replaceFragment(new reminderMainFragment());
                        }
                    });


                }


            }
        });
        replaceFragment(new guidanceMainFragment());
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, fragment);
        fragmentTransaction.commit();
    }

    private void listenForNotification() {
        // Extracts information from the latest document in the collection
        listener = db.collection("user").document(user.getUid()).collection("messages")
                .orderBy("added_at", Query.Direction.DESCENDING).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // Handles potential errors during the event and logs them if present
                if(error != null) {
                    Log.e(TAG, "CollaboratorUploadFragment: Listen exist doc error", error);
                }

                // Processes the data only if there is a change and it is not an initial setup
                if(value != null && !isInitial && MainActivity.this != null){
                    // Get the title and description of the notification
                    String title = value.getDocuments().iterator().next().get("title").toString();
                    String desc = value.getDocuments().iterator().next().get("desc").toString();

                    // Creates an intent to launch the main activity when the notification is tapped
                    Intent i = new Intent(MainActivity.this, MainActivity.class);

                    // Start a new activity and clear all existing activities in the task stack.
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // Adds an extra parameter indicating where to redirect the user within the app ("location" or "reminder")
                    if (title.equals("You have received a new request!")) {
                        i.putExtra("redirect", "location");
                    } else {
                        i.putExtra("redirect", "reminder");
                    }

                    // Configures a PendingIntent to encapsulate the intent for the notification
                    int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, i, flags);

                    // Builds a NotificationCompat.Builder to configure the appearance and behavior of the notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "reminder")
                            .setSmallIcon(R.drawable.baseline_recycling_24)
                            .setContentTitle(title).setContentText(desc)
                            .setAutoCancel(true)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);

                    // Retrieves the NotificationManagerCompat and issues the notification with a unique identifier (123)
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
                    // Checks for the required notification permission before issuing the notification
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    notificationManagerCompat.notify(123, builder.build());
                }

                // Updates the isInitial flag to false after the first setup
                isInitial = false;
            }
        });
    }


}