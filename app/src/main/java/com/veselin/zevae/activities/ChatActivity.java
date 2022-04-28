package com.veselin.zevae.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;
import com.veselin.zevae.R;
import com.veselin.zevae.adapters.MessagesListAdapter;
import com.veselin.zevae.models.Chat;
import com.veselin.zevae.models.Message;
import com.veselin.zevae.models.User;
import com.veselin.zevae.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity implements IPickResult {
    //sender - my User;
    private User myUser;
    //receiver - another user
    private User anotherUser;
    private Chat chat;
    private DatabaseReference chatRef;
    private RecyclerView mMessageRecycler;
    private MessagesListAdapter mMessageAdapter;
    private LinearLayout llProgressBar;
    private boolean isNewChat = false;
    private boolean firstTimeReceiveMessage = true;
    private Toolbar toolbar;
    private ValueEventListener messageListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initUsers();
    }
    private HashMap<DatabaseReference, ValueEventListener> hashMap = new HashMap<>();
    public static void removeValueEventListener(HashMap<DatabaseReference, ValueEventListener> hashMap) {
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : hashMap.entrySet()) {
            DatabaseReference databaseReference = entry.getKey();
            ValueEventListener valueEventListener = entry.getValue();
            databaseReference.removeEventListener(valueEventListener);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(messages.size() != 0) {
            if (!messages.get(messages.size() - 1).getSenderId().equals(myUser.getId()))
                chatRef.getParent().setValue(null);
        }
        hashMap.put(chatRef, messageListener);
        removeValueEventListener(hashMap);
        finish();
    }

    @Override
    protected void onDestroy() {

        finish();
        super.onDestroy();
    }

    private String getCurrUID(){
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    private void initUsers(){
        llProgressBar = findViewById(R.id.llProgressBar);
        llProgressBar.setVisibility(View.VISIBLE);
        DatabaseReference userSenderRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        userSenderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String userId = getIntent().getStringExtra("id");
        DatabaseReference userReceiverRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(Objects.requireNonNull(userId));

        userReceiverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                anotherUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        //Add loading screen so the initialazing methods can be executed
        new Thread() {
            @Override
            public void run() {
                while(!areUsersInit()) {
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
                        llProgressBar.setVisibility(View.GONE);
                        initChat(myUser, anotherUser);
                        initRecyclerView();
                        sendMessage();
                        sendImage();
                        initToolbar();
                    }
                });
            }
        }.start();
    }

    private boolean areUsersInit(){
            return myUser != null && anotherUser != null;
    }

    private void initChat(User user1, User user2){
        chat = new Chat(user1, user2);
        chatRef = FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(chat.getChatID())
                .child("Messages");
    }

    List<Message> messages = new ArrayList<>();
    private void previousConvs(){
        llProgressBar.setVisibility(View.VISIBLE);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) {
                    Message msg = new Message();
                    for (DataSnapshot message : snapshot.getChildren()) {
                        //Reading single message from firebase
                        for(DataSnapshot messageProperties : message.getChildren()){
                            if(Objects.equals(messageProperties.getKey(), "messages")){
                                msg.setMessage(Objects.requireNonNull(messageProperties.getValue()).toString());
                            }else if(Objects.equals(messageProperties.getKey(), "receiverId")){
                                msg.setReceiverId(Objects.requireNonNull(messageProperties.getValue()).toString());
                            }else if(messageProperties.getKey().equals("senderId")){
                                msg.setSenderId(Objects.requireNonNull(messageProperties.getValue()).toString());
                            }else if(messageProperties.getKey().equals("timestamp")){
                                msg.setTimestamp(Objects.requireNonNull(messageProperties.getValue()).toString());
                                messages.add(msg);
                                msg = new Message();
                            }
                        }
                    }
                    if(!messages.get(messages.size() - 1).getSenderId().equals(myUser.getId()))  {
                        updateConvos("Seen", "Seen", messages.get(messages.size() - 1).getTimestamp());
                        FirebaseDatabase.getInstance()
                                .getReference("Users")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("lastOnline").setValue(getCurrDate());
                        chatRef.getParent().setValue(null);
                    }
                }else{
                    isNewChat = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Add loading screen so the initialazing methods can be executed
        new Thread() {
            @Override
            public void run() {
                while(messages.size() == 0 && !isNewChat) {
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
                        mMessageAdapter = new MessagesListAdapter(ChatActivity.this, messages, getCurrUID(), anotherUser.getImageURL());
                        mMessageRecycler.setAdapter(mMessageAdapter);
                        llProgressBar.setVisibility(View.GONE);

                        messageReceiverListener();
                    }
                });
            }
        }.start();
    }

    private void initRecyclerView(){
        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(llm);
        if(isNewChat){
            mMessageAdapter = new MessagesListAdapter(ChatActivity.this, messages, getCurrUID(), anotherUser.getImageURL());
            mMessageRecycler.setAdapter(mMessageAdapter);
        }
        previousConvs();
    }

    EditText input;
    Message message;
    private void sendMessage(){
        ImageButton send = findViewById(R.id.send_btn);
        input = findViewById(R.id.message_input);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = new Message(input.getText().toString(), anotherUser.getId(), myUser.getId(), getCurrDate());
                chatRef.child(Objects.requireNonNull(chatRef.push().getKey())).setValue(message.buildMessageHashMap());
                input.setText("");
                messages.add(message);
                mMessageAdapter.notifyItemInserted(messages.size() - 1);
                updateConvos("Delivered", "Unread", getCurrDate());
                FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("lastOnline").setValue(getCurrDate());
            }
        });
    }
    private String getCurrDate(){
        String date;
        Date d = new Date();
        date = String.valueOf(d.getYear()) + "." + String.valueOf(d.getMonth()) +"."+ String.valueOf(d.getDate()) +"."+ String.valueOf(d.getHours() - (TimeZone.getDefault().getDSTSavings() + TimeZone.getDefault().getRawOffset())/3600000) +"."+ String.valueOf(d.getMinutes())+"."+ String.valueOf(d.getSeconds());
        return date;
    }
    private void messageReceiverListener(){
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!firstTimeReceiveMessage) {
                    if (snapshot.hasChildren()) {
                        Iterable<DataSnapshot> snapshots = snapshot.getChildren();
                        DataSnapshot snapshotLastMsg;
                        while (snapshots.iterator().hasNext()) {
                            snapshotLastMsg = snapshots.iterator().next();
                            if (!snapshots.iterator().hasNext()) {
                                Message msg = new Message();
                                if (snapshotLastMsg.child("senderId").getValue().toString().equals(anotherUser.getId())) {
                                    for (DataSnapshot messageProperties : snapshotLastMsg.getChildren()) {
                                        if (Objects.equals(messageProperties.getKey(), "messages")) {
                                            msg.setMessage(Objects.requireNonNull(messageProperties.getValue()).toString());
                                        } else if (Objects.equals(messageProperties.getKey(), "receiverId")) {
                                            msg.setReceiverId(Objects.requireNonNull(messageProperties.getValue()).toString());
                                        } else if (messageProperties.getKey().equals("senderId")) {
                                            msg.setSenderId(Objects.requireNonNull(messageProperties.getValue()).toString());
                                        } else if (messageProperties.getKey().equals("timestamp")) {
                                            updateConvos("Seen", "Seen", messageProperties.getValue().toString());
                                            FirebaseDatabase.getInstance()
                                                    .getReference("Users")
                                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("lastOnline").setValue(getCurrDate());
                                            msg.setTimestamp(Objects.requireNonNull(messageProperties.getValue()).toString());
                                            messages.add(msg);
                                            mMessageAdapter.notifyItemInserted(messages.size() - 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else{
                    firstTimeReceiveMessage = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };chatRef.addValueEventListener(messageListener);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = findViewById(R.id.title);
        title.setText(anotherUser.getUsername());
        Glide.with(ChatActivity.this).load(FirebaseStorage.getInstance().getReferenceFromUrl(anotherUser.getImageURL())).centerCrop().into((ImageView) findViewById(R.id.imageView));
        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, AnotherUserView.class).putExtra("id", anotherUser.getId()));
            }
        });
        TextView textView = (TextView) findViewById(R.id.last_seen_txt);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        if(anotherUser.getLastOnline() != null)
        textView.setText("Last seen - "+dateFormat.format(Utils.convertStringToDate(anotherUser.getLastOnline())));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void updateConvos(String msgStatusMUser, String msgStatusAUser, String date){
        DatabaseReference convoRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Convos").child(anotherUser.getId());
        convoRef.child("timestamp").setValue(date);
        convoRef.child("status").setValue(msgStatusMUser);

        DatabaseReference convoRefAUser = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(anotherUser.getId())
                .child("Convos").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        convoRefAUser.child("timestamp").setValue(date);
        convoRefAUser.child("status").setValue(msgStatusAUser);
    }

    private StorageReference imagePath;
    private Uri resultUri = null;
    private void sendImage(){
        findViewById(R.id.upload_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickImageDialog.build(new PickSetup()).show(ChatActivity.this);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imagePath = FirebaseStorage.getInstance().getReference().child("chat_pics")
                                        .child(Objects.requireNonNull(resultUri.getLastPathSegment()));
                                imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        message = new Message(imagePath.toString(), anotherUser.getId(), myUser.getId(), getCurrDate());
                                        chatRef.child(Objects.requireNonNull(chatRef.push().getKey())).setValue(message.buildMessageHashMap());
                                        input.setText("");
                                        messages.add(message);
                                        mMessageAdapter.notifyItemInserted(messages.size() - 1);
                                        updateConvos("Delivered", "Unread", getCurrDate());
                                        FirebaseDatabase.getInstance()
                                                .getReference("Users")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("lastOnline").setValue(getCurrDate());}
                                });
                            }
                        });
                    }
                }.start();
            }
        });
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            resultUri = r.getUri();
        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
            }
        }
    }

}