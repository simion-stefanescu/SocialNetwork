package com.example.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
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

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference UserProfileImageRef;

    final static int Galler_Pick = 1;

    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_fullname);
        CountryName = (EditText) findViewById(R.id.setup_countr_name);

        SaveInformationButton = (Button) findViewById(R.id.setup_save_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SaveAccountSetupInformation();


            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        //Picasso.get().load(image).into(ProfileImage);
                        Picasso.get().load(image).placeholder(R.drawable.profile_icon).resize(300, 300).centerCrop().into(ProfileImage);
                        // Glide.with(SetupActivity.this).load(image).into(ProfileImage);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ProfileImage.setOnClickListener(new View.OnClickListener() {
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
                StorageReference  filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            Toast.makeText(SetupActivity.this, "Profile Image storredsuccesfully to Firebase Storage", Toast.LENGTH_SHORT).show();


                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            userRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if( task.isSuccessful()){

                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                        startActivity(selfIntent);

                                        Toast.makeText(SetupActivity.this, "Profile Image storred to database succesfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    } else {

                                        String messge = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error Ocured: " + messge, Toast.LENGTH_SHORT).show();
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

    private void SaveAccountSetupInformation() {

        String username = UserName.getText().toString();
        String fullName = FullName.getText().toString();
        String countryName = CountryName.getText().toString();

        if(TextUtils.isEmpty(username)){

            Toast.makeText(this, "The username Field is empty", Toast.LENGTH_SHORT).show();

        } else if(TextUtils.isEmpty(fullName)){

            Toast.makeText(this, "The Fullname Field is empty", Toast.LENGTH_SHORT).show();

        } else  if(TextUtils.isEmpty(countryName)){

            Toast.makeText(this, "The Country Name Field is empty", Toast.LENGTH_SHORT).show();

        } else {


            loadingBar.setTitle("Saving info");
            loadingBar.setMessage("Please wait while we are saving your information");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullName);
            userMap.put("country", countryName);
            userMap.put("status", "Hey there, i am using poster social network");
            userMap.put("gender", "I am a human");
            userMap.put("dob", "1.1.1");
            userMap.put("RelationshipStatus", "None");
            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){


                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Youraccount is created succesfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured : " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }



                }
            });


        }

    }

    private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

}
