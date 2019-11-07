package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, usersRef, likesRef;
    private String CurrentUserId;

    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");


        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);
    
        DisplayMyAllPosts();    
    }

    private void DisplayMyAllPosts() {

        Query myPostsQuery = PostsRef.orderByChild("uid")
                .startAt(CurrentUserId).endAt(CurrentUserId + "\uf8ff");

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(

                Posts.class,
                R.layout.all_posts_layout,
                MyPostsViewHolder.class,
                myPostsQuery


        ) {
            @Override
            protected void populateViewHolder(MyPostsViewHolder postsViewHolder, Posts posts, int position) {

                final String PostKey = getRef(position).getKey();

                postsViewHolder.setFullname(posts.getFullname());
                postsViewHolder.setDate(posts.getDate());
                postsViewHolder.setTime(posts.getTime());
                postsViewHolder.setDescription(posts.getDescription());
                postsViewHolder.setProfileImage(posts.getProfileimage());
                postsViewHolder.setPostimage(posts.getPostimage());

                postsViewHolder.setLikesButtonStatus(PostKey);

                postsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                postsViewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentPostIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
                        commentPostIntent.putExtra("PostKey", PostKey);
                        startActivity(commentPostIntent);
                    }
                });

                postsViewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true)){

                                    if(dataSnapshot.child(PostKey).hasChild(CurrentUserId)){

                                        likesRef.child(PostKey).child(CurrentUserId).removeValue();
                                        LikeChecker = false;

                                    }else {

                                        likesRef.child(PostKey).child(CurrentUserId).setValue(true);
                                        LikeChecker = false;


                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };

        myPostsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserID;
        DatabaseReference likesRef;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            LikePostButton = (ImageButton) mView.findViewById(R.id.dislike_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes= (TextView) mView.findViewById(R.id.display_no_of_likes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        public void setLikesButtonStatus(final String PostKey){

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(PostKey).hasChild(currentUserID)){

                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));

                    }else {

                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes) + (" Likes")));

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


        public void setFullname(String fullname){

            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileImage(String profileImage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileImage).fit().placeholder(R.drawable.profile).into(image, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }

        public void setTime(String time){

            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("   " + time);
        }

        public void setDate(String date){

            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("   " + date);

        }

        public void setDescription(String description){


            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);

        }

        public void setPostimage(String postimage){

            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).placeholder(R.drawable.profile).into(PostImage);

        }

    }
}
