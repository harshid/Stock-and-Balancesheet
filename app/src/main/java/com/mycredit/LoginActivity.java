package com.mycredit;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.concurrent.TimeUnit;

import bean.SignupBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import utils.Constants;
import utils.Permission;
import utils.Utils;

import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Utils.closeProgress;
import static utils.Utils.showProgress;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.edtPhoneNo)
    EditText edtPhoneNo;
    @BindView(R.id.edtOPT)
    EditText edtOPT;
    @BindView(R.id.countrycodePicker)
    CountryCodePicker countrycodePicker;
    @BindView(R.id.layoutOTP)
    LinearLayout layoutOTP;
    @BindView(R.id.txtOtpTitle)
    TextView txtOtpTitle;

    SmsVerifyCatcher smsVerifyCatcher;
    String mVerificationId;
    String countryCode;
    CountDownTimer countDownTimer;
    String smsVerifyOTP;
    boolean isUserAvail = false;

    FirebaseAuth mAuth;
    String phoneNo;
    SqlLiteDbHelper sqlLiteDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        sqlLiteDbHelper = new SqlLiteDbHelper(LoginActivity.this);

        validateOTP();

        countryCode = countrycodePicker.getSelectedCountryCode();
        countrycodePicker.showFlag(false);
        countrycodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode = countrycodePicker.getSelectedCountryCode();
            }
        });

        /* DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        }); */
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

    @OnClick({R.id.imgValidateNumber, R.id.txtResendOtp, R.id.txtValidateOTP})
    public void performClick(View view) {
        switch (view.getId()) {
            case R.id.txtResendOtp:
                if (Utils.isNetworkAvailable(LoginActivity.this)) {
                    if (Permission.checkInitialPermission(LoginActivity.this))
                        if (validatePhoneNumberClick()) {
                            Utils.hideKeyBoard(edtPhoneNo, LoginActivity.this);
                            stopTimer();
                            firebaseAuthPhoneNo();
                        }
                } else
                    Utils.showToast(LoginActivity.this, getResources().getString(R.string.check_network));
                break;
            case R.id.imgValidateNumber:
                if (Utils.isNetworkAvailable(LoginActivity.this)) {
                    if (Permission.checkInitialPermission(LoginActivity.this))
                        if (validatePhoneNumberClick()) {
                            Utils.hideKeyBoard(edtPhoneNo, LoginActivity.this);
                            stopTimer();
                            firebaseAuthPhoneNo();
                        }
                } else
                    Utils.showToast(LoginActivity.this, getResources().getString(R.string.check_network));
                break;
            case R.id.txtValidateOTP:
                if (Utils.isNetworkAvailable(LoginActivity.this))
                    validateByOTPEnter();
                else
                    Utils.showToast(LoginActivity.this, getResources().getString(R.string.check_network));
                break;
        }
    }

    private void firebaseAuthPhoneNo() {
        closeProgress();
        final String phoneNo = "+" + countryCode + edtPhoneNo.getText().toString();
        showProgress(LoginActivity.this);
        startTimer60Sec();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNo,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        String smsCode = phoneAuthCredential.getSmsCode();
                        if (smsVerifyOTP != null && smsVerifyOTP.matches(smsCode)) {
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, smsVerifyOTP);
                            stopTimer();
                            createUserByPhoneNumber(credential);
                        } else {
                            closeProgress();
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException task) {
                        closeProgress();
                        Utils.showErrorDialog(LoginActivity.this, task.getMessage());
                        stopTimer();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        mVerificationId = verificationId;
                        layoutOTP.setVisibility(View.VISIBLE);
                        txtOtpTitle.setText(getResources().getString(R.string.otp_has_been_sent_on) + " " + "+" + countryCode + " " + edtPhoneNo.getText().toString());
                        closeProgress();
                    }
                });
    }

    private void openMainActivity(SignupBean signupBean) {
        GET_REFERENCE.child(phoneNo).setValue(signupBean);
        GET_BACK_UP_REFERENCE.child(phoneNo).setValue(signupBean);
        sqlLiteDbHelper.insertInfoData(signupBean);
        startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra("bean", signupBean));
        finish();
    }

    public String getDeviceImei() {
        String imeiNo = "";
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imeiNo = telephonyManager.getDeviceId();
        }
        Log.d("===  imei :- ", imeiNo);
        return imeiNo;
    }

    private void validateByOTPEnter() {
        String strOTP = edtOPT.getText().toString().trim();
        if (strOTP == null || strOTP.length() <= 0) {
            Utils.showToast(LoginActivity.this, "OTP should not be null ");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, strOTP);
        stopTimer();

        createUserByPhoneNumber(credential);
    }

    private boolean validatePhoneNumberClick() {
        String phoneNo = "+" + countryCode + edtPhoneNo.getText().toString();
        if (phoneNo == null || phoneNo.length() <= 0) {
            Utils.showToast(LoginActivity.this, "Phone Number Field should not be null ");
            return false;
        } else if (!Utils.isValidMobile(phoneNo)) {
            Utils.showToast(LoginActivity.this, "Phone Number Field is Incurrect");
            return false;
        }
        return true;
    }

    private void createUserByPhoneNumber(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Utils.showToast(LoginActivity.this, getResources().getString(R.string.verification_success));
                            openSignupActivity();
                        } else {
                            closeProgress();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Utils.showToast(LoginActivity.this, getResources().getString(R.string.verification_failed));
                                Utils.showErrorDialog(LoginActivity.this, task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    private void openSignupActivity() {
        phoneNo = "+" + countryCode + edtPhoneNo.getText().toString();
        GET_REFERENCE.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SignupBean signupBean = new SignupBean();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    signupBean.fullname = (String) messageSnapshot.child(Constants.FULL_NAME).getValue();
                    signupBean.business = (String) messageSnapshot.child(Constants.BUSINESS).getValue();
                    signupBean.address = (String) messageSnapshot.child(Constants.ADDRESS).getValue();
                    signupBean.email = (String) messageSnapshot.child(Constants.EMAIL).getValue();
                    signupBean.phoneNo = (String) messageSnapshot.child(Constants.PHONE_NUMBER).getValue();
                    signupBean.deviceIMEI = (String) messageSnapshot.child(Constants.DEVICE_IMEI).getValue();
                    signupBean.password = (String) messageSnapshot.child(Constants.PASSWORD).getValue();
                    signupBean.profile_image = (String) messageSnapshot.child(Constants.PROFILE_IMAGE).getValue();

                    if (signupBean.phoneNo != null && signupBean.phoneNo.length() > 0 && phoneNo.length() > 0) {
                        if (signupBean.phoneNo.contains(phoneNo)) {
                            isUserAvail = true;
                            if (signupBean.deviceIMEI.contains(getDeviceImei())) {
                                closeProgress();
                                startActivity(new Intent(LoginActivity.this, SyncDataFromServerActivity.class).putExtra("signupBean", signupBean));
                                finish();
                                break;
                            } else {
                                closeProgress();
                                Utils.showToast(LoginActivity.this, getResources().getString(R.string.device_not_same));
                                break;
                            }
                        }
                    } else {
                        createNewUser(signupBean);
                        break;
                    }
                }
                if (!isUserAvail) {
                    createNewUser(signupBean);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                closeProgress();
            }
        });
    }

    private void createNewUser(SignupBean signupBean) {
        signupBean.fullname = "";
        signupBean.business = "";
        signupBean.address = "";
        signupBean.email = "";
        signupBean.phoneNo = phoneNo;
        signupBean.deviceIMEI = getDeviceImei();
        signupBean.password = "";
        signupBean.profile_image = "";
        openMainActivity(signupBean);
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // txtValidateNumber.setText(getResources().getString(R.string.validate));
    }

    private void startTimer60Sec() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                // txtValidateNumber.setText(millisUntilFinished / 1000 + " Seconds Remaining");
            }

            public void onFinish() {
                // txtValidateNumber.setText("Resend!");
            }
        }.start();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                final String message = intent.getStringExtra("message");
            }
        }
    };

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
}
