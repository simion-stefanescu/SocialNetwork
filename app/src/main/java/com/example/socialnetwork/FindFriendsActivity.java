package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton) findViewById(R.id.search_people_friends_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchBoxInput = SearchInputText.getText().toString();

                SearchPeopleAndFriends(searchBoxInput);

            }
        });
    }

    private void SearchPeopleAndFriends(String searchBoxInput) {

        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();

        Query searchPeopleAndFriendsQuery = allUsersDatabaseRef.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(

                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindFriendsViewHolder.class,
                        searchPeopleAndFriendsQuery

                ) {
                    @Override
                    protected void populateViewHolder(FindFriendsViewHolder findFriendsViewHolder, FindFriends model, int i) {

                        findFriendsViewHolder.setFullname(model.getFullname());
                        findFriendsViewHolder.setStatus(model.getStatus());
                        findFriendsViewHolder.setProfileImage(getApplicationContext(), model.getProfileimage());

                    }
                };

        SearchResultList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FindFriendsViewHolder(@NonNull android.view.View itemView) {
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

        public void setStatus(String status){

            TextView myStatus = (TextView) mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);

        }
    }

}
