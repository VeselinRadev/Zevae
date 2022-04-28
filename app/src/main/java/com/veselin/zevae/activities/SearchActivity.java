package com.veselin.zevae.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veselin.zevae.R;
import com.veselin.zevae.adapters.SearchResultsAdapter;
import com.veselin.zevae.models.User;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    /* access modifiers changed from: private */
    public ImageButton bt_clear;
    /* access modifiers changed from: private */
    public EditText et_search;
    /* access modifiers changed from: private */
    private List<User> users;
    private List<String> names;
    private RecyclerView recyclerView;
    private List<User> searchedUsers;
    TextWatcher textWatcher = new TextWatcher() {
        public void afterTextChanged(Editable editable) {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (charSequence.toString().trim().length() == 0) {
                SearchActivity.this.bt_clear.setVisibility(View.GONE);
                searchedUsers.clear();
                updateAdapter(users);
            } else {
                searchedUsers.clear();
                SearchActivity.this.bt_clear.setVisibility(View.VISIBLE);
                for(User user : users){
                    if(user.getUsername().toLowerCase().contains(charSequence.toString().toLowerCase()))searchedUsers.add(user);
                }
                updateAdapter(searchedUsers);
            }
        }
    };
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        users = new ArrayList<>();
        names = new ArrayList<>();
        loadUsers();
        initToolbar();
        initComponent();
        initRecyclerView();
        loadUsers();
    }

    private void loadUsers() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);

                    assert user != null;
                    if (!user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        names.add(user.getUsername());
                        users.add(user);
                    }
                }
                updateAdapter(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateAdapter(users);
    }

    private void updateAdapter(List<User> users){
        SearchResultsAdapter adapter = new SearchResultsAdapter(this, users);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void initComponent() {
        searchedUsers = new ArrayList<>();
        EditText editText = (EditText) findViewById(R.id.et_search);
        this.et_search = editText;
        editText.addTextChangedListener(this.textWatcher);
        ImageButton imageButton = (ImageButton) findViewById(R.id.bt_clear);
        this.bt_clear = imageButton;
        imageButton.setVisibility(View.GONE);

        this.bt_clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SearchActivity.this.et_search.setText("");
            }
        });
        this.et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 3) {
                    return false;
                }
                SearchActivity.this.hideKeyboard();
                SearchActivity.this.searchAction();
                return true;
            }
        });
        this.et_search.setOnTouchListener((view, motionEvent) -> {
            SearchActivity.this.getWindow().setSoftInputMode(5);
            return false;
        });
    }
    private void initToolbar() {
        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar = toolbar2;
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this, R.color.grey_1000);
    }
    public void searchAction() {
        String trim = this.et_search.getText().toString().trim();
        if (!trim.equals("")) {

            //this.mAdapterSuggestion.addSearchHistory(trim);
        }
    }

    public void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}