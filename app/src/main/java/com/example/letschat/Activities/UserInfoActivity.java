package com.example.letschat.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.UsersAdapter;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class UserInfoActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView blockUserTextView;
    private String currentUserId, userId;
    private FirebaseDatabase database;
    private CircleImageView profileImage;
    private ImageView editUserBtn, blockUserBtn, deleteUserBtn;
    private TextView userName, userStatus, allFriends;
    private RecyclerView friendsRecyclerView;
    private ArrayList<Users> usersArrayList;
    private UsersAdapter usersAdapter;
    private FirebaseAuth auth;
    private LinearLayout editBtnContainer;
    private LinearLayout blockUsercontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        toolbar = findViewById(R.id.toolbar);
        profileImage = (CircleImageView) findViewById(R.id.user_image);
        editUserBtn = (ImageView) findViewById(R.id.edit_user);
        blockUserBtn = (ImageView) findViewById(R.id.block_user);
        deleteUserBtn = (ImageView) findViewById(R.id.delete_user);
        userName = (TextView) findViewById(R.id.user_name);
        userStatus = (TextView) findViewById(R.id.user_status);
        allFriends = (TextView) findViewById(R.id.member_count);
        editBtnContainer = (LinearLayout) findViewById(R.id.edit_btn_container);
        blockUserTextView = (TextView) findViewById(R.id.block_user_text_view);
        friendsRecyclerView = (RecyclerView) findViewById(R.id.all_friends_recyclerview);
        blockUsercontainer = (LinearLayout) findViewById(R.id.block_user_container);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow_white);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        getSupportActionBar().setTitle("User Info");
        currentUserId = getIntent().getExtras().get("uid").toString();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        friendsRecyclerView.setLayoutManager(layoutManager);
        getUserInfo(currentUserId);
        getAllUsers(currentUserId);
        editUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editGroupIntent = new Intent(UserInfoActivity.this, SettingsActivity.class);
                editGroupIntent.putExtra("uid", currentUserId);
                startActivity(editGroupIntent);
                CustomIntent.customType(UserInfoActivity.this, "left-to-right");
            }
        });
        blockUsercontainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog=new Dialog(UserInfoActivity.this);
                dialog.setContentView(R.layout.custom_delete_msg_dialog);
                dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
                dialog.show();
                TextView title = dialog.findViewById(R.id.custom_delete_title);
                TextView message = dialog.findViewById(R.id.custom_delete_message);
                Button yesBtn = dialog.findViewById(R.id.yes_btn);
                Button noBtn = dialog.findViewById(R.id.no_btn);
                if (blockUserTextView.getText().equals("Unblock")) {
                    title.setText("Unblock User?");
                    message.setText("Are you want to sure unblock this user?");
                    yesBtn.setText("UNBLOCK");
                }else{
                    title.setText("Block User?");
                    message.setText("Are you want to sure block this user?");
                    yesBtn.setText("BLOCK");
                }
                noBtn.setText("NO");
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (blockUserTextView.getText().equals("Unblock")) {
                            UnBlockedUser(currentUserId,blockUserTextView);
                        } else {
                            BlockUser(currentUserId,blockUserTextView);
                        }
                        dialog.dismiss();
                    }
                });
                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        CheckIsBlockOrUnblock(currentUserId);
    }
    private void SendUserToBlockListActivity(){
        Intent intent=new Intent(UserInfoActivity.this,BlockUsersActivity.class);
        startActivity(intent);
        CustomIntent.customType(UserInfoActivity.this,"left-to-right");
    }
    private void getAllUsers(String currentUserId) {
        usersArrayList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersArrayList, UserInfoActivity.this);
        friendsRecyclerView.setAdapter(usersAdapter);
        database.getReference().child("Contacts").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = dataSnapshot.child("uid").getValue().toString();
                    database.getReference().child("Users").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Users users = dataSnapshot.getValue(Users.class);
                                if (!users.getUid().equals(currentUserId)) {
                                    usersArrayList.add(users);
                                }
                            }
                            usersAdapter.notifyDataSetChanged();
                            allFriends.setText("Friends (" + usersArrayList.size() + ")");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getUserInfo(String currentUserId) {
        database.getReference().child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();
                    if (snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                        profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Dialog dialog = new Dialog(UserInfoActivity.this);
                                dialog.setContentView(R.layout.small_image_viewer_layout);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.layout_background));
                                }
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                dialog.show();
                                PhotoView smallImageView = (PhotoView) dialog.findViewById(R.id.small_image_viwer);
                                Picasso.get().load(image).placeholder(R.drawable.profile).into(smallImageView);
                            }
                        });
                    } else {
                        Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(profileImage);
                        profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Dialog dialog = new Dialog(UserInfoActivity.this);
                                dialog.setContentView(R.layout.small_image_viewer_layout);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.layout_background));
                                }
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                dialog.show();
                                PhotoView smallImageView = (PhotoView) dialog.findViewById(R.id.small_image_viwer);
                                Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(smallImageView);
                            }
                        });
                    }
                    String uid = snapshot.child("uid").getValue().toString();
                    userName.setText(name);
                    userStatus.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getUserRole(String currentUserId, String userId, MenuItem item) {
        if (currentUserId.equals(userId)) {
            item.setVisible(true);
            editUserBtn.setVisibility(View.VISIBLE);
            deleteUserBtn.setVisibility(View.VISIBLE);
            editBtnContainer.setVisibility(View.VISIBLE);
            blockUsercontainer.setVisibility(View.GONE);
        } else if (!currentUserId.equals(userId)) {
            item.setVisible(false);
            editBtnContainer.setVisibility(View.GONE);
            blockUsercontainer.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_info_menu, menu);
        MenuItem item = menu.findItem(R.id.block_user_list);
        getUserRole(currentUserId, userId, item);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.block_user_list) {
            SendUserToBlockListActivity();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void BlockUser(String receiverId, TextView blockUserTextView) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("block", "yes");
        map.put("uid",receiverId);
        database.getReference().child("Users").child(userId).child("BlockUsers").child(receiverId)
                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UserInfoActivity.this, "Block Successfully", Toast.LENGTH_SHORT).show();
                    blockUserTextView.setText("Unblock");
                } else {
                    Toast.makeText(UserInfoActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UnBlockedUser(String receiverId,TextView blockUserTextView) {
        database.getReference().child("Users").child(userId)
                .child("BlockUsers")
                .child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserInfoActivity.this, "Unblock Successfully", Toast.LENGTH_SHORT).show();
                            blockUserTextView.setText("Block");
                        } else {
                            Toast.makeText(UserInfoActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void CheckIsBlockOrUnblock(String receiverId) {
        database.getReference().child("Users").child(userId)
                .child("BlockUsers")
                .child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if ((snapshot.child("block").getValue().toString()).equals("yes")) {
                                blockUserTextView.setText("Unblock");
                            } else {
                                blockUserTextView.setText("Block");
                            }
                        } else {
                            blockUserTextView.setText("Block");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}