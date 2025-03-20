package com.example.letschat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.GroupsAdapter;
import com.example.letschat.Models.GroupsModel;
import com.example.letschat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    private ArrayList<GroupsModel> groupsArrayList;
    private GroupsAdapter groupsAdapter;
    private RecyclerView groupRecyclerView;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String currentUserId;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        groupRecyclerView = groupFragmentView.findViewById(R.id.group_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        groupRecyclerView.setLayoutManager(layoutManager);
        groupsArrayList = new ArrayList<>();
        groupsAdapter = new GroupsAdapter(groupsArrayList, getContext());
        groupRecyclerView.setAdapter(groupsAdapter);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        database.getReference().child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    groupsArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.child("Members").child(currentUserId).exists()) {
                            GroupsModel groups = dataSnapshot.getValue(GroupsModel.class);
                            groupsArrayList.add(groups);
                        }
                    }
                    groupsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return groupFragmentView;
    }
}
