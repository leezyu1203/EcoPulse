package com.example.ecopulse.Community;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecopulse.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {
    private RecyclerView RVCommunityPosts;
    private ProgressBar PBLoadCommunity;
    private TextView TVNoPostMsg;
    private ImageAdapter adapter;

    private FirebaseFirestore db;
    private List<DocumentSnapshot> eventList;
    private TextView title;
    private ImageButton backButton;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_community, container, false);


        getActivity().findViewById(R.id.backButton).setVisibility(View.GONE);
        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Community");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RVCommunityPosts = view.findViewById(R.id.RVCommunityPosts);
        RVCommunityPosts.setHasFixedSize(true);
        RVCommunityPosts.setLayoutManager(new LinearLayoutManager(getActivity()));
        PBLoadCommunity = view.findViewById(R.id.PBLoadCommunity);
        TVNoPostMsg = view.findViewById(R.id.TVNoPostMsg);

        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Recycling Community");
        backButton = getActivity().findViewById(R.id.backButton);
        backButton.setVisibility(View.INVISIBLE);

        eventList = new ArrayList<>();
        adapter = new ImageAdapter(getActivity(), eventList);
        RVCommunityPosts.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        db.collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e(TAG, "CommunityFragment error", error);
                        }

                        if(value != null){
                            eventList.clear();

                            for(QueryDocumentSnapshot snapshot: value) {
                                eventList.add(snapshot);
                            }

                            if(eventList.isEmpty()) {
                                TVNoPostMsg.setVisibility(View.VISIBLE);
                            } else {
                                TVNoPostMsg.setVisibility(View.INVISIBLE);
                            }

                            adapter.notifyDataSetChanged();

                            PBLoadCommunity.setVisibility(View.INVISIBLE);
                        }
                    }
                });
        /*db.collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            eventList.clear();

                            for(QueryDocumentSnapshot snapshot: task.getResult()) {
                                eventList.add(snapshot);
                            }

                            if(eventList.isEmpty()){
                                TVNoPostMsg.setVisibility(View.VISIBLE);
                            } else {
                                TVNoPostMsg.setVisibility(View.INVISIBLE);
                            }

                            adapter.notifyDataSetChanged();

                            PBLoadCommunity.setVisibility(View.INVISIBLE);
                        }
                    }
                });*/

        adapter.setOnItemClickListener(position -> {});
        /*storage = FirebaseStorage.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("events");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();

                for(DataSnapshot communitySnapshot : snapshot.getChildren()) {
                    UploadEvent upload = communitySnapshot.getValue(UploadEvent.class);
                    //upload.setKey(communitySnapshot.getKey());
                    eventList.add(upload);
                }

                if(eventList.isEmpty()) {
                    TVNoPostMsg.setVisibility(View.VISIBLE);
                } else {
                    TVNoPostMsg.setVisibility(View.GONE);
                }
                
                adapter.notifyDataSetChanged();

                PBLoadCommunity.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                PBLoadCommunity.setVisibility(View.INVISIBLE);
            }
        });*/
    }
}