package com.example.letschat.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.letschat.Fragments.ChatsFragment;
import com.example.letschat.Fragments.GroupsFragment;
import com.example.letschat.Fragments.RequestFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment=new GroupsFragment();
                return groupsFragment;
            case 2:
                RequestFragment requestFragment=new RequestFragment();
                return requestFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}
