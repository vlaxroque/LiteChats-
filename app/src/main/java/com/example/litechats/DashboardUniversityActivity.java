package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUniversityActivity extends AppCompatActivity {

    //firebase Auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;
    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_university);

        //init
        firebaseAuth=FirebaseAuth.getInstance();

        //bottom navigation
        BottomNavigationView navigationView=findViewById(R.id.navigation_university);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment transactions (-- set as default on start)
        HomeUniversityFragment homeFragment = new HomeUniversityFragment();
        FragmentTransaction fragmentTransactionDoctor=getSupportFragmentManager().beginTransaction();
        fragmentTransactionDoctor.replace(R.id.container_university, homeFragment, "");
        fragmentTransactionDoctor.commit();

        checkUserStatus();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //handle clicks
                    switch (item.getItemId()){
                        case R.id.nav_home_uni:
                            //home fragment transactions
                            HomeUniversityFragment homeFragment = new HomeUniversityFragment();
                            FragmentTransaction fragmentTransactionHome=getSupportFragmentManager().beginTransaction();
                            fragmentTransactionHome.replace(R.id.container_university, homeFragment, "");
                            fragmentTransactionHome.commit();
                            return true;

                        case R.id.nav_news_uni:
                            //newsFragment fragment transactions
                            NewsUniversityFragment newsUniversityFragment = new NewsUniversityFragment();
                            FragmentTransaction fragmentTransactionNewsUniversity=getSupportFragmentManager().beginTransaction();
                            fragmentTransactionNewsUniversity.replace(R.id.container_university, newsUniversityFragment, "");
                            fragmentTransactionNewsUniversity.commit();
                            return true;

                        case R.id.nav_profile_uni:
                            //profile fragment transactions
                            ProfileUniversityFragment profileFragment = new ProfileUniversityFragment();
                            FragmentTransaction fragmentTransactionProfile=getSupportFragmentManager().beginTransaction();
                            fragmentTransactionProfile.replace(R.id.container_university, profileFragment, "");
                            fragmentTransactionProfile.commit();

                            return true;


                    }

                    return false;
                }
            };

    // //check user status
    public  void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user !=null){
            //user is signed in stay here
            //set email for loggged in user
            //mProfileTv.setText(user.getEmail());

            mUID=user.getUid();

            //update token
            //updatetoken(FirebaseInstanceId.getInstance().getToken()); //call the token for auto login

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp=getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            //NB
            //update token
            //updateToken(FirebaseInstanceId.getInstance().getToken());
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(DashboardUniversityActivity.this, LoginAsActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start of the app
        checkUserStatus();
        super.onStart();
    }


}