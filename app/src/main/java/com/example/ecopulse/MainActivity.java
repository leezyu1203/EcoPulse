package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton locationNav;
    private AppCompatButton guidanceNav;
    private AppCompatButton communityNav;
    private AppCompatButton profileNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationNav = findViewById(R.id.location_nav);
        guidanceNav = findViewById(R.id.guidance_nav);
        communityNav = findViewById(R.id.community_nav);
        profileNav = findViewById(R.id.profile_nav);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = "";
        if (user != null) {
            userEmail = user.getEmail();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                            if (role.equals("RC")) {
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
                            replaceFragment(new LocationFragment());
                        }
                    });

                }

            }
        });


    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, fragment);
        fragmentTransaction.commit();
    }


}