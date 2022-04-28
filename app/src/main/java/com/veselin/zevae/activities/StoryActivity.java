package com.veselin.zevae.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.veselin.zevae.R;
import com.veselin.zevae.fragments.MessagesFragment;
import com.veselin.zevae.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    private int PROGRESS_COUNT;
    private Thread t;
    private StoriesProgressView storiesProgressView;
    private ImageView image;
    List<String> stories = new ArrayList<>();
    List<String> storiesId = new ArrayList<>();
    private int counter = 0;
    private String[] resources;
    private long pressTime = 0L;
    private long limit = 500L;

    private void retrieveStories(){
        stories.clear();
        storiesId.clear();
        FirebaseDatabase.getInstance().getReference("Users")
                .child(getIntent().getStringExtra("id")).child("Story").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PROGRESS_COUNT = (int) snapshot.getChildrenCount();

                for(DataSnapshot snap : snapshot.getChildren()){
                    Log.d("TAG", "onNext: " + PROGRESS_COUNT + snap.getKey());
                    stories.add(snap.child("imageURL").getValue().toString());
                    storiesId.add(snap.getKey());
                }

                //check if all his friends have seen the story and delete it
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        t = new Thread() {
            @Override
            public void run() {
                while(stories.size() == 0) {
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                runOnUiThread(() -> {
                    resources = new String[stories.size()];
                    for(int  i = 0; i < stories.size(); i++){
                        resources[i] = stories.get(i);
                    }
                    storiesProgressView = findViewById(R.id.stories);
                    storiesProgressView.setStoriesCount(PROGRESS_COUNT);
                    storiesProgressView.setStoryDuration(3000L);
                    // or
                    // storiesProgressView.setStoriesCountWithDurations(durations);
                    storiesProgressView.setStoriesListener(StoryActivity.this);
                    storiesProgressView.startStories();
                    //counter = 2;
                    //storiesProgressView.startStories(counter);
                    image = (ImageView) findViewById(R.id.image);
                    Glide.with(StoryActivity.this).load(FirebaseStorage.getInstance().getReferenceFromUrl(resources[counter])).centerCrop().into(image);
                    initTime(storiesId.get(counter));


                    // bind reverse view
                    View reverse = findViewById(R.id.reverse);
                    reverse.setOnClickListener(v -> storiesProgressView.reverse());
                    reverse.setOnTouchListener(onTouchListener);

                    // bind skip view
                    View skip = findViewById(R.id.skip);
                    skip.setOnClickListener(v -> storiesProgressView.skip());
                    skip.setOnTouchListener(onTouchListener);
                    if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(getIntent().getStringExtra("id"))) {
                        findViewById(R.id.delete_story).setOnClickListener(view -> {
                            deleteStory(storiesId.get(counter), resources.length);
                            storiesProgressView.pause();
                        });
                        findViewById(R.id.delete_story).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.delete_story).setVisibility(View.GONE);
                    }
                });
            }
        };
        t.start();


    }
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story);
        retrieveStories();
        initPicture();
        initName();
    }

    private void initPicture() {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(getIntent().getStringExtra("id")).child("imageURL").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Glide.with(StoryActivity.this).load(snapshot.getValue()).centerCrop().into((ImageView) findViewById(R.id.profile_pic));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initTime(String id) {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(getIntent().getStringExtra("id")).child("Story").child(id).child("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
                ((TextView)findViewById(R.id.time)).setText(dateFormat.format(Utils.convertStringToDate(snapshot.getValue().toString())) + "h.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initName() {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(getIntent().getStringExtra("id")).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((TextView)findViewById(R.id.name)).setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onNext() {
        Glide.with(this).load(FirebaseStorage.getInstance().getReferenceFromUrl(resources[++counter])).centerCrop().into(image);
        initTime(storiesId.get(counter));

    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Glide.with(this).load(FirebaseStorage.getInstance().getReferenceFromUrl(resources[--counter])).centerCrop().into(image);
        initTime(storiesId.get(counter));
    }

    @Override
    public void onComplete() {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }
    private void deleteStory(String currStoryId, int storiesCount){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete this story:");

// Set up the input
       builder.setPositiveButton("Yes", (dialogInterface, i) -> {
           FirebaseDatabase.getInstance().getReference("Users")
                   .child(getIntent().getStringExtra("id")).child("Story").child(currStoryId).setValue(null).addOnCompleteListener(task ->{
                       if(storiesCount >= 1) startActivity(new Intent(StoryActivity.this, StoryActivity.class).putExtra("id", getIntent().getStringExtra("id")));
                       finish();
                   });

       });
       builder.setNegativeButton("No", (dialogInterface, i) -> storiesProgressView.resume());
       builder.setOnCancelListener(dialogInterface -> storiesProgressView.resume());
       builder.setOnDismissListener(dialogInterface -> storiesProgressView.resume());
       builder.show();

    }
}