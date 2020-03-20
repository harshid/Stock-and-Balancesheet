package com.mycredit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import bean.CustomerBean;
import bean.SignupBean;
import bean.TransactionBean;
import database.SqlLiteDbHelper;
import utils.Constants;

import static utils.Constants.GET_REFERENCE;

/**
 * Created by mac on 03/07/19.
 */

public class SyncDataFromServerActivity extends BaseActivity {
    FirebaseAuth mAuth;
    SqlLiteDbHelper sqlLiteDbHelper;
    ArrayList<CustomerBean> allCustomers;
    SignupBean signupBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        signupBean = (SignupBean) getIntent().getSerializableExtra("signupBean");

        mAuth = FirebaseAuth.getInstance();
        sqlLiteDbHelper = new SqlLiteDbHelper(SyncDataFromServerActivity.this);

        syncDataFromServer(signupBean);
    }

    private void syncDataFromServer(final SignupBean signupBean) {
        sqlLiteDbHelper.insertInfoData(signupBean);
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allCustomers = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    CustomerBean customerBean = data.getValue(CustomerBean.class);
                    sqlLiteDbHelper.insertCustomerData(customerBean);
                    allCustomers.add(customerBean);
                }
                if (allCustomers.size() > 0) {
                    getRemainUplaodTransaction(0);
                } else {
                    openMainActivity(signupBean);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void openMainActivity(SignupBean signupBean) {
        startActivity(new Intent(SyncDataFromServerActivity.this, MainActivity.class).putExtra("bean", signupBean));
        finish();
    }

    private void getRemainUplaodTransaction(final int i) {
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.TRANSACTION).child(allCustomers.get(i).number);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    TransactionBean transactionBean = data.getValue(TransactionBean.class);
                    if (!sqlLiteDbHelper.isTransactionAvailable(allCustomers.get(i), transactionBean))
                        sqlLiteDbHelper.insertTransactionData(allCustomers.get(i), transactionBean);
                }
                if (allCustomers.size() > i + 1) {
                    getRemainUplaodTransaction(i + 1);
                } else {
                    openMainActivity(sqlLiteDbHelper.getDataBean());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
