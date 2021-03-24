package com.example.diabeticcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class authentication extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText firstName, lastName, email, password, bsMinus, bsDiv, carbDiv;
    Button register;
    TextView backToLogin;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        firstName = findViewById(R.id.etFirst);
        lastName = findViewById(R.id.etLast);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        bsMinus = findViewById(R.id.etBSMinus);
        bsDiv = findViewById(R.id.etDivByAuth);
        carbDiv = findViewById(R.id.etDivByAuth);
        register = findViewById(R.id.registerButton);
        backToLogin = findViewById(R.id.tvBackToLogin);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        //Check firebase to see if user is already created
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
        }

        //On "Register" button click
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String mEmail = email.getText().toString().trim();
                String mPassword = password.getText().toString().trim();
                String mFirstName = firstName.getText().toString();
                String mLastName = lastName.getText().toString();
                String mBSMinus = bsMinus.getText().toString();
                String mBSDiv = bsDiv.getText().toString();
                String mCarbDiv = carbDiv.getText().toString();



                //Check if Email and Password are empty
                if(TextUtils.isEmpty(mEmail)){
                    email.setError("Please enter an Email");
                    return;
                }
                if(TextUtils.isEmpty(mPassword)){
                    password.setError("Please enter a Password");
                    return;
                }
                if(mPassword.length() < 6){
                    password.setError("Password must be at least 6 characters");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);


                //Register the user in firebase
                fAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Send Verification Link
                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(authentication.this, "Verification has been sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent " + e.getMessage());

                                }
                            });

                            Toast.makeText(authentication.this, "User Created", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("First-Name", mFirstName);
                            user.put("Last-Name", mLastName);
                            user.put("BS-Minus", mBSMinus);
                            user.put("BS-Divide", mBSDiv);
                            user.put("Carb-Divide", mCarbDiv);
                            user.put("Email", mEmail);

                            //Response to if the user was created
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: user profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), login.class));
                        }else{
                            Toast.makeText(authentication.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        //Event when back button is clicked to go back to the login screen
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), login.class));
            }
        });
    }
}