package com.example.ecopulse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
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
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private AppCompatButton rejectedCatBtn = null;

    private ArrayList<RequestListItem> selectedItems = new ArrayList<>();
    private ListView requestList;

    private Context mContext;
    private FragmentActivity mActivity;

    private PendingIntent pendingIntent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestLocation = inflater.inflate(R.layout.request_fragment, container, false);

        requestList = requestLocation.findViewById(R.id.request_list);
        acceptedCatBtn = requestLocation.findViewById(R.id.accepted_cat);
        pendingCatBtn = requestLocation.findViewById(R.id.pending_cat);
        rejectedCatBtn = requestLocation.findViewById(R.id.rejected_cat);

        showNoRecordsImage(acceptedCatBtn, pendingCatBtn, rejectedCatBtn, "accepted");

        acceptedCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoRecordsImage(acceptedCatBtn, rejectedCatBtn, pendingCatBtn, "accepted");
            }
        });

        pendingCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoRecordsImage(pendingCatBtn, rejectedCatBtn, acceptedCatBtn, "pending");
            }
        });

        rejectedCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoRecordsImage(rejectedCatBtn, pendingCatBtn, acceptedCatBtn, "rejected");
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

    public void showNoRecordsImage(AppCompatButton selected, AppCompatButton notSelected1, AppCompatButton notSelected2, String status) {
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = "";
        if (user != null) {
            userEmail = user.getEmail();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        showLoadingBar(true);
        db.collection("user").whereEqualTo("email", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document1 = task.getResult().getDocuments().iterator().next();
                    String userID = document1.getId();
                    db.collection("recycling_center_information").whereEqualTo("userID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document2 = task.getResult().getDocuments().iterator().next();
                                String recyclingCenterID = document2.getId();
                                db.collection("pick_up_schedule").where(Filter.and(Filter.equalTo("recyclingCenterID", recyclingCenterID), Filter.equalTo("status", status))).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        showLoadingBar(false);
                                        if (task.isSuccessful()) {
                                            selectedItems.clear();
                                            for (DocumentSnapshot document : task.getResult()) {
                                                String dayOfWeek = document.get("day") + "";
                                                String time = document.get("time") + "";
                                                String address = document.get("address") + "";
                                                String contact = document.get("contact") + "";
                                                String note = document.get("note") + "";
                                                String status = document.get("status") + "";
                                                String id = document.getId();
                                                selectedItems.add(new RequestListItem(dayOfWeek, time, address, contact, note , status, id));

                                            }

                                            if (mActivity != null && mContext != null) {
                                                ArrayAdapter<RequestListItem> adapter = new RequestListAdapter(mActivity,mContext, selectedItems, requestLocation);
                                                requestList.setAdapter(adapter);
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
                                });
                            }
                        }
                    });

                }
            }
        });
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

}

