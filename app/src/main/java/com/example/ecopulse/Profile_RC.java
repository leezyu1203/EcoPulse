package com.example.ecopulse;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Profile_RC extends BaseProfile {

    Button BtnManageTimeSlot;
    public Profile_RC(){

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_profile__r_c;
    }

    @Override
    protected String getCollectionPath() {
        return "user"; // or the path to user data in Firestore
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().findViewById(R.id.backButton).setVisibility(View.GONE);
        return view;
    }

}