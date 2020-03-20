package com.mycredit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import bean.SignupBean;
import database.SqlLiteDbHelper;
import utils.Constants;
import utils.Utils;

import static utils.Constants.ADDRESS;
import static utils.Constants.BUSINESS;
import static utils.Constants.EMAIL;
import static utils.Constants.FULL_NAME;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.PASSWORD;
import static utils.Constants.PROFILE_IMAGE;


public class SplashScreen extends BaseActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    SqlLiteDbHelper sqlLiteDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sqlLiteDbHelper = new SqlLiteDbHelper(this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (Utils.isNetworkAvailable(SplashScreen.this)) {
            if (user != null) {
                checkUserAvailable(user.getPhoneNumber());
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                        finish();
                    }
                }, 2500);
            }
        } else {
            SignupBean signupBean = sqlLiteDbHelper.getDataBean();
            if (signupBean != null) {
                startActivity(new Intent(SplashScreen.this, MainActivity.class).putExtra("bean", signupBean));
            } else
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
            finish();
        }
    }

    private void checkUserAvailable(final String phoneNo) {
        GET_REFERENCE.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isFoundNo = false;
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    SignupBean signupBean = new SignupBean();
                    signupBean.fullname = (String) messageSnapshot.child(Constants.FULL_NAME).getValue();
                    signupBean.business = (String) messageSnapshot.child(Constants.BUSINESS).getValue();
                    signupBean.address = (String) messageSnapshot.child(Constants.ADDRESS).getValue();
                    signupBean.email = (String) messageSnapshot.child(Constants.EMAIL).getValue();
                    signupBean.phoneNo = (String) messageSnapshot.child(Constants.PHONE_NUMBER).getValue();
                    signupBean.deviceIMEI = (String) messageSnapshot.child(Constants.DEVICE_IMEI).getValue();
                    signupBean.password = (String) messageSnapshot.child(Constants.PASSWORD).getValue();
                    signupBean.profile_image = (String) messageSnapshot.child(Constants.PROFILE_IMAGE).getValue();
                    if (signupBean.phoneNo != null && signupBean.phoneNo.length() > 0 && phoneNo.length() > 0) {
                        if (signupBean.phoneNo.substring(1).contains(phoneNo.substring(1))) {
                            if (signupBean.deviceIMEI.contains(getDeviceImei())) {
                                isFoundNo = true;
                                SignupBean bean = sqlLiteDbHelper.getDataBean();
                                if (Utils.isNetworkAvailable(SplashScreen.this) && FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    DatabaseReference myRef = GET_REFERENCE.child(signupBean.phoneNo);
                                    DatabaseReference myBaskupRef = GET_BACK_UP_REFERENCE.child(signupBean.phoneNo);
                                    if (!bean.password.equalsIgnoreCase(signupBean.password)) {
                                        myRef.child(PASSWORD).setValue(bean.password);
                                        myBaskupRef.child(PASSWORD).setValue(bean.password);
                                    }
                                    if (!bean.fullname.equalsIgnoreCase(signupBean.fullname)) {
                                        myRef.child(FULL_NAME).setValue(bean.fullname);
                                        myBaskupRef.child(FULL_NAME).setValue(bean.fullname);
                                    }
                                    if (!bean.business.equalsIgnoreCase(signupBean.business)) {
                                        myRef.child(BUSINESS).setValue(bean.business);
                                        myBaskupRef.child(BUSINESS).setValue(bean.business);
                                    }
                                    if (!bean.address.equalsIgnoreCase(signupBean.address)) {
                                        myRef.child(ADDRESS).setValue(bean.address);
                                        myBaskupRef.child(ADDRESS).setValue(bean.address);
                                    }
                                    if (!bean.email.equalsIgnoreCase(signupBean.email)) {
                                        myRef.child(EMAIL).setValue(bean.email);
                                        myBaskupRef.child(EMAIL).setValue(bean.email);
                                    }
                                    if (!bean.profile_image.equalsIgnoreCase(signupBean.profile_image)) {
                                        myRef.child(PROFILE_IMAGE).setValue(bean.profile_image);
                                        myBaskupRef.child(PROFILE_IMAGE).setValue(bean.profile_image);
                                    }
                                }
                                startActivity(new Intent(SplashScreen.this, MainActivity.class).putExtra("bean", bean));
                                finish();
                                break;
                            }
                        }
                    }
                }
                if (!isFoundNo) {
                    startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                finish();
            }
        });
    }

    public String getDeviceImei() {
        String imeiNo = "";
        if (ActivityCompat.checkSelfPermission(SplashScreen.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imeiNo = telephonyManager.getDeviceId();
        }
        return imeiNo;
    }
}
