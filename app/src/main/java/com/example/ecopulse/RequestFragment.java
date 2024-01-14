package com.example.ecopulse;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RequestFragment extends Fragment {
    private View requestLocation = null;
    private AppCompatButton acceptedCatBtn = null;
    private AppCompatButton pendingCatBtn = null;
    private AppCompatButton canceledCatBtn = null;

    private AppCompatButton doneCatBtn = null;
    private static ListenerRegistration listener = null;

    private ArrayList<RequestListItem> selectedItems = new ArrayList<>();
    private ArrayList<RequestListItem> allItems = new ArrayList<>();
    private ListView requestList;

    private Context mContext;
    private FragmentActivity mActivity;

    private String currentStatus, recyclingCenterID;

    public static ListenerRegistration getListener() {
        return listener;
    }
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentStatus = "accepted";
        requestLocation = inflater.inflate(R.layout.request_fragment, container, false);
        requestList = requestLocation.findViewById(R.id.request_list);
        doneCatBtn = requestLocation.findViewById(R.id.done_cat);
        acceptedCatBtn = requestLocation.findViewById(R.id.accepted_cat);
        pendingCatBtn = requestLocation.findViewById(R.id.pending_cat);
        canceledCatBtn = requestLocation.findViewById(R.id.canceled_cat);
        db = FirebaseFirestore.getInstance();
        getRecyclingCenterIDAndData();
        showNoRecordsImage(acceptedCatBtn, pendingCatBtn, canceledCatBtn, doneCatBtn);

        acceptedCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStatus = "accepted";
                showNoRecordsImage(acceptedCatBtn, canceledCatBtn, pendingCatBtn, doneCatBtn);
                displayRequestsBasedOnStatus();
            }
        });

        pendingCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStatus = "pending";
                showNoRecordsImage(pendingCatBtn, canceledCatBtn, acceptedCatBtn, doneCatBtn);
                displayRequestsBasedOnStatus();
            }
        });

        canceledCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStatus = "cancelled";
                showNoRecordsImage(canceledCatBtn, pendingCatBtn, acceptedCatBtn, doneCatBtn);
                displayRequestsBasedOnStatus();
            }
        });

        doneCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentStatus = "done";
                showNoRecordsImage(doneCatBtn, canceledCatBtn, pendingCatBtn, acceptedCatBtn);
                displayRequestsBasedOnStatus();
            }
        });

        return requestLocation;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = requireActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    public void showNoRecordsImage(AppCompatButton selected, AppCompatButton notSelected1, AppCompatButton notSelected2, AppCompatButton notSelected3) {
        final int light_green = ContextCompat.getColor(getContext(), R.color.light_green);
        final int black = ContextCompat.getColor(getContext(), R.color.black);
        final int dark_green = ContextCompat.getColor(getContext(), R.color.primary_dark_green);
        final int white = ContextCompat.getColor(getContext(), R.color.white);

        selected.setBackgroundColor(light_green);
        selected.setTextColor(black);
        notSelected1.setBackgroundColor(dark_green);
        notSelected1.setTextColor(white);
        notSelected2.setBackgroundColor(dark_green);
        notSelected2.setTextColor(white);
        notSelected3.setBackgroundColor(dark_green);
        notSelected3.setTextColor(white);


    }

    private void showLoadingBar(boolean show) {
        LinearLayout loading = requestLocation.findViewById(R.id.loading);
        LinearLayout noRecords = requestLocation.findViewById(R.id.no_records);
        if (show) {
            requestList.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0.0f));
            loading.setVisibility(View.VISIBLE);
            noRecords.setVisibility(View.GONE);
        } else {
            loading.setVisibility(View.GONE);
            requestList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            loading.setLayoutParams(new LinearLayout.LayoutParams(0,0,0.0f));
        }
    }

    private void getRecyclingCenterIDAndData() {
        // Show the circular loading progress before fetching the data from Firestore
        showLoadingBar(true);

        // Get current user's UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();

        // Get the recycling center id related to this collaborator account
        db.collection("recycling_center_information").whereEqualTo("userID", userID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult().getDocuments().iterator().next();
                            recyclingCenterID = document.getId();
                            // Get the requests data
                            getData();
                        }
                    }
                });
    }

    public void getData() {
        // Create listener to listen to the changes of FireStore collection "pick_up_schedule"
        listener = db.collection("pick_up_schedule").whereEqualTo("recyclingCenterID", recyclingCenterID)
                .orderBy("update_at", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(RequestFragment.this.toString(), "Listen failed.", error);
                    return;
                }
                // Clear the allItems ArrayList<RequestListItem> and add all the requests item to it again
                // every time when the data in FireStore collection "pick_up_schedule" changes
                allItems.clear();
                for (QueryDocumentSnapshot document : value) {
                    String dayOfWeek = document.get("day") + "";
                    String time = document.get("time") + "";
                    String address = document.get("address") + "";
                    String contact = document.get("contact") + "";
                    String note = document.get("note") + "";
                    String status = document.get("status") + "";
                    String id = document.getId();
                    allItems.add(new RequestListItem(dayOfWeek, time, address, contact, note , status, id));
                }

                // Display the request based on the active status
                displayRequestsBasedOnStatus();
            }
        });

    }

    public void displayRequestsBasedOnStatus() {
        showLoadingBar(true);
        selectedItems.clear();
        allItems.stream().forEach(
                e -> {
                    if (e.getStatus().equals(currentStatus)) {
                        selectedItems.add(e);
                    }
                }
        );

        if (mActivity != null && mContext != null) {
            ArrayAdapter<RequestListItem> adapter = new RequestListAdapter(mActivity,mContext, selectedItems, requestLocation);
            requestList.setAdapter(adapter);
            showLoadingBar(false);
        }

        LinearLayout noRecords = requestLocation.findViewById(R.id.no_records);
        if (selectedItems.isEmpty()) {
            noRecords.setVisibility(View.VISIBLE);
            requestList.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0.0f));
            noRecords.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f));
        } else {
            noRecords.setVisibility(View.GONE);
            requestList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            noRecords.setLayoutParams(new LinearLayout.LayoutParams(0,0,0.0f));
        }
    }

}

