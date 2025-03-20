package com.example.letschat.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.FriendsAdapter;
import com.example.letschat.Models.Friends;
import com.example.letschat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class FriendsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView allFriends;
    private RecyclerView friendsRecyclerView;
    private ArrayList<Friends> friendsArrayList;
    private FriendsAdapter friendsAdapter;
    private String currentUserId;
    private FirebaseDatabase database;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        /////loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_dialog_background));
        }
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        ///////loading dialog
        toolbar = (Toolbar) findViewById(R.id.group_chat_toolbar);
        allFriends = (TextView) findViewById(R.id.all_users);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Friends");
        database = FirebaseDatabase.getInstance();
        currentUserId = getIntent().getStringExtra("currentUserId");
        friendsRecyclerView = findViewById(R.id.friends_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        friendsRecyclerView.setLayoutManager(layoutManager);
        loadAllFriends();
    }

    private void loadAllFriends() {
        friendsArrayList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(FriendsActivity.this, friendsArrayList);
        friendsRecyclerView.setAdapter(friendsAdapter);
        database.getReference().child("Contacts").child(currentUserId).addValueEventListener(new ValueEventListener() {
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
                            friendsAdapter.notifyDataSetChanged();
                            allFriends.setText("All Friends(" + friendsArrayList.size() + ")");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.find_friends_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.friend_search){
            SendUserToSearchOnlyFriendsActivity();
        }
        return true;
    }
    private void SendUserToSearchOnlyFriendsActivity() {
        Intent searchIntent = new Intent(FriendsActivity.this, SearchOnlyFriendsActivity.class);
        startActivity(searchIntent);
        CustomIntent.customType(FriendsActivity.this,"left-to-right");
    }
}