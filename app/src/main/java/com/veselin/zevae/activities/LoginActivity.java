package com.veselin.zevae.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veselin.zevae.R;

public class LoginActivity extends AppCompatActivity {

    //Widgets
    EditText passwordET, emailET;
    Button loginBtn;
    TextView signUpBtn;
    //Firebase
    FirebaseAuth firebaseAuth;

    @Override
    protected void onStart() {
        super.onStart();
        setFirebaseUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        setForgotpasswordButton();
        initializeViews();
    }

    private void initializeViews(){
        passwordET = findViewById(R.id.password_input);
        emailET = findViewById(R.id.email_input);
        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                //TODO add data verification
                login(email, password);
            }
        });
        openSignUp();
    }
    private void openSignUp(){
        signUpBtn = findViewById(R.id.sign_up_btn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }
    private void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("IsBanned").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() == null){
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        finish();
                                    }else if(snapshot.getValue() == "false"){
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        finish();
                                    }else{
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setTitle("Your account is disabled");
                                        builder.create().show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }else{
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //Checking for user existance and saving user data
    private void setFirebaseUser(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(firebaseUser.getUid()).child("IsBanned").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() == null){
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }else if(snapshot.getValue() == "false"){
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle("Your account is disabled");
                        builder.create().show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }
    private void setForgotpasswordButton(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email:");

// Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setPadding(0, 30, 0, 0);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", (dialogInterface, i) ->
                FirebaseAuth.getInstance().sendPasswordResetEmail(input.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Email sent successfully!", Toast.LENGTH_SHORT).show();
                    }
                }));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        findViewById(R.id.forgot_pass_btn).setOnClickListener(view ->  builder.show());
    }

}