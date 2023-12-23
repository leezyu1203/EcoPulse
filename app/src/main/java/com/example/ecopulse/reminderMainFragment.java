package com.example.ecopulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecopulse.Model.Task;
import com.example.ecopulse.adapter.Myadapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class reminderMainFragment extends Fragment {
    FirebaseFirestore db;
    RecyclerView recyclerView;
    List<Task> taskList;
    TextView title;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminder_main, container, false);

        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Reminder");

        ImageButton BtnAddTask = rootView.findViewById(R.id.BtnAddTask);
        recyclerView = rootView.findViewById(R.id.taskRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskList = new ArrayList<>();
        Myadapter adapter = new Myadapter(taskList, requireContext());
        recyclerView.setAdapter(adapter);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference taskRef = db.collection("tasks");
        taskRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null) {
                    taskList.clear();
                    for (QueryDocumentSnapshot document : snapshot) {
                        Task task = document.toObject(Task.class);
                        task.setKey(document.getId());
                        taskList.add(task);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

                BtnAddTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Create a FragmentManager
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                        // Create a FragmentTransaction
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        // Replace the current fragment/container with UploadFragment
                        uploadFragmentReminder uploadFragment = new uploadFragmentReminder();
                        fragmentTransaction.replace(R.id.main_fragment, uploadFragment); // Replace 'fragment_container' with your container ID
                        fragmentTransaction.addToBackStack(null); // Add transaction to back stack

                        // Commit the transaction
                        fragmentTransaction.commit();
                    }
                });

        return rootView;
    }
}