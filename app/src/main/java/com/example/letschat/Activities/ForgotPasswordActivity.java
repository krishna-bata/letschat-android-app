package com.example.letschat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import maes.tech.intentanim.CustomIntent;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText registerEmail;
    private Button resetPasswordBtn;
    private ImageView forgotPasswordEmailIcon;
    private TextView forgotPasswordEmailIconText, goBackBtn;
    private ViewGroup emailIconContainer;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        registerEmail = (EditText) findViewById(R.id.forgot_password_email);
//        resetPasswordBtn = (Button) findViewById(R.id.forgot_password_reset_btn);
        forgotPasswordEmailIcon = (ImageView) findViewById(R.id.forgot_password_red_email_icon);
        forgotPasswordEmailIconText = (TextView) findViewById(R.id.forgot_password_email_icon_text);
        goBackBtn = (TextView) findViewById(R.id.forgot_password_goback_btn);
        emailIconContainer = (ViewGroup) findViewById(R.id.forgot_password_email_icon_container);
        progressBar = (ProgressBar) findViewById(R.id.forgot_password_progressbar);
        auth = FirebaseAuth.getInstance();
        registerEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(emailIconContainer);
                forgotPasswordEmailIconText.setVisibility(View.GONE);
                resetPasswordBtn.setEnabled(false);
                resetPasswordBtn.setTextColor(Color.argb(50, 255, 255, 255));
                TransitionManager.beginDelayedTransition(emailIconContainer);
                forgotPasswordEmailIcon.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(registerEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            registerEmail.setText("");
                            forgotPasswordEmailIconText.setText("Recovey email send successfully! check your mail box.");
                            forgotPasswordEmailIconText.setTextColor(getResources().getColor(R.color.green));
                            TransitionManager.beginDelayedTransition(emailIconContainer);
                            forgotPasswordEmailIcon.setVisibility(View.VISIBLE);
                            forgotPasswordEmailIconText.setVisibility(View.VISIBLE);

                        }else{
                            String error=task.getException().getMessage();
                            forgotPasswordEmailIconText.setText(error);
                            forgotPasswordEmailIconText.setTextColor(getResources().getColor(R.color.red));
                            TransitionManager.beginDelayedTransition(emailIconContainer);
                            forgotPasswordEmailIconText.setVisibility(View.VISIBLE);
                            resetPasswordBtn.setEnabled(true);
                            resetPasswordBtn.setTextColor(Color.rgb(255, 255, 255));
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(registerEmail.getText().toString()) && registerEmail.getText().toString().matches(emailPattern)) {
            resetPasswordBtn.setEnabled(true);
            resetPasswordBtn.setTextColor(Color.rgb(255, 255, 255));
        } else {
            resetPasswordBtn.setEnabled(false);
            resetPasswordBtn.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }
}