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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

public abstract class BaseProfile extends Fragment {

    protected FirebaseAuth mAuth;
    protected FirebaseUser currentUser;
    protected FirebaseFirestore firestore;
    protected TextView username, email, phone, address,profileName;
    protected ImageView profilePic;
    protected Button logout;

    protected Button updateProfile;
    protected ListenerRegistration userDataListener;

    private TextView title = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResourceId(), container, false);
        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Profile");

        if (currentUser != null) {
            String uid = currentUser.getUid();
            username = view.findViewById(R.id.name);
            email = view.findViewById(R.id.email);
            phone = view.findViewById(R.id.phone);
            address = view.findViewById(R.id.address);
            profileName = view.findViewById(R.id.profile_name);
            profilePic = view.findViewById(R.id.profile_img);
            updateProfile = view.findViewById(R.id.updateProfileButton);

            userDataListener = firestore.collection(getCollectionPath()).document(uid)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            // handle error
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String emailValue = documentSnapshot.getString("email");
                            String usernameValue = documentSnapshot.getString("username");
                            String phoneValue = documentSnapshot.getString("phone");
                            String addressValue = documentSnapshot.getString("address");
                            String imageUrl = documentSnapshot.getString("imageUrl");


                            username.setText(usernameValue);
                            email.setText(emailValue);
                            phone.setText(phoneValue);
                            address.setText(addressValue);
                            profileName.setText(usernameValue);

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).fit().centerInside().into(profilePic);
                            }
                        }
                    });
        }

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),UploadProfileImage.class);
                startActivity(intent);
            }
        });

        updateProfile.setOnClickListener(v->{
            Update updateFragment = new Update();
            FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() if in a Fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(getCurrentContainerId(), updateFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        logout = view.findViewById(R.id.button);
        logout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent loginIntent = new Intent(getActivity(), Login.class);
            startActivity(loginIntent);
            getActivity().finish();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userDataListener != null) {
            userDataListener.remove();
        }
    }

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
