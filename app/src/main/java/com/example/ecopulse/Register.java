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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextRepassword, editTextPhone,editTextAddress;
    private TextView logIn;
    private RadioGroup radioGroup;
    private AppCompatButton buttonReg;
    private FirebaseFirestore databaseReference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseReference = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextName = findViewById(R.id.editTextTextName);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        editTextRepassword = findViewById(R.id.editTextTextRePassword);
        editTextPhone = findViewById(R.id.editTextTextPhone);
        editTextAddress = findViewById(R.id.editTextTextAddress);

        radioGroup = findViewById(R.id.radioGroup);
        buttonReg = findViewById(R.id.SignUp_btn);
        logIn = findViewById(R.id.textView5);
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void register(){
        String name,email, password, rePassword,phone,address;
        name = String.valueOf(editTextName.getText());
        email = String.valueOf(editTextEmail.getText());
        password = String.valueOf(editTextPassword.getText());
        rePassword = String.valueOf(editTextRepassword.getText());
        phone = String.valueOf(editTextPhone.getText());
        address = String.valueOf(editTextAddress.getText());

        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        String role = radioButton.getText().toString();

        // Inside the register() method

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(phone)) {
            Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Register.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(rePassword)) {
            Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        } else {

            databaseReference.collection("user").whereEqualTo("email",email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){

                                if (!task.getResult().isEmpty()){
                                    Toast.makeText(getApplicationContext(),"Email already exists. Please choose a different email.",Toast.LENGTH_LONG).show();

                                }else {
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
                                                user.put("address",address);

                                                // Create a new document with the user's UID
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