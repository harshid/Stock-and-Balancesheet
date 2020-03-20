package com.mycredit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import bean.CustomerBean;
import bean.SignupBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import in.aabhasjindal.otptextview.OtpTextView;
import utils.Constants;
import utils.Utils;

import static utils.Constants.CUSTOMER;
import static utils.Constants.DELETE_TRANSACTION_REQUEST;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.IS_REFRESH;
import static utils.Constants.LANGUAGE_DISABLE;
import static utils.Constants.PASSWORD;
import static utils.Constants.PHONE_NUMBER;
import static utils.Constants.TRANSACTION;
import static utils.Constants.USER;

public class DeleteTransactionActivity extends BaseActivity {
    @BindView(R.id.otpView)
    OtpTextView otpView;
    @BindView(R.id.rlEnterPassword)
    RelativeLayout rlEnterPassword;
    @BindView(R.id.rlSetPassword)
    RelativeLayout rlSetPassword;
    @BindView(R.id.edtSetPassword)
    EditText edtSetPassword;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.imgTop)
    ImageView imgTop;
    @BindView(R.id.txtAmount)
    TextView txtAmount;
    @BindView(R.id.txtPaymentMessage)
    TextView txtPaymentMessage;

    TransactionBean transactionBean;
    CustomerBean customerBean;

    FirebaseAuth mAuth;
    FirebaseUser user;
    String isRefresh = "0", password = "";
    float delete_trans_amount;
    boolean isAfterUpdatedTransaction = false;
    SqlLiteDbHelper sqlLiteDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_transaction);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        sqlLiteDbHelper = new SqlLiteDbHelper(this);

        transactionBean = (TransactionBean) getIntent().getSerializableExtra("bean");
        customerBean = (CustomerBean) getIntent().getSerializableExtra("customer");

        txtTitle.setText(getResources().getString(R.string.delete_payment));
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextColor(getResources().getColor(R.color.dark_red));

        Log.d("==== Amount :- ", transactionBean.Amount);
        txtAmount.setText(getResources().getString(R.string.rupees_symbol) + transactionBean.Amount);
        txtAmount.setTextColor(transactionBean.accept.equals("1") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
        Log.d("==== Amount2 :- ", txtAmount.getText().toString());

        txtPaymentMessage.setText(transactionBean.accept.equals("1") ? getResources().getString(R.string.payment_received) : getResources().getString(R.string.payment_sent));
        imgTop.setImageDrawable(transactionBean.accept.equals("1") ? getResources().getDrawable(R.drawable.ic_accept_payment) : getResources().getDrawable(R.drawable.ic_give_payment));
        getPassword();
    }

    private void setPassword(String pass) {
        SignupBean signupBean = FastSave.getInstance().getObject(USER, SignupBean.class);
        signupBean.password = pass;
        sqlLiteDbHelper.updateInfoData(signupBean);

        DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
        reference.setValue(pass);

        DatabaseReference backUpReference = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
        backUpReference.setValue(pass);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                password = dataSnapshot.getValue(String.class);
                rlSetPassword.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getPassword() {
        password = sqlLiteDbHelper.getDataBean().password;
       /* DatabaseReference reference = database.getReference().child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                password = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        }); */
    }

    @OnClick({R.id.imgCloseEnterPassword, R.id.txtOk, R.id.txtSetPassword, R.id.imgClose, R.id.imgBack, R.id.cardDelete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgCloseEnterPassword:
                otpView.setOTP("");
                rlEnterPassword.setVisibility(View.GONE);
                break;
            case R.id.txtOk:
                if (otpView.getOTP().length() > 0) {
                    if (password.equals(otpView.getOTP()))
                        openDeleteTransactionDialog();
                    else
                        otpView.showError();
                } else otpView.showError();
                break;
            case R.id.txtSetPassword:
                String setPassword = edtSetPassword.getText().toString();
                if (setPassword.length() > 5)
                    setPassword(setPassword);
                else
                    edtSetPassword.setError(getResources().getString(R.string.enter_min_six_digit));
                break;
            case R.id.imgClose:
                rlSetPassword.setVisibility(View.GONE);
                break;
            case R.id.imgBack:
                onBackPressed();
                break;
            case R.id.cardDelete:
                // if (Utils.isNetworkAvailable(DeleteTransactionActivity.this))
                if (password != null && password.length() > 0) {
                    rlEnterPassword.setVisibility(View.VISIBLE);
                } else {
                    rlSetPassword.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void openDeleteTransactionDialog() {
        otpView.setOTP("");
        rlEnterPassword.setVisibility(View.GONE);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.delete_transaction_dialog));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteTransaction();
                    }
                });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteTransaction() {
        final DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.TRANSACTION).child(customerBean.number);
        final DatabaseReference backUpReference = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.TRANSACTION).child(customerBean.number);

        Utils.setEngilsh(DeleteTransactionActivity.this);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm aa");
        String strDate = sdf.format(c.getTime());
        Utils.setSavedLanguage(DeleteTransactionActivity.this);

        transactionBean.deleted_on = strDate;
        reference.child(transactionBean.key).setValue(transactionBean);
        backUpReference.child(transactionBean.key).setValue(transactionBean);

        delete_trans_amount = Float.parseFloat(transactionBean.accept.equalsIgnoreCase("1")
                ? "-" + transactionBean.Amount : transactionBean.Amount);

        float cust_amount = Float.parseFloat(customerBean.type.equalsIgnoreCase(Constants.DUE)
                ? "-" + customerBean.amount : customerBean.amount);
        float total = cust_amount + delete_trans_amount;

        customerBean.amount = (total + "").replace("-", "");
        customerBean.type = total > 0 ? Constants.ADVANCE : Constants.DUE;
        isRefresh = "1";
        sqlLiteDbHelper.updateCustomerData(customerBean);
        sqlLiteDbHelper.updateTransactionData(customerBean.number, transactionBean);

        ArrayList<TransactionBean> list = sqlLiteDbHelper.getTransactionData(customerBean.number);
        for (int i = 0; i < list.size(); i++) {
            TransactionBean trans = list.get(i);
            if (trans.key.equals(transactionBean.key)) {
                isAfterUpdatedTransaction = true;
            }
            if (isAfterUpdatedTransaction) {
                float current_trans_total = Float.parseFloat(trans.type.equalsIgnoreCase(Constants.DUE) ? "-" + trans.total : trans.total);
                float totalAmout = current_trans_total + delete_trans_amount;
                trans.total = (totalAmout + "").replace("-", "");
                trans.type = totalAmout > 0 ? Constants.ADVANCE : Constants.DUE;
                sqlLiteDbHelper.updateTransactionData(customerBean.number, trans);
            }
        }

        DatabaseReference databaseReference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER)
                .child(customerBean.number);
        databaseReference.setValue(customerBean);

        DatabaseReference databaseBackupReference = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER)
                .child(customerBean.number);
        databaseBackupReference.setValue(customerBean);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                customerBean = dataSnapshot.getValue(CustomerBean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    TransactionBean trans = data.getValue(TransactionBean.class);
                    if (trans.key.equals(transactionBean.key)) {
                        isAfterUpdatedTransaction = true;
                    }
                    if (isAfterUpdatedTransaction) {
                        float current_trans_total = Float.parseFloat(trans.type.equalsIgnoreCase(Constants.DUE)
                                ? "-" + trans.total : trans.total);
                        float total = current_trans_total + delete_trans_amount;
                        trans.total = (total + "").replace("-", "");
                        trans.type = total > 0 ? Constants.ADVANCE : Constants.DUE;
                        reference.child(trans.key).setValue(trans);
                        backUpReference.child(trans.key).setValue(trans);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        SignupBean signupBean = sqlLiteDbHelper.getDataBean();
        String name = signupBean.fullname.length() > 0 ? signupBean.fullname : signupBean.phoneNo;
        if (customerBean.language != null && customerBean.language.length() > 0 && !customerBean.language.contains(LANGUAGE_DISABLE)) {
            Utils.setLanguage(DeleteTransactionActivity.this, customerBean.language);
            if (transactionBean.type.equals(Constants.ADVANCE)) {
                Utils.sendMessage("+918780735806", getResources().getString(R.string.delete_advance_msg)
                        .replace("AMOUNT", transactionBean.Amount)
                        .replace("NAME", name)
                        .replace("TOTAL", customerBean.amount)
                        .replace("TYPE", transactionBean.type.equalsIgnoreCase(Constants.ADVANCE)
                                ? getResources().getString(R.string.advance) : getResources().getString(R.string.due)));
            } else {
                Utils.sendMessage("+918780735806", getResources().getString(R.string.delete_give_msg)
                        .replace("AMOUNT", transactionBean.Amount)
                        .replace("NAME", name)
                        .replace("TOTAL", customerBean.amount)
                        .replace("TYPE", transactionBean.type.equalsIgnoreCase(Constants.ADVANCE)
                                ? getResources().getString(R.string.advance) : getResources().getString(R.string.due)));
            }
            Utils.setSavedLanguage(DeleteTransactionActivity.this);
        }
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IS_REFRESH, isRefresh);
        intent.putExtra(TRANSACTION, transactionBean);
        intent.putExtra(CUSTOMER, customerBean);
        setResult(DELETE_TRANSACTION_REQUEST, intent);
        finish();
    }
}
