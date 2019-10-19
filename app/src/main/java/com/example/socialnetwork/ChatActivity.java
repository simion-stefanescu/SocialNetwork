package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar ChatToolbar;
    private ImageButton SendMessageButton, SendImageFileButon;
    private EditText userMessageInput;
    private RecyclerView userMesaggesList;

    private String messageReceiverID, messageReceiverName;

    private TextView receiverName;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        InitializeFields();

        DisplayReceiverInfo();
    }

    private void DisplayReceiverInfo() {

        receiverName.setText(messageReceiverName);

        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {

        ChatToolbar = (Toolbar)findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolbar);

        RootRef = FirebaseDatabase.getInstance().getReference();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        receiverName = (TextView) findViewById(R.id.custom_profile_name);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SendImageFileButon = (ImageButton) findViewById(R.id.send_image_file_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);



    }
}
