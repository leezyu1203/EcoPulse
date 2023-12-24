package com.example.ecopulse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public abstract class BaseProfile extends Fragment {

    protected FirebaseAuth mAuth;
    protected FirebaseUser currentUser;
    protected FirebaseFirestore firestore;
    protected TextView username, email, phone;
    protected Button logout;
    protected ListenerRegistration userDataListener;

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

        if (currentUser != null) {
            String uid = currentUser.getUid();
            username = view.findViewById(R.id.name);
            email = view.findViewById(R.id.email);
            phone = view.findViewById(R.id.phone);

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

                            username.setText(usernameValue);
                            email.setText(emailValue);
                            phone.setText(phoneValue);
                        }
                    });
        }

        logout = view.findViewById(R.id.button);
        logout.setOnClickListener(v -> mAuth.signOut());

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
}
