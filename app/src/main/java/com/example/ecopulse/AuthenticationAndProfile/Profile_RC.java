package com.example.ecopulse.AuthenticationAndProfile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecopulse.AuthenticationAndProfile.BaseProfile;
import com.example.ecopulse.R;


public class Profile_RC extends BaseProfile {


    // Default constructor
    public Profile_RC() {
        // Empty constructor
    }

    // Override method to get the layout resource ID
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_profile__r_c;
    }

    // Override method to get the Firestore collection path
    @Override
    protected String getCollectionPath() {
        return "user"; // or the path to user data in Firestore
    }

    // Override onCreateView method to customize the view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Call the onCreateView method from the superclass
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Set the visibility of the backButton to GONE
        getActivity().findViewById(R.id.backButton).setVisibility(View.GONE);

        // Return the modified view
        return view;
    }
}
