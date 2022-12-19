package com.example.litechats.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litechats.Models.ModelChatStudents;
import com.example.litechats.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChatStudent extends RecyclerView.Adapter<AdapterChatStudent.MyHolder>{

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    Context context;
    List<ModelChatStudents> chatList;
    String imageUrl;

    FirebaseUser fUser;

    public AdapterChatStudent(Context context, List<ModelChatStudents> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        //inflate layouts : left-> receiver,,,, right->sender
        if (i==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return  new MyHolder(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return  new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull AdapterChatStudent.MyHolder myHolder, final int i) {
        //get data
        String message=chatList.get(i).getMessage();
        String timeStamp=chatList.get(i).getTimestamp();
        String type=chatList.get(i).getType();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        //cal.setTimeInMillis(Long.parseLong(timeStamp));// ------------------------------------------------------------------NOT WORKING ???????????////
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if (type.equals("text")){
            //text message
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);
            myHolder.messageTv.setText(message);
        }
        else {
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image).into(myHolder.messageIv);

        }

        //set data
        myHolder.messageTv.setText(message);
       myHolder.timeTv.setText(dateTime);
       try {
           Picasso.get().load(imageUrl).into(myHolder.profileIv);
       }
       catch (Exception e){

       }

       /*
       //click to delete dialog
        myHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete message confirm dialog
                AlertDialog.Builder builder =new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are sure you want to delete this Message ?");
                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(i);
                    }
                });
                //cancel delete button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                //create and show dialog
                builder.create().show();

            }
        });

        */



       //set seen / delivered of status message
        if (i==chatList.size()-1){
            if (chatList.get(i).isSeen()){
                myHolder.isSeenTv.setText("Seen");
            }
            else {
                myHolder.isSeenTv.setText("Delivered");
            }
        }
        else {
            myHolder.isSeenTv.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int position) {
        String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp=chatList.get(position).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    if (ds.child("sender").getValue().equals(myUID)){
                        //CAN WORK IN EITHER OF THE TWO CASES

                        //1. remove the message from the chats
                        //ds.getRef().removeValue();

                        //2.set the value of the message, "This message was deleted..."
                        HashMap<String, Object> hashMap =new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "You can only delete your messages...", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently user
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }

    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout; //for click listener to show delete


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv =itemView.findViewById(R.id.profileIVStudent);
            messageTv =itemView.findViewById(R.id.messageTVStudent);
            timeTv =itemView.findViewById(R.id.timeTvStudent);
            isSeenTv =itemView.findViewById(R.id.isSeenTvStudent);
            messageLayout=itemView.findViewById(R.id.messageStudentLayout);
            messageIv=itemView.findViewById(R.id.messageIV);
        }
    }
}
