package com.example.ecopulse;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class CollaboratorLocationFragment extends Fragment {
    private View collaboratorLocation = null;
    private AppCompatButton timeslotButton = null;
    private AppCompatButton requestButton = null;

    private TextView title = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        collaboratorLocation = inflater.inflate(R.layout.fragment_collaborator_location, container, false);
        replaceFragment(new TimeslotFragment());

        title = (TextView) getActivity().findViewById(R.id.current_title);

        if (title != null) {
            title.setText("Recycling Center Location");
        }

        timeslotButton = collaboratorLocation.findViewById(R.id.timeslot_button);
        requestButton = collaboratorLocation.findViewById(R.id.request_button);
        timeslotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getContext(), R.color.light_green);
                int black = ContextCompat.getColor(getContext(), R.color.black);
                int white = ContextCompat.getColor(getContext(), R.color.white);
                int dark_green = ContextCompat.getColor(getContext(), R.color.primary_dark_green);
                timeslotButton.setBackgroundColor(color);
                timeslotButton.setTextColor(black);
                requestButton.setBackgroundColor(dark_green);
                requestButton.setTextColor(white);
                replaceFragment(new TimeslotFragment());
            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ContextCompat.getColor(getContext(), R.color.light_green);
                int black = ContextCompat.getColor(getContext(), R.color.black);
                int white = ContextCompat.getColor(getContext(), R.color.white);
                int dark_green = ContextCompat.getColor(getContext(), R.color.primary_dark_green);
                requestButton.setBackgroundColor(color);
                requestButton.setTextColor(black);
                timeslotButton.setBackgroundColor(dark_green);
                timeslotButton.setTextColor(white);
                replaceFragment(new RequestFragment());
            }
        });

        return collaboratorLocation;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.collaborator_location_fragment, fragment);
        fragmentTransaction.commit();
    }
}
