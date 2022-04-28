package com.veselin.zevae.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.veselin.zevae.R;
import com.veselin.zevae.activities.AnotherUserView;
import com.veselin.zevae.models.FriendRequest;
import com.veselin.zevae.models.User;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RecyclerViewHolder>{
    private List<User> users;
    private Context context;
    // RecyclerView recyclerView;
    public RequestAdapter (Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        final User mUser = users.get(position);
        holder.getName().setText(mUser.getUsername());
        holder.getItem().setOnClickListener(view -> context.startActivity(new Intent(context, AnotherUserView.class).putExtra("id", mUser.getId())));
        holder.getAcceptBtn().setOnClickListener(view -> acceptFriendRequest(mUser));
        holder.getDeclineBtn().setOnClickListener(view -> declineFriendRequest(mUser));
        Glide.with(context).load(FirebaseStorage.getInstance().getReferenceFromUrl(mUser.getImageURL())).centerCrop().into(holder.getImage());
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private LinearLayout item;
        private CircularImageView image;
        private ImageButton acceptBtn;
        private ImageButton declineBtn;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.item = itemView.findViewById(R.id.ll_parent);
            this.image = itemView.findViewById(R.id.image);
            this.acceptBtn = itemView.findViewById(R.id.accept_btn);
            this.declineBtn = itemView.findViewById(R.id.decline_btn);
        }
        public ImageButton getAcceptBtn() {return acceptBtn;}
        public ImageButton getDeclineBtn() {return declineBtn;}
        public LinearLayout getItem(){
            return item;
        }
        public TextView getName(){
            return name;
        }
        public CircularImageView getImage() {return image; };
    }

    private void acceptFriendRequest(User user){
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            FriendRequest friendRequest = new FriendRequest("Requested", firebaseUser.getUid(), user.getId());
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.getId())
                    .child("FriendRequest").child("Accepted").child(firebaseUser.getUid()).setValue(friendRequest.getHashMap());
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseUser.getUid())
                    .child("FriendRequest").child("Accepted").child(user.getId())
                    .setValue(friendRequest.getHashMap());
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.getId())
                    .child("FriendRequest").child("Sent").child(firebaseUser.getUid()).setValue(null);
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseUser.getUid())
                    .child("FriendRequest").child("Pending").child(user.getId())
                    .setValue(null);

    }
    private void declineFriendRequest(User user){
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(user.getId())
                    .child("FriendRequest").child("Sent").child(firebaseUser.getUid()).setValue(null);
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseUser.getUid())
                    .child("FriendRequest").child("Pending").child(user.getId())
                    .setValue(null);
    }
}
