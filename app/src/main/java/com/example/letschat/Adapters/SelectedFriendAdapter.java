package com.example.letschat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Models.Friends;
import com.example.letschat.Models.SelectedMembersModel;
import com.example.letschat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectedFriendAdapter extends RecyclerView.Adapter<SelectedFriendAdapter.SelectedViewholder> {
    private Context context;
    private ArrayList<SelectedMembersModel> selectedMembersModels;

    public SelectedFriendAdapter(Context context, ArrayList<SelectedMembersModel> selectedMembersModels) {
        this.context = context;
        this.selectedMembersModels = selectedMembersModels;
    }

    @NonNull
    @Override
    public SelectedViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.selected_friend_layout, parent, false);
        return new SelectedViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedViewholder holder, int position) {
        SelectedMembersModel model = selectedMembersModels.get(position);
        if (model.getImage() != null) {
            Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(holder.selectedImage);
        }
    }

    @Override
    public int getItemCount() {
        return selectedMembersModels.size();
    }

    public class SelectedViewholder extends RecyclerView.ViewHolder {
        private CircleImageView selectedImage;

        public SelectedViewholder(@NonNull View itemView) {
            super(itemView);
            selectedImage = itemView.findViewById(R.id.selected_image);
        }
    }
}
