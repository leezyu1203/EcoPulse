package com.example.ecopulse;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
public class Profile_user extends BaseProfile {


    public Profile_user() {
        // Required empty public constructor
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_profile_user;
    }

    @Override
    protected String getCollectionPath() {
        return "user";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().findViewById(R.id.backButton).setVisibility(View.GONE);
        return view;
    }

}