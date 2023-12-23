package com.example.ecopulse;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ecopulse.Model.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class uploadFragmentReminder extends Fragment {
    Button addTaskButton;
    EditText uploadTitle,uploadDesc,uploadDate,uploadTime;
    TextView title;
    int mYear, mMonth, mDay;
    int mHour, mMinute;
    AlarmManager alarmManager;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    private PendingIntent pendingIntent;


    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_upload_task,container,false);
        title = (TextView) getActivity().findViewById(R.id.current_title);
        title.setText("Reminder");
        uploadTitle=rootView.findViewById(R.id.addTaskTitle);
        createNotificationChannel();
        uploadDesc=rootView.findViewById(R.id.addTaskDescription);
        uploadDate=rootView.findViewById(R.id.taskDate);
        uploadTime=rootView.findViewById(R.id.taskTime);
        addTaskButton=rootView.findViewById(R.id.AddTask);

        Bundle bundle = getArguments();
        if(bundle != null) {
            String eventID = bundle.getString("eventID");
            Log.d(TAG, "Reminder Event Check: " + eventID);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events").document(eventID)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(error != null){
                                Log.e(TAG, error.getMessage(), error);
                            }

                            if(value.exists() && value != null) {
                                UploadEvent event = value.toObject(UploadEvent.class);
                                uploadTitle.setText(event.getEventName());
                                uploadDesc.setText("Venue: " + event.getEventVenue());
                                uploadDate.setText(event.getEventDate());
                                uploadTime.setText(event.getEventStartTime());
                            }
                        }
                    });
        }

        uploadDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDateDialog();
            }
        });

        uploadTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimeDialog();
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();

            }
        });

        return rootView;
    }


    public void uploadData(){


        String title = uploadTitle.getText().toString();
        String description = uploadDesc.getText().toString();
        String date = uploadDate.getText().toString();
        String time = uploadTime.getText().toString();
        String requestCode= DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());


        Task task = new Task(title, description, date, time,requestCode);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add the task to Firestore
        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        setAlarm(task);
                        Toast.makeText(requireContext(),"Successfully create task",Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




    public void openDateDialog(){
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(requireContext(), R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                uploadDate.setText(dayOfMonth+"-"+(month+1)+"-"+year);
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
                uploadTime.setText(hourOfDay+":"+minute);
            }
        },mHour,mMinute,false);
        timePickerDialog.show();

    }
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            CharSequence name="MADReminderChannel";
            String description= "Channel For Alarm Manager";
            int importance= NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel= new NotificationChannel("reminder",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }

    private void setAlarm(Task task){

        alarmManager=(AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        int requestCode = task.getRequestCode().hashCode();

        Intent intent =new Intent(requireContext(),AlarmReceiver.class);
        intent.putExtra("title",task.getTaskTitle());
        intent.putExtra("desc",task.getTaskDescription());

        pendingIntent=PendingIntent.getBroadcast(requireContext(),requestCode,intent, PendingIntent.FLAG_IMMUTABLE);


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
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(requireContext(), "Reminder set successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Please select a future date and time", Toast.LENGTH_SHORT).show();
        }
    }

}
