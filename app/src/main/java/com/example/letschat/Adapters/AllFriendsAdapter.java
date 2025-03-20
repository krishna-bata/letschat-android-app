package com.example.letschat.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Activities.GroupInfoActivity;
import com.example.letschat.Models.Users;
import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;


public class AllFriendsAdapter extends RecyclerView.Adapter<AllFriendsAdapter.allFriendsViewHolder> {
    private Context context;
    private ArrayList<Users> usersArrayList;
    private String groupId, myGroupRole;

    public AllFriendsAdapter(Context context, ArrayList<Users> usersArrayList, String groupId, String myGroupRole) {
        this.context = context;
        this.usersArrayList = usersArrayList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public allFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_display_layout, parent, false);
        return new allFriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull allFriendsViewHolder holder, int position) {
        Users users = usersArrayList.get(position);
        if (users.getImage() != null) {
            Picasso.get().load(users.getImage()).placeholder(R.drawable.profile).into(holder.userImage);
        }
        holder.userName.setText(users.getName());
        CheckIfUserAlreadyExist(users, holder);
        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.small_image_viewer_layout);
                dialog.getWindow().setBackgroundDrawable(getDrawable(context, R.drawable.layout_background));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
                dialog.show();
                PhotoView smallImageView = (PhotoView) dialog.findViewById(R.id.small_image_viwer);
                Picasso.get().load(users.getImage()).placeholder(R.drawable.profile).into(smallImageView);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Groups")
                        .child(groupId).child("Members")
                        .child(users.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String hisPreviousRole = snapshot.child("role").getValue().toString();
                            String[] options;
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Choose Option");
                            if (myGroupRole.equals("creator")) {
                                if (hisPreviousRole.equals("admin")) {
                                    options = new String[]{"Remove Admin", "Remove user"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                RemoveAdmin(users);
                                            } else {
                                                RemoveMember(users);
                                            }
                                        }
                                    }).show();
                                } else if (hisPreviousRole.equals("member")) {
                                    options = new String[]{"Make Admin", "Remove user"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                MakeAdmin(users);
                                            } else {
                                                RemoveMember(users);
                                            }
                                        }
                                    }).show();
                                }
                            } else if (myGroupRole.equals("admin")) {
                                if (hisPreviousRole.equals("creator")) {
                                    Toast.makeText(context, "Creator of group...", Toast.LENGTH_SHORT).show();
                                } else if (hisPreviousRole.equals("admin")) {
                                    options = new String[]{"Remove Admin", "Remove user"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                RemoveAdmin(users);
                                            } else {
                                                RemoveMember(users);
                                            }
                                        }
                                    }).show();
                                } else if (hisPreviousRole.equals("member")) {
                                    options = new String[]{"Make Admin", "Remove user"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                MakeAdmin(users);
                                            } else {
                                                RemoveMember(users);
                                            }
                                        }
                                    }).show();
                                }
                            }
                        } else {
                            Dialog dialog=new Dialog(context);
                            dialog.setContentView(R.layout.custom_delete_msg_dialog);
                            dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
                            dialog.show();
                            TextView title = dialog.findViewById(R.id.custom_delete_title);
                            TextView message = dialog.findViewById(R.id.custom_delete_message);
                            Button yesBtn = dialog.findViewById(R.id.yes_btn);
                            Button noBtn = dialog.findViewById(R.id.no_btn);
                            title.setText("Add Membr?");
                            message.setText("Are you want to add this user in current group?");
                            yesBtn.setText("ADD");
                            noBtn.setText("NO");
                            yesBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AddMember(users);
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void AddMember(Users users) {
        HashMap<String, String> map = new HashMap<>();
        map.put("role", "member");
        map.put("uid", users.getUid());
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupId).child("Members")
                .child(users.getUid())
                .setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MakeAdmin(Users users) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("role", "admin");
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupId).child("Members")
                .child(users.getUid())
                .updateChildren(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "This user now admin", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void RemoveMember(Users users) {
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupId).child("Members")
                .child(users.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Remove successfully", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void RemoveAdmin(Users users) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("role", "member");
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupId).child("Members")
                .child(users.getUid())
                .updateChildren(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "This user no longer admin", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void CheckIfUserAlreadyExist(Users users, allFriendsViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("Groups")
                .child(groupId).child("Members")
                .child(users.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String hisRole = snapshot.child("role").getValue().toString();
                            holder.userStatus.setText(hisRole);
                        } else {
                            holder.userStatus.setText("Not member");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class allFriendsViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView userImage;
        private TextView userName, userStatus;
        private ImageView onlineIcon;

        public allFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.group_profile_image);
            userName = itemView.findViewById(R.id.group_name);
            userStatus = itemView.findViewById(R.id.user_status);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
