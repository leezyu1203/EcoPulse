package com.example.ecopulse.Community;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.ecopulse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManagePostsFragment extends Fragment {
    private AppCompatButton BtnAddPost;
    private RecyclerView RVManagesPost;
    private ProgressBar PBManagePost;
    private TextView TVNoManagePostMsg;
    private TextView title;

    private ManagePostsAdapter adapter;
    private List<DocumentSnapshot> eventList;

    private FirebaseFirestore db;

    public ManagePostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_posts, container, false);
        getActivity().findViewById(R.id.backButton).setVisibility(View.VISIBLE);
        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Manage Post");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RVManagesPost = view.findViewById(R.id.RVManagePosts);
        PBManagePost = view.findViewById(R.id.PBManagePost);
        BtnAddPost = view.findViewById(R.id.BtnAddPost);
        TVNoManagePostMsg = view.findViewById(R.id.TVNoManagePostMsg);

        eventList = new ArrayList<>();
        adapter = new ManagePostsAdapter(requireContext(), eventList);
        RVManagesPost.setLayoutManager(new LinearLayoutManager(requireContext()));
        RVManagesPost.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = new Bundle();
        String userEmail = "";
        if(user != null) {
            userEmail = user.getEmail();
        }

        db.collection("user")
                .whereEqualTo("email", userEmail)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value1, @Nullable FirebaseFirestoreException error1) {
                        if(error1 != null) {
                            Log.e(TAG, "ManagePostsFragment user error", error1);
                        }

                        if(value1 != null && !value1.isEmpty()) {
                            DocumentSnapshot snapshot = value1.getDocuments().get(0);
                            String userID = snapshot.getId();

                            bundle.putString("userID", userID);

                            db.collection("events")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .whereEqualTo("userID", userID)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if(error != null) {
                                                Log.e(TAG, "ManagePostsFragment event error: ", error);
                                            }

                                            if(value != null) {
                                                eventList.clear();

                                                for(QueryDocumentSnapshot snapshots : value) {
                                                    eventList.add(snapshots);
                                                }

                                                if(eventList.isEmpty()) {
                                                    TVNoManagePostMsg.setVisibility(View.VISIBLE);
                                                } else {
                                                    TVNoManagePostMsg.setVisibility(View.INVISIBLE);
                                                }

                                                adapter.notifyDataSetChanged();

                                                PBManagePost.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
        /*db.collection("user")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult().getDocuments().iterator().next();
                            String userID = snapshot.getId();

                            bundle.putString("userID", userID);

                            db.collection("events")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .whereEqualTo("userID", userID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()) {
                                                eventList.clear();

                                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                    eventList.add(documentSnapshot);
                                                }

                                                if(eventList.isEmpty()) {
                                                    TVNoManagePostMsg.setVisibility(View.VISIBLE);
                                                } else {
                                                    TVNoManagePostMsg.setVisibility(View.INVISIBLE);
                                                }

                                                adapter.notifyDataSetChanged();

                                                PBManagePost.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            PBManagePost.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        }
                    }
                });*/

        /*db.collection("user")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult().getDocuments().iterator().next();
                            String userID = snapshot.getId();

                            bundle.putString("userID", userID);

                            db.collection("events")
                                    .whereEqualTo("userID", userID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()) {
                                                eventList.clear();

                                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                    UploadEvent current = documentSnapshot.toObject(UploadEvent.class);
                                                    eventList.add(current);
                                                }

                                                if(eventList.isEmpty()) {
                                                    TVNoManagePostMsg.setVisibility(View.VISIBLE);
                                                } else {
                                                    TVNoManagePostMsg.setVisibility(View.INVISIBLE);
                                                }

                                                adapter.notifyDataSetChanged();

                                                PBManagePost.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            PBManagePost.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        }
                    }
                });*/

        adapter.setOnItemClickListener(position -> { });

        BtnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                CollaboratorUploadFragment cuf = new CollaboratorUploadFragment();
                cuf.setArguments(bundle);

                transaction.replace(R.id.main_fragment, cuf);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}