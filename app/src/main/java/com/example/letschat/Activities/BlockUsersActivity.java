package com.example.letschat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.example.letschat.Adapters.BlockUserAdapter;
import com.example.letschat.Adapters.FriendsAdapter;
import com.example.letschat.Models.Friends;
import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BlockUsersActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView allBlockFriends;
    private RecyclerView blockUsersRecyclerView;
    private BlockUserAdapter blockUserAdapter;
    private ArrayList<Friends> friendsArrayList;
    private String currentUserId;
    private FirebaseDatabase database;
    private Dialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_users);
        /////loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_dialog_background));
        }
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        ///////loading dialog
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        allBlockFriends = (TextView) findViewById(R.id.all_block_users);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Block list");
        database = FirebaseDatabase.getInstance();
        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        blockUsersRecyclerView = findViewById(R.id.block_users_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        blockUsersRecyclerView.setLayoutManager(layoutManager);
        loadAllFriends();
    }
    private void loadAllFriends() {
        friendsArrayList = new ArrayList<>();
        blockUserAdapter = new BlockUserAdapter(BlockUsersActivity.this, friendsArrayList);
        blockUsersRecyclerView.setAdapter(blockUserAdapter);
        database.getReference().child("Users").child(currentUserId).child("BlockUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = dataSnapshot.child("uid").getValue().toString();
                    database.getReference().child("Users").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Friends friends = dataSnapshot.getValue(Friends.class);
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                friends.setState(state);
                                friends.setCurrentUserId(currentUserId);
                                friends.setReceiverId(uid);
                                friendsArrayList.add(friends);
                                loadingDialog.dismiss();
                            }
                            blockUserAdapter.notifyDataSetChanged();
                            allBlockFriends.setText("All Friends(" + friendsArrayList.size() + ")");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                loadingDialog.dismiss();
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