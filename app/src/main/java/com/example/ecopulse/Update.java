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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Update extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private EditText nameEditText, emailEditText, phoneEditText, addressEditText;
    private Button updateButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        nameEditText = view.findViewById(R.id.editTextTextName);
        emailEditText = view.findViewById(R.id.editTextTextEmailAddress);
        phoneEditText = view.findViewById(R.id.editTextTextPhone);
        addressEditText = view.findViewById(R.id.editTextTextAddress);

        updateButton = view.findViewById(R.id.updateButton);

        getUserData();

        updateButton.setOnClickListener(v -> updateUserProfile());
    }

    private void updateUserProfile() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = firestore.collection("user").document(uid);

            // Update user data based on EditText fields
            userRef.update(
                    "username", nameEditText.getText().toString(),
                    "email", emailEditText.getText().toString(),
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
        }
    }

    private void getUserData() {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = firestore.collection("user").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("username");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String address = documentSnapshot.getString("address");


                    // Set retrieved data to EditText fields
                    nameEditText.setText(name);
                    emailEditText.setText(email);
                    phoneEditText.setText(phone);
                    addressEditText.setText(address);
                }
            }).addOnFailureListener(e -> {
                // Handle failure
            });
        }

    }

    private void goBackToProfile() {
        // Get the FragmentManager
        FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() if in a Fragment

        // Pop the topmost fragment from the back stack
        fragmentManager.popBackStack();
    }
}