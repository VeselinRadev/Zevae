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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.veselin.zevae.activities.ChatActivity;
import com.veselin.zevae.R;
import com.veselin.zevae.models.User;
import com.veselin.zevae.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.RecyclerViewHolder>{
    private List<User> users;
    private Context context;
    private List<String> times;
    private List<String> status;
    // RecyclerView recyclerView;
    public ChatsAdapter(Context context, List<User> users, List<String> times, List<String> status) {
        this.context = context;
        this.users = users;
        this.times = times;
        this.status = status;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_list_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        final User mUser = users.get(position);
        final String mTime = times.get(position);
        final String mStatus = status.get(position);
        holder.getName().setText(mUser.getUsername());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        holder.getMsgStatus().setText(mStatus + " - " + dateFormat.format(Utils.convertStringToDate(mTime)));
        holder.getItem().setOnClickListener(view -> {
            context.startActivity(new Intent(context, ChatActivity.class).putExtra("id", mUser.getId()));
            ((Activity)context).finish();
        });
        Glide.with(context).load(FirebaseStorage.getInstance().getReferenceFromUrl(mUser.getImageURL())).centerCrop().into(holder.getImage());
        if(mStatus.equals("Unread")){
            holder.getName().setTypeface(null, Typeface.BOLD);
            holder.getMsgStatus().setTypeface(null, Typeface.BOLD);
            holder.getMsgStatus().setTextColor(Color.WHITE);
            holder.getName().setTextColor(Color.WHITE);
        }
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private LinearLayout item;
        private TextView msgStatus;
        private CircularImageView image;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.item = itemView.findViewById(R.id.ll_parent);
            this.msgStatus = itemView.findViewById(R.id.status);
            this.image = itemView.findViewById(R.id.image);
        }

        public LinearLayout getItem(){
            return item;
        }
        public TextView getName(){
            return name;
        }
        public TextView getMsgStatus() {return msgStatus;};
        public CircularImageView getImage() {return image; };
    }
}
