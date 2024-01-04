package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Login extends AppCompatActivity {
    private EditText emailTextView, passwordTextView;
    private TextView forgotPassword,signIn;
    private AppCompatButton Btn;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityManager connManager = (ConnectivityManager) Login.this.getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities networkCapabilities =  connManager.getNetworkCapabilities(connManager.getActiveNetwork());
            if (networkCapabilities == null) {
                Toast.makeText(this, "You are offline now", Toast.LENGTH_SHORT).show();
                Intent offlineView = new Intent(Login.this, OfflineView.class);
                startActivity(offlineView);
                finish();
            }
        } else {
            mAuth = FirebaseAuth.getInstance();

            if (mAuth.getCurrentUser() != null) {
                Intent isLoggedin;
                if (mAuth.getCurrentUser().getEmail().equals("admin@email.com")) {
                    isLoggedin  = new Intent(Login.this, AdminActivity.class);
                } else {
                    isLoggedin = new Intent(Login.this, MainActivity.class);
                }

                startActivity(isLoggedin);
                finish();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(1000);

        } catch (InterruptedException e) {
            Log.e(Login.this.toString(), e.getMessage());
        }
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailTextView = findViewById(R.id.editTextTextEmailAddress);
        passwordTextView = findViewById(R.id.editTextTextPassword);
        Btn = findViewById(R.id.Login_btn);
        forgotPassword = findViewById(R.id.textView5);
        signIn = findViewById(R.id.textView6);

        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loginUserAccount(){
        // Take the value of two edit texts in Strings
        String email, password;
        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email!!", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!!", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseFirestore firestoreRef = FirebaseFirestore.getInstance();
        firestoreRef.collection("user").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                  if (!task.getResult().isEmpty()) {
                      String verified = (String) task.getResult().iterator().next().get("verified");
                      String role = (String) task.getResult().iterator().next().get("role");
                      if (verified.equals("approved")) {
                          // signin existing user
                          mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                              @Override
                              public void onComplete(@NonNull Task<AuthResult> task) {
                                  if (task.isSuccessful()) {
                                      Toast.makeText(getApplicationContext(), "Login successful!!", Toast.LENGTH_LONG).show();
                                      Intent intent;
                                      if (role.equals("Admin")) {
                                          intent = new Intent(Login.this, AdminActivity.class);
                                      } else {
                                          intent = new Intent(Login.this, MainActivity.class);
                                      }
                                      startActivity(intent);
                                      finish();
                                  }

                                  else {
                                      // sign-in failed
                                      Toast.makeText(getApplicationContext(), "Login failed!!", Toast.LENGTH_LONG).show();
                                  }
                              }
                          });
                      } else {
                          Toast.makeText(Login.this, "Account not verified!", Toast.LENGTH_SHORT).show();
                      }
                  } else {
                      Toast.makeText(Login.this, "Login unsuccessful!", Toast.LENGTH_SHORT).show();
                  }
                }
            }
        });


    }
}