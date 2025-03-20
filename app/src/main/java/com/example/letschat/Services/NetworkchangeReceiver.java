package com.example.letschat.Services;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Freezable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NetworkchangeReceiver extends BroadcastReceiver {
    private FirebaseAuth auth;
    private String currentUserId;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if(isConnected(context)){
                //Toast.makeText(context, "Internet Connected", Toast.LENGTH_SHORT).show();
               // UpdateUserStatus("online");
            }else{
                //Toast.makeText(context, "No Internet connected", Toast.LENGTH_SHORT).show();
                //UpdateUserStatus("offline");
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    private boolean isConnected(Context context){
        try {
            ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            return (networkInfo!=null && networkInfo.isConnected());
        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }
    public void UpdateUserStatus(String state) {
        auth=FirebaseAuth.getInstance();
        currentUserId=auth.getCurrentUser().getUid();
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentData = new SimpleDateFormat("MMM dd");
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentDate = currentData.format(calendar.getTime());
        saveCurrentTime = currentTime.format(calendar.getTime());
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);
        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("userState").updateChildren(onlineStateMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }
}
