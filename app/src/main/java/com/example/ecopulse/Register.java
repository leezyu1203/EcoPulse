package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    // UI elements
    private EditText editTextName, editTextEmail, editTextPassword, editTextRepassword, editTextPhone,editTextAddress, editTextOpening, editTextType;
    private RadioGroup radioGroup;
    private AppCompatButton buttonReg;

    private FirebaseFirestore databaseReference;
    private FirebaseAuth mAuth;

    private TextView loginText, openingTV, typeTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase components
        databaseReference = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        editTextName = findViewById(R.id.editTextTextName);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        editTextRepassword = findViewById(R.id.editTextTextRePassword);
        editTextPhone = findViewById(R.id.editTextTextPhone);
        editTextAddress = findViewById(R.id.editTextTextAddress);
        editTextOpening = findViewById(R.id.editTextTextOpening);
        editTextType = findViewById(R.id.editTextTextType);
        openingTV = findViewById(R.id.openingTV);
        typeTV = findViewById(R.id.typeTV);
        radioGroup = findViewById(R.id.radioGroup);
        buttonReg = findViewById(R.id.SignUp_btn);

        // Set onClickListener for the registration button
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        // Set onClickListener for the login text
        loginText = findViewById(R.id.login_text);
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();
            }
        });


        // Set listener for radio button changes
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton btn = findViewById(radioGroup.getCheckedRadioButtonId());
                String role = btn.getText().toString();

                // Show or hide additional fields based on the selected role
                if (role.equals("Recycling Center Collaborator")) {
                    openingTV.setVisibility(View.VISIBLE);
                    editTextOpening.setVisibility(View.VISIBLE);
                    typeTV.setVisibility(View.VISIBLE);
                    editTextType.setVisibility(View.VISIBLE);
                } else {
                    openingTV.setVisibility(View.GONE);
                    editTextOpening.setVisibility(View.GONE);
                    typeTV.setVisibility(View.GONE);
                    editTextType.setVisibility(View.GONE);
                }
            }
        });
    }

    // Method to handle user registration
    private void register(){
        String name,email, password, rePassword,phone, address, opening, type;
        name = String.valueOf(editTextName.getText());
        email = String.valueOf(editTextEmail.getText());
        password = String.valueOf(editTextPassword.getText());
        rePassword = String.valueOf(editTextRepassword.getText());
        phone = String.valueOf(editTextPhone.getText());
        address = String.valueOf(editTextAddress.getText());
        opening = String.valueOf(editTextOpening.getText());
        type = String.valueOf(editTextType.getText());

        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        String role = radioButton.getText().toString();

        // Validate input fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address) || (role.equals("Recycling Center Collaborator") && (TextUtils.isEmpty(opening) ||TextUtils.isEmpty(type) ))) {
            Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Register.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(rePassword)) {
            Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Check if the email already exists
            databaseReference.collection("user").whereEqualTo("email",email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                // Check whether the email exists before
                                if (!task.getResult().isEmpty()){
                                    Toast.makeText(getApplicationContext(),"Email already exists. Please choose a different email.",Toast.LENGTH_LONG).show();
                                } else {
                                    // Create user with email and password
                                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                String userId = mAuth.getCurrentUser().getUid();
                                                Map<String, Object> user = new HashMap<>();
                                                user.put("username", name);
                                                user.put("email", email);
                                                user.put("phone", phone);
                                                user.put("role", role);
                                                user.put("address", address);
                                                if (role.contains("Collaborator")) {
                                                    user.put("verified", "pending");
                                                    if (role.equals("Recycling Center Collaborator")) {
                                                        user.put("opening", opening);
                                                        user.put("type", type);
                                                    }
                                                } else {
                                                    user.put("verified", "approved");
                                                }

                                                // Create a new document with the user's UID
                                                // Keep the information into the database
                                                databaseReference.collection("user")
                                                        .document(userId)
                                                        .set(user)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Intent intent = new Intent(Register.this, Login.class);
                                                                    startActivity(intent);
                                                                    Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    Log.e("RegisterActivity", "Failed to write user data to Firestore", task.getException());
                                                                    Toast.makeText(getApplicationContext(), "Failed to register. Please try again.", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                // Handle the error during user authentication
                                                Log.e("RegisterActivity", "Failed to create user", task.getException());
                                                Toast.makeText(getApplicationContext(), "Failed to register. Please try again.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                // Handle the error during Firestore query
                                Log.e("RegisterActivity", "Failed to check email existence in Firestore", task.getException());
                                Toast.makeText(getApplicationContext(), "Failed to register. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }

    }

    public void checkButton(View view) {

    }




}