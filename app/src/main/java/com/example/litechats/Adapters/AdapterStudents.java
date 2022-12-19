package com.example.litechats.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litechats.ChatActivity;
import com.example.litechats.Models.ModelStudents;
import com.example.litechats.R;
import com.example.litechats.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterStudents extends RecyclerView.Adapter<AdapterStudents.Myholder>{

    Context context;
    List<ModelStudents> studentsList;

    //for getting current user id
    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterStudents(Context context, List<ModelStudents> studentsList) {
        this.context = context;
        this.studentsList = studentsList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull

    @Override
    public Myholder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        //inflate layout(row_users.xml)
        View view= LayoutInflater.from(context).inflate(R.layout.row_students, parent, false);

        return new AdapterStudents.Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterStudents.Myholder myHolder, int i) {
        //get data
        String hisUID=studentsList.get(i).getUid();
        String userImage=studentsList.get(i).getImage();
        String userName=studentsList.get(i).getName();
        String userEmail=studentsList.get(i).getEmail();
        String userPhone=studentsList.get(i).getPhone();

        String userUniversity=studentsList.get(i).getUniversity();
        String userCourse=studentsList.get(i).getCourse();

        //set data
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        myHolder.mPhoneTv.setText(userPhone);

        myHolder.mUniversityTv.setText(userUniversity);
        myHolder.mCourseTV.setText(userCourse);

        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_baseline_person_pin_24)
                    .into(myHolder.mAvatarIv);
        }
        catch (Exception e){

        }
        myHolder.blckIv.setImageResource(R.drawable.ic_unblock_green);
        //check if each user each user is blocked or not
        checkIsBlocked(hisUID, myHolder, i);

        //handle item click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent= new Intent(context, ChatPatientsActivity.class);
                intent.putExtra("hisUid", hisUID);
                context.startActivity(intent);
                 */

                showChooseActionDialog(hisUID, userName, userEmail, userImage);
            }
        });

        //click to block or unblock user
        myHolder.blckIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (studentsList.get(i).isBlocked()){
                    unBlockUser(hisUID);
                }
                else {
                    blockUser(hisUID);
                }
            }
        });
    }

    private void imBlockedOrNot(String hisUID){
        //first check if sender(current user) is blocked by receiver or not
        //Logic ; if uid of the sender(current user) exists in "BlockedUsers" of reciver then sender(current user) is blocked, otherwise not
        //if blocked then just display a message e.g You are Blocked by that user, cant send that message
        //if not blocked then start chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(context, "You are Blocked by that user, cant send that message", Toast.LENGTH_SHORT).show();
                                //blocked, dont proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent= new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, Myholder myHolder,final int i) {
        //check each user, if blocked or not
        //if uid of user exists in "BlockedUsers" then that user is blocked, otherwise not

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                myHolder.blckIv.setImageResource(R.drawable.ic_baseline_block_24);
                                studentsList.get(i).setBlocked(true);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void blockUser(String hisUID) {
        //block the user by adding uid to current users BlockedUsers node

        //put values in hashmap to put to db
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Blocked successfully...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser(String hisUID) {
        //Unblock the user by removing uid from current users BlockedUsers node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
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
                                                Toast.makeText(context, "UnBlocked successfully...", Toast.LENGTH_SHORT).show();


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to block
                                                Toast.makeText(context, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void showChooseActionDialog(String hisUID, String name, String email, String image) {
        //option to show dialog
        String options[]={"View Profile", "Chat"};

        //alert dialogue
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        //set Title
        builder.setTitle("Choose Action ");
        //set items to display
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //hadle dialog item clicks
                if (which==0){
                    //profilr
                    //show specific user/data/posting
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid", hisUID);
                    intent.putExtra("name",name );
                    intent.putExtra("email", email);
                    intent.putExtra("image", image);



                    context.startActivity(intent);
                }
                else if (which==1){
                    //Chat
//                    Intent intent= new Intent(context, ChatActivity.class);
//                    intent.putExtra("hisUid", hisUID);
//                    context.startActivity(intent);
                    imBlockedOrNot(hisUID);
                }
            }
        });
        //create and show dialogue
        builder.create().show();

    }

    @Override
    public int getItemCount() {
        return studentsList.size();
    }

    //view holders
    class Myholder extends RecyclerView.ViewHolder {

        ImageView mAvatarIv, blckIv;
        TextView mNameTv, mEmailTv, mPhoneTv;
        TextView mUniversityTv, mCourseTV;

        public Myholder(@NonNull View itemView) {
            super(itemView);

            //int values
            mAvatarIv = itemView.findViewById(R.id.avatarIvv);
            mNameTv = itemView.findViewById(R.id.nameTvv);
            mEmailTv = itemView.findViewById(R.id.emailTvv);
            mPhoneTv = itemView.findViewById(R.id.phoneTvv);

            mUniversityTv = itemView.findViewById(R.id.universityTvv);
            mCourseTV = itemView.findViewById(R.id.courseTvv);
            blckIv = itemView.findViewById(R.id.blckIv);

        }

    }

}
