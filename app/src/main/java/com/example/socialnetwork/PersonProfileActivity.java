package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendRequestButton, DeclineFriendRequestButton;

    private DatabaseReference profileUserRef, UsersRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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

                    userName.setText("@" + myUsername);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("Date of Birth: " + myDOB);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelation.setText("Relationship Status: " + myRelationshipStatus);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {

        userName = (TextView) findViewById(R.id.person_username);
        userProfileName = (TextView) findViewById(R.id.person_profile_fullname);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelation = (TextView) findViewById(R.id.person_username);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        SendFriendRequestButton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton = (Button) findViewById(R.id.person_decline_friend_request_btn);

    }
}
