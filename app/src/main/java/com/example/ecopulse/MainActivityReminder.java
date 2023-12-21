package com.example.ecopulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecopulse.Model.Task;
import com.example.ecopulse.adapter.Myadapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivityReminder extends AppCompatActivity {
    RecyclerView recyclerView;
    List<Task> taskList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_remider);
        ImageButton BtnAddTask = findViewById(R.id.BtnAddTask);


        recyclerView=findViewById(R.id.taskRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList=new ArrayList<>();

        Myadapter adapter=new Myadapter(taskList,MainActivityReminder.this);
        recyclerView.setAdapter(adapter);


        // With Firestore initialization:
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference taskRef = db.collection("tasks");

        // Replace the ValueEventListener with Firestore SnapshotListener

        taskRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MainActivityReminder.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    taskList.clear();
                    for (QueryDocumentSnapshot document : value) {
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
                Intent intent = new Intent(MainActivityReminder.this, uploadTask.class);
                startActivity(intent);
            }
        });

    }
    /*protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_remider);
        ImageButton BtnAddTask = findViewById(R.id.BtnAddTask);

       // Button addTask=findViewById(R.id.AddTask);

        recyclerView=findViewById(R.id.taskRecycler);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList=new ArrayList<>();

        Myadapter adapter=new Myadapter(taskList,MainActivityReminder.this);
        recyclerView.setAdapter(adapter);

        databaseReference= FirebaseDatabase.getInstance().getReference("task");
        eventListener=databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                   Task task =dataSnapshot.getValue(Task.class);
                   task.setKey(dataSnapshot.getKey());
                   taskList.add(task);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivityReminder.this,"fail",Toast.LENGTH_SHORT).show();
            }
        });

         BtnAddTask.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(MainActivityReminder.this, uploadTask.class);
                 startActivity(intent);
             }
         });

        }*/
}