package com.example.letschat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.letschat.Adapters.FriendsAdapter;
import com.example.letschat.Adapters.UsersAdapter;
import com.example.letschat.Models.Friends;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchOnlyFriendsActivity extends AppCompatActivity {
    private SearchView searchView;
    private TextView userNotFoundTextView;
    private RecyclerView searchRecyclerView;
    private ArrayList<Friends> friendsArrayList;
    private FriendsAdapter friendsAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_only_friends);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        searchView = findViewById(R.id.search_view);
        searchRecyclerView = findViewById(R.id.search_recycler_view);
        userNotFoundTextView = findViewById(R.id.user_not_found_text_view);
        searchRecyclerView.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        searchRecyclerView.setLayoutManager(layoutManager);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    SearchFriends(query);
                } else {
                    loadAllFriends();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    SearchFriends(newText);
                } else {
                    loadAllFriends();
                }
                return false;
            }
        });
    }
    private void loadAllFriends() {
        friendsArrayList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(SearchOnlyFriendsActivity.this, friendsArrayList);
        searchRecyclerView.setAdapter(friendsAdapter);
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
                            }
                            friendsAdapter.notifyDataSetChanged();
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
    private void SearchFriends(String query) {
        friendsArrayList = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(SearchOnlyFriendsActivity.this, friendsArrayList);
        searchRecyclerView.setAdapter(friendsAdapter);
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
                                if (friends.getName().toLowerCase().contains(query.toLowerCase())) {
                                    friendsArrayList.add(friends);
                                }
                            }
                            friendsAdapter.notifyDataSetChanged();
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
}