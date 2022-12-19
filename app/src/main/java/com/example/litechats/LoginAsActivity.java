package com.example.litechats;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginAsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as);


        Button btnStudentLogin=findViewById(R.id.btnStudentLogin);
        btnStudentLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStudentLogin();
            }
        });

        Button btnUniversityLogin=findViewById(R.id.btnUniversityLogin);
        btnUniversityLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUniversityLogin();
            }
        });
    }

    public void openStudentLogin(){
        Intent intent=new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openUniversityLogin(){
        Intent intent=new Intent(this, LoginUniversityActivity.class);
        startActivity(intent);
    }
}