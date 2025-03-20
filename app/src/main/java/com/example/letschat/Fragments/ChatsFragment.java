package com.example.letschat.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.ChatActivity;
import com.example.letschat.Activities.FriendsActivity;
import com.example.letschat.Models.Contacts;
import com.example.letschat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;


public class ChatsFragment extends Fragment {
    private View privateChatsView;
    private RecyclerView chatsList;
    private DatabaseReference chatsRef, userRef;
    private FirebaseAuth auth;
    private String currentUserId;
    private Dialog loadingDialog;
    private FloatingActionButton addFrinds;
    private NetworkInfo wifi, mobile;
    private ConnectivityManager connectivityManager;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatsList = (RecyclerView) privateChatsView.findViewById(R.id.chats_list);
        addFrinds = (FloatingActionButton) privateChatsView.findViewById(R.id.add_friends);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("HasChats").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        /////loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadingDialog.getWindow().setBackgroundDrawable(getDrawable(getContext(), R.drawable.loading_dialog_background));
        }
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        ///////loading dialog
        addFrinds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToFriendsActivity();
            }
        });
        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        if ((wifi != null && wifi.isConnected()) || mobile != null && mobile.isConnected()) {
            chatsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                    } else {
                        loadingDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(chatsRef, Contacts.class).build();
            FirebaseRecyclerAdapter<Contacts, ChatsviewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsviewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final ChatsviewHolder holder, int position, @NonNull Contacts model) {
                    final String usersIds = getRef(position).getKey();
                    final String[] userImage = {"Default Image"};
                    userRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.hasChild("image")) {
                                    userImage[0] = snapshot.child("image").getValue().toString();
                                    Picasso.get().load(userImage[0]).placeholder(R.drawable.profile).into(holder.profileImage);
                                }
                                final String username = snapshot.child("name").getValue().toString();
                                final String userstatus = snapshot.child("status").getValue().toString();
                                holder.userName.setText(username);
                                holder.userStatus.setText(userstatus);
                                if (snapshot.child("userState").hasChild("state")) {
                                    String state = snapshot.child("userState").child("state").getValue().toString();
                                    String date = snapshot.child("userState").child("date").getValue().toString();
                                    String time = snapshot.child("userState").child("time").getValue().toString();
                                    if (state.equals("online")) {
                                        holder.onlineIcon.setVisibility(View.VISIBLE);
                                    } else if (state.equals("offline")) {
                                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                    holder.userStatus.setText("offline");
                                }
                                loadingDialog.dismiss();
                                ///////show last message
                                FirebaseDatabase.getInstance().getReference().child("Chats")
                                        .child(currentUserId + usersIds)
                                        .orderByChild("time")
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChildren()) {
                                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                        if (snapshot1.exists()) {
                                                            String time=snapshot1.child("time").getValue().toString();
                                                            String message=snapshot1.child("message").getValue().toString();
                                                            holder.userStatus.setText(message);
                                                            holder.lastMessageTime.setText(time);
                                                            holder.lastMessageTime.setVisibility(View.VISIBLE);
                                                            loadingDialog.dismiss();
                                                        }else{
                                                            holder.lastMessageTime.setVisibility(View.GONE);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                //////show last message
                                /////show count of unseen message
                                FirebaseDatabase.getInstance().getReference().child("Chats").child(usersIds+currentUserId)
                                        .orderByChild("isseen").equalTo("Delivered").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            int i=(int)snapshot.getChildrenCount();
                                            holder.messageCount.setVisibility(View.VISIBLE);
                                            holder.messageCount.setText(String.valueOf(i));
                                        }else{
                                            holder.messageCount.setVisibility(View.GONE);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                /////show count of unseen message
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userRef.child(usersIds).child("BlockUsers")
                                                .child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    Toast.makeText(getContext(), "This user block you, Please say Unblock you to chatting with each other", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                                    intent.putExtra("userId", usersIds);
                                                    intent.putExtra("userName", username);
                                                    intent.putExtra("profileImage", userImage[0]);
                                                    getContext().startActivity(intent);
                                                    CustomIntent.customType(getContext(), "left-to-right");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                });
                                holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Dialog dialog = new Dialog(getContext());
                                        dialog.setContentView(R.layout.small_image_viewer_layout);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            dialog.getWindow().setBackgroundDrawable(getDrawable(getContext(), R.drawable.layout_background));
                                        }
                                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                        dialog.show();
                                        PhotoView smallImageView = (PhotoView) dialog.findViewById(R.id.small_image_viwer);
                                        Picasso.get().load(userImage[0]).placeholder(R.drawable.profile).into(smallImageView);
                                    }
                                });
                                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        Dialog dialog = new Dialog(getContext());
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
                                                DeleteAllChats(usersIds);
                                                dialog.dismiss();
                                            }
                                        });
                                        noBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                            }
                                        });
                                        return true;
                                    }
                                });
                            } else {
                                loadingDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @NonNull
                @Override
                public ChatsviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.users_display_layout, parent, false);
                    return new ChatsviewHolder(view);
                }
            };
            chatsList.setAdapter(adapter);
            adapter.startListening();
//        } else {
//            loadingDialog.dismiss();
//            Toast.makeText(getContext(), "No Internet Connection, Please turn on your Internet Connection ", Toast.LENGTH_LONG).show();
//        }
    }


    public static class ChatsviewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;
        TextView messageCount,lastMessageTime;

        public ChatsviewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.group_profile_image);
            userName = itemView.findViewById(R.id.group_name);
            userStatus = itemView.findViewById(R.id.user_status);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
            messageCount=itemView.findViewById(R.id.message_count);
            lastMessageTime=itemView.findViewById(R.id.last_message_time);
        }
    }

    private void SendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(getContext(), FriendsActivity.class);
        friendsIntent.putExtra("currentUserId", currentUserId);
        startActivity(friendsIntent);
        CustomIntent.customType(getContext(), "left-to-right");
    }

    private void DeleteAllChats(String receiverId) {
        String senderRoom = currentUserId + receiverId;
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(senderRoom)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseDatabase.getInstance().getReference().child("HasChats")
                        .child(currentUserId)
                        .child(receiverId)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Chat Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }
}