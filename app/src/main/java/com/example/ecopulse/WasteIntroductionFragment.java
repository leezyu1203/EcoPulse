package com.example.ecopulse;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

        introTitle = rootView.findViewById(R.id.introTitle);//page title
        introWasteTypeImage = rootView.findViewById(R.id.introWasteTypeImage);//image
        introWasteTypeDesc = rootView.findViewById(R.id.introWasteTypeDesc);//description title
        introWasteTypeExample = rootView.findViewById(R.id.introWasteTypeExample);//example title
        introWasteTypeTips = rootView.findViewById(R.id.introWasteTypeTips);//tips title
        showMoreExamples = rootView.findViewById(R.id.showWasteList);//show more examples button

        //database connection
        guidanceFirestore = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        int pageNumber =args.getInt("pageNumber", -1);//check which page redirected to

        if (pageNumber == 0) { //recyclable waste introduction page
            introTitle.setText("Recyclable Waste");
            introWasteTypeImage.setImageResource(R.drawable.recyclable_waste_0_widget);
            fetchWasteIntroductionData("recyclable_waste");
            setPageToNavigate(0);
        }

        else if (pageNumber == 1) { //hazardous waste introduction page
            fetchWasteIntroductionData("hazardous_waste");
            introTitle.setText("Hazardous Waste");
            introWasteTypeImage.setImageResource(R.drawable.hazardous_waste_1_widget);

            setPageToNavigate(1);
        }

        else if (pageNumber == 2) { //household food waste introduction page
            fetchWasteIntroductionData("householdFood_waste");
            introTitle.setText("Household Food Waste");
            introWasteTypeImage.setImageResource(R.drawable.household_food_waste_2_widget);
            setPageToNavigate(2);
        }

        else if (pageNumber == 3) {  //residual waste introduction page
            fetchWasteIntroductionData("residual_waste");
            introTitle.setText("Residual Waste");
            introWasteTypeImage.setImageResource(R.drawable.residual_waste_3_widget);
            setPageToNavigate(3);
        }
        return rootView;
    }

    //connected the database, fetching data from the entity of waste_intro
    private void fetchWasteIntroductionData(String docID) {
        DocumentReference docRef = guidanceFirestore.collection("waste_intro").document(docID);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String wasteDescription = documentSnapshot.getString("waste_desc");
                String wasteExample = documentSnapshot.getString("waste_example");
                String wasteTips = documentSnapshot.getString("waste_tips");

                //special case for description, need to split the lines
                wasteTips = wasteTips.replaceAll("\\\\n", "\n");
                introWasteTypeDesc.setText(wasteDescription);
                introWasteTypeExample.setText(wasteExample);
                introWasteTypeTips.setText(wasteTips);

            }
        }).addOnFailureListener(e -> {
            // Handle failure or errors here
        });
    }


    //navigate to another page when the more example button is clicked
    public void setPageToNavigate(int wasteType)
    {
        showMoreExamples.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WasteExampleListFragment fragment = new WasteExampleListFragment();
                Bundle args = new Bundle();
                args.putInt("wasteType", wasteType); //store the waste type as args for later reference
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