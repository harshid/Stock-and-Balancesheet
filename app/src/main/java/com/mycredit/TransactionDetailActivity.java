package com.mycredit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myandroid.views.MultiTouchListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import bean.CustomerBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import utils.Constants;
import utils.Utils;

import static utils.Constants.CUSTOMER;
import static utils.Constants.DELETE_TRANSACTION_REQUEST;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.IMAGE;
import static utils.Constants.IS_REFRESH;
import static utils.Constants.TRANSACTION;
import static utils.Constants.TRANSACTION_REQUEST;

public class TransactionDetailActivity extends BaseActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtAmount)
    TextView txtAmount;
    @BindView(R.id.txtNote)
    TextView txtNote;
    @BindView(R.id.imgTransaction)
    ImageView imgTransaction;
    @BindView(R.id.relFull)
    RelativeLayout relFull;
    @BindView(R.id.imgFull)
    ImageView imgFull;
    @BindView(R.id.txtFullDate)
    TextView txtFullDate;
    @BindView(R.id.txtDeletedOn)
    TextView txtDeletedOn;
    @BindView(R.id.llDeletedOn)
    LinearLayout llDeletedOn;
    @BindView(R.id.rlDeleteTransaction)
    RelativeLayout rlDeleteTransaction;
    @BindView(R.id.frameImage)
    FrameLayout frameImage;
    @BindView(R.id.rlDelete)
    RelativeLayout rlDelete;
    @BindView(R.id.llNote)
    LinearLayout llNote;
    @BindView(R.id.txtSync)
    TextView txtSync;

    TransactionBean transactionBean;
    CustomerBean customerBean;
    String isRefresh = "0";

    FirebaseAuth mAuth;
    FirebaseUser user;
    SqlLiteDbHelper sqlLiteDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        sqlLiteDbHelper = new SqlLiteDbHelper(this);

        transactionBean = (TransactionBean) getIntent().getSerializableExtra(Constants.TRANSACTION);
        customerBean = (CustomerBean) getIntent().getSerializableExtra(Constants.CUSTOMER);

        setData();
    }

    private void setData() {
        txtTitle.setText(customerBean.name);
        txtNote.setText(transactionBean.note);
        llNote.setVisibility(transactionBean.note.length() > 0 ? View.VISIBLE : View.GONE);
        txtFullDate.setText(getResources().getString(R.string.added_on) + " " + transactionBean.time);
        txtDeletedOn.setText(getResources().getString(R.string.deleted_on) + " " + transactionBean.deleted_on);

        txtSync.setText(transactionBean.is_sync.equals("0") ? getResources().getString(R.string.not_sync) : getResources().getString(R.string.sync_successfull));
        txtSync.setTextColor(transactionBean.is_sync.equals("0") ? getResources().getColor(R.color.red) : getResources().getColor(R.color.green));

        Log.d("==== Amount :- ", transactionBean.Amount);
        txtAmount.setText(getResources().getString(R.string.rupees_symbol) + transactionBean.Amount);
        txtAmount.setTextColor(transactionBean.accept.equalsIgnoreCase("1") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
        Log.d("==== from text :- ", txtAmount.getText().toString());

        llDeletedOn.setVisibility(transactionBean.deleted_on.length() > 0 ? View.VISIBLE : View.GONE);
        rlDeleteTransaction.setVisibility(transactionBean.deleted_on.length() > 0 ? View.GONE : View.VISIBLE);

        if (transactionBean.image.length() > 0) {
            if (Utils.isValidURL(transactionBean.image)) {
                Picasso.get().load(transactionBean.image).placeholder(R.drawable.image_placeholder).into(imgTransaction);
                Picasso.get().load(transactionBean.image).placeholder(R.drawable.image_placeholder).into(imgFull);
            } else {
                if (new File(transactionBean.image).exists() && new File(transactionBean.image).length() > 0) {
                    Picasso.get().load(new File(transactionBean.image)).placeholder(R.drawable.image_placeholder).into(imgTransaction);
                    Picasso.get().load(new File(transactionBean.image)).placeholder(R.drawable.image_placeholder).into(imgFull);
                } else {
                    getImageFromFirebase();
                }
            }
            frameImage.setVisibility(View.VISIBLE);
            imgFull.setOnTouchListener(new MultiTouchListener());
        }
    }

    private void getImageFromFirebase() {
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.TRANSACTION).child(customerBean.number).child(transactionBean.key).child(IMAGE);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profile_image = dataSnapshot.getValue(String.class);
                if (profile_image.length() > 0) {
                    if (Utils.isValidURL(profile_image)) {
                        Picasso.get().load(profile_image).placeholder(R.drawable.image_placeholder).into(imgFull);
                        Picasso.get().load(profile_image).placeholder(R.drawable.image_placeholder).into(imgTransaction);
                        transactionBean.image = profile_image;
                        sqlLiteDbHelper.updateTransactionData(customerBean.number, transactionBean);
                    } else {
                        Picasso.get().load(new File(profile_image)).placeholder(R.drawable.image_placeholder).into(imgTransaction);
                        Picasso.get().load(new File(profile_image)).placeholder(R.drawable.image_placeholder).into(imgFull);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @OnClick({R.id.imgClose, R.id.imgBack, R.id.rlShare, R.id.imgTransaction, R.id.rlDeleteTransaction})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rlDeleteTransaction:
                startActivityForResult(new Intent(TransactionDetailActivity.this, DeleteTransactionActivity.class)
                        .putExtra("customer", customerBean)
                        .putExtra("bean", transactionBean), DELETE_TRANSACTION_REQUEST);
                break;
            case R.id.imgClose:
                relFull.setVisibility(View.GONE);
                break;
            case R.id.imgTransaction:
                relFull.setVisibility(View.VISIBLE);
                break;
            case R.id.imgBack:
                if (rlDelete.isShown())
                    rlDelete.setVisibility(View.GONE);
                else
                    onBackPressed();
                break;
            case R.id.rlShare:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IS_REFRESH, isRefresh);
        setResult(TRANSACTION_REQUEST, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_TRANSACTION_REQUEST) {
            if (data != null && data.getStringExtra(IS_REFRESH).equals("1")) {
                transactionBean = (TransactionBean) data.getSerializableExtra(TRANSACTION);
                customerBean = (CustomerBean) data.getSerializableExtra(CUSTOMER);
                isRefresh = "1";
                onBackPressed();
            }
        }
    }
}
