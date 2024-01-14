package com.example.ecopulse.Guidance;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.ecopulse.MainActivity;
import com.example.ecopulse.R;

public class OfflineView extends AppCompatActivity {
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_view);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) { // true: for prevent back and do something in handleOnBackPressed
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(OfflineView.this, "No more previous page!", Toast.LENGTH_SHORT).show();
            }
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
                if (currentFragment instanceof guidanceMainFragment == false) {
                    OfflineView.this.getOnBackPressedDispatcher().onBackPressed();
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
