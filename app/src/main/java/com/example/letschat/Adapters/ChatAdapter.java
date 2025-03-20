package com.example.letschat.Adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.ImageViewerActivity;
import com.example.letschat.Models.MessageModel;
import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import maes.tech.intentanim.CustomIntent;

public class ChatAdapter extends RecyclerView.Adapter {
    private ArrayList<MessageModel> messageModels;
    private Context context;
    private int SENDER_VIEW_TYPE = 1;
    private int RECEIVER_VIEW_TYPE = 2;
    String senderRoom, receiverRoom;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final MessageModel messageModel = messageModels.get(position);
        if (holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder) holder).senderMsg.setText(messageModel.getMessage());
            ((SenderViewHolder) holder).senderTime.setText(messageModel.getTime());
            if (messageModel.getIsseen().equals("Seen")) {
                ((SenderViewHolder) holder).seenMsgIndicator.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).deliveredMsgIndicator.setVisibility(View.GONE);
            } else {
                ((SenderViewHolder) holder).seenMsgIndicator.setVisibility(View.GONE);
                ((SenderViewHolder) holder).deliveredMsgIndicator.setVisibility(View.VISIBLE);
            }
            if (messageModel.getType().equals("image")) {
                ((SenderViewHolder) holder).senderImage.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                Picasso.get().load(messageModel.getImage()).placeholder(R.drawable.placeholder).into(((SenderViewHolder) holder).senderImage);
                ((SenderViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewIntent = new Intent(((SenderViewHolder) holder).itemView.getContext(), ImageViewerActivity.class);
                        viewIntent.putExtra("url", messageModel.getImage());
                        ((SenderViewHolder) holder).itemView.getContext().startActivity(viewIntent);
                        CustomIntent.customType(context,"left-to-right");
                    }
                });
            }
            if (messageModel.getType().equals("pdf")) {
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
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageModel.getPdf()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context, "left-to-right");
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
            if (messageModel.getType().equals("docx")) {
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
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageModel.getDocx()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context, "left-to-right");
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
                    if (messageModel.getType().equals("text")) {
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
                                    DeleteSentMessage(position, ((SenderViewHolder) holder));
                                } else if (i == 1) {
                                    DeleteMessageForEveryone(position);
                                } else if (i == 2) {

                                }
                            }
                        });
                        builder.show();
                    } else if (messageModel.getType().equals("image")) {
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
                                    DeleteSentMessage(position, ((SenderViewHolder) holder));
                                } else if (i == 1) {
                                    DeleteMessageForEveryone(position);
                                } else if (i == 2) {

                                }
                            }
                        });
                        builder.show();
                    } else if (messageModel.getType().equals("pdf")) {
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
                                    DeleteSentMessage(position, ((SenderViewHolder) holder));
                                } else if (i == 1) {
                                    DeleteMessageForEveryone(position);
                                } else if (i == 2) {

                                }
                            }
                        });
                        builder.show();
                    } else if (messageModel.getType().equals("docx")) {
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
                                    DeleteSentMessage(position, ((SenderViewHolder) holder));
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

        } else {
            ((ReceiverViewHolder) holder).receiverMsg.setText(messageModel.getMessage());
            ((ReceiverViewHolder) holder).receiverTime.setText(messageModel.getTime());
            if (messageModel.getType().equals("image")) {
                ((ReceiverViewHolder) holder).receiverImage.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                Picasso.get().load(messageModel.getImage()).placeholder(R.drawable.placeholder).into(((ReceiverViewHolder) holder).receiverImage);
                ((ReceiverViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewIntent = new Intent(((ReceiverViewHolder) holder).itemView.getContext(), ImageViewerActivity.class);
                        viewIntent.putExtra("url", messageModel.getImage());
                        ((ReceiverViewHolder) holder).itemView.getContext().startActivity(viewIntent);
                        CustomIntent.customType(context, "left-to-right");
                    }
                });
            }
            if (messageModel.getType().equals("pdf")) {
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
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageModel.getPdf()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context, "left-to-right");
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
            if (messageModel.getType().equals("docx")) {
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
                        message.setText("Do you want to download this pdf file?");
                        yesBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageModel.getDocx()));
                                holder.itemView.getContext().startActivity(intent);
                                CustomIntent.customType(context, "left-to-right");
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
            ((ReceiverViewHolder) holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (messageModel.getType().equals("text")) {
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
                                    DeleteReceiveMessage(position, ((ReceiverViewHolder) holder));
                                } else if (i == 1) {

                                }
                            }
                        });
                        builder.show();
                    } else if (messageModel.getType().equals("image")) {
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
                                    DeleteReceiveMessage(position, ((ReceiverViewHolder) holder));
                                } else if (i == 1) {

                                }
                            }
                        });
                        builder.show();
                    } else if (messageModel.getType().equals("pdf")) {
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
                                    DeleteReceiveMessage(position, ((ReceiverViewHolder) holder));
                                } else if (i == 1) {

                                }
                            }
                        });
                        builder.show();
                    } else if (messageModel.getType().equals("docx")) {
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
                                    DeleteReceiveMessage(position, ((ReceiverViewHolder) holder));
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
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg, receiverTime;
        ImageView receiverImage, receiverFile;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = (TextView) itemView.findViewById(R.id.receiver_text);
            receiverTime = (TextView) itemView.findViewById(R.id.receiver_time);
            receiverImage = (ImageView) itemView.findViewById(R.id.receiver_image);
            receiverFile = (ImageView) itemView.findViewById(R.id.receiver_file);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;
        ImageView senderImage, senderFile, deliveredMsgIndicator, seenMsgIndicator;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = (TextView) itemView.findViewById(R.id.sender_text);
            senderTime = (TextView) itemView.findViewById(R.id.sender_time);
            senderImage = (ImageView) itemView.findViewById(R.id.sender_image);
            senderFile = (ImageView) itemView.findViewById(R.id.sender_file);
            deliveredMsgIndicator = (ImageView) itemView.findViewById(R.id.sender_single_check);
            seenMsgIndicator = (ImageView) itemView.findViewById(R.id.sender_double_check);
        }
    }

    private void DeleteSentMessage(final int position, SenderViewHolder holder) {
        senderRoom = messageModels.get(position).getUid() + messageModels.get(position).getReceiverId();
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(senderRoom)
                .child(messageModels.get(position).getMessageId())
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
        receiverRoom = messageModels.get(position).getReceiverId() + messageModels.get(position).getUid();
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(receiverRoom)
                .child(messageModels.get(position).getMessageId())
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
        senderRoom = messageModels.get(position).getUid() + messageModels.get(position).getReceiverId();
        receiverRoom = messageModels.get(position).getReceiverId() + messageModels.get(position).getUid();
        final String messageId = messageModels.get(position).getMessageId();
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(senderRoom)
                .child(messageId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(receiverRoom)
                .child(messageId)
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

//    private void SaveImageInGallary(ImageView imageView) {
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
//        Bitmap bitmap = bitmapDrawable.getBitmap();
//        FileOutputStream fileOutputStream = null;
//        File file = Environment.getExternalStorageDirectory();
//        File dir = new File(file.getAbsolutePath() + "/LETSCHAT/Media/Images");
//        dir.mkdirs();
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
//        String saveCurrentTime = currentTime.format(calendar.getTime());
//        String filename =saveCurrentTime+".png";
//        File outFile = new File(dir, filename);
//        try {
//            fileOutputStream = new FileOutputStream(outFile);
//            Toast.makeText(context, "Image save successfully", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
//        try {
//            fileOutputStream.flush();
//        } catch (Exception e) {
//            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//        try {
//            fileOutputStream.close();
//        } catch (Exception e) {
//            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
////        Picasso.get().load(messageModel.getImage()).into(imageView);
////        Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
////        Calendar calendar = Calendar.getInstance();
////        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
////        String saveCurrentTime = currentTime.format(calendar.getTime());
////        File path= Environment.getExternalStorageDirectory();
////        File dir=new File(path+"/LETSCHAT/Media/Images");
////        dir.mkdirs();
////        String imagename=saveCurrentTime+".PNG";
////        File file=new File(dir,imagename);
////        OutputStream out;
////        try{
////            out=new FileOutputStream(file);
////            bitmap.compress(Bitmap.CompressFormat.PNG,100,out);
////            out.flush();
////            out.close();
////            Toast.makeText(context, "Image Save successfully", Toast.LENGTH_SHORT).show();
////        }catch (Exception e){
////            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
////        }
//    }


}
