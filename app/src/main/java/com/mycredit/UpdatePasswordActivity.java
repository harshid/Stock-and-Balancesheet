package com.mycredit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.Utils;

import static utils.Utils.closeProgress;

public class UpdatePasswordActivity extends BaseActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.edtOtp)
    EditText edtOtp;

    FirebaseAuth mAuth;
    SmsVerifyCatcher smsVerifyCatcher;
    String smsVerifyOTP;
    String mVerificationId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        ButterKnife.bind(this);

        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setText(getResources().getString(R.string.update_password));

        mAuth = FirebaseAuth.getInstance();

        validateOTP();
        if (Utils.isNetworkAvailable(UpdatePasswordActivity.this))
            sendOtp();
        else {
            Utils.showToast(UpdatePasswordActivity.this, getString(R.string.check_network));
            finish();
        }
    }

    private void sendOtp() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mAuth.getCurrentUser().getPhoneNumber(),
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        String smsCode = phoneAuthCredential.getSmsCode();
                        if (smsVerifyOTP != null && smsVerifyOTP.matches(smsCode)) {
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, smsVerifyOTP);
                            createUserByPhoneNumber(credential);
                        } else {
                            closeProgress();
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException task) {
                        Utils.showErrorDialog(UpdatePasswordActivity.this, task.getMessage());
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        mVerificationId = verificationId;
                        Utils.showToast(UpdatePasswordActivity.this, getResources().getString(R.string.code_sent));
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick({R.id.imgBack, R.id.txtValidateOtp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.txtValidateOtp:
                if (Utils.isNetworkAvailable(UpdatePasswordActivity.this)) {
                    String strOTP = edtOtp.getText().toString().trim();
                    if (strOTP == null || strOTP.length() <= 0) {
                        Utils.showToast(UpdatePasswordActivity.this, getResources().getString(R.string.otp_null_error_message));
                        return;
                    }
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, strOTP);
                    createUserByPhoneNumber(credential);
                }
                break;
        }
    }

    private void createUserByPhoneNumber(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(UpdatePasswordActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Utils.showToast(UpdatePasswordActivity.this, getResources().getString(R.string.verification_success));
                            startActivity(new Intent(UpdatePasswordActivity.this, SetNewPasswordActivity.class));
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Utils.showErrorDialog(UpdatePasswordActivity.this, task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    private void validateOTP() {
        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                smsVerifyOTP = message.substring(0, Math.min(message.length(), 6)).trim();
            }
        });
    }
}
