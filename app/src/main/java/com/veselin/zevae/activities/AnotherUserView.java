package com.veselin.zevae.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.veselin.zevae.R;
import com.veselin.zevae.models.FriendRequest;
import com.veselin.zevae.models.User;

public class AnotherUserView extends AppCompatActivity {
    private View view;
    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;
    private User user;
    private LinearLayout llProgressBar;
    private TextView interactionsTxt;
    private TextView friendsTxt;
    private TextView description;
    private TextView gender;
    private TextView email;
    private TextView birthday;
    private TextView country;
    private CircularImageView profileImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_user_view);
        setUser();
    }
    private void setUser(){
        llProgressBar = findViewById(R.id.llProgressBar);
        llProgressBar.setVisibility(View.VISIBLE);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(getIntent().getStringExtra("id"));
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        new Thread() {
            @Override
            public void run() {
                while(user == null) {
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
                        user.updateInteractionsConvos();
                        initViews();
                        initToolbar();
                        llProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }.start();
    }

    private void initViews(){
        profileImg = findViewById(R.id.image);
        setProfileImg();
        interactionsTxt = findViewById(R.id.interactions_txt);
        interactionsTxt.setText(String.valueOf(user.getInteractionsCount()));
        friendsTxt = findViewById(R.id.friends_txt);
        friendsTxt.setText(String.valueOf(user.getFriendsCount()));
        description = findViewById(R.id.description_txt);
        description.setText(user.getDescription());
        gender = findViewById(R.id.gender_text);
        gender.setText(user.getGender());
        email = findViewById(R.id.email_txt);
        email.setText(user.getEmail());
        birthday = findViewById(R.id.birthday_txt);
        setBirthday();
        country = findViewById(R.id.country_txt);
        setCountry();
        findViewById(R.id.open_chat_btn).setOnClickListener(view -> startActivity(new Intent(AnotherUserView.this, ChatActivity.class).putExtra("id", user.getId())));
        getFriendsStatus();
    }

    private void getFriendsStatus(){
        ImageButton btn = findViewById(R.id.add_friend);

        DatabaseReference anotherUserFriendRequestRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(getIntent().getStringExtra("id"))
                .child("FriendRequest");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        anotherUserFriendRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Accepted").child(firebaseUser.getUid()).getValue() != null) {
                    //The users are now friends
                    btn.setImageResource(R.drawable.ic_user_accepted);
                    btn.setColorFilter(getResources().getColor(R.color.green));
                    btn.setOnClickListener(view -> {
                        alertDialog.setMessage("Are sure you want to remove it from friends?");
                        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(getIntent().getStringExtra("id"))
                                    .child("FriendRequest").child("Accepted").child(firebaseUser.getUid()).setValue(null);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseUser.getUid())
                                    .child("FriendRequest").child("Accepted").child(getIntent().getStringExtra("id"))
                                    .setValue(null).addOnCompleteListener(task -> getFriendsStatus());
                        });
                        alertDialog.setNegativeButton("No", (dialogInterface, i) -> {
                            dialogInterface.cancel();
                        });
                        alertDialog.create().show();
                    });
                } else if(snapshot.child("Sent").child(firebaseUser.getUid()).getValue() != null){
                    //The request is sent from another user to the my user and is pending acceptance
                    btn.setImageResource(R.drawable.ic_user_pending);
                    btn.setColorFilter(getResources().getColor(R.color.orange));
                    btn.setOnClickListener(view -> {
                        alertDialog.setMessage("Accept or decline the friend request!");
                        alertDialog.setPositiveButton("Accept", (dialogInterface, i) -> {
                            FriendRequest friendRequest = new FriendRequest("Requested", firebaseUser.getUid(), getIntent().getStringExtra("id"));
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(getIntent().getStringExtra("id"))
                                    .child("FriendRequest").child("Accepted").child(firebaseUser.getUid()).setValue(friendRequest.getHashMap());
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseUser.getUid())
                                    .child("FriendRequest").child("Accepted").child(getIntent().getStringExtra("id"))
                                    .setValue(friendRequest.getHashMap());
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(getIntent().getStringExtra("id"))
                                    .child("FriendRequest").child("Sent").child(firebaseUser.getUid()).setValue(null);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseUser.getUid())
                                    .child("FriendRequest").child("Pending").child(getIntent().getStringExtra("id"))
                                    .setValue(null).addOnCompleteListener(task -> getFriendsStatus());
                        });
                        alertDialog.setNegativeButton("Decline", (dialogInterface, i) -> {
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(getIntent().getStringExtra("id"))
                                    .child("FriendRequest").child("Sent").child(firebaseUser.getUid()).setValue(null);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseUser.getUid())
                                    .child("FriendRequest").child("Pending").child(getIntent().getStringExtra("id"))
                                    .setValue(null).addOnCompleteListener(task -> getFriendsStatus());
                            dialogInterface.cancel();
                        });
                        alertDialog.create().show();
                    });
                } else if(snapshot.child("Pending").child(firebaseUser.getUid()).getValue() != null){
                    //The request is sent from my user to the another user and is pending acceptance
                    btn.setImageResource(R.drawable.ic_user_sent);
                    btn.setColorFilter(getResources().getColor(R.color.blue));
                    btn.setOnClickListener(view -> {
                        alertDialog.setMessage("Are you sure you want to cancel your request?");
                        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(getIntent().getStringExtra("id"))
                                    .child("FriendRequest").child("Pending").child(firebaseUser.getUid()).setValue(null);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(firebaseUser.getUid())
                                    .child("FriendRequest").child("Sent").child(getIntent().getStringExtra("id"))
                                    .setValue(null).addOnCompleteListener(task -> getFriendsStatus());
                        });
                        alertDialog.setNegativeButton("No", (dialogInterface, i) -> {

                            dialogInterface.cancel();
                        });
                        alertDialog.create().show();
                    });
                }else{
                    btn.setColorFilter(getResources().getColor(R.color.red));
                    btn.setImageResource(R.drawable.ic_add_user);
                    btn.setOnClickListener(view -> {
                        FriendRequest friendRequest = new FriendRequest("Requested", firebaseUser.getUid(), getIntent().getStringExtra("id"));
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(getIntent().getStringExtra("id"))
                                .child("FriendRequest").child("Pending").child(friendRequest.getSenderID())
                                .setValue(friendRequest.getHashMap());
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(firebaseUser.getUid())
                                .child("FriendRequest").child("Sent").child(friendRequest.getReceiverID())
                                .setValue(friendRequest.getHashMap()).addOnCompleteListener(task -> getFriendsStatus());
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfileImg(){
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(user.getImageURL());
        Glide.with(this).load(storageReference).centerCrop().into(profileImg);
    }
    private void setBirthday(){
        birthday.setText(user.getBirthday());
        //TODO: format date + add zodiac
    }
    private void setCountry(){
        country.setText(user.getCountry());
        //TODO: add flag
    }
    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(user.getUsername());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}