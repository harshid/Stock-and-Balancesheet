package com.mycredit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mac on 25/06/19.
 */

public class SecurityActivity extends BaseActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        ButterKnife.bind(this);

        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setText(getResources().getString(R.string.security));
    }


    @OnClick({R.id.imgBack, R.id.llUpdatePassword, R.id.llAppLock, R.id.llPaymentPassword, R.id.llSignOut})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.llUpdatePassword:
                startActivity(new Intent(SecurityActivity.this, UpdatePasswordActivity.class));
                break;
            case R.id.llAppLock:
                break;
            case R.id.llPaymentPassword:
                break;
            case R.id.llSignOut:
                break;
        }
    }
}
