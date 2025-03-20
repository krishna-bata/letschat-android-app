package com.example.letschat.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.ChatActivity;
import com.example.letschat.Models.Friends;
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

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>{
    private Context context;
    private ArrayList<Friends> friendsArrayList;

    public FriendsAdapter(Context context, ArrayList<Friends> friendsArrayList) {
        this.context = context;
        this.friendsArrayList = friendsArrayList;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.users_display_layout,parent,false);
        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
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
                FirebaseDatabase.getInstance().getReference().child("Users").child(friends.getReceiverId()).child("BlockUsers")
                        .child(friends.getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Toast.makeText(context, "This user block you, Please say Unblock you to chatting with each other", Toast.LENGTH_LONG).show();
                        }else{
                            Intent chatIntent = new Intent(context, ChatActivity.class);
                            chatIntent.putExtra("userId", friends.getReceiverId());
                            chatIntent.putExtra("userName", friends.getName());
                            chatIntent.putExtra("profileImage",friends.getImage());
                            context.startActivity(chatIntent);
                            CustomIntent.customType(context,"left-to-right");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        holder.userImage.setOnClickListener(new View.OnClickListener() {
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
                Picasso.get().load(friends.getImage()).placeholder(R.drawable.profile).into(smallImageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsArrayList.size();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView userImage;
        private TextView userName,userStatus;
        private ImageView onlineIcon;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage=itemView.findViewById(R.id.group_profile_image);
            userName=itemView.findViewById(R.id.group_name);
            userStatus=itemView.findViewById(R.id.user_status);
            onlineIcon=itemView.findViewById(R.id.user_online_status);
        }
    }
}
