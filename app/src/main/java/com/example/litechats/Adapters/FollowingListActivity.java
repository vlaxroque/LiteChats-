package com.example.litechats.Adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.litechats.Models.ModelStudents;
import com.example.litechats.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowingListActivity extends AppCompatActivity {


    RecyclerView recyclerview_following;
    AdapterStudents adapterStudents;
    List<ModelStudents> studentsList;

    //firebase Auth
    FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_list);

        recyclerview_following =findViewById(R.id.recyclerview_following);

        //initalize firebase
        firebaseAuth=FirebaseAuth.getInstance();

        studentsList = new ArrayList<>();

        //set its properties
        recyclerview_following.setHasFixedSize(true);
        recyclerview_following.setLayoutManager(new LinearLayoutManager(this));

        //init users list
        studentsList=new ArrayList<>();

        //get all users
        getAllUsers();

    }

    private void getAllUsers() {

        //get current user
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "users" containing infor
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).child("Following").orderByChild("uid")
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelStudents modelStudents= ds.getValue(ModelStudents.class);
                    //get all users except currently signed in user
                    if (!modelStudents.getUid().equals(fUser.getUid())){
                            studentsList.add(modelStudents);
                    }
                    //adapter
                    adapterStudents=new AdapterStudents(FollowingListActivity.this, studentsList);
                    //set recyclerView
                    recyclerview_following.setAdapter(adapterStudents);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}