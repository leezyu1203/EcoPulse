package com.example.reminder3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.reminder3.Model.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    DatabaseReference databaseReference;
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

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
        updateTitle.setText(bundle.getString("Title"));
        updateDate.setText(bundle.getString("Date"));
        updateDesc.setText(bundle.getString("Desc"));
        updateTime.setText(bundle.getString("Time"));
        key=bundle.getString("Key");
        }
        databaseReference= FirebaseDatabase.getInstance().getReference("task").child(key);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
                Intent intent=new Intent(updateActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
        dltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference= FirebaseDatabase.getInstance().getReference("task");
                reference.child(key).removeValue();
                Toast.makeText(updateActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });


    }

    public void updateData(){
        title=updateTitle.getText().toString().trim();
        desc=updateDesc.getText().toString().trim();
        date=updateDate.getText().toString().trim();
        time=updateTime.getText().toString();


        Task task=new Task(title,desc,date,time);

        databaseReference.setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(updateActivity.this,"Updated",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(updateActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

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