package com.example.ecopulse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SchedulePickUp extends AppCompatActivity {
    TextView centerName = null;
    TextView centerAddress = null;
    TextView centerContact = null;
    String name = null;
    String address = null;
    String contact = null;

    String selectedTimeslot = null;
    String userContact = null;
    String userAddress = null;
    String userNote = null;

    AppCompatButton submit = null;


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
        centerName.setText(name);
        centerAddress.setText(address);
        centerContact.setText(contact);

        Spinner timeslotSpinner = (Spinner) findViewById(R.id.timeslot);

        String[] items = new String[] { "9:00 am", "10:00 am", "11:00 am", "12:00 am" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, R.id.spinnerItem, items);

        timeslotSpinner.setAdapter(adapter);

        timeslotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTimeslot = adapterView.getSelectedItem().toString();
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
                Toast.makeText(SchedulePickUp.this, "Successfully schedule a pick up! Kindly wait patiently for the reply!", Toast.LENGTH_LONG).show();
                finish();

            }
        });


    }
}
