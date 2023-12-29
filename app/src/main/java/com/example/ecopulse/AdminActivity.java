package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ListView adminApprovalList;
    private ScrollView scrollView;
    private LinearLayout loading, noRecords;

    private FirebaseFirestore firestoreRef;
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

        List<AdminApprovalListItem> pendingApprovalItem = new ArrayList<>();
        firestoreRef.collection("user").whereEqualTo("verified", "pending").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
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
                            pendingApprovalItem.add(new AdminApprovalListItem(name, address, contact, email, status, role, id, opening, type));
                        } else {
                            pendingApprovalItem.add(new AdminApprovalListItem(name, address, contact, email, status, role, id));
                        }

                    }


                    if (pendingApprovalItem.isEmpty()) {
                        noRecords.setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                    } else {
                        if (AdminActivity.this != null) {
                            noRecords.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                            ArrayAdapter<AdminApprovalListItem> adapter = new AdminApprovalListAdapter(AdminActivity.this, pendingApprovalItem);
                            adminApprovalList.setAdapter(adapter);
                        }
                    }




                }
            }
        });



    }


}