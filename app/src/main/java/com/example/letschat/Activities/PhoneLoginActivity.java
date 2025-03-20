package com.example.letschat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import maes.tech.intentanim.CustomIntent;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationCodeBtn, verifyCodeBtn;
    private EditText inputPhoneNumber, inputVerificationCode;
    private FirebaseAuth auth;
    private Dialog dialog;
    private TextView dialogTitle, dialogMessage,allReadyHaveAnAccount;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login); /////status dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.setCancelable(false);
        dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
        dialogMessage = (TextView) dialog.findViewById(R.id.dialog_message);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ///////status dialog

        auth = FirebaseAuth.getInstance();
        InitializeFields();
        sendVerificationCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = inputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter your phone number", Toast.LENGTH_SHORT).show();
                } else {
                    dialogTitle.setText("Phone Verification");
                    dialogMessage.setText("Please Wait...");
                    dialog.show();
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber("+91"+phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone Number.please enter correct phone number with your country code ", Toast.LENGTH_SHORT).show();
                sendVerificationCodeBtn.setVisibility(View.VISIBLE);
                verifyCodeBtn.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
                dialog.dismiss();
            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Code Send Successfully", Toast.LENGTH_SHORT).show();
                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);
                verifyCodeBtn.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        };
        verifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCodeBtn.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
                String verificatioCode=inputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificatioCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter verification code", Toast.LENGTH_SHORT).show();
                }else{
                    dialogTitle.setText("OTP Verification");
                    dialogMessage.setText("Please Wait...");
                    dialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificatioCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        allReadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulation,your login is successful", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else {
                            String error=task.getException().getMessage();
                            Toast.makeText(PhoneLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
    }


    private void InitializeFields() {
        sendVerificationCodeBtn = (Button) findViewById(R.id.send_verification_code_btn);
        verifyCodeBtn = (Button) findViewById(R.id.verify_btn);
        inputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        inputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        allReadyHaveAnAccount = (TextView) findViewById(R.id.allready_have_an_account);
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        CustomIntent.customType(PhoneLoginActivity.this,"left-to-right");
        finish();
    }
    private void SendUserToLoginActivity(){
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        CustomIntent.customType(PhoneLoginActivity.this,"right-to-left");
        finish();
    }
}