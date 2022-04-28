package com.veselin.zevae.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;
import com.veselin.zevae.R;
import com.veselin.zevae.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements IPickResult {

    private User user;
    private Toolbar toolbar;
    private LinearLayout llProgressBar;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUser();
        initToolbar();
        initViews();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    private void initViews(){
        LinearLayout changeProfilePic = findViewById(R.id.change_profile_pic);
        changeProfilePic.setOnClickListener(llOnClickListener);
        LinearLayout changeDescription = findViewById(R.id.change_description);
        changeDescription.setOnClickListener(llOnClickListener);
        LinearLayout changeGender = findViewById(R.id.change_gender);
        changeGender.setOnClickListener(llOnClickListener);
        LinearLayout changeBirthday = findViewById(R.id.change_birthday);
        changeBirthday.setOnClickListener(llOnClickListener);
        LinearLayout changeCountry = findViewById(R.id.change_country);
        changeCountry.setOnClickListener(llOnClickListener);
        LinearLayout logout = findViewById(R.id.logout);
        logout.setOnClickListener(llOnClickListener);
    }
    private LinearLayout.OnClickListener llOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.change_profile_pic:
                    changeImage();
                    break;
                case R.id.change_description:
                    changeDescription();
                    break;
                case R.id.change_gender:
                    changeGender();
                    break;
                case R.id.change_birthday:
                    changeBirthday();
                    break;
                case R.id.change_country:
                    changeCountry();
                    break;
                case R.id.logout:
                    logout();
                    break;
            }
        }
    };

    private void logout() {
        AuthUI.getInstance().signOut(SettingsActivity.this)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            });
    }

    private void setUser() {
        llProgressBar = findViewById(R.id.llProgressBar);
        llProgressBar.setVisibility(View.VISIBLE);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
                        llProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }.start();
    }

    private StorageReference imagePath;
    private Uri resultUri = null;
    private void changeImage(){
        PickImageDialog.build(new PickSetup()).show(SettingsActivity.this);
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
                        imagePath = FirebaseStorage.getInstance().getReference().child("profile_pics")
                                .child(Objects.requireNonNull(resultUri.getLastPathSegment()));
                        imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(fu.getUid());
                                myRef.child("imageURL").setValue(imagePath.toString());
                            }
                        });
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

    private String description = "";

    private void changeDescription() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Description:");

// Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setPadding(0, 30, 0, 0);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                description = input.getText().toString();
                myRef.child("description").setValue(description);
                Toast.makeText(SettingsActivity.this, "Description changed!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void changeGender() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gender:");

// Set up the input
        String[] arr = {"\uD83D\uDC68 Male", "\uD83D\uDC69 Female", "❓ Other"};
        builder.setSingleChoiceItems(arr, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myRef.child("gender").setValue(arr[i]);
                Toast.makeText(SettingsActivity.this, "Gender changed!", Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }
    private void changeBirthday() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DatePicker picker = new DatePicker(this);
        picker.setCalendarViewShown(true);
        builder.setView(picker);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String date = getZodiac(picker.getDayOfMonth(), picker.getMonth()) + " " + picker.getDayOfMonth() + "." + picker.getMonth() + "." + picker.getYear();
                myRef.child("birthday").setValue(date);
                Toast.makeText(SettingsActivity.this, "Birthday changed!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private String getZodiac(int day, int month){
        switch (month) {
            case 1:
                if (day < 20) {
                    return "♑";
                } else {
                    return "♒";
                }
            case 2:
                if (day < 18) {
                    return "♒";
                } else {
                    return "♓";
                }
            case 3:
                if (day < 21) {
                    return "♓";
                } else {
                    return "♈";
                }
            case 4:
                if (day < 20) {
                    return "♈";
                } else {
                    return "♉";
                }
            case 5:
                if (day < 21) {
                    return "♉";
                } else {
                    return "♊";
                }
            case 6:
                if (day < 21) {
                    return "♊";
                } else {
                    return "♋";
                }
            case 7:
                if (day < 23) {
                    return "♋";
                } else {
                    return "♌";
                }
            case 8:
                if (day < 23) {
                    return "♌";
                } else {
                    return "♍";
                }
            case 9:
                if (day < 23) {
                    return "♍";
                } else {
                    return "♎";
                }
            case 10:
                if (day < 23) {
                    return "♎";
                } else {
                    return "♏";
                }
            case 11:
                if (day < 22) {
                    return "♏";
                } else {
                    return "♐";
                }
            case 12:
                if (day < 22) {
                    return "♐";
                } else {
                    return "♑";
                }
        }
        return null;
    }

    private void changeCountry(){
        CountryPicker.Builder builder =
                new CountryPicker.Builder().theme(R.style.AppTheme).with(SettingsActivity.this)
                        .listener(new OnCountryPickerListener() {
            @Override
            public void onSelectCountry(Country country) {
                myRef.child("country").setValue(getCountryFlag(country.getName()) + country.getName());
                Toast.makeText(SettingsActivity.this, "Country changed!", Toast.LENGTH_SHORT).show();
            }
        });
        CountryPicker picker = builder.build();

        picker.showDialog(SettingsActivity.this);

    }
    private static Map<String, String> countries = new HashMap<>();
    public static String getCountryFlag(String v) {
        countries.put("🇻🇪🇦🇨 ", "Ascension Island");
        countries.put("🇦🇩 ", "Andorra");
        countries.put("🇦🇪 ", "United Arab Emirates");
        countries.put("🇦🇫 ", "Afghanistan");
        countries.put("🇦🇬 ", "Antigua & Barbuda");
        countries.put("🇦🇮 ", "Anguilla");
        countries.put("🇦🇱 ", "Albania");
        countries.put("🇦🇲 ", "Armenia");
        countries.put("🇦🇴 ", "Angola");
        countries.put("🇦🇶 ", "Antarctica");
        countries.put("🇦🇷 ", "Argentina");
        countries.put("🇦🇸 ", "American Samoa");
        countries.put("🇦🇹 ", "Austria");
        countries.put("🇦🇺 ", "Australia");
        countries.put("🇦🇼 ", "Aruba");
        countries.put("🇦🇽 ", "Åland Islands");
        countries.put("🇦🇿 ", "Azerbaijan");
        countries.put("🇧🇦 ", "Bosnia & Herzegovina");
        countries.put("🇧🇧 ", "Barbados");
        countries.put("🇧🇩 ", "Bangladesh");
        countries.put("🇧🇪 ", "Belgium");
        countries.put("🇧🇫 ", "Burkina Faso");
        countries.put("🇧🇬 ", "Bulgaria");
        countries.put("🇧🇭 ", "Bahrain");
        countries.put("🇧🇮 ", "Burundi");
        countries.put("🇧🇯 ", "Benin");
        countries.put("🇧🇱 ", "St. Barthélemy");
        countries.put("🇧🇲 ", "Bermuda");
        countries.put("🇧🇳 ", "Brunei");
        countries.put("🇧🇴 ", "Bolivia");
        countries.put("🇧🇶 ", "Caribbean Netherlands");
        countries.put("🇧🇷 ", "Brazil");
        countries.put("🇧🇸 ", "Bahamas");
        countries.put("🇧🇹 ", "Bhutan");
        countries.put("🇧🇻 ", "Bouvet Island");
        countries.put("🇧🇼 ", "Botswana");
        countries.put("🇧🇾 ", "Belarus");
        countries.put("🇧🇿 ", "Belize");
        countries.put("🇨🇦 ", "Canada");
        countries.put("🇨🇨 ", "Cocos (Keeling) Islands");
        countries.put("🇨🇩 ", "Congo - Kinshasa");
        countries.put("🇨🇫 ", "Central African Republic");
        countries.put("🇨🇬 ", "Congo - Brazzaville");
        countries.put("🇨🇭 ", "Switzerland");
        countries.put("🇨🇮 ", "Côte d’Ivoire");
        countries.put("🇨🇰 ", "Cook Islands");
        countries.put("🇨🇱 ", "Chile");
        countries.put("🇨🇲 ", "Cameroon");
        countries.put("🇨🇳 ", "China");
        countries.put("🇨🇴 ", "Colombia");
        countries.put("🇨🇵 ", "Clipperton Island");
        countries.put("🇨🇷 ", "Costa Rica");
        countries.put("🇨🇺 ", "Cuba");
        countries.put("🇨🇻 ", "Cape Verde");
        countries.put("🇨🇼 ", "Curaçao");
        countries.put("🇨🇽 ", "Christmas Island");
        countries.put("🇨🇾 ", "Cyprus");
        countries.put("🇨🇿 ", "Czechia");
        countries.put("🇩🇪 ", "Germany");
        countries.put("🇩🇬 ", "Diego Garcia");
        countries.put("🇩🇯 ", "Djibouti");
        countries.put("🇩🇰 ", "Denmark");
        countries.put("🇩🇲 ", "Dominica");
        countries.put("🇩🇴 ", "Dominican Republic");
        countries.put("🇩🇿 ", "Algeria");
        countries.put("🇪🇦 ", "Ceuta & Melilla");
        countries.put("🇪🇨 ", "Ecuador");
        countries.put("🇪🇪 ", "Estonia");
        countries.put("🇪🇬 ", "Egypt");
        countries.put("🇪🇭 ", "Western Sahara");
        countries.put("🇪🇷 ", "Eritrea");
        countries.put("🇪🇸 ", "Spain");
        countries.put("🇪🇹 ", "Ethiopia");
        countries.put("🇪🇺 ", "European Union");
        countries.put("🇫🇮 ", "Finland");
        countries.put("🇫🇯 ", "Fiji");
        countries.put("🇫🇰 ", "Falkland Islands");
        countries.put("🇫🇲 ", "Micronesia");
        countries.put("🇫🇴 ", "Faroe Islands");
        countries.put("🇫🇷 ", "France");
        countries.put("🇬🇦 ", "Gabon");
        countries.put("🇬🇧 ", "United Kingdom");
        countries.put("🇬🇩 ", "Grenada");
        countries.put("🇬🇪 ", "Georgia");
        countries.put("🇬🇫 ", "French Guiana");
        countries.put("🇬🇬 ", "Guernsey");
        countries.put("🇬🇭 ", "Ghana");
        countries.put("🇬🇮 ", "Gibraltar");
        countries.put("🇬🇱 ", "Greenland");
        countries.put("🇬🇲 ", "Gambia");
        countries.put("🇬🇳 ", "Guinea");
        countries.put("🇬🇵 ", "Guadeloupe");
        countries.put("🇬🇶 ", "Equatorial Guinea");
        countries.put("🇬🇷 ", "Greece");
        countries.put("🇬🇸 ", "South Georgia & South Sand");
        countries.put("🇬🇹 ", "Guatemala");
        countries.put("🇬🇺 ", "Guam");
        countries.put("🇬🇼 ", "Guinea-Bissau");
        countries.put("🇬🇾 ", "Guyana");
        countries.put("🇭🇰 ", "Hong Kong SAR China");
        countries.put("🇭🇲 ", "Heard & McDonald Islands");
        countries.put("🇭🇳 ", "Honduras");
        countries.put("🇭🇷 ", "Croatia");
        countries.put("🇭🇹 ", "Haiti");
        countries.put("🇭🇺 ", "Hungary");
        countries.put("🇮🇨 ", "Canary Islands");
        countries.put("🇮🇩 ", "Indonesia");
        countries.put("🇮🇪 ", "Ireland");
        countries.put("🇮🇱 ", "Israel");
        countries.put("🇮🇲 ", "Isle of Man");
        countries.put("🇮🇳 ", "India");
        countries.put("🇮🇴 ", "British Indian Ocean Terri");
        countries.put("🇮🇶 ", "Iraq");
        countries.put("🇮🇷 ", "Iran");
        countries.put("🇮🇸 ", "Iceland");
        countries.put("🇮🇹 ", "Italy");
        countries.put("🇯🇪 ", "Jersey");
        countries.put("🇯🇲 ", "Jamaica");
        countries.put("🇯🇴 ", "Jordan");
        countries.put("🇯🇵 ", "Japan");
        countries.put("🇰🇪 ", "Kenya");
        countries.put("🇰🇬 ", "Kyrgyzstan");
        countries.put("🇰🇭 ", "Cambodia");
        countries.put("🇰🇮 ", "Kiribati");
        countries.put("🇰🇲 ", "Comoros");
        countries.put("🇰🇳 ", "St. Kitts & Nevis");
        countries.put("🇰🇵 ", "North Korea");
        countries.put("🇰🇷 ", "South Korea");
        countries.put("🇰🇼 ", "Kuwait");
        countries.put("🇰🇾 ", "Cayman Islands");
        countries.put("🇰🇿 ", "Kazakhstan");
        countries.put("🇱🇦 ", "Laos");
        countries.put("🇱🇧 ", "Lebanon");
        countries.put("🇱🇨 ", "St. Lucia");
        countries.put("🇱🇮 ", "Liechtenstein");
        countries.put("🇱🇰 ", "Sri Lanka");
        countries.put("🇱🇷 ", "Liberia");
        countries.put("🇱🇸 ", "Lesotho");
        countries.put("🇱🇹 ", "Lithuania");
        countries.put("🇱🇺 ", "Luxembourg");
        countries.put("🇱🇻 ", "Latvia");
        countries.put("🇱🇾 ", "Libya");
        countries.put("🇲🇦 ", "Morocco");
        countries.put("🇲🇨 ", "Monaco");
        countries.put("🇲🇩 ", "Moldova");
        countries.put("🇲🇪 ", "Montenegro");
        countries.put("🇲🇫 ", "St. Martin");
        countries.put("🇲🇬 ", "Madagascar");
        countries.put("🇲🇭 ", "Marshall Islands");
        countries.put("🇲🇰 ", "North Macedonia");
        countries.put("🇲🇱 ", "Mali");
        countries.put("🇲🇲 ", "Myanmar (Burma)");
        countries.put("🇲🇳 ", "Mongolia");
        countries.put("🇲🇴 ", "Macao Sar China");
        countries.put("🇲🇵 ", "Northern Mariana Islands");
        countries.put("🇲🇶 ", "Martinique");
        countries.put("🇲🇷 ", "Mauritania");
        countries.put("🇲🇸 ", "Montserrat");
        countries.put("🇲🇹 ", "Malta");
        countries.put("🇲🇺 ", "Mauritius");
        countries.put("🇲🇻 ", "Maldives");
        countries.put("🇲🇼 ", "Malawi");
        countries.put("🇲🇽 ", "Mexico");
        countries.put("🇲🇾 ", "Malaysia");
        countries.put("🇲🇿 ", "Mozambique");
        countries.put("🇳🇦 ", "Namibia");
        countries.put("🇳🇨 ", "New Caledonia");
        countries.put("🇳🇪 ", "Niger");
        countries.put("🇳🇫 ", "Norfolk Island");
        countries.put("🇳🇬 ", "Nigeria");
        countries.put("🇳🇮 ", "Nicaragua");
        countries.put("🇳🇱 ", "Netherlands");
        countries.put("🇳🇴 ", "Norway");
        countries.put("🇳🇵 ", "Nepal");
        countries.put("🇳🇷 ", "Nauru");
        countries.put("🇳🇺 ", "Niue");
        countries.put("🇳🇿 ", "New Zealand");
        countries.put("🇴🇲 ", "Oman");
        countries.put("🇵🇦 ", "Panama");
        countries.put("🇵🇪 ", "Peru");
        countries.put("🇵🇫 ", "French Polynesia");
        countries.put("🇵🇬 ", "Papua New Guinea");
        countries.put("🇵🇭 ", "Philippines");
        countries.put("🇵🇰 ", "Pakistan");
        countries.put("🇵🇱 ", "Poland");
        countries.put("🇵🇲 ", "St. Pierre & Miquelon");
        countries.put("🇵🇳 ", "Pitcairn Islands");
        countries.put("🇵🇷 ", "Puerto Rico");
        countries.put("🇵🇸 ", "Palestinian Territories");
        countries.put("🇵🇹 ", "Portugal");
        countries.put("🇵🇼 ", "Palau");
        countries.put("🇵🇾 ", "Paraguay");
        countries.put("🇶🇦 ", "Qatar");
        countries.put("🇷🇪 ", "Réunion");
        countries.put("🇷🇴 ", "Romania");
        countries.put("🇷🇸 ", "Serbia");
        countries.put("🇷🇺 ", "Russia");
        countries.put("🇷🇼 ", "Rwanda");
        countries.put("🇸🇦 ", "Saudi Arabia");
        countries.put("🇸🇧 ", "Solomon Islands");
        countries.put("🇸🇨 ", "Seychelles");
        countries.put("🇸🇩 ", "Sudan");
        countries.put("🇸🇪 ", "Sweden");
        countries.put("🇸🇬 ", "Singapore");
        countries.put("🇸🇭 ", "St. Helena");
        countries.put("🇸🇮 ", "Slovenia");
        countries.put("🇸🇯 ", "Svalbard & Jan Mayen");
        countries.put("🇸🇰 ", "Slovakia");
        countries.put("🇸🇱 ", "Sierra Leone");
        countries.put("🇸🇲 ", "San Marino");
        countries.put("🇸🇳 ", "Senegal");
        countries.put("🇸🇴 ", "Somalia");
        countries.put("🇸🇷 ", "Suriname");
        countries.put("🇸🇸 ", "South Sudan");
        countries.put("🇸🇹 ", "São Tomé & Príncipe");
        countries.put("🇸🇻 ", "El Salvador");
        countries.put("🇸🇽 ", "Sint Maarten");
        countries.put("🇸🇾 ", "Syria");
        countries.put("🇸🇿 ", "Eswatini");
        countries.put("🇹🇦 ", "Tristan Da Cunha");
        countries.put("🇹🇨 ", "Turks & Caicos Islands");
        countries.put("🇹🇩 ", "Chad");
        countries.put("🇹🇫 ", "French Southern Territorie");
        countries.put("🇹🇬 ", "Togo");
        countries.put("🇹🇭 ", "Thailand");
        countries.put("🇹🇯 ", "Tajikistan");
        countries.put("🇹🇰 ", "Tokelau");
        countries.put("🇹🇱 ", "Timor-Leste");
        countries.put("🇹🇲 ", "Turkmenistan");
        countries.put("🇹🇳 ", "Tunisia");
        countries.put("🇹🇴 ", "Tonga");
        countries.put("🇹🇷 ", "Turkey");
        countries.put("🇹🇹 ", "Trinidad & Tobago");
        countries.put("🇹🇻 ", "Tuvalu");
        countries.put("🇹🇼 ", "Taiwan");
        countries.put("🇹🇿 ", "Tanzania");
        countries.put("🇺🇦 ", "Ukraine");
        countries.put("🇺🇬 ", "Uganda");
        countries.put("🇺🇲 ", "U.S. Outlying Islands");
        countries.put("🇺🇳 ", "United Nations");
        countries.put("🇺🇸 ", "United States");
        countries.put("🇺🇾 ", "Uruguay");
        countries.put("🇺🇿 ", "Uzbekistan");
        countries.put("🇻🇦 ", "Vatican City");
        countries.put("🇻🇨 ", "St. Vincent & Grenadines");
        countries.put("🇻🇪 ", "Venezuela");
        countries.put("🇻🇬 ", "British Virgin Islands");
        countries.put("🇻🇮 ", "U.S. Virgin Islands");
        countries.put("🇻🇳 ", "Vietnam");
        countries.put("🇻🇺 ", "Vanuatu");
        countries.put("🇼🇫 ", "Wallis & Futuna");
        countries.put("🇼🇸 ", "Samoa");
        countries.put("🇽🇰 ", "Kosovo");
        countries.put("🇾🇪 ", "Yemen");
        countries.put("🇾🇹 ", "Mayotte");
        countries.put("🇿🇦 ", "South Africa");
        countries.put("🇿🇲 ", "Zambia");
        countries.put("🇿🇼 ", "Zimbabwe");
        countries.put("🏴󠁧󠁢󠁥󠁮󠁧󠁿 ", "England");
        countries.put("🏴󠁧󠁢󠁳󠁣󠁴󠁿 ", "Scotland");
        countries.put("🏴󠁧󠁢󠁷󠁬󠁳󠁿 ", "Wales");

        return getSingleKeyFromValue(countries, v) == null ? "" : getSingleKeyFromValue(countries, v);
    }
    public static <K, V> K getSingleKeyFromValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}