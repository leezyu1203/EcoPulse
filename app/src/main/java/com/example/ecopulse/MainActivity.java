package com.example.ecopulse;

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
        import android.widget.Button;

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

        locationNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(MainActivity.this, R.color.olivine);
                int transparent = Color.argb(0, 0, 0, 0);
                locationNav.setBackgroundColor(color);
                guidanceNav.setBackgroundColor(transparent);
                communityNav.setBackgroundColor(transparent);
                profileNav.setBackgroundColor(transparent);
                replaceFragment(new LocationFragment());
            }
        });

        guidanceNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(MainActivity.this, R.color.olivine);
                int transparent = Color.argb(0, 0, 0, 0);
                guidanceNav.setBackgroundColor(color);
                locationNav.setBackgroundColor(transparent);
                communityNav.setBackgroundColor(transparent);
                profileNav.setBackgroundColor(transparent);
                replaceFragment(new LocationFragment());
            }
        });

        communityNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(MainActivity.this, R.color.olivine);
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
                int color = ContextCompat.getColor(MainActivity.this, R.color.olivine);
                int transparent = Color.argb(0, 0, 0, 0);
                profileNav.setBackgroundColor(color);
                locationNav.setBackgroundColor(transparent);
                communityNav.setBackgroundColor(transparent);
                guidanceNav.setBackgroundColor(transparent);
                replaceFragment(new LocationFragment());
            }
        });

//        Intent intent = new Intent(MainActivity.this, NearbyRecyclingCenter.class);
//        startActivity(intent);

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, fragment);
        fragmentTransaction.commit();
    }


}