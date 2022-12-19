package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class RegisterUniversityActivity extends AppCompatActivity {

    TextView mHaveAccountTv;
    EditText mName,mEmailEt, mPasswordEt;
    Button mRegisterBtn;

    //progressbar to display while registering user
    AlertDialog dialog;

    //declare an instance of firebase
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_university);

        mHaveAccountTv=findViewById(R.id.have_accoutnTvU);
        mEmailEt=findViewById(R.id.emailEtU);
        mPasswordEt=findViewById(R.id.passwordEtU);
        mName=findViewById(R.id.universityName);
        mRegisterBtn=findViewById(R.id.registerBtnU);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setMessage("registering university... ");


        //handle textView Listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUniversityActivity.this, LoginUniversityActivity.class));
                finish();
            }
        });

        //handle registerbtn
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input name, Email, password
                String name=mName.getText().toString().trim();
                String email=mEmailEt.getText().toString().trim();
                String password=mPasswordEt.getText().toString().trim();

                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email edit
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length()<6){
                    //set error and focus to password edit
                    mPasswordEt.setError("Password length at least 6 characters");
                    mPasswordEt.setFocusable(true);
                }

                else{
                    registerUser(email, password);
                }

            }


        });

    }

    private void registerUser(String email, String password) {
        //email and passord patterns is valid, show progress dialog and start registering users
        dialog.show();
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();

                            String name2=mName.getText().toString().toUpperCase();
                            String email2=mEmailEt.getText().toString();
                            String password2=mPasswordEt.getText().toString();


                            FirebaseUser user = mAuth.getCurrentUser();

                            //get user email and uid from auth
                            String email=user.getEmail();
                            String uid=user.getUid();
                            String name=user.getDisplayName();

                            //REALTIME DATABASE
                            //when user is registered store user infor in firebase realtime database too
                            // using hashMaps
                            HashMap<Object, String> hashMap=new HashMap<>();
                            //put infor on hashmap
                            /*
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name2);
                            hashMap.put("image", "");//later
                            hashMap.put("cover", "");//later
                            hashMap.put("password", password2);

                             */
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name2);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", "");
                            hashMap.put("university", "");
                            hashMap.put("course", "");
                            hashMap.put("image", "");//later
                            hashMap.put("cover", "");//later
                            hashMap.put("password", password2);
                            hashMap.put("accountType", "University");



                            //firebase instance
                            FirebaseDatabase database=FirebaseDatabase.getInstance();

                            //PATH TO STORE USERS DATA NAMED USERS -----------------------------------------------------------------------------------------------WILL CONSIDER THE PATH WHILE SEPARATING DOCTORS AND PATIENTS
                            DatabaseReference reference=database.getReference("users");
                            //put the data within hashMaps
                            reference.child(uid).setValue(hashMap);



                            Intent intent = new Intent(RegisterUniversityActivity.this, DashboardUniversityActivity.class);
                            startActivity(intent);
                            //startActivity(new Intent(RegisterPatientActivity.this, DashboardPatientActivity.class));
                            finish();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(RegisterUniversityActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(RegisterUniversityActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}