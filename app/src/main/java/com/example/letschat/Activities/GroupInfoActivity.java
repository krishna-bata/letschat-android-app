package com.example.letschat.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.AllFriendsAdapter;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class GroupInfoActivity extends AppCompatActivity {
    private String groupId, currentUserId;
    private String myGroupRole = "";
    private Toolbar toolbar;
    private RecyclerView allMembersRecyclerView;
    private CircleImageView groupImage;
    private TextView grouName, memberCount, createdBy;
    private ImageView editGroup, addMembers, leaveGroup, deleteGroup;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ArrayList<Users> usersArrayList;
    private AllFriendsAdapter allFriendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        groupId = getIntent().getExtras().get("groupId").toString();
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.colorAccent));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow_white);
        allMembersRecyclerView = findViewById(R.id.all_members_recyclerview);
        groupImage = (CircleImageView) findViewById(R.id.new_group_image);
        grouName = (TextView) findViewById(R.id.group_name);
        memberCount = (TextView) findViewById(R.id.member_count);
        createdBy = (TextView) findViewById(R.id.created_by);
        editGroup = (ImageView) findViewById(R.id.edit_group);
        addMembers = (ImageView) findViewById(R.id.add_member);
        leaveGroup = (ImageView) findViewById(R.id.leave_group);
        deleteGroup = (ImageView) findViewById(R.id.delete_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        allMembersRecyclerView.setLayoutManager(layoutManager);
        loadGroupInfo();
        loadMyGroupRole();
        addMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addMemberIntent = new Intent(GroupInfoActivity.this, AddGroupMembersActivity.class);
                addMemberIntent.putExtra("groupId", groupId);
                startActivity(addMemberIntent);
                CustomIntent.customType(GroupInfoActivity.this, "left-to-right");
            }
        });
        leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(GroupInfoActivity.this);
                dialog.setContentView(R.layout.custom_delete_msg_dialog);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.show();
                TextView title = dialog.findViewById(R.id.custom_delete_title);
                TextView message = dialog.findViewById(R.id.custom_delete_message);
                Button yesBtn = dialog.findViewById(R.id.yes_btn);
                Button noBtn = dialog.findViewById(R.id.no_btn);
                title.setText("Leave Group ?");
                message.setText("Are you sure you want to Leave this group permenantly ?");
                yesBtn.setText("LEAVE");
                noBtn.setText("CANCEL");
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LeaveGroup();
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
        deleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(GroupInfoActivity.this);
                dialog.setContentView(R.layout.custom_delete_msg_dialog);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.show();
                TextView title = dialog.findViewById(R.id.custom_delete_title);
                TextView message = dialog.findViewById(R.id.custom_delete_message);
                Button yesBtn = dialog.findViewById(R.id.yes_btn);
                Button noBtn = dialog.findViewById(R.id.no_btn);
                title.setText("Delete Group ?");
                message.setText("Are you sure you want to delete this group permenantly ?");
                yesBtn.setText("DELETE");
                noBtn.setText("NO");
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteGroup();
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
        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editGroupIntent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                editGroupIntent.putExtra("groupId", groupId);
                startActivity(editGroupIntent);
                CustomIntent.customType(GroupInfoActivity.this, "left-to-right");
            }
        });
    }

    private void LeaveGroup() {
        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId)
                .child("Members").child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(GroupInfoActivity.this, "Group left successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(GroupInfoActivity.this, MainActivity.class));
                    CustomIntent.customType(GroupInfoActivity.this, "left-to-right");
                    finish();
                } else {
                    Toast.makeText(GroupInfoActivity.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteGroup() {
        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(GroupInfoActivity.this, "Group Delete successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(GroupInfoActivity.this, MainActivity.class));
                    CustomIntent.customType(GroupInfoActivity.this, "left-to-right");
                    finish();
                } else {
                    Toast.makeText(GroupInfoActivity.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMyGroupRole() {
        database.getReference().child("Groups").child(groupId).child("Members").orderByChild("uid").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myGroupRole = dataSnapshot.child("role").getValue().toString();
                    getSupportActionBar().setSubtitle(myGroupRole);
                    if (myGroupRole.equals("member")) {
                        leaveGroup.setVisibility(View.VISIBLE);
                        editGroup.setVisibility(View.GONE);
                        addMembers.setVisibility(View.GONE);
                        deleteGroup.setVisibility(View.GONE);
                    } else if (myGroupRole.equals("admin")) {
                        leaveGroup.setVisibility(View.VISIBLE);
                        editGroup.setVisibility(View.GONE);
                        addMembers.setVisibility(View.VISIBLE);
                        deleteGroup.setVisibility(View.GONE);
                    } else if (myGroupRole.equals("creator")) {
                        editGroup.setVisibility(View.VISIBLE);
                        addMembers.setVisibility(View.VISIBLE);
                        deleteGroup.setVisibility(View.VISIBLE);
                        leaveGroup.setVisibility(View.GONE);
                    }
                }
                loadGroupMembers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupMembers() {
        usersArrayList = new ArrayList<>();
        allFriendsAdapter = new AllFriendsAdapter(GroupInfoActivity.this, usersArrayList, groupId, myGroupRole);
        allMembersRecyclerView.setAdapter(allFriendsAdapter);
        database.getReference().child("Groups").child(groupId).child("Members").addValueEventListener(new ValueEventListener() {
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
                                usersArrayList.add(users);
                            }
                            allFriendsAdapter.notifyDataSetChanged();
                            memberCount.setText("All Members (" + usersArrayList.size() + ")");
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

    private void loadGroupInfo() {
        database.getReference().child("Groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    String groupId = snapshot.child("groupId").getValue().toString();
                    if (snapshot.child("image").exists()) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(groupImage);
                        groupImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Dialog dialog = new Dialog(GroupInfoActivity.this);
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
                        Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(groupImage);
                    }
                    String groupAdmin = snapshot.child("groupAdmin").getValue().toString();
                    String date = snapshot.child("date").getValue().toString();
                    String time = snapshot.child("time").getValue().toString();
                    String created = snapshot.child("createdBy").getValue().toString();
                    getSupportActionBar().setTitle(name);
                    grouName.setText(name);
                    createdBy.setText("created by " + groupAdmin + " on " + date + " " + time);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}