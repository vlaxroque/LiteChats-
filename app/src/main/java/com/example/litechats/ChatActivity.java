package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.litechats.Adapters.AdapterChatStudent;
import com.example.litechats.Adapters.AdapterStudents;
import com.example.litechats.Models.ModelChatStudents;
import com.example.litechats.Models.ModelStudents;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class ChatActivity extends AppCompatActivity {

    //view
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv, blockIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn, attachBtn;

    //firebase Auth
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    //for checking if user has seen messsage or not
    ValueEventListener seenListener;
    DatabaseReference userREfForSeen;

    List<ModelChatStudents> chatList;
    AdapterChatStudent adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    //APIService apiService;
    boolean notify=false;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=300;

    //Array of permissions to be requested
    String[] cameraPermission;
    String[] storagePermission;

    //image picked will be saved here
    Uri image_rui = null;

    boolean isBlocked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init
        Toolbar toolbar=findViewById(R.id.toolbarStudent);
        //setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.chat_recyclerViewStudent);
        profileIv =findViewById(R.id.profileStudentIv);
        nameTv =findViewById(R.id.nameTvTV);
        messageEt =findViewById(R.id.messageEtStudent);
        userStatusTv=findViewById(R.id.statusStudentTv);
        sendBtn=findViewById(R.id.sendBtnStudent);
        attachBtn=findViewById(R.id.attachBtn);
        blockIv=findViewById(R.id.blockIv);

        //INIT permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};



        //linearLout for recyclerView
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recyclerViewProperties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service
        //apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class); //----------------------------------


        //init
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        userDbRef=firebaseDatabase.getReference("users");

        //on clicking a certain user, pass his profile pic and name
        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid");

        //search for user to get that users infor
        Query userQuery=userDbRef.orderByChild("uid").equalTo(hisUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untill required info is archieved
                for (DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String name=""+ds.child("name").getValue();
                    hisImage=""+ds.child("image").getValue();
                    String typingStatus=""+ds.child("typingTo").getValue();


                    //if check typing status
                    if (typingStatus.equals(myUid)){
                        userStatusTv.setText("typing....");
                        //userStatusTv.setTextColor(Integer.parseInt("#4CAF50"));
                    }
                    else {

                        //get value of online status
                        String onlineStatus=""+ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }
                        else {
                            //convert timestamp to proper time and date
                            //convert timestamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));// ------------------------------------------------------------------NOT WORKING ???????????////
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                            userStatusTv.setText("Last seen at: "+dateTime);
                            //add any time stamp to registered users in firebase database manually

                        }
                    }

                    //set data
                    nameTv.setText(name);

                    try {
                        //image received, set imageView toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_baseline_person_pin_24).into(profileIv);
                    }
                    catch (Exception e){
                        //there is exception
                        Picasso.get().load(R.drawable.ic_baseline_person_pin_24).into(profileIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click button to send
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;// ----------------------------------------------------------------------------------------------------------------

                String message =messageEt.getText().toString().trim();
                //get text from edit text
                if (TextUtils.isEmpty(message)){
                    //text empty
                    Toast.makeText(ChatActivity.this, "cannot send empty message", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(message);
                }
                //reset edit text after sending message
                messageEt.setText("");
            }
        });

        //click button to import image
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showPickImageDialog();
            }
        });

        //check edit text change listListner
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(hisUid); //user Id of the receiver
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //click to block or unblock user
        blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( isBlocked ){
                    unBlockUser();
                }
                else {
                    blockUser();
                }
            }
        });

        readMessage();

        checkIsBlocked();

        seenMessage();

    }


    private void checkIsBlocked() {
        //check each user, if blocked or not
        //if uid of user exists in "BlockedUsers" then that user is blocked, otherwise not

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                               blockIv.setImageResource(R.drawable.ic_baseline_block_24);
                               isBlocked = true;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void blockUser() {
        //block the user by adding uid to current users BlockedUsers node

        //put values in hashmap to put to db
        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("uid", hisUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ChatActivity.this, "Blocked successfully...", Toast.LENGTH_SHORT).show();
                        blockIv.setImageResource(R.drawable.ic_baseline_block_24);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser() {
        //Unblock the user by removing uid from current users BlockedUsers node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (ds.exists()){
                                //remove blocked user data from current users blocklist
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //unblock success
                                                Toast.makeText(ChatActivity.this, "UnBlocked successfully...", Toast.LENGTH_SHORT).show();
                                                blockIv.setImageResource(R.drawable.ic_unblock_green);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to block
                                                Toast.makeText(ChatActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void showPickImageDialog() {
        //options
        String[] options = {"Camera","Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        //set options for dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //camera
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                else if(which == 1){
                    //gallery
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent to pick from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);


    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void seenMessage() {
        userREfForSeen=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=userREfForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatStudents chat=ds.getValue(ModelChatStudents.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hasSeenhashMap=new HashMap<>();
                        hasSeenhashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenhashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void readMessage() {
        chatList=new ArrayList<>();
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelChatStudents chat=ds.getValue(ModelChatStudents.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }

                    //adapter
                    adapterChat=new AdapterChatStudent(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerView
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();

        String timestamp= String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);



        String msg=message;
        DatabaseReference database=FirebaseDatabase.getInstance().getReference("users").child(myUid); //-------------------------------------------------------------------
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                ModelStudents user=snapshot.getValue(ModelStudents.class);

                if (notify){
                    //sendNotification(hisUid, user.getName(), message);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });

        //create chatlist node/chile in firebase
        final DatabaseReference chatRef1=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //create chatlist node/chile in firebase
        final DatabaseReference chatRef2=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*
    private void sendNotification(final String hisUid,final String name,final String message) {
        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Token token=ds.getValue(Token.class);
                    Data data=new Data(myUid, name+":"+message, "New Message", hisUid, R.drawable.ic_default_img);

                    Sender sender=new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatPatientsActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

     */

    public  void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user !=null){
            //user is signed in stay here
            //set email for loggged in user
            //mProfileTv.setText(user.getEmail());
            myUid=user.getUid(); //currently signed in user
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, LoginAsActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("users").child(myUid); ///////////////-------------------path to change
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of online Status of current user;
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("users").child(myUid); ///////////////-------------------path to change
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("typingTo", typing);
        //update value of online Status of current user;
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkUserStatus();
        //get time stamp
        String timeStamp=String.valueOf(System.currentTimeMillis());

        //set ofline with last seen time stamp
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");
        userREfForSeen.removeEventListener(seenListener);

    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();

    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //both permission accepted
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this, "Camera & Storage both permission are necessary", Toast.LENGTH_SHORT).show();
                    }

                }
                else {

                }
            }
            break;
            case  STORAGE_REQUEST_CODE:{

                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_DENIED;
                    if (storageAccepted){
                        if (  storageAccepted){
                            //storage permission accepted
                            pickFromGallery();
                        }
                        else {
                            Toast.makeText(this, "Storage permission are necessary", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //called after picking image from gallery or camera
        if (resultCode ==RESULT_OK){
            if (requestCode ==IMAGE_PICK_GALLERY_CODE){
                //image picked from gallery
                image_rui = data.getData();

                //use this image uri to upload to firebase storage
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image picked from camera
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_rui) throws IOException {
        notify = true;

        android.app.AlertDialog dialog;
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setMessage("Sending image...");

        String timestamp = ""+System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/"+"post_"+timestamp;

        //chat node will be created that will contain all images sent via chat

        //get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_rui);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data  = baos.toByteArray(); //convert image to byte
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //implement uploaded
                        dialog.dismiss();
                        //get uri of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            //add image uri and other infor to database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            //set required data
                            HashMap<String, Object> hashMap =  new HashMap<>();
                            hashMap.put("sender", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("message", downloadUri);
                            hashMap.put("timestamp", timestamp);
                            hashMap.put("type", "image");
                            hashMap.put("isSeen", false);
                            //put this data to firebase
                            databaseReference.child("Chats").push().setValue(hashMap);

                            //send notifications
                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("users").child(myUid);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelStudents students = snapshot.getValue(ModelStudents.class);

                                    if (notify){
                                        //send Notification
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //create chatlist node/chile in firebase
                            final DatabaseReference chatRef1=FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(myUid)
                                    .child(hisUid);
                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()){
                                        chatRef1.child("id").setValue(hisUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //create chatlist node/chile in firebase
                            final DatabaseReference chatRef2=FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);
                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()){
                                        chatRef2.child("id").setValue(myUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        dialog.dismiss();

                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //hide searchView as we dont need it
        menu.findItem(R.id.actionSearch).setVisible(false);
        //hide post as we dont need it
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
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


}