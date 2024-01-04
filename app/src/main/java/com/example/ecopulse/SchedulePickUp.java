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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulePickUp extends AppCompatActivity {
    TextView centerName, centerContact, centerAddress;
    String name, address, contact, id, selectedTimeslot, selectedDay, userContact, userAddress, userNote;

    AppCompatButton submit, cancel;

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

                    if (timeslot.isEmpty()) {
                        Toast.makeText(SchedulePickUp.this, "No Timeslot to be booked!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
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

                        for (String day : dayTimeMap.keySet()) {
                            days.add(day);
                        }

                        adapter.notifyDataSetChanged();
                        adapterDay.notifyDataSetChanged();
                    }
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
        cancel = findViewById(R.id.cancel_btn);
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
                            LocalTime inputLocalTime = LocalTime.parse(selectedTimeslot, DateTimeFormatter.ofPattern("h:mm a"));
                            LocalTime currentTime = LocalTime.now();

                            LocalDate today = LocalDate.now();
                            DayOfWeek targetDayOfWeek = DayOfWeek.valueOf(selectedDay.toUpperCase());
                            LocalDate nearestDate = today.with(targetDayOfWeek);

                            if (nearestDate.isBefore(today) || (nearestDate.isEqual(today) && currentTime.isAfter(inputLocalTime))) {
                                nearestDate = nearestDate.plusWeeks(1);
                            }

                            LocalDate localDate = LocalDate.parse(nearestDate.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                            String outputDate = localDate.format(formatter);
                            pickup_schedule.put("address", userAddress);
                            pickup_schedule.put("contact", userContact);
                            pickup_schedule.put("day", outputDate);
                            pickup_schedule.put("time", selectedTimeslot);
                            pickup_schedule.put("note", userNote);
                            pickup_schedule.put("status", "pending");
                            pickup_schedule.put("recyclingCenterID", id);
                            pickup_schedule.put("userID", userID);
                            pickup_schedule.put("update_at", new Date().getTime());
                            db.collection("pick_up_schedule").add(pickup_schedule).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        db.collection("recycling_center_information").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    String collabUserID = task.getResult().get("userID").toString();
                                                    Map<String, Object> messageObj = new HashMap<>();
                                                    messageObj.put("title", "You have received a new request!");
                                                    messageObj.put("desc", "Check your pending request!");
                                                    messageObj.put("added_at", new Date().getTime());
                                                    db.collection("user").document(collabUserID).collection("messages").add(messageObj);
                                                }
                                            }
                                        });
                                    }
                                }
                            });


                        }
                    }
                });

                Toast.makeText(SchedulePickUp.this, "Successfully schedule a pick up! Kindly wait patiently for the reply!", Toast.LENGTH_LONG).show();
                finish();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
