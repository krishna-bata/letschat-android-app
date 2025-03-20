package com.example.letschat.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.GroupChatActivity;
import com.example.letschat.Models.GroupsModel;
import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {
    private ArrayList<GroupsModel> groupsArrayList;
    private Context context;

    public GroupsAdapter(ArrayList<GroupsModel> groupsArrayList, Context context) {
        this.groupsArrayList = groupsArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.groups_display_layout, parent, false);
        return new GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsViewHolder holder, int position) {
        GroupsModel groups = groupsArrayList.get(position);
        if (groups.getImage() != null) {
            Picasso.get().load(groups.getImage()).placeholder(R.drawable.profile).into(holder.groupProfileImage);
        }else{
            Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(holder.groupProfileImage);
        }
        holder.groupName.setText(groups.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentGroupId = groups.getGroupId();
                Intent groupChatIntent = new Intent(holder.itemView.getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupId", currentGroupId);
                holder.itemView.getContext().startActivity(groupChatIntent);
                CustomIntent.customType(context,"left-to-right");
            }
        });
        holder.groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.small_image_viewer_layout);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dialog.getWindow().setBackgroundDrawable(getDrawable(context,R.drawable.layout_background));
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
                dialog.show();
                PhotoView smallImageView =(PhotoView)dialog.findViewById(R.id.small_image_viwer);
                Picasso.get().load(groups.getImage()).placeholder(R.drawable.profile).into(smallImageView);
            }
        });
        loadLastMessage(groups, holder);
    }

    private void loadLastMessage(GroupsModel groups, GroupsViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groups.getGroupId())
                .child("Chats")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                String senderName = dataSnapshot.child("senderName").getValue().toString();
                                String time = dataSnapshot.child("time").getValue().toString();
                                String message = dataSnapshot.child("message").getValue().toString();
                                holder.senderName.setText(senderName+":");
                                holder.lastMessage.setText(message);
                                holder.lastMessageTime.setText(time);
                                holder.lastMsgContainer.setVisibility(View.VISIBLE);
                                holder.senderName.setVisibility(View.VISIBLE);
                                holder.lastMessage.setVisibility(View.VISIBLE);
                                holder.lastMessageTime.setVisibility(View.VISIBLE);
                            } else {
                                holder.lastMsgContainer.setVisibility(View.GONE);
                                holder.senderName.setVisibility(View.GONE);
                                holder.lastMessage.setVisibility(View.GONE);
                                holder.lastMessageTime.setVisibility(View.GONE);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupsArrayList.size();
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView groupProfileImage;
        private TextView groupName, senderName, lastMessage, lastMessageTime;
        private LinearLayout lastMsgContainer;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            groupProfileImage = itemView.findViewById(R.id.group_profile_image);
            groupName = itemView.findViewById(R.id.group_name);
            senderName = itemView.findViewById(R.id.sender_name);
            lastMessage = itemView.findViewById(R.id.last_msg);
            lastMessageTime = itemView.findViewById(R.id.last_msg_time);
            lastMsgContainer=itemView.findViewById(R.id.last_msg_container);
        }
    }
}
