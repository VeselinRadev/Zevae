package com.veselin.zevae.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.veselin.zevae.R;
import com.veselin.zevae.adapters.RequestAdapter;
import com.veselin.zevae.adapters.SearchResultsAdapter;
import com.veselin.zevae.fragments.MessagesFragment;
import com.veselin.zevae.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFriends;
    private RecyclerView recyclerViewRequests;
    private List<User> friends, pending, sent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        pending = new ArrayList<>();
        friends = new ArrayList<>();
        sent = new ArrayList<>();
initToolbar();
        initRecyclerViews();
        getFriends();
        getRequests();
    }
    private void initRecyclerViews() {
        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        recyclerViewFriends.setHasFixedSize(true);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRequests = findViewById(R.id.recyclerViewRequests);
        recyclerViewRequests.setHasFixedSize(true);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));
        updateAdapter(friends, pending);
    }

    private void updateAdapter(List<User> friends, List<User> requests){
        SearchResultsAdapter adapterFriends = new SearchResultsAdapter(this, friends);
        recyclerViewFriends.setAdapter(adapterFriends);
        adapterFriends.notifyDataSetChanged();
        RequestAdapter adapterRequests = new RequestAdapter(this, requests);
        recyclerViewRequests.setAdapter(adapterRequests);
        adapterRequests.notifyDataSetChanged();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Friends");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

//    public void getSent(){
//        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("Users")
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child("FriendRequest");
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
//        friendsRef.child("Sent").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                sent.clear();
//                for(DataSnapshot snap:snapshot.getChildren()){
//                    userRef.child(snap.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            sent.add(new User(snapshot.child("id").getValue().toString(), snapshot.child("imageURL").getValue().toString(), snapshot.child("username").getValue().toString()));
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//
//                    });
//
//                }
//                new Thread() {
//                    @Override
//                    public void run() {
//                        while(sent.isEmpty()) {
//                            try {
//                                sleep(200);
//                            } catch (InterruptedException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                        }
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                updateAdapter(sent);
//                            }
//                        });
//                    }
//                }.start();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    public void getFriends(){
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("FriendRequest");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        friendsRef.child("Accepted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friends.clear();
                for(DataSnapshot snap:snapshot.getChildren()){
                    userRef.child(snap.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            friends.add(new User(snapshot.child("id").getValue().toString(), snapshot.child("imageURL").getValue().toString(), snapshot.child("username").getValue().toString()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });

                }
                new Thread() {
                    @Override
                    public void run() {
                        while(friends.isEmpty()) {
                            try {
                                sleep(200);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAdapter(friends, pending);
                            }
                        });
                    }
                }.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getRequests(){
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("FriendRequest");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        friendsRef.child("Pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pending.clear();
                for(DataSnapshot snap:snapshot.getChildren()){
                    userRef.child(snap.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            pending.add(new User(snapshot.child("id").getValue().toString(), snapshot.child("imageURL").getValue().toString(), snapshot.child("username").getValue().toString()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });

                }
                new Thread() {
                    @Override
                    public void run() {
                        while(pending.isEmpty()) {
                            try {
                                sleep(200);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAdapter(friends, pending);
                            }
                        });
                    }
                }.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}