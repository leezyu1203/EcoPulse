package com.example.ecopulse;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecopulse.Model.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;


public class updateActivity extends AppCompatActivity {
    int mYear, mMonth, mDay;
    int mHour, mMinute;
    AlarmManager alarmManager;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    Button updateButton,dltButton;
    EditText updateTitle,updateDesc,updateDate,updateTime;
    String title,desc,date,time,key;
    FirebaseFirestore db;
    //DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateButton=findViewById(R.id.updateButton);
        updateTitle=findViewById(R.id.updateTaskTitle);
        updateDesc=findViewById(R.id.updateTaskDescription);
        updateDate=findViewById(R.id.updateTaskDate);
        updateTime=findViewById(R.id.updateTaskTime);
        dltButton=findViewById(R.id.dltButton);



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

        db = FirebaseFirestore.getInstance();

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){

        updateTitle.setText(bundle.getString("Title"));
        updateDate.setText(bundle.getString("Date"));
        updateDesc.setText(bundle.getString("Desc"));
        updateTime.setText(bundle.getString("Time"));
        key=bundle.getString("Key");
        }


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
                Intent intent=new Intent(updateActivity.this, MainActivityReminder.class);
                startActivity(intent);

            }
        });
        dltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask();
            }
        });


    }
    public void updateData() {
        title = updateTitle.getText().toString().trim();
        desc = updateDesc.getText().toString().trim();
        date = updateDate.getText().toString().trim();
        time = updateTime.getText().toString().trim();

        DocumentReference docRef = db.collection("tasks").document(key);

        docRef.update(
                "taskTitle", title,
                "taskDescription", desc,
                "date", date,
                "firstAlarmTime", time
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(updateActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(updateActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(updateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteTask() {
        // Delete the task from Firebase
        db.collection("tasks").document(key)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(updateActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(updateActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(updateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        startActivity(new Intent(getApplicationContext(), MainActivityReminder.class));
        finish();
    }

    public void openDateDialog(){
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(updateActivity.this, new DatePickerDialog.OnDateSetListener() {
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
        timePickerDialog=new TimePickerDialog(updateActivity.this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                updateTime.setText(hourOfDay+":"+minute);
            }
        },mHour,mMinute,false);
        timePickerDialog.show();

    }
}