package com.example.ecopulse.AuthenticationAndProfile;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.ecopulse.MainActivity;
import com.example.ecopulse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class AdminActivity extends AppCompatActivity {

    // UI elements
    private ListView adminApprovalList;
    private ScrollView scrollView;
    private LinearLayout loading, noRecords;
    private AppCompatButton acceptedBtn, pendingBtn;
    private ImageButton logoutBtn;

    // Data structures for handling user items
    private ArrayList<AdminApprovalListItem> allItems = new ArrayList<>();
    private ArrayList<AdminApprovalListItem> selectedItems = new ArrayList<>();
    private FirebaseFirestore firestoreRef;

    // Current status of user items (pending or approved)
    private String currentStatus = "pending";

    private ListenerRegistration listener;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listener.remove();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_approval);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) { // true: for prevent back and do something in handleOnBackPressed
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AdminActivity.this, "No more previous page!", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Firestore reference
        firestoreRef = FirebaseFirestore.getInstance();

        // Initialize UI elements
        adminApprovalList = findViewById(R.id.admin_approval_list);
        noRecords = findViewById(R.id.no_records);
        scrollView = findViewById(R.id.scrollView);
        loading = findViewById(R.id.loading);
        scrollView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        acceptedBtn = findViewById(R.id.acceptedCat);
        pendingBtn = findViewById(R.id.pendingCat);



        // Fetch user data from Firestore based on certain criteria
        listener = firestoreRef.collection("user").where(Filter.and(Filter.or(Filter.equalTo("verified", "pending"),
                Filter.equalTo("verified", "approved")), Filter.or(Filter.equalTo("role", "Recycling Center Collaborator"),
                Filter.equalTo("role", "Event Collaborator")))).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // Populate allItems list with user details
                allItems.clear();
                for (DocumentSnapshot document : value.getDocuments()) {
                    // Extract user details from Firestore
                    String email = String.valueOf(document.get("email"));
                    String name = String.valueOf(document.get("username"));
                    String contact = String.valueOf(document.get("phone"));
                    String address = String.valueOf(document.get("address"));
                    String role = String.valueOf(document.get("role"));
                    String status = String.valueOf(document.get("verified"));
                    String id = document.getId();

                    if (role.equals("Recycling Center Collaborator")) {
                        String opening = String.valueOf(document.get("opening"));
                        String type = String.valueOf(document.get("type"));
                        allItems.add(new AdminApprovalListItem(name, address, contact, email, status, role, id, opening, type));
                    } else {
                        allItems.add(new AdminApprovalListItem(name, address, contact, email, status, role, id));
                    }
                }
                // Update the UI based on the currentStatus
                changeStatus(currentStatus);
            }
        });

        // Initialize logout button and set its onClickListener
        logoutBtn = findViewById(R.id.logout);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent loginIntent = new Intent(AdminActivity.this, Login.class);
            startActivity(loginIntent);
            finish();
        });



        // Set onClickListeners for the status change buttons
        pendingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update UI and change status to "pending"
                final int light_green = ContextCompat.getColor(AdminActivity.this, R.color.light_green);
                final int black = ContextCompat.getColor(AdminActivity.this, R.color.black);
                final int dark_green = ContextCompat.getColor(AdminActivity.this, R.color.primary_dark_green);
                final int white = ContextCompat.getColor(AdminActivity.this, R.color.white);

                pendingBtn.setBackgroundColor(light_green);
                pendingBtn.setTextColor(black);
                acceptedBtn.setBackgroundColor(dark_green);
                acceptedBtn.setTextColor(white);
                changeStatus("pending");
            }
        });

        acceptedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update UI and change status to "approved"
                final int light_green = ContextCompat.getColor(AdminActivity.this, R.color.light_green);
                final int black = ContextCompat.getColor(AdminActivity.this, R.color.black);
                final int dark_green = ContextCompat.getColor(AdminActivity.this, R.color.primary_dark_green);
                final int white = ContextCompat.getColor(AdminActivity.this, R.color.white);

                acceptedBtn.setBackgroundColor(light_green);
                acceptedBtn.setTextColor(black);
                pendingBtn.setBackgroundColor(dark_green);
                pendingBtn.setTextColor(white);

                changeStatus("approved");
            }
        });
    }

    // Method to update UI based on the selected status
    public void changeStatus(String status) {
        currentStatus = status;
        selectedItems.clear();

        // Filter items based on the selected status
        allItems.stream().forEach(e -> {
            if (e.getStatus().equals(currentStatus)) {
                selectedItems.add(e);
            }
        });

        // Update UI visibility and set adapter for the ListView
        if (selectedItems.isEmpty()) {
            noRecords.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
        } else {
            if (AdminActivity.this != null) {
                noRecords.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                ArrayAdapter<AdminApprovalListItem> adapter = new AdminApprovalListAdapter(AdminActivity.this, selectedItems);
                adminApprovalList.setAdapter(adapter);
            }
        }
    }
}
