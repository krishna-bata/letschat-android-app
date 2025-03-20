package com.example.letschat.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.letschat.Adapters.StatusAdapter;
import com.example.letschat.Adapters.TabsAccessorAdapter;
import com.example.letschat.Models.Status;
import com.example.letschat.Models.UserStatus;
import com.example.letschat.R;
import com.example.letschat.Services.NetworkchangeReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private Dialog createNewGroupDialog;
    private String currentUserId, currentUserName, currentUserProfileImage;
    private RecyclerView statusList;
    private ImageView addStatus;
    private StatusAdapter statusAdapter;
    private ArrayList<UserStatus> userStatuses;
    private Dialog loadingDialog, statusDialog;
    private TextView statusDialogTitle, statusDialogMessage;
    private LinearLayout addStatusContainer;
    private FirebaseDatabase database;
    private BroadcastReceiver broadcastReceiver;
    private static final int TIME_INTERVAL=2000;
    private long backPressed;
    private NetworkInfo wifi,mobile;
    private ConnectivityManager connectivityManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("LET'S CHAT");
        broadcastReceiver = new NetworkchangeReceiver();
        registerNetworkBroadCastReceiver();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        statusList = (RecyclerView) findViewById(R.id.status_list);
        addStatus = (ImageView) findViewById(R.id.add_status);
        addStatusContainer = (LinearLayout) findViewById(R.id.add_status_container);
        if (user != null) {
            currentUserId = auth.getCurrentUser().getUid();
            /////loading dialog
            loadingDialog = new Dialog(this);
            loadingDialog.setContentView(R.layout.loading_progress_dialog);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.loading_dialog_background));
            }
            loadingDialog.setCancelable(false);
            loadingDialog.show();
            ///////loading dialog
            /////status dialog
            statusDialog = new Dialog(this);
            statusDialog.setContentView(R.layout.custom_progress_dialog);
            statusDialog.setCancelable(false);
            statusDialogTitle = (TextView) statusDialog.findViewById(R.id.dialog_title);
            statusDialogMessage = (TextView) statusDialog.findViewById(R.id.dialog_message);
            statusDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            statusDialogTitle.setText("Uploading Status");
            statusDialogMessage.setText("Please Wait...");
            ///////status dialog
            ////Status
            userStatuses = new ArrayList<>();
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(RecyclerView.HORIZONTAL);
            statusList.setLayoutManager(layoutManager);
            statusAdapter = new StatusAdapter(this, userStatuses);
            statusList.setAdapter(statusAdapter);
            connectivityManager= (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifi=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            mobile=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//            if((wifi!=null && wifi.isConnected()) || mobile!=null && mobile.isConnected()){
                database.getReference().child("Status").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userStatuses.clear();
                            for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                                UserStatus status = new UserStatus();
                                status.setName(storySnapshot.child("name").getValue().toString());
                                if (storySnapshot.hasChild("profileImage")) {
                                    status.setProfileImage(storySnapshot.child("profileImage").getValue().toString());
                                }
                                status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                                ArrayList<Status> statuses = new ArrayList<>();
                                for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                                    Status sampleStatus = statusSnapshot.getValue(Status.class);
                                    statuses.add(sampleStatus);
                                }
                                status.setStatuses(statuses);
                                userStatuses.add(status);
                            }
                            addStatusContainer.setVisibility(View.VISIBLE);
                            statusAdapter.notifyDataSetChanged();
                            loadingDialog.dismiss();
                        }else{
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//            }else{
//                Toast.makeText(this, "No Internet Connection, Please turn on your Internet Connection ", Toast.LENGTH_LONG).show();
//                loadingDialog.dismiss();
//            }
            addStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 45);
                    CustomIntent.customType(MainActivity.this,"left-to-right");
                }
            });
            GetUserInfo();
        }
        ////Status
        Offline();
    }

    private void Offline() {
        database.getReference().child("Chats").keepSynced(true);
        database.getReference().child("Contacts").keepSynced(true);
        database.getReference().child("HasChats").keepSynced(true);
        database.getReference().child("Status").keepSynced(true);
        database.getReference().child("Users").keepSynced(true);
    }

    @Override
    public void onBackPressed(){
        if(backPressed+TIME_INTERVAL>System.currentTimeMillis()){
            super.onBackPressed();
            return;
        }else{
            Toast.makeText(this, "Press back again to exit app", Toast.LENGTH_SHORT).show();
        }
        backPressed=System.currentTimeMillis();
    }

    ////Status
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 45) {
            statusDialog.show();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final Date date = new Date();
            final StorageReference reference = storage.getReference().child("Status").child(date.getTime() + "");
            reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                UserStatus userStatus = new UserStatus();
                                userStatus.setName(currentUserName);
                                userStatus.setProfileImage(currentUserProfileImage);
                                userStatus.setLastUpdated(date.getTime());
                                HashMap<String, Object> statusObj = new HashMap<>();
                                statusObj.put("name", userStatus.getName());
                                statusObj.put("profileImage", userStatus.getProfileImage());
                                statusObj.put("lastUpdated", userStatus.getLastUpdated());
                                String imageUrl = uri.toString();
                                Status status = new Status(imageUrl, userStatus.getLastUpdated());
                                database.getReference()
                                        .child("Status")
                                        .child(currentUserId)
                                        .updateChildren(statusObj);
                                database.getReference()
                                        .child("Status")
                                        .child(currentUserId)
                                        .child("statuses")
                                        .push()
                                        .setValue(status);
                                Toast.makeText(getApplicationContext(), "Status Uploaded", Toast.LENGTH_SHORT).show();
                                statusDialog.dismiss();
                            }
                        });
                    }
                }
            });
        }
    }

    ////Status
    private void GetUserInfo() {
        database.getReference().child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("name")) {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
                if (snapshot.hasChild("image")) {
                    currentUserProfileImage = snapshot.child("image").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ////Status
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            auth.signOut();
            SendUserToLoginActivity();
        }
        if (currentUser != null) {
            UpdateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = auth.getCurrentUser();
        unRegisterNetworkBroadCastReceiver();
        if (currentUser != null) {
            UpdateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserId = auth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(("name")).exists()) {

                } else {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        CustomIntent.customType(MainActivity.this,"left-to-right");
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_find_friends_option) {
            SendUserToFindFriendsActivity();
        } else if (item.getItemId() == R.id.main_account_option) {
            SendUserToUserInfoActivity();
        } else if (item.getItemId() == R.id.main_logout_option) {
            UpdateUserStatus("offline");
            auth.signOut();
            SendUserToLoginActivity();
            Toast.makeText(this, "logout succefully..", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.main_create_group) {
            SendUserToNewGroupActivity();
        } else if (item.getItemId() == R.id.main_search_option) {
            SendUserToSearchOnlyFriendsActivity();
        }
        return true;
    }

    private void SendUserToSearchOnlyFriendsActivity() {
        Intent searchIntent = new Intent(MainActivity.this, SearchOnlyFriendsActivity.class);
        startActivity(searchIntent);
        CustomIntent.customType(MainActivity.this,"left-to-right");
    }

    private void SendUserToNewGroupActivity() {
        Intent newGroupIntent = new Intent(MainActivity.this, NewGroupActivity.class);
        newGroupIntent.putExtra("currentUserId", currentUserId);
        startActivity(newGroupIntent);
        CustomIntent.customType(MainActivity.this,"left-to-right");
    }

    private void SendUserToUserInfoActivity(){
        Intent userInfoIntent = new Intent(MainActivity.this, UserInfoActivity.class);
        userInfoIntent.putExtra("uid",currentUserId);
        startActivity(userInfoIntent);
        CustomIntent.customType(MainActivity.this,"left-to-right");
    }
    private void SendUserToSettingsActivity() {
        Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingIntent.putExtra("uid", currentUserId);
        startActivity(settingIntent);
        CustomIntent.customType(MainActivity.this,"left-to-right");
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
        CustomIntent.customType(MainActivity.this,"left-to-right");
    }

    public void UpdateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentData = new SimpleDateFormat("MMM dd");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentDate = currentData.format(calendar.getTime());
        saveCurrentTime = currentTime.format(calendar.getTime());
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);
        rootRef.child("Users").child(currentUserId).child("userState").updateChildren(onlineStateMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }
    protected void registerNetworkBroadCastReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unRegisterNetworkBroadCastReceiver() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}