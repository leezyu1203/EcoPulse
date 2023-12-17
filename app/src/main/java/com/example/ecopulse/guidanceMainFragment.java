package com.example.ecopulse;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class guidanceMainFragment extends Fragment {
    FirebaseFirestore guidanceDatabaseReference;
    SearchView guidanceSearchView;
    RecyclerView guidanceRecycleView;
    WasteGuidanceAdapter wasteGuidanceAdapter;
    List<String> wasteList;

    TextView title;


    ImageButton recyclableWasteBtn, householdFoodWasteBtn, hazardousWasteBtn,residualWasteBtn, backButton;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guidance_main, container, false);

        recyclableWasteBtn = rootView.findViewById(R.id.recyclableWaste_btn);
        hazardousWasteBtn = rootView.findViewById(R.id.hazardousWaste_btn);
        householdFoodWasteBtn = rootView.findViewById(R.id.householdFoodWaste_btn);
        residualWasteBtn = rootView.findViewById(R.id.residualWaste_btn);

        recyclableWasteBtn.setOnClickListener(view -> navigateToIntroduction(0));
        hazardousWasteBtn.setOnClickListener(view -> navigateToIntroduction(1));
        householdFoodWasteBtn.setOnClickListener(view -> navigateToIntroduction(2));
        residualWasteBtn.setOnClickListener(view -> navigateToIntroduction(3));
        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Recycling Guidance");
        backButton = getActivity().findViewById(R.id.backButton);
        backButton.setVisibility(View.INVISIBLE);


        //search

        guidanceDatabaseReference = FirebaseFirestore.getInstance();

        guidanceSearchView = rootView.findViewById(R.id.guidanceSearch);
        guidanceRecycleView = rootView.findViewById(R.id.guidanceRecyclerView);
        wasteList = new ArrayList<>();
        wasteGuidanceAdapter =  new WasteGuidanceAdapter(requireContext(), wasteList);

        guidanceRecycleView.setLayoutManager(new LinearLayoutManager(requireContext()));
        guidanceRecycleView.setAdapter(wasteGuidanceAdapter);

        guidanceSearchView.clearFocus();

        guidanceSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.isEmpty()) {
                    guidanceRecycleView.setVisibility(View.GONE);
                } else {
                    guidanceRecycleView.setVisibility(View.VISIBLE);
                    guidanceRecycleView.setElevation(getResources().getDimension(R.dimen.guidanceRecyclerViewElevation));
                }

                queryGuidanceFireStore(newText);
                return true;
            }
        });


        guidanceRecycleView.setVisibility(View.GONE);
        guidanceSearchView.setOnSearchClickListener(view ->{
            guidanceRecycleView.setVisibility(View.VISIBLE);
            guidanceRecycleView.setElevation(getResources().getDimension(R.dimen.guidanceRecyclerViewElevation));
        });

        guidanceSearchView.setOnCloseListener(() ->{
            guidanceRecycleView.setVisibility(View.GONE);
            return false;
        });

        wasteGuidanceAdapter.setOnItemClickListener(position -> {


        });

        return rootView;
    }


    public void queryGuidanceFireStore(String query) {
        wasteList.clear();

        if (query.isEmpty()) {
            wasteGuidanceAdapter.notifyDataSetChanged();
            return;
        }

        String lowercaseQuery = query.toLowerCase();

        guidanceDatabaseReference.collection("recycling_guidance")
                .orderBy("waste_name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    wasteList.clear();
                    boolean foundMatch = false; // Flag to track if any match is found
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String wasteName = documentSnapshot.getString("waste_name");
                        long wasteType = documentSnapshot.getLong("waste_type");
                        String wasteDesc = documentSnapshot.getString("waste_desc");

                        // Convert wasteName to lowercase for case-insensitive comparison
                        String lowercaseWasteName = wasteName.toLowerCase();

                        if (lowercaseWasteName.contains(lowercaseQuery)) {
                            foundMatch = true; // Match found
                            if (wasteType == 0) {
                                wasteList.add(wasteName + " _ " + "Recyclable Waste" + " _ " + wasteDesc);
                            } else if (wasteType == 1) {
                                wasteList.add(wasteName + " _ " + "Hazardous Waste" + " _ " + wasteDesc);
                            } else if (wasteType == 2) {
                                wasteList.add(wasteName + " _ " + "Household Food Waste" + " _ " + wasteDesc);
                            } else if (wasteType == 3) {
                                wasteList.add(wasteName + " _ " + "Residual Waste" + " _ " + wasteDesc);
                            }
                        }
                    }

                    // Check if no match was found and add the message accordingly
                    if (!foundMatch) {
                        wasteList.add("No matching results found _ Try another keyword _ "+"");
                    }

                    wasteGuidanceAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching data: " + e.getMessage(), e);
                });
    }

    public void navigateToIntroduction(int pageNum)
    {
        WasteIntroductionFragment fragment = new WasteIntroductionFragment();

        Bundle args = new Bundle();
        args.putInt("pageNumber", pageNum);
        fragment.setArguments(args);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.main_fragment,fragment);
        backButton = getActivity().findViewById(R.id.backButton);
        backButton.setVisibility(View.VISIBLE);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}