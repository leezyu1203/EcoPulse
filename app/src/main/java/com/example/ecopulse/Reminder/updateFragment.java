package com.example.ecopulse.Reminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ecopulse.Reminder.Model.Task;
import com.example.ecopulse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class updateFragment extends Fragment {
    int mYear, mMonth, mDay;
    int mHour, mMinute;
    AlarmManager alarmManager;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    Button updateButton,dltButton;
    EditText updateTitle,updateDesc,updateDate,updateTime;
    String title,desc,date,time,key;
    int requestCode;
    FirebaseFirestore db;
    PendingIntent pendingIntent;
    String userID;

    TextView warnText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        createNotificationChannel();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userID=user.getUid();
        View rootView = inflater.inflate(R.layout.fragment_update_task,container,false);
        getActivity().findViewById(R.id.backButton).setVisibility(View.VISIBLE);
        updateButton=rootView.findViewById(R.id.updateButton);
        updateTitle=rootView.findViewById(R.id.updateTaskTitle);
        updateDesc=rootView.findViewById(R.id.updateTaskDescription);
        updateDate=rootView.findViewById(R.id.updateTaskDate);
        updateTime=rootView.findViewById(R.id.updateTaskTime);
        dltButton=rootView.findViewById(R.id.dltButton);
        warnText = rootView.findViewById(R.id.warn_text);



        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            updateTitle.setText(getArguments().getString("Title", ""));
            updateDesc.setText(getArguments().getString("Desc", ""));
            updateDate.setText(getArguments().getString("Date", ""));
            updateTime.setText(getArguments().getString("Time", ""));
            key=getArguments().getString("Key", "");
            requestCode=getArguments().getString("requestCode","").hashCode();
        }

        if (getArguments().getString("Title","").equals("Recycling Pick Up Schedule")) {
            updateButton.setVisibility(View.GONE);
            updateTitle.setEnabled(false);
            updateDesc.setEnabled(false);
            updateDate.setEnabled(false);
            updateTime.setEnabled(false);
            warnText.setVisibility(View.VISIBLE);
            dltButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatePickup("cancelled", false);
                    cancelAlarm(requestCode);
                }
            });
        } else if (getArguments().getString("Title", "").equals("Pick Up Request")) {
            updateTitle.setEnabled(false);
            updateDesc.setEnabled(false);
            updateDate.setEnabled(false);
            updateTime.setEnabled(false);
            warnText.setVisibility(View.VISIBLE);
            updateButton.setText("Done");
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatePickup("done", true);
                    cancelAlarm(requestCode);
                }
            });

            dltButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatePickup("cancelled", true);
                    cancelAlarm(requestCode);
                }
            });
        } else {
            updateButton.setVisibility(View.VISIBLE);
            updateTitle.setEnabled(true);
            updateDesc.setEnabled(true);
            updateDate.setEnabled(true);
            updateTime.setEnabled(true);
            warnText.setVisibility(View.GONE);
            updateButton.setText("Update");
            updateDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDateDialog();
                }
            });

            updateTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openTimeDialog();
                }
            });

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData();
                    cancelAlarm(requestCode);

                }
            });
            dltButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteTask();
                    cancelAlarm(requestCode);
                }
            });
        }


        return rootView;
    }

    public void updateData() {
         FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                title = updateTitle.getText().toString().trim();
                desc = updateDesc.getText().toString().trim();
                date = updateDate.getText().toString().trim();
                time = updateTime.getText().toString().trim();
                String NewRequestCode= DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                cancelAlarm(requestCode);


                Task Ntask=new Task(title,desc,date,time,NewRequestCode);


                DocumentReference docRef = db.collection("user").document(userID).collection("tasks").document(key);

                docRef.update(
                        "taskTitle", title,
                        "taskDescription", desc,
                        "date", date,
                        "firstAlarmTime", time
                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (task.isSuccessful()) {
                    setAlarm(Ntask);
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().popBackStack();

                } else {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePickup(String status, boolean isColab) {
        date = updateDate.getText().toString().trim();
        time = updateTime.getText().toString().trim();
        db.collection("user").document(userID).collection("tasks").document(key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String pickUpID = task.getResult().get("userID").toString();

                    db.collection("pick_up_schedule").document(pickUpID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                String customerID = task.getResult().get("userID").toString();
                                String collabID = task.getResult().get("recyclingCenterID").toString();
                                db.collection("pick_up_schedule").document(pickUpID).update("status", status, "day", date, "time", time, "update_at", new Date().getTime()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            if (isColab) {
                                                db.collection("user").document(customerID).collection("tasks").whereEqualTo("userID",  pickUpID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            String taskKey = task.getResult().iterator().next().getId();
                                                            db.collection("user").document(customerID).collection("tasks").document(taskKey).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                                    db.collection("recycling_center_information").document(collabID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                String collabName = task.getResult().get("name").toString();
                                                                                Map<String, Object> messageObj = new HashMap<>();
                                                                                if (status.equals("cancelled")) {
                                                                                    messageObj.put("title", "Your pick up schedule on " + collabName + " has been cancelled!");
                                                                                    messageObj.put("desc", "Make your new request!");
                                                                                } else {
                                                                                    messageObj.put("title", "Your schedule on " + collabName + " has completed!");
                                                                                    messageObj.put("desc", "Thank you for using this app!");
                                                                                }
                                                                                messageObj.put("added_at", new Date().getTime());
                                                                                db.collection("user").document(customerID).collection("messages").add(messageObj);
                                                                                deleteTask();
                                                                            }
                                                                        }
                                                                    });

                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            } else {
                                                db.collection("recycling_center_information").document(collabID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            String recyclingCenterUserID = task.getResult().get("userID").toString();

                                                            db.collection("user").document(recyclingCenterUserID).collection("tasks").whereEqualTo("userID",  pickUpID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        String taskKey = task.getResult().iterator().next().getId();

                                                                        db.collection("user").document(recyclingCenterUserID).collection("tasks").document(taskKey).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                                                db.collection("recycling_center_information").document(collabID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            String collabUserID = task.getResult().get("userID"). toString();
                                                                                            db.collection("user").document(customerID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        String username = task.getResult().get("username").toString();
                                                                                                        Map<String, Object> messageObj = new HashMap<>();
                                                                                                        messageObj.put("title", "Pick up schedule has been cancelled by " + username + "!");
                                                                                                        messageObj.put("desc", "Skip this request!");
                                                                                                        messageObj.put("added_at", new Date().getTime());
                                                                                                        db.collection("user").document(collabUserID).collection("messages").add(messageObj);
                                                                                                        deleteTask();
                                                                                                    }
                                                                                                }
                                                                                            });

                                                                                        }
                                                                                    }
                                                                                });

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });


                }
            }
        });
    }



    public void deleteTask() {

        // Delete the task from Firebase
        db.collection("user").document(userID).collection("tasks").document(key)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Deletion failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }
    private void setAlarm(Task task){

        alarmManager=(AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        int requestCode = task.getRequestCode().hashCode();

        Intent intent =new Intent(requireContext(), AlarmReceiver.class);
        intent.putExtra("title",task.getTaskTitle());
        intent.putExtra("desc",task.getTaskDescription());

        pendingIntent=PendingIntent.getBroadcast(requireContext(),requestCode,intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


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
           // Toast.makeText(requireContext(), "Reminder set successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Please select a future date and time", Toast.LENGTH_SHORT).show();
        }
    }
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            CharSequence name="MADReminderChannel";
            String description= "Channel For Alarm Manager";
            int importance= NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel= new NotificationChannel("reminder",name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE) ;
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void openDateDialog(){
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(requireContext(), R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                updateDate.setText(dayOfMonth+"-"+(month+1)+"-"+year);
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public void openTimeDialog(){
        //get the current time
        final Calendar c = Calendar.getInstance();
        mHour=c.get(Calendar.HOUR_OF_DAY);
        mMinute=c.get(Calendar.MINUTE);

        //launch time picker
        timePickerDialog=new TimePickerDialog(requireContext(), R.style.DatePickerTheme, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                updateTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }
        },mHour,mMinute,false);
        timePickerDialog.show();

    }
    private void cancelAlarm(int requestCode) {
        alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);

        // Create a PendingIntent with the same request code used when setting the alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
       // Toast.makeText(requireContext(), "Alarm canceled successfully!", Toast.LENGTH_SHORT).show();
    }



}