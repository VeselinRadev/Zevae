package com.veselin.zevae.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.veselin.zevae.R;
import com.veselin.zevae.activities.StoryActivity;
import com.veselin.zevae.adapters.ChatsAdapter;
import com.veselin.zevae.adapters.FriendsAdapter;
import com.veselin.zevae.models.Message;
import com.veselin.zevae.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;


public class MessagesFragment extends Fragment {
    private List<User> users;
    private List<String> status;
    private List<String> times;
    private RecyclerView recyclerViewChat;
    private RecyclerView recyclerViewFriends;
    private List<User> friends;
    public MessagesFragment(){

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        users = new ArrayList<>();
        status = new ArrayList<>();
        times = new ArrayList<>();
        initMyStories(view);
        setAddStoryBtn(view);
        initChatRecyclerView(view);
        displayUsers();
        friends = new ArrayList<>();
        initFriendsRecyclerView(view);
        displayFriends();
        return view;
    }

    private void initChatRecyclerView(View view) {
        recyclerViewChat = view.findViewById(R.id.recyclerView);
        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(view.getContext()));
        updateChatAdapter();
    }

    private void updateChatAdapter(){
        ChatsAdapter adapter = new ChatsAdapter(getContext(), users, times, status);
        recyclerViewChat.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void displayUsers(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("Convos");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                times.clear();
                status.clear();
                users.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    FirebaseDatabase.getInstance().getReference("Users").child(snap.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotUser) {
                            User user = snapshotUser.getValue(User.class);
                            Set<User> depdupeCustomers = new LinkedHashSet<>(users);
                            users.clear();
                            users.addAll(depdupeCustomers);
                            assert firebaseUser != null;
                            assert user != null;
                            if (!user.getId().equals(firebaseUser.getUid())) {
                                users.add(user);
                                for(DataSnapshot snapInfo : snap.getChildren()){
                                    switch(snapInfo.getKey()){
                                        case "status":
                                            status.add(snapInfo.getValue().toString());
                                            break;
                                        case "timestamp":
                                            times.add(snapInfo.getValue().toString());
                                            break;
                                    }
                                }
                            }

                            updateChatAdapter();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void displayFriends(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("FriendRequest").child("Accepted");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friends.clear();
                for(DataSnapshot snap : snapshot.getChildren()){
                    FirebaseDatabase.getInstance().getReference("Users").child(snap.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotUser) {
                            User user = snapshotUser.getValue(User.class);
                            for(User friend : friends){
                                if(friend.getId().equals(user.getId()))friends.remove(friend);
                            }

                            //assert firebaseUser != null;
                            //assert user != null;
                            if (!user.getId().equals(firebaseUser.getUid())) {
                                friends.add(user);
                            }
                            updateFriendsAdapter();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateFriendsAdapter() {
        FriendsAdapter adapter = new FriendsAdapter(getContext() ,friends);
        recyclerViewFriends.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    private void initFriendsRecyclerView(View view) {
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);
        recyclerViewFriends.setHasFixedSize(true);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        updateChatAdapter();
    }
    private StorageReference imagePath;
    private Uri resultUri = null;
    private void setAddStoryBtn(View v){
        LinearLayout ll = v.findViewById(R.id.ll_parent);
        ll.setOnLongClickListener(view -> {
            PickImageDialog.build(new PickSetup()).show(getActivity()).setOnPickResult(r -> {
                if (r.getError() == null) {
                    Uri mImageUri = r.getUri();
                    Intent intent = CropImage.activity(mImageUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .getIntent(getContext());
                    startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                } else {
                    Toast.makeText(getActivity(), r.getError().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            new Thread() {
                @Override
                public void run() {
                    while(resultUri == null) {
                        try {
                            sleep(200);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imagePath = FirebaseStorage.getInstance().getReference().child("stories")
                                    .child(Objects.requireNonNull(resultUri.getLastPathSegment()));
                            imagePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> {
                                FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(fu.getUid());
                                DatabaseReference story = myRef.child("Story").push();
                                story.child("imageURL").setValue(imagePath.toString());
                                story.child("timestamp").setValue(getCurrDate().toString());
                                resultUri = null;
                            });
                        }
                    });
                }
            }.start();
            return false;
        });
    }
    private String getCurrDate(){
        String date;
        Date d = new Date();
        date = String.valueOf(d.getYear()) + "." + String.valueOf(d.getMonth()) +"."+ String.valueOf(d.getDate()) +"."+ String.valueOf(d.getHours() - (TimeZone.getDefault().getDSTSavings() + TimeZone.getDefault().getRawOffset())/3600000) +"."+ String.valueOf(d.getMinutes())+"."+ String.valueOf(d.getSeconds());
        return date;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
            }
        }
    }

    private void initMyStories(View v){
        CircularImageView image = v.findViewById(R.id.image);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("imageURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Glide.with(MessagesFragment.this).load(FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.getValue().toString())).centerCrop().into(image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("Story").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    image.setBorderWidth(5);
                    v.findViewById(R.id.ll_parent).setOnClickListener(view -> {
                        startActivity(new Intent(getActivity(), StoryActivity.class).putExtra("id", firebaseUser.getUid()));
                    });
                }else{
                    image.setBorderWidth(0);
                    setAddStoryBtn(v);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}