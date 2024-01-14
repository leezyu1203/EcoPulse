package com.example.ecopulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

// Abstract class serving as the base for user profiles
public abstract class BaseProfile extends Fragment {

    // Firebase authentication instance
    protected FirebaseAuth mAuth;

    // Current user instance
    protected FirebaseUser currentUser;

    // Firestore instance
    protected FirebaseFirestore firestore;

    // UI elements
    protected TextView username, email, phone, address, profileName, opening, type;
    protected ImageView profilePic;
    protected Button logout, updateProfile;

    // Listener for user data changes in Firestore
    protected ListenerRegistration userDataListener;

    private TextView title = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResourceId(), container, false);

        // Set visibility of the back button
        getActivity().findViewById(R.id.backButton).setVisibility(View.GONE);

        // Set title
        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Profile");

        // Check if the user is logged in
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Initialize UI elements
            username = view.findViewById(R.id.name);
            email = view.findViewById(R.id.email);
            phone = view.findViewById(R.id.phone);
            address = view.findViewById(R.id.address);
            opening = view.findViewById(R.id.opening);
            type = view.findViewById(R.id.type);
            profileName = view.findViewById(R.id.profile_name);
            profilePic = view.findViewById(R.id.profile_img);
            updateProfile = view.findViewById(R.id.updateProfileButton);

            // Set up listener for user data changes in Firestore
            userDataListener = firestore.collection(getCollectionPath()).document(uid)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            // Handle error
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Retrieve user data from Firestore
                            String emailValue = documentSnapshot.getString("email");
                            String usernameValue = documentSnapshot.getString("username");
                            String phoneValue = documentSnapshot.getString("phone");
                            String addressValue = documentSnapshot.getString("address");
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            // Update UI with retrieved data
                            username.setText(usernameValue);
                            email.setText(emailValue);
                            phone.setText(phoneValue);
                            address.setText(addressValue);
                            profileName.setText(usernameValue);

                            if (opening != null && type != null) {
                                String openingValue = documentSnapshot.getString("opening");
                                String typeValue = documentSnapshot.getString("type");
                                opening.setText(openingValue);
                                type.setText(typeValue);
                            }

                            // Load and display profile image using Picasso library
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).fit().centerInside().into(profilePic);
                            }
                        }
                    });
        }

        // Set onClickListener for the profile picture to navigate to UploadProfileImage activity
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadProfileImage.class);
                startActivity(intent);
            }
        });

        // Set onClickListener for the update profile button to navigate to Update fragment
        updateProfile.setOnClickListener(v -> {
            Update updateFragment = new Update();
            FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() if in a Fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(getCurrentContainerId(), updateFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        // Set onClickListener for the logout button to sign out the user
        logout = view.findViewById(R.id.button);
        logout.setOnClickListener(v -> {
            mAuth.signOut();

            // Remove listeners for other fragments
            if (RequestFragment.getListener() != null) {
                RequestFragment.getListener().remove();
            }

            if (reminderMainFragment.getListener() != null) {
                reminderMainFragment.getListener().remove();
            }

            if (MainActivity.getListener() != null) {
                MainActivity.getListener().remove();
            }

            // Navigate to the Login activity
            Intent loginIntent = new Intent(getActivity(), Login.class);
            startActivity(loginIntent);
            getActivity().finish();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove Firestore listener when the view is destroyed
        if (userDataListener != null) {
            userDataListener.remove();
        }
    }

    // Abstract methods to be implemented by subclasses
    protected abstract int getLayoutResourceId();
    protected abstract String getCollectionPath();
    protected int getCurrentContainerId() {
        View view = getView();
        if (view != null && view.getParent() != null) {
            return ((ViewGroup) view.getParent()).getId();
        }
        return View.NO_ID;
    }
}
