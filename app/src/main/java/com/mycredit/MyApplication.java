package com.mycredit;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FastSave.init(getApplicationContext());
        FirebaseApp.initializeApp(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}
