package com.example.litechats;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    //https://www.flaticon.com/free-icon/shop_610365?term=grocery+store&page=1&position=44

    private FirebaseAuth firebaseAuth;
    //progressbar to display while registering user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make full screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        //start login after 1 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null){
                    //user not logged in, start login activity
                    startActivity(new Intent(SplashActivity.this, LoginAsActivity.class));
                    finish();
                }
                else {
                    //user is logged in, check user type
                    checkUserType();
                }
            }
        }, 1000);
    }

    private void checkUserType() {
        //if user is seller, start seller main activity
        //if user is user, start user main activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = ""+snapshot.child("accountType").getValue();
                        if (accountType.equals("Student")){
                            //user is seller
                            startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                            finish();
                        }
                        else if(accountType.equals("University")){
                            //user is buyer
                            startActivity(new Intent(SplashActivity.this, DashboardUniversityActivity.class));
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}