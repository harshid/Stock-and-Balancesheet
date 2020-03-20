package com.mycredit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import bean.CustomerBean;
import bean.SignupBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import in.aabhasjindal.otptextview.OTPListener;
import in.aabhasjindal.otptextview.OtpTextView;
import utils.Constants;
import utils.Utils;

import static utils.Constants.CUSTOMER;
import static utils.Constants.DELETE_CUSTOMER_REQUEST;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.LANGUAGE;
import static utils.Constants.LANGUAGE_DISABLE;
import static utils.Constants.PASSWORD;
import static utils.Constants.TIME;
import static utils.Constants.USER;

/**
 * Created by mac on 25/06/19.
 */

public class DeleteCustomerActivity extends BaseActivity {
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
    @BindView(R.id.imgProfile)
    CircleImageView imgProfile;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtAmount)
    TextView txtAmount;
    @BindView(R.id.txtPaymentType)
    TextView txtPaymentType;
    @BindView(R.id.txtPerformCredit)
    TextView txtPerformCredit;
    @BindView(R.id.imgArrow)
    ImageView imgArrow;
    @BindView(R.id.cardPerfomAction)
    CardView cardPerfomAction;
    @BindView(R.id.cardDelete)
    CardView cardDelete;

    CustomerBean customerBean;
    FirebaseAuth mAuth;
    TransactionBean transactionBean;
    String last_time, isRefresh = "0", password = "";
    SqlLiteDbHelper sqlLiteDbHelper;
    ArrayList<TransactionBean> transactionBeans;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_customer);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        sqlLiteDbHelper = new SqlLiteDbHelper(this);
        transactionBeans = (ArrayList<TransactionBean>) getIntent().getSerializableExtra(Constants.TRANSACTION);
        customerBean = (CustomerBean) getIntent().getSerializableExtra(CUSTOMER);
        last_time = getIntent().getStringExtra(TIME);

        setData();
        getPassword();
    }

    private void setPassword(String pass) {
        SignupBean signupBean = FastSave.getInstance().getObject(USER, SignupBean.class);
        signupBean.password = pass;
        sqlLiteDbHelper.updateInfoData(signupBean);

        DatabaseReference backupReference = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
        backupReference.setValue(pass);

        DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
        reference.setValue(pass);
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
    /*  DatabaseReference reference = database.getReference().child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
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

    private void setData() {
        txtTitle.setText(getResources().getString(R.string.delete_customer));
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextColor(getResources().getColor(R.color.dark_red));
        txtAmount.setText(getResources().getString(R.string.rupees_symbol) + customerBean.amount);
        txtName.setText(customerBean.name);
        txtPaymentType.setText(customerBean.type.equalsIgnoreCase(Constants.ADVANCE) ? getResources().getString(R.string.advance) : getResources().getString(R.string.due));

        if (customerBean.profile.length() > 0)
            if (Utils.isValidURL(customerBean.profile))
                Picasso.get().load(customerBean.profile).placeholder(R.drawable.ic_profile).into(imgProfile);
            else
                Picasso.get().load(new File(customerBean.profile)).placeholder(R.drawable.ic_profile).into(imgProfile);

        String message = (customerBean.type.equalsIgnoreCase(Constants.ADVANCE) ? getResources().getString(R.string.give_payment) : getResources().getString(R.string.accept_payment_message)) + " ";
        txtPerformCredit.setText(FastSave.getInstance().getString(LANGUAGE, "en").equalsIgnoreCase(Constants.HINDI_CODE) ? getResources().getString(R.string.rupees_symbol) + customerBean.amount + " " + message : message + getResources().getString(R.string.rupees_symbol) + customerBean.amount);
        imgArrow.setRotation(customerBean.type.equalsIgnoreCase(Constants.ADVANCE) ? 180 : 0);
        cardDelete.setVisibility(Float.parseFloat(customerBean.amount) == 0 ? View.VISIBLE : View.GONE);
        cardPerfomAction.setVisibility(Float.parseFloat(customerBean.amount) == 0 ? View.GONE : View.VISIBLE);
    }

    @OnClick({R.id.imgCloseEnterPassword, R.id.txtOk, R.id.txtSetPassword, R.id.imgClose, R.id.imgBack, R.id.cardPerfomAction, R.id.cardDelete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgCloseEnterPassword:
                otpView.setOTP("");
                rlEnterPassword.setVisibility(View.GONE);
                break;
            case R.id.txtOk:
                if (otpView.getOTP().length() > 0) {
                    if (password.equals(otpView.getOTP()))
                        openDeleteCustomeDialog();
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
            case R.id.cardPerfomAction:
                // if (Utils.isNetworkAvailable(DeleteCustomerActivity.this))
                openPaymentDialog();
                break;
            case R.id.cardDelete:
                // if (Utils.isNetworkAvailable(DeleteCustomerActivity.this))
                if (password.length() > 0) {
                    rlEnterPassword.setVisibility(View.VISIBLE);
                } else {
                    rlSetPassword.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.IS_REFRESH, isRefresh);
        setResult(DELETE_CUSTOMER_REQUEST, intent);
        finish();
    }

    private void openDeleteCustomeDialog() {
        otpView.setOTP("");
        rlEnterPassword.setVisibility(View.GONE);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.delete_customer_dialog));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteCustomer();
                    }
                });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteCustomer() {
        sqlLiteDbHelper.deleteCustomerAndTransaction(customerBean);

        startActivity(new Intent(DeleteCustomerActivity.this, MainActivity.class)
                .putExtra("bean", FastSave.getInstance().getObject(USER, SignupBean.class))
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }


    private void openPaymentDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.make_payment_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText edtPayment = dialog.findViewById(R.id.edtPayment);
        final TextView txtMakePayment = dialog.findViewById(R.id.txtMakePayment);
        edtPayment.setText(customerBean.amount);
        txtMakePayment.setText(customerBean.type.equalsIgnoreCase(Constants.ADVANCE) ? getResources().getString(R.string.give) : getResources().getString(R.string.accept));

        txtMakePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtPayment.getText().toString().length() > 0) {
                    addPayment(edtPayment.getText().toString(), customerBean.type.equalsIgnoreCase(Constants.ADVANCE) ? "1" : "0");
                    dialog.dismiss();
                } else
                    edtPayment.setError(txtMakePayment.getText().toString());
            }
        });
        dialog.show();
    }

    private void addPayment(String payment, String accept) {
        Utils.setEngilsh(DeleteCustomerActivity.this);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm aa");
        String time_date = sdf.format(c.getTime());
        Utils.setSavedLanguage(DeleteCustomerActivity.this);

        transactionBean = new TransactionBean();
        transactionBean.accept = accept;
        transactionBean.Amount = payment;
        transactionBean.note = "";
        transactionBean.time = time_date;
        transactionBean.image = "";
        transactionBean.deleted_on = "";

        try {
            Date current = (Date) sdf.parse(time_date);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yy");
            String current_date = newFormat.format(current);
            transactionBean.trans_date = current_date;

            if (last_time.length() > 0) {
                Date last_transaction_date = newFormat.parse(last_time);
                String last_date = newFormat.format(last_transaction_date);
                if (last_date.equals(current_date)) {
                    transactionBean.date = "";
                } else {
                    transactionBean.date = current_date;
                }
            } else transactionBean.date = current_date;
        } catch (ParseException e) {
            e.printStackTrace();
            transactionBean.date = "";
        }

        float amount = Float.parseFloat(customerBean.type.equalsIgnoreCase(Constants.DUE) ? "-" + customerBean.amount : customerBean.amount);
        float paymnet = Float.parseFloat(payment);
        float total = (customerBean.type.equalsIgnoreCase(Constants.ADVANCE))
                ? amount - paymnet : amount + paymnet;

        customerBean.type = total > 0 ? Constants.ADVANCE : Constants.DUE;
        transactionBean.type = customerBean.type;

        String s = total + "";
        transactionBean.total = s.replace("-", "");
        customerBean.amount = s.replace("-", "");

        addTransactionData();
    }

    private void addTransactionData() {
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
                isRefresh = "1";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.TRANSACTION).child(customerBean.number);
        DatabaseReference backUpReference = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.TRANSACTION).child(customerBean.number);
        String key = reference.push().getKey();

        reference.child(key).setValue(transactionBean);
        backUpReference.child(key).setValue(transactionBean);

        transactionBean.key = key;
        sqlLiteDbHelper.updateCustomerData(customerBean);
        if (!sqlLiteDbHelper.isTransactionAvailable(customerBean, transactionBean))
            sqlLiteDbHelper.insertTransactionData(customerBean, transactionBean);
        customerBean = sqlLiteDbHelper.getCustomer(customerBean.number);

        SignupBean signupBean = sqlLiteDbHelper.getDataBean();
        String name = signupBean.fullname.length() > 0 ? signupBean.fullname : signupBean.phoneNo;
        if (customerBean.language != null && customerBean.language.length() > 0 && !customerBean.language.contains(LANGUAGE_DISABLE)) {
            Utils.setLanguage(DeleteCustomerActivity.this, customerBean.language);
            if (transactionBean.accept.equals("1")) {
                Utils.sendMessage("+918780735806", getResources().getString(R.string.accept_payment_msg)
                        .replace("AMOUNT", transactionBean.Amount)
                        .replace("NAME", name)
                        .replace("NUMBER", signupBean.phoneNo)
                        .replace("TOTAL", transactionBean.total)
                        .replace("TYPE", transactionBean.type.equalsIgnoreCase(Constants.ADVANCE) ? getResources().getString(R.string.advance) : getResources().getString(R.string.due)));
            } else {
                Utils.sendMessage("+918780735806", getResources().getString(R.string.give_payment_msg)
                        .replace("AMOUNT", transactionBean.Amount)
                        .replace("NAME", name)
                        .replace("NUMBER", signupBean.phoneNo)
                        .replace("TOTAL", transactionBean.total)
                        .replace("TYPE", transactionBean.type.equalsIgnoreCase(Constants.ADVANCE) ? getResources().getString(R.string.advance) : getResources().getString(R.string.due)));
            }
            Utils.setSavedLanguage(DeleteCustomerActivity.this);
        }
        setData();
    }
}
