package com.example.ecopulse;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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

    private boolean initial = true;
    private ImageButton IBtnReminder;
    private static ListenerRegistration listener;


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


        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            String redirect = getIntent().getExtras().get("redirect").toString();
            System.out.println(redirect);
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
                if (currentFragment instanceof guidanceMainFragment == false && currentFragment instanceof LocationFragment == false && currentFragment instanceof CollaboratorLocationFragment == false && currentFragment instanceof CommunityFragment == false) {
                    MainActivity.this.getOnBackPressedDispatcher().onBackPressed();
                }


            }
        });



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = "";
        if (user != null) {
            userEmail = user.getEmail();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        listener = db.collection("user").document(user.getUid()).collection("messages").orderBy("added_at", Query.Direction.DESCENDING).limit(1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    Log.e(TAG, "CollaboratorUploadFragment: Listen exist doc error", error);
                }

                if(value != null && !initial && MainActivity.this != null){
                    String title = value.getDocuments().iterator().next().get("title").toString();
                    String desc = value.getDocuments().iterator().next().get("desc").toString();

                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (title.equals("You have received a new request!")) {
                        i.putExtra("redirect", "location");
                    } else {
                        i.putExtra("redirect", "reminder");
                    }
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "reminder")
                            .setSmallIcon(R.drawable.baseline_recycling_24)
                            .setContentTitle(title).setContentText(desc)
                            .setAutoCancel(true)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    notificationManagerCompat.notify(123, builder.build());
                }

                initial = false;
            }
        });
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

                            if (currentUser != null) {
                                String uid = currentUser.getUid();
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

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, fragment);
        fragmentTransaction.commit();
    }


}