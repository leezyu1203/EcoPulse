package com.example.ecopulse.AuthenticationAndProfile;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.ecopulse.Community.ManagePostsFragment;
import com.example.ecopulse.R;


public class Profile_Collaborator extends BaseProfile {
    // Button for managing posts (Assuming it's declared in the XML layout)
    Button BtnManagePosts;

    // Default constructor
    public Profile_Collaborator() {
        // Required empty public constructor
    }

    // Override method to get the layout resource ID
    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_profile__collaborator;
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
        return view;
    }

    // Override onViewCreated method to perform actions after the view is created
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the visibility of the backButton to GONE
        getActivity().findViewById(R.id.backButton).setVisibility(View.GONE);

        // Find the Button for managing posts in the view
        BtnManagePosts = view.findViewById(R.id.BtnManagePosts);

        // Set onClickListener to navigate to the ManagePostsFragment
        BtnManagePosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a FragmentManager and FragmentTransaction
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                // Replace the current fragment with ManagePostsFragment
                transaction.replace(R.id.main_fragment, new ManagePostsFragment());

                // Add the transaction to the back stack
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        });
    }
}
