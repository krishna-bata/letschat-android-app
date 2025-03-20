package com.example.letschat.Activities;


import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.letschat.Adapters.ChatAdapter;
import com.example.letschat.Models.MessageModel;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import maes.tech.intentanim.CustomIntent;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

public class ChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private LottieAnimationView typingAnimation;
    private ViewGroup typingAnimationContainer;
    private TextView userName, userStatus;
    private EmojiconEditText messageText;
    private ImageButton sendMsgBtn, sendFilesBtn, emojiBtn;
    private View view;
    private EmojIconActions emojIconActions;
    private ImageView backArrowBtn, sendImage, sendPdf, sendDocx;
    private CircleImageView profileImage;
    private RecyclerView chatRecyclerView;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private Dialog sendingFileDialog;
    private TextView sendingFileDialogTitle, sendingFileDialogMessage;
    private String senderRoom;
    private String receiverRoom;
    private String receiverName, receiverImage, messageId;
    private String saveCurrentTime, saveCurrentDate, senderId, receiverId, currentUserName, currentUserImage;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private String SERVER_KEY = "AAAAqPBSoAs:APA91bH2hXbIRLNFQel54VXceasAFWoUMBOBc9ndxAxQ7vcMrnyoQbheWGacUETvf2LtVXndr-nnoS9fAtcyoUcwR46B0ki_RohEm87_TkTDoQ2LgHF5UxIjWgTCat7dKgPRTl3fPlZs";
    private String checker = "";
    private ValueEventListener seenListener;
    private DatabaseReference reference;
    private RelativeLayout attachmentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        typingAnimation = (LottieAnimationView) findViewById(R.id.typing_animation);
        typingAnimationContainer = (ViewGroup) findViewById(R.id.typing_animation_container);
        //Dialog
        sendingFileDialog = new Dialog(ChatActivity.this);
        sendingFileDialog.setContentView(R.layout.custom_progress_dialog);
        sendingFileDialog.setCancelable(false);
        sendingFileDialogTitle = (TextView) sendingFileDialog.findViewById(R.id.dialog_title);
        sendingFileDialogMessage = (TextView) sendingFileDialog.findViewById(R.id.dialog_message);
        sendingFileDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //Dialog
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentDate = currentDate.format(calendar.getTime());
        saveCurrentTime = currentTime.format(calendar.getTime());
        senderId = auth.getCurrentUser().getUid();
        if (getIntent().hasExtra("messageId")) {
            messageId = getIntent().getStringExtra("messageId");
            receiverName = getIntent().getStringExtra("userName");
            receiverImage = getIntent().getStringExtra("profileImage");
            receiverId = getIntent().getStringExtra("userId");
        } else {
            receiverName = getIntent().getStringExtra("userName");
            receiverImage = getIntent().getStringExtra("profileImage");
            receiverId = getIntent().getStringExtra("userId");
        }
        userName = (TextView) findViewById(R.id.profile_group_name);
        userStatus = (TextView) findViewById(R.id.profile_status);
        messageText = (EmojiconEditText) findViewById(R.id.input_msg);
        profileImage = (CircleImageView) findViewById(R.id.profile_group_image);
        sendMsgBtn = (ImageButton) findViewById(R.id.send_msg_btn);
        sendFilesBtn = (ImageButton) findViewById(R.id.send_files_btn);
        emojiBtn = (ImageButton) findViewById(R.id.emoji_btn);
        view = (View) findViewById(R.id.root_layout);
        backArrowBtn = (ImageView) findViewById(R.id.back_arrow);
        sendImage = (ImageView) findViewById(R.id.send_image);
        sendPdf = (ImageView) findViewById(R.id.send_pdf);
        sendDocx = (ImageView) findViewById(R.id.send_docx);
        attachmentLayout = findViewById(R.id.attachment_layout);
        chatRecyclerView = (RecyclerView) findViewById(R.id.groupChatRecyclerView);
        userName.setText(receiverName);
        Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(profileImage);
        emojIconActions = new EmojIconActions(this, view, messageText, emojiBtn);
        emojIconActions.ShowEmojIcon();
        backArrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ChatActivity.this);
                dialog.setContentView(R.layout.small_image_viewer_layout);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.layout_background));
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.show();
                PhotoView smallImageView = (PhotoView) dialog.findViewById(R.id.small_image_viwer);
                Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(smallImageView);
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
                CustomIntent.customType(ChatActivity.this, "left-to-right");
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
                CustomIntent.customType(ChatActivity.this, "left-to-right");
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
                CustomIntent.customType(ChatActivity.this, "left-to-right");
                HideAttachmentLayout();
            }
        });
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this);
        chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        chatRecyclerView.setLayoutManager(layoutManager);
        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        database.getReference().child("Chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.smoothScrollToPosition(chatRecyclerView.getAdapter().getItemCount());
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

        final Handler handler = new Handler();
        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("Users").child(senderId).child("userState")
                        .child("state").setValue("typing");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStopTyping, 2000);
                DisplayLastSeen();
            }

            Runnable userStopTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Users").child(senderId).child("userState")
                            .child("state").setValue("online");
                    DisplayLastSeen();
                }
            };
        });
        GetUserInfo();
        DisplayLastSeen();
        SeenMessage();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 50 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (checker.equals("pdf")) {
                Uri selectedFile = data.getData();
                Calendar calendar = Calendar.getInstance();
                final StorageReference reference = storage.getReference().child("Chats PDF Files").child(calendar.getTimeInMillis() + " ");
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
                                    String message = " pdf file";
                                    String type = "pdf";
                                    DatabaseReference userMessageKeyRef = database.getReference().child("Child").child(senderRoom).push();
                                    final String messagePushId = userMessageKeyRef.getKey();
                                    MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Delivered");
                                    model.setPdf(filepath);
                                    messageText.setText("");
                                    database.getReference().child("Chats")
                                            .child(senderRoom)
                                            .child(messagePushId)
                                            .setValue(model)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Seen");
                                                    model.setPdf(filepath);
                                                    database.getReference().child("Chats")
                                                            .child(receiverRoom)
                                                            .child(messagePushId)
                                                            .setValue(model)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                    map.put("Chats", "yes");
                                                                    map.put("uid", receiverId);
                                                                    database.getReference().child("HasChats")
                                                                            .child(senderId).child(receiverId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                                    map.put("Chats", "yes");
                                                                                    map.put("uid", senderId);
                                                                                    database.getReference().child("HasChats")
                                                                                            .child(receiverId).child(senderId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    getToken(message, receiverId, currentUserImage, messagePushId, currentUserName);
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }
                                                            });
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
                final StorageReference reference = storage.getReference().child("Chats DOCX Files").child(calendar.getTimeInMillis() + " ");
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
                                    DatabaseReference userMessageKeyRef = database.getReference().child("Child").child(senderRoom).push();
                                    final String messagePushId = userMessageKeyRef.getKey();
                                    final MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Delivered");
                                    model.setDocx(filepath);
                                    messageText.setText("");
                                    database.getReference().child("Chats")
                                            .child(senderRoom)
                                            .child(messagePushId)
                                            .setValue(model)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    final MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Seen");
                                                    model.setDocx(filepath);
                                                    database.getReference().child("Chats")
                                                            .child(receiverRoom)
                                                            .child(messagePushId)
                                                            .setValue(model)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                    map.put("Chats", "yes");
                                                                    map.put("uid", receiverId);
                                                                    database.getReference().child("HasChats")
                                                                            .child(senderId).child(receiverId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                                    map.put("Chats", "yes");
                                                                                    map.put("uid", senderId);
                                                                                    database.getReference().child("HasChats")
                                                                                            .child(receiverId).child(senderId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    getToken(message, receiverId, currentUserImage, messagePushId, currentUserName);
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }
                                                            });
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
                final StorageReference reference = storage.getReference().child("Chats Images").child(calendar.getTimeInMillis() + " ");
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
                                    DatabaseReference userMessageKeyRef = database.getReference().child("Child").child(senderRoom).push();
                                    final String messagePushId = userMessageKeyRef.getKey();
                                    final MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Delivered");
                                    model.setImage(filepath);
                                    messageText.setText("");
                                    database.getReference().child("Chats")
                                            .child(senderRoom)
                                            .child(messagePushId)
                                            .setValue(model)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    final MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Seen");
                                                    model.setImage(filepath);
                                                    database.getReference().child("Chats")
                                                            .child(receiverRoom)
                                                            .child(messagePushId)
                                                            .setValue(model)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                    map.put("Chats", "yes");
                                                                    map.put("uid", receiverId);
                                                                    database.getReference().child("HasChats")
                                                                            .child(senderId).child(receiverId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                                    map.put("Chats", "yes");
                                                                                    map.put("uid", senderId);
                                                                                    database.getReference().child("HasChats")
                                                                                            .child(receiverId).child(senderId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    getToken(message, receiverId, currentUserImage, messagePushId, currentUserName);
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }
                                                            });
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

    private void DisplayLastSeen() {
        database.getReference().child("Users").child(receiverId).child("userState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("state")) {
                    String state = snapshot.child("state").getValue().toString();
                    String date = snapshot.child("date").getValue().toString();
                    String time = snapshot.child("time").getValue().toString();
                    ConnectivityManager connectivityManager = (ConnectivityManager) ChatActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    if ((wifi != null && wifi.isConnected()) || mobile != null && mobile.isConnected()) {
                        if (state.equals("online")) {
                            userStatus.setVisibility(View.VISIBLE);
                            userStatus.setText("online");
                            TransitionManager.beginDelayedTransition(typingAnimationContainer);
                            typingAnimationContainer.setVisibility(GONE);
                            typingAnimation.cancelAnimation();
                        } else if (state.equals("typing")) {
                            userStatus.setVisibility(View.VISIBLE);
                            TransitionManager.beginDelayedTransition(typingAnimationContainer);
                            typingAnimationContainer.setVisibility(View.VISIBLE);
                            typingAnimation.playAnimation();
                            chatRecyclerView.smoothScrollToPosition(chatRecyclerView.getAdapter().getItemCount());
                            userStatus.setText("typing..");
                        } else if (state.equals("offline")) {
                            userStatus.setVisibility(View.VISIBLE);
                            userStatus.setText("Last Seen " + date + " at " + time);
                            TransitionManager.beginDelayedTransition(typingAnimationContainer);
                            typingAnimationContainer.setVisibility(GONE);
                            typingAnimation.cancelAnimation();
                        }
                    } else {
                        userStatus.setVisibility(View.GONE);
                    }
                } else {
                    userStatus.setVisibility(View.VISIBLE);
                    userStatus.setText("offline");
                    TransitionManager.beginDelayedTransition(typingAnimationContainer);
                    typingAnimationContainer.setVisibility(GONE);
                    typingAnimation.cancelAnimation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendMessage() {
        final String message = messageText.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            DatabaseReference userMessageKeyRef = database.getReference().child("Chats").child(senderRoom).push();
            final String messagePushId = userMessageKeyRef.getKey();
            String type = "text";
            MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Delivered");
            messageText.setText("");
            database.getReference().child("Chats")
                    .child(senderRoom)
                    .child(messagePushId)
                    .setValue(model)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            MessageModel model = new MessageModel(message, senderId, saveCurrentTime, saveCurrentDate, receiverId, messagePushId, type, "Seen");
                            database.getReference().child("Chats")
                                    .child(receiverRoom)
                                    .child(messagePushId)
                                    .setValue(model)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("Chats", "yes");
                                            map.put("uid", receiverId);
                                            database.getReference().child("HasChats")
                                                    .child(senderId).child(receiverId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put("Chats", "yes");
                                                            map.put("uid", senderId);
                                                            database.getReference().child("HasChats")
                                                                    .child(receiverId).child(senderId).updateChildren(map)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            getToken(message, receiverId, currentUserImage, messagePushId, currentUserName);
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
        } else {
            Toast.makeText(ChatActivity.this, "Please type message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (attachmentLayout.getVisibility() == VISIBLE) {
            HideAttachmentLayout();
        } else {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);

    }

    private void SeenMessage() {
        reference = FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        MessageModel model = dataSnapshot.getValue(MessageModel.class);
                        if (model.getUid() != senderId) {
                            String messageId = model.getMessageId();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("isseen", "Seen");
                            snapshot.child(messageId).getRef().updateChildren(map);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getToken(String message, String receiverId, String receiverImage, String messageId, String name) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = null;
                if (snapshot.child("device_token").getValue() != null) {
                    token = snapshot.child("device_token").getValue().toString();
                }
                JSONObject to = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("type", "Chatting");
                    data.put("sender_name", name);
                    data.put("message", message);
                    data.put("receiverId", senderId);
                    data.put("receiverImage", receiverImage);
                    data.put("messageId", messageId);
                    to.put("to", token);
                    to.put("data", data);
                    sendNotification(to);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void sendNotification(JSONObject to) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, to, response -> {
            Log.d("notification", "sendNotification: " + response);
        }, error -> {
            Log.d("notification", "sendNotification: " + error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "key=" + SERVER_KEY);
                map.put("Content-Type", "application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private void GetUserInfo() {
        database.getReference().child("Users").child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("name").getValue().toString();
                    if(snapshot.child("image").getValue() != null){
                        currentUserImage = snapshot.child("image").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.clear_chat) {
            ClearAllChat();
        } else if (item.getItemId() == R.id.call_chat) {

        } else if (item.getItemId() == R.id.video_call_chat) {

        } else if (item.getItemId() == R.id.user_info) {
            SendUserToUserInfoActivity(receiverId);
        }
        return true;
    }

    private void ClearAllChat() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_delete_msg_dialog);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
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
                database.getReference().child("Chats").child(senderRoom).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ChatActivity.this, "Chat Deleted Successfully", Toast.LENGTH_SHORT).show();
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

    private void ShowAttachmentLayout() {
        float radius = Math.max(attachmentLayout.getWidth(), attachmentLayout.getHeight());
        Animator animator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(attachmentLayout, attachmentLayout.getRight(), attachmentLayout.getTop(), 0, radius * 2);
        }
        animator.setDuration(500);
        animator.start();
        attachmentLayout.setVisibility(View.VISIBLE);
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

//    private void BlockUser(String receiverId, MenuItem item) {
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("block", "yes");
//        database.getReference().child("Users").child(senderId).child("BlockUsers").child(receiverId)
//                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    item.setTitle("Unblock");
//                    Toast.makeText(ChatActivity.this, "Blocked Successfully", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(ChatActivity.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

//    private void UnBlockedUser(String receiverId, MenuItem item) {
//        database.getReference().child("Users").child(senderId)
//                .child("BlockUsers")
//                .child(receiverId)
//                .removeValue()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            item.setTitle("Block");
//                            Toast.makeText(ChatActivity.this, "Unblocked Successfully", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(ChatActivity.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//    }

    private void SendUserToUserInfoActivity(String receiverId) {
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("uid", receiverId);
        startActivity(intent);
        CustomIntent.customType(ChatActivity.this, "left-to-right");
    }

//    private void CheckIsBlockOrUnblock(String receiverId, MenuItem item) {
//        database.getReference().child("Users").child(senderId)
//                .child("BlockUsers")
//                .child(receiverId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            if ((snapshot.child("block").getValue().toString()).equals("yes")) {
//                                item.setTitle("Unblock");
//                            } else {
//                                item.setTitle("Block");
//                            }
//                        } else {
//                            item.setTitle("Block");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }
}