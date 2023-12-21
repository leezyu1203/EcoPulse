package com.example.ecopulse;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {
    private RecyclerView RVCommunityPosts;
    private ProgressBar PBLoadCommunity;
    private TextView TVNoPostMsg;
    private ImageAdapter adapter;

    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    private List<UploadEvent> eventList;
    private TextView title;
    private ImageButton backButton;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_community, container, false);
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

        storage = FirebaseStorage.getInstance();
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
        });
    }
}