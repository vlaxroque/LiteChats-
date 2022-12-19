package com.example.litechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VideoPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        VideoView video = findViewById(R.id.video_view);
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("url");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String videoUrl = snapshot.getValue(String.class);
                Log.i("Video", "onDataChange: "+videoUrl);
                Uri uri = Uri.parse(videoUrl);
                video.setVideoURI(uri);
                MediaController mediaController = new MediaController(VideoPlayActivity.this);
                video.setMediaController(mediaController);
                mediaController.setAnchorView(video);
                video.start();
                video.setOnInfoListener((mp, what, extra) -> {
                    if (what== MediaPlayer.MEDIA_INFO_BUFFERING_START){
                        progressBar.setVisibility(View.VISIBLE);
                    }else if (what== MediaPlayer.MEDIA_INFO_BUFFERING_END){
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    return false;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VideoPlayActivity.this, "Fail to get video url.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}