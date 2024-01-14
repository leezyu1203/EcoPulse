package com.example.ecopulse.AuthenticationAndProfile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecopulse.AuthenticationAndProfile.BaseProfile;
import com.example.ecopulse.R;


public class Profile_user extends BaseProfile {

    // Default constructor
    public Profile_user() {
        // Required empty public constructor
    }

    // Override method to get the layout resource ID
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_profile_user;
    }

    // Override method to get the Firestore collection path
    @Override
    protected String getCollectionPath() {
        return "user";
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
