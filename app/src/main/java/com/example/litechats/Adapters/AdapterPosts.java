package com.example.litechats.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.litechats.AddPostActivity;
import com.example.litechats.AddPostActivity2;
import com.example.litechats.Models.ModelPost;
import com.example.litechats.PostDetailActivity;
import com.example.litechats.R;
import com.example.litechats.ThereProfileActivity;
import com.example.litechats.ThereSpecificPostActivity;
import com.example.litechats.VideoPlayActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;
    String myUid;

    private DatabaseReference likesRef; // for likes database node
    private DatabaseReference postsRef; //reference of posts

    boolean mProcessLikes = false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        //inflate layout
         View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPosts.MyHolder holder, int position) {
        //get data
        String uid  =postList.get(position).getUid();
        String uEmail  =postList.get(position).getuEmail();
        String uName  =postList.get(position).getuName();
        String uDp  =postList.get(position).getuDp();
        String pId  =postList.get(position).getpId();
        String pTitle  =postList.get(position).getpTitle();
        String pDescription  =postList.get(position).getpDescr();
        String pImage  =postList.get(position).getpImage();
        String pTimestamp  =postList.get(position).getpTime();
        String uUniversity = postList.get(position).getuUniversity();
        String pLikes = postList.get(position).getpLikes(); //contains total number of likes
        String pComments = postList.get(position).getpComments();
        String pVideoURL = postList.get(position).getVideolink(); //for video URL



        //convert timestamp to dd//mm//yyy hh: mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimestamp));
        String pTime = DateFormat.format("dd//MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.universityTv.setText(uUniversity);
        holder.pLikesTv.setText(pLikes+" Likes"); //eg 500 Likes
        holder.pComments.setText(pComments+" Comments");
        //set likes for each post
        setLikes(holder, pId);

        Toast.makeText(context, "VIDEO URL"+pVideoURL, Toast.LENGTH_SHORT).show();




        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_baseline_person_pin_24).into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        //set post image
        //if there is no image
        if (pImage.equals("noImage")){
            //hide image view
            holder.pImageIv.setVisibility(View.GONE);
        }

        //not empty
        else if(! pVideoURL.isEmpty()){

            //holder.videoFrameLayout.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(pVideoURL);
            holder.video_view.setVideoURI(uri);
            MediaController mediaController = new MediaController(context);
            holder.video_view.setMediaController(mediaController);
            mediaController.setAnchorView(holder.video_view);
            holder.video_view.start();
            holder.video_view.setOnInfoListener((mp, what, extra) -> {
                if (what== MediaPlayer.MEDIA_INFO_BUFFERING_START){
                    holder.progressBar2.setVisibility(View.VISIBLE);
                }else if (what== MediaPlayer.MEDIA_INFO_BUFFERING_END){
                    holder.progressBar2.setVisibility(View.INVISIBLE);
                }
                return false;
            });
        }

        else {
            //hide image view
            holder.pImageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            }
            catch (Exception e){

            }
        }



        //handle button clicks
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get number of likes for the post, whose like button clicked
                //if currently user not liked it before
                //increase the number of likes by +1
                int pLikes=Integer.parseInt(postList.get(position).getpLikes());
                mProcessLikes = true;
                //get id of the post clicked
                String postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLikes){
                            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            if (snapshot.child(postIde).hasChild(myUid)){
                                //already liked, so remove like
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLikes = false;

                            }
                            else {
                                //not liked, like it
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked"); //set any value
                                mProcessLikes = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId); //passing data from activity to another, ie pId of the post clicked
                context.startActivity(intent);

            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //later
                Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show specific user/data/posting
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid); //pass data to ThereProfileActivity
                intent.putExtra("email", uEmail);
                intent.putExtra("name", uName);
                intent.putExtra("image", uDp);
                context.startActivity(intent);
            }
        });
        holder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show specific user/data/posting
                Intent intent = new Intent(context, ThereSpecificPostActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("postId",pId); //passing data from activity to another, ie pId of the post clicked, the id is passed to ThereSpecific Activity
                intent.putExtra("email", uEmail);
                intent.putExtra("name", uName);
                context.startActivity(intent);
            }
        });



    }

    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    //to indicate that the post is liked by this signed in user
                    //change drawable icon to like icon
                    //change text of button from like to liked
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);
                    holder.likeBtn.setText("Liked");
                }
                else {
                    //user has not liked this post
                    //to indicate that the post is liked by this signed in user
                    //change drawable icon to like icon
                    //change text of button from like to liked
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);
                    holder.likeBtn.setText("Like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);


        if (uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0, 0 ,"Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0 ,"Edit");

        }

        popupMenu.getMenu().add(Menu.NONE, 2, 0 ,"View Detail");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int  id = item.getItemId();
                if (id == 0){
                    //delete is clicked
                    beginDelete(pId, pImage);
                }
                else if (id == 1){
                    //edit is clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

                }

                else if (id == 2){
                    //start PostDetailActivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId); //passing data from activity to another, ie pId
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")){
            //posts without image
            deleteWithoutImage(pId);
        }
        else {
            //post with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithoutImage(String pId) {
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");


        //image deleted, now delete database
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();//remove from firebase where id matches
                }
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteWithImage(String pId, String pImage) {
        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //image deleted, now delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue();//remove from firebase where id matches
                                }
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, ""+e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pComments;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        TextView universityTv;
        LinearLayout profileLayout;
        FrameLayout videoFrameLayout;
        VideoView video_view;
        ProgressBar progressBar2;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIV);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTime);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pComments = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            universityTv = itemView.findViewById(R.id.universityTVV);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            videoFrameLayout = itemView.findViewById(R.id.videoFrameLayout);
            video_view = itemView.findViewById(R.id.video_view);
            progressBar2 = itemView.findViewById(R.id.progressBar2);

            //Hide
            //videoFrameLayout.setVisibility(View.INVISIBLE);

        }
    }
}
