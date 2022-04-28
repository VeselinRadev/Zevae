package com.veselin.zevae.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veselin.zevae.R;
import com.veselin.zevae.fragments.ConnectFragment;
import com.veselin.zevae.fragments.MapsFragment2;
import com.veselin.zevae.fragments.MessagesFragment;
import com.veselin.zevae.fragments.MyProfileFragment;
import com.veselin.zevae.models.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;
    private User user;
    private Toolbar toolbar;
    private LinearLayout llProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();
        setUser();
        initializeBottomNavigationView();
    }

    private void initializeBottomNavigationView(){
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new MessagesFragment())
                .commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment activeFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_messages:
                    activeFragment = new MessagesFragment();
                    toolbar.setTitle("Messages");
                    break;

                case R.id.navigation_location:
                    activeFragment = new MapsFragment2();
                    toolbar.setTitle("");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                    break;

//                case R.id.navigation_camera:
//                    //activeFragment = new CameraFragment();
//                    break;
                case R.id.navigation_connect:
                    toolbar.setTitle("Connect");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                    activeFragment = new ConnectFragment();
                    break;
                case R.id.navigation_my_profile:
                    activeFragment = new MyProfileFragment();
                    toolbar.setTitle(user.getUsername());
                    break;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, activeFragment)
                    .commit();
            return true;
        }
    };

    private void setUser() {
        llProgressBar = findViewById(R.id.llProgressBar);
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
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.nav_host_fragment, new MessagesFragment())
                                .commit();
                        setToolbar();
                        llProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class).putExtra("id", user.getId()));
            return true;
        } else if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class).putExtra("id", user.getId()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Messages");
    }
}