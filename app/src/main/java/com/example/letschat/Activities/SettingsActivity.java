package com.example.letschat.Activities;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class SettingsActivity extends AppCompatActivity {
    private Button updateAccountBtn;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private static final int galleryPick = 1;
    private StorageReference userProfileImagesRef;
    private Toolbar toolbar;
    private UploadTask uploadTask;
    private Dialog settingsDialog;
    private TextView settingsDialogTitle, settingsDialogMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        /////status dialog
        settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.custom_progress_dialog);
        settingsDialog.setCancelable(false);
        settingsDialogTitle = (TextView) settingsDialog.findViewById(R.id.dialog_title);
        settingsDialogMessage = (TextView) settingsDialog.findViewById(R.id.dialog_message);
        settingsDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///////status dialog
        InitializeFields();
        auth = FirebaseAuth.getInstance();
        currentUserId = getIntent().getExtras().get("uid").toString();
        rootRef = FirebaseDatabase.getInstance().getReference();
        updateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        RetriveUserInfo();
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
    }

    private void UpdateSettings() {
        settingsDialogTitle.setText("Updating Profile");
        settingsDialogMessage.setText("Please Wait...");
        settingsDialog.show();
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        String nameTags = userName.getText().toString().toLowerCase();
        if (TextUtils.isEmpty(setUserName)) {
            settingsDialog.dismiss();
            Toast.makeText(this, "Please write your name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(setStatus)) {
            settingsDialog.dismiss();
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            profileMap.put("nameTags", nameTags);
            rootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        finish();
                        //SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "profile Updated", Toast.LENGTH_SHORT).show();
                        settingsDialog.dismiss();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_SHORT).show();
                        settingsDialog.dismiss();
                    }
                }
            });
        }
    }

    private void RetriveUserInfo() {
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image"))) {
                    String retriveUserName = snapshot.child("name").getValue().toString();
                    String retriveStatus = snapshot.child("status").getValue().toString();
                    String retriveProfileImage = snapshot.child("image").getValue().toString();
                    userName.setText(retriveUserName);
                    userStatus.setText(retriveStatus);
                    Glide.with(getApplicationContext()).load(retriveProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                } else if ((snapshot.exists()) && (snapshot.hasChild("name"))) {
                    String retriveUserName = snapshot.child("name").getValue().toString();
                    String retriveStatus = snapshot.child("status").getValue().toString();
                    userName.setText(retriveUserName);
                    userStatus.setText(retriveStatus);
                } else {
                    Toast.makeText(SettingsActivity.this, "Please set your profile information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        updateAccountBtn = (Button) findViewById(R.id.update_settings_btn);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.profile_group_image);
        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    private void SelectImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String[] options;
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select Profile Image");
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

    private void RemovePhoto() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("image").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SettingsActivity.this, "Remove photo successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SettingsActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void GotoCamera() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,galleryPick);
        CustomIntent.customType(SettingsActivity.this,"left-to-right");
    }

    private void GotoGallery() {
//        Intent galleryIntent = new Intent();
//        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//        galleryIntent.setType("image/*");
//        startActivityForResult(galleryIntent, galleryPick);
//        CustomIntent.customType(SettingsActivity.this,"left-to-right");
        CropImage.activity()
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                settingsDialogTitle.setText("Setting Profile Image");
                settingsDialogMessage.setText("Please Wait...");
                settingsDialog.show();
                Uri resultUri = result.getUri();
                final StorageReference filepath = userProfileImagesRef.child(currentUserId + ".jpg");
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
                            final String downloadUrl = task.getResult().toString();
                            rootRef.child("Users").child(currentUserId).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "Image Save in database suuceefully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                    settingsDialog.dismiss();
                                }
                            });
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                        settingsDialog.dismiss();
                    }
                });
            }
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        CustomIntent.customType(SettingsActivity.this, "left-to-right");
        finish();
    }
}