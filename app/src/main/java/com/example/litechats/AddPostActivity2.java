package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class AddPostActivity2 extends AppCompatActivity {

    ActionBar actionBar;

    //firebase Auth
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    EditText titleEt, descriptionEt;
    ImageView imageIV;
    Button uploadBtn;

    //image picked will be saved here
    Uri image_rui = null;

    //users infor
    String name, email, uid, dp;
    String university;

    String editTitle, editDescription, editImage;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=300;

    //Array of permissions to be requested
    String[] cameraPermission;
    String[] storagePermission;

    android.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post2);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post");
        //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init
        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();
        actionBar.setTitle(email);

        //init views
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionET);
        imageIV = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);

        //get data from previous Intent of activity
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");
        //validate if we came here to update post.. ie came from adapterPost
        if (isUpdateKey.equals("editPost")){
            actionBar.setTitle("Update post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        }
        else {
            actionBar.setTitle("Add new Post");
            uploadBtn.setText("Upload");
        }


        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();


        //get some infor to post
        userDbRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    name = ""+ds.child("name").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("image").getValue();
                    university = ""+ds.child("university").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //INIT permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};




        //get image from camera/gallery
        imageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showPickImageDialog();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data(tile, desc) from editText
                String title =titleEt.getText().toString();
                String description = descriptionEt.getText().toString();

                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity2.this, "Enter Title", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity2.this, "Enter description", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")){
                   beginUpdate(title, description, editPostId);
                }
                else {
                    uploadData(title, description);
                }


            }
        });

    }

    private void beginUpdate(String title, String description, String editPostId) {
        dialog.setMessage("Updating post ...");
        dialog.show();
        
        if (!editImage.equals("noImage")){
            //with image
            updateWasWithImage(title,description, editPostId);
        }
        else if (imageIV.getDrawable() != null){
            //with image
            updateWithNowImage(title,description, editPostId);
        }
        else {
            //without image
            uploadWithoutImage(title, description, editPostId);
        }
        
    }

    private void uploadWithoutImage(String title, String description, String editPostId) {
        HashMap< String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        //hashMap.put("pId", timeStamp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");
        //hashMap.put("pTime", timeStamp);
        hashMap.put("uUniversity", university);

        //path to post dtata
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //put data in this ref
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //success in adding to database
                        dialog.dismiss();
                        Toast.makeText(AddPostActivity2.this, "Successfully updated post", Toast.LENGTH_SHORT).show();
                        //reset views
                        titleEt.setText("");
                        descriptionEt.setText("");
                        imageIV.setImageURI(null);
                        image_rui = null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding post in database
                        dialog.dismiss();
                        Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWasWithImage(String title, String description, String editPostId) {
        //post is with image, delete previous image first
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //image deleted, upload new imge
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/"+"post_"+timeStamp;

                        //get image from image view
                        Bitmap bitmap = ((BitmapDrawable)imageIV.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //image uploaded, get its url
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()){
                                            //uri is received, upload to database

                                            if (uriTask.isSuccessful()){
                                                //uri is received upload post to firebase database
                                                HashMap< String, Object> hashMap = new HashMap<>();
                                                hashMap.put("uid", uid);
                                                hashMap.put("uName", name);
                                                hashMap.put("uEmail", email);
                                                hashMap.put("uDp", dp);
                                                //hashMap.put("pId", timeStamp);
                                                hashMap.put("pTitle", title);
                                                hashMap.put("pDescr", description);
                                                hashMap.put("pImage", downloadUri);
                                                //hashMap.put("pTime", timeStamp);
                                                hashMap.put("uUniversity", university);

                                                //path to post dtata
                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                                //put data in this ref
                                                ref.child(editPostId)
                                                        .updateChildren(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                //success in adding to database
                                                                dialog.dismiss();
                                                                Toast.makeText(AddPostActivity2.this, "Successfully updated post", Toast.LENGTH_SHORT).show();
                                                                //reset views
                                                                titleEt.setText("");
                                                                descriptionEt.setText("");
                                                                imageIV.setImageURI(null);
                                                                image_rui = null;
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                //failed adding post in database
                                                                dialog.dismiss();
                                                                Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }


                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //image not uploaded
                                        dialog.dismiss();
                                        Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void updateWithNowImage(String title, String description, String editPostId) {

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timeStamp;

        //get image from image view
        Bitmap bitmap = ((BitmapDrawable)imageIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded, get its url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()){
                            //uri is received, upload to database

                            if (uriTask.isSuccessful()){
                                //uri is received upload post to firebase database
                                HashMap< String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                //hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescr", description);
                                hashMap.put("pImage", downloadUri);
                                //hashMap.put("pTime", timeStamp);
                                hashMap.put("uUniversity", university);

                                //path to post dtata
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                ref.child(editPostId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //success in adding to database
                                                dialog.dismiss();
                                                Toast.makeText(AddPostActivity2.this, "Successfully updated post", Toast.LENGTH_SHORT).show();
                                                //reset views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIV.setImageURI(null);
                                                image_rui = null;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding post in database
                                                dialog.dismiss();
                                                Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }


                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //image not uploaded
                        dialog.dismiss();
                        Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of the post using id of post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescr").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    //setData to viewa
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);
                    //set image
                    if (!editImage.equals("noImage")){
                     try {
                         Picasso.get().load(editImage).into(imageIV);
                     }
                     catch (Exception e){

                     }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadData(String title, String description) {
        dialog.setMessage("publishing post ...");
        dialog.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timeStamp;

        if (imageIV.getDrawable() != null){
            //get image from image view
            Bitmap bitmap = ((BitmapDrawable)imageIV.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();




            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image upload to firebase storage, now gets its uri
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()){
                                //uri is received upload post to firebase database
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescr", description);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTime", timeStamp);
                                hashMap.put("uUniversity", university);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pComments", "0");



                                //path to post dtata
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put dara in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //success in adding to database
                                                dialog.dismiss();
                                                Toast.makeText(AddPostActivity2.this, "Successfully published post", Toast.LENGTH_SHORT).show();
                                                //reset views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIV.setImageURI(null);
                                                image_rui = null;
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding post in database
                                                dialog.dismiss();
                                                Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to upload image
                            dialog.dismiss();
                            Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else {
            //post without image
                //uri is received upload post to firebase database
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("uid", uid);
                hashMap.put("uName", name);
                hashMap.put("uEmail", email);
                hashMap.put("uDp", dp);
                hashMap.put("pId", timeStamp);
                hashMap.put("pTitle", title);
                hashMap.put("pDescr", description);
                hashMap.put("pImage", "noImage");
                hashMap.put("pTime", timeStamp);
                hashMap.put("uUniversity", university);
                hashMap.put("pLikes", "0");
                hashMap.put("pComments", "0");

                //path to post dtata
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                //put dara in this ref
                ref.child(timeStamp).setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // successfully posted
                                dialog.dismiss();
                                Toast.makeText(AddPostActivity2.this, "Successfully published post", Toast.LENGTH_SHORT).show();
                                //reset views
                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIV.setImageURI(null);
                                image_rui = null;
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed adding post in database
                                dialog.dismiss();
                                Toast.makeText(AddPostActivity2.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
        }


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


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go back to previous page
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(true);
        menu.findItem(R.id.actionSearch).setVisible(true);
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

    public  void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user !=null){
            //user is signed in stay here
            email = user.getEmail();
            uid = user.getUid();
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(AddPostActivity2.this, LoginActivity.class));
            finish();
        }
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
                //set to imageView
                imageIV.setImageURI(image_rui);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image picked from camera
                imageIV.setImageURI(image_rui);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}