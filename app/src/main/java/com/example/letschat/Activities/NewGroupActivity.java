package com.example.letschat.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.letschat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class NewGroupActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CircleImageView groupImage;
    private Button groupSubmit;
    private EditText groupNameInput;
    private String groupName;
    private StorageReference groupProfileImagesRef;
    private UploadTask uploadTask;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String groupProfileImageUrl, currentUserId, currentUserName;
    private Dialog dialog;
    private TextView dialogTitle, dialogMessage;
    private String saveCurrentTime, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        currentUserId = getIntent().getStringExtra("currentUserId");
        toolbar = (Toolbar) findViewById(R.id.new_group_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow_white);
        getSupportActionBar().setTitle("New Group");
        groupProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Groups Profile Images");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentDate = currentDate.format(calendar.getTime());
        saveCurrentTime = currentTime.format(calendar.getTime());
        /////status dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.setCancelable(false);
        dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
        dialogMessage = (TextView) dialog.findViewById(R.id.dialog_message);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///////status dialog
        database = FirebaseDatabase.getInstance();
        database.getReference().child("Users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        groupImage = (CircleImageView) findViewById(R.id.new_group_image);
        groupNameInput = (EditText) findViewById(R.id.new_group_name);
        groupSubmit = (Button) findViewById(R.id.group_submit);
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        groupSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupName = groupNameInput.getText().toString();
                if (!TextUtils.isEmpty(groupName)) {
                    dialogTitle.setText("Creating New Group");
                    dialogMessage.setText("Please Wait...");
                    dialog.show();
                    DatabaseReference groupRef = database.getReference().child("Groups").push();
                    String groupId = groupRef.getKey();
                    Map<String, String> groupInfo = new HashMap<>();
                    if (groupProfileImageUrl != null) {
                        groupInfo.put("image", groupProfileImageUrl);
                    }
                    groupInfo.put("name", groupName);
                    groupInfo.put("createdBy", currentUserId);
                    groupInfo.put("groupAdmin", currentUserName);
                    groupInfo.put("groupId", groupId);
                    groupInfo.put("date", saveCurrentDate);
                    groupInfo.put("time", saveCurrentTime);
                    groupRef.setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Map<String, String> members = new HashMap<>();
                                members.put("role", "creator");
                                members.put("uid", currentUserId);
                                groupRef.child("Members").child(currentUserId)
                                        .setValue(members).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            dialog.dismiss();
                                            SendUserToMainActivity();
                                            Toast.makeText(NewGroupActivity.this, "Group Created Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            dialog.dismiss();
                                            String error = task.getException().getMessage();
                                            Toast.makeText(NewGroupActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                        dialog.dismiss();
                                    }
                                });
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(NewGroupActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(NewGroupActivity.this, "Please Enter Group Name", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(NewGroupActivity.this, MainActivity.class);
        startActivity(mainIntent);
        CustomIntent.customType(NewGroupActivity.this,"left-to-right");
        finish();
    }
    private void SelectImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(NewGroupActivity.this);
        builder.setTitle("Select Group Profile Image");
        String[] options = new String[]{"Camera", "Gallery","Cancel"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    GotoCamera();
                } else if(which==1){
                    GotoGallery();
                }else if(which==3){
                    dialog.dismiss();
                }
            }
        }).show();
    }

    private void GotoCamera() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,24);
        CustomIntent.customType(NewGroupActivity.this,"left-to-right");
    }

    private void GotoGallery() {
//        Intent galleryIntent = new Intent();
//        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//        galleryIntent.setType("image/*");
//        startActivityForResult(galleryIntent, 24);
//        CustomIntent.customType(NewGroupActivity.this,"left-to-right");
        CropImage.activity()
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 24 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                dialogTitle.setText("Setting Profile Image");
                dialogMessage.setText("Please Wait...");
                dialog.show();
                Uri resultUri = result.getUri();
                final StorageReference filepath = groupProfileImagesRef.child(1 + ".jpg");
                uploadTask = filepath.putFile(resultUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            groupProfileImageUrl = task.getResult().toString();
                            Picasso.get().load(groupProfileImageUrl).placeholder(R.drawable.profile).into(groupImage);
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(NewGroupActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
            }
        }
    }
}