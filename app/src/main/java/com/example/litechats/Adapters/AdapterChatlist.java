package com.example.litechats.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litechats.ChatActivity;
import com.example.litechats.Models.ModelStudents;
import com.example.litechats.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder>{

    Context context;
    List<ModelStudents> userList;
    private HashMap<String, String> lastMessageMap;

    public AdapterChatlist(Context context, List<ModelStudents> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  AdapterChatlist.MyHolder myHolder, int i) {
        //get data
        String hisUid=userList.get(i).getUid();
        String userImage=userList.get(i).getImage();
        String userName=userList.get(i).getName();
        String lastMessage=lastMessageMap.get(hisUid);

        //set data
        myHolder.nameTv.setText(userName);
        if (lastMessage==null || lastMessage.equals("default")){
            myHolder.lastMessageTv.setVisibility(View.GONE);
        }
        else {
            myHolder.lastMessageTv.setVisibility(View.VISIBLE);
            myHolder.lastMessageTv.setText(lastMessage);

        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_baseline_person_pin_24).into(myHolder.profileIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_baseline_person_pin_24).into(myHolder.profileIv);
        }

        //set online status of other users in chatlist
        if (userList.get(i).getOnlineStatus().equals("online")){
            //online
           myHolder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        //handle click of user in chatlist
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity whith that user
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);

            }
        });

    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv=itemView.findViewById(R.id.profileIV1);
            onlineStatusIv=itemView.findViewById(R.id.onlineStatusTv);
            nameTv=itemView.findViewById(R.id.nameTv1);
            lastMessageTv=itemView.findViewById(R.id.lastMessagePatientTv);

        }


    }
}
