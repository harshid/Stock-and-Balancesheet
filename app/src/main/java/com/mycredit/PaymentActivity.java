package com.mycredit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bean.CustomerBean;
import bean.SignupBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;
import utils.Constants;
import utils.Permission;
import utils.Utils;

import static utils.Constants.FIREBASE_STORAGE_BASEURL;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.IS_REFRESH;
import static utils.Constants.LANGUAGE_DISABLE;
import static utils.Constants.MAKE_PAYMENT;
import static utils.Constants.TIME;

public class PaymentActivity extends BaseActivity implements SlyCalendarDialog.Callback {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtPay)
    TextView txtPay;
    @BindView(R.id.txtAmount)
    TextView txtAmount;
    @BindView(R.id.edtPayment)
    EditText edtPayment;
    @BindView(R.id.edtNote)
    EditText edtNote;
    @BindView(R.id.imgTransaction)
    ImageView imgTransaction;
    @BindView(R.id.rlImage)
    RelativeLayout rlImage;
    @BindView(R.id.rlNote)
    RelativeLayout rlNote;
    @BindView(R.id.llCalculator)
    LinearLayout llCalculator;
    @BindView(R.id.txtTotal)
    TextView txtTotal;
    @BindView(R.id.imgSelectImage)
    ImageView imgSelectImage;
    @BindView(R.id.llDate)
    LinearLayout llDate;
    @BindView(R.id.txtDate)
    TextView txtDate;
    @BindView(R.id.imgDeleteImage)
    ImageView imgDeleteImage;

    TransactionBean transactionBean;
    String payment, note, s = "";
    CustomerBean customerBean;

    FirebaseAuth mAuth;

    String last_time;
    File imageFile;
    String accept = "", time_date = "", key = "", isRefresh = "0";
    SqlLiteDbHelper sqlLiteDbHelper;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        sqlLiteDbHelper = new SqlLiteDbHelper(this);

        accept = getIntent().getStringExtra(Constants.ACCEPT);
        customerBean = (CustomerBean) getIntent().getSerializableExtra(Constants.CUSTOMER);
        last_time = getIntent().getStringExtra(TIME);

        txtTitle.setText(customerBean.name);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextColor(getResources().getColor(R.color.full_dark_gray));
        txtAmount.setText(getResources().getString(R.string.rupees_symbol) + customerBean.amount);
        txtAmount.setVisibility(View.VISIBLE);
        txtAmount.setTextColor(customerBean.type.equalsIgnoreCase(Constants.ADVANCE)
                ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
        txtAmount.setVisibility(View.VISIBLE);

        edtPayment.setFocusable(true);
        edtPayment.setInputType(InputType.TYPE_NULL);
        edtPayment.setTextIsSelectable(true);
        edtPayment.setTextColor(accept.equals("1") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));

        edtPayment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                llCalculator.setVisibility(View.VISIBLE);
                edtNote.clearFocus();
                edtNote.setCursorVisible(false);
                edtPayment.setCursorVisible(true);
                Utils.hideKeyBoard(edtPayment, PaymentActivity.this);
                return true;
            }
        });

        edtPayment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 1)
                    if (Character.isDigit(s.toString().charAt(s.toString().length() - 1))) {
                        perfomrCalculator(s.toString().replace("X", "*"), false, false);
                    }
                int visibility = s.toString().length() > 0 ? View.VISIBLE : View.GONE;
                edtNote.setVisibility(visibility);
                rlNote.setVisibility(visibility);
                imgSelectImage.setVisibility(visibility);
                llDate.setVisibility(visibility);
                txtPay.setVisibility(visibility);
                txtTotal.setVisibility(visibility);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edtNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                llCalculator.setVisibility(View.GONE);
                return false;
            }
        });

        Utils.setEngilsh(PaymentActivity.this);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm aa");
        time_date = sdf.format(c.getTime());
        txtDate.setText(time_date);
        Utils.setSavedLanguage(PaymentActivity.this);
    }

    void openDatePicker() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm aa");
        try {
            Date current = (Date) sdf.parse(sdf.format(c.getTime()));
            llCalculator.setVisibility(View.GONE);
            Utils.setEngilsh(PaymentActivity.this);
            new SlyCalendarDialog()
                    .setSingle(true)
                    .setStartDate(current)
                    .setCallback(this)
                    .show(getSupportFragmentManager(), "TAG_SLYCALENDAR");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.imgDeleteImage, R.id.llDate, R.id.txtPay, R.id.imgSelectImage, R.id.imgBack})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgDeleteImage:
                imageFile = null;
                imgDeleteImage.setVisibility(View.GONE);
                imgTransaction.setVisibility(View.GONE);
                rlImage.setVisibility(View.GONE);
                break;
            case R.id.llDate:
                openDatePicker();
                break;
            case R.id.txtPay:
                if (validate()) {
                    String text = edtPayment.getText().toString();
                    if (Character.isDigit(text.charAt(text.length() - 1))) {
                        s = "";
                        perfomrCalculator(text.replace("X", "*"), true, true);
                    }
                }
                break;
            case R.id.imgSelectImage:
                llCalculator.setVisibility(View.GONE);
                if (Permission.checkStoragePermission(PaymentActivity.this))
                    selectImage();
                break;
            case R.id.imgBack:
                finish();
                break;
        }
    }

    /* private void copyAndSaveInDataBase() {
        try {
            File destination = Utils.copyTransactionFile(PaymentActivity.this, imageFile, customerBean.number.replace("+", ""));
            transactionBean.image = destination.getAbsolutePath();
            isRefresh = "1";
            addPayment();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } */

    private void addPayment() {
        transactionBean.accept = accept;
        transactionBean.Amount = payment;
        transactionBean.note = note;
        transactionBean.time = time_date;
        transactionBean.deleted_on = "";

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm aa");
        String strDate = sdf.format(c.getTime());
        try {
            Date current = (Date) sdf.parse(strDate);
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
        float total = (accept.equals("1") ? amount + paymnet : amount - paymnet);

        customerBean.type = total > 0 ? Constants.ADVANCE : Constants.DUE;
        transactionBean.type = customerBean.type;

        String s = total + "";
        transactionBean.total = s.replace("-", "");
        customerBean.amount = s.replace("-", "");

        addTransactionData();
    }

    private void addTransactionData() {
        GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER).child(customerBean.number).setValue(customerBean);
        GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER).child(customerBean.number).setValue(customerBean);

        DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.TRANSACTION).child(customerBean.number);

        DatabaseReference backupReference = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.TRANSACTION).child(customerBean.number);

        key = reference.push().getKey();
        transactionBean.key = key;

        reference.child(key).setValue(transactionBean);
        backupReference.child(key).setValue(transactionBean);

        if (imageFile != null)
            try {
                File destination = Utils.copyTransactionFile(PaymentActivity.this, imageFile, key.replace("+", ""));
                transactionBean.image = destination.getAbsolutePath();
                isRefresh = "1";
            } catch (IOException e) {
                e.printStackTrace();
            }
        isRefresh = "1";

        sqlLiteDbHelper.insertTransactionData(customerBean, transactionBean);
        sqlLiteDbHelper.updateCustomerData(customerBean);

        SignupBean signupBean = sqlLiteDbHelper.getDataBean();
        String name = signupBean.fullname.length() > 0 ? signupBean.fullname : signupBean.phoneNo;
        if (customerBean.language != null && customerBean.language.length() > 0 && !customerBean.language.contains(LANGUAGE_DISABLE)) {
            Utils.setLanguage(PaymentActivity.this, customerBean.language);
            if (accept.equals("1")) {
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
            Utils.setSavedLanguage(PaymentActivity.this);
        }
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.IS_REFRESH, isRefresh);
        intent.putExtra(Constants.IMAGE_FILE, imageFile);
        intent.putExtra(Constants.KEY, key);
        intent.putExtra(Constants.TRANSACTION, transactionBean);
        setResult(MAKE_PAYMENT, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private boolean validate() {
        payment = edtPayment.getText().toString();
        note = edtNote.getText().toString();
        if (payment == null || !(payment.length() > 0)) {
            edtPayment.setError(getResources().getString(R.string.add_payment));
            return false;
        }
        return true;
    }

    private void selectImage() {
        ImagePicker.create(this)
                .folderMode(true)
                .toolbarFolderTitle("Folder")
                .toolbarImageTitle("Tap to select")
                .toolbarArrowColor(Color.WHITE)
                .includeVideo(false)
                .single()
                .limit(1)
                .showCamera(true)
                .imageDirectory("Camera")
                .enableLog(false)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            if (images.size() > 0) {
                String path = images.get(0).getPath();
                imageFile = new File(path);
                imgTransaction.setImageURI(Uri.parse(path));
                imgTransaction.setVisibility(View.VISIBLE);
                imgDeleteImage.setVisibility(View.VISIBLE);
                rlImage.setVisibility(View.VISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Utils.closeProgress();
        super.onDestroy();
    }

    @OnClick({R.id.txtOne, R.id.txtTwo, R.id.txtThird, R.id.txtBackPress, R.id.txtFour, R.id.txtFive, R.id.txtSix, R.id.txtMultiple, R.id.txtSeven, R.id.txtEight, R.id.txtNine, R.id.txtMinus, R.id.txtPoint, R.id.txtZero, R.id.txtEqual, R.id.txtPlus})
    public void onViewClick(View view) {
        String text = edtPayment.getText().toString();
        switch (view.getId()) {
            case R.id.txtBackPress:
                if (text.length() > 0) {
                    edtPayment.setText(text.substring(0, text.length() - 1));
                    text = edtPayment.getText().toString();
                    if (text.length() > 0) {
                        if (!Character.isDigit(text.charAt(text.length() - 1))) {
                            s = "";
                        }
                    }
                }
                break;
            case R.id.txtMultiple:
                intputSymbol(text, "X");
                break;
            case R.id.txtMinus:
                intputSymbol(text, "-");
                break;
            case R.id.txtPoint:
                if (text.length() > 0) {
                    if (Character.isDigit(text.charAt(text.length() - 1)) && (!s.contains(".")) && s.length() > 0) {
                        edtPayment.setText(text + ".");
                        s = s + ".";
                    }
                }
                break;
            case R.id.txtEqual:
                if (text.length() > 0)
                    if (Character.isDigit(text.charAt(text.length() - 1))) {
                        s = "";
                        perfomrCalculator(text.replace("X", "*"), true, false);
                    }
                break;
            case R.id.txtPlus:
                intputSymbol(text, "+");
                break;
            case R.id.txtZero:
                intputNumber(text, "0");
                break;
            case R.id.txtOne:
                intputNumber(text, "1");
                break;
            case R.id.txtTwo:
                intputNumber(text, "2");
                break;
            case R.id.txtThird:
                intputNumber(text, "3");
                break;
            case R.id.txtFour:
                intputNumber(text, "4");
                break;
            case R.id.txtFive:
                intputNumber(text, "5");
                break;
            case R.id.txtSix:
                intputNumber(text, "6");
                break;
            case R.id.txtSeven:
                intputNumber(text, "7");
                break;
            case R.id.txtEight:
                intputNumber(text, "8");
                break;
            case R.id.txtNine:
                intputNumber(text, "9");
                break;
        }
    }

    private void intputNumber(String text, String number) {
        if (s.contains(".")) {
            if (s.length() - s.indexOf(".") < 3) {
                edtPayment.setText(text + number);
                s = s + number;
            }
        } else {
            edtPayment.setText(text + number);
            s = s + number;
        }
    }

    private void intputSymbol(String text, String symbol) {
        if (text.length() > 0) {
            if (Character.isDigit(text.charAt(text.length() - 1))) {
                edtPayment.setText(text + symbol);
                s = "";
            }
        }
    }

    void perfomrCalculator(String text, boolean isFromEqual, boolean performTransaction) {
        try {
            Expression expression = new ExpressionBuilder(text).build();
            float result = (float) expression.evaluate();
            txtTotal.setText(String.format("%.2f", result));
            if (isFromEqual) {
                edtPayment.setText(String.format("%.2f", result));
                txtTotal.setText("");
                s = edtPayment.getText().toString();
                if (performTransaction) {
                    if (result == 0) {
                        edtPayment.setError(getResources().getString(R.string.transaction_error));
                    } else {
                        payment = edtPayment.getText().toString();
                        transactionBean = new TransactionBean();
                        transactionBean.image = "";
                        isRefresh = "1";
                        addPayment();
                    }
                }
            }
        } catch (ArithmeticException ex) {
        }
    }

    @Override
    public void onCancelled() {
    }

    @Override
    public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {
        firstDate.set(Calendar.HOUR_OF_DAY, hours);
        firstDate.set(Calendar.MINUTE, minutes);
        time_date = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa", Locale.getDefault()).format(firstDate.getTime());
        txtDate.setText(time_date);
        Utils.setSavedLanguage(PaymentActivity.this);
    }
}
