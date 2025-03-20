package com.example.letschat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.example.letschat.Adapters.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView allUsers;
    private RecyclerView findFriendsRecyclerLst;
    private DatabaseReference userRef;
    private ArrayList<Users> usersArrayList;
    private Dialog loadingDialog;
    private FirebaseAuth auth;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        auth=FirebaseAuth.getInstance();
        currentUserId=auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        findFriendsRecyclerLst = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerLst.setLayoutManager(new LinearLayoutManager(this));
        toolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        allUsers=(TextView)findViewById(R.id.all_users);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find New Friends");
        /////loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_dialog_background));
        }
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        ///////loading dialog
        /////
        usersArrayList = new ArrayList<>();
        UsersAdapter usersAdapter = new UsersAdapter(usersArrayList, this);
        findFriendsRecyclerLst.setAdapter(usersAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        findFriendsRecyclerLst.setLayoutManager(layoutManager);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    usersArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        if(!users.getUid().equals(currentUserId)) {
                            usersArrayList.add(users);
                        }
                        loadingDialog.dismiss();
                    }
                    usersAdapter.notifyDataSetChanged();
                    allUsers.setText("All Users("+usersArrayList.size()+")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /////
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.find_friends_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.friend_search){
            SendUserToSearchAllFriendsActivity();
        }
        return true;
    }

    private void SendUserToSearchAllFriendsActivity() {
        Intent searchIntent = new Intent(FindFriendsActivity.this, SearchAllFriendsActivity.class);
        startActivity(searchIntent);
        CustomIntent.customType(FindFriendsActivity.this,"left-to-right");
    }
}