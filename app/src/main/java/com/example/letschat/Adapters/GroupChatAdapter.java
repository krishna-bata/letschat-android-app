package com.example.letschat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.ChatActivity;
import com.example.letschat.Activities.ImageViewerActivity;
import com.example.letschat.Models.GroupMessagesModel;
import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import maes.tech.intentanim.CustomIntent;

public class GroupChatAdapter extends RecyclerView.Adapter{
    private ArrayList<GroupMessagesModel> groupMessagesModels;
    private Context context;
    private int SENDER_VIEW_TYPE=1;
    private int RECEIVER_VIEW_TYPE=2;
    public GroupChatAdapter(ArrayList<GroupMessagesModel> groupMessagesModels, Context context) {
        this.groupMessagesModels = groupMessagesModels;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==SENDER_VIEW_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.sample_group_sender_layout,parent,false);
            return new SenderViewHolder(view);
        }else{
            View view= LayoutInflater.from(context).inflate(R.layout.sample_group_receiver_layout,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(groupMessagesModels.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }else{
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final GroupMessagesModel groupMessagesModel=groupMessagesModels.get(position);
        if(holder.getClass()==SenderViewHolder.class){
            ((SenderViewHolder) holder).senderMsg.setText(groupMessagesModel.getMessage());
            ((SenderViewHolder) holder).senderTime.setText(groupMessagesModel.getTime());
            ((SenderViewHolder) holder).senderName.setText(groupMessagesModel.getSenderName());
            if(groupMessagesModel.getType().equals("image")){
                ((SenderViewHolder) holder).senderImage.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                Picasso.get().load(groupMessagesModel.getImage()).placeholder(R.drawable.placeholder).into(((SenderViewHolder) holder).senderImage);
                ((SenderViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewIntent = new Intent(((SenderViewHolder) holder).itemView.getContext(), ImageViewerActivity.class);
                        viewIntent.putExtra("url", groupMessagesModel.getImage());
                        ((SenderViewHolder) holder).itemView.getContext().startActivity(viewIntent);
                        CustomIntent.customType(context,"left-to-right");
                    }
                });
            }
            if(groupMessagesModel.getType().equals("pdf")){
                ((SenderViewHolder) holder).senderFile.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                Picasso.get().load(R.drawable.pdf).placeholder(R.drawable.placeholder).into(((SenderViewHolder) holder).senderFile);
                ((SenderViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = LayoutInflater.from(context).inflate(R.layout.custom_delete_msg_dialog, null);
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setView(view)
                                .create();
                        dialog.show();
                        TextView title = view.findViewById(R.id.custom_delete_title);
                        TextView message = view.findViewById(R.id.custom_delete_message);
                        Button yesBtn = view.findViewById(R.id.yes_btn);
                        Button noBtn = view.findViewById(R.id.no_btn);
                        title.setText("Download file ?");
                        message.setText("Do you want to download this pdf file?");
                        yesBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupMessagesModel.getPdf()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context,"left-to-right");
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
            if(groupMessagesModel.getType().equals("docx")){
                ((SenderViewHolder) holder).senderFile.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                Picasso.get().load(R.drawable.docx).placeholder(R.drawable.placeholder).into(((SenderViewHolder) holder).senderFile);
                ((SenderViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = LayoutInflater.from(context).inflate(R.layout.custom_delete_msg_dialog, null);
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setView(view)
                                .create();
                        dialog.show();
                        TextView title = view.findViewById(R.id.custom_delete_title);
                        TextView message = view.findViewById(R.id.custom_delete_message);
                        Button yesBtn = view.findViewById(R.id.yes_btn);
                        Button noBtn = view.findViewById(R.id.no_btn);
                        title.setText("Download file ?");
                        message.setText("Do you want to download this docx file?");
                        yesBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupMessagesModel.getDocx()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context,"left-to-right");
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
            ((SenderViewHolder) holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (groupMessagesModel.getType().equals("text")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Delete For Everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteSentMessage(position, (SenderViewHolder) holder);
                                } else if (i == 1) {
                                    DeleteMessageForEveryone(position);
                                } else if (i == 2) {

                                }
                            }
                        });
                        builder.show();
                    } else if (groupMessagesModel.getType().equals("image")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Delete For Everyone",
                                "Cancel"
                        };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteSentMessage(position, (SenderViewHolder) holder);
                                } else if (i == 1) {
                                    DeleteMessageForEveryone(position);
                                } else if (i == 2) {

                                }
                            }
                        });
                        builder.show();
                    }else if (groupMessagesModel.getType().equals("pdf")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Delete For Everyone",
                                "Cancel"
                        };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteSentMessage(position, (SenderViewHolder) holder);
                                } else if (i == 1) {
                                    DeleteMessageForEveryone(position);
                                } else if (i == 2) {

                                }
                            }
                        });
                        builder.show();
                    }else if (groupMessagesModel.getType().equals("docx")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Delete For Everyone",
                                "Cancel"
                        };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteSentMessage(position, (SenderViewHolder) holder);
                                } else if (i == 1) {
                                    DeleteMessageForEveryone(position);
                                } else if (i == 2) {

                                }
                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        }else{
            ((ReceiverViewHolder)holder).receiverMsg.setText(groupMessagesModel.getMessage());
            ((ReceiverViewHolder)holder).receiverTime.setText(groupMessagesModel.getTime());
            ((ReceiverViewHolder)holder).receiverName.setText(groupMessagesModel.getSenderName());
            if(groupMessagesModel.getType().equals("image")){
                ((ReceiverViewHolder) holder).receiverImage.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                Picasso.get().load(groupMessagesModel.getImage()).placeholder(R.drawable.placeholder).into(((ReceiverViewHolder) holder).receiverImage);
                ((ReceiverViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewIntent = new Intent(((ReceiverViewHolder) holder).itemView.getContext(), ImageViewerActivity.class);
                        viewIntent.putExtra("url", groupMessagesModel.getImage());
                        ((ReceiverViewHolder) holder).itemView.getContext().startActivity(viewIntent);
                        CustomIntent.customType(context,"left-to-right");
                    }
                });
            }
            if(groupMessagesModel.getType().equals("pdf")){
                ((ReceiverViewHolder) holder).receiverFile.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                Picasso.get().load(R.drawable.pdf).placeholder(R.drawable.placeholder).into(((ReceiverViewHolder) holder).receiverFile);
                ((ReceiverViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = LayoutInflater.from(context).inflate(R.layout.custom_delete_msg_dialog, null);
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setView(view)
                                .create();
                        dialog.show();
                        TextView title = view.findViewById(R.id.custom_delete_title);
                        TextView message = view.findViewById(R.id.custom_delete_message);
                        Button yesBtn = view.findViewById(R.id.yes_btn);
                        Button noBtn = view.findViewById(R.id.no_btn);
                        title.setText("Download file ?");
                        message.setText("Do you want to download this pdf file?");
                        yesBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupMessagesModel.getPdf()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context,"left-to-right");
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
            if(groupMessagesModel.getType().equals("docx")){
                ((ReceiverViewHolder) holder).receiverFile.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                Picasso.get().load(R.drawable.docx).placeholder(R.drawable.placeholder).into(((ReceiverViewHolder) holder).receiverFile);
                ((ReceiverViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = LayoutInflater.from(context).inflate(R.layout.custom_delete_msg_dialog, null);
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setView(view)
                                .create();
                        dialog.show();
                        TextView title = view.findViewById(R.id.custom_delete_title);
                        TextView message = view.findViewById(R.id.custom_delete_message);
                        Button yesBtn = view.findViewById(R.id.yes_btn);
                        Button noBtn = view.findViewById(R.id.no_btn);
                        title.setText("Download file ?");
                        message.setText("Do you want to download this docx file?");
                        yesBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupMessagesModel.getDocx()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context,"left-to-right");
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
            ((ReceiverViewHolder)holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (groupMessagesModel.getType().equals("text")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteReceiveMessage(position, (ReceiverViewHolder) holder);
                                } else if (i == 1) {

                                }
                            }
                        });
                        builder.show();
                    } else if (groupMessagesModel.getType().equals("image")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteReceiveMessage(position, (ReceiverViewHolder) holder);
                                } else if (i == 1) {

                                }
                            }
                        });
                        builder.show();
                    }else if (groupMessagesModel.getType().equals("pdf")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteReceiveMessage(position, (ReceiverViewHolder) holder);
                                } else if (i == 1) {

                                }
                            }
                        });
                        builder.show();
                    }else if (groupMessagesModel.getType().equals("docx")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    DeleteReceiveMessage(position, (ReceiverViewHolder) holder);
                                } else if (i == 1) {

                                }
                            }
                        });
                        builder.show();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return groupMessagesModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        TextView receiverMsg,receiverTime,receiverName;
        ImageView receiverImage,receiverFile;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg=(TextView) itemView.findViewById(R.id.receiver_text);
            receiverTime=(TextView) itemView.findViewById(R.id.receiver_time);
            receiverName=(TextView) itemView.findViewById(R.id.receiver_name);
            receiverImage=(ImageView) itemView.findViewById(R.id.receiver_image);
            receiverFile=(ImageView) itemView.findViewById(R.id.receiver_file);
        }
    }
    public class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView senderMsg,senderTime,senderName;
        ImageView senderImage,senderFile;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg=(TextView) itemView.findViewById(R.id.sender_text);
            senderTime=(TextView) itemView.findViewById(R.id.sender_time);
            senderName=(TextView) itemView.findViewById(R.id.sender_name);
            senderImage=(ImageView) itemView.findViewById(R.id.sender_image);
            senderFile=(ImageView) itemView.findViewById(R.id.sender_file);
        }
    }

    private void DeleteSentMessage(final int position, SenderViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupMessagesModels.get(position).getGroupId())
                .child("Chats")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(groupMessagesModels.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void DeleteReceiveMessage(final int position, ReceiverViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupMessagesModels.get(position).getGroupId())
                .child("Chats")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(groupMessagesModels.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteMessageForEveryone(final int position) {
        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupMessagesModels.get(position).getGroupId()).child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = dataSnapshot.child("uid").getValue().toString();
                    FirebaseDatabase.getInstance().getReference().child("Groups")
                            .child(groupMessagesModels.get(position).getGroupId())
                            .child("Chats")
                            .child(uid)
                            .child(groupMessagesModels.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
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
}
