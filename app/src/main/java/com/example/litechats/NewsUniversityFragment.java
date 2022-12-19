package com.example.litechats;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.litechats.Adapters.AdapterNews;
import com.example.litechats.Models.ModelNews;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewsUniversityFragment extends Fragment {

    private Button addNewsBtn;

    //firebase Auth
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelNews> newsList;
    AdapterNews adapterNews;



    public NewsUniversityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news_university, container, false);


        //init
        firebaseAuth=FirebaseAuth.getInstance();

        //recycler view and its properties
        recyclerView =view.findViewById(R.id.newsUniversityRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest news first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init psot list
        newsList=new ArrayList<>();

        //load News
        loadNews();


        addNewsBtn = view.findViewById(R.id.addNewsBtn);

        addNewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddNewsActivity.class);
                startActivity(intent);
            }
        });

        return  view;
    }

    private void loadNews() {

        //get path of database named "users" containing infor                                        ////----------------------------------------------------------------------------------//////
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("News");  /////PATH TO CHANGE INCASE WONNA CHNGE NAMES TO DISPLAYED ON THE RECYCLER VIEW///////////////
        //get all data from path                                                                     //////-----------------------------------------------------------------------------------------------------------
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelNews modelNews= ds.getValue(ModelNews.class);
                    newsList.add(modelNews);
                    //adapter
                    adapterNews=new AdapterNews(getActivity(), newsList);
                    //set adapter to recyclerView
                    recyclerView.setAdapter(adapterNews);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
                //Toast.makeText(getActivity(), "[ERROR] : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void searchNews(String query) {
        //get path of database named "users" containing infor                                        ////----------------------------------------------------------------------------------//////
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("News");  /////PATH TO CHANGE INCASE WONNA CHNGE NAMES TO DISPLAYED ON THE RECYCLER VIEW///////////////
        //get all data from path                                                                     //////-----------------------------------------------------------------------------------------------------------
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelNews modelNews= ds.getValue(ModelNews.class);

                    if (modelNews.getpTitle().toLowerCase().contains(query.toLowerCase()) ||
                            modelNews.getpDescr().toLowerCase().contains(query.toLowerCase()) ||
                            modelNews.getuName().toLowerCase().contains(query.toLowerCase())){
                        newsList.add(modelNews);
                    }


                    //adapter
                    adapterNews=new AdapterNews(getActivity(), newsList);
                    //set adapter to recyclerView
                    recyclerView.setAdapter(adapterNews);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
                Toast.makeText(getActivity(), "[ERROR] : "+error.getMessage(), Toast.LENGTH_SHORT).show();
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

        //search view to search News by post title/description
        MenuItem item = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query)){
                    searchNews(query);
                }
                else {
                    //search text empty, get all News
                    loadNews();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //if search query is not empty then search
                if (!TextUtils.isEmpty(newText)){
                    searchNews(newText);
                }
                else {
                    //search text empty, get all News
                    loadNews();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu item click

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
        }
        else {
            //user not signed in, go to main activity
            startActivity(new Intent(getActivity(), LoginAsActivity.class));
            getActivity().finish();
        }
    }
}