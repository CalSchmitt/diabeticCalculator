package com.example.diabeticcalculator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private EditText bloodSugar;
    private EditText carbs;
    private Button calculateButton, resendCode, resetButton;
    private TextView totalUnits, verifyMessage, subBy, divBy, carbDivide, equation;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    String subtract;
    String divide;
    String divideBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bloodSugar = findViewById(R.id.etCurrentBS);
        //subBy = findViewById(R.id.tvBSMinus);
       //divBy = findViewById(R.id.tvBSDivide);
        //carbDivide = findViewById(R.id.tvBSCarbDiv);
        carbs = findViewById(R.id.etCarbsEaten);
        calculateButton = findViewById(R.id.button);
        totalUnits = findViewById(R.id.unitsDisplayed);
        resetButton = findViewById(R.id.button2);
        equation = findViewById(R.id.tvEquation);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                //subBy.setText("Subtract: " + documentSnapshot.getString("BS-Minus"));
                //divBy.setText("BS Div: " + documentSnapshot.getString("BS-Divide"));
                //carbDivide.setText("Carb Div: " + documentSnapshot.getString("Carb-Divide"));

                subtract = documentSnapshot.getString("BS-Minus");
                divide = documentSnapshot.getString("BS-Divide");
                divideBy = documentSnapshot.getString("Carb-Divide");
            }
        });

        resendCode = findViewById(R.id.resendCode);
        verifyMessage = findViewById(R.id.tvEmailAlert);

        FirebaseUser user = fAuth.getCurrentUser();
        if(!user.isEmailVerified()){
            resendCode.setVisibility(View.VISIBLE);
            verifyMessage.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Send Verification Link
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "Verification has been sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "onFailure: Email not sent " + e.getMessage());

                        }
                    });
                }
            });
        }

        //Calculate button method
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateTotal();
            }
        });

        //Reset button method
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }


    //Reset method
    public void reset(){
        bloodSugar.getText().clear();
        carbs.getText().clear();
        totalUnits.setText("");
        equation.setText("");
    }

    //Calculate method
    public void calculateTotal(){
        String bs = bloodSugar.getText().toString();
        if(bs == null || bs.length() == 0){
            Toast.makeText(this, "Enter Your Current Blood Sugar", Toast.LENGTH_SHORT).show();
        }

        //Calculation for correction
        int bloodSugar = Integer.parseInt(bs);
        double sub = Integer.parseInt(subtract);
        double div = Integer.parseInt(divide);

        double correction = (bloodSugar - sub) / div;

        //Calculation for carbohydrates
        double totalCarb = Integer.parseInt(carbs.getText().toString());
        double carbDiv = Integer.parseInt(divideBy);

        double dosage = totalCarb / carbDiv;

        double totalDosage = correction + dosage;
        DecimalFormat df = new DecimalFormat("0.00");
        String output = df.format(totalDosage) + " Units";
        String equate = "(" +"(" + bs + " - " + sub + ")" + " / " + div + ") + " + "(" + totalCarb + " / " + div + ")";
        equation.setText(equate);
        totalUnits.setText(output);
    }

    //Logout method
    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), login.class));
        finish();
    }
}