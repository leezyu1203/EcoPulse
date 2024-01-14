package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPassword extends AppCompatActivity {

    // UI elements
    private Button btnReset;
    private ImageButton btnBack;
    private EditText edtEmail;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // String to store the entered email
    private String strEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);

        // Initialize UI elements
        btnBack = findViewById(R.id.backButton);
        btnReset = findViewById(R.id.buttonReset);
        edtEmail = findViewById(R.id.Email);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Set onClickListener for the reset button
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered email
                strEmail = edtEmail.getText().toString().trim();

                // Check if the email is not empty
                if (!TextUtils.isEmpty(strEmail)){
                    // Call the ResetPassword method
                    ResetPassword();
                } else {
                    // Show an error message if the email field is empty
                    edtEmail.setError("Email field can't be empty");
                }
            }
        });

        // Set onClickListener for the back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the Login activity
                Intent intent = new Intent(ForgotPassword.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Method to reset the password
    private void ResetPassword() {
        mAuth.sendPasswordResetEmail(strEmail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Show a success message and navigate to the Login activity
                        Toast.makeText(ForgotPassword.this, "Reset Password link has been sent", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ForgotPassword.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show an error message if the password reset fails
                        Toast.makeText(ForgotPassword.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
