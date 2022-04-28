package com.veselin.zevae.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.veselin.zevae.R;
import com.veselin.zevae.activities.AnotherUserView;
import com.veselin.zevae.models.User;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.RecyclerViewHolder>{
    private List<User> users;
    private Context context;
    // RecyclerView recyclerView;
    public SearchResultsAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        final User mUser = users.get(position);
        holder.getName().setText(mUser.getUsername());
        holder.getItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, AnotherUserView.class).putExtra("id", mUser.getId()));
            }
        });
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
}
