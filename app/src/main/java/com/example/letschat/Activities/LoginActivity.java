package com.example.letschat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import maes.tech.intentanim.CustomIntent;

public class LoginActivity extends AppCompatActivity {

    private Button logInBtn, phoneLogInBtn;
    private EditText userEmail, userPassword;
    private TextView createNewAccount, forgotPassword;
    private Dialog signInDialog;
    private TextView signInDialogTitle, signInDialogMessage;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitializeFields();
        /////SignIn dialog
        signInDialog = new Dialog(LoginActivity.this);
        signInDialog.setContentView(R.layout.custom_progress_dialog);
        signInDialog.setCancelable(false);
        signInDialogTitle = (TextView) signInDialog.findViewById(R.id.dialog_title);
        signInDialogMessage = (TextView) signInDialog.findViewById(R.id.dialog_message);
        signInDialogTitle.setText("Sign In");
        signInDialogMessage.setText("Please Wait...");
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///////SignIn dialog
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userEmail.addTextChangedListener(new TextWatcher() {
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
        userPassword.addTextChangedListener(new TextWatcher() {
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
        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        phoneLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLogInIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLogInIntent);
                CustomIntent.customType(LoginActivity.this,"left-to-right");
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToForgotPasswordActivity();
            }
        });
    }

    private void SendUserToForgotPasswordActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(registerIntent);
        CustomIntent.customType(LoginActivity.this,"left-to-right");
    }

    private void AllowUserToLogin() {
        String email = userEmail.getText().toString();
        String passsword = userPassword.getText().toString();
        if (email.matches(emailPattern)) {
            logInBtn.setEnabled(false);
            logInBtn.setTextColor(Color.argb(50, 255, 255, 255));
            signInDialog.show();
            auth.signInWithEmailAndPassword(email, passsword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String currentUserId = auth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        userRef.child(currentUserId).child("device_token")
                                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    SendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                    signInDialog.dismiss();
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    signInDialog.dismiss();
                                }
                            }
                        });
                    } else {
                        logInBtn.setEnabled(true);
                        logInBtn.setTextColor(Color.rgb(255, 255, 255));
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        signInDialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please enter valid email", Toast.LENGTH_LONG).show();
            userEmail.setError("Invalid Email !");
        }
    }

    private void InitializeFields() {
        logInBtn = (Button) findViewById(R.id.login_btn);
        logInBtn.setEnabled(false);
        logInBtn.setTextColor(Color.argb(50, 255, 255, 255));
        phoneLogInBtn = (Button) findViewById(R.id.login_with_phone_btn);
        userEmail = (EditText) findViewById(R.id.login_email);
        userPassword = (EditText) findViewById(R.id.login_password);
        createNewAccount = (TextView) findViewById(R.id.create_new_account_link);
        forgotPassword = (TextView) findViewById(R.id.forgot_password);
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(userEmail.getText().toString())) {
            logInBtn.setEnabled(true);
            logInBtn.setTextColor(Color.rgb(255, 255, 255));
            if (!TextUtils.isEmpty(userPassword.getText().toString()) && userPassword.length() >= 8) {
                logInBtn.setEnabled(true);
                logInBtn.setTextColor(Color.rgb(255, 255, 255));
            } else {
                logInBtn.setEnabled(false);
                logInBtn.setTextColor(Color.argb(50, 255, 255, 255));
            }
        } else {
            logInBtn.setEnabled(false);
            logInBtn.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        CustomIntent.customType(LoginActivity.this,"left-to-right");
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        CustomIntent.customType(LoginActivity.this,"left-to-right");
        finish();
    }
}