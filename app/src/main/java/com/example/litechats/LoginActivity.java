package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    TextView notHaveAccountTv;
    TextView mRecoverPassTv;
    EditText  mEmailEt, mPasswordEt;
    Button mLoginBtn;
    //declare an instance of firebase
    private FirebaseAuth mAuth;

    //progressbar to display while registering user
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        notHaveAccountTv=findViewById(R.id.nothave_accoutnTv);
        mEmailEt=findViewById(R.id.emailEtLogin);
        mPasswordEt=findViewById(R.id.passwordEtLogin);
        mLoginBtn=findViewById(R.id.LoginBtn);
        mRecoverPassTv=findViewById(R.id.recoverPassTv);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        //mLogin btn listener
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String email=mEmailEt.getText().toString().trim();
                String passw=mPasswordEt.getText().toString().trim();

                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email edit
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }

                else{
                    LoginUser(email, passw);
                }
            }
        });


        //not have account
        notHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        //recover pass textview click
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowRecoverPasswordDialog();
            }
        });
    }

    private void LoginUser(String email, String passw) {
        //show progress dialogues
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setMessage("Login in progress... ");



        //email and passord patterns is valid
        dialog.show();
        mAuth.signInWithEmailAndPassword(email,passw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismiss progress dialogue
                            dialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();

                            String email1=mEmailEt.getText().toString();
                            String password1=mPasswordEt.getText().toString();
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            ///passing data to the dashboard activity
                            intent.putExtra("email1", email1);
                            intent.putExtra("password1", password1);

                            //user logged in, start profile activity
                            startActivity(intent);
                            finish();
                        } else {
                            //if sign in fails

                            //dismiss progress dialogue
                            dialog.dismiss();

                            Toast.makeText(LoginActivity.this, "Authentication failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, "Authentication failed \n"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recover password");

        //set layout linear layout
        LinearLayout linearLayout=new LinearLayout(this);

        //views to set in the layout
        EditText emailEt=new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        //set the min width of editView to fit a text
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email=emailEt.getText().toString().trim();
                //recover email
                beginRecovery(email);
            }
        });
        //buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //diamiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        //show progress dialogues
        dialog.setMessage("Sending email....");
        dialog.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Failed ...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show proper error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}