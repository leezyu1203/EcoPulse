package com.example.ecopulse;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class WasteExampleListFragment extends Fragment {

    RecyclerView guidanceRecWasteList;
    ArrayList<String> wasteList;
    WasteGuidanceAdapter wasteGuidanceAdapter;
    TextView wasteTypeTitle;
    FirebaseFirestore guidanceDatabaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_waste_example_list, container, false);


        wasteTypeTitle =rootView.findViewById (R.id.wasteTypeTitle);//page title
        guidanceRecWasteList = rootView.findViewById(R.id.guidanceRecWasteList);//recycler view for waste example list

        //adapter for recycler view
        wasteList = new ArrayList<>();
        wasteGuidanceAdapter =  new WasteGuidanceAdapter(requireContext(), wasteList);
        guidanceRecWasteList.setLayoutManager(new LinearLayoutManager(requireContext()));
        guidanceRecWasteList.setAdapter(wasteGuidanceAdapter);

        //check which waste type example list page is navigated to
        int wasteType = requireArguments().getInt("wasteType", -1);

        //database connection
        guidanceDatabaseReference = FirebaseFirestore.getInstance();

        if(wasteType == 0)//recyclable waste
        {
            wasteList.clear();// clear the previous result
            wasteTypeTitle.setText("Recyclable Waste");
            showWasteList(0);

        }
        if(wasteType == 1)//hazardous waste
        {
            wasteList.clear();
            wasteTypeTitle.setText("Hazardous Waste");
            showWasteList(1);

        }
        if(wasteType == 2)//household food waste
        {
            wasteList.clear();
            wasteTypeTitle.setText("Household Food Waste");
            showWasteList(2);

        }
        if(wasteType == 3)//residual waste
        {
            wasteList.clear();
            wasteTypeTitle.setText("Residual Waste");
            showWasteList(3);

        }

        return rootView;
    }


    //connected to the database, fetch the data from entity of recycling_guidance
    public void showWasteList(int wasteType)
    {
        guidanceDatabaseReference.collection("recycling_guidance")
                .whereEqualTo("waste_type", wasteType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String wasteName = documentSnapshot.getString("waste_name");
                        String wasteDesc = documentSnapshot.getString("waste_desc");
                        String result = wasteName + " _ " + "" + " _ " + wasteDesc;
                        wasteList.add(result);
                    }
                    wasteGuidanceAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->{
                    Log.e("FirestoreError", "Error fetching data: " + e.getMessage(), e);
                });
    }
}