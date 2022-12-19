package com.example.litechats;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterAsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_as);

        Button btnStudentCreate=findViewById(R.id.btnStudentCreate);
        btnStudentCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStudentRegisterActivity();
            }
        });

        Button btnUniversityCreate=findViewById(R.id.btnUniversityCreate);
        btnUniversityCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUniversityRegisterActivity();
            }
        });
    }

    public void openStudentRegisterActivity(){
        Intent intent=new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void openUniversityRegisterActivity(){
        Intent intent=new Intent(this, RegisterUniversityActivity.class);
        startActivity(intent);
    }
}