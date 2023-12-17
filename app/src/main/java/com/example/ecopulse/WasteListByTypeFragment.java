package com.example.ecopulse;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AppCompatActivity;
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

public class WasteListByTypeFragment extends Fragment {

    RecyclerView guidanceRecWasteList;
    ArrayList<String> wasteList;
    WasteGuidanceAdapter wasteGuidanceAdapter;
    TextView wasteTypeTitle;
    FirebaseFirestore guidanceDatabaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_waste_list_by_type, container, false);

        wasteTypeTitle =rootView.findViewById (R.id.wasteTypeTitle);

        guidanceRecWasteList = rootView.findViewById(R.id.guidanceRecWasteList);
        wasteList = new ArrayList<>();
        wasteGuidanceAdapter =  new WasteGuidanceAdapter(requireContext(), wasteList);
        guidanceRecWasteList.setLayoutManager(new LinearLayoutManager(requireContext()));
        guidanceRecWasteList.setAdapter(wasteGuidanceAdapter);

        int wasteType = requireArguments().getInt("wasteType", -1);
        guidanceDatabaseReference = FirebaseFirestore.getInstance();

        if(wasteType == 0)
        {
            wasteList.clear();
            wasteTypeTitle.setText("Recyclable Waste");
            showWasteList(0);

        }
        if(wasteType == 1)
        {
            wasteList.clear();
            wasteTypeTitle.setText("Hazardous Waste");
            showWasteList(1);

        }
        if(wasteType == 2)
        {
            wasteList.clear();
            wasteTypeTitle.setText("Household Food Waste");
            showWasteList(2);

        }
        if(wasteType == 3)
        {
            wasteList.clear();
            wasteTypeTitle.setText("Residual Waste");
            showWasteList(3);

        }

        return rootView;
    }

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