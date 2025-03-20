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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.ChatActivity;
import com.example.letschat.Activities.ProfileActivity;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ContactsViewHolder>{
    private ArrayList<Users> usersArrayList;
    private Context context;

    public UsersAdapter(ArrayList<Users> usersArrayList, Context context) {
        this.usersArrayList = usersArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.users_display_layout,parent,false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {
        Users users=usersArrayList.get(position);
            Picasso.get().load(users.getImage()).placeholder(R.drawable.profile).into(holder.image);
            holder.name.setText(users.getName());
            holder.status.setText(users.getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = users.getUid();
                        Intent profileIntent = new Intent(context, ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        context.startActivity(profileIntent);
                        CustomIntent.customType(context,"left-to-right");
                    }
                });
        holder.image.setOnClickListener(new View.OnClickListener() {
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
                Picasso.get().load(users.getImage()).placeholder(R.drawable.profile).into(smallImageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder{
        TextView name,status;
        ImageView image;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.group_name);
            status = (TextView) itemView.findViewById(R.id.user_status);
            image = (ImageView) itemView.findViewById(R.id.group_profile_image);
        }
    }
}
