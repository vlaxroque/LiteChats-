package com.example.litechats;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.litechats.Adapters.AdapterChatlist;
import com.example.litechats.Adapters.AdapterStudents;
import com.example.litechats.Models.ModelChatStudents;
import com.example.litechats.Models.ModelChatlist;
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

public class ChatListFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelStudents> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;


    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth=FirebaseAuth.getInstance();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        recyclerView=view.findViewById(R.id.recyclerViewStudentCL);
        chatlistList=new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelChatlist chatlist=ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        userList =new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("users"); //-------------------------------------------------path to change
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelStudents user=ds.getValue(ModelStudents.class);
                    for(ModelChatlist chatlistPatient : chatlistList){
                        if (user.getUid() !=null && user.getUid().equals(chatlistPatient.getId())){
                            userList.add(user);
                            break;
                        }
                    }
                    //adapter
                    adapterChatlist=new AdapterChatlist(getContext(),userList);
                    //setAdapter
                    recyclerView.setAdapter(adapterChatlist);
                    //set last message
                    for (int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage="default";
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatStudents chat=ds.getValue(ModelChatStudents.class);
                    if (chat==null){
                        continue;
                    }
                    String sender=chat.getSender();
                    String receiver=chat.getReceiver();
                    if (sender==null || receiver==null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) &&
                                    chat.getSender().equals(currentUser.getUid())){
                        if (chat.getType().equals("image")){
                            theLastMessage = "sent a photo   ";
                        }
                        else {
                            theLastMessage =chat.getMessage();
                        }

                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu options in fragment
        super.onCreate(savedInstanceState);

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

    //inflate option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)  {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);


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


}