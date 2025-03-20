package com.example.letschat.Activities;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.GroupChatAdapter;
import com.example.letschat.Models.GroupMessagesModel;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import maes.tech.intentanim.CustomIntent;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView groupName;
    private ImageView backArrowBtn, sendImage, sendPdf, sendDocx;
    private CircleImageView profileGroupImage;
    private ImageButton sendMsgBtn, sendFilesBtn,emojiBtn;
    private RecyclerView groupChatRecyclerView;
    private EmojiconEditText messageInput;
    private View view;
    private EmojIconActions emojIconActions;
    private String currentGroupName, currentGroupId, currentGroupImage;
    private String myGroupRole = "";
    private FirebaseAuth auth;
    private String currentUserId, currentUserName, saveCurrentDate, saveCurrentTime;
    private DatabaseReference userRef, groupNameRef;
    private FirebaseStorage storage;
    private Dialog sendingFileDialog;
    private TextView sendingFileDialogTitle, sendingFileDialogMessage;
    private String checker = "";
    private RelativeLayout attachmentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //Dialog
        sendingFileDialog = new Dialog(GroupChatActivity.this);
        sendingFileDialog.setContentView(R.layout.custom_progress_dialog);
        sendingFileDialog.setCancelable(false);
        sendingFileDialogTitle = (TextView) sendingFileDialog.findViewById(R.id.dialog_title);
        sendingFileDialogMessage = (TextView) sendingFileDialog.findViewById(R.id.dialog_message);
        sendingFileDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //Dialog
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentGroupId = getIntent().getExtras().get("groupId").toString();
        storage = FirebaseStorage.getInstance();
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Chats");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentDate = currentDate.format(calendar.getTime());
        saveCurrentTime = currentTime.format(calendar.getTime());
        backArrowBtn = (ImageView) findViewById(R.id.back_arrow);
        sendImage = (ImageView) findViewById(R.id.send_image);
        sendPdf = (ImageView) findViewById(R.id.send_pdf);
        sendDocx = (ImageView) findViewById(R.id.send_docx);
        attachmentLayout = findViewById(R.id.attachment_layout);
        groupName = (TextView) findViewById(R.id.profile_group_name);
        profileGroupImage = (CircleImageView) findViewById(R.id.profile_group_image);
        sendMsgBtn = (ImageButton) findViewById(R.id.send_msg_btn);
        sendFilesBtn = (ImageButton) findViewById(R.id.send_files_btn);
        messageInput = (EmojiconEditText) findViewById(R.id.input_msg);
        emojiBtn = (ImageButton) findViewById(R.id.emoji_btn);
        view = (View) findViewById(R.id.root_layout);
        if (currentGroupImage != null) {
            Picasso.get().load(currentGroupImage).placeholder(R.drawable.profile).into(profileGroupImage);
        }
        emojIconActions = new EmojIconActions(this, view, messageInput, emojiBtn);
        emojIconActions.ShowEmojIcon();
        backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "image";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select Images"), 50);
                CustomIntent.customType(GroupChatActivity.this,"left-to-right");
                HideAttachmentLayout();
            }
        });
        sendPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "pdf";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent.createChooser(intent, "Select PDF File"), 50);
                CustomIntent.customType(GroupChatActivity.this,"left-to-right");
                HideAttachmentLayout();
            }
        });
        sendDocx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "docx";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/msword");
                startActivityForResult(intent.createChooser(intent, "Select DOCX File"), 50);
                CustomIntent.customType(GroupChatActivity.this,"left-to-right");
                HideAttachmentLayout();
            }
        });
        groupChatRecyclerView = (RecyclerView) findViewById(R.id.groupChatRecyclerView);
        final ArrayList<GroupMessagesModel> groupMessagesModels = new ArrayList<>();
        final GroupChatAdapter groupChatAdapter = new GroupChatAdapter(groupMessagesModels, this);
        groupChatRecyclerView.setAdapter(groupChatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        groupChatRecyclerView.setLayoutManager(layoutManager);
        groupNameRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupMessagesModels.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    GroupMessagesModel model = snapshot1.getValue(GroupMessagesModel.class);
                    groupMessagesModels.add(model);
                }
                groupChatAdapter.notifyDataSetChanged();
                groupChatRecyclerView.smoothScrollToPosition(groupChatRecyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        sendFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachmentLayout.getVisibility() == View.GONE) {
                    ShowAttachmentLayout();
                } else {
                    HideAttachmentLayout();
                }
            }
        });
        GetUserInfo();
        loadMyGroupRole();
        LoadGroupInfo();
    }

    private void loadMyGroupRole() {
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(currentGroupId).child("Members")
                .orderByChild("uid").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            myGroupRole = dataSnapshot.child("role").getValue().toString();
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (checker.equals("pdf")) {
                Uri selectedFile = data.getData();
                Calendar calendar = Calendar.getInstance();
                final StorageReference reference = storage.getReference().child("Group Chats PDF Files").child(calendar.getTimeInMillis() + " ");
                sendingFileDialogTitle.setText("Sending PDF File");
                sendingFileDialogMessage.setText("Please Wait...");
                sendingFileDialog.show();
                reference.putFile(selectedFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    sendingFileDialog.dismiss();
                                    String filepath = uri.toString();
                                    String message = "pdf file";
                                    String type = "pdf";
                                    DatabaseReference userMessageKeyRef = groupNameRef.push();
                                    final String messagePushId = userMessageKeyRef.getKey();
                                    final GroupMessagesModel model = new GroupMessagesModel(message, currentUserId, saveCurrentTime, saveCurrentDate, messagePushId, type);
                                    model.setPdf(filepath);
                                    model.setSenderName(currentUserName);
                                    model.setGroupName(currentGroupName);
                                    model.setGroupId(currentGroupId);
                                    messageInput.setText("");
                                    FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Members").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                String uid = dataSnapshot.child("uid").getValue().toString();
                                                groupNameRef.child(uid).child(messagePushId)
                                                        .setValue(model)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            } else if (checker.equals("docx")) {
                Uri selectedFile = data.getData();
                Calendar calendar = Calendar.getInstance();
                final StorageReference reference = storage.getReference().child("Group Chats DOCX Files").child(calendar.getTimeInMillis() + " ");
                sendingFileDialogTitle.setText("Sending DOCX File");
                sendingFileDialogMessage.setText("Please Wait...");
                sendingFileDialog.show();
                reference.putFile(selectedFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    sendingFileDialog.dismiss();
                                    String filepath = uri.toString();
                                    String message = "docx file";
                                    String type = "docx";
                                    DatabaseReference userMessageKeyRef = groupNameRef.push();
                                    final String messagePushId = userMessageKeyRef.getKey();
                                    final GroupMessagesModel model = new GroupMessagesModel(message, currentUserId, saveCurrentTime, saveCurrentDate, messagePushId, type);
                                    model.setDocx(filepath);
                                    model.setSenderName(currentUserName);
                                    model.setGroupName(currentGroupName);
                                    model.setGroupId(currentGroupId);
                                    messageInput.setText("");
                                    FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Members").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                String uid = dataSnapshot.child("uid").getValue().toString();
                                                groupNameRef.child(uid).child(messagePushId)
                                                        .setValue(model)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            } else if (checker.equals("image")) {
                Uri selectedImage = data.getData();
                Calendar calendar = Calendar.getInstance();
                final StorageReference reference = storage.getReference().child("Group Chats Images").child(calendar.getTimeInMillis() + " ");
                sendingFileDialogTitle.setText("Sending Image");
                sendingFileDialogMessage.setText("Please Wait...");
                sendingFileDialog.show();
                reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    sendingFileDialog.dismiss();
                                    String filepath = uri.toString();
                                    String message = "photo";
                                    String type = "image";
                                    DatabaseReference userMessageKeyRef = groupNameRef.push();
                                    final String messagePushId = userMessageKeyRef.getKey();
                                    final GroupMessagesModel model = new GroupMessagesModel(message, currentUserId, saveCurrentTime, saveCurrentDate, messagePushId, type);
                                    model.setImage(filepath);
                                    model.setSenderName(currentUserName);
                                    model.setGroupName(currentGroupName);
                                    model.setGroupId(currentGroupId);
                                    messageInput.setText("");
                                    FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Members").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                String uid = dataSnapshot.child("uid").getValue().toString();
                                                groupNameRef.child(uid).child(messagePushId)
                                                        .setValue(model)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            } else {
                sendingFileDialog.dismiss();
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void GetUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ShowAttachmentLayout() {
        float radius = Math.max(attachmentLayout.getWidth(), attachmentLayout.getHeight());
        Animator animator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(attachmentLayout, attachmentLayout.getRight(), attachmentLayout.getTop(), 0, radius * 2);
        }
        animator.setDuration(500);
        attachmentLayout.setVisibility(View.VISIBLE);
        animator.start();
    }
    private void SendMessage(){
        String message = messageInput.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            DatabaseReference userMessageKeyRef = groupNameRef.push();
            final String messagePushId = userMessageKeyRef.getKey();
            String type = "text";
            final GroupMessagesModel model = new GroupMessagesModel(message, currentUserId, saveCurrentTime, saveCurrentDate, messagePushId, type);
            model.setSenderName(currentUserName);
            model.setGroupName(currentGroupName);
            model.setGroupId(currentGroupId);
            messageInput.setText("");
            FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String uid = dataSnapshot.child("uid").getValue().toString();
                        groupNameRef.child(uid).child(messagePushId)
                                .setValue(model)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(GroupChatActivity.this, "Please type message", Toast.LENGTH_SHORT).show();
        }
    }

    private void HideAttachmentLayout() {
        float radius = Math.max(attachmentLayout.getWidth(), attachmentLayout.getHeight());
        Animator animator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(attachmentLayout, attachmentLayout.getRight(), attachmentLayout.getTop(), radius * 2, 0);
        }
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                attachmentLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }
    private void LoadGroupInfo(){
        FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    currentGroupName= snapshot.child("name").getValue().toString();
//                    if(snapshot.child("image").exists()) {
//                        currentGroupImage = snapshot.child("image").getValue().toString();
//                    }
                    String groupId = snapshot.child("groupId").getValue().toString();
                    if(snapshot.child("image").exists()) {
                       currentGroupImage = snapshot.child("image").getValue().toString();
                        Picasso.get().load(currentGroupImage).placeholder(R.drawable.profile).into(profileGroupImage);
                        profileGroupImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Dialog dialog = new Dialog(GroupChatActivity.this);
                                dialog.setContentView(R.layout.small_image_viewer_layout);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.layout_background));
                                }
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
                                dialog.show();
                                PhotoView smallImageView =(PhotoView)dialog.findViewById(R.id.small_image_viwer);
                                Picasso.get().load(currentGroupImage).placeholder(R.drawable.profile).into(smallImageView);
                            }
                        });
                    }else{
                        Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(profileGroupImage);
                    }
                    groupName.setText(currentGroupName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void ClearAllChat(){
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.custom_delete_msg_dialog);
        dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
        dialog.show();
        TextView title = dialog.findViewById(R.id.custom_delete_title);
        TextView message = dialog.findViewById(R.id.custom_delete_message);
        Button yesBtn = dialog.findViewById(R.id.yes_btn);
        Button noBtn = dialog.findViewById(R.id.no_btn);
        title.setText("Clear All Chat?");
        message.setText("Are you want to sure clear all chats?");
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Chats").child(currentUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupChatActivity.this, "Chat Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")) {
            menu.findItem(R.id.add_member).setVisible(true);
        } else {
            menu.findItem(R.id.add_member).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_member) {
            Intent addMemberIntent = new Intent(GroupChatActivity.this, AddGroupMembersActivity.class);
            addMemberIntent.putExtra("groupId", currentGroupId);
            addMemberIntent.putExtra("groupName", currentGroupName);
            startActivity(addMemberIntent);
            CustomIntent.customType(GroupChatActivity.this,"left-to-right");
        } else if (item.getItemId() == R.id.group_info) {
            Intent infoIntent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
            infoIntent.putExtra("groupId", currentGroupId);
            startActivity(infoIntent);
            CustomIntent.customType(GroupChatActivity.this,"left-to-right");
        }else if(item.getItemId()==R.id.clear_chat){
            ClearAllChat();
        }
        return super.onOptionsItemSelected(item);
    }
}