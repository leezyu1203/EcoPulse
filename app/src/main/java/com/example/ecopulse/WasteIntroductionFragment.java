package com.example.ecopulse;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class WasteIntroductionFragment extends Fragment {

    TextView introTitle, introWasteTypeDesc, introWasteTypeExample, introWasteTypeTips;
    ImageView introWasteTypeImage;

    Button showMoreExamples;
    FirebaseFirestore guidanceFirestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_waste_introduction, container, false);

        introTitle = rootView.findViewById(R.id.introTitle);
        introWasteTypeDesc = rootView.findViewById(R.id.introWasteTypeDesc);
        introWasteTypeExample = rootView.findViewById(R.id.introWasteTypeExample);
        introWasteTypeTips = rootView.findViewById(R.id.introWasteTypeTips);
        introWasteTypeImage = rootView.findViewById(R.id.introWasteTypeImage);
        showMoreExamples = rootView.findViewById(R.id.showWasteList);

        guidanceFirestore = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        int pageNumber =args.getInt("pageNumber", -1);

        if (pageNumber == 0) {
            fetchRecyclableWasteData("recyclable_waste");
            introTitle.setText("Recyclable Waste");
            introWasteTypeImage.setImageResource(R.drawable.recyclable_waste_0_widget);

            setPageToNavigate(0);
        }
        else if (pageNumber == 1) {
            fetchRecyclableWasteData("hazardous_waste");
            introTitle.setText("Hazardous Waste");
            introWasteTypeImage.setImageResource(R.drawable.hazardous_waste_1_widget);

            setPageToNavigate(1);
        }
        else if (pageNumber == 2) {
            fetchRecyclableWasteData("householdFood_waste");
            introTitle.setText("Household Food Waste");
            introWasteTypeImage.setImageResource(R.drawable.household_food_waste_2_widget);

            setPageToNavigate(2);
        }
        else if (pageNumber == 3) {
            fetchRecyclableWasteData("residual_waste");
            introTitle.setText("Residual Waste");
            introWasteTypeImage.setImageResource(R.drawable.residual_waste_3_widget);

            setPageToNavigate(3);
        }
        return rootView;

    }

    private void fetchRecyclableWasteData(String docID) {
        DocumentReference docRef = guidanceFirestore.collection("waste_intro").document(docID);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String wasteDescription = documentSnapshot.getString("waste_desc");
                String wasteExample = documentSnapshot.getString("waste_example");
                String wasteTips = documentSnapshot.getString("waste_tips");

                wasteTips = wasteTips.replaceAll("\\\\n", "\n");
                introWasteTypeDesc.setText(wasteDescription);
                introWasteTypeExample.setText(wasteExample);
                introWasteTypeTips.setText(wasteTips);


            }
        }).addOnFailureListener(e -> {
            // Handle failure or errors here
        });
    }


    public void setPageToNavigate(int wasteType)
    {
        showMoreExamples.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WasteListByTypeFragment fragment = new WasteListByTypeFragment();
                Bundle args = new Bundle();
                args.putInt("wasteType", wasteType);
                fragment.setArguments(args);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_fragment, fragment);
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });
    }
}