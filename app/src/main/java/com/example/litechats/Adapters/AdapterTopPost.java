package com.example.litechats.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litechats.Models.ModelPost;
import com.example.litechats.R;
import com.example.litechats.ThereProfileActivity;
import com.example.litechats.ThereSpecificPostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterTopPost extends RecyclerView.Adapter<AdapterTopPost.MyHolder>{

    Context context;
    List<ModelPost> postList;
    String myUid;

    public AdapterTopPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate row_top_posts
        View view  = LayoutInflater.from(context).inflate(R.layout.row_top_posts, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterTopPost.MyHolder holder, int position) {
        //get data
        ModelPost modelPost = postList.get(position);
        String uid = modelPost.getUid();
        String pId = modelPost.getpId();
        String  uDp = modelPost.getuDp();
        String pImage =modelPost.getpImage();

        //set data
        try {
            Picasso.get().load(pImage).placeholder(R.drawable.ic_baseline_person_pin_24).into(holder.uPictureIVRTP);
        }
        catch (Exception e){
            //incase the poster has no dp
            Picasso.get().load(R.drawable.ic_baseline_person_pin_24);
        }

        holder.uPictureIVRTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show specific user/data/posting
                Intent intent = new Intent(context, ThereSpecificPostActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("postId",pId); //passing data from activity to another, ie pId of the post clicked, the id is passed to ThereSpecific Activity
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView uPictureIVRTP;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIVRTP = itemView.findViewById(R.id.uPictureIVRTP);

        }
    }
}
