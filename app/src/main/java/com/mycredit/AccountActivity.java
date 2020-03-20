package com.mycredit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import bean.CustomerBean;
import bean.SignupBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.Constants;

import static utils.Constants.GET_REFERENCE;
import static utils.Constants.LANGUAGE;
import static utils.Constants.PROFILE;

public class AccountActivity extends BaseActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtCustomerNumber)
    TextView txtCustomerCount;
    @BindView(R.id.txtBalance)
    TextView txtBalance;
    @BindView(R.id.txtSelectedLanguage)
    TextView txtSelectedLanguage;

    FirebaseAuth mAuth;
    DatabaseReference myRef;
    SignupBean signupBean;

    float balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ButterKnife.bind(this);

        signupBean = (SignupBean) getIntent().getSerializableExtra("bean");

        mAuth = FirebaseAuth.getInstance();
        setSelectedLan();

        if (mAuth.getCurrentUser() != null)
            getUserData();
    }

    private void setSelectedLan() {
        String lan = FastSave.getInstance().getString(LANGUAGE, "en");
        if (lan.equalsIgnoreCase(Constants.HINDI_CODE)) {
            txtSelectedLanguage.setText(getResources().getString(R.string.hindi));
        } else if (lan.equalsIgnoreCase(Constants.HINGLISH_CODE)) {
            txtSelectedLanguage.setText(getResources().getString(R.string.hinglish));
        } else if (lan.equalsIgnoreCase(Constants.ENGLISH_CODE)) {
            txtSelectedLanguage.setText(getResources().getString(R.string.english));
        }
    }

    private void getUserData() {
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setText(signupBean.fullname);
        txtTitle.setTextColor(getResources().getColor(R.color.green));

        myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null)
                    txtCustomerCount.setText(dataSnapshot.getChildrenCount() + "");
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    CustomerBean customerBean = data.getValue(CustomerBean.class);
                    balance = customerBean.type.equalsIgnoreCase(Constants.DUE) ?
                            balance + Float.parseFloat(customerBean.amount) : balance - Float.parseFloat(customerBean.amount);
                }
                txtBalance.setTextColor(balance > 0 ? getResources().getColor(R.color.red) : getResources().getColor(R.color.green));
                txtBalance.setText(getResources().getString(R.string.rupees_symbol) + (balance + "").replace("-", ""));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @OnClick({R.id.imgBack, R.id.llLanguage, R.id.llAccountStatement, R.id.llDownloadBackup, R.id.llSecurity, R.id.llProfile, R.id.llReminder})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.llAccountStatement:
                break;
            case R.id.llDownloadBackup:
                break;
            case R.id.llLanguage:
                startActivity(new Intent(AccountActivity.this, LanguageActivity.class));
                break;
            case R.id.llSecurity:
                startActivity(new Intent(AccountActivity.this, SecurityActivity.class));
                break;
            case R.id.llProfile:
                startActivityForResult(new Intent(AccountActivity.this, ProfileActivity.class)
                        .putExtra("bean", signupBean), PROFILE);
                finish();
                break;
            case R.id.llReminder:
                break;

        }
    }
}
