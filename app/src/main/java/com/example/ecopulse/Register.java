package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class Register extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextRepassword;
    private AppCompatButton buttonReg;

    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://pmxmad-default-rtdb.firebaseio.com/");

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editTextName = findViewById(R.id.editTextTextName);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        editTextRepassword = findViewById(R.id.editTextTextRePassword);
        buttonReg = findViewById(R.id.SignUp_btn);
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register(){
        String name,email, password, rePassword;
        name = String.valueOf(editTextName.getText());
        email = String.valueOf(editTextEmail.getText());
        password = String.valueOf(editTextPassword.getText());
        rePassword = String.valueOf(editTextRepassword.getText());

        if (TextUtils.isEmpty(name)||TextUtils.isEmpty(email)||TextUtils.isEmpty(password)||TextUtils.isEmpty(rePassword))
        {
            Toast.makeText(Register.this,"Please fill all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        else if (!password.equals(rePassword)){
            Toast.makeText(Register.this,"Password are not matching",Toast.LENGTH_SHORT).show();
            return;
        }

        else {

            databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    // check if phone is not registered before

                    if (snapshot.hasChild()){
                        Toast.makeText(Register.this,"Phone is already registered",Toast.LENGTH_SHORT).show();
                    }

                    else{
                        // sending data to realtime database
                        //
                        databaseReference.child("users").child().child("fullname").setValue(name);
                        databaseReference.child("users").child().child("email").setValue(email);
                        databaseReference.child("user").child().child("password").setValue(password);
                        Toast.makeText(Register.this,"User registered successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }


}