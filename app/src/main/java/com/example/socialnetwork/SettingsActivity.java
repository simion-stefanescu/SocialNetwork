package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private Toolbar mToolbar;

    private EditText userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfileImage;

    private DatabaseReference SettingsuserRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;

    private String currentUserID;
    final static int Galler_Pick = 1;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        SettingsuserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        mToolbar =(Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.settings_username);
        userProfileName = (EditText) findViewById(R.id.settings_profile_full_name);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userCountry = (EditText) findViewById(R.id.settings_country);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userRelation = (EditText) findViewById(R.id.settings_relationships_status);
        userDOB = (EditText) findViewById(R.id.settings_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        UpdateAccountSettingsButton = (Button) findViewById(R.id.update_account_settings_button);
        loadingBar = new ProgressDialog(this);

        SettingsuserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String myprofileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("RelationshipStatus").getValue().toString();

                    Picasso.get().load(myprofileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText(myUsername);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationshipStatus);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidateAccountInfo();

            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Galler_Pick);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Galler_Pick && resultCode == RESULT_OK && data != null){

            Uri imageUri = data.getData();

            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).
                    setAspectRatio(1,1).start(this);


        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait while we are updating your profileImage");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();



                Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            Toast.makeText(SettingsActivity.this, "Profile Image storredsuccesfully to Firebase Storage", Toast.LENGTH_SHORT).show();


                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            SettingsuserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if( task.isSuccessful()){

                                        Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                        startActivity(selfIntent);

                                        Toast.makeText(SettingsActivity.this, "Profile Image storred to database succesfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    } else {

                                        String messge = task.getException().getMessage();
                                        Toast.makeText(SettingsActivity.this, "Error Ocured: " + messge, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    }

                                }
                            });
                        }

                    }
                });

            }else {

                Toast.makeText(this, "Error Occured: Image can't be cropped ,try again", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

        }

    }


    private void ValidateAccountInfo() {

        String username = userName.getText().toString();
        String userprofilename = userProfileName.getText().toString();
        String userstatus = userStatus.getText().toString();
        String userdob = userDOB.getText().toString();
        String usercountry = userCountry.getText().toString();
        String usergender = userGender.getText().toString();
        String userrelation = userRelation.getText().toString();

        if (TextUtils.isEmpty(username)) {

            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(userprofilename)){

            Toast.makeText(this, "Please write your pofile name", Toast.LENGTH_SHORT).show();
            
        }else if(TextUtils.isEmpty(userstatus)){

            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(userdob)){

            Toast.makeText(this, "Please write your date of birth", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(usercountry)){

            Toast.makeText(this, "Please write your country name", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(usergender)){

            Toast.makeText(this, "Please write your gender", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(userrelation)){

            Toast.makeText(this, "Please write your relationship status", Toast.LENGTH_SHORT).show();

        } else{

            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait while we are updating your profileImage");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username,userprofilename, userstatus, userdob ,usercountry ,usergender ,userrelation);

        }
        
        }

    private void UpdateAccountInfo(String username, String userprofilename, String userstatus, String userdob, String usercountry, String usergender, String userrelation) {
        HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("fullname", userprofilename);
        userMap.put("status", userstatus);
        userMap.put("dob", userdob);
        userMap.put("country", usercountry);
        userMap.put("gender", usergender);
        userMap.put("RelationshipStatus", userrelation);
        SettingsuserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful()){


                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                }else {

                    Toast.makeText(SettingsActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                }

            }
        });
    }


    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();


    }
}

