package com.example.letschat.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.UsersAdapter;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchAllFriendsActivity extends AppCompatActivity {
    private SearchView searchView;
    private TextView userNotFoundTextView;
    private RecyclerView searchRecyclerView;
    private ArrayList<Users> usersArrayList;
    private UsersAdapter usersAdapter;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
        usersArrayList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersArrayList, this);
        searchRecyclerView.setAdapter(usersAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    SearchFriends(query);
                } else {
                    GetAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    SearchFriends(newText);
                } else {
                    GetAllUsers();
                }
                return false;
            }
        });
    }

    private void SearchFriends(String query) {
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                        if (!users.getUid().equals(currentUserId)) {
                            if (users.getName().toLowerCase().contains(query.toLowerCase())) {
                                usersArrayList.add(users);
                            }
                        }
                        usersAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetAllUsers() {
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    usersArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        if (!users.getUid().equals(currentUserId)) {
                            usersArrayList.add(users);
                        }
                    }
                    usersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}