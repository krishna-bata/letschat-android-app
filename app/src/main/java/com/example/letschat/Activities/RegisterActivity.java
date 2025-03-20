package com.example.letschat.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import maes.tech.intentanim.CustomIntent;

public class RegisterActivity extends AppCompatActivity {
    private Button registerInBtn;
    private EditText userEmail, userPassword, confirmPassword;
    private TextView alreadyHaveAccount;
    private ProgressDialog loadingBar;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private Dialog signUpDialog;
    private TextView signUpDialogTitle, signUpDialogMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();
        /////SignIn dialog
        signUpDialog = new Dialog(RegisterActivity.this);
        signUpDialog.setContentView(R.layout.custom_progress_dialog);
        signUpDialog.setCancelable(false);
        signUpDialogTitle = (TextView) signUpDialog.findViewById(R.id.dialog_title);
        signUpDialogMessage = (TextView) signUpDialog.findViewById(R.id.dialog_message);
        signUpDialogTitle.setText("Sign Up");
        signUpDialogMessage.setText("Please Wait...");
        signUpDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///////SignIn dialog
        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });
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
        confirmPassword.addTextChangedListener(new TextWatcher() {
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
        registerInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = userEmail.getText().toString();
        String passsword = userPassword.getText().toString();
        String ConfirmPassword=confirmPassword.getText().toString();
        if (email.matches(emailPattern)) {
            registerInBtn.setEnabled(false);
            registerInBtn.setTextColor(Color.argb(50, 255, 255, 255));
            if (!ConfirmPassword.equals("") && ConfirmPassword.equals(passsword)) {
                signUpDialog.show();
                auth.createUserWithEmailAndPassword(email, passsword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String currentUserId = auth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            rootRef.child("Users").child(currentUserId).child("device_token")
                                    .setValue(deviceToken);
                            rootRef.child("Users")
                                    .child(currentUserId).setValue("");
                            SendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            signUpDialog.dismiss();
                        } else {
                            registerInBtn.setEnabled(true);
                            registerInBtn.setTextColor(Color.rgb(255, 255, 255));
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            signUpDialog.dismiss();
                        }
                    }
                });
            }else{
                Toast.makeText(this, "Password Dosen't Match", Toast.LENGTH_SHORT).show();
                confirmPassword.setError("Password Dosen't match");
            }
        } else {
            Toast.makeText(this, "Please enter valid email", Toast.LENGTH_LONG).show();
            userEmail.setError("Invalid Email !");
    }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        CustomIntent.customType(RegisterActivity.this, "left-to-right");
        finish();
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(userEmail.getText().toString())) {
            registerInBtn.setEnabled(true);
            registerInBtn.setTextColor(Color.rgb(255, 255, 255));
            if (!TextUtils.isEmpty(userPassword.getText().toString()) && userPassword.length() >= 8) {
                registerInBtn.setEnabled(true);
                registerInBtn.setTextColor(Color.rgb(255, 255, 255));
                if (!TextUtils.isEmpty(confirmPassword.getText().toString()) && confirmPassword.length() >= 8) {
                    registerInBtn.setEnabled(true);
                    registerInBtn.setTextColor(Color.rgb(255, 255, 255));
                } else {
                    registerInBtn.setEnabled(false);
                    registerInBtn.setTextColor(Color.argb(50, 255, 255, 255));
                }
            } else {
                registerInBtn.setEnabled(false);
                registerInBtn.setTextColor(Color.argb(50, 255, 255, 255));
            }
        } else {
            registerInBtn.setEnabled(false);
            registerInBtn.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }

    private void InitializeFields() {
        registerInBtn = (Button) findViewById(R.id.register_btn);
        registerInBtn.setEnabled(false);
        registerInBtn.setTextColor(Color.argb(50, 255, 255, 255));
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        confirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        alreadyHaveAccount = (TextView) findViewById(R.id.already_have_an_account_link);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        CustomIntent.customType(RegisterActivity.this, "right-to-left");
        finish();
    }
}