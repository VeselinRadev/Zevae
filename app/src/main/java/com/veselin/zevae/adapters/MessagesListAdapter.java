package com.veselin.zevae.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.veselin.zevae.R;
import com.veselin.zevae.activities.ChatActivity;
import com.veselin.zevae.models.Message;
import com.veselin.zevae.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.time.format.DateTimeFormatter;


public class MessagesListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;

    private Context mContext;
    private List<Message> mMessageList;
    private String currUID;
    private String imageURL;

    public MessagesListAdapter(Context context, List<Message> messageList, String currUID, String imageURL) {
        mContext = context;
        mMessageList = messageList;
        this.currUID = currUID;
        this.imageURL = imageURL;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_SENT:
                ((SentImageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED:
                ((ReceivedImageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);
        if (message.getSenderId().equals(currUID)) {
            // If the current user is the sender of the message
            if (message.getMessage().contains("gs://zevae-f4156.appspot.com/chat_pics/")) {
                return VIEW_TYPE_IMAGE_SENT;
            }else return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            if (message.getMessage().contains("gs://zevae-f4156.appspot.com/chat_pics/")) {
                return VIEW_TYPE_IMAGE_RECEIVED;
            }else return VIEW_TYPE_MESSAGE_RECEIVED;
        }

    }

    // Inflates the appropriate layout according to the ViewType.
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_received, parent, false);
            return new ReceivedImageHolder(view);
        }else if (viewType == VIEW_TYPE_IMAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_sent, parent, false);
            return new SentImageHolder(view);
        }

        return null;
    }


    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        @SuppressLint("SetTextI18n")
        void bind(Message message) {
            messageText.setText(message.getMessage());
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            timeText.setText(dateFormat.format(Utils.convertStringToDate(message.getTimestamp())));
        }
    }
    private class SentImageHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        ImageView image;
        SentImageHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        @SuppressLint("SetTextI18n")
        void bind(Message message) {
            Glide.with(mContext).load(FirebaseStorage.getInstance().getReferenceFromUrl(message.getMessage())).centerCrop().into(image);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            timeText.setText(dateFormat.format(Utils.convertStringToDate(message.getTimestamp())));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            profileImage = itemView.findViewById(R.id.imageView);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            timeText.setText(dateFormat.format(Utils.convertStringToDate(message.getTimestamp())));
            Glide.with(mContext).load(FirebaseStorage.getInstance().getReferenceFromUrl(imageURL)).centerCrop().into(profileImage);
        }
    }

    private class ReceivedImageHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        ImageView profileImage;
        ImageView image;

        ReceivedImageHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            profileImage = itemView.findViewById(R.id.imageView);
        }

        void bind(Message message) {
            Glide.with(mContext).load(FirebaseStorage.getInstance().getReferenceFromUrl(message.getMessage())).centerCrop().into(image);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            timeText.setText(dateFormat.format(Utils.convertStringToDate(message.getTimestamp())));
            Glide.with(mContext).load(FirebaseStorage.getInstance().getReferenceFromUrl(imageURL)).centerCrop().into(profileImage);
        }
    }
}