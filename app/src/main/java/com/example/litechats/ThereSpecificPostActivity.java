package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.litechats.Adapters.AdapterComments;
import com.example.litechats.Adapters.AdapterPosts;
import com.example.litechats.Models.ModelComment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class ThereSpecificPostActivity extends AppCompatActivity {

    ImageView avatarIv,coverIv;
    TextView nameTv, emailTv, phoneTv, universityTV, courseTv;
    ImageView profileImage;

    FirebaseAuth firebaseAuth;

    RecyclerView postRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;


    ///////////////////////////////////////////////////////////////////////////////////////////////
    //progressbar to display while registering user
    AlertDialog dialog;

    //users infor
    String  hisUid, myName, myEmail, myUid, myDp, postId, pLikes, hisDp, hisName, pImage;

    boolean mProcessComment = false;
    boolean mProcessLike = false;


    String editTitle, editDescription, editImage;


    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn,  shareBtn;
    TextView pUniversityTv;
    LinearLayout profileLayout;
    TextView followingTv, followBtn, unfollowBtn;

    //add comment view
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    RecyclerView recyclerView;
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_specific_post);

        profileImage=findViewById(R.id.profileImage);
        coverIv=findViewById(R.id.coverIv);
        nameTv=findViewById(R.id.nameTv);
        emailTv=findViewById(R.id.emailTv);
        phoneTv=findViewById(R.id.phoneTv);
        universityTV=findViewById(R.id.universityTv);
        courseTv=findViewById(R.id.courseTv);
        postRecyclerView =findViewById(R.id.recyclerview_postsProfile);

        postRecyclerView = findViewById(R.id.recyclerview_postsThere);

        followingTv = findViewById(R.id.followingTv);
        followBtn = findViewById(R.id.followBtn);
        unfollowBtn = findViewById(R.id.unfollowBtn);
        followBtn.setBackgroundResource(R.drawable.shape_rect02);


        //initalize firebase
        firebaseAuth=FirebaseAuth.getInstance();

        String uid, hisEmail, hisName, hisImage;
        //get uid of clicked user to review his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        hisEmail = intent.getStringExtra("email");
        hisName = intent.getStringExtra("name");
        hisImage = intent.getStringExtra("image");


        //followingTv.setText(hisName.toUpperCase()+" is not following you");
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start following
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

        Query query= FirebaseDatabase.getInstance().getReference("users").orderByChild("uid").equalTo(uid);
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


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Specific Post Details");
        //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get id of post using intent from AdapterTopPost
        postId = intent.getStringExtra("postId");


        //init views
        uPictureIv = findViewById(R.id.uPictureIVD);
        pImageIv = findViewById(R.id.pImageIvD);
        uNameTv = findViewById(R.id.uNameTvD);
        pTimeTv = findViewById(R.id.pTimeD);
        pTitleTv =findViewById(R.id.pTitleTvD);
        pDescriptionTv = findViewById(R.id.pDescriptionTvD);
        pLikesTv = findViewById(R.id.pLikesTvD);
        pCommentsTv = findViewById(R.id.pCommentsTvD);
        moreBtn = findViewById(R.id.moreBtnD);
        likeBtn = findViewById(R.id.likeBtnD);
        //commentBtn = findViewById(R.id.commentBtnD);
        shareBtn = findViewById(R.id.shareBtnD);
        pUniversityTv = findViewById(R.id.universityTVVD);
        profileLayout = findViewById(R.id.profileLayoutD);
        recyclerView = findViewById(R.id.recyclerViewComments);

        commentEt = findViewById(R.id.commentEtD);
        sendBtn = findViewById(R.id.sendBtnD);
        cAvatarIv = findViewById(R.id.cAvatarIvD);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();


        //set subtitle of action bar
        actionBar.setSubtitle("Signed in as "+ myEmail);

        loadComments();

        checkIsFollowed(uid);
        imFollowedOrNot(uid);

        //send comment button click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //like button handle click comment listnere
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

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
                                                Toast.makeText(ThereSpecificPostActivity.this, "unFollowed successfully...", Toast.LENGTH_SHORT).show();
                                                followBtn.setText("Follow");
                                                followBtn.setBackgroundResource(R.drawable.shape_rect02);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unfollow
                                                Toast.makeText(ThereSpecificPostActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).child("Following").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ThereSpecificPostActivity.this, "Followed successfully...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ThereSpecificPostActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadComments() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init comment list
        commentList = new ArrayList<>();

        //path of the post, to get its comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    //set adapter
                    adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
                    //set adapter
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);


        if (hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0, 0 ,"Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0 ,"Edit");

        }



        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int  id = item.getItemId();
                if (id == 0){
                    //delete is clicked
                    beginDelete();
                }
                else if (id == 1){
                    //edit is clicked
                    Intent intent = new Intent(ThereSpecificPostActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);

                }


                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        if (pImage.equals("noImage")){
            //posts without image
            deleteWithoutImage();
        }
        else {
            //post with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //image deleted, now delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue();//remove from firebase where id matches
                                }
                                Toast.makeText(ThereSpecificPostActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, cant go
                        progressDialog.dismiss();
                        Toast.makeText(ThereSpecificPostActivity.this, ""+e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");


        //image deleted, now delete database
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();//remove from firebase where id matches
                }
                Toast.makeText(ThereSpecificPostActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setLikes() {
        //when the details of post are loading, check whether current user has liked or not

        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)){
                    //user has liked this post
                    //to indicate that the post is liked by this signed in user
                    //change drawable icon to like icon
                    //change text of button from like to liked
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);
                    likeBtn.setText("Liked");
                }
                else {
                    //user has not liked this post
                    //to indicate that the post is liked by this signed in user
                    //change drawable icon to like icon
                    //change text of button from like to liked
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);
                    likeBtn.setText("Like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        //get number of likes for the post, whose like button clicked
        //if currently user not liked it before
        //increase the number of likes by +1
        mProcessLike = true;
        //get id of the post clicked
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike){
                    myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    if (snapshot.child(postId).hasChild(myUid)){
                        //already liked, so remove like
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;


                    }
                    else {
                        //not liked, like it
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked"); //set any value
                        mProcessLike = false;


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void postComment() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setMessage("Adding comment ...");

        //get data from comment
        String comment = commentEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)){
            //no value is entered
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = String.valueOf(System.currentTimeMillis());


        //each post will have a child "Comments" that will contain comments of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        //put info in hashmap
        hashMap.put("cId", timestamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", myUid);
        hashMap.put("uName", myName);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);

        //put data in db
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //added successfully
                        dialog.dismiss();
                        Toast.makeText(ThereSpecificPostActivity.this, "Comment added successfully...", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to add
                        dialog.dismiss();
                        Toast.makeText(ThereSpecificPostActivity.this, "Failed to upload comment\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void updateCommentCount() {
        //whenever user adds a comment, increase the number of comments by +1
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment){
                    String comments = ""+snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserInfo() {
        //get current user information
        Query myRef = FirebaseDatabase.getInstance().getReference("users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    //set data
                    try {
                        //if image is received, then post
                        //Picasso.get().load(R.drawable.ic_baseline_person_pin_24).into(cAvatarIv);
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_baseline_person_pin_24).into(cAvatarIv);

                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_baseline_person_pin_24).into(cAvatarIv);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        //get id of post using PostId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query fquery = ref.orderByChild("pId").equalTo(postId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //keep checking until get required post
                for (DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimestamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();
                    String uUniversity = ""+ds.child("uUniversity").getValue();


                    //convert timestamp to dd//mm//yyy hh: mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                    String pTime = DateFormat.format("dd//MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+"Likes");
                    pTimeTv.setText(pTime);
                    pCommentsTv.setText(commentCount+" Comments");
                    pUniversityTv.setText(uUniversity);
                    uNameTv.setText(hisName);

                    //setImage of user who has posted
                    //set post image
                    //if there is no image
                    if (pImage.equals("noImage")){
                        //hide image view
                        pImageIv.setVisibility(View.GONE);
                    }
                    else {
                        //hide image view
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        }
                        catch (Exception e){

                        }
                    }

                    //set user image in comment
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_baseline_person_pin_24).into(uPictureIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_baseline_person_pin_24).into(uPictureIv);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {
                Toast.makeText(ThereSpecificPostActivity.this, "ERROR : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public  void checkUserStatus(){
        //get current user

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user !=null){
            //user is signed in stay here
            myEmail = user.getEmail();
            myUid = user.getUid();
        }
        else {
            //user not signed in, go to login activity
            startActivity(new Intent(ThereSpecificPostActivity.this, LoginAsActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide menus since we dont need them
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.actionSearch).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }

        if (id==R.id.action_video_call){
            startActivity(new Intent(this, VideoCallActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }



}