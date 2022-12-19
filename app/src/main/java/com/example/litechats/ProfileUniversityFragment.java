package com.example.litechats;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.litechats.Adapters.AdapterPosts;
import com.example.litechats.Models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class ProfileUniversityFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage reference
    StorageReference storageReference;
    //path where images of users profile and cover will be stored
    String storagePath="Users_profile_Cover_Imgs/";

    ImageView avatarIv,coverIv;
    TextView nameTv, emailTv, phoneTv, universityTV, courseTv;
    FloatingActionButton floatingActionButton;

    //progress dialog
    ProgressDialog progressDialog;

    AlertDialog dialog;

    RecyclerView postRecyclerView;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;
    //Array of permissions to be requested
    String[] cameraPermission;
    String[] storagePermission;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //uri for picked image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto="image";

    //////////////////////////////////////////////////////////////////////
    ImageView profileImage;
    Button changeProfileImage;
    ///////////////////////////////////////////////////////////////

    public ProfileUniversityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_university, container, false);


        //initalize firebase
        firebaseAuth=FirebaseAuth.getInstance();
        user =firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("users");
        storageReference= FirebaseStorage.getInstance().getReference(); //firebase Storage reference----------
        //init arrays
        cameraPermission=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        profileImage=view.findViewById(R.id.profileImageU);
        nameTv=view.findViewById(R.id.nameTvU);
        emailTv=view.findViewById(R.id.emailTvU);
        floatingActionButton=view.findViewById(R.id.fabU);
        changeProfileImage = view.findViewById(R.id.changeProfileImageU);
        postRecyclerView = view.findViewById(R.id.recyclerview_postsProfileU);

        //initialize progress progressDialogue
        progressDialog =new ProgressDialog(getActivity());

        //initialize progress progressDialogue
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check untill required data get
                for(DataSnapshot ds: snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String image=""+ds.child("image").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);


                    try {
                        //if image is received  then
                        Picasso.get().load(image).into(profileImage);
                    }
                    catch (Exception e){
                        //if there is an exeception while getting the image
                        Picasso.get().load(R.drawable.ic_baseline_add_a_photo_24).into(profileImage);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //fab Button onclick
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList = new ArrayList<>();


        checkUserStatus();
        loadMyPosts();

        StorageReference profileRef=storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });
        //change profile pic
        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGallaeryIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGallaeryIntent, 1000);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });



        return view;
    }


    /************************/
    private void showImagePickDialog() {
        //options to display in dialog
        String[] options ={"Camera"," Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(getActivity(), storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);


        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(), cameraPermission, CAMERA_REQUEST_CODE);
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
                        Toast.makeText(getActivity(), "Camera permissions are required...", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri = data.getData();
                //set to image view
                profileImage.setImageURI(image_uri);

                //upload to firebase
                uploadProfileImageToFirebase(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //set to image view
                profileImage.setImageURI(image_uri);

                //upload to firebase
                uploadProfileImageToFirebase(image_uri);
            }

        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileImageToFirebase(Uri uri) {
        dialog.setMessage("updating profile picture... ");
        dialog.show();
        //path and name of image to be stored in firebase storage
        //String filePathAndName=storagePath+""+profileOrCoverPhoto+"_"+user.getUid();
        // StorageReference storageReference2nd=storageReference.child(filePathAndName);
        StorageReference storageReference2nd=storageReference.child("users/" + firebaseAuth.getCurrentUser().getUid() + "profile.jpg");
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is apploaded to storage, now get its uri and store in users databases
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri=uriTask.getResult();
                        //check if image is uploaded or not and uri is registered
                        if (uriTask.isSuccessful()){
                            //image uploaded
                            //add or update uri in users database
                            HashMap<String, Object> results=new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //uri in database of user is added successfully
                                            //dismiss
                                            dialog.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated Successfully", Toast.LENGTH_SHORT).show();

                                            //if user edit his profile image, also change from his posts
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            Query query = ref.orderByChild("uid").equalTo(uid);
                                            query.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds : snapshot.getChildren()){
                                                        String child = ds.getKey();
                                                        snapshot.getRef().child(child).child("uDp").setValue(uri.toString());
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            //update image in current users comment on post
                                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds : snapshot.getChildren()){
                                                        String child = ds.getKey();
                                                        if (snapshot.child(child).hasChild("Comments")){
                                                            String child1 = ""+snapshot.child(child).getKey();
                                                            Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                            child2.addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                                                        String child = ds.getKey();
                                                                        snapshot.getRef().child(child).child("uImage").setValue(uri.toString());

                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull  DatabaseError error) {

                                                                }
                                                            });
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull  DatabaseError error) {

                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding uri in database of user
                                            dialog.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating Image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else {
                            //error uploading image
                            dialog.dismiss();
                            Toast.makeText(getActivity(), "Some error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there was an error
                        dialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    /**********************/

    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set adapter to recycler view
                    postRecyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMyPosts(String searchQuerry) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuerry.toLowerCase()) ||
                            myPosts.getpTitle().toLowerCase().contains(searchQuerry.toLowerCase())) {

                        //add to list
                        postList.add(myPosts);

                    }

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set adapter to recycler view
                    postRecyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProfileDialog() {
        //option to show dialog
        String options[]={"Edit Name"};

        //alert dialogue
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //set Title
        builder.setTitle("Choose Action ");
        //set items to display
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which==0){
                    //edit Name clicked
                    progressDialog.setMessage("Updating name");
                    showInputDialog("name");
                }
            }
        });
        //create and show dialogue
        builder.create().show();

    }

    private void showInputDialog(String key) {
        //customer dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key); // upddate name or update phone

        ///set layout of dialog
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10,10,10);
        //add edit Text
        EditText editText=new EditText(getActivity());
        editText.setHint("enter "+key); //hint name or phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add button dialog update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value=editText.getText().toString().trim();
                //validate if user has cancelled something or not
                if (!TextUtils.isEmpty(value)){
                    progressDialog.show();;
                    HashMap<String, Object> result=new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //update dismiss progress
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed, dismiss progres, get and shoe error messages
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    //if users edit his name, also change it from his posts
                    if (key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    String child  = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //update name in current users comment on post
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull  DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull  DatabaseError error) {

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Please enter"+key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //add button dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void uploadImageFirebase(Uri imageUri) {
        /*
        //upload image to firebase storage
        StorageReference fileRef=storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(MainActivity.this, "Image uploaded successfully ", Toast.LENGTH_SHORT).show();
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImagePatient);

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed ! could not upload Image"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        */
        //upload image to firebase storage
        //show progress dialogues
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        dialog.setMessage("updating profile picture... ");
        StorageReference fileRef = storageReference.child("users/" + firebaseAuth.getCurrentUser().getUid() + "profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(MainActivity.this, "Image uploaded successfully ", Toast.LENGTH_SHORT).show();
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                        FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid() + "/image")
                                .setValue(uri.toString())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Toast.makeText(requireContext(), "Image updated successfully", Toast.LENGTH_SHORT).show();


                                        //if user edit his profile image, also change from his posts
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                        Query query = ref.orderByChild("uid").equalTo(uid);
                                        query.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uDp").setValue(uri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                        //update image in current users comment on post
                                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    if (snapshot.child(child).hasChild("Comments")){
                                                        String child1 = ""+snapshot.child(child).getKey();
                                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                        child2.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                                    String child = ds.getKey();
                                                                    snapshot.getRef().child(child).child("uImage").setValue(uri.toString());

                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull  DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull  DatabaseError error) {

                                            }
                                        });
                                    }



                                });

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getActivity(), "Failed ! could not upload Image" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu options in fragment
        super.onCreate(savedInstanceState);
    }

    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)  {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item=menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){
                    searchMyPosts(query);
                }
                else {
                    //search text empty, get all users
                    loadMyPosts();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //if search query is not empty then search
                if (!TextUtils.isEmpty(newText.trim())){
                    searchMyPosts(newText);
                }
                else {
                    //search text empty, get all posts
                    loadMyPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if (id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }

        if (id==R.id.action_video_call){
            startActivity(new Intent(getActivity(), VideoCallActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public  void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user !=null){
            //user is signed in stay here
            //set email for loggged in user
            //mProfileTv.setText(user.getEmail());
            uid = user.getUid();
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(getActivity(), LoginAsActivity.class));
            getActivity().finish();
        }
    }





}