package com.example.letschat.Activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.AllFriendsAdapter;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddGroupMembersActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView allFriendsTextView;
    private String groupName, groupId, currenUserId;
    private String myGroupRole;
    private RecyclerView allFriendsRecyclerView;
    private ArrayList<Users> usersArrayList;
    private AllFriendsAdapter allFriendsAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);
        auth = FirebaseAuth.getInstance();
        currenUserId = auth.getCurrentUser().getUid();
        if (getIntent().getExtras().get("groupName") != null) {
            groupName = getIntent().getExtras().get("groupName").toString();
        }
        groupId = getIntent().getExtras().get("groupId").toString();
        database = FirebaseDatabase.getInstance();
        toolbar = (Toolbar) findViewById(R.id.new_group_toolbar);
        allFriendsTextView=(TextView)findViewById(R.id.all_users);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Members to " + groupName);
        allFriendsRecyclerView = (RecyclerView) findViewById(R.id.all_friends_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        allFriendsRecyclerView.setLayoutManager(layoutManager);
        loadGroupInfo();
    }

    private void getAllUsers() {
        usersArrayList = new ArrayList<>();
        allFriendsAdapter = new AllFriendsAdapter(AddGroupMembersActivity.this, usersArrayList, groupId, myGroupRole);
        allFriendsRecyclerView.setAdapter(allFriendsAdapter);
        database.getReference().child("Contacts").child(currenUserId).addValueEventListener(new ValueEventListener() {
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
                                if(!users.getUid().equals(currenUserId)){
                                    usersArrayList.add(users);
                                }
                            }
                            allFriendsAdapter.notifyDataSetChanged();
                            allFriendsTextView.setText("All Members (" + usersArrayList.size() + ")");
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups");
        reference.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        String groupName = dataSnapshot.child("name").getValue().toString();
                        String groupId = dataSnapshot.child("groupId").getValue().toString();
                        if (dataSnapshot.child("image").getValue() != null) {
                            String groupImage = dataSnapshot.child("image").getValue().toString();
                        }
                        String groupAdmin = dataSnapshot.child("groupAdmin").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String createdBy = dataSnapshot.child("createdBy").getValue().toString();
                        getSupportActionBar().setTitle("Add Members");
                        database.getReference().child("Groups").child(groupId).child("Members")
                                .child(currenUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    myGroupRole = snapshot.child("role").getValue().toString();
                                    getSupportActionBar().setTitle(groupName + " (" + myGroupRole + ")");
                                    getAllUsers();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
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