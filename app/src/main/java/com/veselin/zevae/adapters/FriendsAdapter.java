package com.veselin.zevae.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.veselin.zevae.R;
import com.veselin.zevae.activities.AnotherUserView;
import com.veselin.zevae.activities.ChatActivity;
import com.veselin.zevae.activities.MainActivity;
import com.veselin.zevae.activities.StoryActivity;
import com.veselin.zevae.fragments.MessagesFragment;
import com.veselin.zevae.models.User;
import com.veselin.zevae.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.RecyclerViewHolder> {
    private List<User> users;
    private Context context;
    public FriendsAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public FriendsAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        return new FriendsAdapter.RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        final User mUser = users.get(position);
        checkAreThereStories(mUser);
        holder.getName().setText(mUser.getUsername());
        Glide.with(context).load(FirebaseStorage.getInstance().getReferenceFromUrl(mUser.getImageURL())).centerCrop().into(holder.getImage());
        new Thread() {
            @Override
            public void run() {
                while(mUser.getHasStory() == null) {
                    Log.d("TAG", "run: " + mUser.getHasStory());
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                ((MainActivity)context).runOnUiThread((Runnable) () -> {
                    Log.d("TAG", "run: " + String.valueOf(mUser.getHasStory()) +mUser.getUsername());
                    if(mUser.getHasStory()){
                        holder.getImage().setBorderWidth(5);
                        holder.getItem().setOnClickListener(view -> {
                            context.startActivity(new Intent(context, StoryActivity.class).putExtra("id", mUser.getId()));
                            ((Activity)context).finish();
                        });
                    }else {
                        holder.getImage().setBorderWidth(0);
                        holder.getImage().setBorderColor(R.color.colorAccent);
                        holder.getImage().setCircleColor(R.color.colorAccent);
                        holder.getItem().setOnClickListener(view -> {
                            context.startActivity(new Intent(context, AnotherUserView.class).putExtra("id", mUser.getId()));
                            ((Activity)context).finish();
                        });
                    }
                });
            }
        }.start();
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private LinearLayout item;
        private CircularImageView image;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.item = itemView.findViewById(R.id.ll_parent);
            this.image = itemView.findViewById(R.id.image);
        }

        public LinearLayout getItem(){
            return item;
        }
        public TextView getName(){
            return name;
        }
        public CircularImageView getImage() {return image; };
    }

    private void checkAreThereStories(User user){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getId()).child("Story").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() != 0)user.setHasStory(true);
                else user.setHasStory(false);
                Log.d("TAG", "onDataChange: " + snapshot.getChildrenCount() + user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
