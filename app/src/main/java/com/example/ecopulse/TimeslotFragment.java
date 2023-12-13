package com.example.ecopulse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class TimeslotFragment extends Fragment {
    private View timeslotLocation = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        timeslotLocation = inflater.inflate(R.layout.timeslot_fragment, container, false);

        return timeslotLocation;
    }
}
