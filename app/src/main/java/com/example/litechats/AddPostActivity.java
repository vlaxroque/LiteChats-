package com.example.litechats;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.firebase.storage.OnProgressListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class AddPostActivity extends AppCompatActivity {


    //permission constants
    private static  final int CAMERA_REQUEST_CODE=200;
    private static  final int STORAGE_REQUEST_CODE=300;

    //IMAGE PICK CONSTANT
    private static  final int IMAGE_PICK_GALLERY_CODE=400;
    private static  final int IMAGE_PICK_CAMERA_CODE=500;

    //permission arrays
    private String[] cameraPermission;
    private String[] storagePermission;
    //image picked Uri
    private Uri image_uri;

    //progressbar to display while registering user
    AlertDialog dialog;

    //declare an instance of firebase
    private FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    //firebase Auth
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

    Button playVidBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        /***************/
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
        playVidBtn = findViewById(R.id.playVidBtn);

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


        //get image from camera/gallery
        imageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                //showImagePickDialog();
                showImageOrVideoPicDialog();
            }
        });

        playVidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddPostActivity.this, VideoPlayActivity.class));
            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data(tile, desc) from editText
                String title =titleEt.getText().toString();
                String description = descriptionEt.getText().toString();

                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter Title", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")){
                    beginUpdate(title, description, editPostId);
                }

                //upload video
                if (videouri != null) {
                    dialog.setTitle("Uploading...");
                    dialog.show();
                    uploadvideo();
                }

                else {
                    uploadData(title, description);
                }


            }
        });

        //init permission array
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setTitle("Please wait ");


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
            startActivity(new Intent(AddPostActivity.this, LoginActivity.class));
            finish();
        }
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
                        Toast.makeText(AddPostActivity.this, "Successfully updated post", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                Toast.makeText(AddPostActivity.this, "Successfully updated post", Toast.LENGTH_SHORT).show();
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
                                                                Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddPostActivity.this, "Successfully updated post", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                hashMap.put("videolink", "");



                                //path to post dtata
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put dara in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //success in adding to database
                                                dialog.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Successfully published post", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            hashMap.put("videolink", "");

            //path to post dtata
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put dara in this ref
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // successfully posted
                            dialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "Successfully published post", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


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

    private void showImageOrVideoPicDialog(){
        //options to display in dialog
        String[] options ={"Image"," Video"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image or Video")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0){
                            //Image clicked
                            showImagePickDialog();
                        }
                        else {
                            //video clicked
                            showVideoPicDialog();
                        }
                    }
                }).show();
    }

    private void showVideoPicDialog() {
        Toast.makeText(this, "Video Pick dialog clicked", Toast.LENGTH_SHORT).show();
        choosevideo();
    }

    /**UPLOAD VIDEO**/
    // choose a video from phone storage
    private void choosevideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 5);
    }

    Uri videouri;

    private String getfiletype(Uri videouri) {
        ContentResolver r = getContentResolver();
        // get the file type ,in this case its mp4
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri));
    }

    private void uploadvideo() {
        //get data(tile, desc) from editText
        String title =titleEt.getText().toString();
        String description = descriptionEt.getText().toString();

        if (videouri != null) {
            // save the selected video in Firebase storage
            final StorageReference reference = FirebaseStorage.getInstance().getReference("Files/" + System.currentTimeMillis() + "." + getfiletype(videouri));
            reference.putFile(videouri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    // get the link of video
                    String downloadUriVideo = uriTask.getResult().toString();
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Video");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("videolink", downloadUriVideo);
                    reference1.child("" + System.currentTimeMillis()).setValue(map);
                    // Video uploaded successfully
                    // Dismiss dialog


                    /**UPLOAD VIDEO TO THE POSTS **/
                    String timeStamp = String.valueOf(System.currentTimeMillis());
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
                    hashMap.put("videolink", downloadUriVideo);

                    //path to post dtata
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    //put dara in this ref
                    ref.child(timeStamp).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // successfully posted
                                    dialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Successfully published post", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(AddPostActivity.this, "[ERROR] : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });



                    dialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Video Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Error, Image not uploaded
                    dialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                // Progress Listener for loading
                // percentage on the dialog box
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    // show the progress bar
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options ={"Camera"," Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //camera permission allowed
                                pickFromCamera();
                            }
                            else {
                                //camera permission not allowed
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery clicked
                            if (checkStoragePermission()){
                                //storage permission allowed
                                pickFromGallery();

                            }
                            else {
                                //storage permission not allowed
                                requestStoragePermission();
                            }

                        }
                    }
                }).show();
    }

    private void pickFromGallery(){
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        //intent to pick image from camera

        //using media store ro pick high/original quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);


        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted= grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission accepted
                        pickFromCamera();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Camera permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission accepted
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri = data.getData();
                //set to image view
                imageIV.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //set to image view
                imageIV.setImageURI(image_uri);
            }

            /**UPLOAD VIDEO**/
            if (requestCode == 5 && resultCode == RESULT_OK && data != null && data.getData() != null) {
                videouri = data.getData();
//                dialog.setTitle("Uploading...");
//                dialog.show();
//                uploadvideo();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}