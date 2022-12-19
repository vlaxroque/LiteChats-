package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    TextView mHaveAccountTv;
    EditText mName,mEmailEt, mPasswordEt, mPhone, mUniversity, mCourse;
    Button mRegisterBtn;

    //progressbar to display while registering user
    AlertDialog dialog;

    //declare an instance of firebase
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mHaveAccountTv=findViewById(R.id.have_accoutnTv);
        mEmailEt=findViewById(R.id.emailEt);
        mPasswordEt=findViewById(R.id.passwordEt);
        mName=findViewById(R.id.fullNameEt);
        mRegisterBtn=findViewById(R.id.registerBtn);
        mPhone=findViewById(R.id.phoneEt);
        mHaveAccountTv=findViewById(R.id.have_accoutnTv);
        mUniversity=findViewById(R.id.universityEt);
        mCourse=findViewById(R.id.courseEt);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setMessage("registering user... ");



        //handle textView Listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        //handle registerbtn
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input name, Email, password
                String fullName=mName.getText().toString().trim();
                String email=mEmailEt.getText().toString().trim();
                String password=mPasswordEt.getText().toString().trim();
                String phone=mPhone.getText().toString().trim();

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

                            String name2=mName.getText().toString();
                            String phone2=mPhone.getText().toString();
                            String email2=mEmailEt.getText().toString();
                            String password2=mPasswordEt.getText().toString();
                            String university=mUniversity.getText().toString();
                            String course=mCourse.getText().toString();


                            FirebaseUser user = mAuth.getCurrentUser();

                            //get user email and uid from auth
                            String email=user.getEmail();
                            String uid=user.getUid();
                            String name=user.getDisplayName();
                            String phone=user.getPhoneNumber();

                            //REALTIME DATABASE
                            //when user is registered store user infor in firebase realtime database too
                            // using hashMaps
                            HashMap<Object, String> hashMap=new HashMap<>();
                            //put infor on hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name2);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", phone2);
                            hashMap.put("university", university);
                            hashMap.put("course", course);
                            hashMap.put("image", "");//later
                            hashMap.put("cover", "");//later
                            hashMap.put("password", password2);
                            hashMap.put("accountType", "Student");

                            //firebase instance
                            FirebaseDatabase database=FirebaseDatabase.getInstance();

                            //PATH TO STORE USERS DATA NAMED USERS -----------------------------------------------------------------------------------------------WILL CONSIDER THE PATH WHILE SEPARATING DOCTORS AND PATIENTS
                            DatabaseReference reference=database.getReference("users");
                            //put the data within hashMaps
                            reference.child(uid).setValue(hashMap);



                            Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            //startActivity(new Intent(RegisterPatientActivity.this, DashboardPatientActivity.class));
                            finish();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}