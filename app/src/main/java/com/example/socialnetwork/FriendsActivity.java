package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;

    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id); // F mare?????????
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    private void DisplayAllFriends() {

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        FriendsRef
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position) {

                friendsViewHolder.setDate(friends.getDate());

                final String userIDs = getRef(position).getKey();

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                            friendsViewHolder.setFullname(userName);
                            friendsViewHolder.setProfileImage(getApplicationContext(), profileImage);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }


        public void setProfileImage(Context applicationContext, String profileImage){

            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(myImage);

        }

        public void setFullname(String fullname){

            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_fullname);
            myName.setText(fullname);

        }

        public void setDate(String date){

            TextView friendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends since: " + date);

        }

    }
}
