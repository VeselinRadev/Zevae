package com.veselin.zevae.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.veselin.zevae.activities.FriendListActivity;
import com.veselin.zevae.models.User;

import java.net.URL;


public class MyProfileFragment extends Fragment {
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
    public MyProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        setUser();
        return view;
    }

    private void setUser(){
        llProgressBar = view.findViewById(R.id.llProgressBar);
        llProgressBar.setVisibility(View.VISIBLE);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid());
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
                while(user == null || getActivity() == null) {
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                getActivity().runOnUiThread(() -> {
                    user.updateInteractionsConvos();
                    initViews();
                    llProgressBar.setVisibility(View.GONE);
                });
            }
        }.start();
    }

    private void initViews(){
        profileImg = view.findViewById(R.id.image);
        setProfileImg();
        interactionsTxt = view.findViewById(R.id.interactions_txt);
        interactionsTxt.setText(String.valueOf(user.getInteractionsCount()));
        friendsTxt = view.findViewById(R.id.friends_txt);
        friendsTxt.setText(String.valueOf(user.getFriendsCount()));
        description = view.findViewById(R.id.description_txt);
        description.setText(user.getDescription());
        gender = view.findViewById(R.id.gender_text);
        gender.setText(user.getGender());
        email = view.findViewById(R.id.email_txt);
        email.setText(user.getEmail());
        birthday = view.findViewById(R.id.birthday_txt);
        setBirthday();
        country = view.findViewById(R.id.country_txt);
        setCountry();
        view.findViewById(R.id.friends).setOnClickListener(view -> {startActivity(new Intent(getActivity(), FriendListActivity.class));});
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
}