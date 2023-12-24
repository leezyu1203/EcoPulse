package com.example.ecopulse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulePickUp extends AppCompatActivity {
    TextView centerName = null;
    TextView centerAddress = null;
    TextView centerContact = null;
    String name = null;
    String address = null;
    String contact = null;
    String id = null;

    String selectedTimeslot = null;
    String selectedDay = null;
    String userContact = null;
    String userAddress = null;
    String userNote = null;

    AppCompatButton submit = null;

    Map<String, ArrayList<String>> dayTimeMap = new HashMap<String, ArrayList<String>>();
    ArrayList<String> items = new ArrayList<>();
    ArrayList<String> days = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_pick_up_layout);
        centerName = findViewById(R.id.recycle_name);
        centerAddress = findViewById(R.id.recycle_address);
        centerContact = findViewById(R.id.recycle_contact);
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        address = intent.getStringExtra("address");
        contact = intent.getStringExtra("contact");
        id = intent.getStringExtra("id");
        centerName.setText(name);
        centerAddress.setText(address);
        centerContact.setText(contact);

        Spinner timeslotSpinner = (Spinner) findViewById(R.id.timeslot);
        Spinner daySpinner = (Spinner) findViewById(R.id.day);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, R.id.spinnerItem, items);
        ArrayAdapter<String> adapterDay = new ArrayAdapter<String>(this,
                R.layout.spinner_item, R.id.spinnerItem, days);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recycling_center_information").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> timeslot = (List<String>) task.getResult().getData().get("timeslot");
                    timeslot.stream().forEach((slot)-> {
                        String[] dayAndTime = slot.split(", ");

                        if (dayTimeMap.containsKey(dayAndTime[0])) {
                            if (!dayTimeMap.get(dayAndTime[0]).contains(dayAndTime[1])) {
                                dayTimeMap.get(dayAndTime[0]).add(dayAndTime[1]);
                            }
                        } else {
                            dayTimeMap.put(dayAndTime[0], new ArrayList<String>(Arrays.asList(dayAndTime[1])));
                        }
                    });

                    items.clear();
                    items.addAll(dayTimeMap.get(dayTimeMap.keySet().toArray()[0]));

                    Log.d("DEBUG", items.toString());
                    for (String day : dayTimeMap.keySet()) {
                        days.add(day);
                    }

                    adapter.notifyDataSetChanged();
                    adapterDay.notifyDataSetChanged();
                } else {
                    Log.e("Get Timeslot Failed", "GET TIME SLOT FAILED!");
                }
            }
        });

        timeslotSpinner.setAdapter(adapter);
        daySpinner.setAdapter(adapterDay);
        timeslotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTimeslot = adapterView.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDay = adapterView.getSelectedItem().toString();
                items.clear();
                items.addAll(dayTimeMap.get(selectedDay));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        submit = findViewById(R.id.submit_schedule);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText _userContact = findViewById(R.id.contact);
                EditText _userAddress = findViewById(R.id.address);
                EditText _userNote = findViewById(R.id.note);

                userContact = _userContact.getText().toString();
                userAddress = _userAddress.getText().toString();
                userNote = _userNote.getText().toString();

                if (userAddress.equals("")) {
                    Toast.makeText(SchedulePickUp.this, "Address field must not be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userContact.equals("")) {
                    Toast.makeText(SchedulePickUp.this, "Contact field must not be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userNote.equals("")) {
                    Toast.makeText(SchedulePickUp.this, "Note field must not be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userEmail = "";
                if (user != null) {
                    userEmail = user.getEmail();
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("user").whereEqualTo("email", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QueryDocumentSnapshot document1 = task.getResult().iterator().next();
                            String userID = document1.getId();
                            Map<String, Object> pickup_schedule = new HashMap<>();
                            pickup_schedule.put("address", userAddress);
                            pickup_schedule.put("contact", userContact);
                            pickup_schedule.put("day", selectedDay);
                            pickup_schedule.put("time", selectedTimeslot);
                            pickup_schedule.put("note", userNote);
                            pickup_schedule.put("status", "pending");
                            pickup_schedule.put("recyclingCenterID", id);
                            pickup_schedule.put("userID", userID);
                            db.collection("pick_up_schedule").add(pickup_schedule);


                        }
                    }
                });

                Toast.makeText(SchedulePickUp.this, "Successfully schedule a pick up! Kindly wait patiently for the reply!", Toast.LENGTH_LONG).show();
                finish();

            }
        });


    }
}