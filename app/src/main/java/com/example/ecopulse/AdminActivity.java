package com.example.ecopulse;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ListView adminApprovalList;
    private ScrollView scrollView;
    private LinearLayout loading, noRecords;

    private ArrayList<AdminApprovalListItem> allItems = new ArrayList<>();
    private ArrayList<AdminApprovalListItem> selectedItems = new ArrayList<>();
    private FirebaseFirestore firestoreRef;

    private AppCompatButton acceptedBtn, pendingBtn;
    private ImageButton logoutBtn;
    private String currentStatus = "pending";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_approval);
        firestoreRef = FirebaseFirestore.getInstance();
        adminApprovalList = findViewById(R.id.admin_approval_list);
        noRecords = findViewById(R.id.no_records);
        scrollView = findViewById(R.id.scrollView);
        loading = findViewById(R.id.loading);
        scrollView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        acceptedBtn = findViewById(R.id.acceptedCat);
        pendingBtn = findViewById(R.id.pendingCat);
        firestoreRef.collection("user").where(Filter.and(Filter.or(Filter.equalTo("verified", "pending"), Filter.equalTo("verified", "approved")), Filter.or(Filter.equalTo("role", "Recycling Center Collaborator"), Filter.equalTo("role", "Event Collaborator")))).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allItems.clear();
                    for (DocumentSnapshot document : task.getResult()) {
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
                    changeStatus(currentStatus);
                }
            }
        });

        logoutBtn = findViewById(R.id.logout);
        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent loginIntent = new Intent(AdminActivity.this, Login.class);
            startActivity(loginIntent);
            finish();
        });

        pendingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    public void changeStatus(String status) {
        currentStatus = status;
        selectedItems.clear();
        allItems.stream().forEach(e -> {
            if (e.getStatus().equals(currentStatus)) {
                selectedItems.add(e);
            }
        });


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