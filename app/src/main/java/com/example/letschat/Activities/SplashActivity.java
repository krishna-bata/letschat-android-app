package com.example.letschat.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.letschat.R;

import maes.tech.intentanim.CustomIntent;

public class SplashActivity extends AppCompatActivity {
    private TextView appName;
    private LottieAnimationView noInternetConnection,chatLogoAnimation;
    private Button refreshBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        appName=(TextView) findViewById(R.id.app_name);
        noInternetConnection=(LottieAnimationView)findViewById(R.id.no_internet_connection);
        chatLogoAnimation=(LottieAnimationView)findViewById(R.id.chat_logo_animation);
        refreshBtn=(Button) findViewById(R.id.refresh_btn);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                isConnected(SplashActivity.this);
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                CustomIntent.customType(SplashActivity.this,"left-to-right");
                finish();
            }
        }, 3000);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, SplashActivity.class);
                startActivity(intent);
                CustomIntent.customType(SplashActivity.this,"left-to-right");
                finish();
                appName.setVisibility(View.VISIBLE);
                chatLogoAnimation.setVisibility(View.VISIBLE);
                noInternetConnection.setVisibility(View.GONE);
                refreshBtn.setVisibility(View.GONE);
            }
        });
    }
//    private void isConnected(Context context){
//        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo wifi=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        NetworkInfo mobile=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        if((wifi!=null && wifi.isConnected()) || mobile!=null && mobile.isConnected()){
//            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//            startActivity(intent);
//            CustomIntent.customType(SplashActivity.this,"left-to-right");
//            finish();
//        }else{
//            Toast.makeText(context, "No Internet Connection, Please turn on your Internet Connection ", Toast.LENGTH_LONG).show();
//            appName.setVisibility(View.GONE);
//            chatLogoAnimation.setVisibility(View.GONE);
//            noInternetConnection.setVisibility(View.VISIBLE);
//            refreshBtn.setVisibility(View.VISIBLE);
//        }
//    }
}