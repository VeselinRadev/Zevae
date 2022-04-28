package com.veselin.zevae.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements IPickResult {

    //Widgets
    EditText usernameET, passwordET, emailET;
    Button registerBtn;

    //Firebase
    FirebaseAuth firebaseAuth;
    DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        initializeViews();
    }

    private void initializeViews(){
        usernameET = findViewById(R.id.username_input);
        passwordET = findViewById(R.id.password_input);
        emailET = findViewById(R.id.email_input);
        findViewById(R.id.image).setOnClickListener(view -> setImage());
        findViewById(R.id.login_btn).setOnClickListener(view -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
        registerBtn = findViewById(R.id.sign_up_btn);
        registerBtn.setOnClickListener(view -> {
            String username = usernameET.getText().toString();
            String email = emailET.getText().toString();
            String password = passwordET.getText().toString();
            if(isValid(username, email, password, resultUri))
            register(username, email, password, resultUri.toString());
        });
    }

    private void register(final String username, String email, String password, String imageURL){
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();
                            myRef = FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(userId);
                            imagePath = FirebaseStorage.getInstance().getReference().child("profile_pics")
                                    .child(Objects.requireNonNull(resultUri.getLastPathSegment()));
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", imagePath.toString());

                            //Starting MainActivity after successful login

                            imagePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> myRef.setValue(hashMap).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                }else{
                                    Toast.makeText(RegisterActivity.this, "Error registering", Toast.LENGTH_SHORT).show();
                                }
                            }));

                        }else{
                            Toast.makeText(RegisterActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private StorageReference imagePath;
    private Uri resultUri = null;
    private void setImage(){
        PickImageDialog.build(new PickSetup()).show(RegisterActivity.this);
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
                        Glide.with(RegisterActivity.this).load(resultUri).centerCrop().into((ImageView) findViewById(R.id.image));
                        //imagePath.putFile(resultUri);
                    }

                });
            }
        }.start();
    }
    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            Uri mImageUri = r.getUri();
            CropImage.activity(mImageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
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

    private boolean isValid(String username, String email, String password, Uri imagePath){
        boolean isValid = true;
        if(!email.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$")){
            isValid = false;
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
        }
        if(username.length() == 0){
            isValid = false;
            Toast.makeText(this, "Username can't be empty", Toast.LENGTH_SHORT).show();
        }
        if(password.length() < 5){
            isValid = false;
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
        }
        if(imagePath == null){
            isValid = false;
            Toast.makeText(this, "Please upload image", Toast.LENGTH_SHORT).show();
        }
        return isValid;
    }

}