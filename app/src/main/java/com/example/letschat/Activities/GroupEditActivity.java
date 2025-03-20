package com.example.letschat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.TextView;
import android.widget.Toast;

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

public class GroupEditActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String groupId,groupName;
    private CircleImageView groupImage;
    private EditText groupNameInput;
    private Button updateBtn;
    private StorageReference groupProfileImagesRef;
    private UploadTask uploadTask;
    private String groupProfileImageUrl, currentUserId, currentUserName;
    private Dialog LoadingDialog;
    private TextView dialogTitle, dialogMessage;
    private String saveCurrentTime, saveCurrentDate;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Group");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow_white);
        groupId=getIntent().getExtras().get("groupId").toString();
        groupImage=(CircleImageView)findViewById(R.id.group_image);
        groupNameInput=(EditText) findViewById(R.id.group_name);
        updateBtn=(Button)findViewById(R.id.update_btn);
        groupProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Groups Profile Images");
        loadGroupInfo();
        auth=FirebaseAuth.getInstance();
        currentUserId=auth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentDate = currentDate.format(calendar.getTime());
        saveCurrentTime = currentTime.format(calendar.getTime());
        ///// dialog
        LoadingDialog = new Dialog(this);
        LoadingDialog.setContentView(R.layout.custom_progress_dialog);
        LoadingDialog.setCancelable(false);
        dialogTitle = (TextView) LoadingDialog.findViewById(R.id.dialog_title);
        dialogMessage = (TextView) LoadingDialog.findViewById(R.id.dialog_message);
        LoadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        /////// dialog
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupName = groupNameInput.getText().toString();
                if (!TextUtils.isEmpty(groupName)) {
                    dialogTitle.setText("Updating Group Info");
                    dialogMessage.setText("Please Wait...");
                    LoadingDialog.show();
                    DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
                    Map<String, Object> groupInfo = new HashMap<>();
                    if (groupProfileImageUrl != null) {
                        groupInfo.put("image", groupProfileImageUrl);
                    }
                    groupInfo.put("name", groupName);
                    groupRef.updateChildren(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                LoadingDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, "GroupInfo Update Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                LoadingDialog.dismiss();;
                                String error = task.getException().getMessage();
                                Toast.makeText(GroupEditActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                           LoadingDialog.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(GroupEditActivity.this, "Please Enter Group Name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
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
                LoadingDialog.show();
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
                            Toast.makeText(GroupEditActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                        LoadingDialog.dismiss();
                    }
                });
            }
        }
    }
    private void loadGroupInfo() {
        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    String groupId = snapshot.child("groupId").getValue().toString();
                    if(snapshot.child("image").exists()) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(groupImage);
                    }
                    String groupAdmin = snapshot.child("groupAdmin").getValue().toString();
                    String date = snapshot.child("date").getValue().toString();
                    String time = snapshot.child("time").getValue().toString();
                    String created = snapshot.child("createdBy").getValue().toString();
                    getSupportActionBar().setTitle(name);
                    groupNameInput.setText(name);
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
    private void SelectImage(){
        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String[] options;
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupEditActivity.this);
                builder.setTitle("Select Group Profile Image");
                if(snapshot.hasChild("image")){
                    options = new String[]{"Camera", "Gallery","Remove Photo","Cancel"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                GotoCamera();
                            } else if(which==1){
                                GotoGallery();
                            }else if(which==2){
                                RemovePhoto();
                            }else if(which==3){
                                dialog.dismiss();
                            }
                        }
                    }).show();
                }else{
                    options = new String[]{"Camera", "Gallery","Cancel"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                GotoCamera();
                            } else if(which==1){
                                GotoGallery();
                            }else if(which==2){
                                dialog.dismiss();
                            }
                        }
                    }).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GotoCamera() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,24);
        CustomIntent.customType(GroupEditActivity.this,"left-to-right");
    }

    private void GotoGallery() {
//        Intent galleryIntent = new Intent();
//        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//        galleryIntent.setType("image/*");
//        startActivityForResult(galleryIntent, 24);
//        CustomIntent.customType(GroupEditActivity.this,"left-to-right");
        CropImage.activity()
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
    private void RemovePhoto(){
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupId).child("image").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(groupImage);
                    Toast.makeText(GroupEditActivity.this, "Remove Photo Successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(GroupEditActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}