package com.example.letschat.Activities;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserId, sendertUserId, current_state, currentUserName;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMsgRequestBtn, declineMsgRequestBtn;
    private FirebaseAuth auth;
    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private String SERVER_KEY = "AAAAqPBSoAs:APA91bH2hXbIRLNFQel54VXceasAFWoUMBOBc9ndxAxQ7vcMrnyoQbheWGacUETvf2LtVXndr-nnoS9fAtcyoUcwR46B0ki_RohEm87_TkTDoQ2LgHF5UxIjWgTCat7dKgPRTl3fPlZs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();
        sendertUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_profile_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        sendMsgRequestBtn = (Button) findViewById(R.id.send_msg_request_btn);
        declineMsgRequestBtn = (Button) findViewById(R.id.decline_msg_request_btn);
        current_state = "new";
        RetriveUserInfo();
        GetUserInfo();
        CheckUserBlockOrNot();
    }

    private void RetriveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))) {
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Glide.with(getApplicationContext()).load(userImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    userProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Dialog dialog = new Dialog(ProfileActivity.this);
                            dialog.setContentView(R.layout.small_image_viewer_layout);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.layout_background));
                            }
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
                            dialog.show();
                            PhotoView smallImageView =(PhotoView)dialog.findViewById(R.id.small_image_viwer);
                            Picasso.get().load(userImage).placeholder(R.drawable.profile).into(smallImageView);
                        }
                    });
                    ManageChatRequests();
                } else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests() {
        chatRequestRef.child(sendertUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserId)) {
                    String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        current_state = "request_sent";
                        sendMsgRequestBtn.setText("Cancel Friend Request ");
                    } else if (request_type.equals("received")) {
                        current_state = "request_received";
                        sendMsgRequestBtn.setText("Accept Friend Request");
                        declineMsgRequestBtn.setVisibility(View.VISIBLE);
                        declineMsgRequestBtn.setEnabled(true);
                        declineMsgRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                } else {
                    contactsRef.child(sendertUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserId)) {
                                current_state = "friends";
                                sendMsgRequestBtn.setText("UnFriend");
                            }
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
        if (!sendertUserId.equals(receiverUserId)) {
            sendMsgRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMsgRequestBtn.setEnabled(false);
                    sendMsgRequestBtn.setTextColor(Color.argb(50, 255, 255, 255));
                    if (current_state.equals("new")) {
                        SendChatRequest();
                    }
                    if (current_state.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (current_state.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if (current_state.equals("friends")) {
                        RemoveSpecificContact();
                    }
                    if (current_state.equals("block")) {
                        UnBlockUser();
                    }
                }
            });
        } else {
            sendMsgRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void UnBlockUser() {
        userRef.child(sendertUserId)
                .child("BlockUsers")
                .child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendMsgRequestBtn.setEnabled(true);
                            sendMsgRequestBtn.setTextColor(Color.rgb(255, 255, 255));
                            current_state = "new";
                            sendMsgRequestBtn.setText("Send Friend Request");
                            Toast.makeText(ProfileActivity.this, "Unblocked Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void RemoveSpecificContact() {
        contactsRef.child(sendertUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId).child(sendertUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                declineMsgRequestBtn.setVisibility(View.INVISIBLE);
                                                declineMsgRequestBtn.setEnabled(false);
                                                sendMsgRequestBtn.setEnabled(true);
                                                sendMsgRequestBtn.setTextColor(Color.rgb(255, 255, 255));
                                                current_state = "new";
                                                sendMsgRequestBtn.setText("Send Friend Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        HashMap<String, String> map = new HashMap<>();
        map.put("Contacts", "Saved");
        map.put("uid", receiverUserId);
        contactsRef.child(sendertUserId).child(receiverUserId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Contacts", "Saved");
                    map.put("uid", sendertUserId);
                    contactsRef.child(receiverUserId).child(sendertUserId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chatRequestRef.child(sendertUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chatRequestRef.child(receiverUserId).child(sendertUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.isSuccessful()) {
                                                            sendMsgRequestBtn.setEnabled(true);
                                                            current_state = "friends";
                                                            sendMsgRequestBtn.setTextColor(Color.rgb(255, 255, 255));
                                                            sendMsgRequestBtn.setText("UnFriend");
                                                            declineMsgRequestBtn.setVisibility(View.INVISIBLE);
                                                            declineMsgRequestBtn.setEnabled(false);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelChatRequest() {
        chatRequestRef.child(sendertUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(sendertUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                declineMsgRequestBtn.setVisibility(View.INVISIBLE);
                                                declineMsgRequestBtn.setEnabled(false);
                                                sendMsgRequestBtn.setEnabled(true);
                                                sendMsgRequestBtn.setTextColor(Color.rgb(255, 255, 255));
                                                current_state = "new";
                                                sendMsgRequestBtn.setText("Send Friend Request");
                                                Toast.makeText(ProfileActivity.this, "cancel request successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        userRef.child(receiverUserId).child("BlockUsers")
                .child(sendertUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(ProfileActivity.this, "This user block you, Please say Unblock you..", Toast.LENGTH_LONG).show();
                    sendMsgRequestBtn.setEnabled(true);
                    sendMsgRequestBtn.setTextColor(Color.rgb(255, 255, 255));
                } else {
                    chatRequestRef.child(sendertUserId).child(receiverUserId)
                            .child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        chatRequestRef.child(receiverUserId).child(sendertUserId)
                                                .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    getToken(receiverUserId, currentUserName);
                                                    sendMsgRequestBtn.setEnabled(true);
                                                    sendMsgRequestBtn.setTextColor(Color.rgb(255, 255, 255));
                                                    current_state = "request_sent";
                                                    sendMsgRequestBtn.setText("Cancel Friend Request");
                                                } else {
                                                    Toast.makeText(ProfileActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getToken(String receiverId, String name) {
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
                    data.put("type", "Friend Request");
                    data.put("title", name);
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
        userRef.child(sendertUserId).addValueEventListener(new ValueEventListener() {
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

    private void CheckUserBlockOrNot() {
        userRef.child(sendertUserId).child("BlockUsers").child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    current_state = "block";
                    sendMsgRequestBtn.setVisibility(View.VISIBLE);
                    sendMsgRequestBtn.setText("Unblock");
                    sendMsgRequestBtn.setEnabled(true);
                    sendMsgRequestBtn.setTextColor(Color.rgb(255, 255, 255));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
