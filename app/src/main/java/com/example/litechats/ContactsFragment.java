package com.example.litechats;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.SearchView;

import com.example.litechats.Adapters.AdapterStudents;
import com.example.litechats.Models.ModelStudents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {


    RecyclerView recyclerView;
    AdapterStudents adapterStudents;
    List<ModelStudents> studentsList;

    //firebase Auth
    FirebaseAuth firebaseAuth;



    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu options in fragmen
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        //init
        firebaseAuth=FirebaseAuth.getInstance();


        //init recyclerView
        recyclerView =view.findViewById(R.id.students_recyclerView);
        //set its properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init users list
        studentsList=new ArrayList<>();

        //get all users
        getAllUsers();


        return view;
    }

    private void getAllUsers() {

        //get current user
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "users" containing infor                                        ////----------------------------------------------------------------------------------//////
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("users");  /////PATH TO CHANGE INCASE WONNA CHNGE NAMES TO DISPLAYED ON THE RECYCLER VIEW///////////////
        //get all data from path                                                                     //////-----------------------------------------------------------------------------------------------------------
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelStudents modelStudents= ds.getValue(ModelStudents.class);
                    //get all users except currently signed in user
                    if (!modelStudents.getUid().equals(fUser.getUid())){
                        //get all students except Universities
                        String accountType = ""+ds.child("accountType").getValue();
                        if (accountType.equals("Student")){
                            studentsList.add(modelStudents);
                        }
                    }
                    //adapter
                    adapterStudents=new AdapterStudents(getActivity(), studentsList);
                    //set recyclerView
                    recyclerView.setAdapter(adapterStudents);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)  {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //search view
        MenuItem item=menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){
                    searchUsers(query);
                }
                else {
                    //search text empty, get all users
                    getAllUsers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //if search query is not empty then search
                if (!TextUtils.isEmpty(newText.trim())){
                    searchUsers(newText);
                }
                else {
                    //search text empty, get all users
                    getAllUsers();
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
        if (id==R.id.action_video_call){
            startActivity(new Intent(getActivity(), VideoCallActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchUsers(String query) {
        //get current user
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "users" containing infor                                        ////----------------------------------------------------------------------------------//////
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("users");  /////PATH TO CHANGE INCASE WONNA CHNGE NAMES TO DISPLAYED ON THE RECYCLER VIEW///////////////
        //get all data from path                                                                     //////-----------------------------------------------------------------------------------------------------------
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelStudents modelStudents= ds.getValue(ModelStudents.class);
                    //get all users searched except currently signed in user
                    if (!modelStudents.getUid().equals(fUser.getUid())){
                        if (modelStudents.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelStudents.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                                modelStudents.getPhone().toLowerCase().contains(query.toLowerCase()) ||
                                modelStudents.getUniversity().toLowerCase().contains(query.toLowerCase()) ||
                                modelStudents.getCourse().toLowerCase().contains(query.toLowerCase())

                        ){
                            studentsList.add(modelStudents);
                        }


                    }
                    //adapter
                    adapterStudents=new AdapterStudents(getActivity(), studentsList);

                    //refresh adapter
                    adapterStudents.notifyDataSetChanged();

                    //set recyclerView
                    recyclerView.setAdapter(adapterStudents);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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