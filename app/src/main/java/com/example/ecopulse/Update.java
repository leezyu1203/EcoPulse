package com.example.ecopulse;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

// Fragment to update user profile information
public class Update extends Fragment {

    // Firebase authentication instance
    private FirebaseAuth mAuth;

    // Firestore instance
    private FirebaseFirestore firestore;

    // Current user instance
    private FirebaseUser currentUser;

    // UI elements
    private EditText nameEditText, phoneEditText, addressEditText, openingEditText, typeEditText;
    private TextView openingTV, typeTV;
    private Button updateButton;

    // Back button
    private ImageButton backBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_update, container, false);
        getActivity().findViewById(R.id.backButton).setVisibility(View.VISIBLE);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        nameEditText = view.findViewById(R.id.editTextTextName);
        phoneEditText = view.findViewById(R.id.editTextTextPhone);
        addressEditText = view.findViewById(R.id.editTextTextAddress);
        openingEditText = view.findViewById(R.id.editTextOpening);
        typeEditText = view.findViewById(R.id.editTextType);
        openingTV = view.findViewById(R.id.Opening);
        typeTV = view.findViewById(R.id.Type);
        updateButton = view.findViewById(R.id.updateButton);

        // Get user data and set up UI
        getUserData();

        // Set onClickListener for the update button
        updateButton.setOnClickListener(v -> updateUserProfile());
    }

    // Method to update user profile information
    private void updateUserProfile() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = firestore.collection("user").document(uid);

            // Update user data based on EditText fields
            if (openingEditText.getVisibility() == View.GONE && typeEditText.getVisibility() == View.GONE) {
                userRef.update(
                        "username", nameEditText.getText().toString(),
                        "phone", phoneEditText.getText().toString(),
                        "address", addressEditText.getText().toString()
                ).addOnSuccessListener(aVoid -> {
                    // Handle success
                    Toast.makeText(getActivity(), "Update Successfully", Toast.LENGTH_LONG).show();
                    goBackToProfile();
                }).addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(getActivity(), "Cannot successfully", Toast.LENGTH_LONG).show();
                });
            } else {
                userRef.update(
                        "username", nameEditText.getText().toString(),
                        "phone", phoneEditText.getText().toString(),
                        "address", addressEditText.getText().toString(),
                        "opening", openingEditText.getText().toString(),
                        "type", typeEditText.getText().toString()
                ).addOnSuccessListener(aVoid -> {
                    // Handle success
                    Toast.makeText(getActivity(), "Update Successfully", Toast.LENGTH_LONG).show();
                    goBackToProfile();
                }).addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(getActivity(), "Cannot successfully", Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    // Method to retrieve user data from Firestore
    private void getUserData() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = firestore.collection("user").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Retrieve user data
                    String name = documentSnapshot.getString("username");
                    String phone = documentSnapshot.getString("phone");
                    String address = documentSnapshot.getString("address");
                    String role = documentSnapshot.getString("role");

                    // Set retrieved data to EditText fields
                    nameEditText.setText(name);
                    phoneEditText.setText(phone);
                    addressEditText.setText(address);

                    if (role.equals("Recycling Center Collaborator")) {
                        openingTV.setVisibility(View.VISIBLE);
                        openingEditText.setVisibility(View.VISIBLE);
                        typeTV.setVisibility(View.VISIBLE);
                        typeEditText.setVisibility(View.VISIBLE);

                        String openingVal = documentSnapshot.getString("opening");
                        String typeVal = documentSnapshot.getString("type");

                        openingEditText.setText(openingVal);
                        typeEditText.setText(typeVal);
                    } else {
                        openingTV.setVisibility(View.GONE);
                        openingEditText.setVisibility(View.GONE);
                        typeTV.setVisibility(View.GONE);
                        typeEditText.setVisibility(View.GONE);
                    }
                }
            }).addOnFailureListener(e -> {
                // Handle failure
            });
        }
    }

    // Method to navigate back to the profile fragment
    private void goBackToProfile() {
        // Get the FragmentManager
        FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() if in a Fragment

        // Pop the topmost fragment from the back stack
        fragmentManager.popBackStack();
    }
}
