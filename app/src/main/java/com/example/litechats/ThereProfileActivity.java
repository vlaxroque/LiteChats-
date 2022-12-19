package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.litechats.Adapters.AdapterPosts;
import com.example.litechats.Adapters.AdapterStudents;
import com.example.litechats.Models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    ImageView avatarIv,coverIv;
    TextView nameTv, emailTv, phoneTv, universityTV, courseTv;
    ImageView profileImage;
    TextView followingTv, followBtn, unfollowBtn;

    FirebaseAuth firebaseAuth;

    RecyclerView postRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid, hisEmail, hisName, hisImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        profileImage=findViewById(R.id.profileImage);
        coverIv=findViewById(R.id.coverIv);
        nameTv=findViewById(R.id.nameTv);
        emailTv=findViewById(R.id.emailTv);
        phoneTv=findViewById(R.id.phoneTv);
        universityTV=findViewById(R.id.universityTv);
        courseTv=findViewById(R.id.courseTv);
        postRecyclerView =findViewById(R.id.recyclerview_postsThere);

        followingTv = findViewById(R.id.followingTv);
        followBtn = findViewById(R.id.followBtn);
        unfollowBtn = findViewById(R.id.unfollowBtn);
        followBtn.setBackgroundResource(R.drawable.shape_rect02);

        //initalize firebase
        firebaseAuth=FirebaseAuth.getInstance();
        
        //get uid of clicked user to review his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid"); //get his id from adapter post
        hisEmail = intent.getStringExtra("email");
        hisName = intent.getStringExtra("name");
        hisImage = intent.getStringExtra("image");


        followingTv.setText(hisName.toUpperCase()+" is not following you");
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start following
                //if its the current user, hide the buttons
                followUser(uid, hisName, hisEmail); //hisUid
            }
        });

        unfollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //unfollow user
                unfollowUser(uid); //hisUid
            }
        });


        postList = new ArrayList<>();

        Query query=FirebaseDatabase.getInstance().getReference("users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untill required data get
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();
                    String university=""+ds.child("university").getValue();
                    String course=""+ds.child("course").getValue();


                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    universityTV.setText(university);
                    courseTv.setText(course);

                    try {
                        //if image is received  then
                        Picasso.get().load(image).into(profileImage);
                    }
                    catch (Exception e){
                        //if there is an exeception while getting the image
                        Picasso.get().load(R.drawable.ic_baseline_add_a_photo_24).into(profileImage);
                    }

                    try {
                        //if image is received  then
                        Picasso.get().load(cover ).into(coverIv);
                    }
                    catch (Exception e){
                        //if there is an exception while getting the image
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        checkUserStatus();
        loadHisPosts();

        checkIsFollowed(uid);
        imFollowedOrNot(uid);
    }

    private void imFollowedOrNot(String hisUID){
        //first check if sender(current user) is followed by receiver or not
        //Logic ; if uid of the sender(current user) exists in "Following" of reciver then sender(current user) is followed, otherwise not
        //if followed then just display a message e.g You are followed by that user, cant send that messag
        //if not....
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUID).child("Following").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                               followingTv.setText(hisName.toUpperCase()+" is already following you");
                            }
                            else {
                                followingTv.setText(hisName.toUpperCase()+" is Not following you");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFollowed(String hisUID) {
        //check each user, if followed or not
        //if uid of user exists in "Following" then that user is followed, otherwise not

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).child("Following").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                followBtn.setText("Already Following");
                                followBtn.setBackgroundResource(R.drawable.shape_rect03);
                            }
                            else {
                                followBtn.setText("Follow");
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void unfollowUser(String hisUID) {
        //Unfollow user by removing uid from current users Following node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).child("Following").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (ds.exists()){
                                //remove following user data from current users followList
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //unfollow success
                                                Toast.makeText(ThereProfileActivity.this, "unFollowed successfully...", Toast.LENGTH_SHORT).show();
                                                followBtn.setText("Follow");
                                                followBtn.setBackgroundResource(R.drawable.shape_rect02);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unfollow
                                                Toast.makeText(ThereProfileActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void followUser(String hisUID, String hisName, String hisEmail) {
        //follow the user by adding uid to current users BlockedUsers node

        //put values in hashmap to put to db
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);
        hashMap.put("name", hisName);
        hashMap.put("email", hisEmail);
        hashMap.put("image", hisImage);



        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).child("Following").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ThereProfileActivity.this, "Followed successfully...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ThereProfileActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadHisPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show new posts
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        postRecyclerView.setLayoutManager(layoutManager);

        //int post lists
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set adapter to recycler view
                    postRecyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchHistoryPosts(String searchQuery){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show new posts
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        postRecyclerView.setLayoutManager(layoutManager);

        //int post lists
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())) {

                        //add to list
                        postList.add(myPosts);

                    }

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set adapter to recycler view
                    postRecyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        if (id==R.id.action_video_call){
            startActivity(new Intent(this, VideoCallActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);

        MenuItem item=menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){
                    searchHistoryPosts(query);
                }
                else {
                    //search text empty, get all users
                    loadHisPosts();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //if search query is not empty then search
                if (!TextUtils.isEmpty(newText.trim())){
                    searchHistoryPosts(newText);
                }
                else {
                    //search text empty, get all posts
                    loadHisPosts();
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    public  void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user !=null){
            //user is signed in stay here
            //set email for loggged in user
            //mProfileTv.setText(user.getEmail());
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, LoginAsActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}