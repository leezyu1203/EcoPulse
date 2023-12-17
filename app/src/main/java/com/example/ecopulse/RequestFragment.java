package com.example.ecopulse;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class RequestFragment extends Fragment {
    private View requestLocation = null;
    private AppCompatButton acceptedCatBtn = null;
    private AppCompatButton pendingCatBtn = null;
    private AppCompatButton rejectedCatBtn = null;

    private ArrayList<RequestListItem> items = new ArrayList<>();
    private ArrayList<RequestListItem> selectedItems = new ArrayList<>();
    private ListView requestList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestLocation = inflater.inflate(R.layout.request_fragment, container, false);

        requestList = requestLocation.findViewById(R.id.request_list);
        acceptedCatBtn = requestLocation.findViewById(R.id.accepted_cat);
        pendingCatBtn = requestLocation.findViewById(R.id.pending_cat);
        rejectedCatBtn = requestLocation.findViewById(R.id.rejected_cat);
        items.add(new RequestListItem("Monday", "8:00 AM", "BLK 123, Taman ABC, 81300, JB, Johor.", "012-34567890", "This is my note for 1" , "pending", 0));
        items.add(new RequestListItem("Friday", "10:00 AM", "BLK 3241, Taman ABC, 81300, JB, Johor.", "012-12312412", "This is my note for 2", "pending", 1));
        items.add(new RequestListItem("Sunday", "11:00 AM", "BLK 1233, Taman ABC, 81300, JB, Johor.", "012-12314123", "This is my note for 3", "pending", 3));

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
        selectedItems.clear();

        items.stream().forEach((RequestListItem e)->{
            if (e.getStatus().equals(status)) {
                selectedItems.add(e);
            }
        });

        ArrayAdapter<RequestListItem> adapter = new RequestListAdapter(getActivity(),getContext(), selectedItems, requestLocation);
        requestList.setAdapter(adapter);
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
