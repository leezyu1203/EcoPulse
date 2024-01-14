package com.example.ecopulse.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecopulse.Reminder.Model.Task;
import com.example.ecopulse.R;
import com.example.ecopulse.Reminder.adapter.Myadapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class reminderMainFragment extends Fragment {

    RecyclerView recyclerView;
    List<Task> taskList;
    TextView title;

    private static ListenerRegistration listener;

    public static ListenerRegistration getListener() {
        return listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminder_main, container, false);

        getActivity().findViewById(R.id.backButton).setVisibility(View.GONE);
        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Reminder");

        ImageButton BtnAddTask = rootView.findViewById(R.id.BtnAddTask);
        recyclerView = rootView.findViewById(R.id.taskRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskList = new ArrayList<>();
        Myadapter adapter = new Myadapter(taskList, requireContext());
        recyclerView.setAdapter(adapter);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID=user.getUid();
        CollectionReference taskRef = db.collection("user").document(userID).collection("tasks");

        db.collection("user").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String role = task.getResult().get("role").toString();

                    listener = taskRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                    if (getContext() != null) {
                                        Intent intent = new Intent(getContext(), AlarmReceiver.class);
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), task.getRequestCode().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                                        alarmManager.cancel(pendingIntent);
                                        setAlarm(task);
                                    }
                                    taskList.add(task);
                                }


                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
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

    private void setAlarm(Task task){
        if (getContext() != null) {
            AlarmManager alarmManager=(AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            int requestCode = task.getRequestCode().hashCode();

            Intent intent =new Intent(getContext(),AlarmReceiver.class);
            intent.putExtra("title",task.getTaskTitle());
            intent.putExtra("desc",task.getTaskDescription());

            PendingIntent pendingIntent= PendingIntent.getBroadcast(getContext(),requestCode,intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


            // Parse the date and time strings to create a Calendar instance
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(dateFormat.parse(task.getDate() + " " + task.getFirstAlarmTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Set the alarm using AlarmManager
            if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                // Only set the alarm if it's in the future
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }

    }
}