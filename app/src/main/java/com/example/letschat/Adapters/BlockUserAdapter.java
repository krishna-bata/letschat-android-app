package com.example.letschat.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.UserInfoActivity;
import com.example.letschat.Models.Friends;
import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockUserAdapter extends RecyclerView.Adapter<BlockUserAdapter.BlockUsersViewHolder>{
    private Context context;
    private ArrayList<Friends> friendsArrayList;

    public BlockUserAdapter(Context context, ArrayList<Friends> friendsArrayList) {
        this.context = context;
        this.friendsArrayList = friendsArrayList;
    }

    @NonNull
    @Override
    public BlockUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.users_display_layout,parent,false);
        return new BlockUsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockUsersViewHolder holder, int position) {
        Friends friends=friendsArrayList.get(position);
        if(friends.getImage()!=null) {
            Picasso.get().load(friends.getImage()).placeholder(R.drawable.profile).into(holder.userImage);
        }
        holder.userName.setText(friends.getName());
        holder.userStatus.setText(friends.getStatus());
        if(friends.getState().equals("online")){
            holder.onlineIcon.setVisibility(View.VISIBLE);
        }else{
            holder.onlineIcon.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog=new Dialog(context);
                dialog.setContentView(R.layout.custom_delete_msg_dialog);
                dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
                dialog.show();
                TextView title = dialog.findViewById(R.id.custom_delete_title);
                TextView message = dialog.findViewById(R.id.custom_delete_message);
                Button yesBtn = dialog.findViewById(R.id.yes_btn);
                Button noBtn = dialog.findViewById(R.id.no_btn);
                title.setText("Unblock User?");
                message.setText("Are you want to sure unblock this user?");
                yesBtn.setText("UNBLOCK");
                noBtn.setText("NO");
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UnBlockedUser(context,friends.getReceiverId(),friends.getCurrentUserId());
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
        });
    }

    @Override
    public int getItemCount() {
        return friendsArrayList.size();
    }

    public class BlockUsersViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView userImage;
        private TextView userName,userStatus;
        private ImageView onlineIcon;
        public BlockUsersViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage=itemView.findViewById(R.id.group_profile_image);
            userName=itemView.findViewById(R.id.group_name);
            userStatus=itemView.findViewById(R.id.user_status);
            onlineIcon=itemView.findViewById(R.id.user_online_status);
        }
    }
    private void UnBlockedUser(Context context,String receiverId,String currentUserId) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId)
                .child("BlockUsers")
                .child(receiverId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Unblock Successfully", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
