package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton locationNav;
    private AppCompatButton guidanceNav;
    private AppCompatButton communityNav;
    private AppCompatButton profileNav;
    private ImageButton backButton;
    private ImageButton IBtnReminder;
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
                            if (role.equals("RC") || role.equals("Recycling Center Collaborator")) {
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
                            int color = ContextCompat.getColor(MainActivity.this, R.color.light_green);
                            int transparent = Color.argb(0, 0, 0, 0);
                            IBtnReminder.setBackgroundColor(color);
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