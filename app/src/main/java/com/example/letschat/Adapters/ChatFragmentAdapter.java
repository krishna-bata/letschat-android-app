package com.example.letschat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.ChatActivity;
import com.example.letschat.Models.Friends;
import com.example.letschat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class ChatFragmentAdapter extends RecyclerView.Adapter<ChatFragmentAdapter.ChatFragmentViewHolder> {
    private Context context;
    private ArrayList<Friends> friendsArrayList;

    public ChatFragmentAdapter(Context context, ArrayList<Friends> friendsArrayList) {
        this.context = context;
        this.friendsArrayList = friendsArrayList;
    }

    @NonNull
    @Override
    public ChatFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_display_layout, parent, false);
        return new ChatFragmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatFragmentViewHolder holder, int position) {
        Friends friends = friendsArrayList.get(position);
        if (friends.getImage() != null) {
            Picasso.get().load(friends.getImage()).placeholder(R.drawable.profile).into(holder.profileImage);
        }
        holder.userName.setText(friends.getName());
        holder.userStatus.setText(friends.getStatus());
        if (friends.getState().equals("online")) {
            holder.onlineIcon.setVisibility(View.VISIBLE);
        } else {
            holder.onlineIcon.setVisibility(View.GONE);
        }
        ///////show last message
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(friends.getCurrentUserId() + friends.getReceiverId())
                .orderByChild("time")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                holder.userStatus.setText(snapshot1.child("message").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //////show last message
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(context, ChatActivity.class);
                chatIntent.putExtra("userId", friends.getReceiverId());
                chatIntent.putExtra("userName", friends.getName());
                chatIntent.putExtra("profileImage", friends.getImage());
                holder.itemView.getContext().startActivity(chatIntent);
                CustomIntent.customType(context,"left-to-right");
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsArrayList.size();
    }

    public class ChatFragmentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ChatFragmentViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.group_profile_image);
            userName = itemView.findViewById(R.id.group_name);
            userStatus = itemView.findViewById(R.id.user_status);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
