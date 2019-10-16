package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendRequestButton, DeclineFriendRequestButton;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        senderUserId = mAuth.getCurrentUser().getUid();
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

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


                    MaintananceButtons();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){

            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendRequestButton.setEnabled(false);

                    if(CURRENT_STATE.equals("not friends")){

                        SendFriendRequestToButton();

                    }if(CURRENT_STATE.equals("request_sent")){

                        CancelFriendRequest();

                    }if(CURRENT_STATE.equals("request_received")){

                        AcceptFriendRequest();

                    }
                }
            });

        }else {

            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);

        }

    }

    private void AcceptFriendRequest() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());


        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){


                                                FriendRequestRef.child(senderUserId).child(receiverUserId).
                                                        removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if(task.isSuccessful()){

                                                                                SendFriendRequestButton.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                SendFriendRequestButton.setText("Unfriend this person");

                                                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                DeclineFriendRequestButton.setEnabled(false);

                                                                            }

                                                                        }
                                                                    });

                                                        }

                                                    }
                                                });

                                            }

                                        }
                                    });

                        }

                    }
                });
    }

    private void CancelFriendRequest() {

        FriendRequestRef.child(senderUserId).child(receiverUserId).
                removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        SendFriendRequestButton.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        SendFriendRequestButton.setText("Send Friend Request");

                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        DeclineFriendRequestButton.setEnabled(false);

                                    }

                                }
                            });

                }

            }
        });

    }

    private void MaintananceButtons() {

        FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserId)){

                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){

                        CURRENT_STATE = "request_sent";
                        SendFriendRequestButton.setText("Cancel Friend Request");

                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                        DeclineFriendRequestButton.setEnabled(false);
                    }else if(request_type.equals("received")){

                        CURRENT_STATE ="request_received";
                        SendFriendRequestButton.setText("Accept Friend Request");

                        DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                        DeclineFriendRequestButton.setEnabled(true);

                    }
                }else {

                    FriendsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiverUserId)){

                                        CURRENT_STATE = "friends";
                                        SendFriendRequestButton.setText("Unfriend this person");

                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        DeclineFriendRequestButton.setEnabled(false);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendFriendRequestToButton() {

        FriendRequestRef.child(senderUserId).child(receiverUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        SendFriendRequestButton.setEnabled(true);
                                        CURRENT_STATE = "request_sent";
                                        SendFriendRequestButton.setText("Cancel Friend Request");

                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        DeclineFriendRequestButton.setEnabled(false);

                                    }

                                }
                            });

                }

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
        CURRENT_STATE = "not friends";

    }
}
