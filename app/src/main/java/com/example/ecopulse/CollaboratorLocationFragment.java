package com.example.ecopulse;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
    private ImageButton backButton = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            String redirect = getArguments().getString("redirect");
            Log.d("REDIRECT", redirect);
            if (redirect.equals("request")) {
                replaceFragment(new RequestFragment());
            } else {
                replaceFragment(new TimeslotFragment());
            }
        } else {
            replaceFragment(new TimeslotFragment());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        collaboratorLocation = inflater.inflate(R.layout.fragment_collaborator_location, container, false);


        title = (TextView) getActivity().findViewById(R.id.current_title);

        if (title != null) {
            title.setText("Recycling Center Location");
        }

        backButton = getActivity().findViewById(R.id.backButton);
        backButton.setVisibility(View.INVISIBLE);

        timeslotButton = collaboratorLocation.findViewById(R.id.timeslot_button);
        requestButton = collaboratorLocation.findViewById(R.id.request_button);
        timeslotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTabColorAndNavigate(timeslotButton, requestButton, new TimeslotFragment());
            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTabColorAndNavigate(requestButton, timeslotButton, new RequestFragment());
            }
        });

        return collaboratorLocation;
    }

    private void changeTabColorAndNavigate(AppCompatButton selected, AppCompatButton notSelected, Fragment navigateTo) {
        int color = ContextCompat.getColor(getContext(), R.color.light_green);
        int black = ContextCompat.getColor(getContext(), R.color.black);
        int white = ContextCompat.getColor(getContext(), R.color.white);
        int dark_green = ContextCompat.getColor(getContext(), R.color.primary_dark_green);
        selected.setBackgroundColor(color);
        selected.setTextColor(black);
        notSelected.setBackgroundColor(dark_green);
        notSelected.setTextColor(white);
        replaceFragment(navigateTo);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.collaborator_location_fragment, fragment);
        fragmentTransaction.commit();
    }

}
